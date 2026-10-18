package com.library.service.impl;

import com.library.dao.FineDAO;
import com.library.dao.impl.FineDAOImpl;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.service.FineService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cài đặt lớp dịch vụ FineService.
 */
public class FineServiceImpl implements FineService {

    private final FineDAO fineDAO;

    public FineServiceImpl() {
        this.fineDAO = new FineDAOImpl();
    }

    public FineServiceImpl(FineDAO fineDAO) {
        this.fineDAO = fineDAO;
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
        List<Fine> fines = fineDAO.findByUserId(userId);
        return fines.stream().anyMatch(f -> f.getStatus() == FineStatus.UNPAID);
    }

    @Override
    public List<Fine> getFinesByUserId(int userId) {
        return fineDAO.findByUserId(userId);
    }

    @Override
    public List<Fine> getUnpaidFines() {
        return fineDAO.findByStatus(FineStatus.UNPAID);
    }

    @Override
    public Fine getFineById(int fineId) {
        return fineDAO.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu phạt", fineId));
    }

    @Override
    public Fine getFineByRecordId(int recordId) {
        return fineDAO.findByRecordId(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu phạt gắn với phiếu mượn", recordId));
    }

    @Override
    public List<Fine> getAllFines() {
        return fineDAO.findAll();
    }
}
