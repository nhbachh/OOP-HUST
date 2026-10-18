package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cửa sổ làm việc cho Thủ thư (Librarian Dashboard).
 * Cung cấp đầy đủ các nghiệp vụ thư viện: Quản lý sách & thể loại, mượn sách ACID, trả sách quá hạn, thu tiền phạt.
 * Thiết kế theo nguyên tắc Loose Coupling: Tương tác 100% qua tầng Service.
 * Hỗ trợ kế thừa (Inheritance) cho AdminDashboardFrame.
 */
public class LibrarianDashboardFrame extends JFrame {

    protected final Staff staff;
    protected final BookService bookService;
    protected final CategoryService categoryService;
    protected final BorrowService borrowService;
    protected final FineService fineService;
    protected final UserService userService;

    protected JTabbedPane tabbedPane;
    protected final Map<Integer, String> categoryMap = new HashMap<>();

    // Books tab components
    protected DefaultTableModel bookTableModel;
    protected JTable bookTable;
    protected JTextField txtBookSearch;
    protected JComboBox<CategoryItem> cboBookCategoryFilter;

    // Borrow tab components
    protected JTextField txtBorrowMemberCode;
    protected JLabel lblBorrowMemberInfo;
    protected Member currentBorrowMember;

    protected JTextField txtBorrowBookId;
    protected JLabel lblBorrowBookInfo;
    protected Book currentBorrowBook;

    protected JSpinner spnBorrowDays;

    // Return tab components
    protected DefaultTableModel returnTableModel;
    protected JTable returnTable;
    protected JTextField txtReturnSearch;

    // Fine tab components
    protected DefaultTableModel fineTableModel;
    protected JTable fineTable;
    protected JComboBox<String> cboFineStatusFilter;

    public LibrarianDashboardFrame(Staff staff,
                                   BookService bookService,
                                   CategoryService categoryService,
                                   BorrowService borrowService,
                                   FineService fineService,
                                   UserService userService) {
        this.staff = staff;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;

        initCategoryCache();
        initComponents();
        loadAllInitialData();
    }

    protected void initCategoryCache() {
        categoryMap.clear();
        try {
            List<Category> categories = categoryService.getAllCategories();
            for (Category c : categories) {
                categoryMap.put(c.getCategoryId(), c.getName());
            }
        } catch (Exception ignored) {
        }
    }

    protected void initComponents() {
        setTitle(getFrameTitle());
        setSize(1080, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13f));

        initLibrarianTabs(tabbedPane);

