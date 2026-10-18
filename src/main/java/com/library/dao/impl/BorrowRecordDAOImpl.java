package com.library.dao.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.BorrowRecordDAO;
import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt JDBC cho BorrowRecordDAO.
 * Quản lý các giao dịch mượn/trả sách an toàn với PreparedStatement và try-with-resources.
 */
public class BorrowRecordDAOImpl implements BorrowRecordDAO {

    private static final String SELECT_BASE =
            "SELECT record_id, user_id, book_id, staff_id, borrow_date, due_date, return_date, status FROM borrow_records ";

    @Override
    public boolean insert(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (user_id, book_id, staff_id, borrow_date, due_date, return_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBookId());
            if (record.getStaffId() != null) {
                ps.setInt(3, record.getStaffId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDate(4, Date.valueOf(record.getBorrowDate()));
            ps.setDate(5, Date.valueOf(record.getDueDate()));
            if (record.getReturnDate() != null) {
                ps.setDate(6, Date.valueOf(record.getReturnDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, record.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setRecordId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.insert] Lỗi thêm phiếu mượn: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET user_id = ?, book_id = ?, staff_id = ?, borrow_date = ?, " +
                     "due_date = ?, return_date = ?, status = ? WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBookId());
            if (record.getStaffId() != null) {
                ps.setInt(3, record.getStaffId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDate(4, Date.valueOf(record.getBorrowDate()));
            ps.setDate(5, Date.valueOf(record.getDueDate()));
            if (record.getReturnDate() != null) {
                ps.setDate(6, Date.valueOf(record.getReturnDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, record.getStatus().name());
            ps.setInt(8, record.getRecordId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.update] Lỗi cập nhật phiếu mượn ID " + record.getRecordId() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM borrow_records WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.deleteById] Lỗi xóa phiếu mượn ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<BorrowRecord> findById(Integer id) {
        String sql = SELECT_BASE + "WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.findById] Lỗi tìm phiếu mượn ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY record_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.findAll] Lỗi lấy toàn bộ danh sách phiếu mượn: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<BorrowRecord> findByUserId(int userId) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY record_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.findByUserId] Lỗi lấy phiếu mượn của user ID " + userId + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<BorrowRecord> findByStatus(BorrowStatus status) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE status = ? ORDER BY record_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.findByStatus] Lỗi lọc phiếu mượn theo trạng thái " + status + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<BorrowRecord> findOverdueRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE status = 'OVERDUE' ORDER BY due_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BorrowRecordDAOImpl.findOverdueRecords] Lỗi lấy danh sách phiếu quá hạn: " + e.getMessage());
        }
        return list;
    }

    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));

        int staffId = rs.getInt("staff_id");
        record.setStaffId(rs.wasNull() ? null : staffId);

        Date borrowDate = rs.getDate("borrow_date");
        Date dueDate = rs.getDate("due_date");
        record.setDates(borrowDate != null ? borrowDate.toLocalDate() : null,
                        dueDate != null ? dueDate.toLocalDate() : null);

        Date returnDate = rs.getDate("return_date");
        record.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);

        record.setStatus(BorrowStatus.valueOf(rs.getString("status")));
        return record;
    }
}
