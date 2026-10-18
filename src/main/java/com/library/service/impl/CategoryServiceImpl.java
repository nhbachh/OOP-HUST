package com.library.service.impl;

import com.library.dao.CategoryDAO;
import com.library.dao.impl.CategoryDAOImpl;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Category;
import com.library.service.CategoryService;

import java.util.List;
import java.util.Optional;

/**
 * Cài đặt lớp dịch vụ CategoryService.
 */
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryServiceImpl() {
        this.categoryDAO = new CategoryDAOImpl();
    }

    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public Category createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tên thể loại không được để trống.");
        }
        String trimmedName = name.trim();

        if (categoryDAO.findByName(trimmedName).isPresent()) {
            throw new ValidationException("Thể loại có tên '" + trimmedName + "' đã tồn tại trong hệ thống.");
        }

        Category category = new Category(trimmedName, description != null ? description.trim() : null);
        boolean success = categoryDAO.insert(category);
        if (!success) {
            throw new LibraryException("Lỗi hệ thống khi thêm thể loại '" + trimmedName + "'.");
        }
        return category;
    }

    @Override
    public boolean updateCategory(int categoryId, String name, String description) {
        Category existing = getCategoryById(categoryId);

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tên thể loại không được để trống.");
        }
        String trimmedName = name.trim();

        Optional<Category> byName = categoryDAO.findByName(trimmedName);
        if (byName.isPresent() && byName.get().getCategoryId() != categoryId) {
            throw new ValidationException("Tên thể loại '" + trimmedName + "' đã được sử dụng bởi thể loại khác.");
        }

        existing.setName(trimmedName);
        existing.setDescription(description != null ? description.trim() : null);

        return categoryDAO.update(existing);
    }

    @Override
    public boolean deleteCategory(int categoryId) {
        // Kiểm tra tồn tại trước khi xóa
        getCategoryById(categoryId);
        return categoryDAO.deleteById(categoryId);
    }

    @Override
    public Category getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Thể loại", categoryId));
    }

    @Override
    public Category getCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tên thể loại tìm kiếm không được để trống.");
        }
        return categoryDAO.findByName(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Thể loại", name));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
}
