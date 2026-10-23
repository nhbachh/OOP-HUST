package com.library.view;

import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Trung tâm Quản trị Hệ thống — Phong cách Bách Khoa Hiện Đại (HUST Lib Admin Dashboard).
 * Kế thừa LibrarianDashboardFrame (Tính kế thừa OOP), tích hợp toàn bộ nghiệp vụ Thủ thư,
 * đồng thời bổ sung thêm 2 Card đặc quyền: Quản trị Người Dùng & Thẻ và Báo Cáo Thống Kê Chi Tiết.
 */
public class AdminDashboardFrame extends LibrarianDashboardFrame {

    public static final String CARD_USERS = "USERS";
    public static final String CARD_STATS = "STATS";

    // Components Card Quản lý Người dùng
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JComboBox<String> cboUserRoleFilter;
    private JComboBox<String> cboUserStatusFilter;
    private JTextField txtUserSearch;

    // Components Card Thống kê Báo cáo
    private JPanel statsCardsContainer;
    private DefaultTableModel overdueTableModel;
    private JTable overdueTable;

    public AdminDashboardFrame(Staff staff,
                               BookService bookService,
                               CategoryService categoryService,
                               BorrowService borrowService,
                               FineService fineService,
                               UserService userService) {
        super(staff, bookService, categoryService, borrowService, fineService, userService);
        loadUsersTable();
        loadStatisticsData();
    }

    @Override
    protected String getFrameTitle() {
        return "HUST Lib — Trung tâm Quản trị Hệ thống (" + staff.getUserCode() + " - " + staff.getFullName() + ")";
    }

    @Override
    protected String getRoleBadgeText() {
        return "QUẢN TRỊ VIÊN • " + staff.getUserCode();
    }

    @Override
    protected void populateSidebarMenuItems() {
        // Thêm 5 mục cơ bản của thủ thư
        super.populateSidebarMenuItems();

        // Thêm 2 mục đặc quyền của Quản trị viên
        addSidebarItem("👥  Quản lý Người Dùng", CARD_USERS, "Trang chủ / Quản lý Người Dùng & Thẻ");
        addSidebarItem("📈  Báo Cáo Thống Kê", CARD_STATS, "Trang chủ / Báo Cáo Thống Kê Hệ Thống");
    }

    @Override
    protected void initCards(JPanel cardPanel) {
        super.initCards(cardPanel);

        cardPanel.add(createUserManagementCard(), CARD_USERS);
        cardPanel.add(createStatisticsCard(), CARD_STATS);
    }

    // =========================================================================
    // CARD: QUẢN LÝ NGƯỜI DÙNG & TÀI KHOẢN
    // =========================================================================
    private JPanel createUserManagementCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Tìm kiếm:"));
        txtUserSearch = new JTextField(12);
        txtUserSearch.putClientProperty("JTextField.placeholderText", "Họ tên, mã, email...");
        txtUserSearch.addActionListener(e -> loadUsersTable());
        toolbar.add(txtUserSearch);

        toolbar.add(new JLabel("Vai trò:"));
        cboUserRoleFilter = new JComboBox<>(new String[]{"-- Tất cả --", "ADMIN", "LIBRARIAN", "MEMBER"});
        cboUserRoleFilter.addActionListener(e -> loadUsersTable());
        toolbar.add(cboUserRoleFilter);

        toolbar.add(new JLabel("Trạng thái:"));
        cboUserStatusFilter = new JComboBox<>(new String[]{"-- Tất cả --", "ACTIVE (Hoạt động)", "LOCKED (Bị khóa)"});
        cboUserStatusFilter.addActionListener(e -> loadUsersTable());
        toolbar.add(cboUserStatusFilter);

        JButton btnFilter = UIUtils.createSecondaryButton("Lọc");
        btnFilter.addActionListener(e -> loadUsersTable());
        toolbar.add(btnFilter);

