package com.library.service;

import com.library.model.Book;

import java.util.List;

/**
 * Giao diện dịch vụ nghiệp vụ quản lý Đầu sách.
 */
public interface BookService {

    /**
     * Thêm mới một đầu sách vào thư viện.
     *
     * @param isbn         Mã ISBN (bắt buộc, không trùng lặp)
     * @param title        Tiêu đề sách
     * @param author       Tác giả
     * @param categoryId   Mã thể loại sách
     * @param publishYear  Năm xuất bản
     * @param totalCopies  Tổng số lượng bản in (phải >= 1)
     * @return Đối tượng Book đã được lưu
     */
    Book createBook(String isbn, String title, String author, int categoryId, int publishYear, int totalCopies);

    /**
     * Cập nhật thông tin cuốn sách.
     *
     * @param book Đối tượng Book chứa thông tin cập nhật
     * @return true nếu cập nhật thành công
     */
    boolean updateBook(Book book);

    /**
     * Xóa đầu sách theo ID.
     *
     * @param bookId Mã sách cần xóa
     * @return true nếu xóa thành công
     */
    boolean deleteBook(int bookId);

    /**
     * Lấy sách theo ID.
     *
     * @param bookId Mã sách
     * @return Đối tượng Book
     */
    Book getBookById(int bookId);

    /**
     * Tìm sách theo mã ISBN.
     *
     * @param isbn Mã ISBN
     * @return Đối tượng Book
     */
    Book getBookByIsbn(String isbn);

    /**
     * Tìm kiếm sách theo từ khóa xuất hiện trong tiêu đề.
     *
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sách khớp từ khóa
     */
    List<Book> searchBooks(String keyword);

    /**
     * Lấy danh sách sách thuộc một thể loại.
     *
     * @param categoryId Mã thể loại
     * @return Danh sách sách
     */
    List<Book> getBooksByCategory(int categoryId);

    /**
     * Lấy toàn bộ danh sách đầu sách trong thư viện.
     *
     * @return Danh sách toàn bộ sách
     */
    List<Book> getAllBooks();
}
