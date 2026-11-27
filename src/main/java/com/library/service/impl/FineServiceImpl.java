package com.library.service.impl;

import com.library.dao.BorrowRecordDAO;
import com.library.dao.FineDAO;
import com.library.dao.impl.BorrowRecordDAOImpl;
import com.library.dao.impl.FineDAOImpl;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.service.FineService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt lớp dịch vụ FineService.
 */
public class FineServiceImpl implements FineService {

    private final FineDAO fineDAO;
    private final BorrowRecordDAO borrowRecordDAO;

    public FineServiceImpl() {
        this.fineDAO = new FineDAOImpl();
        this.borrowRecordDAO = new BorrowRecordDAOImpl();
    }

    public FineServiceImpl(FineDAO fineDAO) {
        this.fineDAO = fineDAO;
        this.borrowRecordDAO = null;
    }

    public FineServiceImpl(FineDAO fineDAO, BorrowRecordDAO borrowRecordDAO) {
        this.fineDAO = fineDAO;
        this.borrowRecordDAO = borrowRecordDAO;
    }

    /**
     * Đồng bộ tự động phiếu phạt cho các phiếu mượn đang quá hạn nhưng chưa trả sách.
     * - Nếu phiếu mượn quá hạn chưa có vé phạt -> tự động tạo vé phạt UNPAID.
     * - Nếu đã có vé phạt UNPAID và số ngày trễ tăng thêm -> tự động cập nhật lại số ngày & tiền phạt.
     * - Nếu vé phạt đã thanh toán (PAID) -> giữ nguyên lịch sử thanh toán.
     */
    private void syncOverdueFines() {
        if (borrowRecordDAO == null) {
            return;
        }
        try {
            List<BorrowRecord> overdueRecords = borrowRecordDAO.findOverdueRecords();
            LocalDate today = LocalDate.now();
            for (BorrowRecord record : overdueRecords) {
                if (record.getDueDate() == null || !today.isAfter(record.getDueDate())) {
                    continue;
                }
                int overdueDays = (int) ChronoUnit.DAYS.between(record.getDueDate(), today);
                BigDecimal fineAmount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));
                String reason = String.format("Quá hạn trả sách %d ngày (Mức phạt: %s VNĐ/ngày)",
                        overdueDays, DAILY_FINE_RATE);

                Optional<Fine> existingOpt = fineDAO.findByRecordId(record.getRecordId());
                if (existingOpt.isEmpty()) {
                    Fine newFine = new Fine(record.getRecordId(), record.getUserId(), overdueDays, fineAmount, reason);
                    fineDAO.insert(newFine);
                } else {
                    Fine existing = existingOpt.get();
                    if (existing.getStatus() == FineStatus.UNPAID && existing.getOverdueDays() != overdueDays) {
                        existing.setOverdueDays(overdueDays);
                        existing.setFineAmount(fineAmount);
                        existing.setReason(reason);
                        fineDAO.update(existing);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FineServiceImpl.syncOverdueFines] Lỗi đồng bộ phiếu phạt quá hạn: " + e.getMessage());
        }
    }

    @Override
    public Fine createFine(int recordId, int userId, int overdueDays, String reason) {
        if (overdueDays < 0) {
            throw new ValidationException("Số ngày quá hạn không được âm.");
        }

        BigDecimal fineAmount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));
        Fine fine = new Fine(recordId, userId, overdueDays, fineAmount, reason);

        boolean success = fineDAO.insert(fine);
        if (!success) {
            throw new LibraryException("Lỗi hệ thống khi khởi tạo phiếu phạt cho phiếu mượn #" + recordId);
        }
        return fine;
    }

    @Override
    public boolean payFine(int fineId) {
        Fine fine = getFineById(fineId);
        if (fine.getStatus() == FineStatus.PAID) {
            throw new ValidationException("Phiếu phạt #" + fineId + " đã được thanh toán trước đó.");
        }

        fine.markAsPaid();
        return fineDAO.update(fine);
    }

    @Override
    public boolean hasUnpaidFines(int userId) {
        syncOverdueFines();
        List<Fine> fines = fineDAO.findByUserId(userId);
        return fines.stream().anyMatch(f -> f.getStatus() == FineStatus.UNPAID);
    }

    @Override
    public List<Fine> getFinesByUserId(int userId) {
        syncOverdueFines();
        return fineDAO.findByUserId(userId);
    }

    @Override
    public List<Fine> getUnpaidFines() {
        syncOverdueFines();
        return fineDAO.findByStatus(FineStatus.UNPAID);
    }

    @Override
    public Fine getFineById(int fineId) {
        return fineDAO.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu phạt", fineId));
    }

    @Override
    public Fine getFineByRecordId(int recordId) {
        syncOverdueFines();
        return fineDAO.findByRecordId(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu phạt gắn với phiếu mượn", recordId));
    }

    @Override
    public List<Fine> getAllFines() {
        syncOverdueFines();
        return fineDAO.findAll();
    }
}
