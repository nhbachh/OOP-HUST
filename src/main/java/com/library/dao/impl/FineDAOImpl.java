package com.library.dao.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.FineDAO;
import com.library.model.Fine;
import com.library.model.FineStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt JDBC cho FineDAO.
 * Quản lý tiền phạt và các giao dịch nộp phạt bằng PreparedStatement chống SQL Injection.
 */
public class FineDAOImpl implements FineDAO {

    private static final String SELECT_BASE =
            "SELECT fine_id, record_id, user_id, overdue_days, fine_amount, reason, status, created_at, paid_at FROM fines ";

    @Override
    public boolean insert(Fine fine) {
        String sql = "INSERT INTO fines (record_id, user_id, overdue_days, fine_amount, reason, status, paid_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, fine.getRecordId());
            ps.setInt(2, fine.getUserId());
            ps.setInt(3, fine.getOverdueDays());
            ps.setBigDecimal(4, fine.getFineAmount());
            ps.setString(5, fine.getReason());
            ps.setString(6, fine.getStatus().name());
            if (fine.getPaidAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(fine.getPaidAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fine.setFineId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.insert] Lỗi thêm phiếu phạt: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Fine fine) {
        String sql = "UPDATE fines SET record_id = ?, user_id = ?, overdue_days = ?, fine_amount = ?, " +
                     "reason = ?, status = ?, paid_at = ? WHERE fine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fine.getRecordId());
            ps.setInt(2, fine.getUserId());
            ps.setInt(3, fine.getOverdueDays());
            ps.setBigDecimal(4, fine.getFineAmount());
            ps.setString(5, fine.getReason());
            ps.setString(6, fine.getStatus().name());
            if (fine.getPaidAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(fine.getPaidAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.setInt(8, fine.getFineId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.update] Lỗi cập nhật phiếu phạt ID " + fine.getFineId() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM fines WHERE fine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.deleteById] Lỗi xóa phiếu phạt ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Fine> findById(Integer id) {
        String sql = SELECT_BASE + "WHERE fine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.findById] Lỗi tìm phiếu phạt ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Fine> findAll() {
        List<Fine> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY fine_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToFine(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.findAll] Lỗi lấy toàn bộ danh sách phiếu phạt: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Fine> findByRecordId(int recordId) {
        String sql = SELECT_BASE + "WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.findByRecordId] Lỗi tìm phiếu phạt cho phiếu mượn ID " + recordId + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Fine> findByUserId(int userId) {
        List<Fine> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY fine_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.findByUserId] Lỗi lấy phiếu phạt của user ID " + userId + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Fine> findByStatus(FineStatus status) {
        List<Fine> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE status = ? ORDER BY fine_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FineDAOImpl.findByStatus] Lỗi lọc phiếu phạt theo trạng thái " + status + ": " + e.getMessage());
        }
        return list;
    }

    private Fine mapResultSetToFine(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setFineId(rs.getInt("fine_id"));
        fine.setRecordId(rs.getInt("record_id"));
        fine.setUserId(rs.getInt("user_id"));
        fine.setOverdueDays(rs.getInt("overdue_days"));
        fine.setFineAmount(rs.getBigDecimal("fine_amount"));
        fine.setReason(rs.getString("reason"));
        fine.setStatus(FineStatus.valueOf(rs.getString("status")));

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        fine.setCreatedAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now());

        Timestamp paidAtTs = rs.getTimestamp("paid_at");
        fine.setPaidAt(paidAtTs != null ? paidAtTs.toLocalDateTime() : null);

        return fine;
    }
}
