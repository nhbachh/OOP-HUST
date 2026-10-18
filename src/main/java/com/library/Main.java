package com.library;

import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;
import com.library.view.*;

import java.util.List;

/**
 * Điểm khởi chạy chính của ứng dụng Quản lý Thư viện (Library Management System).
 * Áp dụng kiến trúc phân tầng: Model -> DAO -> Service -> View.
 */
public class Main {

    private final CategoryService categoryService;
    private final BookService bookService;
    private final UserService userService;
    private final FineService fineService;
    private final BorrowService borrowService;
    private final AuthView authView;

    public Main() {
        this.categoryService = new CategoryServiceImpl();
        this.bookService = new BookServiceImpl();
        this.userService = new UserServiceImpl();
        this.fineService = new FineServiceImpl();
        this.borrowService = new BorrowServiceImpl();
        this.authView = new AuthView(userService);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        boolean running = true;
        printWelcomeBanner();

        while (running) {
            try {
                ConsoleUtils.printHeader("HỆ THỐNG QUẢN LÝ THƯ VIỆN — TRANG CHỦ");
                System.out.println("  1. Đăng nhập hệ thống (Độc giả / Thủ thư / Quản trị viên)");
                System.out.println("  2. Tra cứu danh mục sách công khai");
                System.out.println("  3. Giới thiệu hệ thống & Quy định mượn trả thư viện");
                System.out.println("  0. Thoát ứng dụng");
                ConsoleUtils.printDivider();

                int choice = ConsoleUtils.readInt(">> Vui lòng chọn chức năng [0-3]: ", 0, 3);
                switch (choice) {
                    case 1 -> handleLoginFlow();
                    case 2 -> handlePublicBookSearch();
                    case 3 -> handleShowRulesAndAbout();
                    case 0 -> {
                        ConsoleUtils.printSuccess("Cảm ơn bạn đã sử dụng Hệ thống Quản lý Thư viện. Hẹn gặp lại!");
                        running = false;
                    }
                }
            } catch (Exception e) {
                ConsoleUtils.printError("Phát sinh sự cố bất ngờ: " + e.getMessage());
                ConsoleUtils.pressEnterToContinue();
            }
        }

        // Dọn dẹp background thread của JDBC driver khi thoát
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }

    private void handleLoginFlow() {
        User authenticatedUser = authView.showLogin();
        if (authenticatedUser == null) {
            return;
        }

        // Điều hướng dựa theo Vai trò (Role-based Navigation)
        switch (authenticatedUser.getRole()) {
            case ADMIN -> {
                AdminView adminView = new AdminView((Staff) authenticatedUser,
                        bookService, categoryService, borrowService, fineService, userService);
                adminView.showMenu();
            }
            case LIBRARIAN -> {
                LibrarianView librarianView = new LibrarianView((Staff) authenticatedUser,
                        bookService, categoryService, borrowService, fineService, userService);
                librarianView.showMenu();
            }
            case MEMBER -> {
                MemberView memberView = new MemberView((Member) authenticatedUser,
                        bookService, categoryService, borrowService, fineService, userService);
                memberView.showMenu();
            }
        }
    }

    private void handlePublicBookSearch() {
        ConsoleUtils.printSubHeader("TRA CỨU SÁCH CÔNG KHAI DÀNH CHO BẠN ĐỌC");
        System.out.println("  1. Xem toàn bộ đầu sách");
        System.out.println("  2. Tìm kiếm theo từ khóa tiêu đề");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Lựa chọn: ", 0, 2);
        switch (choice) {
            case 1 -> TableFormatter.printBooksTable(bookService.getAllBooks(), categoryService);
            case 2 -> {
                String kw = ConsoleUtils.readString("Nhập từ khóa tìm kiếm: ", false);
                List<Book> list = bookService.searchBooks(kw);
                TableFormatter.printBooksTable(list, categoryService);
            }
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void handleShowRulesAndAbout() {
        ConsoleUtils.printHeader("GIỚI THIỆU HỆ THỐNG & NỘI QUY THƯ VIỆN");
        System.out.println("""
          1. GIỚI THIỆU CHUNG:
             - Ứng dụng Quản lý Thư viện (Library Management System).
             - Học phần: Lập trình Hướng đối tượng (OOP).
             - Kiến trúc: 4 tầng phân lập (Model - DAO - Service - View).
             - Áp dụng đầy đủ 4 tính chất OOP: Đóng gói, Kế thừa, Đa hình, Trừu tượng.
             - Quản lý giao dịch ACID (JDBC Transactions) bảo vệ toàn vẹn dữ liệu.

          2. QUY ĐỊNH MƯỢN TRẢ SÁCH:
             - Mỗi độc giả (Member) được mượn tối đa 5 cuốn sách cùng lúc.
             - Thời hạn mượn mặc định là 14 ngày kể từ ngày mượn.
             - Nếu trả sách quá hạn, hệ thống tự động áp dụng mức phạt: 2.000 VNĐ / ngày trễ.
             - Độc giả đang nợ tiền phạt chưa thanh toán sẽ tạm thời bị đình chỉ quyền mượn sách.
             - Độc giả bảo quản sách cẩn thận, không làm rách, viết vẽ bẩn lên sách.
        """);
        ConsoleUtils.pressEnterToContinue();
    }

    private void printWelcomeBanner() {
        System.out.println(ConsoleUtils.CYAN + ConsoleUtils.BOLD + """
        ================================================================================
        *                                                                              *
        *                  HỆ THỐNG QUẢN LÝ THƯ VIỆN ĐIỆN TỬ                           *
        *                     LIBRARY MANAGEMENT SYSTEM                                *
        *            Bài tập lớn môn: Lập trình Hướng đối tượng (Java + MySQL)         *
        *                                                                              *
        ================================================================================
        """ + ConsoleUtils.RESET);
    }
}
