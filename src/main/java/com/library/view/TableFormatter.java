package com.library.view;

import com.library.model.*;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.UserService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Lớp tiện ích định dạng bảng Console với căn lề thẳng hàng đẹp mắt.
 */
public final class TableFormatter {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private TableFormatter() {
    }

    public static void printBooksTable(List<Book> books, CategoryService categoryService) {
        if (books == null || books.isEmpty()) {
            ConsoleUtils.printWarning("Không có đầu sách nào để hiển thị.");
            return;
        }

        System.out.println("┌──────┬─────────────────────┬──────────────────────────────────────────┬──────────────────────┬──────────────────────┬─────────┬───────────┬──────────────┐");
        System.out.printf("│ %-4s │ %-19s │ %-40s │ %-20s │ %-20s │ %-7s │ %-9s │ %-12s │%n",
                "ID", "ISBN", "Tiêu đề sách", "Tác giả", "Thể loại", "Năm XB", "Tồn/Tổng", "Trạng thái");
        System.out.println("├──────┼─────────────────────┼──────────────────────────────────────────┼──────────────────────┼──────────────────────┼─────────┼───────────┼──────────────┤");

        for (Book b : books) {
            String catName = "Không rõ";
            if (categoryService != null) {
                try {
                    catName = categoryService.getCategoryById(b.getCategoryId()).getName();
                } catch (Exception ignored) {
                }
            }
            if (catName.length() > 20) catName = catName.substring(0, 17) + "...";
            String title = b.getTitle().length() > 40 ? b.getTitle().substring(0, 37) + "..." : b.getTitle();
            String author = b.getAuthor().length() > 20 ? b.getAuthor().substring(0, 17) + "..." : b.getAuthor();
            String stock = b.getAvailableCopies() + "/" + b.getTotalCopies();
            String status = b.isAvailable() ? "Còn sách" : "Hết sách";

            System.out.printf("│ %-4d │ %-19s │ %-40s │ %-20s │ %-20s │ %-7d │ %-9s │ %-12s │%n",
                    b.getBookId(), b.getIsbn(), title, author, catName, b.getPublishYear(), stock, status);
        }
        System.out.println("└──────┴─────────────────────┴──────────────────────────────────────────┴──────────────────────┴──────────────────────┴─────────┴───────────┴──────────────┘");
        System.out.println("Tổng cộng: " + books.size() + " đầu sách.");
    }

    public static void printCategoriesTable(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            ConsoleUtils.printWarning("Không có thể loại nào để hiển thị.");
            return;
        }

        System.out.println("┌──────┬──────────────────────────────────┬─────────────────────────────────────────────────────────────────┐");
        System.out.printf("│ %-4s │ %-32s │ %-63s │%n", "ID", "Tên thể loại", "Mô tả chi tiết");
        System.out.println("├──────┼──────────────────────────────────┼─────────────────────────────────────────────────────────────────┤");

