package com.library.model;

import java.time.LocalDateTime;

/**
 * Lớp trừu tượng cơ sở đại diện cho người dùng trong hệ thống (User).
 * Áp dụng mô hình Single Table Inheritance (STI) tương ứng với bảng 'users'.
 * Đảm bảo tính đóng gói (Encapsulation) và trừu tượng (Abstraction).
 */
public abstract class User {

    private int userId;
    private String userCode;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    /**
     * Constructor mặc định không tham số (No-arg constructor).
     */
    public User() {
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor hỗ trợ tạo đối tượng mới chưa có ID và thời gian tạo từ CSDL.
     */
    public User(String userCode, String username, String passwordHash,
                String fullName, String email, String phone,
                Role role, UserStatus status) {
        this();
        setUserCode(userCode);
        this.username = username;
        this.passwordHash = passwordHash;
        setFullName(fullName);
        this.email = email;
        this.phone = phone;
        setRole(role);
        if (status != null) {
            this.status = status;
        }
    }

    /**
     * Constructor đầy đủ tham số (Full-arg constructor).
     */
    public User(int userId, String userCode, String username, String passwordHash,
                String fullName, String email, String phone,
                Role role, UserStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        setUserCode(userCode);
        this.username = username;
        this.passwordHash = passwordHash;
        setFullName(fullName);
        this.email = email;
        this.phone = phone;
        setRole(role);
        setStatus(status);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Phương thức trừu tượng trả về mô tả chi tiết vai trò của người dùng.
     * Thể hiện tính Đa hình (Polymorphism) tại thời điểm thực thi.
     *
     * @return Chuỗi mô tả vai trò
     */
    public abstract String getRoleDescription();

    // ================= Getters & Setters với kiểm tra hợp lệ =================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng (userCode) không được để trống.");
        }
        this.userCode = userCode.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên (fullName) không được để trống.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Vai trò (role) không được để trống (null).");
        }
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái (status) không được để trống (null).");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userCode='" + userCode + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", roleDescription='" + getRoleDescription() + '\'' +
                '}';
    }
}
