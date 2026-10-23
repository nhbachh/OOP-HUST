package com.library.view;

import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cổng thông tin Bạn đọc Bách Khoa — HUST Lib Modern Member Dashboard.
 * Cung cấp chức năng: Tra cứu sách theo ngành/thể loại, theo dõi sách đang mượn,
 * xem thông báo phạt cá nhân và thông tin thẻ bạn đọc HUST.
 * Tuân thủ Loose Coupling: Tương tác 100% qua tầng Service.
 */
public class MemberDashboardFrame extends JFrame {

    private final Member member;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

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
        setTitle("HUST Lib — Cổng Thông Tin Bạn Đọc Bách Khoa (" + member.getUserCode() + ")");
        setSize(1020, 700);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel HUST Red
        JPanel topHeader = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền gradient Đỏ Bách Khoa
                GradientPaint gp = new GradientPaint(0, 0, HustTheme.PRIMARY_RED_DARK, getWidth(), getHeight(), HustTheme.PRIMARY_RED);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Điểm xuyết viền vàng kỹ thuật dưới đáy
                g2.setColor(HustTheme.ACCENT_GOLD);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);

                g2.dispose();
            }
        };
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        userInfoPanel.setOpaque(false);

        lblGreeting = new JLabel("Xin chào, " + member.getFullName() + " (Mã: " + member.getUserCode() + ")");
        lblGreeting.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblGreeting.setForeground(Color.WHITE);

        int borrowed = member.getCurrentBorrowCount();
        int max = member.getMaxBorrowLimit();
        lblBorrowQuota = new JLabel("Hạn mức mượn: Đang giữ " + borrowed + "/" + max + " cuốn | Trạng thái thẻ: " +
                (member.getStatus() == UserStatus.ACTIVE ? "Đang hoạt động" : "Bị khóa"));
        lblBorrowQuota.setFont(HustTheme.FONT_SMALL);
        lblBorrowQuota.setForeground(new Color(254, 243, 199));

        userInfoPanel.add(lblGreeting);
        userInfoPanel.add(lblBorrowQuota);

        JButton btnLogout = new JButton("🚪  Đăng xuất");
        btnLogout.setFont(HustTheme.FONT_BODY_BOLD);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(127, 29, 29));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202, 100), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        topHeader.add(userInfoPanel, BorderLayout.CENTER);
        topHeader.add(btnLogout, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // JTabbedPane phong cách hiện đại
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HustTheme.FONT_BODY_BOLD);
        tabbedPane.setBackground(HustTheme.CONTENT_BG);

        tabbedPane.addTab("📖  Tra cứu & Tìm kiếm Sách", createBookSearchTab());
        tabbedPane.addTab("📑  Phiếu mượn & Vé phạt cá nhân", createBorrowAndFineTab());
        tabbedPane.addTab("👤  Hồ sơ bạn đọc HUST", createAccountInfoTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBookSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Từ khóa tìm kiếm:"));

        txtSearchKeyword = new JTextField(18);
        txtSearchKeyword.putClientProperty("JTextField.placeholderText", "Tên sách, tác giả, ISBN...");
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

        JButton btnSearch = UIUtils.createPrimaryButton("Tìm kiếm");
        btnSearch.addActionListener(e -> performBookSearch());
        filterBar.add(btnSearch);

        JButton btnRefresh = UIUtils.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtSearchKeyword.setText("");
            cboCategories.setSelectedIndex(0);
            loadBookData();
        });
        filterBar.add(btnRefresh);

        panel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Mã ISBN", "Tiêu đề sách", "Tác giả", "Thể loại", "Năm XB", "Còn lại / Tổng", "Tình trạng"};
        bookTableModel = new DefaultTableModel(columns, 0);
        bookTable = UIUtils.createReadonlyTable(bookTableModel);

        UIUtils.centerAlignColumns(bookTable, 0, 1, 5, 6);
        UIUtils.setStatusPillRenderer(bookTable, 7);

        UIUtils.setColumnWidth(bookTable, 0, 50);
        UIUtils.setColumnWidth(bookTable, 1, 120);
        UIUtils.setColumnWidth(bookTable, 2, 280);
        UIUtils.setColumnWidth(bookTable, 3, 160);
        UIUtils.setColumnWidth(bookTable, 4, 130);
        UIUtils.setColumnWidth(bookTable, 7, 110);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBorrowAndFineTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 14, 14));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Part 1: Borrow records
        JPanel borrowPanel = new JPanel(new BorderLayout(6, 6));
        borrowPanel.setOpaque(false);
        JLabel lblBorrowTitle = new JLabel("Lịch sử phiếu mượn sách");
        lblBorrowTitle.setFont(HustTheme.FONT_SUBHEADER);
        lblBorrowTitle.setForeground(HustTheme.PRIMARY_RED);
        borrowPanel.add(lblBorrowTitle, BorderLayout.NORTH);

        String[] borrowCols = {"Mã phiếu", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Ngày trả thực tế", "Trạng thái"};
        borrowTableModel = new DefaultTableModel(borrowCols, 0);
        borrowTable = UIUtils.createReadonlyTable(borrowTableModel);
        UIUtils.centerAlignColumns(borrowTable, 0, 1, 3, 4, 5);
        UIUtils.setStatusPillRenderer(borrowTable, 6);

        UIUtils.setColumnWidth(borrowTable, 0, 80);
        UIUtils.setColumnWidth(borrowTable, 1, 70);
        UIUtils.setColumnWidth(borrowTable, 2, 280);

        JScrollPane sp1 = new JScrollPane(borrowTable);
        sp1.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        borrowPanel.add(sp1, BorderLayout.CENTER);

        // Part 2: Fines
        JPanel finePanel = new JPanel(new BorderLayout(6, 6));
        finePanel.setOpaque(false);
        JLabel lblFineTitle = new JLabel("Danh sách vé phạt trễ hạn cá nhân");
        lblFineTitle.setFont(HustTheme.FONT_SUBHEADER);
        lblFineTitle.setForeground(HustTheme.PRIMARY_RED);
        finePanel.add(lblFineTitle, BorderLayout.NORTH);

        String[] fineCols = {"Mã phạt", "Mã phiếu mượn", "Số ngày trễ", "Số tiền phạt", "Lý do phạt", "Trạng thái", "Ngày đóng phạt"};
        fineTableModel = new DefaultTableModel(fineCols, 0);
        fineTable = UIUtils.createReadonlyTable(fineTableModel);
        UIUtils.centerAlignColumns(fineTable, 0, 1, 2, 6);
        UIUtils.rightAlignColumns(fineTable, 3);
        UIUtils.setStatusPillRenderer(fineTable, 5);

        JScrollPane sp2 = new JScrollPane(fineTable);
        sp2.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        finePanel.add(sp2, BorderLayout.CENTER);

        panel.add(borrowPanel);
        panel.add(finePanel);

        return panel;
    }

    private JPanel createAccountInfoTab() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 25));
        wrapper.setBackground(HustTheme.CONTENT_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(540, 370));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HỒ SƠ BẠN ĐỌC THƯ VIỆN HUST");
        title.setFont(HustTheme.FONT_HEADER);
        title.setForeground(HustTheme.PRIMARY_RED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        addInfoRow(card, gbc, row++, "Mã độc giả / MSSV:", member.getUserCode());
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
        lbl.setFont(HustTheme.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel(value);
        val.setFont(HustTheme.FONT_BODY);
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
