package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Màn hình Đăng nhập Hiện đại (HUST Lib Modern Login Screen).
 * Thiết kế Split-Screen Card lấy cảm hứng từ nhận diện ĐH Bách Khoa Hà Nội.
 * Cột trái: Banner Đỏ thẫm Bách Khoa (#7F1D1D -> #B91C1C) cùng biểu tượng sách & bánh răng kỹ thuật.
 * Cột phải: Form đăng nhập tối giản, nút bấm HUST Red và chip chọn nhanh tài khoản mẫu.
 */
public class LoginFrame extends JFrame {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin;

    public LoginFrame() {
        this(new BookServiceImpl(), new CategoryServiceImpl(), new BorrowServiceImpl(), new FineServiceImpl(), new UserServiceImpl());
    }

    public LoginFrame(BookService bookService,
                      CategoryService categoryService,
                      BorrowService borrowService,
                      FineService fineService,
                      UserService userService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;

        initComponents();
    }

    private void initComponents() {
        setTitle("HUST Lib — Đăng nhập Hệ thống Thư viện ĐH Bách Khoa Hà Nội");
        setSize(820, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridLayout(1, 2));

        // =====================================================================
        // CỘT TRÁI: BANNER ĐỎ BÁCH KHOA (HUST BRAND PANEL)
        // =====================================================================
        JPanel leftBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Gradient Đỏ Bách Khoa
                GradientPaint gp = new GradientPaint(0, 0, HustTheme.PRIMARY_RED_DARK, w, h, HustTheme.PRIMARY_RED);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Họa tiết vòng tròn kỹ thuật / compa bánh răng mờ ảo
                g2.setColor(new Color(255, 255, 255, 18));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(w - 180, -40, 260, 260);
                g2.drawOval(w - 140, 0, 180, 180);
                g2.drawOval(-60, h - 160, 220, 220);

                // Biểu tượng Quyển sách & Bánh răng Bách Khoa
                int centerX = w / 2;
                int iconY = 130;

                // Vầng sáng biểu tượng
                RadialGradientPaint rgp = new RadialGradientPaint(
                        centerX, iconY + 15, 60,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(245, 158, 11, 70), new Color(245, 158, 11, 0)}
                );
                g2.setPaint(rgp);
                g2.fillOval(centerX - 60, iconY - 45, 120, 120);

                // Vẽ bánh răng vàng (Gear)
                g2.setColor(HustTheme.ACCENT_GOLD);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(centerX - 24, iconY - 24, 48, 48);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = (int) (centerX + 24 * Math.cos(angle));
                    int y1 = (int) (iconY + 24 * Math.sin(angle));
                    int x2 = (int) (centerX + 30 * Math.cos(angle));
                    int y2 = (int) (iconY + 30 * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }

                // Vẽ cuốn sách mở màu trắng
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Cánh trái sách
                g2.drawArc(centerX - 36, iconY - 5, 36, 30, 20, 150);
                g2.drawLine(centerX - 36, iconY + 18, centerX, iconY + 28);
                // Cánh phải sách
                g2.drawArc(centerX, iconY - 5, 36, 30, 10, 150);
                g2.drawLine(centerX, iconY + 28, centerX + 36, iconY + 18);
                // Gáy sách
                g2.drawLine(centerX, iconY + 10, centerX, iconY + 28);

                g2.dispose();
            }
        };
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(new EmptyBorder(190, 30, 30, 30));

        JLabel lblBrand = new JLabel("HUST Lib");
        lblBrand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Thư Viện ĐH Bách Khoa Hà Nội");
        lblSub.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblSub.setForeground(new Color(254, 243, 199));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTag = new JLabel("<html><center>Hệ thống Quản lý Mượn Trả & Tra cứu Sách</center></html>");
        lblTag.setFont(HustTheme.FONT_SMALL);
        lblTag.setForeground(new Color(255, 255, 255, 190));
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftBanner.add(lblBrand);
        leftBanner.add(Box.createVerticalStrut(6));
        leftBanner.add(lblSub);
        leftBanner.add(Box.createVerticalStrut(15));
        leftBanner.add(lblTag);

        leftBanner.add(Box.createVerticalGlue());
        JLabel lblVer = new JLabel("Phiên bản 2.0 • HUST Red Edition");
        lblVer.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblVer.setForeground(new Color(255, 255, 255, 140));
        lblVer.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftBanner.add(lblVer);

        // =====================================================================
        // CỘT PHẢI: FORM ĐĂNG NHẬP TRẮNG HIỆN ĐẠI (LOGIN FORM)
        // =====================================================================
        JPanel rightForm = new JPanel();
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBackground(Color.WHITE);
        rightForm.setBorder(new EmptyBorder(35, 40, 30, 40));

        JLabel lblHeader = new JLabel("Đăng Nhập Hệ Thống");
        lblHeader.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        lblHeader.setForeground(HustTheme.TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Chào mừng Giảng viên, Sinh viên & Bạn đọc Bách Khoa");
        lblDesc.setFont(HustTheme.FONT_SMALL);
        lblDesc.setForeground(HustTheme.TEXT_MUTED);

        rightForm.add(lblHeader);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(lblDesc);
        rightForm.add(Box.createVerticalStrut(22));

        // Trường Username
        JLabel lblUser = new JLabel("Tên đăng nhập hoặc Mã sinh viên / độc giả:");
        lblUser.setFont(HustTheme.FONT_BODY_BOLD);
        lblUser.setForeground(HustTheme.TEXT_PRIMARY);
        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "admin, thuthu_mai hoặc MBR001");
        txtUsername.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));

        rightForm.add(lblUser);
        rightForm.add(Box.createVerticalStrut(5));
        rightForm.add(txtUsername);
        rightForm.add(Box.createVerticalStrut(12));

        // Trường Password
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(HustTheme.FONT_BODY_BOLD);
        lblPass.setForeground(HustTheme.TEXT_PRIMARY);
        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu tài khoản");
        txtPassword.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));

        rightForm.add(lblPass);
        rightForm.add(Box.createVerticalStrut(5));
        rightForm.add(txtPassword);
        rightForm.add(Box.createVerticalStrut(6));

        // Checkbox hiển thị mật khẩu
        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setOpaque(false);
        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setFont(HustTheme.FONT_SMALL);
        chkShowPassword.setForeground(HustTheme.TEXT_MUTED);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        optionPanel.add(chkShowPassword, BorderLayout.WEST);
        optionPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));

        rightForm.add(optionPanel);
        rightForm.add(Box.createVerticalStrut(18));

        // Nút Đăng nhập Đỏ Bách Khoa
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(HustTheme.FONT_BODY_BOLD.deriveFont(14f));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(HustTheme.PRIMARY_RED);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> executeLogin());

        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(HustTheme.PRIMARY_RED_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(HustTheme.PRIMARY_RED);
            }
        });

        rightForm.add(btnLogin);

        // Nút liên kết Đăng ký tài khoản Độc giả mới
        JPanel regLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regLinkPanel.setOpaque(false);
        JLabel lblNoAcc = new JLabel("Chưa có thẻ bạn đọc?");
        lblNoAcc.setFont(HustTheme.FONT_SMALL);
        lblNoAcc.setForeground(HustTheme.TEXT_MUTED);

        JButton btnRegister = new JButton("<html><u>Đăng ký thẻ mới ngay</u></html>");
        btnRegister.setFont(HustTheme.FONT_SMALL.deriveFont(Font.BOLD));
        btnRegister.setForeground(HustTheme.PRIMARY_RED);
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> showRegisterDialog());

        regLinkPanel.add(lblNoAcc);
        regLinkPanel.add(btnRegister);
        regLinkPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));

        rightForm.add(Box.createVerticalStrut(10));
        rightForm.add(regLinkPanel);
        rightForm.add(Box.createVerticalStrut(12));

        // Khối chọn nhanh tài khoản mẫu (Clickable chips)
        JLabel lblQuick = new JLabel("Chọn nhanh tài khoản mẫu để thử nghiệm:");
        lblQuick.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblQuick.setForeground(HustTheme.TEXT_MUTED);
        rightForm.add(lblQuick);
        rightForm.add(Box.createVerticalStrut(8));

        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipPanel.setOpaque(false);

        JButton chipAdmin = createRoleChip("Admin", "admin", "Admin@123");
        JButton chipLib = createRoleChip("Thủ thư", "thuthu_mai", "Lib@2025");
        JButton chipMem = createRoleChip("Độc giả", "MBR001", "");

        chipPanel.add(chipAdmin);
        chipPanel.add(chipLib);
        chipPanel.add(chipMem);
        chipPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));

        rightForm.add(chipPanel);

        container.add(leftBanner);
        container.add(rightForm);

        add(container, BorderLayout.CENTER);

        // Phím Enter để đăng nhập
        getRootPane().setDefaultButton(btnLogin);
    }

    private JButton createRoleChip(String label, String username, String password) {
        JButton btn = new JButton(label);
        btn.setFont(HustTheme.FONT_SMALL);
        btn.setForeground(HustTheme.PRIMARY_RED);
        btn.setBackground(new Color(254, 242, 242));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            txtUsername.setText(username);
            txtPassword.setText(password);
            txtPassword.requestFocus();
        });

        return btn;
    }

    private void executeLogin() {
        String input = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (input.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Tên đăng nhập hoặc Mã độc giả.");
            txtUsername.requestFocus();
            return;
        }

        try {
            User user = null;

            // 1. Thử xác thực username + password qua UserService
            try {
                user = userService.authenticate(input, password);
            } catch (ValidationException | BorrowException authEx) {
                // Hỗ trợ bạn đọc đăng nhập bằng mã thẻ MBR...
                if (input.toUpperCase().startsWith("MBR") || password.isEmpty()) {
                    try {
                        User potentialMember = userService.getUserByCode(input.toUpperCase());
                        if (potentialMember instanceof Member m) {
                            if (m.getStatus() == UserStatus.LOCKED) {
                                UIUtils.showError(this, "Tài khoản độc giả này hiện đang bị KHÓA. Vui lòng liên hệ thủ thư!");
                                return;
                            }
                            user = m;
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (user == null) {
                    throw authEx;
                }
            }

            if (user == null) {
                UIUtils.showError(this, "Thông tin đăng nhập không hợp lệ!");
                return;
            }

            // 2. Điều hướng theo Vai trò (Role-based Navigation)
            dispose();

            final User finalUser = user;
            SwingUtilities.invokeLater(() -> {
                switch (finalUser.getRole()) {
                    case ADMIN -> {
                        AdminDashboardFrame adminFrame = new AdminDashboardFrame(
                                (Staff) finalUser, bookService, categoryService, borrowService, fineService, userService);
                        adminFrame.setVisible(true);
                    }
                    case LIBRARIAN -> {
                        LibrarianDashboardFrame libFrame = new LibrarianDashboardFrame(
                                (Staff) finalUser, bookService, categoryService, borrowService, fineService, userService);
                        libFrame.setVisible(true);
                    }
                    case MEMBER -> {
                        MemberDashboardFrame memFrame = new MemberDashboardFrame(
                                (Member) finalUser, bookService, categoryService, borrowService, fineService, userService);
                        memFrame.setVisible(true);
                    }
                }
            });

        } catch (ValidationException ve) {
            UIUtils.showError(this, ve.getMessage());
        } catch (BorrowException be) {
            UIUtils.showError(this, be.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Lỗi đăng nhập: " + ex.getMessage());
        }
    }

    private void showRegisterDialog() {
        RegisterMemberDialog dialog = new RegisterMemberDialog(this, userService, (regUser, regPass) -> {
            txtUsername.setText(regUser);
            txtPassword.setText(regPass);
            btnLogin.requestFocus();
        });
        dialog.setVisible(true);
    }
}
