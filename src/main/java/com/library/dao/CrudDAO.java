package com.library.dao;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện Generic CRUD DAO áp dụng nguyên lý Trừu tượng (Abstraction).
 * Định nghĩa các thao tác dữ liệu chuẩn mực độc lập với công nghệ CSDL cụ thể.
 *
 * @param <T>  Kiểu thực thể (Entity Model)
 * @param <ID> Kiểu khóa chính (Primary Key Type)
 */
public interface CrudDAO<T, ID> {

    /**
     * Thêm mới một bản ghi vào cơ sở dữ liệu.
     *
     * @param entity Thực thể cần lưu
     * @return true nếu thêm thành công, ngược lại false
     */
    boolean insert(T entity);

    /**
     * Cập nhật thông tin một bản ghi đã tồn tại.
     *
     * @param entity Thực thể mang dữ liệu mới
     * @return true nếu cập nhật thành công, ngược lại false
     */
    boolean update(T entity);

    /**
     * Xóa một bản ghi dựa theo khóa chính ID.
     *
     * @param id Khóa chính của bản ghi cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    boolean deleteById(ID id);

    /**
     * Tìm kiếm một bản ghi theo khóa chính ID.
     *
     * @param id Khóa chính cần tìm
     * @return Optional chứa thực thể nếu tìm thấy, ngược lại Optional.empty()
     */
    Optional<T> findById(ID id);

    /**
     * Lấy toàn bộ danh sách các bản ghi từ bảng.
     *
     * @return Danh sách các thực thể (rỗng nếu không có bản ghi)
     */
    List<T> findAll();
}
