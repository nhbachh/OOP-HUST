package com.library.dao;

import com.library.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO dành cho thực thể Book (Đầu sách).
 * Kế thừa các thao tác CRUD từ CrudDAO<Book, Integer> và cung cấp các hàm tra cứu đặc thù.
 */
public interface BookDAO extends CrudDAO<Book, Integer> {

    /**
     * Tìm sách theo mã ISBN duy nhất.
     *
     * @param isbn Mã ISBN cần tra cứu
     * @return Optional chứa Book nếu tìm thấy
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Tìm kiếm danh sách sách theo từ khóa xuất hiện trong tiêu đề (LIKE %keyword%).
     *
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách các cuốn sách khớp từ khóa
     */
    List<Book> findByTitle(String keyword);

    /**
     * Lấy danh sách các cuốn sách thuộc một thể loại cụ thể.
     *
     * @param categoryId Mã thể loại
     * @return Danh sách sách thuộc thể loại
     */
    List<Book> findByCategoryId(int categoryId);

    /**
     * Tăng hoặc giảm số lượng sách khả dụng (available_copies).
     *
     * @param bookId Mã sách cần cập nhật
     * @param delta  Độ biến thiên (+1 khi trả sách, -1 khi mượn sách)
     * @return true nếu cập nhật thành công, ngược lại false
     */
    boolean updateAvailableCopies(int bookId, int delta);
}
