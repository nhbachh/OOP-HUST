package com.library.dao.impl;

import com.library.config.DatabaseConfig;
import com.library.dao.BookDAO;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt JDBC cho BookDAO.
 * Đảm bảo 100% PreparedStatement và quản lý kết nối an toàn qua try-with-resources.
 */
public class BookDAOImpl implements BookDAO {

    @Override
    public boolean insert(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category_id, publish_year, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getCategoryId());
            ps.setInt(5, book.getPublishYear());
            ps.setInt(6, book.getTotalCopies());
            ps.setInt(7, book.getAvailableCopies());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        book.setBookId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.insert] Lỗi thêm sách '" + book.getTitle() + "': " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Book book) {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, category_id = ?, " +
                     "publish_year = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getCategoryId());
            ps.setInt(5, book.getPublishYear());
            ps.setInt(6, book.getTotalCopies());
            ps.setInt(7, book.getAvailableCopies());
            ps.setInt(8, book.getBookId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.update] Lỗi cập nhật sách ID " + book.getBookId() + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.deleteById] Lỗi xóa sách ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Book> findById(Integer id) {
        String sql = "SELECT book_id, isbn, title, author, category_id, publish_year, total_copies, available_copies " +
                     "FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.findById] Lỗi tìm sách ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT book_id, isbn, title, author, category_id, publish_year, total_copies, available_copies " +
                     "FROM books ORDER BY book_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.findAll] Lỗi lấy toàn bộ danh sách sách: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = "SELECT book_id, isbn, title, author, category_id, publish_year, total_copies, available_copies " +
                     "FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.findByIsbn] Lỗi tìm sách theo ISBN '" + isbn + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findByTitle(String keyword) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT book_id, isbn, title, author, category_id, publish_year, total_copies, available_copies " +
                     "FROM books WHERE title LIKE ? ORDER BY book_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + (keyword != null ? keyword.trim() : "") + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.findByTitle] Lỗi tìm sách theo tiêu đề '" + keyword + "': " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Book> findByCategoryId(int categoryId) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT book_id, isbn, title, author, category_id, publish_year, total_copies, available_copies " +
                     "FROM books WHERE category_id = ? ORDER BY book_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.findByCategoryId] Lỗi tìm sách theo thể loại ID " + categoryId + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateAvailableCopies(int bookId, int delta) {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE book_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, bookId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookDAOImpl.updateAvailableCopies] Lỗi cập nhật tồn kho sách ID " + bookId +
                    " với delta " + delta + ": " + e.getMessage());
        }
        return false;
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setPublishYear(rs.getInt("publish_year"));
        book.setCopies(rs.getInt("total_copies"), rs.getInt("available_copies"));
        return book;
    }
}
