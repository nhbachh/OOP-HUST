package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Cửa sổ Đăng nhập Hệ thống (Login Frame) thiết kế hiện đại với FlatLaf.
 * Kích thước cố định 420x480, căn giữa màn hình.
 * Hỗ trợ đăng nhập cho cả Quản trị viên, Thủ thư (Username + Mật khẩu) và Độc giả (Mã thẻ bạn đọc).
 * Điều hướng tự động dựa trên Vai trò (Role-based Navigation).
 */
public class LoginFrame extends JFrame {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    /**
     * Constructor mặc định khởi tạo các Service implementations tương ứng.
     */
    public LoginFrame() {
        this(new BookServiceImpl(), new CategoryServiceImpl(), new BorrowServiceImpl(), new FineServiceImpl(), new UserServiceImpl());
    }

    /**
     * Constructor nhận vào các Service (hỗ trợ Dependency Injection và tái sử dụng).
     */
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
        setTitle("Đăng nhập — Hệ thống Quản lý Thư viện");
        setSize(420, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // Header Title
        JLabel lblLogo = new JLabel("THƯ VIỆN ĐIỆN TỬ");
        lblLogo.setFont(lblLogo.getFont().deriveFont(Font.BOLD, 22f));
        lblLogo.setForeground(new Color(24, 90, 157));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Hệ thống Quản lý Mượn Trả Sách");
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 12f));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblLogo);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(lblSub);
        mainPanel.add(Box.createVerticalStrut(25));

        // Form Fields
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 4, 8));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(360, 180));

        JLabel lblUser = new JLabel("Tên đăng nhập hoặc Mã độc giả:");
        lblUser.setFont(lblUser.getFont().deriveFont(Font.BOLD, 12f));
        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "VD: admin, thuthu_mai hoặc MBR001");

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(lblPass.getFont().deriveFont(Font.BOLD, 12f));
        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu tài khoản");

        formPanel.add(lblUser);
        formPanel.add(txtUsername);
        formPanel.add(lblPass);
        formPanel.add(txtPassword);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(360, 38));

        btnExit = new JButton("Thoát");
        btnExit.setFont(btnExit.getFont().deriveFont(Font.PLAIN, 13f));
        btnExit.addActionListener(e -> System.exit(0));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 13f));
        btnLogin.setForeground(new Color(24, 90, 157));
        btnLogin.addActionListener(e -> executeLogin());

        btnPanel.add(btnExit);
        btnPanel.add(btnLogin);

        mainPanel.add(btnPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Hint Card for Demo credentials
        JPanel hintBox = new JPanel(new GridLayout(4, 1, 2, 2));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 235, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        hintBox.setBackground(new Color(248, 250, 252));
        hintBox.setMaximumSize(new Dimension(360, 95));

        JLabel h0 = new JLabel("Tài khoản mẫu thử nghiệm:");
        h0.setFont(h0.getFont().deriveFont(Font.BOLD, 11f));
        h0.setForeground(new Color(24, 90, 157));

        JLabel h1 = new JLabel("• Admin: admin / Admin@123");
        h1.setFont(h1.getFont().deriveFont(Font.PLAIN, 11f));

        JLabel h2 = new JLabel("• Thủ thư: thuthu_mai / Lib@2025");
        h2.setFont(h2.getFont().deriveFont(Font.PLAIN, 11f));

        JLabel h3 = new JLabel("• Độc giả: MBR001 (không cần mật khẩu)");
        h3.setFont(h3.getFont().deriveFont(Font.PLAIN, 11f));

        hintBox.add(h0);
        hintBox.add(h1);
        hintBox.add(h2);
        hintBox.add(h3);

        mainPanel.add(hintBox);

        add(mainPanel, BorderLayout.CENTER);

        // Enter key activates login
        getRootPane().setDefaultButton(btnLogin);
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
                // Nếu authenticate thất bại, kiểm tra xem có phải là Độc giả đăng nhập bằng mã thẻ MBR... hay không
                if (input.toUpperCase().startsWith("MBR") || password.isEmpty()) {
                    try {
                        User potentialMember = userService.getUserByCode(input.toUpperCase());
                        if (potentialMember instanceof Member m) {
                            if (m.getStatus() == UserStatus.LOCKED) {
                                UIUtils.showError(this, "Tài khoản độc giả này hiện đang bị KHÓA. Vui lòng liên hệ thủ thư!");
                                return;
                            }
                            // Nếu độc giả chưa cấu hình mật khẩu hoặc nhập đúng mã thẻ
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
            dispose(); // Đóng LoginFrame

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
}
