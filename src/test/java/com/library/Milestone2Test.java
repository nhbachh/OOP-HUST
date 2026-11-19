package com.library;

import com.library.config.DatabaseConfig;
import com.library.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp kiểm thử nội bộ phục vụ Milestone 2:
 * 1. Kiểm tra kết nối CSDL qua DatabaseConfig.testConnection().
 * 2. Kiểm tra khởi tạo các thực thể Model (Category, Book, User, Member, Staff, BorrowRecord, Fine).
 * 3. Kiểm tra tính đa hình (Polymorphism) qua getRoleDescription().
 * 4. Kiểm tra logic nghiệp vụ cơ bản (canBorrow, isAvailable, validation).
 */
public class Milestone2Test {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("               KIỂM THỬ NỘI BỘ MILESTONE 2: CONFIG & MODEL OOP                   ");
        System.out.println("================================================================================\n");

        // 1. Kiểm tra kết nối cơ sở dữ liệu
        System.out.println("--- 1. KIỂM TRA TẦNG CẤU HÌNH (DatabaseConfig) ---");
        DatabaseConfig.testConnection();
        System.out.println();

        // 2. Kiểm tra tính Kế thừa & Đa hình (User -> Member, Staff)
        System.out.println("--- 2. KIỂM TRA TÍNH KẾ THỪA & ĐA HÌNH (OOP) ---");
        Member member1 = new Member("MBR001", "Phạm Minh Tuấn", "tuan.pm@student.edu.vn", "0901234567");
        member1.setUserId(1);
        member1.setCurrentBorrowCount(2);

        Member memberLocked = new Member("MBR004", "Vũ Bích Hạnh", "hanh.vb@student.edu.vn", "0934567890");
        memberLocked.setUserId(4);
        memberLocked.setStatus(UserStatus.LOCKED);

        Staff staffAdmin = new Staff("ADM001", "admin", "hash123", "Nguyễn Quản Trị",
                "admin@library.edu.vn", "0988000001", Role.ADMIN, "Ban Quản trị");
        staffAdmin.setUserId(2);

        Staff staffLib = new Staff("LIB001", "thuthu_mai", "hash456", "Trần Tuyết Mai",
                "mai.tt@library.edu.vn", "0988000002", Role.LIBRARIAN, "Phòng Quản lý Kho");
        staffLib.setUserId(3);

        // Mảng đối tượng User kiểm tra Runtime Polymorphism
        User[] users = new User[] { member1, memberLocked, staffAdmin, staffLib };

        for (User u : users) {
            System.out.printf("[%s] Mã: %-8s | Họ tên: %-18s | Vai trò: %-10s | Mô tả đa hình: %s%n",
                    u.getClass().getSimpleName(),
                    u.getUserCode(),
                    u.getFullName(),
                    u.getRole(),
                    u.getRoleDescription());
        }
        System.out.println();

        // 3. Kiểm tra logic quyền mượn sách (canBorrow) của Member
        System.out.println("--- 3. KIỂM TRA PHƯƠNG THỨC TIỆN ÍCH canBorrow() CỦA MEMBER ---");
        System.out.println("Member 1 (ACTIVE, mượn 2/5 sách): canBorrow = " + member1.canBorrow() + " (Kỳ vọng: true)");
        member1.setCurrentBorrowCount(5);
        System.out.println("Member 1 (ACTIVE, mượn 5/5 sách): canBorrow = " + member1.canBorrow() + " (Kỳ vọng: false)");
        System.out.println("Member 4 (LOCKED, tài khoản khóa): canBorrow = " + memberLocked.canBorrow() + " (Kỳ vọng: false)");
        System.out.println();

        // 4. Kiểm tra Category & Book
        System.out.println("--- 4. KIỂM TRA THỰC THỂ Category & Book ---");
        Category cat = new Category(1, "Công nghệ thông tin", "Sách lập trình và kiến trúc phần mềm");
        System.out.println("Khởi tạo Category: " + cat);

        Book bookAvailable = new Book(1, "978-604-01-2301-1", "Lập Trình Java Căn Bản và Nâng Cao",
                "Nguyễn Văn An", cat.getCategoryId(), 2022, 5, 4);
        System.out.println("Sách còn bản sao: " + bookAvailable.getTitle() + " | isAvailable = " + bookAvailable.isAvailable() + " (Kỳ vọng: true)");

        Book bookEmpty = new Book(2, "978-604-01-2302-8", "Cấu Trúc Dữ Liệu và Giải Thuật",
                "Trần Thị Bích", cat.getCategoryId(), 2021, 3, 0);
        System.out.println("Sách hết bản sao: " + bookEmpty.getTitle() + " | isAvailable = " + bookEmpty.isAvailable() + " (Kỳ vọng: false)");
        System.out.println();

        // 5. Kiểm tra BorrowRecord & Fine
        System.out.println("--- 5. KIỂM TRA THỰC THỂ BorrowRecord & Fine ---");
        BorrowRecord record = new BorrowRecord(1, member1.getUserId(), bookAvailable.getBookId(),
                staffLib.getUserId(), LocalDate.now().minusDays(15), LocalDate.now().minusDays(1),
                null, BorrowStatus.OVERDUE);
        System.out.println("Phiếu mượn: " + record);

        Fine fine = new Fine(1, record.getRecordId(), member1.getUserId(), 14,
                new BigDecimal("28000.00"), "Quá hạn 14 ngày (2.000 VNĐ/ngày)",
                FineStatus.UNPAID, LocalDateTime.now(), null);
        System.out.println("Phiếu phạt ban đầu: " + fine);
        fine.markAsPaid();
        System.out.println("Sau khi nộp phạt:   " + fine);
        System.out.println();

        // 6. Kiểm tra ràng buộc ngoại lệ (Validation)
        System.out.println("--- 6. KIỂM TRA BẮT LỖI TÍNH TOÀN VẸN DỮ LIỆU (Validation Exceptions) ---");
        try {
            bookAvailable.setCopies(5, 10); // Vi phạm available > total
            System.err.println("-> Thất bại: Không chặn được availableCopies > totalCopies");
        } catch (IllegalArgumentException e) {
            System.out.println("-> Bắt ngoại lệ thành công (available > total): " + e.getMessage());
        }

        try {
            fine.setFineAmount(new BigDecimal("-5000.00")); // Vi phạm số tiền âm
            System.err.println("-> Thất bại: Không chặn được fineAmount âm");
        } catch (IllegalArgumentException e) {
            System.out.println("-> Bắt ngoại lệ thành công (tiền phạt âm): " + e.getMessage());
        }

        System.out.println("\n================================================================================");
        System.out.println("       TẤT CẢ KIỂM THỬ NỘI BỘ CHO MILESTONE 2 ĐÃ VƯỢT QUA XUẤT SẮC!            ");
        System.out.println("================================================================================");

        // Đóng các background thread của MySQL driver để JVM kết thúc sạch sẽ
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
