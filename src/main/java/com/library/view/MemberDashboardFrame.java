package com.library.view;

import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện dành riêng cho Độc giả thư viện (Member Dashboard).
 * Cung cấp chức năng: Tra cứu sách, Xem lịch sử mượn trả & vé phạt cá nhân, và Thông tin tài khoản.
 * Tuân thủ Loose Coupling: Chỉ tương tác qua tầng Service.
 */
public class MemberDashboardFrame extends JFrame {

    private final Member member;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

    // Cache Category ID -> Name để hiển thị nhanh
    private final Map<Integer, String> categoryMap = new HashMap<>();

    // Components
    private JLabel lblGreeting;
    private JLabel lblBorrowQuota;
    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private JComboBox<CategoryItem> cboCategories;
    private JTextField txtSearchKeyword;

    private DefaultTableModel borrowTableModel;
    private JTable borrowTable;

    private DefaultTableModel fineTableModel;
    private JTable fineTable;

    public MemberDashboardFrame(Member member,
                                BookService bookService,
                                CategoryService categoryService,
                                BorrowService borrowService,
                                FineService fineService,
                                UserService userService) {
        this.member = member;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;

        initCategoryCache();
        initComponents();
        loadBookData();
        loadBorrowData();
        loadFineData();
    }

    private void initCategoryCache() {
        categoryMap.clear();
        try {
            List<Category> categories = categoryService.getAllCategories();
            for (Category cat : categories) {
                categoryMap.put(cat.getCategoryId(), cat.getName());
            }
        } catch (Exception ignored) {
        }
    }

