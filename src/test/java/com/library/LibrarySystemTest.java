package com.library;

import com.library.model.*;
import com.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử đơn vị tự động (Unit Tests) cho toàn bộ hệ thống Library Management System.
 * Kiểm tra các tính chất Hướng đối tượng OOP, tính toàn vẹn dữ liệu và nghiệp vụ lõi.
 */
public class LibrarySystemTest {

    @Test
    @DisplayName("Kiểm tra tính Đóng gói (Encapsulation) & Hợp lệ thuộc tính của Book")
    void testBookEncapsulationAndValidation() {
        Book book = new Book("978-604-0-12345-6", "Lập Trình Hướng Đối Tượng", "Bách Khoa", 1, 2024, 10, 10);

        assertEquals("978-604-0-12345-6", book.getIsbn());
        assertEquals("Lập Trình Hướng Đối Tượng", book.getTitle());
        assertEquals(10, book.getTotalCopies());
        assertEquals(10, book.getAvailableCopies());
        assertTrue(book.isAvailable());

        // Kiểm tra giảm tồn kho
        book.setAvailableCopies(9);
        assertEquals(9, book.getAvailableCopies());

        // Bắt ngoại lệ khi số lượng tồn vượt quá tổng số
        assertThrows(IllegalArgumentException.class, () -> book.setAvailableCopies(15));
        // Bắt ngoại lệ khi số lượng tồn âm
        assertThrows(IllegalArgumentException.class, () -> book.setAvailableCopies(-1));
    }

    @Test
    @DisplayName("Kiểm tra tính Kế thừa (Inheritance) & Đa hình (Polymorphism) của User, Staff, Member")
    void testUserHierarchyAndPolymorphism() {
        User adminStaff = new Staff("ADM001", "admin", "hash123", "Nguyễn Quản Trị",
                "admin@hust.edu.vn", "0912345678", Role.ADMIN, "Ban Quản trị");

        User memberUser = new Member("MBR001", "Trần Sinh Viên", "sinhvien@hust.edu.vn", "0987654321");

        // Đa hình thời điểm chạy (Runtime Polymorphism)
        assertTrue(adminStaff.getRoleDescription().contains("Quản trị viên"));
        assertTrue(memberUser.getRoleDescription().contains("Độc giả"));

        // Kiểm tra quan hệ kế thừa
        assertTrue(adminStaff instanceof User);
        assertTrue(memberUser instanceof User);
    }

    @Test
    @DisplayName("Kiểm tra Nghiệp vụ Hạn mức Mượn của Độc giả (Member Quota Logic)")
    void testMemberBorrowQuota() {
        Member member = new Member("MBR002", "Lê Văn Bạn Đọc", "bandoc@hust.edu.vn", "0900000000");
        assertEquals(5, member.getMaxBorrowLimit());
        assertEquals(0, member.getCurrentBorrowCount());
        assertTrue(member.canBorrow());

        // Mượn 4 cuốn -> vẫn được mượn tiếp
        member.setCurrentBorrowCount(4);
        assertTrue(member.canBorrow());

        // Đạt tối đa 5 cuốn -> chặn mượn
        member.setCurrentBorrowCount(5);
        assertFalse(member.canBorrow());

        // Thẻ bị khóa -> chặn mượn
        member.setCurrentBorrowCount(2);
        member.setStatus(UserStatus.LOCKED);
        assertFalse(member.canBorrow());
    }

    @Test
    @DisplayName("Kiểm tra Thực thể Phiếu Mượn (BorrowRecord) và Ràng buộc Ngày tháng")
    void testBorrowRecordDateConstraint() {
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        BorrowRecord record = new BorrowRecord(1, 1, 1, borrowDate, dueDate);
        assertEquals(BorrowStatus.BORROWED, record.getStatus());
        assertNull(record.getReturnDate());

        // Ngày hẹn trả trước ngày mượn phải ném ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> {
            new BorrowRecord(1, 1, 1, borrowDate, borrowDate.minusDays(1));
        });
    }

    @Test
    @DisplayName("Kiểm tra Thực thể Tiền Phạt (Fine) và Chuyển trạng thái Đã Thanh Toán")
    void testFinePaymentStatus() {
        Fine fine = new Fine(1, 1, 5, new BigDecimal("10000.00"), "Quá hạn trả sách 5 ngày");
        assertEquals(FineStatus.UNPAID, fine.getStatus());
        assertNull(fine.getPaidAt());

        // Đánh dấu thanh toán
        fine.markAsPaid();
        assertEquals(FineStatus.PAID, fine.getStatus());
        assertNotNull(fine.getPaidAt());
    }

    @Test
    @DisplayName("Kiểm tra Mã hóa Mật khẩu SHA-256 an toàn và nhất quán")
    void testSha256PasswordHashing() {
        String raw = "Admin@123";
        String hash1 = UserServiceImpl.hashPassword(raw);
        String hash2 = UserServiceImpl.hashPassword(raw);

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "Độ dài băm SHA-256 phải đúng 64 ký tự hex");
        assertEquals(hash1, hash2, "Cùng một mật khẩu phải tạo ra cùng chuỗi băm SHA-256");

        String hashDifferent = UserServiceImpl.hashPassword("OtherPassword@456");
        assertNotEquals(hash1, hashDifferent);
    }
}