        for (Category c : categories) {
            String desc = c.getDescription() != null ? c.getDescription() : "";
            if (desc.length() > 63) desc = desc.substring(0, 60) + "...";
            System.out.printf("│ %-4d │ %-32s │ %-63s │%n", c.getCategoryId(), c.getName(), desc);
        }
        System.out.println("└──────┴──────────────────────────────────┴─────────────────────────────────────────────────────────────────┘");
        System.out.println("Tổng cộng: " + categories.size() + " thể loại.");
    }

    public static void printMembersTable(List<Member> members) {
        if (members == null || members.isEmpty()) {
            ConsoleUtils.printWarning("Không có độc giả nào để hiển thị.");
            return;
        }

        System.out.println("┌──────┬───────────┬──────────────────────┬─────────────┬─────────────┬──────────────────────────┬──────────────┬──────────────┬─────────────┐");
        System.out.printf("│ %-4s │ %-9s │ %-20s │ %-11s │ %-11s │ %-24s │ %-12s │ %-12s │ %-11s │%n",
                "ID", "Mã ĐG", "Họ và tên", "Đang mượn", "Hạn mức", "Email", "SĐT", "Trạng thái", "Được mượn?");
        System.out.println("├──────┼───────────┼──────────────────────┼─────────────┼─────────────┼──────────────────────────┼──────────────┼──────────────┼─────────────┤");

        for (Member m : members) {
            String email = m.getEmail() != null ? m.getEmail() : "";
            if (email.length() > 24) email = email.substring(0, 21) + "...";
            String phone = m.getPhone() != null ? m.getPhone() : "";
            String canBorrowStr = m.canBorrow() ? "Có thể mượn" : "Bị chặn";

            System.out.printf("│ %-4d │ %-9s │ %-20s │ %-11d │ %-11d │ %-24s │ %-12s │ %-12s │ %-11s │%n",
                    m.getUserId(), m.getUserCode(), m.getFullName(), m.getCurrentBorrowCount(),
                    m.getMaxBorrowLimit(), email, phone, m.getStatus().getDescription(), canBorrowStr);
        }
        System.out.println("└──────┴───────────┴──────────────────────┴─────────────┴─────────────┴──────────────────────────┴──────────────┴──────────────┴─────────────┘");
        System.out.println("Tổng cộng: " + members.size() + " độc giả.");
    }

    public static void printStaffsTable(List<Staff> staffs) {
        if (staffs == null || staffs.isEmpty()) {
            ConsoleUtils.printWarning("Không có nhân viên nào để hiển thị.");
            return;
        }

        System.out.println("┌──────┬───────────┬──────────────────────┬─────────────────┬──────────────────────────┬──────────────┬──────────────────────┬──────────────┐");
        System.out.printf("│ %-4s │ %-9s │ %-20s │ %-15s │ %-24s │ %-12s │ %-20s │ %-12s │%n",
                "ID", "Mã NV", "Họ và tên", "Username", "Email", "SĐT", "Phòng ban", "Vai trò");
        System.out.println("├──────┼───────────┼──────────────────────┼─────────────────┼──────────────────────────┼──────────────┼──────────────────────┼──────────────┤");

        for (Staff s : staffs) {
            String email = s.getEmail() != null ? s.getEmail() : "";
            if (email.length() > 24) email = email.substring(0, 21) + "...";
            String dept = s.getDepartment() != null ? s.getDepartment() : "";
            System.out.printf("│ %-4d │ %-9s │ %-20s │ %-15s │ %-24s │ %-12s │ %-20s │ %-12s │%n",
                    s.getUserId(), s.getUserCode(), s.getFullName(), s.getUsername(),
                    email, s.getPhone() != null ? s.getPhone() : "", dept, s.getRoleDescription());
        }
        System.out.println("└──────┴───────────┴──────────────────────┴─────────────────┴──────────────────────────┴──────────────┴──────────────────────┴──────────────┘");
        System.out.println("Tổng cộng: " + staffs.size() + " nhân viên.");
    }

    public static void printBorrowRecordsTable(List<BorrowRecord> records, BookService bookService, UserService userService) {
        if (records == null || records.isEmpty()) {
            ConsoleUtils.printWarning("Không có phiếu mượn nào để hiển thị.");
            return;
        }

        System.out.println("┌───────────┬──────────────────────┬──────────────────────────────────────────┬──────────────┬──────────────┬──────────────┬──────────────┐");
        System.out.printf("│ %-9s │ %-20s │ %-40s │ %-12s │ %-12s │ %-12s │ %-12s │%n",
                "Mã phiếu", "Độc giả", "Sách mượn", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái");
        System.out.println("├───────────┼──────────────────────┼──────────────────────────────────────────┼──────────────┼──────────────┼──────────────┼──────────────┤");

        for (BorrowRecord r : records) {
            String memberName = "User #" + r.getUserId();
            if (userService != null) {
                try {
                    memberName = userService.getUserById(r.getUserId()).getFullName();
                } catch (Exception ignored) {
                }
            }
            if (memberName.length() > 20) memberName = memberName.substring(0, 17) + "...";

            String bookTitle = "Book #" + r.getBookId();
            if (bookService != null) {
                try {
                    bookTitle = bookService.getBookById(r.getBookId()).getTitle();
                } catch (Exception ignored) {
                }
            }
            if (bookTitle.length() > 40) bookTitle = bookTitle.substring(0, 37) + "...";

            String returnDateStr = r.getReturnDate() != null ? r.getReturnDate().toString() : "Chưa trả";

            System.out.printf("│ %-9d │ %-20s │ %-40s │ %-12s │ %-12s │ %-12s │ %-12s │%n",
                    r.getRecordId(), memberName, bookTitle, r.getBorrowDate(),
                    r.getDueDate(), returnDateStr, r.getStatus().getDescription());
        }
        System.out.println("└───────────┴──────────────────────┴──────────────────────────────────────────┴──────────────┴──────────────┴──────────────┴──────────────┘");
        System.out.println("Tổng cộng: " + records.size() + " phiếu mượn.");
    }

    public static void printFinesTable(List<Fine> fines, UserService userService) {
        if (fines == null || fines.isEmpty()) {
            ConsoleUtils.printWarning("Không có bản ghi tiền phạt nào.");
            return;
        }

        System.out.println("┌──────────┬───────────┬──────────────────────┬─────────────┬────────────────────┬──────────────────────────────────────┬────────────┐");
        System.out.printf("│ %-8s │ %-9s │ %-20s │ %-11s │ %-18s │ %-36s │ %-10s │%n",
                "Mã phạt", "Mã phiếu", "Độc giả", "Trễ (ngày)", "Số tiền (VNĐ)", "Lý do phạt", "Trạng thái");
        System.out.println("├──────────┼───────────┼──────────────────────┼─────────────┼────────────────────┼──────────────────────────────────────┼────────────┤");

        for (Fine f : fines) {
            String memberName = "User #" + f.getUserId();
            if (userService != null) {
                try {
                    memberName = userService.getUserById(f.getUserId()).getFullName();
                } catch (Exception ignored) {
                }
            }
            if (memberName.length() > 20) memberName = memberName.substring(0, 17) + "...";

            String reason = f.getReason() != null ? f.getReason() : "";
            if (reason.length() > 36) reason = reason.substring(0, 33) + "...";
            String amountStr = CURRENCY_FORMAT.format(f.getFineAmount()) + " đ";

            System.out.printf("│ %-8d │ %-9d │ %-20s │ %-11d │ %-18s │ %-36s │ %-10s │%n",
                    f.getFineId(), f.getRecordId(), memberName, f.getOverdueDays(),
                    amountStr, reason, f.getStatus().getDescription());
        }
        System.out.println("└──────────┴───────────┴──────────────────────┴─────────────┴────────────────────┴──────────────────────────────────────┴────────────┘");
        System.out.println("Tổng cộng: " + fines.size() + " phiếu phạt.");
    }
}
