package com.library.dao.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.CategoryDAO;
import com.library.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt JDBC cho CategoryDAO.
 * Đảm bảo 100% PreparedStatement chống SQL Injection và try-with-resources giải phóng tài nguyên.
 */
public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public boolean insert(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.insert] Lỗi thêm thể loại: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.update] Lỗi cập nhật thể loại ID " + category.getCategoryId() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.deleteById] Lỗi xóa thể loại ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.findById] Lỗi tìm thể loại ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name, description FROM categories ORDER BY category_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.findAll] Lỗi lấy danh sách thể loại: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT category_id, name, description FROM categories WHERE name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAOImpl.findByName] Lỗi tìm thể loại theo tên '" + name + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
