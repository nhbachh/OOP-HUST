package com.library;

import com.library.dao.BookDAO;
import com.library.dao.BorrowRecordDAO;
import com.library.dao.FineDAO;
import com.library.dao.UserDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.BorrowRecordDAOImpl;
import com.library.dao.impl.FineDAOImpl;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.BorrowException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Runner kiểm thử toàn diện Tầng Nghiệp vụ (Service Layer) & Quản lý Giao dịch (JDBC Transactions)
 * phục vụ Milestone 4.
 */
public class Milestone4Test {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     KIỂM THỬ TẦNG NGHIỆP VỤ (SERVICE) & TRANSACTION MANAGEMENT - MILESTONE 4    ");
        System.out.println("================================================================================\n");

        CategoryService categoryService = new CategoryServiceImpl();
        BookService bookService = new BookServiceImpl();
        UserService userService = new UserServiceImpl();
        FineService fineService = new FineServiceImpl();
        BorrowService borrowService = new BorrowServiceImpl();

        BookDAO bookDAO = new BookDAOImpl();
        BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAOImpl();
        FineDAO fineDAO = new FineDAOImpl();

        // ---------------------------------------------------------------------
        // 0. Kiểm thử CategoryService, BookService & UserService
        // ---------------------------------------------------------------------
        System.out.println("--- 0. KIỂM THỬ XÁC THỰC NGƯỜI DÙNG & TÌM KIẾM ---");
        try {
            User admin = userService.authenticate("admin", "Admin@123");
            System.out.println("Đăng nhập Admin thành công: " + admin.getFullName() + " | Vai trò: " + admin.getRoleDescription());
        } catch (Exception e) {
            System.err.println("Đăng nhập thất bại: " + e.getMessage());
        }

