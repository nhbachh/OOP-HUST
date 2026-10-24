package com.library.view;

import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cổng thông tin Bạn đọc Bách Khoa — HUST Lib Modern Member Dashboard.
 * Thiết kế đồng bộ cấu trúc Sidebar Navigation bên trái (Dark Burgundy #18181B)
 * tương tự như bàn làm việc Quản trị viên và Thủ thư.
 * Nội dung chia thành các phần độc lập qua CardLayout:
 * 1. Khám Phá Sách (Kèm 3 thẻ KPI Hạn mức cá nhân)
 * 2. Sách Đang Mượn
 * 3. Lịch Sử Mượn Trả
 * 4. Vé Phạt Của Tôi
 * 5. Hồ Sơ Bạn Đọc
 * Tuân thủ Loose Coupling: Tương tác 100% qua tầng Service.
 */
public class MemberDashboardFrame extends JFrame {

    private final Member member;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

    // CardLayout
    private CardLayout cardLayout;
    private JPanel contentCardPanel;

    // Sidebar items
    private final List<JButton> sidebarButtons = new ArrayList<>();
    private JPanel sidebarMenuContainer;
    private JLabel lblBreadcrumb;

    private final Map<Integer, String> categoryMap = new HashMap<>();

    // Tên các Card
    public static final String CARD_DISCOVER = "DISCOVER";
    public static final String CARD_CURRENT_BORROW = "CURRENT_BORROW";
    public static final String CARD_HISTORY = "HISTORY";
    public static final String CARD_FINES = "FINES";
    public static final String CARD_PROFILE = "PROFILE";

    // Components Tab Khám Phá Sách
    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private JComboBox<CategoryItem> cboCategories;
    private JTextField txtSearchKeyword;
    private JPanel quotaCardsPanel;

    // Components Tab Sách Đang Mượn
    private DefaultTableModel currentBorrowTableModel;
    private JTable currentBorrowTable;

    // Components Tab Lịch Sử
    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    // Components Tab Tiền Phạt
    private DefaultTableModel fineTableModel;
    private JTable fineTable;
    private JLabel lblFineSummary;

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
        loadAllData();
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
        setTitle("HUST Lib — Cổng Thông Tin Bạn Đọc (" + member.getUserCode() + " - " + member.getFullName() + ")");
        setSize(1140, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. THANH SIDEBAR BÊN TRÁI (DARK BURGUNDY)
        JPanel sidebar = createSidebarPanel();
        add(sidebar, BorderLayout.WEST);

        // 2. VÙNG NỘI DUNG CHÍNH (TOP BAR + CONTENT CARDS)
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(HustTheme.CONTENT_BG);

        // Top bar
        JPanel topBar = createTopBar();
        mainArea.add(topBar, BorderLayout.NORTH);

        // CardLayout Panel
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(HustTheme.CONTENT_BG);

        initCards(contentCardPanel);

        mainArea.add(contentCardPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // Mặc định chọn mục đầu tiên (Khám phá sách)
        if (!sidebarButtons.isEmpty()) {
            switchCard(CARD_DISCOVER, "Trang chủ / Khám Phá & Tra Cứu Sách", sidebarButtons.get(0));
        }
    }

    // =========================================================================
    // 1. THANH ĐIỀU HƯỚNG BÊN TRÁI (SIDEBAR)
    // =========================================================================
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(HustTheme.SIDEBAR_BG);

        // Logo HUST Lib
        JPanel brandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(HustTheme.PRIMARY_RED);
                g2.fillRect(0, 0, 4, getHeight());

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

        // Profile chip độc giả sinh viên
        JPanel profileChip = new JPanel(new BorderLayout(10, 0));
        profileChip.setBackground(new Color(39, 39, 42));
        profileChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 14, 15, 14),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                )
        ));

        JLabel lblAvatar = new JLabel("🎓");
        lblAvatar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        lblAvatar.setForeground(Color.WHITE);

        JPanel profileText = new JPanel(new GridLayout(2, 1, 2, 2));
        profileText.setOpaque(false);

        JLabel lblName = new JLabel(member.getFullName());
        lblName.setFont(HustTheme.FONT_BODY_BOLD);
        lblName.setForeground(Color.WHITE);

        JLabel lblRoleBadge = new JLabel("BẠN ĐỌC • " + member.getUserCode());
        lblRoleBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        lblRoleBadge.setForeground(new Color(167, 243, 208)); // Emerald green tint

        profileText.add(lblName);
        profileText.add(lblRoleBadge);

        profileChip.add(lblAvatar, BorderLayout.WEST);
        profileChip.add(profileText, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(brandPanel, BorderLayout.NORTH);
        topContainer.add(profileChip, BorderLayout.SOUTH);

        sidebar.add(topContainer, BorderLayout.NORTH);

        // Menu items
        sidebarMenuContainer = new JPanel();
        sidebarMenuContainer.setLayout(new BoxLayout(sidebarMenuContainer, BoxLayout.Y_AXIS));
        sidebarMenuContainer.setOpaque(false);
        sidebarMenuContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        addSidebarItem("📖  Khám Phá Sách", CARD_DISCOVER, "Trang chủ / Khám Phá & Tra Cứu Sách");
        addSidebarItem("📚  Sách Đang Mượn", CARD_CURRENT_BORROW, "Trang chủ / Sách Đang Mượn Của Tôi");
        addSidebarItem("📜  Lịch Sử Mượn Trả", CARD_HISTORY, "Trang chủ / Lịch Sử Mượn Trả Sách");
        addSidebarItem("💰  Vé Phạt Của Tôi", CARD_FINES, "Trang chủ / Vé Phạt & Tiền Nợ Phạt");
        addSidebarItem("👤  Hồ Sơ Cá Nhân", CARD_PROFILE, "Trang chủ / Hồ Sơ Thẻ Bạn Đọc HUST");

        sidebar.add(sidebarMenuContainer, BorderLayout.CENTER);

        // Nút Đăng xuất
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

    private void addSidebarItem(String title, String cardName, String breadcrumbText) {
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

    private void switchCard(String cardName, String breadcrumbText, JButton activeBtn) {
        cardLayout.show(contentCardPanel, cardName);
        if (lblBreadcrumb != null) {
            lblBreadcrumb.setText(breadcrumbText);
        }

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
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HustTheme.BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)
        ));

        lblBreadcrumb = new JLabel("Trang chủ / Khám Phá & Tra Cứu Sách");
        lblBreadcrumb.setFont(HustTheme.FONT_BODY_BOLD);
        lblBreadcrumb.setForeground(HustTheme.TEXT_PRIMARY);
        topBar.add(lblBreadcrumb, BorderLayout.WEST);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightBar.setOpaque(false);

        // Badge trạng thái thẻ
        JLabel lblStatus = new JLabel("Thẻ: " + member.getStatus());
        lblStatus.setFont(HustTheme.FONT_SMALL.deriveFont(Font.BOLD));
        lblStatus.setForeground(member.getStatus() == UserStatus.ACTIVE ? new Color(5, 150, 105) : new Color(220, 38, 38));
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(member.getStatus() == UserStatus.ACTIVE ? new Color(167, 243, 208) : new Color(254, 202, 202), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        // Ngày hiện tại
        LocalDate now = LocalDate.now();
        String dateStr = "📅 " + now.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new java.util.Locale("vi", "VN")));
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(HustTheme.FONT_SMALL);
        lblDate.setForeground(HustTheme.TEXT_MUTED);

        rightBar.add(lblStatus);
        rightBar.add(lblDate);
        topBar.add(rightBar, BorderLayout.EAST);

        return topBar;
    }

    // =========================================================================
    // 3. KHỞI TẠO CÁC CARD TRONG NỘI DUNG CHÍNH
    // =========================================================================
    private void initCards(JPanel cardPanel) {
        cardPanel.add(createDiscoverCard(), CARD_DISCOVER);
        cardPanel.add(createCurrentBorrowCard(), CARD_CURRENT_BORROW);
        cardPanel.add(createHistoryCard(), CARD_HISTORY);
        cardPanel.add(createFinesCard(), CARD_FINES);
        cardPanel.add(createProfileCard(), CARD_PROFILE);
    }

    // CARD 1: KHÁM PHÁ & TRA CỨU SÁCH (KÈM 3 THẺ KPI CÁ NHÂN)
    private JPanel createDiscoverCard() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Top: 3 Thẻ chỉ số cá nhân (Quota KPI Cards)
        quotaCardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        quotaCardsPanel.setOpaque(false);
        quotaCardsPanel.setPreferredSize(new Dimension(0, 85));
        updateQuotaCards();
        panel.add(quotaCardsPanel, BorderLayout.NORTH);

        // Center: Bảng danh sách sách & thanh tìm kiếm
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Tìm kiếm sách:"));

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

        centerPanel.add(filterBar, BorderLayout.NORTH);

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
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateQuotaCards() {
        if (quotaCardsPanel == null) return;

        quotaCardsPanel.removeAll();

        int borrowed = member.getCurrentBorrowCount();
        int max = member.getMaxBorrowLimit();
        int remaining = Math.max(0, max - borrowed);

        boolean hasFines = false;
        BigDecimal unpaidTotal = BigDecimal.ZERO;
        try {
            List<Fine> fines = fineService.getFinesByUserId(member.getUserId());
            for (Fine f : fines) {
                if (f.getStatus() == FineStatus.UNPAID) {
                    hasFines = true;
                    unpaidTotal = unpaidTotal.add(f.getFineAmount());
                }
            }
        } catch (Exception ignored) {
        }

        quotaCardsPanel.add(UIUtils.createStatCard("Sách đang mượn", borrowed + " / " + max + " cuốn", new Color(217, 119, 6), "📖"));
        quotaCardsPanel.add(UIUtils.createStatCard("Hạn mức còn lại", remaining + " cuốn", new Color(5, 150, 105), "📗"));
        quotaCardsPanel.add(UIUtils.createStatCard("Tiền nợ phạt", hasFines ? UIUtils.formatCurrency(unpaidTotal) : "0 VNĐ (Không nợ)",
                hasFines ? new Color(220, 38, 38) : new Color(5, 150, 105), "💰"));

        quotaCardsPanel.revalidate();
        quotaCardsPanel.repaint();
    }

    // CARD 2: SÁCH ĐANG MƯỢN CỦA TÔI
    private JPanel createCurrentBorrowCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        JPanel header = UIUtils.createHeaderPanel("DANH SÁCH SÁCH HIỆN ĐANG MƯỢN",
                "Theo dõi thời hạn trả để tránh phát sinh phí phạt quá hạn (2.000 VNĐ / ngày trễ)");
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Mã phiếu", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Hạn trả thực tế"};
        currentBorrowTableModel = new DefaultTableModel(cols, 0);
        currentBorrowTable = UIUtils.createReadonlyTable(currentBorrowTableModel);
        UIUtils.centerAlignColumns(currentBorrowTable, 0, 1, 3, 4, 6);
        UIUtils.setStatusPillRenderer(currentBorrowTable, 5);

        UIUtils.setColumnWidth(currentBorrowTable, 0, 80);
        UIUtils.setColumnWidth(currentBorrowTable, 1, 70);
        UIUtils.setColumnWidth(currentBorrowTable, 2, 280);

        JScrollPane sp = new JScrollPane(currentBorrowTable);
        sp.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    // CARD 3: LỊCH SỬ MƯỢN TRẢ
    private JPanel createHistoryCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        JPanel header = UIUtils.createHeaderPanel("TOÀN BỘ LỊCH SỬ MƯỢN VÀ TRẢ SÁCH",
                "Nhật ký các lần mượn và hoàn trả tài liệu tại thư viện Bách Khoa");
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Mã phiếu", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Ngày trả thực tế", "Trạng thái"};
        historyTableModel = new DefaultTableModel(cols, 0);
        historyTable = UIUtils.createReadonlyTable(historyTableModel);
        UIUtils.centerAlignColumns(historyTable, 0, 1, 3, 4, 5);
        UIUtils.setStatusPillRenderer(historyTable, 6);

        UIUtils.setColumnWidth(historyTable, 0, 80);
        UIUtils.setColumnWidth(historyTable, 1, 70);
        UIUtils.setColumnWidth(historyTable, 2, 280);

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    // CARD 4: VÉ PHẠT CỦA TÔI
    private JPanel createFinesCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        JPanel topP = new JPanel(new BorderLayout());
        topP.setOpaque(false);
        JPanel header = UIUtils.createHeaderPanel("DANH SÁCH VÉ PHẠT QUÁ HẠN CỦA BẠN",
                "Độc giả đang có vé phạt chưa thanh toán sẽ tạm thời bị đình chỉ quyền mượn sách");
        lblFineSummary = new JLabel("");
        lblFineSummary.setFont(HustTheme.FONT_BODY_BOLD);

        topP.add(header, BorderLayout.WEST);
        topP.add(lblFineSummary, BorderLayout.EAST);
        panel.add(topP, BorderLayout.NORTH);

        String[] cols = {"Mã phạt", "Mã phiếu mượn", "Số ngày trễ", "Số tiền phạt", "Lý do phạt", "Trạng thái", "Ngày đóng phạt"};
        fineTableModel = new DefaultTableModel(cols, 0);
        fineTable = UIUtils.createReadonlyTable(fineTableModel);
        UIUtils.centerAlignColumns(fineTable, 0, 1, 2, 6);
        UIUtils.rightAlignColumns(fineTable, 3);
        UIUtils.setStatusPillRenderer(fineTable, 5);

        JScrollPane sp = new JScrollPane(fineTable);
        sp.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    // CARD 5: HỒ SƠ BẠN ĐỌC HUST
    private JPanel createProfileCard() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        wrapper.setBackground(HustTheme.CONTENT_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(560, 380));
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

    // =========================================================================
    // TẢI DỮ LIỆU
    // =========================================================================
    public void loadAllData() {
        loadBookData();
        loadBorrowData();
        loadFineData();
        updateQuotaCards();
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

            // 1. Load History table
            historyTableModel.setRowCount(0);
            currentBorrowTableModel.setRowCount(0);
            LocalDate today = LocalDate.now();

            for (BorrowRecord r : records) {
                String bookTitle = "Mã sách #" + r.getBookId();
                try {
                    Book b = bookService.getBookById(r.getBookId());
                    if (b != null) {
                        bookTitle = b.getTitle();
                    }
                } catch (Exception ignored) {
                }

                // Add to history
                historyTableModel.addRow(new Object[]{
                        "#" + r.getRecordId(),
                        r.getBookId(),
                        bookTitle,
                        r.getBorrowDate(),
                        r.getDueDate(),
                        r.getReturnDate() != null ? r.getReturnDate() : "Chưa trả",
                        r.getStatus()
                });

                // Add to current borrows if active
                if (r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE) {
                    long overdueDays = 0;
                    String dueStatus;
                    if (today.isAfter(r.getDueDate())) {
                        overdueDays = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), today);
                        dueStatus = "Quá hạn " + overdueDays + " ngày!";
                    } else {
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, r.getDueDate());
                        dueStatus = "Còn " + daysLeft + " ngày";
                    }

                    currentBorrowTableModel.addRow(new Object[]{
                            "#" + r.getRecordId(),
                            r.getBookId(),
                            bookTitle,
                            r.getBorrowDate(),
                            r.getDueDate(),
                            r.getStatus(),
                            dueStatus
                    });
                }
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải lịch sử mượn sách: " + e.getMessage());
        }
    }

    private void loadFineData() {
        try {
            List<Fine> fines = fineService.getFinesByUserId(member.getUserId());
            fineTableModel.setRowCount(0);
            BigDecimal unpaidTotal = BigDecimal.ZERO;

            for (Fine f : fines) {
                if (f.getStatus() == FineStatus.UNPAID) {
                    unpaidTotal = unpaidTotal.add(f.getFineAmount());
                }

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

            if (lblFineSummary != null) {
                if (unpaidTotal.compareTo(BigDecimal.ZERO) > 0) {
                    lblFineSummary.setText("Tổng nợ phạt cần thanh toán: " + UIUtils.formatCurrency(unpaidTotal) + "  ");
                    lblFineSummary.setForeground(new Color(220, 38, 38));
                } else {
                    lblFineSummary.setText("Tình trạng: Bạn không có khoản nợ phạt nào  ");
                    lblFineSummary.setForeground(new Color(5, 150, 105));
                }
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
