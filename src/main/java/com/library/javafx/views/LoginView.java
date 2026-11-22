package com.library.javafx.views;

import com.library.javafx.JavaFxTheme;
import com.library.model.User;
import com.library.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Giao diện Đăng nhập JavaFX Version 2 Hiện Đại (LoginView).
 * - Bố cục Split-Screen toàn màn hình (Full-bleed), loại bỏ 100% khoảng viền trắng dư thừa xung quanh.
 * - Cột trái: Nhận diện thương hiệu Đỏ Bách Khoa tràn viền và nút điền tài khoản mẫu tiện lợi.
 * - Cột phải: Form đăng nhập trung tâm, tích hợp tính năng Bật / Tắt xem mật khẩu (Show/Hide Password).
 * - Kết nối trực tiếp xác thực qua UserService và mở hộp thoại đăng ký thẻ mới.
 */
public class LoginView extends HBox {

    // SVG Icons cho tính năng xem mật khẩu (Material Design 24x24)
    private static final String SVG_EYE_OPEN =
            "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";

    private static final String SVG_EYE_CLOSED =
            "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.44-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final TextField txtPasswordVisible;
    private final Button btnTogglePassword;
    private final Button btnLogin;
    private final Hyperlink lnkRegister;

    public LoginView(Stage stage, UserService userService, Consumer<User> onLoginSuccess) {
        // Thiết lập layout HBox toàn màn hình, 0 viền ngoài
        setFillHeight(true);
        setStyle("-fx-background-color: #FFFFFF;");
        setPadding(Insets.EMPTY);

        // 1. Khởi tạo các trường nhập liệu
        this.txtUsername = new TextField("admin");
        this.txtUsername.setPromptText("Nhập tên đăng nhập hoặc MSSV...");
        this.txtUsername.setStyle(JavaFxTheme.CSS_INPUT);

        // Trường mật khẩu ẩn (dấu chấm tròn)
        String pwdStyle = JavaFxTheme.CSS_INPUT + " -fx-padding: 8 38 8 12;";
        this.txtPassword = new PasswordField();
        this.txtPassword.setText("Admin@123");
        this.txtPassword.setPromptText("Nhập mật khẩu...");
        this.txtPassword.setStyle(pwdStyle);

        // Trường mật khẩu hiển thị văn bản thường
        this.txtPasswordVisible = new TextField();
        this.txtPasswordVisible.setPromptText("Nhập mật khẩu...");
        this.txtPasswordVisible.setStyle(pwdStyle);
        this.txtPasswordVisible.setVisible(false);
        this.txtPasswordVisible.setManaged(false);

        // Đồng bộ 2 chiều dữ liệu giữa trường ẩn và trường hiện
        this.txtPasswordVisible.textProperty().bindBidirectional(this.txtPassword.textProperty());

        // Nút bấm biểu tượng mắt Xem / Ẩn mật khẩu
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent(SVG_EYE_OPEN);
        eyeIcon.setFill(Color.web("#94A3B8"));
        eyeIcon.setScaleX(0.75);
        eyeIcon.setScaleY(0.75);

        this.btnTogglePassword = new Button();
        this.btnTogglePassword.setGraphic(eyeIcon);
        this.btnTogglePassword.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0 10 0 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        this.btnTogglePassword.setTooltip(new Tooltip("Hiển thị mật khẩu"));
        this.btnTogglePassword.setFocusTraversable(false);

        // Xử lý sự kiện chuyển đổi trạng thái xem mật khẩu
        this.btnTogglePassword.setOnAction(e -> {
            boolean isShowing = txtPasswordVisible.isVisible();
            if (isShowing) {
                // Đang hiện -> Ẩn mật khẩu (dùng PasswordField)
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                txtPassword.setVisible(true);
                txtPassword.setManaged(true);
                txtPassword.requestFocus();
                txtPassword.positionCaret(txtPassword.getText().length());

                eyeIcon.setContent(SVG_EYE_OPEN);
                eyeIcon.setFill(Color.web("#94A3B8"));
                btnTogglePassword.setTooltip(new Tooltip("Hiển thị mật khẩu"));
            } else {
                // Đang ẩn -> Hiện mật khẩu (dùng TextField)
                txtPassword.setVisible(false);
                txtPassword.setManaged(false);
                txtPasswordVisible.setVisible(true);
                txtPasswordVisible.setManaged(true);
                txtPasswordVisible.requestFocus();
                txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());

                eyeIcon.setContent(SVG_EYE_CLOSED);
                eyeIcon.setFill(Color.web(JavaFxTheme.COLOR_PRIMARY_RED));
                btnTogglePassword.setTooltip(new Tooltip("Ẩn mật khẩu"));
            }
        });

