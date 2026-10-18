package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.*;

import java.util.List;

/**
 * Giao diện Console dành riêng cho Thủ thư (Role: LIBRARIAN).
 */
public class LibrarianView {

    protected final Staff currentStaff;
    protected final BookService bookService;
    protected final CategoryService categoryService;
    protected final BorrowService borrowService;
    protected final FineService fineService;
    protected final UserService userService;

    public LibrarianView(Staff currentStaff, BookService bookService,
                         CategoryService categoryService, BorrowService borrowService,
                         FineService fineService, UserService userService) {
        this.currentStaff = currentStaff;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("BÀN LÀM VIỆC THỦ THƯ — " + currentStaff.getFullName().toUpperCase());
            System.out.println("  1. Quản lý đầu sách (Tra cứu, Thêm mới, Cập nhật tồn kho)");
            System.out.println("  2. Quản lý thể loại sách (Danh mục, Thêm mới)");
            System.out.println("  3. Thực hiện nghiệp vụ Mượn sách (Borrow Book)");
            System.out.println("  4. Thực hiện nghiệp vụ Trả sách (Return Book)");
            System.out.println("  5. Quản lý vé phạt & Thu tiền phạt quá hạn");
            System.out.println("  6. Tra cứu danh sách độc giả & Tình trạng mượn");
            System.out.println("  0. Đăng xuất");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(">> Chọn chức năng [0-6]: ", 0, 6);
            switch (choice) {
                case 1 -> manageBooks();
                case 2 -> manageCategories();
                case 3 -> handleBorrowBook();
                case 4 -> handleReturnBook();
                case 5 -> manageFines();
                case 6 -> viewMembersList();
                case 0 -> {
                    ConsoleUtils.printInfo("Đã đăng xuất tài khoản thủ thư.");
                    running = false;
                }
            }
        }
    }

    // 1. Quản lý sách
    protected void manageBooks() {
        ConsoleUtils.printSubHeader("QUẢN LÝ ĐẦU SÁCH");
        System.out.println("  1. Xem toàn bộ sách");
        System.out.println("  2. Tìm kiếm sách theo từ khóa tiêu đề");
        System.out.println("  3. Thêm mới đầu sách");
        System.out.println("  4. Cập nhật thông tin & số lượng sách");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Lựa chọn: ", 0, 4);
        switch (choice) {
            case 1 -> TableFormatter.printBooksTable(bookService.getAllBooks(), categoryService);
            case 2 -> {
                String kw = ConsoleUtils.readString("Nhập từ khóa tiêu đề: ", false);
                TableFormatter.printBooksTable(bookService.searchBooks(kw), categoryService);
            }
            case 3 -> addNewBook();
            case 4 -> updateExistingBook();
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void addNewBook() {
        ConsoleUtils.printSubHeader("THÊM MỚI ĐẦU SÁCH VÀO KHO");
        String isbn = ConsoleUtils.readString("Nhập mã ISBN (VD: 978-604-01-xxxx-x): ", false);
        String title = ConsoleUtils.readString("Nhập tiêu đề sách: ", false);
        String author = ConsoleUtils.readString("Nhập tên tác giả: ", false);

        TableFormatter.printCategoriesTable(categoryService.getAllCategories());
        int catId = ConsoleUtils.readInt("Nhập mã ID thể loại phù hợp: ", 1, 99999);
        int publishYear = ConsoleUtils.readInt("Nhập năm xuất bản (VD: 2023): ", 1900, 2099);
        int totalCopies = ConsoleUtils.readInt("Nhập tổng số bản in nhập kho (>= 1): ", 1, 1000);

        try {
            Book book = bookService.createBook(isbn, title, author, catId, publishYear, totalCopies);
            ConsoleUtils.printSuccess("Thêm sách mới thành công! ID = " + book.getBookId() + ", Tiêu đề: " + book.getTitle());
        } catch (LibraryException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void updateExistingBook() {
        ConsoleUtils.printSubHeader("CẬP NHẬT THÔNG TIN ĐẦU SÁCH");
        int bookId = ConsoleUtils.readInt("Nhập ID cuốn sách cần sửa: ", 1, 99999);

        try {
            Book book = bookService.getBookById(bookId);
            System.out.println("Thông tin hiện tại: " + book.getTitle() + " | Tồn kho: " +
                    book.getAvailableCopies() + "/" + book.getTotalCopies());

            String newTitle = ConsoleUtils.readString("Nhập tiêu đề mới (để trống nếu giữ nguyên): ", true);
            if (!newTitle.isEmpty()) book.setTitle(newTitle);

            String newAuthor = ConsoleUtils.readString("Nhập tác giả mới (để trống nếu giữ nguyên): ", true);
            if (!newAuthor.isEmpty()) book.setAuthor(newAuthor);

            System.out.print("Bạn có muốn đổi tổng số sách không? (1: Có, 0: Không): ");
            int changeCopies = ConsoleUtils.readInt("", 0, 1);
            if (changeCopies == 1) {
                int newTotal = ConsoleUtils.readInt("Nhập tổng số sách mới: ", 0, 10000);
                int newAvail = ConsoleUtils.readInt("Nhập số sách có sẵn mới: ", 0, newTotal);
                book.setCopies(newTotal, newAvail);
            }

            bookService.updateBook(book);
            ConsoleUtils.printSuccess("Cập nhật thông tin sách thành công!");
        } catch (Exception e) {
            ConsoleUtils.printError("Không thể cập nhật sách: " + e.getMessage());
        }
    }

    // 2. Quản lý thể loại
    protected void manageCategories() {
        ConsoleUtils.printSubHeader("QUẢN LÝ THỂ LOẠI SÁCH");
        System.out.println("  1. Xem danh sách thể loại");
        System.out.println("  2. Thêm thể loại mới");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Lựa chọn: ", 0, 2);
        switch (choice) {
            case 1 -> TableFormatter.printCategoriesTable(categoryService.getAllCategories());
            case 2 -> {
                String name = ConsoleUtils.readString("Nhập tên thể loại mới: ", false);
                String desc = ConsoleUtils.readString("Nhập mô tả thể loại: ", false);
                try {
                    Category cat = categoryService.createCategory(name, desc);
                    ConsoleUtils.printSuccess("Thêm thể loại thành công: " + cat.getName() + " (ID: " + cat.getCategoryId() + ")");
                } catch (Exception e) {
                    ConsoleUtils.printError(e.getMessage());
                }
            }
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // 3. Nghiệp vụ Mượn sách
    protected void handleBorrowBook() {
        ConsoleUtils.printSubHeader("THỰC HIỆN NGHIỆP VỤ MƯỢN SÁCH (ACID TRANSACTION)");
        String memberCode = ConsoleUtils.readString("Nhập Mã thẻ bạn đọc (VD: MBR001): ", false);

        try {
            User user = userService.getUserByCode(memberCode);
            if (!(user instanceof Member member)) {
                ConsoleUtils.printError("Mã người dùng không phải là độc giả!");
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            System.out.println(">> Độc giả: " + member.getFullName() + " | Sách đang mượn: " +
                    member.getCurrentBorrowCount() + "/" + member.getMaxBorrowLimit());

            int bookId = ConsoleUtils.readInt("Nhập ID cuốn sách muốn mượn: ", 1, 99999);
            Book book = bookService.getBookById(bookId);
            System.out.println(">> Cuốn sách: " + book.getTitle() + " | Khả dụng: " + book.getAvailableCopies());

            int borrowDays = ConsoleUtils.readInt("Nhập số ngày cho mượn (mặc định 14 ngày): ", 1, 60);

            BorrowRecord record = borrowService.borrowBook(member.getUserId(), bookId, currentStaff.getUserId(), borrowDays);

            ConsoleUtils.printSuccess(String.format("Lập phiếu mượn thành công! Mã phiếu: #%d | Hạn trả: %s",
                    record.getRecordId(), record.getDueDate()));

        } catch (BorrowException | ValidationException | ResourceNotFoundException e) {
            ConsoleUtils.printError("MƯỢN SÁCH THẤT BẠI: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // 4. Nghiệp vụ Trả sách
    protected void handleReturnBook() {
        ConsoleUtils.printSubHeader("THỰC HIỆN NGHIỆP VỤ TRẢ SÁCH (ACID TRANSACTION)");
        int recordId = ConsoleUtils.readInt("Nhập Mã phiếu mượn cần trả: ", 1, 99999);

        try {
            BorrowRecord record = borrowService.getBorrowRecordById(recordId);
            System.out.printf(">> Thông tin phiếu #%d: User ID=%d, Book ID=%d, Hạn trả=%s, Trạng thái=%s%n",
                    record.getRecordId(), record.getUserId(), record.getBookId(),
                    record.getDueDate(), record.getStatus().getDescription());

            BorrowRecord returned = borrowService.returnBook(recordId);
            ConsoleUtils.printSuccess("Trả sách thành công! Đã hoàn trả sách về kho.");

            // Kiểm tra xem có vé phạt mới được tạo không
            try {
                Fine fine = fineService.getFineByRecordId(recordId);
                if (fine.getStatus() == FineStatus.UNPAID) {
                    ConsoleUtils.printWarning(String.format("Phát hiện trả trễ %d ngày! Tự động lập vé phạt #%d: Số tiền %s đ",
                            fine.getOverdueDays(), fine.getFineId(), fine.getFineAmount()));
                }
            } catch (ResourceNotFoundException ignored) {
            }

        } catch (BorrowException | ResourceNotFoundException e) {
            ConsoleUtils.printError("TRẢ SÁCH THẤT BẠI: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // 5. Quản lý vé phạt
    protected void manageFines() {
        ConsoleUtils.printSubHeader("QUẢN LÝ VÉ PHẠT QUÁ HẠN");
        System.out.println("  1. Xem danh sách vé phạt chưa thanh toán (UNPAID)");
        System.out.println("  2. Thực hiện thu tiền phạt (Ghi nhận PAID)");
        System.out.println("  0. Quay lại");
        ConsoleUtils.printDivider();

        int choice = ConsoleUtils.readInt(">> Lựa chọn: ", 0, 2);
        switch (choice) {
            case 1 -> {
                List<Fine> unpaids = fineService.getUnpaidFines();
                TableFormatter.printFinesTable(unpaids, userService);
            }
            case 2 -> {
                int fineId = ConsoleUtils.readInt("Nhập Mã phiếu phạt cần thu tiền: ", 1, 99999);
                try {
                    Fine fine = fineService.getFineById(fineId);
                    System.out.println("Thông tin vé phạt: Số tiền " + fine.getFineAmount() + " VNĐ | Trạng thái: " + fine.getStatus());
                    boolean ok = fineService.payFine(fineId);
                    if (ok) {
                        ConsoleUtils.printSuccess("Đã thu tiền và đóng phiếu phạt #" + fineId + " thành công!");
                    }
                } catch (Exception e) {
                    ConsoleUtils.printError("Thanh toán phạt thất bại: " + e.getMessage());
                }
            }
            case 0 -> {
                return;
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // 6. Tra cứu danh sách độc giả
    protected void viewMembersList() {
        ConsoleUtils.printSubHeader("DANH SÁCH ĐỘC GIẢ TRONG HỆ THỐNG");
        List<Member> members = userService.getAllMembers();
        TableFormatter.printMembersTable(members);
        ConsoleUtils.pressEnterToContinue();
    }
}
