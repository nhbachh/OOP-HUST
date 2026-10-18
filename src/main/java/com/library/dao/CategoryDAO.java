package com.library.dao;

import com.library.model.Category;

import java.util.Optional;

/**
 * Interface DAO dành riêng cho thực thể Category (Thể loại sách).
 * Kế thừa các thao tác CRUD từ CrudDAO<Category, Integer> và bổ sung tra cứu theo tên.
 */
public interface CategoryDAO extends CrudDAO<Category, Integer> {

    /**
     * Tìm thể loại theo tên (do tên là duy nhất - UNIQUE).
     *
     * @param name Tên thể loại cần tìm
     * @return Optional chứa thể loại nếu tìm thấy, ngược lại Optional.empty()
     */
    Optional<Category> findByName(String name);
}
