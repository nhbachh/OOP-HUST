package com.library.service.impl;

import com.library.dao.BookDAO;
import com.library.dao.CategoryDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.CategoryDAOImpl;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.service.BookService;

import java.util.List;
import java.util.Optional;

/**
 * Cài đặt lớp dịch vụ BookService với các ràng buộc kiểm tra nghiệp vụ.
 */
public class BookServiceImpl implements BookService {

    private final BookDAO bookDAO;
    private final CategoryDAO categoryDAO;

    public BookServiceImpl() {
        this.bookDAO = new BookDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
    }

    public BookServiceImpl(BookDAO bookDAO, CategoryDAO categoryDAO) {
        this.bookDAO = bookDAO;
        this.categoryDAO = categoryDAO;
    }

    @Override
    public Book createBook(String isbn, String title, String author, int categoryId, int publishYear, int totalCopies) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ValidationException("Mã ISBN không được để trống.");
        }
        String trimmedIsbn = isbn.trim();
        if (bookDAO.findByIsbn(trimmedIsbn).isPresent()) {
            throw new ValidationException("Sách có mã ISBN '" + trimmedIsbn + "' đã tồn tại trong hệ thống.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Tiêu đề sách không được để trống.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Tên tác giả không được để trống.");
        }
        if (totalCopies < 1) {
            throw new ValidationException("Tổng số sách (totalCopies) khi thêm mới phải lớn hơn hoặc bằng 1.");
        }

        // Kiểm tra thể loại có tồn tại không
        categoryDAO.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Thể loại", categoryId)
        );

        // Ban đầu số bản sao khả dụng bằng tổng số bản in
        Book book = new Book(trimmedIsbn, title.trim(), author.trim(), categoryId, publishYear, totalCopies, totalCopies);
        boolean success = bookDAO.insert(book);
        if (!success) {
            throw new LibraryException("Lỗi hệ thống khi thêm đầu sách '" + title + "'.");
        }
        return book;
    }

    @Override
    public boolean updateBook(Book book) {
        if (book == null) {
            throw new ValidationException("Thông tin sách cập nhật không được null.");
        }
        Book existing = getBookById(book.getBookId());

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new ValidationException("Mã ISBN không được để trống.");
        }
        Optional<Book> byIsbn = bookDAO.findByIsbn(book.getIsbn().trim());
        if (byIsbn.isPresent() && byIsbn.get().getBookId() != book.getBookId()) {
            throw new ValidationException("Mã ISBN '" + book.getIsbn() + "' đã được sử dụng bởi cuốn sách khác.");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ValidationException("Tiêu đề sách không được để trống.");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Tên tác giả không được để trống.");
        }

        categoryDAO.findById(book.getCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("Thể loại", book.getCategoryId())
        );

        if (book.getTotalCopies() < 0) {
            throw new ValidationException("Tổng số sách không được âm.");
        }
        if (book.getAvailableCopies() < 0 || book.getAvailableCopies() > book.getTotalCopies()) {
            throw new ValidationException("Số lượng bản sao khả dụng (" + book.getAvailableCopies() +
                    ") phải >= 0 và <= tổng số sách (" + book.getTotalCopies() + ").");
        }

        return bookDAO.update(book);
    }

    @Override
    public boolean deleteBook(int bookId) {
        getBookById(bookId);
        return bookDAO.deleteById(bookId);
    }

    @Override
    public Book getBookById(int bookId) {
        return bookDAO.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Sách", bookId));
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ValidationException("Mã ISBN tìm kiếm không được để trống.");
        }
        return bookDAO.findByIsbn(isbn.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Sách với mã ISBN", isbn));
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        return bookDAO.findByTitle(keyword);
    }

    @Override
    public List<Book> getBooksByCategory(int categoryId) {
        return bookDAO.findByCategoryId(categoryId);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }
}