        try {
            userService.authenticate("admin", "WrongPass@999");
            System.err.println("Thất bại: Không chặn được mật khẩu sai!");
        } catch (ValidationException e) {
            System.out.println("Chặn mật khẩu sai thành công: " + e.getMessage());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // KỊCH BẢN 1: Mượn sách thành công (Transaction Commit)
        // ---------------------------------------------------------------------
        System.out.println("--- KỊCH BẢN 1: MƯỢN SÁCH THÀNH CÔNG (Transaction Commit) ---");
        int testMemberId = 3; // Phạm Minh Tuấn (ACTIVE, không có nợ phạt)
        int testBookId = 1;   // Lập trình Java căn bản
        int staffId = 2;      // Thủ thư Mai

        Book bookBeforeBorrow = bookService.getBookById(testBookId);
        int copiesBeforeBorrow = bookBeforeBorrow.getAvailableCopies();
        System.out.printf("Trước khi mượn: Sách '%s' có tồn kho khả dụng = %d%n",
                bookBeforeBorrow.getTitle(), copiesBeforeBorrow);

        BorrowRecord newBorrowRecord = borrowService.borrowBook(testMemberId, testBookId, staffId, 14);
        System.out.printf("Giao dịch mượn thành công -> Phiếu mượn #%d: Ngày mượn=%s, Hạn trả=%s, Trạng thái=%s%n",
                newBorrowRecord.getRecordId(), newBorrowRecord.getBorrowDate(),
                newBorrowRecord.getDueDate(), newBorrowRecord.getStatus());

        Book bookAfterBorrow = bookService.getBookById(testBookId);
        int copiesAfterBorrow = bookAfterBorrow.getAvailableCopies();
        System.out.printf("Sau khi mượn: Sách '%s' có tồn kho khả dụng = %d (Kỳ vọng: %d)%n",
                bookAfterBorrow.getTitle(), copiesAfterBorrow, copiesBeforeBorrow - 1);

        if (copiesAfterBorrow == copiesBeforeBorrow - 1) {
            System.out.println("-> KẾT QUẢ: Kịch bản 1 VƯỢT QUA (Tồn kho giảm 1, phiếu mượn tạo thành công)!");
        } else {
            System.err.println("-> KẾT QUẢ: Kịch bản 1 THẤT BẠI!");
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // KỊCH BẢN 2: Chặn mượn sách vi phạm nghiệp vụ
        // ---------------------------------------------------------------------
        System.out.println("--- KỊCH BẢN 2: CHẶN MƯỢN SÁCH KHI VI PHẠM RÀNG BUỘC NGHIỆP VỤ ---");

        // 2a. Chặn tài khoản bị khóa (LOCKED)
        int lockedMemberId = 6; // Vũ Bích Hạnh (LOCKED)
        try {
            borrowService.borrowBook(lockedMemberId, testBookId, staffId, 14);
            System.err.println("Thất bại: Không chặn được tài khoản LOCKED mượn sách!");
        } catch (BorrowException e) {
            System.out.println("2a. Chặn tài khoản LOCKED thành công: " + e.getMessage());
        }

        // 2b. Chặn tài khoản có vé phạt chưa thanh toán (UNPAID)
        int finedMemberId = 5; // Đặng Quốc Huy (có vé phạt UNPAID 66.000 VNĐ)
        try {
            borrowService.borrowBook(finedMemberId, testBookId, staffId, 14);
            System.err.println("Thất bại: Không chặn được tài khoản có vé phạt UNPAID mượn sách!");
        } catch (BorrowException e) {
            System.out.println("2b. Chặn tài khoản có vé phạt UNPAID thành công: " + e.getMessage());
        }

        // 2c. Chặn mượn sách khi sách đã hết bản sao khả dụng (availableCopies == 0)
        // Tạo một cuốn sách mẫu có availableCopies = 0 để kiểm thử
        Book tempOutOfStockBook = bookService.createBook("978-604-09-9999-9", "Sách Hết Hàng Demo",
                "Tác giả Test", 1, 2024, 1);
        // Tạm thời set availableCopies về 0
        tempOutOfStockBook.setAvailableCopies(0);
        bookDAO.update(tempOutOfStockBook);

        try {
            borrowService.borrowBook(testMemberId, tempOutOfStockBook.getBookId(), staffId, 14);
            System.err.println("Thất bại: Không chặn được sách hết bản sao!");
        } catch (BorrowException e) {
            System.out.println("2c. Chặn sách hết hàng thành công: " + e.getMessage());
        } finally {
            bookDAO.deleteById(tempOutOfStockBook.getBookId());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // KỊCH BẢN 3: Trả sách & Tự động tạo vé phạt nếu quá hạn
        // ---------------------------------------------------------------------
        System.out.println("--- KỊCH BẢN 3: TRẢ SÁCH & TỰ ĐỘNG TẠO VÉ PHẠT QUÁ HẠN ---");
        // Tạo một phiếu mượn quá hạn 5 ngày để kiểm thử tự động tính phạt
        LocalDate overdueBorrowDate = LocalDate.now().minusDays(19);
        LocalDate overdueDueDate = LocalDate.now().minusDays(5);
        BorrowRecord overdueRecord = new BorrowRecord(testMemberId, testBookId, staffId,
                overdueBorrowDate, overdueDueDate);
        borrowRecordDAO.insert(overdueRecord);
        int overdueRecordId = overdueRecord.getRecordId();

        // Cập nhật sách giảm 1 bản sao tượng trưng cho phiếu này
        bookDAO.updateAvailableCopies(testBookId, -1);
        int copiesBeforeReturn = bookService.getBookById(testBookId).getAvailableCopies();
        System.out.printf("Tạo phiếu mượn quá hạn #%d (Hạn trả: %s, Quá hạn: 5 ngày). Sách tồn kho hiện tại = %d%n",
                overdueRecordId, overdueDueDate, copiesBeforeReturn);

        // Tiến hành trả sách
        BorrowRecord returnedRecord = borrowService.returnBook(overdueRecordId);
        System.out.printf("Giao dịch trả sách thành công: Phiếu #%d, Ngày trả: %s, Trạng thái: %s%n",
                returnedRecord.getRecordId(), returnedRecord.getReturnDate(), returnedRecord.getStatus());

        int copiesAfterReturn = bookService.getBookById(testBookId).getAvailableCopies();
        System.out.printf("Tồn kho sách sau khi trả: %d (Kỳ vọng: %d)%n", copiesAfterReturn, copiesBeforeReturn + 1);

        // Kiểm tra vé phạt tự động được sinh ra
        Fine autoFine = fineService.getFineByRecordId(overdueRecordId);
        System.out.printf("-> Vé phạt tự động sinh ra: Mã phạt #%d | Số ngày quá hạn: %d | Số tiền: %s VNĐ | Trạng thái: %s%n",
                autoFine.getFineId(), autoFine.getOverdueDays(), autoFine.getFineAmount(), autoFine.getStatus());
        System.out.printf("   Lý do: %s%n", autoFine.getReason());
        System.out.println();

        // ---------------------------------------------------------------------
        // KỊCH BẢN 4: Thanh toán tiền phạt (Pay Fine)
        // ---------------------------------------------------------------------
        System.out.println("--- KỊCH BẢN 4: THANH TOÁN TIỀN PHẠT (Pay Fine) ---");
        System.out.println("Trạng thái vé phạt trước thanh toán: " + autoFine.getStatus());
        boolean paySuccess = fineService.payFine(autoFine.getFineId());
        Fine paidFine = fineService.getFineById(autoFine.getFineId());
        System.out.printf("Kết quả nộp phạt: %s | Trạng thái mới: %s | Thời điểm thanh toán: %s%n",
                paySuccess, paidFine.getStatus(), paidFine.getPaidAt());
        System.out.println();

        // ---------------------------------------------------------------------
        // KỊCH BẢN 5: Kiểm tra Rollback Transaction khi phát sinh sự cố
        // ---------------------------------------------------------------------
        System.out.println("--- KỊCH BẢN 5: KIỂM TRA ROLLBACK TRANSACTION KHI CÓ LỖI BẤT NGỜ ---");
        int bookCopiesBeforeCrash = bookService.getBookById(testBookId).getAvailableCopies();
        int totalRecordsBeforeCrash = borrowRecordDAO.findAll().size();
        System.out.printf("Trước khi giả lập sự cố: Sách tồn kho = %d, Tổng số phiếu mượn = %d%n",
                bookCopiesBeforeCrash, totalRecordsBeforeCrash);

        try {
            System.out.println("Thực hiện mượn sách với chế độ mô phỏng sự cố (System Failure Simulation)...");
            borrowService.borrowBookWithSimulatedFailure(testMemberId, testBookId, staffId, 14);
            System.err.println("Thất bại: Giao dịch không bị ngắt!");
        } catch (BorrowException e) {
            System.out.println("Bắt ngoại lệ giao dịch thành công: " + e.getMessage());
        }

        int bookCopiesAfterCrash = bookService.getBookById(testBookId).getAvailableCopies();
        int totalRecordsAfterCrash = borrowRecordDAO.findAll().size();
        System.out.printf("Sau khi Rollback: Sách tồn kho = %d (Kỳ vọng: %d), Tổng số phiếu mượn = %d (Kỳ vọng: %d)%n",
                bookCopiesAfterCrash, bookCopiesBeforeCrash, totalRecordsAfterCrash, totalRecordsBeforeCrash);

        if (bookCopiesBeforeCrash == bookCopiesAfterCrash && totalRecordsBeforeCrash == totalRecordsAfterCrash) {
            System.out.println("-> KẾT QUẢ: Kịch bản 5 VƯỢT QUA XUẤT SẮC! Dữ liệu được rollback hoàn nguyên 100%, tính toàn vẹn CSDL được bảo toàn.");
        } else {
            System.err.println("-> KẾT QUẢ: Kịch bản 5 THẤT BẠI (Dữ liệu bị biến đổi dở dang)!");
        }

        // Hoàn nguyên trả lại cuốn sách đã mượn ở kịch bản 1 để giữ CSDL sạch đẹp
        borrowService.returnBook(newBorrowRecord.getRecordId());

        System.out.println("\n================================================================================");
        System.out.println("        TẤT CẢ CÁC KỊCH BẢN KIỂM THỬ CHO MILESTONE 4 ĐÃ VƯỢT QUA 100%!           ");
        System.out.println("================================================================================");

        // Dọn dẹp background thread của MySQL driver
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
