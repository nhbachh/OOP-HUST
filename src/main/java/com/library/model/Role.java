package com.library.model;

/**
 * Phân quyền người dùng trong hệ thống:
 * - ADMIN: Quản trị viên toàn quyền hệ thống.
 * - LIBRARIAN: Thủ thư quản lý sách, độc giả và giao dịch mượn trả.
 * - MEMBER: Độc giả thư viện.
 */
public enum Role {
    ADMIN("Quản trị viên"),
    LIBRARIAN("Thủ thư"),
    MEMBER("Độc giả");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