        this.btnTogglePassword.setOnMouseEntered(e -> {
            if (!txtPasswordVisible.isVisible()) {
                eyeIcon.setFill(Color.web("#334155"));
            }
        });
        this.btnTogglePassword.setOnMouseExited(e -> {
            if (!txtPasswordVisible.isVisible()) {
                eyeIcon.setFill(Color.web("#94A3B8"));
            }
        });

        // Hộp chứa mật khẩu xếp chồng nút mắt bên phải
        StackPane passwordContainer = new StackPane();
        StackPane.setAlignment(btnTogglePassword, Pos.CENTER_RIGHT);
        passwordContainer.getChildren().addAll(txtPassword, txtPasswordVisible, btnTogglePassword);

        this.btnLogin = new Button("Đăng Nhập Ngay →");
        this.lnkRegister = new Hyperlink("Đăng ký thẻ mới ngay");

        // =====================================================================
        // CỘT TRÁI: BANNER ĐỎ BÁCH KHOA TRÀN VIỀN (FULL-BLEED HUST BRANDING)
        // =====================================================================
        VBox leftBanner = new VBox(16);
        leftBanner.setPrefWidth(380);
        leftBanner.setMinWidth(340);
        leftBanner.setMaxWidth(420);
        leftBanner.setPadding(new Insets(48, 36, 40, 36));
        leftBanner.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #7F1D1D, #991B1B, #881337);" +
                "-fx-background-radius: 0;"
        );

        Label lblLogo = new Label("📚");
        lblLogo.setFont(Font.font("Segoe UI Emoji, Apple Color Emoji", 40));

        Label lblBrand = new Label("HUST Lib");
        lblBrand.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        lblBrand.setStyle("-fx-text-fill: white;");

        Label lblSub = new Label("Thư Viện ĐH Bách Khoa Hà Nội");
        lblSub.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblSub.setStyle("-fx-text-fill: #FDE68A;");

        Label lblDesc = new Label("Hệ thống quản lý mượn trả và tra cứu tài liệu học tập, nghiên cứu khoa học.");
        lblDesc.setFont(Font.font("System", 12));
        lblDesc.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.85); -fx-line-spacing: 2px;");
        lblDesc.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Hộp hướng dẫn tài khoản mẫu (Click để điền nhanh)
        VBox sampleBox = new VBox(6);
        sampleBox.setPadding(new Insets(12, 14, 12, 14));
        sampleBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.28); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 10;");

        Label lblSampleTitle = new Label("TÀI KHOẢN MẪU KIỂM THỬ (CLICK ĐỂ ĐIỀN):");
        lblSampleTitle.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblSampleTitle.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7);");

        Hyperlink accAdmin = new Hyperlink("• Quản trị : admin / Admin@123");
        accAdmin.setFont(Font.font("Monospaced", 11));
        accAdmin.setStyle("-fx-text-fill: #F8FAFC; -fx-padding: 2 0 2 0;");
        accAdmin.setOnAction(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("Admin@123");
        });

        Hyperlink accLib = new Hyperlink("• Thủ thư  : thuthu_mai / Lib@2025");
        accLib.setFont(Font.font("Monospaced", 11));
        accLib.setStyle("-fx-text-fill: #F8FAFC; -fx-padding: 2 0 2 0;");
        accLib.setOnAction(e -> {
            txtUsername.setText("thuthu_mai");
            txtPassword.setText("Lib@2025");
        });

        Hyperlink accMem = new Hyperlink("• Độc giả  : MBR001 (Thẻ bạn đọc)");
        accMem.setFont(Font.font("Monospaced", 11));
        accMem.setStyle("-fx-text-fill: #F8FAFC; -fx-padding: 2 0 2 0;");
        accMem.setOnAction(e -> {
            txtUsername.setText("MBR001");
            txtPassword.setText("");
        });

        sampleBox.getChildren().addAll(lblSampleTitle, accAdmin, accLib, accMem);
        leftBanner.getChildren().addAll(lblLogo, lblBrand, lblSub, lblDesc, spacer, sampleBox);

        // =====================================================================
        // CỘT PHẢI: FORM ĐĂNG NHẬP TRẮNG HIỆN ĐẠI CANH GIỮA
        // =====================================================================
        StackPane rightWrapper = new StackPane();
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);
        rightWrapper.setStyle("-fx-background-color: #FFFFFF;");

        VBox formCard = new VBox(14);
        formCard.setMaxWidth(380);
        formCard.setAlignment(Pos.CENTER_LEFT);
        formCard.setPadding(new Insets(30, 20, 30, 20));
        StackPane.setAlignment(formCard, Pos.CENTER);

        Label lblTitle = new Label("Đăng Nhập");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        lblTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Label lblSubTitle = new Label("Chào mừng Bạn đọc & Cán bộ Thư viện Bách Khoa");
        lblSubTitle.setFont(Font.font("System", 13));
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
        btnLogin.setPrefHeight(42);
        btnLogin.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY + " -fx-font-size: 14px;");

        // Xử lý sự kiện đăng nhập
        Runnable handleLogin = () -> {
            String u = txtUsername.getText().trim();
            String p = txtPassword.getText();
            if (u.isEmpty()) {
                JavaFxTheme.showError("Lỗi đăng nhập", "Vui lòng nhập tên đăng nhập hoặc mã thẻ bạn đọc!");
                return;
            }
            if (p.isEmpty() && !u.toUpperCase().startsWith("MBR") && !"member".equalsIgnoreCase(u)) {
                JavaFxTheme.showError("Lỗi đăng nhập", "Vui lòng nhập mật khẩu cho tài khoản cán bộ / thủ thư!");
                return;
            }
            try {
                User authenticated = userService != null ? userService.authenticate(u, p) : null;
                if (authenticated != null && onLoginSuccess != null) {
                    onLoginSuccess.accept(authenticated);
                } else if (userService == null && onLoginSuccess != null) {
                    // Demo fallback
                    JavaFxTheme.showInfo("Demo", "Đăng nhập mô phỏng thành công với tài khoản: " + u);
                }
            } catch (Exception ex) {
                JavaFxTheme.showError("Đăng nhập thất bại", ex.getMessage());
            }
        };

        btnLogin.setOnAction(e -> handleLogin.run());
        txtUsername.setOnAction(e -> {
            if (txtPasswordVisible.isVisible()) {
                txtPasswordVisible.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
        });
        txtPassword.setOnAction(e -> handleLogin.run());
        txtPasswordVisible.setOnAction(e -> handleLogin.run());

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

        formCard.getChildren().addAll(
                lblTitle, lblSubTitle,
                new Region() {{ setMinHeight(6); }},
                lblUser, txtUsername,
                lblPass, passwordContainer,
                chkRemember,
                new Region() {{ setMinHeight(8); }},
                btnLogin,
                regBox
        );

        rightWrapper.getChildren().add(formCard);

        getChildren().addAll(leftBanner, rightWrapper);
    }

    public LoginView() {
        this(null, null, null);
    }

    public Button getBtnLogin() { return btnLogin; }
    public TextField getTxtUsername() { return txtUsername; }
    public PasswordField getTxtPassword() { return txtPassword; }
    public TextField getTxtPasswordVisible() { return txtPasswordVisible; }
    public Button getBtnTogglePassword() { return btnTogglePassword; }
}
