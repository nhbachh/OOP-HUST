package com.library.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp cấu hình Nhận diện Thương hiệu & Màu sắc ĐH Bách Khoa Hà Nội (HUST Theme).
 * Cung cấp hệ thống màu chuẩn: Đỏ Bách Khoa (Crimson Red), Đen Burgundy, Vàng Kỹ thuật
 * và các thiết lập FlatLaf hiện đại (bo góc, chiều cao dòng bảng, font chữ).
 */
public final class HustTheme {

    // =========================================================================
    // HỆ THỐNG MÀU SẮC ĐẠI HỌC BÁCH KHOA HÀ NỘI (HUST COLOR PALETTE)
    // =========================================================================
    /** Màu Đỏ Bách Khoa chủ đạo (#B91C1C) */
    public static final Color PRIMARY_RED = new Color(185, 28, 28);

    /** Màu Đỏ Bách Khoa khi Hover / Active (#DC2626) */
    public static final Color PRIMARY_RED_HOVER = new Color(220, 38, 38);

    /** Màu Đỏ Thẫm / Dark Crimson (#7F1D1D) */
    public static final Color PRIMARY_RED_DARK = new Color(127, 29, 29);

    /** Màu Vàng Kỹ thuật / Bánh răng compa Bách Khoa (#F59E0B) */
    public static final Color ACCENT_GOLD = new Color(245, 158, 11);

    /** Màu nền Sidebar tối / Dark Burgundy Charcoal (#18181B) */
    public static final Color SIDEBAR_BG = new Color(24, 24, 27);

    /** Màu nền mục menu Sidebar khi được chọn (#B91C1C) */
    public static final Color SIDEBAR_ACTIVE = new Color(185, 28, 28);

    /** Màu nền mục menu Sidebar khi hover (#27272A) */
    public static final Color SIDEBAR_HOVER = new Color(39, 39, 42);

    /** Màu nền khu vực nội dung chính (#F8FAFC) */
    public static final Color CONTENT_BG = new Color(248, 250, 252);

    /** Màu nền Card / Bảng dữ liệu trắng (#FFFFFF) */
    public static final Color CARD_BG = Color.WHITE;

    /** Màu đường viền nhẹ (#E2E8F0) */
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    /** Màu chữ chính / Dark Slate (#0F172A) */
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);

    /** Màu chữ phụ / Slate Gray (#64748B) */
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // Huy hiệu trạng thái (Status Pills)
    public static final Color PILL_AVAILABLE_BG = new Color(236, 253, 245);
    public static final Color PILL_AVAILABLE_FG = new Color(5, 150, 105);

    public static final Color PILL_BORROWED_BG = new Color(254, 243, 199);
    public static final Color PILL_BORROWED_FG = new Color(217, 119, 6);

    public static final Color PILL_OVERDUE_BG = new Color(254, 226, 226);
    public static final Color PILL_OVERDUE_FG = new Color(185, 28, 28);

    public static final Color PILL_ROLE_ADMIN_BG = new Color(254, 226, 226);
    public static final Color PILL_ROLE_ADMIN_FG = new Color(153, 27, 27);

    // =========================================================================
    // FONT CHỮ CHUẨN HIỆN ĐẠI
    // =========================================================================
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_STAT_NUMBER = new Font(Font.SANS_SERIF, Font.BOLD, 22);

    private HustTheme() {
        // Utility class
    }

    /**
     * Khởi tạo và thiết lập FlatLaf Look and Feel theo phong cách hiện đại ĐH Bách Khoa Hà Nội.
     */
    public static void setupFlatLaf() {
        try {
            // Bo góc toàn diện cho các thành phần UI
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.showButtons", false);

            // Bảng dữ liệu hiện đại
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("TableHeader.height", 38);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", new Color(241, 245, 249));
            UIManager.put("Table.selectionBackground", new Color(254, 226, 226));
            UIManager.put("Table.selectionForeground", new Color(153, 27, 27));

            // Focus ring tinh tế
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", PRIMARY_RED);

            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Không thể khởi tạo FlatLaf: " + e.getMessage());
        }
    }
}
