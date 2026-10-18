package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp thực thể đại diện cho Phiếu phạt quá hạn (Fine).
 * Ánh xạ với bảng 'fines' trong cơ sở dữ liệu.
 */
public class Fine {

    private int fineId;
    private int recordId;
    private int userId;
    private int overdueDays;
    private BigDecimal fineAmount;
    private String reason;
    private FineStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    /**
     * Constructor mặc định không tham số.
     */
    public Fine() {
        this.fineAmount = BigDecimal.ZERO;
        this.status = FineStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor tạo phiếu phạt mới (chưa có ID từ CSDL).
     */
    public Fine(int recordId, int userId, int overdueDays,
                BigDecimal fineAmount, String reason) {
        this();
        setRecordId(recordId);
        setUserId(userId);
        setOverdueDays(overdueDays);
        setFineAmount(fineAmount);
        this.reason = reason;
        this.status = FineStatus.UNPAID;
        this.paidAt = null;
    }

    /**
     * Constructor đầy đủ tham số.
     */
    public Fine(int fineId, int recordId, int userId, int overdueDays,
                BigDecimal fineAmount, String reason, FineStatus status,
                LocalDateTime createdAt, LocalDateTime paidAt) {
        this.fineId = fineId;
        setRecordId(recordId);
        setUserId(userId);
        setOverdueDays(overdueDays);
        setFineAmount(fineAmount);
        this.reason = reason;
        setStatus(status);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.paidAt = paidAt;
    }

    /**
     * Đánh dấu phiếu phạt đã thanh toán thành công.
     */
    public void markAsPaid() {
        this.status = FineStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    // ================= Getters & Setters =================

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        if (recordId <= 0) {
            throw new IllegalArgumentException("Mã phiếu mượn (recordId) phải là số dương.");
        }
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

    public int getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(int overdueDays) {
        if (overdueDays < 0) {
            throw new IllegalArgumentException("Số ngày quá hạn (overdueDays) không được âm.");
        }
        this.overdueDays = overdueDays;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        if (fineAmount == null) {
            throw new IllegalArgumentException("Số tiền phạt không được để trống (null).");
        }
        if (fineAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền phạt không được âm.");
        }
        this.fineAmount = fineAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : null;
    }

    public FineStatus getStatus() {
        return status;
    }

    public void setStatus(FineStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái nộp phạt không được để trống (null).");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public String toString() {
        return "Fine{" +
                "fineId=" + fineId +
                ", recordId=" + recordId +
                ", userId=" + userId +
                ", overdueDays=" + overdueDays +
                ", fineAmount=" + fineAmount +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", paidAt=" + paidAt +
                '}';
    }
}