    private void initComponents() {
        setTitle("Hệ thống Quản lý Thư viện — Cổng thông tin Bạn đọc (" + member.getUserCode() + ")");
        setSize(980, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel topHeader = new JPanel(new BorderLayout(15, 10));
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        topHeader.setBackground(Color.WHITE);

        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        userInfoPanel.setOpaque(false);

        lblGreeting = new JLabel("Xin chào, " + member.getFullName() + " (" + member.getUserCode() + ")");
        lblGreeting.setFont(lblGreeting.getFont().deriveFont(Font.BOLD, 17f));
        lblGreeting.setForeground(new Color(24, 90, 157));

        int borrowed = member.getCurrentBorrowCount();
        int max = member.getMaxBorrowLimit();
        lblBorrowQuota = new JLabel("Hạn mức mượn: Đang mượn " + borrowed + "/" + max + " cuốn | Trạng thái: " + member.getStatus());
        lblBorrowQuota.setFont(lblBorrowQuota.getFont().deriveFont(Font.PLAIN, 13f));
        lblBorrowQuota.setForeground(Color.DARK_GRAY);

        userInfoPanel.add(lblGreeting);
        userInfoPanel.add(lblBorrowQuota);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> handleLogout());

        topHeader.add(userInfoPanel, BorderLayout.CENTER);
        topHeader.add(btnLogout, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13f));
        tabbedPane.addTab("Tra cứu & Tìm kiếm Sách", createBookSearchTab());
        tabbedPane.addTab("Phiếu mượn & Tiền phạt cá nhân", createBorrowAndFineTab());
        tabbedPane.addTab("Thông tin tài khoản", createAccountInfoTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBookSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.add(new JLabel("Từ khóa tìm kiếm:"));

        txtSearchKeyword = new JTextField(18);
        txtSearchKeyword.addActionListener(e -> performBookSearch());
        filterBar.add(txtSearchKeyword);

        filterBar.add(new JLabel("Thể loại:"));
        cboCategories = new JComboBox<>();
        cboCategories.addItem(new CategoryItem(0, "-- Tất cả thể loại --"));
        try {
            List<Category> categories = categoryService.getAllCategories();
            for (Category c : categories) {
                cboCategories.addItem(new CategoryItem(c.getCategoryId(), c.getName()));
            }
        } catch (Exception ignored) {
        }
        filterBar.add(cboCategories);

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> performBookSearch());
        filterBar.add(btnSearch);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtSearchKeyword.setText("");
            cboCategories.setSelectedIndex(0);
            loadBookData();
        });
        filterBar.add(btnRefresh);

        panel.add(filterBar, BorderLayout.NORTH);

        // Books Table
        String[] columns = {"ID", "Mã ISBN", "Tiêu đề sách", "Tác giả", "Thể loại", "Năm XB", "Còn lại / Tổng", "Tình trạng"};
        bookTableModel = new DefaultTableModel(columns, 0);
        bookTable = UIUtils.createReadonlyTable(bookTableModel);

        UIUtils.centerAlignColumns(bookTable, 0, 1, 5, 6, 7);
        UIUtils.setColumnWidth(bookTable, 0, 50);
        UIUtils.setColumnWidth(bookTable, 1, 120);
        UIUtils.setColumnWidth(bookTable, 2, 280);
        UIUtils.setColumnWidth(bookTable, 3, 160);
        UIUtils.setColumnWidth(bookTable, 4, 120);
        UIUtils.setColumnWidth(bookTable, 5, 80);
        UIUtils.setColumnWidth(bookTable, 6, 100);
        UIUtils.setColumnWidth(bookTable, 7, 100);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBorrowAndFineTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Part 1: Borrow records
        JPanel borrowPanel = new JPanel(new BorderLayout(5, 5));
        JLabel lblBorrowTitle = new JLabel("Lịch sử phiếu mượn sách");
        lblBorrowTitle.setFont(lblBorrowTitle.getFont().deriveFont(Font.BOLD, 14f));
        borrowPanel.add(lblBorrowTitle, BorderLayout.NORTH);

        String[] borrowCols = {"Mã phiếu", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Ngày trả thực tế", "Trạng thái"};
        borrowTableModel = new DefaultTableModel(borrowCols, 0);
        borrowTable = UIUtils.createReadonlyTable(borrowTableModel);
        UIUtils.centerAlignColumns(borrowTable, 0, 1, 3, 4, 5, 6);
        UIUtils.setColumnWidth(borrowTable, 0, 80);
        UIUtils.setColumnWidth(borrowTable, 1, 70);
        UIUtils.setColumnWidth(borrowTable, 2, 280);

        borrowPanel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        // Part 2: Fines
        JPanel finePanel = new JPanel(new BorderLayout(5, 5));
        JLabel lblFineTitle = new JLabel("Danh sách vé phạt trễ hạn");
        lblFineTitle.setFont(lblFineTitle.getFont().deriveFont(Font.BOLD, 14f));
        finePanel.add(lblFineTitle, BorderLayout.NORTH);

        String[] fineCols = {"Mã phạt", "Mã phiếu mượn", "Số ngày trễ", "Số tiền phạt", "Lý do", "Trạng thái", "Ngày đóng phạt"};
        fineTableModel = new DefaultTableModel(fineCols, 0);
        fineTable = UIUtils.createReadonlyTable(fineTableModel);
        UIUtils.centerAlignColumns(fineTable, 0, 1, 2, 5, 6);
        UIUtils.rightAlignColumns(fineTable, 3);

        finePanel.add(new JScrollPane(fineTable), BorderLayout.CENTER);

        panel.add(borrowPanel);
        panel.add(finePanel);

        return panel;
    }

    private JPanel createAccountInfoTab() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 25));

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 360));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        card.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HỒ SƠ BẠN ĐỌC THƯ VIỆN");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(24, 90, 157));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        addInfoRow(card, gbc, row++, "Mã độc giả:", member.getUserCode());
        addInfoRow(card, gbc, row++, "Họ và tên:", member.getFullName());
        addInfoRow(card, gbc, row++, "Email:", member.getEmail() != null ? member.getEmail() : "Chưa cập nhật");
        addInfoRow(card, gbc, row++, "Số điện thoại:", member.getPhone() != null ? member.getPhone() : "Chưa cập nhật");
        addInfoRow(card, gbc, row++, "Hạn mức mượn tối đa:", member.getMaxBorrowLimit() + " cuốn sách");
        addInfoRow(card, gbc, row++, "Số sách đang mượn:", member.getCurrentBorrowCount() + " cuốn");
        addInfoRow(card, gbc, row++, "Trạng thái thẻ:", member.getStatus().name() + " (" + (member.getStatus() == UserStatus.ACTIVE ? "Đang hoạt động" : "Bị khóa") + ")");

        wrapper.add(card);
        return wrapper;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.PLAIN, 13f));
        panel.add(val, gbc);
    }

    private void performBookSearch() {
        String keyword = txtSearchKeyword.getText().trim();
        CategoryItem selectedCat = (CategoryItem) cboCategories.getSelectedItem();
        int catId = selectedCat != null ? selectedCat.id : 0;

        try {
            List<Book> books;
            if (!keyword.isEmpty()) {
                books = bookService.searchBooks(keyword);
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
            UIUtils.showError(this, "Lỗi khi tìm kiếm sách: " + e.getMessage());
        }
    }

    private void loadBookData() {
        try {
            List<Book> books = bookService.getAllBooks();
            renderBookTable(books);
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách sách: " + e.getMessage());
        }
    }

    private void renderBookTable(List<Book> books) {
        bookTableModel.setRowCount(0);
        for (Book b : books) {
            String catName = categoryMap.getOrDefault(b.getCategoryId(), "Không rõ");
            String status = b.isAvailable() ? "Còn sách" : "Hết sách";
            bookTableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    catName,
                    b.getPublishYear(),
                    b.getAvailableCopies() + " / " + b.getTotalCopies(),
                    status
            });
        }
    }

    private void loadBorrowData() {
        try {
            List<BorrowRecord> records = borrowService.getBorrowRecordsByUserId(member.getUserId());
            borrowTableModel.setRowCount(0);
            for (BorrowRecord r : records) {
                String bookTitle = "Mã sách #" + r.getBookId();
                try {
                    Book b = bookService.getBookById(r.getBookId());
                    if (b != null) {
                        bookTitle = b.getTitle();
                    }
                } catch (Exception ignored) {
                }

                borrowTableModel.addRow(new Object[]{
                        "#" + r.getRecordId(),
                        r.getBookId(),
                        bookTitle,
                        r.getBorrowDate(),
                        r.getDueDate(),
                        r.getReturnDate() != null ? r.getReturnDate() : "Chưa trả",
                        r.getStatus()
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải lịch sử mượn sách: " + e.getMessage());
        }
    }

    private void loadFineData() {
        try {
            List<Fine> fines = fineService.getFinesByUserId(member.getUserId());
            fineTableModel.setRowCount(0);
            for (Fine f : fines) {
                fineTableModel.addRow(new Object[]{
                        "#" + f.getFineId(),
                        "#" + f.getRecordId(),
                        f.getOverdueDays() + " ngày",
                        UIUtils.formatCurrency(f.getFineAmount()),
                        f.getReason(),
                        f.getStatus(),
                        f.getPaidAt() != null ? f.getPaidAt().toLocalDate() : "Chưa nộp"
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách tiền phạt: " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (UIUtils.confirm(this, "Bạn có chắc chắn muốn đăng xuất không?", "Xác nhận đăng xuất")) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(bookService, categoryService, borrowService, fineService, userService).setVisible(true));
        }
    }

    private static class CategoryItem {
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
