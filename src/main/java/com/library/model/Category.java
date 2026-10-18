package com.library.model;

/**
 * Lớp thực thể đại diện cho Thể loại sách (Category).
 * Ánh xạ với bảng 'categories' trong cơ sở dữ liệu.
 */
public class Category {

    private int categoryId;
    private String name;
    private String description;

    /**
     * Constructor mặc định không tham số.
     */
    public Category() {
    }

    /**
     * Constructor hỗ trợ tạo mới thể loại (chưa có ID từ CSDL).
     */
    public Category(String name, String description) {
        setName(name);
        this.description = description;
    }

    /**
     * Constructor đầy đủ tham số.
     */
    public Category(int categoryId, String name, String description) {
        this.categoryId = categoryId;
        setName(name);
        this.description = description;
    }

    // ================= Getters & Setters =================

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null && !description.trim().isEmpty()) ? description.trim() : null;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
