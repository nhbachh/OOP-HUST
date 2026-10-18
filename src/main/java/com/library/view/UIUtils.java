package com.library.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Lớp tiện ích giao diện dùng chung (UI Utilities) cho ứng dụng Swing.
 * Cung cấp các hàm tạo bảng chuẩn, hiển thị thông báo, định dạng tiền tệ và styling components.
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
     * Tạo JTable chuẩn ở chế độ chỉ đọc (Read-only), tùy chỉnh giao diện phẳng FlatLaf.
     *
     * @param model DefaultTableModel chứa dữ liệu
     * @return JTable đã được cấu hình tối ưu
     */
    public static JTable createReadonlyTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 13f));

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
     * Cấu hình độ rộng ưu tiên cho một cột cụ thể.
     */
    public static void setColumnWidth(JTable table, int columnIndex, int width) {
        if (columnIndex >= 0 && columnIndex < table.getColumnCount()) {
            TableColumn column = table.getColumnModel().getColumn(columnIndex);
            column.setPreferredWidth(width);
        }
    }

    /**
     * Hiển thị hộp thoại thông báo thành công.
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại báo lỗi.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại cảnh báo.
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại xác nhận lựa chọn Có/Không.
     */
    public static boolean confirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Định dạng tiền tệ VNĐ (ví dụ: 50,000 VNĐ).
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VNĐ";
        }
        return CURRENCY_FORMAT.format(amount) + " VNĐ";
    }

    /**
     * Tạo JPanel tiêu đề phần (Header Section Panel) với phong cách phẳng hiện đại.
     */
    public static JPanel createHeaderPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        lblTitle.setForeground(new Color(24, 90, 157));

        panel.add(lblTitle, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSubtitle = new JLabel(subtitle);
            lblSubtitle.setFont(lblSubtitle.getFont().deriveFont(Font.PLAIN, 12f));
            lblSubtitle.setForeground(Color.GRAY);
            panel.add(lblSubtitle, BorderLayout.SOUTH);
        }

        return panel;
    }

    /**
     * Tạo card thống kê hiển thị số liệu nổi bật.
     */
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.setBackground(new Color(248, 249, 250));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.PLAIN, 12f));
        lblTitle.setForeground(Color.DARK_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(lblValue.getFont().deriveFont(Font.BOLD, 20f));
        lblValue.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }
}
