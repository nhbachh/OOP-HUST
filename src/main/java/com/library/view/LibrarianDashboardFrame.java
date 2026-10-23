package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bàn làm việc Thủ thư — Phong cách Bách Khoa Hiện Đại (HUST Lib Modern Dashboard).
 * Bố cục: Thanh Sidebar bên trái (Dark Burgundy), Thanh Header trên cùng với Breadcrumb & Tìm kiếm,
 * Vùng nội dung trung tâm chuyển đổi mượt mà qua CardLayout.
 * Tuân thủ Loose Coupling: 100% tương tác qua Service Layer.
 */
public class LibrarianDashboardFrame extends JFrame {

    protected final Staff staff;
    protected final BookService bookService;
    protected final CategoryService categoryService;
    protected final BorrowService borrowService;
    protected final FineService fineService;
    protected final UserService userService;

    // CardLayout cho vùng nội dung chính
    protected CardLayout cardLayout;
    protected JPanel contentCardPanel;

    // Danh sách các nút menu trên Sidebar để quản lý trạng thái active
    protected final List<JButton> sidebarButtons = new ArrayList<>();
    protected JPanel sidebarMenuContainer;

    protected JLabel lblBreadcrumb;
    protected final Map<Integer, String> categoryMap = new HashMap<>();

    // Các hằng số định danh Card
    public static final String CARD_OVERVIEW = "OVERVIEW";
    public static final String CARD_BOOKS = "BOOKS";
    public static final String CARD_BORROW = "BORROW";
    public static final String CARD_RETURN = "RETURN";
    public static final String CARD_FINES = "FINES";

    // Components Tab Sách
    protected DefaultTableModel bookTableModel;
    protected JTable bookTable;
    protected JTextField txtBookSearch;
    protected JComboBox<CategoryItem> cboBookCategoryFilter;

    // Components Tab Mượn Sách
    protected JTextField txtBorrowMemberCode;
    protected JLabel lblBorrowMemberInfo;
    protected Member currentBorrowMember;
    protected JTextField txtBorrowBookId;
    protected JLabel lblBorrowBookInfo;
    protected Book currentBorrowBook;
    protected JSpinner spnBorrowDays;

    // Components Tab Trả Sách
    protected DefaultTableModel returnTableModel;
    protected JTable returnTable;
    protected JTextField txtReturnSearch;

    // Components Tab Tiền Phạt
    protected DefaultTableModel fineTableModel;
    protected JTable fineTable;
    protected JComboBox<String> cboFineStatusFilter;

