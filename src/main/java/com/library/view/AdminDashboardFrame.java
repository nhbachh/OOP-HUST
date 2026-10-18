package com.library.view;

import com.library.model.*;
import com.library.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cửa sổ làm việc cho Quản trị viên (Admin Dashboard).
 * Kế thừa LibrarianDashboardFrame (Inheritance - Kế thừa trong OOP) để tái sử dụng toàn bộ chức năng của Thủ thư,
 * đồng thời bổ sung thêm các đặc quyền quản trị: Quản lý Người dùng và Thống kê & Báo cáo tổng thể.
 * Tuân thủ Loose Coupling: Chỉ thao tác thông qua Service layer.
 */
public class AdminDashboardFrame extends LibrarianDashboardFrame {

    // Tab User Management components
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JComboBox<String> cboUserRoleFilter;
    private JComboBox<String> cboUserStatusFilter;
    private JTextField txtUserSearch;

    // Tab Statistics components
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
        return "Hệ thống Quản lý Thư viện — Bàn làm việc Quản trị viên (" + staff.getUserCode() + " - " + staff.getFullName() + ")";
    }

    @Override
    protected void initLibrarianTabs(JTabbedPane tabbed) {
        // Gọi lại 4 tab nghiệp vụ thủ thư
        super.initLibrarianTabs(tabbed);

        // Bổ sung 2 tab đặc quyền quản trị
        tabbed.addTab("Quản lý Người dùng (Users)", createUserManagementTab());
        tabbed.addTab("Thống kê & Báo cáo Hệ thống", createStatisticsTab());
    }

    // =========================================================================
    // TAB 5: QUẢN LÝ NGƯỜI DÙNG (USERS MANAGEMENT)
    // =========================================================================
    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Tìm kiếm:"));
        txtUserSearch = new JTextField(12);
        txtUserSearch.setToolTipText("Tìm theo Họ tên, Mã, Username, Email");
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

        JButton btnSearch = new JButton("Lọc");
        btnSearch.addActionListener(e -> loadUsersTable());
        toolbar.add(btnSearch);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtUserSearch.setText("");
            cboUserRoleFilter.setSelectedIndex(0);
            cboUserStatusFilter.setSelectedIndex(0);
            loadUsersTable();
        });
        toolbar.add(btnRefresh);

        toolbar.add(new JSeparator(JSeparator.VERTICAL));

        JButton btnAddStaff = new JButton("+ Thêm Nhân viên");
        btnAddStaff.addActionListener(e -> showAddStaffDialog());
        toolbar.add(btnAddStaff);

        JButton btnAddMember = new JButton("+ Thêm Độc giả");
        btnAddMember.addActionListener(e -> showAddMemberDialog());
        toolbar.add(btnAddMember);

        JButton btnToggleStatus = new JButton("Khóa / Mở khóa");
        btnToggleStatus.setFont(btnToggleStatus.getFont().deriveFont(Font.BOLD, 12f));
        btnToggleStatus.addActionListener(e -> handleToggleUserStatus());
        toolbar.add(btnToggleStatus);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Mã người dùng", "Tài khoản", "Họ và tên", "Email", "SĐT", "Vai trò", "Trạng thái", "Thông tin bổ sung"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = UIUtils.createReadonlyTable(userTableModel);
        UIUtils.centerAlignColumns(userTable, 0, 1, 6, 7);
        UIUtils.setColumnWidth(userTable, 0, 50);
        UIUtils.setColumnWidth(userTable, 1, 100);
        UIUtils.setColumnWidth(userTable, 2, 110);
        UIUtils.setColumnWidth(userTable, 3, 170);
        UIUtils.setColumnWidth(userTable, 4, 160);
        UIUtils.setColumnWidth(userTable, 5, 110);
        UIUtils.setColumnWidth(userTable, 6, 90);
        UIUtils.setColumnWidth(userTable, 7, 90);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        return panel;
    }

    public void loadUsersTable() {
        try {
            List<User> users = userService.getAllUsers();
            String kw = txtUserSearch != null ? txtUserSearch.getText().trim().toLowerCase() : "";
            int roleFilterIdx = cboUserRoleFilter != null ? cboUserRoleFilter.getSelectedIndex() : 0;
            int statusFilterIdx = cboUserStatusFilter != null ? cboUserStatusFilter.getSelectedIndex() : 0;

            List<User> filtered = users.stream().filter(u -> {
                // Role filter
                if (roleFilterIdx == 1 && u.getRole() != Role.ADMIN) return false;
                if (roleFilterIdx == 2 && u.getRole() != Role.LIBRARIAN) return false;
                if (roleFilterIdx == 3 && u.getRole() != Role.MEMBER) return false;

                // Status filter
                if (statusFilterIdx == 1 && u.getStatus() != UserStatus.ACTIVE) return false;
                if (statusFilterIdx == 2 && u.getStatus() != UserStatus.LOCKED) return false;

                // Keyword search
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
                String extraInfo = "";
                if (u instanceof Staff s) {
                    extraInfo = "Phòng ban: " + (s.getDepartment() != null ? s.getDepartment() : "N/A");
                } else if (u instanceof Member m) {
                    extraInfo = "Đang mượn: " + m.getCurrentBorrowCount() + "/" + m.getMaxBorrowLimit() + " cuốn";
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
                        extraInfo
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Không thể tải danh sách người dùng: " + e.getMessage());
        }
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog(this, "Thêm Mới Nhân Viên / Quản Trị Viên", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

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
        form.add(new JLabel("Tên đăng nhập (Username) (*):"));
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
        JButton btnSave = new JButton("Lưu nhân viên");
        JButton btnCancel = new JButton("Hủy bỏ");

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
        JDialog dialog = new JDialog(this, "Thêm Mới Độc Giả Bạn Đọc", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 25, 10, 25));

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();

        form.add(new JLabel("Mã độc giả (VD: MBR002) (*):"));
        form.add(txtCode);
        form.add(new JLabel("Họ và tên (*):"));
        form.add(txtName);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtPhone);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnSave = new JButton("Lưu độc giả");
        JButton btnCancel = new JButton("Hủy bỏ");

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

        // Không cho phép tự khóa tài khoản Admin đang đăng nhập
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
                UIUtils.showError(this, "Lỗi cập nhật trạng thái: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // TAB 6: THỐNG KÊ & BÁO CÁO HỆ THỐNG (SYSTEM STATISTICS)
    // =========================================================================
    private JPanel createStatisticsTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header and Refresh Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("BÁO CÁO VÀ THỐNG KÊ CHỈ SỐ HOẠT ĐỘNG THƯ VIỆN");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(24, 90, 157));

        JButton btnRefreshStats = new JButton("Làm mới số liệu");
        btnRefreshStats.addActionListener(e -> loadStatisticsData());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnRefreshStats, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Center Content: Metric Cards + Overdue Table
        JPanel center = new JPanel(new BorderLayout(15, 15));

        statsCardsContainer = new JPanel(new GridLayout(2, 3, 12, 12));
        center.add(statsCardsContainer, BorderLayout.NORTH);

        // Overdue Borrows Table Panel
        JPanel overduePanel = new JPanel(new BorderLayout(5, 5));
        JLabel lblOverdue = new JLabel("Danh sách các sách đang bị mượn quá hạn (Cần thu hồi & xử phạt):");
        lblOverdue.setFont(lblOverdue.getFont().deriveFont(Font.BOLD, 13f));
        overduePanel.add(lblOverdue, BorderLayout.NORTH);

        String[] overdueCols = {"Mã phiếu", "Mã độc giả", "Họ tên độc giả", "Mã sách", "Tên sách", "Hạn trả", "Số ngày trễ", "Tiền phạt dự tính"};
        overdueTableModel = new DefaultTableModel(overdueCols, 0);
        overdueTable = UIUtils.createReadonlyTable(overdueTableModel);
        UIUtils.centerAlignColumns(overdueTable, 0, 1, 3, 5, 6);
        UIUtils.rightAlignColumns(overdueTable, 7);

        overduePanel.add(new JScrollPane(overdueTable), BorderLayout.CENTER);
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

            // Re-render Stat Cards
            statsCardsContainer.removeAll();
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số đầu sách", String.valueOf(totalTitles) + " đầu sách", new Color(24, 90, 157)));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số bản sách lưu hành", String.valueOf(totalCopies) + " cuốn", new Color(40, 167, 69)));
            statsCardsContainer.add(UIUtils.createStatCard("Số sách đang được mượn", String.valueOf(borrowedCopies) + " cuốn", new Color(255, 140, 0)));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng số độc giả (Members)", String.valueOf(totalMembers) + " bạn đọc", new Color(111, 66, 193)));
            statsCardsContainer.add(UIUtils.createStatCard("Phiếu mượn đang hiệu lực", String.valueOf(activeBorrows) + " phiếu", new Color(23, 162, 184)));
            statsCardsContainer.add(UIUtils.createStatCard("Tổng tiền phạt chưa thu", UIUtils.formatCurrency(totalUnpaidAmount), new Color(220, 53, 69)));

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
