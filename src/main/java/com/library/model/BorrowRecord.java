package com.library.model;

import java.time.LocalDate;

/**
 * Lớp thực thể đại diện cho Phiếu mượn / trả sách (BorrowRecord).
 * Ánh xạ với bảng 'borrow_records' trong cơ sở dữ liệu.
 */
public class BorrowRecord {

    private int recordId;
    private int userId;
    private int bookId;
    private Integer staffId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;

    /**
     * Constructor mặc định không tham số.
     */
    public BorrowRecord() {
        this.borrowDate = LocalDate.now();
        this.status = BorrowStatus.BORROWED;
    }

    /**
     * Constructor tạo phiếu mượn mới (chưa có ID từ CSDL).
     */
    public BorrowRecord(int userId, int bookId, Integer staffId,
                        LocalDate borrowDate, LocalDate dueDate) {
        setUserId(userId);
        setBookId(bookId);
        this.staffId = staffId;
        setDates(borrowDate, dueDate);
        this.returnDate = null;
        this.status = BorrowStatus.BORROWED;
    }

    /**
     * Constructor đầy đủ tham số.
     */
    public BorrowRecord(int recordId, int userId, int bookId, Integer staffId,
                        LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                        BorrowStatus status) {
        this.recordId = recordId;
        setUserId(userId);
        setBookId(bookId);
        this.staffId = staffId;
        setDates(borrowDate, dueDate);
        this.returnDate = returnDate;
        setStatus(status);
    }

    /**
     * Thiết lập và xác thực ngày mượn và ngày hẹn trả.
     */
    public void setDates(LocalDate borrowDate, LocalDate dueDate) {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Ngày mượn sách không được để trống.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Hạn trả sách (dueDate) không được để trống.");
        }
        if (dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Hạn trả sách không thể trước ngày mượn sách.");
        }
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    // ================= Getters & Setters =================

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Mã người dùng (userId) phải là số dương.");
        }
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Mã sách (bookId) phải là số dương.");
        }
        this.bookId = bookId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Ngày mượn sách không được để trống.");
        }
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Hạn trả sách không được để trống.");
        }
        if (this.borrowDate != null && dueDate.isBefore(this.borrowDate)) {
            throw new IllegalArgumentException("Hạn trả sách không thể trước ngày mượn sách.");
        }
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái mượn trả không được để trống (null).");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", staffId=" + staffId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }
}