    // Overview KPI container
    protected JPanel overviewCardsPanel;

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
        setSize(1140, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. THANH SIDEBAR BÊN TRÁI (DARK BURGUNDY)
        JPanel sidebar = createSidebarPanel();
        add(sidebar, BorderLayout.WEST);

        // 2. KHU VỰC CHÍNH (TOP BAR + CONTENT CARDS)
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(HustTheme.CONTENT_BG);

        // Top bar
        JPanel topBar = createTopBar();
        mainArea.add(topBar, BorderLayout.NORTH);

        // Content Area with CardLayout
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(HustTheme.CONTENT_BG);

        // Khởi tạo các Card
        initCards(contentCardPanel);

        mainArea.add(contentCardPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // Chọn mặc định mục đầu tiên
        switchCard(CARD_OVERVIEW, "Trang chủ / Tổng quan", sidebarButtons.get(0));
    }

    protected String getFrameTitle() {
        return "HUST Lib — Bàn làm việc Thủ thư (" + staff.getUserCode() + " - " + staff.getFullName() + ")";
    }

    // =========================================================================
    // 1. THANH ĐIỀU HƯỚNG BÊN TRÁI (SIDEBAR)
    // =========================================================================
    protected JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(HustTheme.SIDEBAR_BG);

        // Phần đầu Sidebar: Logo HUST Lib
        JPanel brandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Điểm xuyết viền đỏ tinh tế
                g2.setColor(HustTheme.PRIMARY_RED);
                g2.fillRect(0, 0, 4, getHeight());

                // Biểu tượng sách & bánh răng nhỏ ở góc logo
                int ix = 24, iy = 26;
                g2.setColor(HustTheme.ACCENT_GOLD);
                g2.drawOval(ix + 6, iy + 6, 16, 16);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(ix, iy, 28, 22, 6, 6);

                g2.dispose();
            }
        };
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 60, 20, 16));

        JLabel lblBrandName = new JLabel("HUST Lib");
        lblBrandName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        lblBrandName.setForeground(Color.WHITE);

        JLabel lblBrandUni = new JLabel("ĐH BÁCH KHOA HÀ NỘI");
        lblBrandUni.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        lblBrandUni.setForeground(HustTheme.ACCENT_GOLD);

        brandPanel.add(lblBrandName);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblBrandUni);

        // Profile chip người dùng
        JPanel profileChip = new JPanel(new BorderLayout(10, 0));
        profileChip.setBackground(new Color(39, 39, 42));
        profileChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 14, 15, 14),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                )
        ));

        JLabel lblAvatar = new JLabel("👤");
        lblAvatar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        lblAvatar.setForeground(Color.WHITE);

        JPanel profileText = new JPanel(new GridLayout(2, 1, 2, 2));
        profileText.setOpaque(false);

        JLabel lblName = new JLabel(staff.getFullName());
        lblName.setFont(HustTheme.FONT_BODY_BOLD);
        lblName.setForeground(Color.WHITE);

        JLabel lblRoleBadge = new JLabel(getRoleBadgeText());
        lblRoleBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        lblRoleBadge.setForeground(new Color(254, 202, 202));

        profileText.add(lblName);
        profileText.add(lblRoleBadge);

        profileChip.add(lblAvatar, BorderLayout.WEST);
        profileChip.add(profileText, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(brandPanel, BorderLayout.NORTH);
        topContainer.add(profileChip, BorderLayout.SOUTH);

        sidebar.add(topContainer, BorderLayout.NORTH);

        // Danh sách Menu
        sidebarMenuContainer = new JPanel();
        sidebarMenuContainer.setLayout(new BoxLayout(sidebarMenuContainer, BoxLayout.Y_AXIS));
        sidebarMenuContainer.setOpaque(false);
        sidebarMenuContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        populateSidebarMenuItems();

        sidebar.add(sidebarMenuContainer, BorderLayout.CENTER);

        // Nút Đăng xuất ở dưới cùng
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(12, 14, 20, 14));

        JButton btnLogout = new JButton("🚪  Đăng xuất");
        btnLogout.setFont(HustTheme.FONT_BODY_BOLD);
        btnLogout.setForeground(new Color(244, 63, 94));
        btnLogout.setBackground(new Color(39, 39, 42));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(244, 63, 94, 80), 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        bottomPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    protected String getRoleBadgeText() {
        return "THỦ THƯ • " + staff.getUserCode();
    }

    protected void populateSidebarMenuItems() {
        addSidebarItem("📊  Tổng quan", CARD_OVERVIEW, "Trang chủ / Tổng quan & Chỉ số");
        addSidebarItem("📚  Quản lý Sách", CARD_BOOKS, "Trang chủ / Quản lý Sách & Thể loại");
        addSidebarItem("📝  Lập Phiếu Mượn", CARD_BORROW, "Trang chủ / Lập Phiếu Mượn Sách (ACID)");
        addSidebarItem("📥  Quản lý Trả Sách", CARD_RETURN, "Trang chủ / Quản lý Trả Sách");
        addSidebarItem("💰  Vé Phạt & Thu Phí", CARD_FINES, "Trang chủ / Quản lý Thu Tiền Phạt");
    }

    protected void addSidebarItem(String title, String cardName, String breadcrumbText) {
        JButton btn = new JButton(title);
        btn.setFont(HustTheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(228, 228, 231));
        btn.setBackground(HustTheme.SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> switchCard(cardName, breadcrumbText, btn));

        sidebarButtons.add(btn);
        sidebarMenuContainer.add(btn);
        sidebarMenuContainer.add(Box.createVerticalStrut(4));
    }

    protected void switchCard(String cardName, String breadcrumbText, JButton activeBtn) {
        cardLayout.show(contentCardPanel, cardName);
        if (lblBreadcrumb != null) {
            lblBreadcrumb.setText(breadcrumbText);
        }

        // Cập nhật style nút active
        for (JButton b : sidebarButtons) {
            if (b == activeBtn) {
                b.setBackground(HustTheme.PRIMARY_RED);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(HustTheme.SIDEBAR_BG);
                b.setForeground(new Color(228, 228, 231));
            }
        }
    }

    // =========================================================================
    // 2. THANH HEADER TRÊN CÙNG (TOP BAR)
    // =========================================================================
    protected JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HustTheme.BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Breadcrumbs bên trái
        lblBreadcrumb = new JLabel("Trang chủ / Tổng quan");
        lblBreadcrumb.setFont(HustTheme.FONT_BODY_BOLD);
        lblBreadcrumb.setForeground(HustTheme.TEXT_PRIMARY);
        topBar.add(lblBreadcrumb, BorderLayout.WEST);

        // Phần bên phải: Ngày tháng & Tìm kiếm nhanh
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightBar.setOpaque(false);

        LocalDate now = LocalDate.now();
        String dateStr = "📅 " + now.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new java.util.Locale("vi", "VN")));
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(HustTheme.FONT_SMALL);
        lblDate.setForeground(HustTheme.TEXT_MUTED);

        rightBar.add(lblDate);
        topBar.add(rightBar, BorderLayout.EAST);

        return topBar;
    }

    // =========================================================================
    // 3. KHỞI TẠO CÁC CARD TRONG NỘI DUNG CHÍNH
    // =========================================================================
    protected void initCards(JPanel cardPanel) {
        cardPanel.add(createOverviewCard(), CARD_OVERVIEW);
        cardPanel.add(createBooksCard(), CARD_BOOKS);
        cardPanel.add(createBorrowCard(), CARD_BORROW);
        cardPanel.add(createReturnCard(), CARD_RETURN);
        cardPanel.add(createFinesCard(), CARD_FINES);
    }

    // CARD TỔNG QUAN (OVERVIEW & METRICS)
    protected JPanel createOverviewCard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Header
        JPanel head = UIUtils.createHeaderPanel("TỔNG QUAN CHỈ SỐ HOẠT ĐỘNG THƯ VIỆN HUST",
                "Số liệu tổng hợp thời gian thực của hệ thống thư viện Bách Khoa");
        panel.add(head, BorderLayout.NORTH);

        // Center: 4 Stat Cards
        overviewCardsPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        overviewCardsPanel.setOpaque(false);

        panel.add(overviewCardsPanel, BorderLayout.CENTER);

        return panel;
    }

    // CARD QUẢN LÝ SÁCH
    protected JPanel createBooksCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Tìm kiếm:"));
        txtBookSearch = new JTextField(16);
        txtBookSearch.putClientProperty("JTextField.placeholderText", "Nhập tiêu đề hoặc ISBN...");
        txtBookSearch.addActionListener(e -> searchBooks());
        toolbar.add(txtBookSearch);

        toolbar.add(new JLabel("Thể loại:"));
        cboBookCategoryFilter = new JComboBox<>();
        populateCategoryFilterCombo();
        toolbar.add(cboBookCategoryFilter);

        JButton btnSearch = UIUtils.createSecondaryButton("Tìm");
        btnSearch.addActionListener(e -> searchBooks());
        toolbar.add(btnSearch);

        JButton btnRefresh = UIUtils.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtBookSearch.setText("");
            cboBookCategoryFilter.setSelectedIndex(0);
            loadBooksTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(Box.createHorizontalStrut(10));

        JButton btnAddBook = UIUtils.createPrimaryButton("+ Thêm Sách Mới");
        btnAddBook.addActionListener(e -> showAddBookDialog());
        toolbar.add(btnAddBook);

        JButton btnEditBook = UIUtils.createSecondaryButton("Sửa");
        btnEditBook.addActionListener(e -> showEditBookDialog());
        toolbar.add(btnEditBook);

        JButton btnDeleteBook = UIUtils.createSecondaryButton("Xóa");
        btnDeleteBook.addActionListener(e -> handleDeleteBook());
        toolbar.add(btnDeleteBook);

        JButton btnCat = UIUtils.createSecondaryButton("Thể loại");
        btnCat.addActionListener(e -> showManageCategoriesDialog());
        toolbar.add(btnCat);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Mã ISBN", "Tiêu đề sách", "Tác giả", "Thể loại", "Năm XB", "Còn lại / Tổng", "Trạng thái"};
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

    // CARD LẬP PHIẾU MƯỢN ACID
    protected JPanel createBorrowCard() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        wrapper.setBackground(HustTheme.CONTENT_BG);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(660, 510));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));
        formCard.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("LẬP PHIẾU MƯỢN SÁCH — GIAO DỊCH ACID");
        lblTitle.setFont(HustTheme.FONT_HEADER);
        lblTitle.setForeground(HustTheme.PRIMARY_RED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // Độc giả
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        formCard.add(new JLabel("Mã độc giả / MSSV:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        txtBorrowMemberCode = new JTextField();
        txtBorrowMemberCode.putClientProperty("JTextField.placeholderText", "VD: MBR001");
        txtBorrowMemberCode.addActionListener(e -> validateBorrowMember());
        formCard.add(txtBorrowMemberCode, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.20;
        JButton btnCheckMember = UIUtils.createSecondaryButton("Kiểm tra");
        btnCheckMember.addActionListener(e -> validateBorrowMember());
        formCard.add(btnCheckMember, gbc);

        // Thông tin Độc giả
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblBorrowMemberInfo = new JLabel("<html><i>Nhập mã sinh viên / độc giả và nhấn Kiểm tra để xác thực...</i></html>");
        lblBorrowMemberInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        lblBorrowMemberInfo.setBackground(new Color(248, 250, 252));
        lblBorrowMemberInfo.setOpaque(true);
        formCard.add(lblBorrowMemberInfo, gbc);

        // Sách
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        formCard.add(new JLabel("Mã ID / ISBN Sách:"), gbc);

        gbc.gridx = 1;
        txtBorrowBookId = new JTextField();
        txtBorrowBookId.putClientProperty("JTextField.placeholderText", "VD: 1 hoặc 978-604-...");
        txtBorrowBookId.addActionListener(e -> validateBorrowBook());
        formCard.add(txtBorrowBookId, gbc);

        gbc.gridx = 2;
        JButton btnCheckBook = UIUtils.createSecondaryButton("Kiểm tra");
        btnCheckBook.addActionListener(e -> validateBorrowBook());
        formCard.add(btnCheckBook, gbc);

        // Thông tin Sách
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblBorrowBookInfo = new JLabel("<html><i>Nhập mã sách và nhấn Kiểm tra để xác thực số lượng bản sao...</i></html>");
        lblBorrowBookInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        lblBorrowBookInfo.setBackground(new Color(248, 250, 252));
        lblBorrowBookInfo.setOpaque(true);
        formCard.add(lblBorrowBookInfo, gbc);

        // Thời hạn mượn
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        formCard.add(new JLabel("Thời hạn mượn:"), gbc);

        gbc.gridx = 1;
        spnBorrowDays = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        JPanel dayP = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dayP.setOpaque(false);
        dayP.add(spnBorrowDays);
        dayP.add(new JLabel("ngày (Mặc định 14 ngày)"));
        formCard.add(dayP, gbc);

        // Nút Xác nhận
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(18, 8, 8, 8);
        JButton btnSubmit = UIUtils.createPrimaryButton("Xác nhận Lập Phiếu Mượn (ACID)");
        btnSubmit.setFont(HustTheme.FONT_BODY_BOLD.deriveFont(14f));
        btnSubmit.addActionListener(e -> executeBorrowTransaction());
        formCard.add(btnSubmit, gbc);

        wrapper.add(formCard);
        return wrapper;
    }

    // CARD QUẢN LÝ TRẢ SÁCH
    protected JPanel createReturnCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Tìm phiếu:"));
        txtReturnSearch = new JTextField(16);
        txtReturnSearch.putClientProperty("JTextField.placeholderText", "Mã phiếu, độc giả, sách...");
        txtReturnSearch.addActionListener(e -> filterReturnTable());
        toolbar.add(txtReturnSearch);

        JButton btnFilter = UIUtils.createSecondaryButton("Lọc");
        btnFilter.addActionListener(e -> filterReturnTable());
        toolbar.add(btnFilter);

        JButton btnRefresh = UIUtils.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtReturnSearch.setText("");
            loadReturnTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(Box.createHorizontalStrut(10));

        JButton btnReturn = UIUtils.createPrimaryButton("Xác nhận Trả Sách");
        btnReturn.addActionListener(e -> executeReturnBook());
        toolbar.add(btnReturn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Mã phiếu", "Mã độc giả", "Tên độc giả", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Quá hạn"};
        returnTableModel = new DefaultTableModel(cols, 0);
        returnTable = UIUtils.createReadonlyTable(returnTableModel);
        UIUtils.centerAlignColumns(returnTable, 0, 1, 3, 5, 6, 8);
        UIUtils.setStatusPillRenderer(returnTable, 7);

        UIUtils.setColumnWidth(returnTable, 0, 80);
        UIUtils.setColumnWidth(returnTable, 1, 90);
        UIUtils.setColumnWidth(returnTable, 2, 160);
        UIUtils.setColumnWidth(returnTable, 4, 250);

        JScrollPane scroll = new JScrollPane(returnTable);
        scroll.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // CARD QUẢN LÝ TIỀN PHẠT
    protected JPanel createFinesCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Lọc theo trạng thái:"));
        cboFineStatusFilter = new JComboBox<>(new String[]{"-- Tất cả phiếu phạt --", "Chưa thu (UNPAID)", "Đã thu (PAID)"});
        cboFineStatusFilter.addActionListener(e -> loadFinesTable());
        toolbar.add(cboFineStatusFilter);

        JButton btnRefresh = UIUtils.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadFinesTable());
        toolbar.add(btnRefresh);

        toolbar.add(Box.createHorizontalStrut(10));

        JButton btnPayFine = UIUtils.createPrimaryButton("Thu Tiền Phạt");
        btnPayFine.addActionListener(e -> executePayFine());
        toolbar.add(btnPayFine);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Mã phạt", "Mã phiếu mượn", "Mã độc giả", "Họ tên độc giả", "Số ngày trễ", "Số tiền phạt", "Lý do", "Trạng thái", "Ngày nộp phạt"};
        fineTableModel = new DefaultTableModel(cols, 0);
        fineTable = UIUtils.createReadonlyTable(fineTableModel);
        UIUtils.centerAlignColumns(fineTable, 0, 1, 2, 4, 8);
        UIUtils.rightAlignColumns(fineTable, 5);
        UIUtils.setStatusPillRenderer(fineTable, 7);

        JScrollPane scroll = new JScrollPane(fineTable);
        scroll.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ NGHIỆP VỤ & TẢI DỮ LIỆU
    // =========================================================================
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

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Thêm Đầu Sách Mới — HUST Lib", true);
        dialog.setSize(480, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

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
        spnYear.setEditor(new JSpinner.NumberEditor(spnYear, "#"));
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
        JButton btnSave = UIUtils.createPrimaryButton("Lưu sách");
        JButton btnCancel = UIUtils.createSecondaryButton("Hủy bỏ");

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
                loadOverviewMetrics();
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
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

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

        form.add(new JLabel("Mã ISBN:"));
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
        JButton btnSave = UIUtils.createPrimaryButton("Lưu thay đổi");
        JButton btnCancel = UIUtils.createSecondaryButton("Hủy bỏ");

        btnSave.addActionListener(e -> {
            try {
                book.setTitle(txtTitle.getText().trim());
                book.setAuthor(txtAuthor.getText().trim());
                CategoryItem selCat = (CategoryItem) cboCat.getSelectedItem();
                if (selCat != null) {
                    book.setCategoryId(selCat.id);
                }
                book.setPublishYear((Integer) spnYear.getValue());
                book.setCopies((Integer) spnTotal.getValue(), (Integer) spnAvailable.getValue());

                bookService.updateBook(book);
                UIUtils.showSuccess(dialog, "Cập nhật sách thành công!");
                dialog.dispose();
                loadBooksTable();
                loadOverviewMetrics();
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

        if (UIUtils.confirm(this, "Bạn có chắc chắn muốn xóa cuốn sách \"" + title + "\" (ID: " + bookId + ")?", "Xác nhận xóa sách")) {
            try {
                bookService.deleteBook(bookId);
                UIUtils.showSuccess(this, "Đã xóa sách thành công!");
                loadBooksTable();
                loadOverviewMetrics();
            } catch (Exception e) {
                UIUtils.showError(this, "Không thể xóa sách: " + e.getMessage());
            }
        }
    }

    private void showManageCategoriesDialog() {
        JDialog dialog = new JDialog(this, "Danh Mục Thể Loại Sách — HUST Lib", true);
        dialog.setSize(580, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Mã", "Tên thể loại", "Mô tả"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = UIUtils.createReadonlyTable(model);
        UIUtils.centerAlignColumns(table, 0);

        Runnable refreshCat = () -> {
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

        refreshCat.run();
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAdd = UIUtils.createPrimaryButton("+ Thêm thể loại");
        JButton btnClose = UIUtils.createSecondaryButton("Đóng");

        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Tên thể loại mới:", "Thêm thể loại", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                String desc = JOptionPane.showInputDialog(dialog, "Mô tả thể loại:", "Thêm thể loại", JOptionPane.PLAIN_MESSAGE);
                try {
                    categoryService.createCategory(name.trim(), desc != null ? desc.trim() : "");
                    UIUtils.showSuccess(dialog, "Thêm thể loại thành công!");
                    refreshCat.run();
                    initCategoryCache();
                    populateCategoryFilterCombo();
                    loadBooksTable();
                } catch (Exception ex) {
                    UIUtils.showError(dialog, "Lỗi thêm thể loại: " + ex.getMessage());
                }
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnAdd);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void validateBorrowMember() {
        String input = txtBorrowMemberCode.getText().trim();
        if (input.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Mã độc giả / MSSV.");
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
            sb.append("<b>Trạng thái:</b> ").append(active ? "<span style='color:green;'>ACTIVE</span>" : "<span style='color:red;'>LOCKED (Khóa)</span>").append(" | ");
            sb.append("<b>Đang mượn:</b> ").append(m.getCurrentBorrowCount()).append("/").append(m.getMaxBorrowLimit()).append(" cuốn | ");
            sb.append("<b>Nợ phạt:</b> ").append(hasFines ? "<span style='color:red;'>CÓ NỢ PHẠT CHƯA NỘP</span>" : "<span style='color:green;'>Không nợ</span>");

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
                UIUtils.showWarning(this, "Vui lòng xác thực Độc giả trước.");
                return;
            }
        }

        if (currentBorrowBook == null) {
            validateBorrowBook();
            if (currentBorrowBook == null) {
                UIUtils.showWarning(this, "Vui lòng xác thực Đầu sách trước.");
                return;
            }
        }

        int days = (Integer) spnBorrowDays.getValue();

        try {
            BorrowRecord record = borrowService.borrowBook(currentBorrowMember.getUserId(), currentBorrowBook.getBookId(), staff.getUserId(), days);

            UIUtils.showSuccess(this, "Mượn sách thành công!\n" +
                    "- Mã phiếu: #" + record.getRecordId() + "\n" +
                    "- Bạn đọc: " + currentBorrowMember.getFullName() + "\n" +
                    "- Sách: " + currentBorrowBook.getTitle() + "\n" +
                    "- Hạn trả: " + record.getDueDate() + "\n\n" +
                    "(Giao dịch ACID hoàn tất: Đã trừ tồn kho và cập nhật số sách mượn)");

            txtBorrowMemberCode.setText("");
            txtBorrowBookId.setText("");
            lblBorrowMemberInfo.setText("<html><i>Nhập mã sinh viên / độc giả và nhấn Kiểm tra để xác thực...</i></html>");
            lblBorrowBookInfo.setText("<html><i>Nhập mã sách và nhấn Kiểm tra để xác thực số lượng bản sao...</i></html>");
            currentBorrowMember = null;
            currentBorrowBook = null;

            loadBooksTable();
            loadReturnTable();
            loadOverviewMetrics();
        } catch (Exception ex) {
            UIUtils.showError(this, "Không thể mượn sách: " + ex.getMessage());
        }
    }

    public void loadReturnTable() {
        try {
            List<BorrowRecord> list = borrowService.getAllBorrowRecords();
            List<BorrowRecord> active = list.stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                    .toList();
            renderReturnTable(active);
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách phiếu mượn: " + e.getMessage());
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
            UIUtils.showWarning(this, "Vui lòng chọn một phiếu mượn từ bảng.");
            return;
        }

        int recordId = (Integer) returnTableModel.getValueAt(row, 0);
        String bookTitle = (String) returnTableModel.getValueAt(row, 4);

        if (!UIUtils.confirm(this, "Xác nhận nhận lại sách \"" + bookTitle + "\" (Phiếu #" + recordId + ")?", "Xác nhận trả sách")) {
            return;
        }

        try {
            borrowService.returnBook(recordId);

            try {
                Fine fine = fineService.getFineByRecordId(recordId);
                if (fine != null && fine.getStatus() == FineStatus.UNPAID) {
                    UIUtils.showWarning(this, "Đã nhận lại sách thành công!\n\n" +
                            "CẢNH BÁO QUÁ HẠN:\n" +
                            "- Trả trễ: " + fine.getOverdueDays() + " ngày\n" +
                            "- Tiền phạt: " + UIUtils.formatCurrency(fine.getFineAmount()) + "\n" +
                            "Vui lòng chuyển qua mục 'Vé Phạt & Thu Phí' để thu tiền bạn đọc.");
                } else {
                    UIUtils.showSuccess(this, "Trả sách đúng hạn thành công! Tồn kho sách đã được hoàn lại.");
                }
            } catch (Exception ignored) {
                UIUtils.showSuccess(this, "Trả sách thành công! Tồn kho sách đã được hoàn lại.");
            }

            loadReturnTable();
            loadBooksTable();
            loadFinesTable();
            loadOverviewMetrics();
        } catch (Exception ex) {
            UIUtils.showError(this, "Không thể trả sách: " + ex.getMessage());
        }
    }

    public void loadFinesTable() {
        try {
            int filterIdx = cboFineStatusFilter != null ? cboFineStatusFilter.getSelectedIndex() : 0;
            List<Fine> list = fineService.getAllFines();

            if (filterIdx == 1) {
                list = list.stream().filter(f -> f.getStatus() == FineStatus.UNPAID).toList();
            } else if (filterIdx == 2) {
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
            UIUtils.showWarning(this, "Vui lòng chọn một phiếu phạt cần thu tiền.");
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

        if (!UIUtils.confirm(this, "Xác nhận thu số tiền " + amount + " từ độc giả \"" + memberName + "\" (Phiếu #" + fineId + ")?", "Thu tiền phạt")) {
            return;
        }

        try {
            fineService.payFine(fineId);
            UIUtils.showSuccess(this, "Thu tiền phạt thành công!");
            loadFinesTable();
            loadOverviewMetrics();
        } catch (Exception ex) {
            UIUtils.showError(this, "Lỗi thanh toán: " + ex.getMessage());
        }
    }

    public void loadOverviewMetrics() {
        if (overviewCardsPanel == null) return;

        try {
            List<Book> books = bookService.getAllBooks();
            int totalTitles = books.size();
            int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
            int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
            int borrowedCopies = totalCopies - availableCopies;

            List<Fine> unpaidFines = fineService.getUnpaidFines();
            BigDecimal totalUnpaid = unpaidFines.stream()
                    .map(Fine::getFineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            overviewCardsPanel.removeAll();
            overviewCardsPanel.add(UIUtils.createStatCard("Tổng số đầu sách", totalTitles + " đầu sách", HustTheme.PRIMARY_RED, "📕"));
            overviewCardsPanel.add(UIUtils.createStatCard("Tổng số bản in", totalCopies + " cuốn", HustTheme.ACCENT_GOLD, "📚"));
            overviewCardsPanel.add(UIUtils.createStatCard("Số sách đang mượn", borrowedCopies + " cuốn", new Color(217, 119, 6), "📖"));
            overviewCardsPanel.add(UIUtils.createStatCard("Tiền phạt chưa thu", UIUtils.formatCurrency(totalUnpaid), new Color(220, 38, 38), "💰"));

            overviewCardsPanel.revalidate();
            overviewCardsPanel.repaint();
        } catch (Exception ignored) {
        }
    }

    public void loadAllInitialData() {
        loadBooksTable();
        loadReturnTable();
        loadFinesTable();
        loadOverviewMetrics();
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
