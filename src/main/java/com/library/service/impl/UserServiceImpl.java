package com.library.service.impl;

import com.library.dao.UserDAO;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.BorrowException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.model.Role;
import com.library.model.Staff;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.service.UserService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Cài đặt lớp dịch vụ UserService.
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }

        User user = userDAO.findByUsername(username.trim())
                .or(() -> userDAO.findByUserCode(username.trim()))
                .orElseThrow(() -> new ValidationException("Tên đăng nhập hoặc mật khẩu không chính xác."));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BorrowException("Tài khoản '" + username + "' hiện đang bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        String hashedInput = hashPassword(rawPassword.trim());
        String storedHash = user.getPasswordHash();

        if (storedHash == null || (!storedHash.equalsIgnoreCase(hashedInput) && !storedHash.equals(rawPassword.trim()))) {
            throw new ValidationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        return user;
    }

    @Override
    public User getUserById(int userId) {
        return userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
    }

    @Override
    public User getUserByCode(String userCode) {
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new ValidationException("Mã người dùng không được để trống.");
        }
        return userDAO.findByUserCode(userCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng với mã", userCode));
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        return userDAO.findByUsername(username.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng với username", username));
    }

    @Override
    public boolean toggleUserStatus(int userId) {
        User user = getUserById(userId);
        UserStatus newStatus = (user.getStatus() == UserStatus.ACTIVE) ? UserStatus.LOCKED : UserStatus.ACTIVE;
        user.setStatus(newStatus);
        return userDAO.update(user);
    }

    @Override
    public List<Member> getAllMembers() {
        return userDAO.findAllMembers();
    }

    @Override
    public List<Staff> getAllStaffs() {
        return userDAO.findAllStaffs();
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public Staff createStaff(String userCode, String username, String rawPassword,
                             String fullName, String email, String phone,
                             Role role, String department) {
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không được để trống.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ và tên không được để trống.");
        }
        if (role != Role.ADMIN && role != Role.LIBRARIAN) {
            throw new ValidationException("Vai trò nhân viên phải là ADMIN hoặc LIBRARIAN.");
        }

        if (userDAO.findByUserCode(userCode.trim()).isPresent()) {
            throw new ValidationException("Mã người dùng '" + userCode.trim() + "' đã tồn tại.");
        }
        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Tên đăng nhập '" + username.trim() + "' đã tồn tại.");
        }

        String passwordHash = hashPassword(rawPassword.trim());
        Staff staff = new Staff(userCode.trim(), username.trim(), passwordHash,
                fullName.trim(), email != null ? email.trim() : null,
                phone != null ? phone.trim() : null, role, department != null ? department.trim() : null);

        boolean success = userDAO.insert(staff);
        if (!success) {
            throw new RuntimeException("Lỗi hệ thống khi thêm nhân viên mới.");
        }
        return staff;
    }

    @Override
    public Member createMember(String userCode, String fullName, String email, String phone) {
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new ValidationException("Mã độc giả không được để trống.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ và tên độc giả không được để trống.");
        }
        if (userDAO.findByUserCode(userCode.trim()).isPresent()) {
            throw new ValidationException("Mã người dùng '" + userCode.trim() + "' đã tồn tại.");
        }

        Member member = new Member(userCode.trim(), fullName.trim(),
                email != null ? email.trim() : null, phone != null ? phone.trim() : null);

        boolean success = userDAO.insert(member);
        if (!success) {
            throw new RuntimeException("Lỗi hệ thống khi thêm độc giả mới.");
        }
        return member;
    }

    @Override
    public Member registerMember(String userCode, String username, String rawPassword,
                                 String fullName, String email, String phone) {
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new ValidationException("Mã sinh viên / mã độc giả không được để trống.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống.");
        }
        if (rawPassword.trim().length() < 6) {
            throw new ValidationException("Mật khẩu phải chứa ít nhất 6 ký tự để đảm bảo an toàn.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ và tên không được để trống.");
        }

        String code = userCode.trim();
        String user = username.trim();

        if (userDAO.findByUserCode(code).isPresent()) {
            throw new ValidationException("Mã bạn đọc / sinh viên '" + code + "' đã tồn tại trong hệ thống.");
        }
        if (userDAO.findByUsername(user).isPresent()) {
            throw new ValidationException("Tên đăng nhập '" + user + "' đã được sử dụng. Vui lòng chọn tên khác.");
        }
        if (email != null && !email.trim().isEmpty()) {
            if (userDAO.findByEmail(email.trim()).isPresent()) {
                throw new ValidationException("Địa chỉ email '" + email.trim() + "' đã được đăng ký cho tài khoản khác.");
            }
        }

        String passwordHash = hashPassword(rawPassword.trim());
        Member member = new Member(code, user, passwordHash,
                fullName.trim(),
                email != null && !email.trim().isEmpty() ? email.trim() : null,
                phone != null && !phone.trim().isEmpty() ? phone.trim() : null);

        boolean success = userDAO.insert(member);
        if (!success) {
            throw new RuntimeException("Lỗi hệ thống khi đăng ký thẻ bạn đọc mới.");
        }
        return member;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu hiện tại không được để trống.");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new ValidationException("Mật khẩu mới phải có độ dài tối thiểu 6 ký tự.");
        }

        String storedHash = user.getPasswordHash();
        String oldHashed = hashPassword(oldPassword.trim());

        if (storedHash != null && !storedHash.isEmpty()) {
            if (!storedHash.equalsIgnoreCase(oldHashed) && !storedHash.equals(oldPassword.trim())) {
                throw new ValidationException("Mật khẩu hiện tại không chính xác.");
            }
        }

        user.setPasswordHash(hashPassword(newPassword.trim()));
        return userDAO.update(user);
    }

    @Override
    public boolean updateProfile(int userId, String fullName, String email, String phone) {
        User user = getUserById(userId);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ và tên không được để trống.");
        }
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            userDAO.findByEmail(trimmedEmail).ifPresent(existing -> {
                if (existing.getUserId() != userId) {
                    throw new ValidationException("Địa chỉ email '" + trimmedEmail + "' đã được sử dụng bởi người dùng khác.");
                }
            });
            user.setEmail(trimmedEmail);
        } else {
            user.setEmail(null);
        }

        user.setFullName(fullName.trim());
        user.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);

        return userDAO.update(user);
    }

    /**
     * Tiện ích băm mật khẩu theo chuẩn SHA-256.
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa SHA-256", e);
        }
    }
}
