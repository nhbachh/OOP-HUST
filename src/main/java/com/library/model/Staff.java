package com.library.model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho Nhân viên thư viện và Quản trị viên (Staff).
 * Kế thừa từ lớp trừu tượng User (Inheritance) và ghi đè phương thức getRoleDescription() (Polymorphism).
 */
public class Staff extends User {

    private String department;

    /**
     * Constructor mặc định (No-arg constructor).
     */
    public Staff() {
        super();
        setRole(Role.LIBRARIAN);
    }

    /**
     * Constructor tạo nhân viên mới thuận tiện (chưa có ID từ CSDL).
     */
    public Staff(String userCode, String username, String passwordHash,
                 String fullName, String email, String phone,
                 Role role, String department) {
        super(userCode, username, passwordHash, fullName, email, phone,
                (role == Role.ADMIN || role == Role.LIBRARIAN) ? role : Role.LIBRARIAN,
                UserStatus.ACTIVE);
        this.department = department;
    }

    /**
     * Constructor đầy đủ tham số (Full-arg constructor).
     */
    public Staff(int userId, String userCode, String username, String passwordHash,
                 String fullName, String email, String phone,
                 Role role, UserStatus status, LocalDateTime createdAt,
                 String department) {
        super(userId, userCode, username, passwordHash, fullName, email, phone,
                (role == Role.ADMIN || role == Role.LIBRARIAN) ? role : Role.LIBRARIAN,
                status, createdAt);
        this.department = department;
    }

    /**
     * Ghi đè phương thức trừu tượng từ lớp User (Đa hình - Polymorphism).
     * Trả về "Quản trị viên" nếu vai trò là ADMIN, hoặc "Nhân viên thư viện" nếu là LIBRARIAN.
     *
     * @return Chuỗi mô tả vai trò cụ thể
     */
    @Override
    public String getRoleDescription() {
        if (getRole() == Role.ADMIN) {
            return "Quản trị viên";
        }
        return "Nhân viên thư viện";
    }

    // ================= Getters & Setters =================

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = (department != null && !department.trim().isEmpty()) ? department.trim() : null;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "userId=" + getUserId() +
                ", userCode='" + getUserCode() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", role=" + getRole() +
                ", status=" + getStatus() +
                ", department='" + department + '\'' +
                ", roleDescription='" + getRoleDescription() + '\'' +
                '}';
    }
}
