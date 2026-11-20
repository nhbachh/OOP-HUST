package com.library.javafx.views;

import com.library.javafx.JavaFxTheme;
import com.library.model.Member;
import com.library.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Hộp thoại đăng ký thẻ bạn đọc mới bằng JavaFX (RegisterDialog).
 * Kết nối trực tiếp với UserService.registerMember().
 */
public class RegisterDialog extends Stage {

    public RegisterDialog(Stage owner, UserService userService, Consumer<Member> onSuccess) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        setTitle("Đăng Ký Thẻ Bạn Đọc Mới — HUST Lib");
        setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color: #FFFFFF;");

        // Header
        Label lblHead = new Label("ĐĂNG KÝ THẺ BẠN ĐỌC HUST");
        lblHead.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblHead.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        Label lblSub = new Label("Dành cho Sinh viên, Giảng viên & Học viên ĐH Bách Khoa Hà Nội");
        lblSub.setFont(Font.font("System", 12));
        lblSub.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        TextField txtCode = new TextField();
        txtCode.setPromptText("VD: SV20241234");
        txtCode.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Tên đăng nhập (viết liền)");
        txtUsername.setStyle(JavaFxTheme.CSS_INPUT);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Mật khẩu (tối thiểu 6 ký tự)");
        txtPass.setStyle(JavaFxTheme.CSS_INPUT);

        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Nhập lại mật khẩu");
        txtConfirm.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtFullName = new TextField();
        txtFullName.setPromptText("Họ và tên đầy đủ");
        txtFullName.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email (VD: an.nv24@sis.hust.edu.vn)");
        txtEmail.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Số điện thoại liên hệ");
        txtPhone.setStyle(JavaFxTheme.CSS_INPUT);

        grid.add(new Label("Mã SV / Mã thẻ (*):") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 0);
        grid.add(txtCode, 1, 0);

        grid.add(new Label("Tên đăng nhập (*):") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 1);
        grid.add(txtUsername, 1, 1);

        grid.add(new Label("Họ và tên (*):") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 2);
        grid.add(txtFullName, 1, 2);

        grid.add(new Label("Mật khẩu (*):") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 3);
        grid.add(txtPass, 1, 3);

        grid.add(new Label("Xác nhận mật khẩu (*):") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 4);
        grid.add(txtConfirm, 1, 4);

        grid.add(new Label("Email HUST:") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 5);
        grid.add(txtEmail, 1, 5);

        grid.add(new Label("Số điện thoại:") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); }}, 0, 6);
        grid.add(txtPhone, 1, 6);

        // Buttons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnCancel.setOnAction(e -> close());

        Button btnSubmit = new Button("Hoàn Tất Đăng Ký");
        btnSubmit.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnSubmit.setOnAction(e -> {
            String code = txtCode.getText().trim();
            String user = txtUsername.getText().trim();
            String name = txtFullName.getText().trim();
            String p1 = txtPass.getText();
            String p2 = txtConfirm.getText();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            if (code.isEmpty() || user.isEmpty() || name.isEmpty() || p1.isEmpty()) {
                JavaFxTheme.showError("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường bắt buộc (*)");
                return;
            }
            if (!p1.equals(p2)) {
                JavaFxTheme.showError("Mật khẩu không khớp", "Xác nhận mật khẩu không trùng khớp!");
                return;
            }
            if (p1.length() < 6) {
                JavaFxTheme.showError("Mật khẩu quá ngắn", "Mật khẩu phải có độ dài tối thiểu 6 ký tự!");
                return;
            }

            try {
                Member registered = userService.registerMember(code, user, p1, name, email.isEmpty() ? null : email, phone.isEmpty() ? null : phone);
                JavaFxTheme.showInfo("Đăng ký thành công", "Chúc mừng bạn đã tạo thẻ bạn đọc thành công!\nMã thẻ: " + code + "\nTên đăng nhập: " + user);
                if (onSuccess != null) {
                    onSuccess.accept(registered);
                }
                close();
            } catch (Exception ex) {
                JavaFxTheme.showError("Đăng ký thất bại", ex.getMessage());
            }
        });

        btnBox.getChildren().addAll(btnCancel, btnSubmit);

        root.getChildren().addAll(lblHead, lblSub, new Separator(), grid, new Separator(), btnBox);

        Scene scene = new Scene(root, 480, 480);
        setScene(scene);
    }
}
