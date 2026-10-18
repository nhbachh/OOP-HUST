package com.library.model;

/**
 * Trạng thái thanh toán của phiếu phạt quá hạn:
 * - UNPAID: Chưa nộp phạt.
 * - PAID: Đã hoàn tất nộp phạt.
 */
public enum FineStatus {
    UNPAID("Chưa đóng"),
    PAID("Đã đóng");

    private final String description;

    FineStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