        add(tabbedPane, BorderLayout.CENTER);
    }

    protected String getFrameTitle() {
        return "Hệ thống Quản lý Thư viện — Bàn làm việc Thủ thư (" + staff.getUserCode() + " - " + staff.getFullName() + ")";
    }

    protected JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        header.setBackground(Color.WHITE);

        JPanel left = new JPanel(new GridLayout(2, 1, 2, 2));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ THƯ VIỆN ĐIỆN TỬ");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        lblTitle.setForeground(new Color(24, 90, 157));

        String dept = (staff.getDepartment() != null && !staff.getDepartment().isEmpty()) ? staff.getDepartment() : "Bộ phận Thư viện";
        JLabel lblUser = new JLabel("Nhân viên: " + staff.getFullName() + " | Mã: " + staff.getUserCode() +
                " | Vai trò: " + staff.getRoleDescription() + " | Phòng ban: " + dept);
        lblUser.setFont(lblUser.getFont().deriveFont(Font.PLAIN, 12f));
        lblUser.setForeground(Color.DARK_GRAY);

        left.add(lblTitle);
        left.add(lblUser);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> handleLogout());

        header.add(left, BorderLayout.CENTER);
        header.add(btnLogout, BorderLayout.EAST);

        return header;
    }

    protected void initLibrarianTabs(JTabbedPane tabbed) {
        tabbed.addTab("Quản lý Sách & Thể loại", createBooksAndCategoriesTab());
        tabbed.addTab("Lập Phiếu Mượn Sách (ACID)", createBorrowTab());
        tabbed.addTab("Quản lý Trả Sách", createReturnTab());
        tabbed.addTab("Quản lý Tiền Phạt", createFinesTab());
    }

    // =========================================================================
    // TAB 1: QUẢN LÝ SÁCH & THỂ LOẠI
    // =========================================================================
    protected JPanel createBooksAndCategoriesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Từ khóa:"));
        txtBookSearch = new JTextField(15);
        txtBookSearch.addActionListener(e -> searchBooks());
        toolbar.add(txtBookSearch);

        toolbar.add(new JLabel("Thể loại:"));
        cboBookCategoryFilter = new JComboBox<>();
        populateCategoryFilterCombo();
        toolbar.add(cboBookCategoryFilter);

        JButton btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> searchBooks());
        toolbar.add(btnSearch);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtBookSearch.setText("");
            cboBookCategoryFilter.setSelectedIndex(0);
            loadBooksTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(new JSeparator(JSeparator.VERTICAL));

        JButton btnAddBook = new JButton("+ Thêm sách");
        btnAddBook.addActionListener(e -> showAddBookDialog());
        toolbar.add(btnAddBook);

        JButton btnEditBook = new JButton("Sửa sách");
        btnEditBook.addActionListener(e -> showEditBookDialog());
        toolbar.add(btnEditBook);

        JButton btnDeleteBook = new JButton("Xóa sách");
        btnDeleteBook.addActionListener(e -> handleDeleteBook());
        toolbar.add(btnDeleteBook);

        JButton btnManageCategories = new JButton("Quản lý Thể loại");
        btnManageCategories.addActionListener(e -> showManageCategoriesDialog());
        toolbar.add(btnManageCategories);

        panel.add(toolbar, BorderLayout.NORTH);

        // Books Table
        String[] columns = {"Mã ID", "ISBN", "Tiêu đề", "Tác giả", "Thể loại", "Năm XB", "Còn lại", "Tổng số", "Tình trạng"};
        bookTableModel = new DefaultTableModel(columns, 0);
        bookTable = UIUtils.createReadonlyTable(bookTableModel);
        UIUtils.centerAlignColumns(bookTable, 0, 1, 5, 6, 7, 8);
        UIUtils.setColumnWidth(bookTable, 0, 60);
        UIUtils.setColumnWidth(bookTable, 1, 120);
        UIUtils.setColumnWidth(bookTable, 2, 280);
        UIUtils.setColumnWidth(bookTable, 3, 160);
        UIUtils.setColumnWidth(bookTable, 4, 120);

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        return panel;
    }

    private void populateCategoryFilterCombo() {
        cboBookCategoryFilter.removeAllItems();
        cboBookCategoryFilter.addItem(new CategoryItem(0, "-- Tất cả thể loại --"));
        try {
            List<Category> list = categoryService.getAllCategories();
            for (Category c : list) {
                cboBookCategoryFilter.addItem(new CategoryItem(c.getCategoryId(), c.getName()));
            }
        } catch (Exception ignored) {
        }
    }

    public void loadBooksTable() {
        try {
            initCategoryCache();
            List<Book> books = bookService.getAllBooks();
            renderBookTable(books);
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách sách: " + e.getMessage());
        }
    }

    private void searchBooks() {
        String kw = txtBookSearch.getText().trim();
        CategoryItem item = (CategoryItem) cboBookCategoryFilter.getSelectedItem();
        int catId = item != null ? item.id : 0;

        try {
            List<Book> books;
            if (!kw.isEmpty()) {
                books = bookService.searchBooks(kw);
                if (catId > 0) {
                    books = books.stream().filter(b -> b.getCategoryId() == catId).toList();
                }
            } else if (catId > 0) {
                books = bookService.getBooksByCategory(catId);
            } else {
                books = bookService.getAllBooks();
            }
            renderBookTable(books);
        } catch (Exception e) {
            UIUtils.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void renderBookTable(List<Book> books) {
        bookTableModel.setRowCount(0);
        for (Book b : books) {
            String catName = categoryMap.getOrDefault(b.getCategoryId(), "Không xác định");
            String status = b.isAvailable() ? "Còn bản sao" : "Đã hết";
            bookTableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    catName,
                    b.getPublishYear(),
                    b.getAvailableCopies(),
                    b.getTotalCopies(),
                    status
            });
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Thêm Đầu Sách Mới", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        JTextField txtIsbn = new JTextField();
        JTextField txtTitle = new JTextField();
        JTextField txtAuthor = new JTextField();

        JComboBox<CategoryItem> cboCat = new JComboBox<>();
        try {
            List<Category> cats = categoryService.getAllCategories();
            for (Category c : cats) {
                cboCat.addItem(new CategoryItem(c.getCategoryId(), c.getName()));
            }
        } catch (Exception ignored) {
        }

        JSpinner spnYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1800, 2100, 1));
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(spnYear, "#");
        spnYear.setEditor(yearEditor);

        JSpinner spnTotal = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

        form.add(new JLabel("Mã ISBN (*):"));
        form.add(txtIsbn);
        form.add(new JLabel("Tiêu đề sách (*):"));
        form.add(txtTitle);
        form.add(new JLabel("Tác giả (*):"));
        form.add(txtAuthor);
        form.add(new JLabel("Thể loại (*):"));
        form.add(cboCat);
        form.add(new JLabel("Năm xuất bản:"));
        form.add(spnYear);
        form.add(new JLabel("Tổng số bản in:"));
        form.add(spnTotal);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnSave = new JButton("Lưu sách");
        JButton btnCancel = new JButton("Hủy bỏ");

        btnSave.addActionListener(e -> {
            try {
                String isbn = txtIsbn.getText().trim();
                String title = txtTitle.getText().trim();
                String author = txtAuthor.getText().trim();
                CategoryItem selectedCat = (CategoryItem) cboCat.getSelectedItem();
                int catId = selectedCat != null ? selectedCat.id : 0;
                int year = (Integer) spnYear.getValue();
                int total = (Integer) spnTotal.getValue();

                bookService.createBook(isbn, title, author, catId, year, total);
                UIUtils.showSuccess(dialog, "Thêm đầu sách mới thành công!");
                dialog.dispose();
                loadBooksTable();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Lỗi thêm sách: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showWarning(this, "Vui lòng chọn một cuốn sách từ bảng để sửa.");
            return;
        }

        int bookId = (Integer) bookTableModel.getValueAt(selectedRow, 0);
        Book book;
        try {
            book = bookService.getBookById(bookId);
        } catch (Exception e) {
            UIUtils.showError(this, "Không tìm thấy thông tin sách: " + e.getMessage());
            return;
        }

        JDialog dialog = new JDialog(this, "Cập Nhật Thông Tin Sách #" + bookId, true);
        dialog.setSize(480, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        JTextField txtIsbn = new JTextField(book.getIsbn());
        txtIsbn.setEditable(false);
        JTextField txtTitle = new JTextField(book.getTitle());
        JTextField txtAuthor = new JTextField(book.getAuthor());

        JComboBox<CategoryItem> cboCat = new JComboBox<>();
        try {
            List<Category> cats = categoryService.getAllCategories();
            for (Category c : cats) {
                CategoryItem item = new CategoryItem(c.getCategoryId(), c.getName());
                cboCat.addItem(item);
                if (c.getCategoryId() == book.getCategoryId()) {
                    cboCat.setSelectedItem(item);
                }
            }
        } catch (Exception ignored) {
        }

        JSpinner spnYear = new JSpinner(new SpinnerNumberModel(book.getPublishYear(), 1800, 2100, 1));
        spnYear.setEditor(new JSpinner.NumberEditor(spnYear, "#"));

        JSpinner spnTotal = new JSpinner(new SpinnerNumberModel(book.getTotalCopies(), 1, 1000, 1));
        JSpinner spnAvailable = new JSpinner(new SpinnerNumberModel(book.getAvailableCopies(), 0, 1000, 1));

        form.add(new JLabel("Mã ISBN (Cố định):"));
        form.add(txtIsbn);
        form.add(new JLabel("Tiêu đề sách (*):"));
        form.add(txtTitle);
        form.add(new JLabel("Tác giả (*):"));
        form.add(txtAuthor);
        form.add(new JLabel("Thể loại (*):"));
        form.add(cboCat);
        form.add(new JLabel("Năm xuất bản:"));
        form.add(spnYear);
        form.add(new JLabel("Tổng số bản in:"));
        form.add(spnTotal);
        form.add(new JLabel("Số bản sẵn có:"));
        form.add(spnAvailable);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnSave = new JButton("Lưu thay đổi");
        JButton btnCancel = new JButton("Hủy bỏ");

        btnSave.addActionListener(e -> {
            try {
                book.setTitle(txtTitle.getText().trim());
                book.setAuthor(txtAuthor.getText().trim());
                CategoryItem selCat = (CategoryItem) cboCat.getSelectedItem();
                if (selCat != null) {
                    book.setCategoryId(selCat.id);
                }
                book.setPublishYear((Integer) spnYear.getValue());
                int tot = (Integer) spnTotal.getValue();
                int avail = (Integer) spnAvailable.getValue();
                book.setCopies(tot, avail);

                bookService.updateBook(book);
                UIUtils.showSuccess(dialog, "Cập nhật sách thành công!");
                dialog.dispose();
                loadBooksTable();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Lỗi cập nhật: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleDeleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showWarning(this, "Vui lòng chọn một cuốn sách để xóa.");
            return;
        }

        int bookId = (Integer) bookTableModel.getValueAt(selectedRow, 0);
        String title = (String) bookTableModel.getValueAt(selectedRow, 2);

        if (UIUtils.confirm(this, "Bạn có chắc chắn muốn xóa cuốn sách \"" + title + "\" (ID: " + bookId + ") không?", "Xác nhận xóa sách")) {
            try {
                bookService.deleteBook(bookId);
                UIUtils.showSuccess(this, "Đã xóa sách thành công!");
                loadBooksTable();
            } catch (Exception e) {
                UIUtils.showError(this, "Không thể xóa sách: " + e.getMessage());
            }
        }
    }

    private void showManageCategoriesDialog() {
        JDialog dialog = new JDialog(this, "Quản lý Danh mục Thể loại Sách", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Mã thể loại", "Tên thể loại", "Mô tả"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = UIUtils.createReadonlyTable(model);
        UIUtils.centerAlignColumns(table, 0);
        UIUtils.setColumnWidth(table, 0, 90);
        UIUtils.setColumnWidth(table, 1, 160);

        Runnable refreshCatTable = () -> {
            try {
                model.setRowCount(0);
                List<Category> list = categoryService.getAllCategories();
                for (Category c : list) {
                    model.addRow(new Object[]{c.getCategoryId(), c.getName(), c.getDescription()});
                }
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Lỗi tải thể loại: " + ex.getMessage());
            }
        };

        refreshCatTable.run();
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAdd = new JButton("Thêm thể loại");
        JButton btnEdit = new JButton("Sửa thể loại");
        JButton btnDel = new JButton("Xóa thể loại");
        JButton btnClose = new JButton("Đóng");

        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Nhập tên thể loại mới:", "Thêm thể loại", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                String desc = JOptionPane.showInputDialog(dialog, "Nhập mô tả thể loại:", "Thêm thể loại", JOptionPane.PLAIN_MESSAGE);
                try {
                    categoryService.createCategory(name.trim(), desc != null ? desc.trim() : "");
                    UIUtils.showSuccess(dialog, "Thêm thể loại thành công!");
                    refreshCatTable.run();
                    initCategoryCache();
                    populateCategoryFilterCombo();
                    loadBooksTable();
                } catch (Exception ex) {
                    UIUtils.showError(dialog, "Lỗi thêm thể loại: " + ex.getMessage());
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIUtils.showWarning(dialog, "Vui lòng chọn thể loại cần sửa.");
                return;
            }
            int catId = (Integer) model.getValueAt(row, 0);
            String currName = (String) model.getValueAt(row, 1);
            String currDesc = (String) model.getValueAt(row, 2);

            String newName = JOptionPane.showInputDialog(dialog, "Tên thể loại:", currName);
            if (newName != null && !newName.trim().isEmpty()) {
                String newDesc = JOptionPane.showInputDialog(dialog, "Mô tả thể loại:", currDesc != null ? currDesc : "");
                try {
                    categoryService.updateCategory(catId, newName.trim(), newDesc != null ? newDesc.trim() : "");
                    UIUtils.showSuccess(dialog, "Cập nhật thể loại thành công!");
                    refreshCatTable.run();
                    initCategoryCache();
                    populateCategoryFilterCombo();
                    loadBooksTable();
                } catch (Exception ex) {
                    UIUtils.showError(dialog, "Lỗi sửa thể loại: " + ex.getMessage());
                }
            }
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIUtils.showWarning(dialog, "Vui lòng chọn thể loại cần xóa.");
                return;
            }
            int catId = (Integer) model.getValueAt(row, 0);
            String name = (String) model.getValueAt(row, 1);
            if (UIUtils.confirm(dialog, "Bạn có chắc chắn muốn xóa thể loại \"" + name + "\"?", "Xác nhận xóa")) {
                try {
                    categoryService.deleteCategory(catId);
                    UIUtils.showSuccess(dialog, "Đã xóa thể loại!");
                    refreshCatTable.run();
                    initCategoryCache();
                    populateCategoryFilterCombo();
                    loadBooksTable();
                } catch (Exception ex) {
                    UIUtils.showError(dialog, "Không thể xóa thể loại: " + ex.getMessage());
                }
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDel);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // =========================================================================
    // TAB 2: LẬP PHIẾU MƯỢN SÁCH (ACID TRANSACTIONS)
    // =========================================================================
    protected JPanel createBorrowTab() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(640, 520));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        formCard.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblHeader = new JLabel("LẬP PHIẾU MƯỢN SÁCH — GIAO DỊCH ACID");
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD, 16f));
        lblHeader.setForeground(new Color(24, 90, 157));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        formCard.add(lblHeader, gbc);

        gbc.gridwidth = 1;

        // Row 1: Member lookup
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        formCard.add(new JLabel("Mã/ID Độc giả:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        txtBorrowMemberCode = new JTextField();
        txtBorrowMemberCode.setToolTipText("Nhập mã độc giả (VD: MBR001) hoặc ID số");
        txtBorrowMemberCode.addActionListener(e -> validateBorrowMember());
        formCard.add(txtBorrowMemberCode, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.20;
        JButton btnCheckMember = new JButton("Kiểm tra");
        btnCheckMember.addActionListener(e -> validateBorrowMember());
        formCard.add(btnCheckMember, gbc);

        // Member status box
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblBorrowMemberInfo = new JLabel("<html><i>Nhập mã độc giả và nhấn Kiểm tra để xác thực điều kiện mượn...</i></html>");
        lblBorrowMemberInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        lblBorrowMemberInfo.setBackground(new Color(248, 249, 250));
        lblBorrowMemberInfo.setOpaque(true);
        formCard.add(lblBorrowMemberInfo, gbc);

        // Row 3: Book lookup
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        formCard.add(new JLabel("Mã ID/ISBN Sách:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        txtBorrowBookId = new JTextField();
        txtBorrowBookId.setToolTipText("Nhập ID sách hoặc mã ISBN");
        txtBorrowBookId.addActionListener(e -> validateBorrowBook());
        formCard.add(txtBorrowBookId, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.20;
        JButton btnCheckBook = new JButton("Kiểm tra");
        btnCheckBook.addActionListener(e -> validateBorrowBook());
        formCard.add(btnCheckBook, gbc);

        // Book status box
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblBorrowBookInfo = new JLabel("<html><i>Nhập mã sách và nhấn Kiểm tra để xác thực số lượng bản sao...</i></html>");
        lblBorrowBookInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        lblBorrowBookInfo.setBackground(new Color(248, 249, 250));
        lblBorrowBookInfo.setOpaque(true);
        formCard.add(lblBorrowBookInfo, gbc);

        // Row 5: Borrow days
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        formCard.add(new JLabel("Thời hạn mượn:"), gbc);

        gbc.gridx = 1;
        spnBorrowDays = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dayPanel.setOpaque(false);
        dayPanel.add(spnBorrowDays);
        dayPanel.add(new JLabel("ngày (Mặc định 14 ngày)"));
        formCard.add(dayPanel, gbc);

        // Row 6: Submit Button
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 8, 8);
        JButton btnSubmitBorrow = new JButton("Xác nhận Lập Phiếu Mượn (ACID Transaction)");
        btnSubmitBorrow.setFont(btnSubmitBorrow.getFont().deriveFont(Font.BOLD, 14f));
        btnSubmitBorrow.setForeground(new Color(24, 90, 157));
        btnSubmitBorrow.setPreferredSize(new Dimension(280, 40));
        btnSubmitBorrow.addActionListener(e -> executeBorrowTransaction());
        formCard.add(btnSubmitBorrow, gbc);

        wrapper.add(formCard);
        return wrapper;
    }

    private void validateBorrowMember() {
        String input = txtBorrowMemberCode.getText().trim();
        if (input.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Mã độc giả (VD: MBR001) hoặc ID.");
            return;
        }

        try {
            User user;
            if (input.matches("\\d+")) {
                user = userService.getUserById(Integer.parseInt(input));
            } else {
                user = userService.getUserByCode(input);
            }

            if (!(user instanceof Member m)) {
                lblBorrowMemberInfo.setText("<html><b style='color:red;'>Người dùng này không phải là Độc giả (Member).</b></html>");
                currentBorrowMember = null;
                return;
            }

            currentBorrowMember = m;
            boolean hasFines = fineService.hasUnpaidFines(m.getUserId());
            boolean active = m.getStatus() == UserStatus.ACTIVE;
            boolean quotaAvailable = m.canBorrow();

            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b>Họ tên:</b> ").append(m.getFullName()).append(" (Mã: ").append(m.getUserCode()).append(")<br>");
            sb.append("<b>Trạng thái:</b> ").append(active ? "<span style='color:green;'>ACTIVE</span>" : "<span style='color:red;'>LOCKED (Bị khóa)</span>").append(" | ");
            sb.append("<b>Đang mượn:</b> ").append(m.getCurrentBorrowCount()).append("/").append(m.getMaxBorrowLimit()).append(" cuốn | ");
            sb.append("<b>Nợ phạt:</b> ").append(hasFines ? "<span style='color:red;'>CÓ VÉ PHẠT CHƯA NỘP</span>" : "<span style='color:green;'>Không nợ</span>");

            if (!active || !quotaAvailable || hasFines) {
                sb.append("<br><b style='color:red;'>=> Không đủ điều kiện mượn sách!</b>");
            } else {
                sb.append("<br><b style='color:green;'>=> Đủ điều kiện mượn sách.</b>");
            }
            sb.append("</html>");

            lblBorrowMemberInfo.setText(sb.toString());
        } catch (Exception ex) {
            lblBorrowMemberInfo.setText("<html><b style='color:red;'>Lỗi: " + ex.getMessage() + "</b></html>");
            currentBorrowMember = null;
        }
    }

    private void validateBorrowBook() {
        String input = txtBorrowBookId.getText().trim();
        if (input.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập ID sách hoặc mã ISBN.");
            return;
        }

        try {
            Book book;
            if (input.matches("\\d+")) {
                book = bookService.getBookById(Integer.parseInt(input));
            } else {
                book = bookService.getBookByIsbn(input);
            }

            currentBorrowBook = book;
            String catName = categoryMap.getOrDefault(book.getCategoryId(), "Không rõ");
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b>Tiêu đề:</b> ").append(book.getTitle()).append("<br>");
            sb.append("<b>Tác giả:</b> ").append(book.getAuthor()).append(" | <b>Thể loại:</b> ").append(catName).append("<br>");
            sb.append("<b>Bản sao sẵn có:</b> ").append(book.getAvailableCopies()).append(" / ").append(book.getTotalCopies()).append(" | ");
            sb.append(book.isAvailable() ? "<span style='color:green;'><b>CÒN SÁCH</b></span>" : "<span style='color:red;'><b>HẾT SÁCH</b></span>");
            sb.append("</html>");

            lblBorrowBookInfo.setText(sb.toString());
        } catch (Exception ex) {
            lblBorrowBookInfo.setText("<html><b style='color:red;'>Lỗi: " + ex.getMessage() + "</b></html>");
            currentBorrowBook = null;
        }
    }

    private void executeBorrowTransaction() {
        if (currentBorrowMember == null) {
            validateBorrowMember();
            if (currentBorrowMember == null) {
                UIUtils.showWarning(this, "Vui lòng kiểm tra và xác thực Độc giả trước.");
                return;
            }
        }

        if (currentBorrowBook == null) {
            validateBorrowBook();
            if (currentBorrowBook == null) {
                UIUtils.showWarning(this, "Vui lòng kiểm tra và xác thực Đầu sách trước.");
                return;
            }
        }

        int days = (Integer) spnBorrowDays.getValue();

        try {
            BorrowRecord record = borrowService.borrowBook(currentBorrowMember.getUserId(), currentBorrowBook.getBookId(), staff.getUserId(), days);

            UIUtils.showSuccess(this, "Mượn sách thành công!\n" +
                    "- Mã phiếu mượn: #" + record.getRecordId() + "\n" +
                    "- Độc giả: " + currentBorrowMember.getFullName() + "\n" +
                    "- Sách: " + currentBorrowBook.getTitle() + "\n" +
                    "- Hạn trả: " + record.getDueDate() + "\n\n" +
                    "(Giao dịch ACID hoàn tất: Đã trừ tồn kho và tăng số sách mượn của độc giả)");

            // Reset form
            txtBorrowMemberCode.setText("");
            txtBorrowBookId.setText("");
            lblBorrowMemberInfo.setText("<html><i>Nhập mã độc giả và nhấn Kiểm tra để xác thực điều kiện mượn...</i></html>");
            lblBorrowBookInfo.setText("<html><i>Nhập mã sách và nhấn Kiểm tra để xác thực số lượng bản sao...</i></html>");
            currentBorrowMember = null;
            currentBorrowBook = null;

            loadBooksTable();
            loadReturnTable();
        } catch (BorrowException | ResourceNotFoundException | ValidationException be) {
            UIUtils.showError(this, "Không thể mượn sách: " + be.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Giao dịch lỗi hệ thống: " + ex.getMessage());
        }
    }

    // =========================================================================
    // TAB 3: QUẢN LÝ TRẢ SÁCH
    // =========================================================================
    protected JPanel createReturnTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Tìm phiếu mượn:"));
        txtReturnSearch = new JTextField(16);
        txtReturnSearch.setToolTipText("Lọc theo Mã phiếu, Mã độc giả, hoặc Mã sách");
        txtReturnSearch.addActionListener(e -> filterReturnTable());
        toolbar.add(txtReturnSearch);

        JButton btnFilter = new JButton("Lọc");
        btnFilter.addActionListener(e -> filterReturnTable());
        toolbar.add(btnFilter);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtReturnSearch.setText("");
            loadReturnTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(new JSeparator(JSeparator.VERTICAL));

        JButton btnReturn = new JButton("Xác nhận Trả sách");
        btnReturn.setFont(btnReturn.getFont().deriveFont(Font.BOLD, 13f));
        btnReturn.setForeground(new Color(24, 90, 157));
        btnReturn.addActionListener(e -> executeReturnBook());
        toolbar.add(btnReturn);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã phiếu", "Mã độc giả", "Tên độc giả", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Quá hạn (ngày)"};
        returnTableModel = new DefaultTableModel(columns, 0);
        returnTable = UIUtils.createReadonlyTable(returnTableModel);
        UIUtils.centerAlignColumns(returnTable, 0, 1, 3, 5, 6, 7, 8);
        UIUtils.setColumnWidth(returnTable, 0, 80);
        UIUtils.setColumnWidth(returnTable, 1, 90);
        UIUtils.setColumnWidth(returnTable, 2, 160);
        UIUtils.setColumnWidth(returnTable, 3, 70);
        UIUtils.setColumnWidth(returnTable, 4, 250);

        panel.add(new JScrollPane(returnTable), BorderLayout.CENTER);

        return panel;
    }

    public void loadReturnTable() {
        try {
            List<BorrowRecord> list = borrowService.getAllBorrowRecords();
            // Chỉ lấy các phiếu chưa trả (BORROWED hoặc OVERDUE)
            List<BorrowRecord> activeRecords = list.stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                    .toList();

            renderReturnTable(activeRecords);
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách phiếu mượn trả: " + e.getMessage());
        }
    }

    private void filterReturnTable() {
        String kw = txtReturnSearch.getText().trim().toLowerCase();
        try {
            List<BorrowRecord> list = borrowService.getAllBorrowRecords().stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                    .toList();

            if (kw.isEmpty()) {
                renderReturnTable(list);
                return;
            }

            List<BorrowRecord> filtered = list.stream().filter(r -> {
                String recId = String.valueOf(r.getRecordId());
                String bookId = String.valueOf(r.getBookId());
                String userId = String.valueOf(r.getUserId());
                return recId.contains(kw) || bookId.contains(kw) || userId.contains(kw);
            }).toList();

            renderReturnTable(filtered);
        } catch (Exception e) {
            UIUtils.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void renderReturnTable(List<BorrowRecord> records) {
        returnTableModel.setRowCount(0);
        LocalDate today = LocalDate.now();

        for (BorrowRecord r : records) {
            String memberName = "Độc giả #" + r.getUserId();
            String userCode = "";
            try {
                User u = userService.getUserById(r.getUserId());
                if (u != null) {
                    memberName = u.getFullName();
                    userCode = u.getUserCode();
                }
            } catch (Exception ignored) {
            }

            String bookTitle = "Sách #" + r.getBookId();
            try {
                Book b = bookService.getBookById(r.getBookId());
                if (b != null) {
                    bookTitle = b.getTitle();
                }
            } catch (Exception ignored) {
            }

            long overdueDays = 0;
            if (today.isAfter(r.getDueDate())) {
                overdueDays = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), today);
            }

            returnTableModel.addRow(new Object[]{
                    r.getRecordId(),
                    userCode,
                    memberName,
                    r.getBookId(),
                    bookTitle,
                    r.getBorrowDate(),
                    r.getDueDate(),
                    r.getStatus(),
                    overdueDays > 0 ? (overdueDays + " ngày trễ") : "Trong hạn"
            });
        }
    }

    private void executeReturnBook() {
        int row = returnTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showWarning(this, "Vui lòng chọn một phiếu mượn từ bảng để trả sách.");
            return;
        }

        int recordId = (Integer) returnTableModel.getValueAt(row, 0);
        String bookTitle = (String) returnTableModel.getValueAt(row, 4);

        if (!UIUtils.confirm(this, "Xác nhận nhận lại sách \"" + bookTitle + "\" cho phiếu #" + recordId + "?", "Xác nhận trả sách")) {
            return;
        }

        try {
            BorrowRecord updated = borrowService.returnBook(recordId);

            // Kiểm tra xem có vé phạt sinh ra do quá hạn không
            try {
                Fine fine = fineService.getFineByRecordId(recordId);
                if (fine != null && fine.getStatus() == FineStatus.UNPAID) {
                    UIUtils.showWarning(this, "Đã nhận lại sách thành công!\n\n" +
                            "CẢNH BÁO PHẠT QUÁ HẠN:\n" +
                            "- Sách đã bị trả trễ: " + fine.getOverdueDays() + " ngày\n" +
                            "- Mức phạt (2.000 VNĐ/ngày): " + UIUtils.formatCurrency(fine.getFineAmount()) + "\n" +
                            "- Trạng thái phạt: CHƯA THU (UNPAID)\n" +
                            "Vui lòng chuyển qua tab 'Quản lý Tiền Phạt' để thu tiền bạn đọc.");
                } else {
                    UIUtils.showSuccess(this, "Trả sách đúng hạn thành công! Tồn kho sách đã được cập nhật hoàn lại.");
                }
            } catch (Exception ignored) {
                UIUtils.showSuccess(this, "Trả sách thành công! Tồn kho sách đã được cập nhật hoàn lại.");
            }

            loadReturnTable();
            loadBooksTable();
            loadFinesTable();
        } catch (Exception ex) {
            UIUtils.showError(this, "Không thể trả sách: " + ex.getMessage());
        }
    }

    // =========================================================================
    // TAB 4: QUẢN LÝ TIỀN PHẠT
    // =========================================================================
    protected JPanel createFinesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Lọc theo trạng thái:"));

        cboFineStatusFilter = new JComboBox<>(new String[]{"-- Tất cả phiếu phạt --", "Chưa thu (UNPAID)", "Đã thu (PAID)"});
        cboFineStatusFilter.addActionListener(e -> loadFinesTable());
        toolbar.add(cboFineStatusFilter);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadFinesTable());
        toolbar.add(btnRefresh);

        toolbar.add(new JSeparator(JSeparator.VERTICAL));

        JButton btnPayFine = new JButton("Thu tiền phạt");
        btnPayFine.setFont(btnPayFine.getFont().deriveFont(Font.BOLD, 13f));
        btnPayFine.setForeground(new Color(24, 90, 157));
        btnPayFine.addActionListener(e -> executePayFine());
        toolbar.add(btnPayFine);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Mã phạt", "Mã phiếu mượn", "Mã độc giả", "Họ tên độc giả", "Số ngày trễ", "Số tiền phạt", "Lý do", "Trạng thái", "Ngày thu tiền"};
        fineTableModel = new DefaultTableModel(columns, 0);
        fineTable = UIUtils.createReadonlyTable(fineTableModel);
        UIUtils.centerAlignColumns(fineTable, 0, 1, 2, 4, 7, 8);
        UIUtils.rightAlignColumns(fineTable, 5);
        UIUtils.setColumnWidth(fineTable, 0, 70);
        UIUtils.setColumnWidth(fineTable, 1, 90);
        UIUtils.setColumnWidth(fineTable, 2, 90);
        UIUtils.setColumnWidth(fineTable, 3, 160);

        panel.add(new JScrollPane(fineTable), BorderLayout.CENTER);
        return panel;
    }

    public void loadFinesTable() {
        try {
            int filterIdx = cboFineStatusFilter != null ? cboFineStatusFilter.getSelectedIndex() : 0;
            List<Fine> list = fineService.getAllFines();

            if (filterIdx == 1) { // UNPAID
                list = list.stream().filter(f -> f.getStatus() == FineStatus.UNPAID).toList();
            } else if (filterIdx == 2) { // PAID
                list = list.stream().filter(f -> f.getStatus() == FineStatus.PAID).toList();
            }

            fineTableModel.setRowCount(0);
            for (Fine f : list) {
                String memberName = "Độc giả #" + f.getUserId();
                String memberCode = "";
                try {
                    User u = userService.getUserById(f.getUserId());
                    if (u != null) {
                        memberName = u.getFullName();
                        memberCode = u.getUserCode();
                    }
                } catch (Exception ignored) {
                }

                fineTableModel.addRow(new Object[]{
                        f.getFineId(),
                        f.getRecordId(),
                        memberCode,
                        memberName,
                        f.getOverdueDays() + " ngày",
                        UIUtils.formatCurrency(f.getFineAmount()),
                        f.getReason(),
                        f.getStatus(),
                        f.getPaidAt() != null ? f.getPaidAt().toLocalDate() : "Chưa thu"
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách tiền phạt: " + e.getMessage());
        }
    }

    private void executePayFine() {
        int row = fineTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showWarning(this, "Vui lòng chọn một phiếu phạt cần thu tiền từ bảng.");
            return;
        }

        int fineId = (Integer) fineTableModel.getValueAt(row, 0);
        String memberName = (String) fineTableModel.getValueAt(row, 3);
        String amount = (String) fineTableModel.getValueAt(row, 5);
        FineStatus status = (FineStatus) fineTableModel.getValueAt(row, 7);

        if (status == FineStatus.PAID) {
            UIUtils.showWarning(this, "Phiếu phạt #" + fineId + " đã được thanh toán trước đó.");
            return;
        }

        if (!UIUtils.confirm(this, "Xác nhận thu số tiền " + amount + " từ độc giả \"" + memberName + "\" cho phiếu phạt #" + fineId + "?", "Xác nhận thu tiền phạt")) {
            return;
        }

        try {
            fineService.payFine(fineId);
            UIUtils.showSuccess(this, "Thu tiền phạt thành công! Phiếu phạt #" + fineId + " đã chuyển sang trạng thái PAID.");
            loadFinesTable();
        } catch (Exception ex) {
            UIUtils.showError(this, "Lỗi thanh toán phạt: " + ex.getMessage());
        }
    }

    public void loadAllInitialData() {
        loadBooksTable();
        loadReturnTable();
        loadFinesTable();
    }

    protected void handleLogout() {
        if (UIUtils.confirm(this, "Bạn có chắc chắn muốn đăng xuất không?", "Xác nhận đăng xuất")) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(bookService, categoryService, borrowService, fineService, userService).setVisible(true));
        }
    }

    protected static class CategoryItem {
        final int id;
        final String name;

        CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