        JButton btnRefresh = UIUtils.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtUserSearch.setText("");
            cboUserRoleFilter.setSelectedIndex(0);
            cboUserStatusFilter.setSelectedIndex(0);
            loadUsersTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(Box.createHorizontalStrut(10));

        JButton btnAddStaff = UIUtils.createPrimaryButton("+ Thêm Nhân Viên");
        btnAddStaff.addActionListener(e -> showAddStaffDialog());
        toolbar.add(btnAddStaff);

        JButton btnAddMember = UIUtils.createSecondaryButton("+ Thêm Độc Giả");
        btnAddMember.addActionListener(e -> showAddMemberDialog());
        toolbar.add(btnAddMember);

        JButton btnToggle = UIUtils.createSecondaryButton("Khóa / Mở Khóa");
        btnToggle.addActionListener(e -> handleToggleUserStatus());
        toolbar.add(btnToggle);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Mã người dùng", "Tài khoản", "Họ và tên", "Email", "SĐT", "Vai trò", "Trạng thái", "Ghi chú"};
        userTableModel = new DefaultTableModel(cols, 0);
        userTable = UIUtils.createReadonlyTable(userTableModel);
        UIUtils.centerAlignColumns(userTable, 0, 1, 6);
        UIUtils.setStatusPillRenderer(userTable, 7);

