package com.library.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Lớp tiện ích giao diện dùng chung (UI Utilities) — Phiên bản Bách Khoa Hiện Đại (HUST Theme).
 * Cung cấp các hàm tạo bảng phẳng bo góc, hiển thị Status Pills viên thuốc, nút bấm HUST Red,
 * Card thống kê KPI và các hộp thoại thông báo.
 */
public final class UIUtils {

    private static final DecimalFormat CURRENCY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator(',');
        CURRENCY_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private UIUtils() {
        // Utility class
    }

    /**
     * Tạo JTable chuẩn phong cách hiện đại với chiều cao dòng 36px và không cho sửa ô trực tiếp.
     */
    public static JTable createReadonlyTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(HustTheme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(HustTheme.TEXT_MUTED);
        table.setFont(HustTheme.FONT_BODY);

        return table;
    }

    /**
     * Căn giữa các cột được chỉ định theo chỉ số.
     */
    public static void centerAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int colIndex : columnIndices) {
            if (colIndex >= 0 && colIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(colIndex).setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Căn phải các cột được chỉ định (thường dùng cho số lượng, tiền tệ).
     */
    public static void rightAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int colIndex : columnIndices) {
            if (colIndex >= 0 && colIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(colIndex).setCellRenderer(rightRenderer);
            }
        }
    }

    /**
     * Đặt Renderer hiển thị dạng Huy hiệu viên thuốc (Status Pill) cho cột trạng thái.
     */
    public static void setStatusPillRenderer(JTable table, int columnIndex) {
        if (columnIndex >= 0 && columnIndex < table.getColumnCount()) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(new StatusPillRenderer());
        }
    }

    /**
     * Cấu hình độ rộng ưu tiên cho một cột cụ thể.
     */
    public static void setColumnWidth(JTable table, int columnIndex, int width) {
        if (columnIndex >= 0 && columnIndex < table.getColumnCount()) {
            TableColumn column = table.getColumnModel().getColumn(columnIndex);
            column.setPreferredWidth(width);
        }
    }

    /**
     * Tạo nút hành động chính (Primary Button) mang sắc Đỏ Bách Khoa HUST (#B91C1C).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(HustTheme.FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(HustTheme.PRIMARY_RED);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HustTheme.PRIMARY_RED_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(HustTheme.PRIMARY_RED);
            }
        });

        return btn;
    }

    /**
     * Tạo nút phụ (Secondary Button) màu xám nhẹ, viền thanh mảnh.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(HustTheme.FONT_BODY);
        btn.setForeground(HustTheme.TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    /**
     * Tạo Card thống kê chỉ số KPI hiện đại có viền màu Bách Khoa nổi bật.
     */
    public static JPanel createStatCard(String title, String value, Color accentColor, String iconText) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HustTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        card.setBackground(Color.WHITE);

        // Header của Card: Icon + Tiêu đề
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(HustTheme.FONT_SMALL);
        lblTitle.setForeground(HustTheme.TEXT_MUTED);

        if (iconText != null && !iconText.isEmpty()) {
            JLabel lblIcon = new JLabel(iconText);
            lblIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            lblIcon.setForeground(accentColor);
            top.add(lblIcon, BorderLayout.WEST);
        }
        top.add(lblTitle, BorderLayout.CENTER);

        // Số liệu to bản ở giữa
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(HustTheme.FONT_STAT_NUMBER);
        lblValue.setForeground(HustTheme.TEXT_PRIMARY);

        // Đường chỉ thị màu dưới đáy card
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(10, 3));
        accentBar.setBackground(accentColor);

        card.add(top, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(accentBar, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Tạo Header Section Panel mang nhận diện Bách Khoa.
     */
    public static JPanel createHeaderPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(HustTheme.FONT_HEADER);
        lblTitle.setForeground(HustTheme.PRIMARY_RED);

        panel.add(lblTitle, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSubtitle = new JLabel(subtitle);
            lblSubtitle.setFont(HustTheme.FONT_SMALL);
            lblSubtitle.setForeground(HustTheme.TEXT_MUTED);
            panel.add(lblSubtitle, BorderLayout.SOUTH);
        }

        return panel;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "HUST Lib — Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "HUST Lib — Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "HUST Lib — Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        return CURRENCY_FORMAT.format(amount) + " VNĐ";
    }

    /**
     * Tạo biểu tượng logo nhận diện HUST Lib dùng làm icon cửa sổ ứng dụng.
     */
    public static Image createLogoIcon() {
        int size = 64;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền đỏ Bách Khoa bo góc
        g2.setColor(HustTheme.PRIMARY_RED);
        g2.fillRoundRect(0, 0, size, size, 16, 16);

        // Bánh răng vàng
        g2.setColor(HustTheme.ACCENT_GOLD);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(size / 2 - 12, size / 2 - 12, 24, 24);

        // Cuốn sách mở trắng
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(size / 2 - 16, size / 2 - 10, 32, 20, 6, 6);

        g2.dispose();
        return img;
    }

    /**
     * Renderer tùy biến vẽ Huy hiệu viên thuốc (Status Pill) bo tròn mềm mại cho JTable.
     */
    public static class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String text = value != null ? value.toString() : "";
            JLabel label = new JLabel(text, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bg = HustTheme.PILL_AVAILABLE_BG;
                    Color fg = HustTheme.PILL_AVAILABLE_FG;

                    String upper = text.toUpperCase();
                    if (upper.contains("CÒN SÁCH") || upper.contains("ACTIVE") || upper.contains("PAID")) {
                        bg = HustTheme.PILL_AVAILABLE_BG;
                        fg = HustTheme.PILL_AVAILABLE_FG;
                    } else if (upper.contains("ĐANG MƯỢN") || upper.contains("BORROWED")) {
                        bg = HustTheme.PILL_BORROWED_BG;
                        fg = HustTheme.PILL_BORROWED_FG;
                    } else if (upper.contains("HẾT") || upper.contains("LOCKED") || upper.contains("OVERDUE") || upper.contains("UNPAID")) {
                        bg = HustTheme.PILL_OVERDUE_BG;
                        fg = HustTheme.PILL_OVERDUE_FG;
                    } else if (upper.contains("ADMIN")) {
                        bg = HustTheme.PILL_ROLE_ADMIN_BG;
                        fg = HustTheme.PILL_ROLE_ADMIN_FG;
                    }

                    setForeground(fg);

                    // Vẽ nền viên thuốc bo tròn
                    g2.setColor(bg);
                    int arc = getHeight() - 8;
                    int x = 6;
                    int y = 4;
                    int w = getWidth() - 12;
                    int h = getHeight() - 8;
                    g2.fillRoundRect(x, y, w, h, arc, arc);

                    // Viền viên thuốc
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60));
                    g2.drawRoundRect(x, y, w, h, arc, arc);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            label.setFont(HustTheme.FONT_SMALL.deriveFont(Font.BOLD));
            label.setOpaque(false);
            return label;
        }
    }
}
