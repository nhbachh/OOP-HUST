package com.library.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Hệ thống Theme & Phong cách thiết kế giao diện JavaFX hiện đại (HUST Red Style).
 * Cung cấp các hằng số màu sắc, CSS inline và các hàm dựng giao diện tái sử dụng.
 */
public final class JavaFxTheme {

    public static final String COLOR_PRIMARY_RED = "#B91C1C";
    public static final String COLOR_PRIMARY_HOVER = "#991B1B";
    public static final String COLOR_PRIMARY_LIGHT = "#FEF2F2";
    public static final String COLOR_ACCENT_GOLD = "#D97706";
    public static final String COLOR_SUCCESS_GREEN = "#059669";
    public static final String COLOR_TEXT_PRIMARY = "#0F172A";
    public static final String COLOR_TEXT_MUTED = "#64748B";
    public static final String COLOR_BORDER = "#E2E8F0";
    public static final String COLOR_BG_LIGHT = "#F8FAFC";
    public static final String COLOR_CARD_BG = "#FFFFFF";

    public static final String CSS_BUTTON_PRIMARY =
            "-fx-background-color: " + COLOR_PRIMARY_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;";

    public static final String CSS_BUTTON_SECONDARY =
            "-fx-background-color: #FFFFFF;" +
            "-fx-text-fill: " + COLOR_TEXT_PRIMARY + ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;";

    public static final String CSS_INPUT =
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: #CBD5E1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-font-size: 13px;";

    public static final String CSS_CARD =
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 16 18 16 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);";

    private JavaFxTheme() {}

    /**
     * Tạo một Card thống kê chỉ số KPI hiện đại, tự động co giãn không bao giờ bị cắt chữ.
     */
    public static VBox createStatCard(String title, String value, String accentColorHex, String icon) {
        VBox card = new VBox(8);
        card.setStyle(CSS_CARD);
        HBox.setHgrow(card, Priority.ALWAYS);

        // Header: Icon + Title
        HBox topBox = new HBox(6);
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label(icon);
        lblIcon.setFont(Font.font("Segoe UI Emoji, Apple Color Emoji, SansSerif", 16));

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        lblTitle.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        topBox.getChildren().addAll(lblIcon, lblTitle);

        // Value to rõ ràng
        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblValue.setStyle("-fx-text-fill: " + COLOR_TEXT_PRIMARY + ";");
        lblValue.setWrapText(false);

        // Accent indicator bar ở đáy card
        Region accentBar = new Region();
        accentBar.setMinHeight(3);
        accentBar.setMaxHeight(3);
        accentBar.setStyle("-fx-background-color: " + accentColorHex + "; -fx-background-radius: 2;");

        card.getChildren().addAll(topBox, lblValue, accentBar);
        return card;
    }

    /**
     * Tạo Header Section Panel chuẩn phong cách Bách Khoa.
     */
    public static VBox createHeaderSection(String title, String subtitle) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(10, 0, 14, 0));

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + COLOR_PRIMARY_RED + ";");

        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitle.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        box.getChildren().addAll(lblTitle, lblSubtitle);
        return box;
    }

    /**
     * Định dạng tiền tệ VNĐ chuẩn Việt Nam (VD: 20,000 VNĐ).
     */
    public static String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(amount) + " VNĐ";
    }

    /**
     * Hiển thị hộp thoại thông báo thành công / thông tin.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại thông báo lỗi.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận (Yes/No).
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        java.util.Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
