package com.library.view;

import com.library.model.*;
import com.library.service.*;

import java.util.List;

/**
 * Giao diện Console dành riêng cho Độc giả (Role: MEMBER).
 */
public class MemberView {

    private final Member currentMember;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;

    public MemberView(Member currentMember, BookService bookService,
                      CategoryService categoryService, BorrowService borrowService,
                      FineService fineService, UserService userService) {
        this.currentMember = currentMember;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("KHÔNG GIAN ĐỘC GIẢ — " + currentMember.getFullName().toUpperCase());
            System.out.println("  1. Tra cứu danh mục sách thư viện");
            System.out.println("  2. Xem sách đang mượn & Hạn trả");
            System.out.println("  3. Xem toàn bộ lịch sử mượn / trả");
            System.out.println("  4. Tra cứu vé phạt quá hạn & Hướng dẫn nộp phạt");
            System.out.println("  5. Thông tin tài khoản & Hạn mức mượn còn lại");
            System.out.println("  6. Đổi mật khẩu tài khoản");
            System.out.println("  0. Đăng xuất");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(">> Vui lòng chọn chức năng [0-6]: ", 0, 6);
            switch (choice) {
                case 1 -> searchBooks();
                case 2 -> viewCurrentBorrowedBooks();
                case 3 -> viewBorrowHistory();
                case 4 -> viewFines();
                case 5 -> viewAccountInfo();
                case 6 -> handleChangePassword();
                case 0 -> {
                    ConsoleUtils.printInfo("Đã đăng xuất tài khoản độc giả.");
                    running = false;
                }
            }
        }
    }

    private void searchBooks() {
        ConsoleUtils.printSubHeader("TRA CỨU DANH MỤC SÁCH THƯ VIỆN");
        System.out.println("  1. Xem tất cả sách trong thư viện");
        System.out.println("  2. Tìm kiếm sách theo từ khóa tiêu đề");
        System.out.println("  3. Lọc sách theo thể loại");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Chọn cách thức tìm kiếm: ", 0, 3);
        switch (choice) {
            case 1 -> TableFormatter.printBooksTable(bookService.getAllBooks(), categoryService);
            case 2 -> {
                String keyword = ConsoleUtils.readString("Nhập từ khóa cần tìm: ", false);
                List<Book> results = bookService.searchBooks(keyword);
                TableFormatter.printBooksTable(results, categoryService);
            }
            case 3 -> {
                List<Category> categories = categoryService.getAllCategories();
                TableFormatter.printCategoriesTable(categories);
                int catId = ConsoleUtils.readInt("Nhập ID thể loại muốn lọc: ", 1, 99999);
                List<Book> results = bookService.getBooksByCategory(catId);
                TableFormatter.printBooksTable(results, categoryService);
            }
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewCurrentBorrowedBooks() {
        ConsoleUtils.printSubHeader("DANH SÁCH SÁCH BẠN ĐANG MƯỢN");
        List<BorrowRecord> allRecords = borrowService.getBorrowRecordsByUserId(currentMember.getUserId());
        List<BorrowRecord> activeBorrows = allRecords.stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                .toList();

        if (activeBorrows.isEmpty()) {
            ConsoleUtils.printSuccess("Bạn hiện không giữ cuốn sách nào của thư viện.");
        } else {
            TableFormatter.printBorrowRecordsTable(activeBorrows, bookService, userService);
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewBorrowHistory() {
        ConsoleUtils.printSubHeader("TOÀN BỘ LỊCH SỬ MƯỢN / TRẢ CỦA BẠN");
        List<BorrowRecord> records = borrowService.getBorrowRecordsByUserId(currentMember.getUserId());
        TableFormatter.printBorrowRecordsTable(records, bookService, userService);
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewFines() {
        ConsoleUtils.printSubHeader("TRA CỨU VÉ PHẠT QUÁ HẠN");
        List<Fine> fines = fineService.getFinesByUserId(currentMember.getUserId());
        TableFormatter.printFinesTable(fines, userService);

        boolean hasUnpaid = fines.stream().anyMatch(f -> f.getStatus() == FineStatus.UNPAID);
        if (hasUnpaid) {
            ConsoleUtils.printWarning("Bạn đang có khoản phạt CHƯA ĐÓNG. Vui lòng gặp Thủ thư tại quầy để thanh toán trước khi mượn thêm sách!");
        } else {
            ConsoleUtils.printSuccess("Bạn không có khoản phạt nợ nào chưa thanh toán.");
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewAccountInfo() {
        ConsoleUtils.printSubHeader("THÔNG TIN TÀI KHOẢN ĐỘC GIẢ");
        // Lấy lại thông tin mới nhất từ DB
        User updated = userService.getUserById(currentMember.getUserId());
        if (updated instanceof Member m) {
            System.out.printf("  * Mã thẻ bạn đọc:      %s%n", m.getUserCode());
            System.out.printf("  * Họ và tên độc giả:   %s%n", m.getFullName());
            System.out.printf("  * Email liên hệ:       %s%n", m.getEmail() != null ? m.getEmail() : "(Chưa có)");
            System.out.printf("  * Số điện thoại:       %s%n", m.getPhone() != null ? m.getPhone() : "(Chưa có)");
            System.out.printf("  * Trạng thái thẻ:      %s%n", m.getStatus().getDescription());
            System.out.printf("  * Số sách đang giữ:    %d cuốn%n", m.getCurrentBorrowCount());
            System.out.printf("  * Hạn mức mượn tối đa: %d cuốn%n", m.getMaxBorrowLimit());
            System.out.printf("  * Số sách được mượn:   %d cuốn còn lại%n", Math.max(0, m.getMaxBorrowLimit() - m.getCurrentBorrowCount()));
            System.out.printf("  * Quyền mượn sách:     %s%n", m.canBorrow() ? "ĐỦ ĐIỀU KIỆN" : "BỊ CHẶN (Vui lòng trả sách hoặc đóng phạt)");
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void handleChangePassword() {
        ConsoleUtils.printSubHeader("ĐỔI MẬT KHẨU TÀI KHOẢN ĐỘC GIẢ");
        String oldPass = ConsoleUtils.readString("Nhập mật khẩu hiện tại: ", false);
        String newPass = ConsoleUtils.readString("Nhập mật khẩu mới (tối thiểu 6 ký tự): ", false);
        if (newPass.length() < 6) {
            ConsoleUtils.printError("Mật khẩu mới phải có tối thiểu 6 ký tự.");
            ConsoleUtils.pressEnterToContinue();
            return;
        }
        String confirmPass = ConsoleUtils.readString("Xác nhận lại mật khẩu mới: ", false);
        if (!newPass.equals(confirmPass)) {
            ConsoleUtils.printError("Mật khẩu mới và mật khẩu xác nhận không trùng khớp!");
            ConsoleUtils.pressEnterToContinue();
            return;
        }
        try {
            boolean ok = userService.changePassword(currentMember.getUserId(), oldPass, newPass);
            if (ok) {
                ConsoleUtils.printSuccess("Đổi mật khẩu thành công! Vui lòng ghi nhớ mật khẩu mới của bạn.");
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Lỗi đổi mật khẩu: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }
}
