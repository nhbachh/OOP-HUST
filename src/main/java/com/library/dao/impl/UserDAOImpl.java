package com.library.dao.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.UserDAO;
import com.library.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt JDBC cho UserDAO với kỹ thuật ánh xạ Đa hình (Polymorphism Mapping).
 * Ánh xạ bảng Single Table Inheritance (STI) 'users' thành các thực thể con Member hoặc Staff tương ứng.
 */
public class UserDAOImpl implements UserDAO {

    private static final String SELECT_BASE =
            "SELECT user_id, user_code, username, password_hash, full_name, email, phone, role, status, created_at FROM users ";

    @Override
    public boolean insert(User user) {
        String sql = "INSERT INTO users (user_code, username, password_hash, full_name, email, phone, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUserCode());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole().name());
            ps.setString(8, user.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.insert] Lỗi thêm người dùng '" + user.getFullName() + "': " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET user_code = ?, username = ?, password_hash = ?, full_name = ?, " +
                     "email = ?, phone = ?, role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserCode());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole().name());
            ps.setString(8, user.getStatus().name());
            ps.setInt(9, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.update] Lỗi cập nhật người dùng ID " + user.getUserId() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.deleteById] Lỗi xóa người dùng ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = SELECT_BASE + "WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findById] Lỗi tìm người dùng ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findAll] Lỗi lấy toàn bộ danh sách người dùng: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<User> findByUserCode(String userCode) {
        String sql = SELECT_BASE + "WHERE user_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findByUserCode] Lỗi tìm người dùng theo mã '" + userCode + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = SELECT_BASE + "WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findByUsername] Lỗi tìm người dùng theo username '" + username + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = SELECT_BASE + "WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findByEmail] Lỗi tìm người dùng theo email '" + email + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE role = ? ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findByRole] Lỗi lọc người dùng theo vai trò '" + role + "': " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Member> findAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE role = 'MEMBER' ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs, conn);
                if (user instanceof Member member) {
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findAllMembers] Lỗi lấy danh sách độc giả: " + e.getMessage());
        }
        return members;
    }

    @Override
    public List<Staff> findAllStaffs() {
        List<Staff> staffs = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE role IN ('ADMIN', 'LIBRARIAN') ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs, conn);
                if (user instanceof Staff staff) {
                    staffs.add(staff);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAOImpl.findAllStaffs] Lỗi lấy danh sách nhân viên: " + e.getMessage());
        }
        return staffs;
    }

    /**
     * Phương thức ánh xạ ResultSet sang đối tượng con tương ứng (Đa hình - Polymorphic Mapping).
     * Phân tách:
     * - Role.MEMBER  -> Khởi tạo đối tượng Member (gán maxBorrowLimit, tính currentBorrowCount).
     * - Role.ADMIN / Role.LIBRARIAN -> Khởi tạo đối tượng Staff (gán phòng ban công tác).
     */
    private User mapResultSetToUser(ResultSet rs, Connection conn) throws SQLException {
        int userId = rs.getInt("user_id");
        String userCode = rs.getString("user_code");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        Role role = Role.valueOf(rs.getString("role"));
        UserStatus status = UserStatus.valueOf(rs.getString("status"));
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now();

        if (role == Role.MEMBER) {
            Member member = new Member();
            member.setUserId(userId);
            member.setUserCode(userCode);
            member.setUsername(username);
            member.setPasswordHash(passwordHash);
            member.setFullName(fullName);
            member.setEmail(email);
            member.setPhone(phone);
            member.setRole(role);
            member.setStatus(status);
            member.setCreatedAt(createdAt);
            member.setMaxBorrowLimit(Member.DEFAULT_MAX_BORROW_LIMIT);

            int activeBorrows = countActiveBorrows(conn, userId);
            member.setCurrentBorrowCount(activeBorrows);

            return member;
        } else {
            Staff staff = new Staff();
            staff.setUserId(userId);
            staff.setUserCode(userCode);
            staff.setUsername(username);
            staff.setPasswordHash(passwordHash);
            staff.setFullName(fullName);
            staff.setEmail(email);
            staff.setPhone(phone);
            staff.setRole(role);
            staff.setStatus(status);
            staff.setCreatedAt(createdAt);

            if (role == Role.ADMIN) {
                staff.setDepartment("Ban Quản trị");
            } else {
                staff.setDepartment("Phòng Quản lý Kho");
            }

            return staff;
        }
    }

    /**
     * Đếm số lượng sách độc giả hiện đang mượn (trạng thái BORROWED hoặc OVERDUE).
     */
    private int countActiveBorrows(Connection conn, int userId) {
        if (conn == null) return 0;
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status IN ('BORROWED', 'OVERDUE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            // Không làm gián đoạn việc ánh xạ nếu có lỗi đếm phụ
            System.err.println("[UserDAOImpl.countActiveBorrows] Lỗi đếm sách mượn cho user " + userId + ": " + e.getMessage());
        }
        return 0;
    }
}
