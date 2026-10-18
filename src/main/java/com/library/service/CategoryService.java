package com.library.service;

import com.library.model.Category;

import java.util.List;

/**
 * Giao diện dịch vụ nghiệp vụ quản lý Thể loại sách.
 */
public interface CategoryService {

    /**
     * Thêm mới một thể loại vào hệ thống.
     *
     * @param name        Tên thể loại (bắt buộc, không trùng lặp)
     * @param description Mô tả chi tiết
     * @return Đối tượng Category sau khi được lưu thành công
     */
    Category createCategory(String name, String description);

    /**
     * Cập nhật thông tin thể loại.
     *
     * @param categoryId  Mã thể loại cần cập nhật
     * @param name        Tên mới
     * @param description Mô tả mới
     * @return true nếu cập nhật thành công
     */
    boolean updateCategory(int categoryId, String name, String description);

    /**
     * Xóa thể loại theo mã ID.
     *
     * @param categoryId Mã thể loại cần xóa
     * @return true nếu xóa thành công
     */
    boolean deleteCategory(int categoryId);

    /**
     * Lấy thông tin thể loại theo ID.
     *
     * @param categoryId Mã thể loại
     * @return Đối tượng Category
     * @throws com.library.exception.ResourceNotFoundException nếu không tìm thấy
     */
    Category getCategoryById(int categoryId);

    /**
     * Lấy thông tin thể loại theo tên.
     *
     * @param name Tên thể loại
     * @return Đối tượng Category
     * @throws com.library.exception.ResourceNotFoundException nếu không tìm thấy
     */
    Category getCategoryByName(String name);

    /**
     * Lấy toàn bộ danh sách thể loại.
     *
     * @return Danh sách thể loại
     */
    List<Category> getAllCategories();
}
