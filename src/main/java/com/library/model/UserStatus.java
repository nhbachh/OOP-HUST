package com.library.model;

/**
 * Trạng thái tài khoản người dùng:
 * - ACTIVE: Đang hoạt động bình thường.
 * - LOCKED: Bị khóa (không thể mượn sách hoặc đăng nhập).
 */
public enum UserStatus {
    ACTIVE("Hoạt động"),
    LOCKED("Bị khóa");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
