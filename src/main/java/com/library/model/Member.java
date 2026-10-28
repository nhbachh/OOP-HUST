package com.library.model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho Độc giả thư viện (Member).
 * Kế thừa từ lớp trừu tượng User (Inheritance) và ghi đè phương thức getRoleDescription() (Polymorphism).
 */
public class Member extends User {

    public static final int DEFAULT_MAX_BORROW_LIMIT = 5;

    private int maxBorrowLimit = DEFAULT_MAX_BORROW_LIMIT;
    private int currentBorrowCount = 0;

    /**
     * Constructor mặc định (No-arg constructor).
     */
    public Member() {
        super();
        setRole(Role.MEMBER);
    }

    /**
     * Constructor tạo độc giả mới thuận tiện (chưa có ID từ CSDL).
     */
    public Member(String userCode, String fullName, String email, String phone) {
        super(userCode, null, null, fullName, email, phone, Role.MEMBER, UserStatus.ACTIVE);
        this.maxBorrowLimit = DEFAULT_MAX_BORROW_LIMIT;
        this.currentBorrowCount = 0;
    }

    /**
     * Constructor hỗ trợ độc giả tự đăng ký tài khoản có tên đăng nhập và mật khẩu.
     */
    public Member(String userCode, String username, String passwordHash,
                  String fullName, String email, String phone) {
        super(userCode, username, passwordHash, fullName, email, phone, Role.MEMBER, UserStatus.ACTIVE);
        this.maxBorrowLimit = DEFAULT_MAX_BORROW_LIMIT;
        this.currentBorrowCount = 0;
    }

    /**
     * Constructor tạo độc giả với hạn mức tùy chỉnh.
     */
    public Member(String userCode, String fullName, String email, String phone,
                  int maxBorrowLimit, int currentBorrowCount) {
        super(userCode, null, null, fullName, email, phone, Role.MEMBER, UserStatus.ACTIVE);
        setMaxBorrowLimit(maxBorrowLimit);
        setCurrentBorrowCount(currentBorrowCount);
    }

    /**
     * Constructor đầy đủ tham số (Full-arg constructor).
     */
    public Member(int userId, String userCode, String username, String passwordHash,
                  String fullName, String email, String phone,
                  Role role, UserStatus status, LocalDateTime createdAt,
                  int maxBorrowLimit, int currentBorrowCount) {
        super(userId, userCode, username, passwordHash, fullName, email, phone,
                role != null ? role : Role.MEMBER, status, createdAt);
        setMaxBorrowLimit(maxBorrowLimit);
        setCurrentBorrowCount(currentBorrowCount);
    }

    /**
     * Ghi đè phương thức trừu tượng từ lớp User (Đa hình - Polymorphism).
     *
     * @return Chuỗi mô tả "Độc giả thư viện"
     */
    @Override
    public String getRoleDescription() {
        return "Độc giả thư viện";
    }

    /**
     * Kiểm tra độc giả có đủ điều kiện mượn thêm sách hay không:
     * - Tài khoản phải ở trạng thái ACTIVE (không bị khóa).
     * - Số sách đang mượn hiện tại phải nhỏ hơn hạn mức mượn tối đa.
     *
     * @return true nếu được phép mượn sách, ngược lại false
     */
    public boolean canBorrow() {
        return getStatus() == UserStatus.ACTIVE && currentBorrowCount < maxBorrowLimit;
    }

    // ================= Getters & Setters =================

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) {
        if (maxBorrowLimit <= 0) {
            throw new IllegalArgumentException("Hạn mức mượn sách tối đa phải lớn hơn 0.");
        }
        this.maxBorrowLimit = maxBorrowLimit;
    }

    public int getCurrentBorrowCount() {
        return currentBorrowCount;
    }

    public void setCurrentBorrowCount(int currentBorrowCount) {
        if (currentBorrowCount < 0) {
            throw new IllegalArgumentException("Số lượng sách đang mượn không được âm.");
        }
        this.currentBorrowCount = currentBorrowCount;
    }

    @Override
    public String toString() {
        return "Member{" +
                "userId=" + getUserId() +
                ", userCode='" + getUserCode() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", status=" + getStatus() +
                ", maxBorrowLimit=" + maxBorrowLimit +
                ", currentBorrowCount=" + currentBorrowCount +
                ", canBorrow=" + canBorrow() +
                ", roleDescription='" + getRoleDescription() + '\'' +
                '}';
    }
}
