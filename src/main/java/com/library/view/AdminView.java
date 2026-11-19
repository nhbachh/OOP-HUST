package com.library.view;

import com.library.model.*;
import com.library.service.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện Console dành riêng cho Quản trị viên (Role: ADMIN).
 * Kế thừa toàn bộ quyền hạn từ LibrarianView (Inheritance trong View Layer)
 * và mở rộng các chức năng quản trị cấp cao.
 */
public class AdminView extends LibrarianView {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    public AdminView(Staff currentAdmin, BookService bookService,
                     CategoryService categoryService, BorrowService borrowService,
                     FineService fineService, UserService userService) {
        super(currentAdmin, bookService, categoryService, borrowService, fineService, userService);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("TRUNG TÂM ĐIỀU HÀNH QUẢN TRỊ VIÊN (ADMIN) — " + currentStaff.getFullName().toUpperCase());
            System.out.println("  1. Quản lý đầu sách trong thư viện");
            System.out.println("  2. Quản lý danh mục thể loại sách");
            System.out.println("  3. Nghiệp vụ Mượn sách (Borrow)");
            System.out.println("  4. Nghiệp vụ Trả sách (Return)");
            System.out.println("  5. Quản lý vé phạt & Thu tiền phạt");
            System.out.println("  6. Quản trị Tài khoản người dùng (Xem danh sách, Thêm NV, Khóa/Mở thẻ)");
            System.out.println("  7. Báo cáo thống kê toàn diện hệ thống thư viện");
            System.out.println("  8. Đổi mật khẩu tài khoản");
            System.out.println("  0. Đăng xuất");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(">> Chọn chức năng quản trị [0-8]: ", 0, 8);
            switch (choice) {
                case 1 -> manageBooks();
                case 2 -> manageCategories();
                case 3 -> handleBorrowBook();
                case 4 -> handleReturnBook();
                case 5 -> manageFines();
                case 6 -> manageUserAccounts();
                case 7 -> viewSystemStatistics();
                case 8 -> handleChangePassword();
                case 0 -> {
                    ConsoleUtils.printInfo("Đã đăng xuất tài khoản quản trị viên.");
                    running = false;
                }
            }
        }
    }

    // 6. Quản trị tài khoản
    private void manageUserAccounts() {
        ConsoleUtils.printSubHeader("QUẢN TRỊ TÀI KHOẢN NGƯỜI DÙNG (USERS)");
        System.out.println("  1. Xem danh sách toàn bộ người dùng (Độc giả & Nhân viên)");
        System.out.println("  2. Xem danh sách Nhân viên & Quản trị viên");
        System.out.println("  3. Thêm mới tài khoản Nhân viên / Thủ thư");
        System.out.println("  4. Thêm mới Thẻ Độc giả");
        System.out.println("  5. Khóa / Mở khóa tài khoản người dùng (Toggle Status)");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Lựa chọn: ", 0, 5);
        switch (choice) {
            case 1 -> TableFormatter.printStaffsTable(userService.getAllStaffs());
            case 2 -> TableFormatter.printStaffsTable(userService.getAllStaffs());
            case 3 -> addNewStaffAccount();
            case 4 -> addNewMemberAccount();
            case 5 -> handleToggleStatus();
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void addNewStaffAccount() {
        ConsoleUtils.printSubHeader("THÊM MỚI TÀI KHOẢN NHÂN VIÊN THƯ VIỆN");
        String code = ConsoleUtils.readString("Nhập mã nhân viên (VD: LIB002): ", false);
        String username = ConsoleUtils.readString("Nhập username đăng nhập: ", false);
        String pass = ConsoleUtils.readString("Nhập mật khẩu: ", false);
        String fullName = ConsoleUtils.readString("Nhập họ và tên: ", false);
        String email = ConsoleUtils.readString("Nhập email: ", true);
        String phone = ConsoleUtils.readString("Nhập số điện thoại: ", true);

        System.out.println("Chọn vai trò:");
        System.out.println("  1. LIBRARIAN (Thủ thư)");
        System.out.println("  2. ADMIN (Quản trị viên)");
        int rChoice = ConsoleUtils.readInt(">> Chọn vai trò [1-2]: ", 1, 2);
        Role role = (rChoice == 2) ? Role.ADMIN : Role.LIBRARIAN;

        String dept = ConsoleUtils.readString("Nhập phòng ban công tác: ", true);

        try {
            Staff newStaff = userService.createStaff(code, username, pass, fullName, email, phone, role, dept);
            ConsoleUtils.printSuccess(String.format("Tạo tài khoản %s thành công! ID: %d, Họ tên: %s",
                    role.getDescription(), newStaff.getUserId(), newStaff.getFullName()));
        } catch (Exception e) {
            ConsoleUtils.printError("Tạo nhân viên thất bại: " + e.getMessage());
        }
    }

    private void addNewMemberAccount() {
        ConsoleUtils.printSubHeader("THÊM MỚI THẺ ĐỘC GIẢ");
        String code = ConsoleUtils.readString("Nhập mã thẻ độc giả (VD: MBR005): ", false);
        String fullName = ConsoleUtils.readString("Nhập họ và tên độc giả: ", false);
        String email = ConsoleUtils.readString("Nhập email: ", true);
        String phone = ConsoleUtils.readString("Nhập số điện thoại: ", true);

        try {
            Member member = userService.createMember(code, fullName, email, phone);
            ConsoleUtils.printSuccess(String.format("Đăng ký thẻ độc giả thành công! ID: %d, Mã thẻ: %s, Họ tên: %s",
                    member.getUserId(), member.getUserCode(), member.getFullName()));
        } catch (Exception e) {
            ConsoleUtils.printError("Đăng ký độc giả thất bại: " + e.getMessage());
        }
    }

    private void handleToggleStatus() {
        ConsoleUtils.printSubHeader("KHÓA / MỞ KHÓA TÀI KHOẢN NGƯỜI DÙNG");
        int userId = ConsoleUtils.readInt("Nhập ID người dùng cần chuyển đổi trạng thái: ", 1, 99999);

        try {
            User userBefore = userService.getUserById(userId);
            boolean success = userService.toggleUserStatus(userId);
            if (success) {
                User userAfter = userService.getUserById(userId);
                ConsoleUtils.printSuccess(String.format("Chuyển trạng thái người dùng '%s' từ [%s] sang [%s] thành công!",
                        userAfter.getFullName(), userBefore.getStatus().getDescription(), userAfter.getStatus().getDescription()));
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Không thể chuyển đổi trạng thái: " + e.getMessage());
        }
    }

    // 7. Báo cáo thống kê toàn diện
    private void viewSystemStatistics() {
        ConsoleUtils.printSubHeader("BÁO CÁO THỐNG KÊ TỔNG HỢP HỆ THỐNG THƯ VIỆN");

        List<Book> books = bookService.getAllBooks();
        int totalBooks = books.size();
        int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
        int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
        int borrowedCopies = totalCopies - availableCopies;

        List<Member> members = userService.getAllMembers();
        List<Staff> staffs = userService.getAllStaffs();
        List<BorrowRecord> borrowRecords = borrowService.getAllBorrowRecords();
        long activeBorrows = borrowRecords.stream().filter(r -> r.getStatus() == BorrowStatus.BORROWED).count();
        long overdueBorrows = borrowRecords.stream().filter(r -> r.getStatus() == BorrowStatus.OVERDUE).count();
        long returnedBorrows = borrowRecords.stream().filter(r -> r.getStatus() == BorrowStatus.RETURNED).count();

        List<Fine> unpaidFines = fineService.getUnpaidFines();
        BigDecimal totalUnpaidAmount = unpaidFines.stream()
                .map(Fine::getFineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("  1. THỐNG KÊ TÀI NGUYÊN SÁCH:");
        System.out.printf("     - Tổng số đầu sách:              %d đầu sách%n", totalBooks);
        System.out.printf("     - Tổng số bản sách vật lý:        %d cuốn%n", totalCopies);
        System.out.printf("     - Số lượng sách hiện đang có sẵn: %d cuốn (%.1f%%)%n",
                availableCopies, totalCopies > 0 ? (availableCopies * 100.0 / totalCopies) : 0);
        System.out.printf("     - Số lượng sách đang được mượn:   %d cuốn (%.1f%%)%n",
                borrowedCopies, totalCopies > 0 ? (borrowedCopies * 100.0 / totalCopies) : 0);

        System.out.println("\n  2. THỐNG KÊ NGƯỜI DÙNG & TƯ CÁCH THÀNH VIÊN:");
        System.out.printf("     - Tổng số độc giả đăng ký:        %d người%n", members.size());
        System.out.printf("     - Tổng số nhân viên / thủ thư:    %d người%n", staffs.size());

        System.out.println("\n  3. THỐNG KÊ HOẠT ĐỘNG MƯỢN TRẢ:");
        System.out.printf("     - Tổng số lượt phiếu mượn:        %d phiếu%n", borrowRecords.size());
        System.out.printf("     - Số phiếu đang mượn (đúng hạn):  %d phiếu%n", activeBorrows);
        System.out.printf("     - Số phiếu đang quá hạn (cảnh báo): %d phiếu%n", overdueBorrows);
        System.out.printf("     - Số phiếu đã hoàn trả an toàn:   %d phiếu%n", returnedBorrows);

        System.out.println("\n  4. THỐNG KÊ TÀI CHÍNH & VÉ PHẠT:");
        System.out.printf("     - Số vé phạt chưa thanh toán:     %d vé%n", unpaidFines.size());
        System.out.printf("     - Tổng số tiền phạt còn nợ:       %s VNĐ%n", CURRENCY_FORMAT.format(totalUnpaidAmount));

        ConsoleUtils.pressEnterToContinue();
    }
}
