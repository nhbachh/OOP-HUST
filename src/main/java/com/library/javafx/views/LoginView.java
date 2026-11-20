package com.library.javafx.views;

import com.library.javafx.JavaFxTheme;
import com.library.model.User;
import com.library.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Giao diện Đăng nhập JavaFX kết nối nghiệp vụ thực tế (LoginView).
 * Tích hợp xác thực qua UserService, nút chọn tài khoản mẫu nhanh, và đăng ký thẻ mới.
 */
public class LoginView extends StackPane {

    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final Button btnLogin;
    private final Hyperlink lnkRegister;

    public LoginView(Stage stage, UserService userService, Consumer<User> onLoginSuccess) {
        // Khởi tạo các trường nhập liệu trước
        this.txtUsername = new TextField("member");
        this.txtUsername.setPromptText("Nhập tên đăng nhập hoặc MSSV...");
        this.txtUsername.setStyle(JavaFxTheme.CSS_INPUT);

        this.txtPassword = new PasswordField();
        this.txtPassword.setText("mem123");
        this.txtPassword.setPromptText("Nhập mật khẩu...");
        this.txtPassword.setStyle(JavaFxTheme.CSS_INPUT);

        this.btnLogin = new Button("Đăng Nhập Ngay →");
        this.lnkRegister = new Hyperlink("Đăng ký thẻ mới ngay");

        setStyle("-fx-background-color: " + JavaFxTheme.COLOR_BG_LIGHT + ";");
        setPadding(new Insets(20));

        // Khung cửa sổ thẻ nổi chính giữa (Card Window)
        HBox container = new HBox();
        container.setMaxWidth(760);
        container.setMaxHeight(470);
        container.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-border-color: " + JavaFxTheme.COLOR_BORDER + ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 20, 0, 0, 8);"
        );

        // =====================================================================
        // CỘT TRÁI: BANNER ĐỎ BÁCH KHOA (HUST BRANDING)
        // =====================================================================
        VBox leftBanner = new VBox(14);
        leftBanner.setPrefWidth(310);
        leftBanner.setPadding(new Insets(36, 28, 28, 28));
        leftBanner.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #7F1D1D, #B91C1C, #991B1B);" +
                "-fx-background-radius: 14 0 0 14;"
        );

        Label lblLogo = new Label("📚");
        lblLogo.setFont(Font.font("Segoe UI Emoji, Apple Color Emoji", 36));

        Label lblBrand = new Label("HUST Lib");
        lblBrand.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 26));
        lblBrand.setStyle("-fx-text-fill: white;");

        Label lblSub = new Label("Thư Viện ĐH Bách Khoa Hà Nội");
        lblSub.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblSub.setStyle("-fx-text-fill: #FDE68A;");

        Label lblDesc = new Label("Hệ thống quản lý mượn trả và tra cứu tài liệu học tập, nghiên cứu khoa học.");
        lblDesc.setFont(Font.font("System", 11));
        lblDesc.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.85);");
        lblDesc.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Hộp hướng dẫn tài khoản mẫu (Click để điền nhanh)
        VBox sampleBox = new VBox(4);
        sampleBox.setPadding(new Insets(10));
        sampleBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25); -fx-background-radius: 8;");
        Label lblSampleTitle = new Label("TÀI KHOẢN MẪU (CLICK ĐỂ ĐIỀN):");
        lblSampleTitle.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblSampleTitle.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6);");

        Hyperlink accAdmin = new Hyperlink("• Quản trị: admin / admin123");
        accAdmin.setFont(Font.font("Monospaced", 10));
        accAdmin.setStyle("-fx-text-fill: white; -fx-padding: 0;");
        accAdmin.setOnAction(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
        });

        Hyperlink accLib = new Hyperlink("• Thủ thư: librarian / lib123");
        accLib.setFont(Font.font("Monospaced", 10));
        accLib.setStyle("-fx-text-fill: white; -fx-padding: 0;");
        accLib.setOnAction(e -> {
            txtUsername.setText("librarian");
            txtPassword.setText("lib123");
        });

        Hyperlink accMem = new Hyperlink("• Độc giả: member / mem123");
        accMem.setFont(Font.font("Monospaced", 10));
        accMem.setStyle("-fx-text-fill: white; -fx-padding: 0;");
        accMem.setOnAction(e -> {
            txtUsername.setText("member");
            txtPassword.setText("mem123");
        });

        sampleBox.getChildren().addAll(lblSampleTitle, accAdmin, accLib, accMem);

        leftBanner.getChildren().addAll(lblLogo, lblBrand, lblSub, lblDesc, spacer, sampleBox);

        // =====================================================================
        // CỘT PHẢI: FORM ĐĂNG NHẬP TRẮNG HIỆN ĐẠI
        // =====================================================================
        VBox rightForm = new VBox(12);
        rightForm.setPadding(new Insets(36, 36, 30, 36));
        HBox.setHgrow(rightForm, Priority.ALWAYS);
        rightForm.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Đăng Nhập");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Label lblSubTitle = new Label("Chào mừng Bạn đọc & Cán bộ Thư viện Bách Khoa");
        lblSubTitle.setFont(Font.font("System", 12));
        lblSubTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");

        // Input Username
        Label lblUser = new Label("Tên đăng nhập hoặc MSSV:");
        lblUser.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblUser.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        // Input Password
        Label lblPass = new Label("Mật khẩu:");
        lblPass.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblPass.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        CheckBox chkRemember = new CheckBox("Ghi nhớ tài khoản trên máy này");
        chkRemember.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + "; -fx-font-size: 11px;");

        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);

        // Xử lý sự kiện đăng nhập
        Runnable handleLogin = () -> {
            String u = txtUsername.getText().trim();
            String p = txtPassword.getText();
            if (u.isEmpty() || p.isEmpty()) {
                JavaFxTheme.showError("Lỗi đăng nhập", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
                return;
            }
            try {
                User authenticated = userService.authenticate(u, p);
                if (authenticated != null && onLoginSuccess != null) {
                    onLoginSuccess.accept(authenticated);
                }
            } catch (Exception ex) {
                JavaFxTheme.showError("Đăng nhập thất bại", ex.getMessage());
            }
        };

        btnLogin.setOnAction(e -> handleLogin.run());
        txtUsername.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> handleLogin.run());

        HBox regBox = new HBox(4);
        regBox.setAlignment(Pos.CENTER);
        Label lblNoAcc = new Label("Chưa có thẻ bạn đọc?");
        lblNoAcc.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        lnkRegister.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        lnkRegister.setOnAction(e -> {
            RegisterDialog dialog = new RegisterDialog(stage, userService, registered -> {
                txtUsername.setText(registered.getUsername());
                txtPassword.setText("");
                txtPassword.requestFocus();
            });
            dialog.showAndWait();
        });
        regBox.getChildren().addAll(lblNoAcc, lnkRegister);

        rightForm.getChildren().addAll(
                lblTitle, lblSubTitle,
                new Region() {{ setMinHeight(6); }},
                lblUser, txtUsername,
                lblPass, txtPassword,
                chkRemember,
                new Region() {{ setMinHeight(8); }},
                btnLogin,
                regBox
        );

        container.getChildren().addAll(leftBanner, rightForm);
        getChildren().add(container);
    }

    public LoginView() {
        this(null, null, null);
    }

    public Button getBtnLogin() { return btnLogin; }
    public TextField getTxtUsername() { return txtUsername; }
    public PasswordField getTxtPassword() { return txtPassword; }
}
