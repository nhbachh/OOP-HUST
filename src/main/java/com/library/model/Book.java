package com.library.model;

/**
 * Lớp thực thể đại diện cho Đầu sách (Book).
 * Ánh xạ với bảng 'books' trong cơ sở dữ liệu.
 */
public class Book {

    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private int categoryId;
    private int publishYear;
    private int totalCopies;
    private int availableCopies;

    /**
     * Constructor mặc định không tham số.
     */
    public Book() {
    }

    /**
     * Constructor tạo sách mới (chưa có ID từ CSDL).
     */
    public Book(String isbn, String title, String author, int categoryId,
                int publishYear, int totalCopies, int availableCopies) {
        setIsbn(isbn);
        setTitle(title);
        setAuthor(author);
        setCategoryId(categoryId);
        setPublishYear(publishYear);
        setCopies(totalCopies, availableCopies);
    }

    /**
     * Constructor đầy đủ tham số.
     */
    public Book(int bookId, String isbn, String title, String author, int categoryId,
                int publishYear, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        setIsbn(isbn);
        setTitle(title);
        setAuthor(author);
        setCategoryId(categoryId);
        setPublishYear(publishYear);
        setCopies(totalCopies, availableCopies);
    }

    /**
     * Phương thức tiện ích kiểm tra xem sách còn bản sao có sẵn để mượn hay không.
     *
     * @return true nếu availableCopies > 0, ngược lại false
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /**
     * Thiết lập đồng thời tổng số lượng và số lượng khả dụng, bảo đảm tính toàn vẹn dữ liệu:
     * availableCopies >= 0 AND availableCopies <= totalCopies.
     */
    public void setCopies(int totalCopies, int availableCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Tổng số sách (totalCopies) không được âm.");
        }
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException(
                    "Số sách khả dụng (" + availableCopies + ") phải >= 0 và <= tổng số sách (" + totalCopies + ")."
            );
        }
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // ================= Getters & Setters =================

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ISBN không được để trống.");
        }
        this.isbn = isbn.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sách không được để trống.");
        }
        this.title = title.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tác giả không được để trống.");
        }
        this.author = author.trim();
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Mã thể loại (categoryId) phải là số dương.");
        }
        this.categoryId = categoryId;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Tổng số sách không được âm.");
        }
        if (this.availableCopies > totalCopies) {
            throw new IllegalArgumentException("Tổng số sách mới không thể nhỏ hơn số sách đang khả dụng hiện tại.");
        }
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0 || availableCopies > this.totalCopies) {
            throw new IllegalArgumentException(
                    "Số sách khả dụng (" + availableCopies + ") phải >= 0 và <= tổng số sách (" + this.totalCopies + ")."
            );
        }
        this.availableCopies = availableCopies;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", categoryId=" + categoryId +
                ", publishYear=" + publishYear +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                ", isAvailable=" + isAvailable() +
                '}';
    }
}