        UIUtils.setColumnWidth(userTable, 0, 50);
        UIUtils.setColumnWidth(userTable, 1, 100);
        UIUtils.setColumnWidth(userTable, 2, 110);
        UIUtils.setColumnWidth(userTable, 3, 170);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void loadUsersTable() {
        try {
            List<User> users = userService.getAllUsers();
            String kw = txtUserSearch != null ? txtUserSearch.getText().trim().toLowerCase() : "";
            int roleFilterIdx = cboUserRoleFilter != null ? cboUserRoleFilter.getSelectedIndex() : 0;
            int statusFilterIdx = cboUserStatusFilter != null ? cboUserStatusFilter.getSelectedIndex() : 0;

            List<User> filtered = users.stream().filter(u -> {
                if (roleFilterIdx == 1 && u.getRole() != Role.ADMIN) return false;
                if (roleFilterIdx == 2 && u.getRole() != Role.LIBRARIAN) return false;
                if (roleFilterIdx == 3 && u.getRole() != Role.MEMBER) return false;

                if (statusFilterIdx == 1 && u.getStatus() != UserStatus.ACTIVE) return false;
                if (statusFilterIdx == 2 && u.getStatus() != UserStatus.LOCKED) return false;

                if (!kw.isEmpty()) {
                    boolean matchName = u.getFullName() != null && u.getFullName().toLowerCase().contains(kw);
                    boolean matchCode = u.getUserCode() != null && u.getUserCode().toLowerCase().contains(kw);
                    boolean matchUser = u.getUsername() != null && u.getUsername().toLowerCase().contains(kw);
                    boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(kw);
                    if (!matchName && !matchCode && !matchUser && !matchEmail) return false;
                }
                return true;
            }).toList();

            userTableModel.setRowCount(0);
            for (User u : filtered) {
                String extra = "";
                if (u instanceof Staff s) {
                    extra = "Phòng ban: " + (s.getDepartment() != null ? s.getDepartment() : "N/A");
                } else if (u instanceof Member m) {
                    extra = "Đang mượn: " + m.getCurrentBorrowCount() + "/" + m.getMaxBorrowLimit() + " cuốn";
                }

                userTableModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getUserCode(),
                        u.getUsername() != null ? u.getUsername() : "(Không có)",
                        u.getFullName(),
                        u.getEmail() != null ? u.getEmail() : "",
                        u.getPhone() != null ? u.getPhone() : "",
                        u.getRole(),
                        u.getStatus(),
                        extra
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách người dùng: " + e.getMessage());
        }
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog(this, "Thêm Mới Nhân Viên / Quản Trị Viên — HUST Lib", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        JTextField txtCode = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<Role> cboRole = new JComboBox<>(new Role[]{Role.LIBRARIAN, Role.ADMIN});
        JTextField txtDept = new JTextField();

        form.add(new JLabel("Mã nhân viên (VD: LIB003) (*):"));
        form.add(txtCode);
        form.add(new JLabel("Tên đăng nhập (*):"));
        form.add(txtUsername);
        form.add(new JLabel("Mật khẩu (*):"));
        form.add(txtPass);
        form.add(new JLabel("Họ và tên (*):"));
        form.add(txtName);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtPhone);
        form.add(new JLabel("Vai trò (*):"));
        form.add(cboRole);
        form.add(new JLabel("Phòng ban / Bộ phận:"));
        form.add(txtDept);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnSave = UIUtils.createPrimaryButton("Lưu nhân viên");
        JButton btnCancel = UIUtils.createSecondaryButton("Hủy bỏ");

        btnSave.addActionListener(e -> {
            try {
                String code = txtCode.getText().trim();
                String username = txtUsername.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                Role role = (Role) cboRole.getSelectedItem();
                String dept = txtDept.getText().trim();

                userService.createStaff(code, username, pass, name, email, phone, role, dept);
                UIUtils.showSuccess(dialog, "Thêm nhân viên mới thành công!");
                dialog.dispose();
                loadUsersTable();
                loadStatisticsData();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Lỗi thêm nhân viên: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Thêm Mới Độc Giả Bạn Đọc — HUST Lib", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(25, 25, 10, 25));

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();

        form.add(new JLabel("Mã độc giả / MSSV (*):"));
        form.add(txtCode);
        form.add(new JLabel("Họ và tên (*):"));
        form.add(txtName);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtPhone);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnSave = UIUtils.createPrimaryButton("Lưu độc giả");
        JButton btnCancel = UIUtils.createSecondaryButton("Hủy bỏ");

        btnSave.addActionListener(e -> {
            try {
                String code = txtCode.getText().trim();
                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();

                userService.createMember(code, name, email, phone);
                UIUtils.showSuccess(dialog, "Thêm độc giả mới thành công!");
                dialog.dispose();
                loadUsersTable();
                loadStatisticsData();
            } catch (Exception ex) {
                UIUtils.showError(dialog, "Lỗi thêm độc giả: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleToggleUserStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showWarning(this, "Vui lòng chọn một người dùng từ bảng.");
            return;
        }

        int userId = (Integer) userTableModel.getValueAt(row, 0);
        String code = (String) userTableModel.getValueAt(row, 1);
        String name = (String) userTableModel.getValueAt(row, 3);
        UserStatus status = (UserStatus) userTableModel.getValueAt(row, 7);

        if (userId == staff.getUserId()) {
            UIUtils.showWarning(this, "Bạn không thể khóa tài khoản của chính mình!");
            return;
        }

        String nextAction = (status == UserStatus.ACTIVE) ? "KHÓA" : "MỞ KHÓA";
        if (UIUtils.confirm(this, "Bạn có chắc chắn muốn " + nextAction + " tài khoản " + code + " (" + name + ")?", "Xác nhận đổi trạng thái")) {
            try {
                userService.toggleUserStatus(userId);
                UIUtils.showSuccess(this, "Đã cập nhật trạng thái tài khoản thành công!");
                loadUsersTable();
            } catch (Exception ex) {
                UIUtils.showError(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // CARD: BÁO CÁO THỐNG KÊ CHI TIẾT
    // =========================================================================
    private JPanel createStatisticsCard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(HustTheme.CONTENT_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("BÁO CÁO THỐNG KÊ TOÀN DIỆN THƯ VIỆN HUST");
        title.setFont(HustTheme.FONT_HEADER);
        title.setForeground(HustTheme.PRIMARY_RED);

        JButton btnRefreshStats = UIUtils.createSecondaryButton("Làm mới số liệu");
        btnRefreshStats.addActionListener(e -> loadStatisticsData());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnRefreshStats, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center Content: Metric Cards + Overdue Table
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);

        statsCardsContainer = new JPanel(new GridLayout(2, 3, 14, 14));
        statsCardsContainer.setOpaque(false);
        center.add(statsCardsContainer, BorderLayout.NORTH);

        // Overdue Table
        JPanel overduePanel = new JPanel(new BorderLayout(6, 6));
        overduePanel.setOpaque(false);

        JLabel lblOverdue = new JLabel("Danh sách các sách đang bị mượn quá hạn (Cần liên hệ thu hồi & phạt):");
        lblOverdue.setFont(HustTheme.FONT_BODY_BOLD);
        lblOverdue.setForeground(HustTheme.PRIMARY_RED);
        overduePanel.add(lblOverdue, BorderLayout.NORTH);

        String[] overdueCols = {"Mã phiếu", "Mã độc giả", "Họ tên bạn đọc", "Mã sách", "Tên sách", "Hạn trả", "Số ngày trễ", "Tiền phạt dự kiến"};
        overdueTableModel = new DefaultTableModel(overdueCols, 0);
        overdueTable = UIUtils.createReadonlyTable(overdueTableModel);
        UIUtils.centerAlignColumns(overdueTable, 0, 1, 3, 5, 6);
        UIUtils.rightAlignColumns(overdueTable, 7);

        JScrollPane scroll = new JScrollPane(overdueTable);
        scroll.setBorder(BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1));
        overduePanel.add(scroll, BorderLayout.CENTER);

        center.add(overduePanel, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    public void loadStatisticsData() {
        if (statsCardsContainer == null) return;

        try {
            List<Book> books = bookService.getAllBooks();
            int totalTitles = books.size();
            int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
            int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
            int borrowedCopies = totalCopies - availableCopies;

            List<Member> members = userService.getAllMembers();
            int totalMembers = members.size();

            List<BorrowRecord> allBorrows = borrowService.getAllBorrowRecords();
            long activeBorrows = allBorrows.stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                    .count();

            List<Fine> unpaidFines = fineService.getUnpaidFines();
            BigDecimal totalUnpaidAmount = unpaidFines.stream()
                    .map(Fine::getFineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            statsCardsContainer.removeAll();
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số đầu sách", totalTitles + " đầu sách", HustTheme.PRIMARY_RED, "📕"));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số bản in lưu hành", totalCopies + " cuốn", HustTheme.ACCENT_GOLD, "📚"));
            statsCardsContainer.add(UIUtils.createStatCard("Số sách đang mượn", borrowedCopies + " cuốn", new Color(217, 119, 6), "📖"));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số độc giả HUST", totalMembers + " bạn đọc", new Color(111, 66, 193), "👥"));
            statsCardsContainer.add(UIUtils.createStatCard("Phiếu mượn đang hiệu lực", activeBorrows + " phiếu", new Color(2, 132, 199), "📝"));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng tiền phạt chưa thu", UIUtils.formatCurrency(totalUnpaidAmount), new Color(220, 38, 38), "💰"));

            statsCardsContainer.revalidate();
            statsCardsContainer.repaint();

            // Load Overdue Records
            List<BorrowRecord> overdueRecords = borrowService.getOverdueBorrowRecords();
            overdueTableModel.setRowCount(0);
            LocalDate today = LocalDate.now();

            for (BorrowRecord r : overdueRecords) {
                String memberName = "Độc giả #" + r.getUserId();
                String memberCode = "";
                try {
                    User u = userService.getUserById(r.getUserId());
                    if (u != null) {
                        memberName = u.getFullName();
                        memberCode = u.getUserCode();
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
                BigDecimal estFine = FineService.DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));

                overdueTableModel.addRow(new Object[]{
                        "#" + r.getRecordId(),
                        memberCode,
                        memberName,
                        r.getBookId(),
                        bookTitle,
                        r.getDueDate(),
                        overdueDays + " ngày",
                        UIUtils.formatCurrency(estFine)
                });
            }

        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải số liệu thống kê: " + e.getMessage());
        }
    }
}
