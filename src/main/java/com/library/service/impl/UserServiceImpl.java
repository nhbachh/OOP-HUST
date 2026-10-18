package com.library.service.impl;

import com.library.dao.UserDAO;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.BorrowException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Member;
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
