package com.library.model;

/**
 * Trạng thái của phiếu mượn sách:
 * - BORROWED: Đang trong thời hạn mượn.
 * - RETURNED: Đã hoàn trả sách về kho.
 * - OVERDUE: Đã quá hạn trả sách.
 */
public enum BorrowStatus {
    BORROWED("Đang mượn"),
    RETURNED("Đã trả"),
    OVERDUE("Quá hạn");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
