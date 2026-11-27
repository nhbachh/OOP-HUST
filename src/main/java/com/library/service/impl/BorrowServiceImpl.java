package com.library.service.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.BookDAO;
import com.library.dao.BorrowRecordDAO;
import com.library.dao.UserDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.BorrowRecordDAOImpl;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.BorrowException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.BorrowService;
import com.library.service.FineService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Cài đặt lớp dịch vụ BorrowService xử lý các giao dịch mượn/trả sách.
 * Quản lý giao dịch JDBC thủ công (conn.setAutoCommit(false), conn.commit(), conn.rollback())
 * đảm bảo trọn vẹn 4 tính chất ACID trong cơ sở dữ liệu.
 */
public class BorrowServiceImpl implements BorrowService {

    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final FineService fineService;

    public BorrowServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.borrowRecordDAO = new BorrowRecordDAOImpl();
        this.fineService = new FineServiceImpl();
    }

    public BorrowServiceImpl(UserDAO userDAO, BookDAO bookDAO, BorrowRecordDAO borrowRecordDAO, FineService fineService) {
        this.userDAO = userDAO;
        this.bookDAO = bookDAO;
        this.borrowRecordDAO = borrowRecordDAO;
        this.fineService = fineService;
    }

    @Override
    public BorrowRecord borrowBook(int memberId, int bookId, Integer staffId, int borrowDays) {
        return executeBorrow(memberId, bookId, staffId, borrowDays, false);
    }

    @Override
    public BorrowRecord borrowBookWithSimulatedFailure(int memberId, int bookId, Integer staffId, int borrowDays) {
        return executeBorrow(memberId, bookId, staffId, borrowDays, true);
    }

    private BorrowRecord executeBorrow(int memberId, int bookId, Integer staffId, int borrowDays, boolean simulateFailure) {
        // 1. Kiểm tra tính hợp lệ của tham số
        if (borrowDays <= 0) {
            throw new ValidationException("Thời hạn mượn sách phải lớn hơn 0 ngày.");
        }

        // 2. Kiểm tra thông tin độc giả
        User user = userDAO.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Độc giả", memberId));

        if (!(user instanceof Member member)) {
            throw new ValidationException("Người dùng ID " + memberId + " không có vai trò Độc giả (MEMBER).");
        }

        if (member.getStatus() == UserStatus.LOCKED) {
            throw new BorrowException("Tài khoản của độc giả '" + member.getFullName() + "' đang bị KHÓA. Không thể mượn sách!");
        }

        if (!member.canBorrow()) {
            throw new BorrowException("Độc giả '" + member.getFullName() + "' đã đạt giới hạn mượn tối đa (" +
                    member.getMaxBorrowLimit() + " cuốn). Vui lòng trả sách trước khi mượn tiếp!");
        }

        // 3. Kiểm tra tiền phạt chưa nộp
        if (fineService.hasUnpaidFines(memberId)) {
            throw new BorrowException("Độc giả '" + member.getFullName() + "' hiện đang có khoản tiền phạt chưa thanh toán. " +
                    "Vui lòng thanh toán tiền phạt trước khi thực hiện mượn sách!");
        }

        // 4. Kiểm tra sách và số lượng tồn kho khả dụng
        Book book = bookDAO.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Sách", bookId));

        if (!book.isAvailable() || book.getAvailableCopies() <= 0) {
            throw new BorrowException("Đầu sách '" + book.getTitle() + "' hiện đã HẾT bản sao khả dụng để mượn!");
        }

        // 5. Kiểm tra nhân viên xử lý nếu có truyền vào
        if (staffId != null) {
            userDAO.findById(staffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nhân viên xử lý", staffId));
        }

        // 6. Bắt đầu Giao dịch CSDL (Transaction ACID)
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(borrowDays);
        int generatedRecordId;

        String updateBookSql = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";
        String insertRecordSql = "INSERT INTO borrow_records (user_id, book_id, staff_id, borrow_date, due_date, status) " +
                                 "VALUES (?, ?, ?, ?, ?, 'BORROWED')";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try {
                // Thao tác 1: Trừ 1 cuốn sách khả dụng
                try (PreparedStatement psBook = conn.prepareStatement(updateBookSql)) {
                    psBook.setInt(1, bookId);
                    int affected = psBook.executeUpdate();
                    if (affected == 0) {
                        throw new BorrowException("Sách vừa hết bản sao khả dụng trong quá trình xử lý giao dịch.");
                    }
                }

                // Nếu kích hoạt chế độ giả lập sự cố -> ném ngoại lệ kiểm thử Rollback
                if (simulateFailure) {
                    throw new RuntimeException("Sự cố hệ thống giả lập phát sinh sau khi trừ sách, kích hoạt conn.rollback()!");
                }

                // Thao tác 2: Tạo bản ghi phiếu mượn
                try (PreparedStatement psRecord = conn.prepareStatement(insertRecordSql, Statement.RETURN_GENERATED_KEYS)) {
                    psRecord.setInt(1, memberId);
                    psRecord.setInt(2, bookId);
                    if (staffId != null) {
                        psRecord.setInt(3, staffId);
                    } else {
                        psRecord.setNull(3, Types.INTEGER);
                    }
                    psRecord.setDate(4, Date.valueOf(borrowDate));
                    psRecord.setDate(5, Date.valueOf(dueDate));

                    int affected = psRecord.executeUpdate();
                    if (affected == 0) {
                        throw new BorrowException("Không thể tạo bản ghi phiếu mượn mới.");
                    }

                    try (ResultSet keys = psRecord.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedRecordId = keys.getInt(1);
                        } else {
                            throw new BorrowException("Không nhận được mã ID tự sinh của phiếu mượn.");
                        }
                    }
                }

                // Cam kết giao dịch thành công (Commit)
                conn.commit();

                return new BorrowRecord(generatedRecordId, memberId, bookId, staffId,
                        borrowDate, dueDate, null, BorrowStatus.BORROWED);

            } catch (Exception e) {
                // Hoàn nguyên toàn bộ dữ liệu (Rollback)
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[BorrowServiceImpl] Lỗi khi thực hiện rollback: " + rollbackEx.getMessage());
                }

                if (e instanceof BorrowException be) {
                    throw be;
                }
                throw new BorrowException("Giao dịch mượn sách bị hủy và hoàn nguyên: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new LibraryException("Lỗi kết nối cơ sở dữ liệu khi thực hiện mượn sách: " + e.getMessage(), e);
        }
    }

    @Override
    public BorrowRecord returnBook(int recordId) {
        BorrowRecord record = getBorrowRecordById(recordId);

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BorrowException("Phiếu mượn #" + recordId + " đã được hoàn trả trước đó rồi.");
        }

        LocalDate returnDate = LocalDate.now();
        boolean isOverdue = returnDate.isAfter(record.getDueDate());
        long overdueDays = isOverdue ? ChronoUnit.DAYS.between(record.getDueDate(), returnDate) : 0;
        BigDecimal fineAmount = isOverdue ? FineService.DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays)) : BigDecimal.ZERO;

        String updateRecordSql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED' WHERE record_id = ?";
        String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
        String findFineSql = "SELECT fine_id, status FROM fines WHERE record_id = ? LIMIT 1";
        String updateFineSql = "UPDATE fines SET overdue_days = ?, fine_amount = ?, reason = ? WHERE fine_id = ?";
        String insertFineSql = "INSERT INTO fines (record_id, user_id, overdue_days, fine_amount, reason, status, created_at) " +
                               "VALUES (?, ?, ?, ?, ?, 'UNPAID', NOW())";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try {
                // Thao tác 1: Cập nhật phiếu mượn sang RETURNED
                try (PreparedStatement psRecord = conn.prepareStatement(updateRecordSql)) {
                    psRecord.setDate(1, Date.valueOf(returnDate));
                    psRecord.setInt(2, recordId);
                    int affected = psRecord.executeUpdate();
                    if (affected == 0) {
                        throw new BorrowException("Không thể cập nhật trạng thái phiếu mượn #" + recordId);
                    }
                }

                // Thao tác 2: Hoàn trả +1 bản sao khả dụng cho sách
                try (PreparedStatement psBook = conn.prepareStatement(updateBookSql)) {
                    psBook.setInt(1, record.getBookId());
                    int affected = psBook.executeUpdate();
                    if (affected == 0) {
                        throw new BorrowException("Không thể tăng số lượng sách khả dụng của sách ID " + record.getBookId());
                    }
                }

                // Thao tác 3: Tự động tạo hoặc cập nhật vé phạt nếu quá hạn (tránh tạo trùng)
                if (isOverdue && fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                    String reason = String.format("Quá hạn trả sách %d ngày (Mức phạt: %s VNĐ/ngày)",
                            overdueDays, FineService.DAILY_FINE_RATE);

                    Integer existingFineId = null;
                    String existingStatus = null;
                    try (PreparedStatement psFind = conn.prepareStatement(findFineSql)) {
                        psFind.setInt(1, recordId);
                        try (ResultSet rs = psFind.executeQuery()) {
                            if (rs.next()) {
                                existingFineId = rs.getInt("fine_id");
                                existingStatus = rs.getString("status");
                            }
                        }
                    }

                    if (existingFineId == null) {
                        try (PreparedStatement psFine = conn.prepareStatement(insertFineSql)) {
                            psFine.setInt(1, recordId);
                            psFine.setInt(2, record.getUserId());
                            psFine.setInt(3, (int) overdueDays);
                            psFine.setBigDecimal(4, fineAmount);
                            psFine.setString(5, reason);
                            psFine.executeUpdate();
                        }
                    } else if ("UNPAID".equalsIgnoreCase(existingStatus)) {
                        try (PreparedStatement psUpdateFine = conn.prepareStatement(updateFineSql)) {
                            psUpdateFine.setInt(1, (int) overdueDays);
                            psUpdateFine.setBigDecimal(2, fineAmount);
                            psUpdateFine.setString(3, reason);
                            psUpdateFine.setInt(4, existingFineId);
                            psUpdateFine.executeUpdate();
                        }
                    }
                }

                // Cam kết giao dịch (Commit)
                conn.commit();

                record.setReturnDate(returnDate);
                record.setStatus(BorrowStatus.RETURNED);
                return record;

            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[BorrowServiceImpl] Lỗi khi thực hiện rollback trả sách: " + rollbackEx.getMessage());
                }

                if (e instanceof BorrowException be) {
                    throw be;
                }
                throw new BorrowException("Giao dịch trả sách bị hủy và hoàn nguyên: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new LibraryException("Lỗi kết nối cơ sở dữ liệu khi thực hiện trả sách: " + e.getMessage(), e);
        }
    }

    @Override
    public BorrowRecord getBorrowRecordById(int recordId) {
        return borrowRecordDAO.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu mượn", recordId));
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByUserId(int userId) {
        return borrowRecordDAO.findByUserId(userId);
    }

    @Override
    public List<BorrowRecord> getOverdueBorrowRecords() {
        return borrowRecordDAO.findOverdueRecords();
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordDAO.findAll();
    }
}
