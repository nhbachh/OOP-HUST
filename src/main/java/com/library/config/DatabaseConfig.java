package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý cấu hình và cung cấp kết nối đến cơ sở dữ liệu MySQL (library_db).
 * Áp dụng Utility Pattern với private constructor.
 */
public final class DatabaseConfig {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    // Private constructor ngăn việc khởi tạo thể hiện từ bên ngoài (Utility Pattern)
    private DatabaseConfig() {
        throw new UnsupportedOperationException("DatabaseConfig là lớp tiện ích (Utility Class), không thể khởi tạo thể hiện.");
    }

    /**
     * Lấy URL kết nối CSDL, ưu tiên biến môi trường DB_URL nếu có.
     */
    public static String getUrl() {
        String envUrl = System.getenv("DB_URL");
        return (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl : DEFAULT_URL;
    }

    /**
     * Lấy tên người dùng CSDL, ưu tiên biến môi trường DB_USER nếu có.
     */
    public static String getUser() {
        String envUser = System.getenv("DB_USER");
        return (envUser != null && !envUser.trim().isEmpty()) ? envUser : DEFAULT_USER;
    }

    /**
     * Lấy mật khẩu CSDL, ưu tiên biến môi trường DB_PASSWORD nếu có.
     */
    public static String getPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        return envPass != null ? envPass : DEFAULT_PASSWORD;
    }

    /**
     * Mở và trả về một kết nối Connection mới đến MySQL.
     *
     * @return Connection đối tượng kết nối JDBC
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    /**
     * Kiểm tra nhanh trạng thái sẵn sàng kết nối đến CSDL MySQL.
     *
     * @return true nếu kết nối thành công và CSDL hoạt động bình thường
     */
    public static boolean isConnectionValid() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Phương thức tiện ích kiểm tra kết nối CSDL và in thông tin trạng thái ra console.
     */
    public static void testConnection() {
        System.out.println("==================================================");
        System.out.println("[DatabaseConfig] Đang kiểm tra kết nối đến MySQL...");
        System.out.println("URL:  " + getUrl());
        System.out.println("User: " + getUser());

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("-> KẾT NỐI CƠ SỞ DỮ LIỆU THÀNH CÔNG!");
                System.out.println("   Database Name:    " + conn.getCatalog());
                System.out.println("   DBMS Version:     " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("   JDBC Driver Name: " + conn.getMetaData().getDriverName());
                System.out.println("   Driver Version:   " + conn.getMetaData().getDriverVersion());
            } else {
                System.err.println("-> KẾT NỐI THẤT BẠI: Kết nối trả về null hoặc đã bị đóng.");
            }
        } catch (SQLException e) {
            System.err.println("-> KẾT NỐI THẤT BẠI! Lỗi: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
            System.err.println("   ErrorCode: " + e.getErrorCode());
        }
        System.out.println("==================================================");
    }

    /**
     * Điểm chạy độc lập để kiểm tra kết nối CSDL trực tiếp từ IDE hoặc dòng lệnh.
     */
    public static void main(String[] args) {
        testConnection();
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
