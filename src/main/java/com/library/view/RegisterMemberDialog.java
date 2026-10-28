package com.library.view;

import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hộp thoại Đăng ký Thẻ Bạn đọc mới (HUST Lib Member Registration Dialog).
 * Cho phép sinh viên, học viên và bạn đọc tự đăng ký tài khoản thẻ thư viện trực tuyến.
 * Tuân thủ HUST Red Theme và nguyên tắc Loose Coupling (gọi qua UserService).
 */
public class RegisterMemberDialog extends JDialog {

    public interface OnRegisterSuccessListener {
        void onSuccess(String registeredIdentifier, String registeredPassword);
    }

    private final UserService userService;
    private final OnRegisterSuccessListener successListener;

    private JTextField txtUserCode;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JCheckBox chkShowPassword;

    public RegisterMemberDialog(JFrame parent, UserService userService, OnRegisterSuccessListener successListener) {
        super(parent, "HUST Lib — Đăng Ký Thẻ Bạn Đọc Trực Tuyến", true);
        this.userService = userService;
        this.successListener = successListener;

        initComponents();
    }

    private void initComponents() {
        setSize(520, 590);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("ĐĂNG KÝ THẺ BẠN ĐỌC HUST");
        lblTitle.setFont(HustTheme.FONT_HEADER.deriveFont(18f));
        lblTitle.setForeground(HustTheme.PRIMARY_RED);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Thư Viện Đại Học Bách Khoa Hà Nội");
        lblSubtitle.setFont(HustTheme.FONT_BODY_BOLD);
        lblSubtitle.setForeground(HustTheme.TEXT_PRIMARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNote = new JLabel("Dành cho Sinh viên, Học viên & Bạn đọc mới tham gia thư viện");
        lblNote.setFont(HustTheme.FONT_SMALL);
        lblNote.setForeground(HustTheme.TEXT_MUTED);
        lblNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSubtitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblNote);
        headerPanel.add(Box.createVerticalStrut(14));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Fields Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        txtUserCode = new JTextField();
        txtUserCode.putClientProperty("JTextField.placeholderText", "VD: 20210001 hoặc MBR002");
        addFormField(formPanel, gbc, row++, "Mã SV / Mã bạn đọc (*):", txtUserCode);

        txtFullName = new JTextField();
        txtFullName.putClientProperty("JTextField.placeholderText", "VD: Phạm Minh Tuấn");
        addFormField(formPanel, gbc, row++, "Họ và tên (*):", txtFullName);

        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "VD: tuan.pm210001");
        addFormField(formPanel, gbc, row++, "Tên đăng nhập (*):", txtUsername);

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Tối thiểu 6 ký tự");
        addFormField(formPanel, gbc, row++, "Mật khẩu (*):", txtPassword);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.putClientProperty("JTextField.placeholderText", "Nhập lại mật khẩu");
        addFormField(formPanel, gbc, row++, "Xác nhận mật khẩu (*):", txtConfirmPassword);

        txtEmail = new JTextField();
        txtEmail.putClientProperty("JTextField.placeholderText", "VD: tuan.pm210001@sis.hust.edu.vn");
        addFormField(formPanel, gbc, row++, "Email liên hệ:", txtEmail);

        txtPhone = new JTextField();
        txtPhone.putClientProperty("JTextField.placeholderText", "VD: 0987654321");
        addFormField(formPanel, gbc, row++, "Số điện thoại:", txtPhone);

        // Checkbox hiện mật khẩu
        gbc.gridy = row++;
        gbc.gridx = 1;
        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setFont(HustTheme.FONT_SMALL);
        chkShowPassword.setForeground(HustTheme.TEXT_MUTED);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(e -> {
            char echo = chkShowPassword.isSelected() ? (char) 0 : '•';
            txtPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });
        formPanel.add(chkShowPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnCancel = UIUtils.createSecondaryButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UIUtils.createPrimaryButton("Đăng ký ngay");
        btnSubmit.addActionListener(e -> executeRegistration());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Enter key submits
        getRootPane().setDefaultButton(btnSubmit);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(HustTheme.FONT_BODY_BOLD);
        lbl.setForeground(HustTheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setPreferredSize(new Dimension(240, 34));
        panel.add(field, gbc);
    }

    private void executeRegistration() {
        String userCode = txtUserCode.getText().trim();
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String confirmPass = new String(txtConfirmPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (userCode.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Mã sinh viên hoặc Mã độc giả.");
            txtUserCode.requestFocus();
            return;
        }
        if (fullName.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Họ và tên.");
            txtFullName.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Tên đăng nhập.");
            txtUsername.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            UIUtils.showWarning(this, "Vui lòng nhập Mật khẩu.");
            txtPassword.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            UIUtils.showWarning(this, "Mật khẩu phải chứa ít nhất 6 ký tự để đảm bảo an toàn.");
            txtPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirmPass)) {
            UIUtils.showWarning(this, "Mật khẩu xác nhận không khớp. Vui lòng nhập lại.");
            txtConfirmPassword.requestFocus();
            return;
        }

        try {
            Member member = userService.registerMember(userCode, username, pass, fullName, email, phone);

            UIUtils.showSuccess(this, "Chúc mừng bạn đã đăng ký Thẻ Bạn đọc HUST thành công!\n\n" +
                    "- Mã thẻ / MSSV: " + member.getUserCode() + "\n" +
                    "- Họ và tên: " + member.getFullName() + "\n" +
                    "- Tên đăng nhập: " + member.getUsername() + "\n" +
                    "- Hạn mức mượn sách: Tối đa " + member.getMaxBorrowLimit() + " cuốn cùng lúc\n\n" +
                    "Thông tin tài khoản đã được tự động điền vào màn hình đăng nhập!");

            if (successListener != null) {
                // Ưu tiên điền username và password để người dùng đăng nhập ngay
                successListener.onSuccess(member.getUsername(), pass);
            }

            dispose();
        } catch (ValidationException ve) {
            UIUtils.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Lỗi khi đăng ký thẻ bạn đọc: " + ex.getMessage());
        }
    }
}
