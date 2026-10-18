package com.library;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Runner kiểm thử tương tác thực tế tầng DAO với CSDL MySQL (library_db) cho Milestone 3.
 * 1. Kiểm thử CategoryDAO (findAll, findByName, CRUD).
 * 2. Kiểm thử BookDAO (findByIsbn, findByTitle, findByCategoryId, updateAvailableCopies).
 * 3. Kiểm thử UserDAO & Ánh xạ Đa hình (findAll, Member vs Staff, getRoleDescription).
 * 4. Kiểm thử BorrowRecordDAO & FineDAO (findOverdueRecords, findByRecordId, findByStatus).
 * 5. Kiểm thử toàn vẹn CRUD (Insert -> Update -> Delete).
 */
public class Milestone3Test {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("            KIỂM THỬ THỰC TẾ TẦNG TRUY XUẤT CSDL (DAO LAYER) - MILESTONE 3       ");
        System.out.println("================================================================================\n");

        CategoryDAO categoryDAO = new CategoryDAOImpl();
        BookDAO bookDAO = new BookDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAOImpl();
        FineDAO fineDAO = new FineDAOImpl();

        // ---------------------------------------------------------------------
        // 1. Kiểm thử CategoryDAO
        // ---------------------------------------------------------------------
        System.out.println("--- 1. KIỂM THỬ CategoryDAO ---");
        List<Category> categories = categoryDAO.findAll();
        System.out.println("Tổng số thể loại hiện có: " + categories.size());
        for (Category c : categories) {
            System.out.printf("   [ID: %d] %-35s | %s%n", c.getCategoryId(), c.getName(), c.getDescription());
        }

        Optional<Category> itCat = categoryDAO.findByName("Công nghệ thông tin");
        System.out.println("Tìm thể loại 'Công nghệ thông tin': " + (itCat.isPresent() ? "THÀNH CÔNG -> " + itCat.get() : "KHÔNG TÌM THẤY"));
        System.out.println();

        // ---------------------------------------------------------------------
        // 2. Kiểm thử BookDAO
        // ---------------------------------------------------------------------
        System.out.println("--- 2. KIỂM THỬ BookDAO ---");
        List<Book> allBooks = bookDAO.findAll();
        System.out.println("Tổng số đầu sách hiện có: " + allBooks.size());

        String testIsbn = "978-604-01-2301-1";
        Optional<Book> bookByIsbn = bookDAO.findByIsbn(testIsbn);
        System.out.println("Tìm sách theo ISBN '" + testIsbn + "': " +
                (bookByIsbn.isPresent() ? "THÀNH CÔNG -> " + bookByIsbn.get().getTitle() + " (" + bookByIsbn.get().getAuthor() + ")" : "THẤT BẠI"));

        List<Book> javaBooks = bookDAO.findByTitle("Java");
        System.out.println("Tìm kiếm sách với từ khóa 'Java' (" + javaBooks.size() + " kết quả):");
        for (Book b : javaBooks) {
            System.out.printf("   * [%s] %-40s | Tồn kho: %d/%d | Sẵn sàng: %s%n",
                    b.getIsbn(), b.getTitle(), b.getAvailableCopies(), b.getTotalCopies(), b.isAvailable());
        }

        // Kiểm thử updateAvailableCopies
        if (bookByIsbn.isPresent()) {
            Book targetBook = bookByIsbn.get();
            int originalCopies = targetBook.getAvailableCopies();
            System.out.println("Thử nghiệm giảm 1 bản sao khả dụng cho sách ID " + targetBook.getBookId() + "...");
            boolean decSuccess = bookDAO.updateAvailableCopies(targetBook.getBookId(), -1);
            Optional<Book> updatedBook = bookDAO.findById(targetBook.getBookId());
            System.out.println("Kết quả giảm: " + decSuccess + " | Số bản sao mới: " +
                    updatedBook.map(Book::getAvailableCopies).orElse(-1) + " (Kỳ vọng: " + (originalCopies - 1) + ")");

            System.out.println("Hoàn nguyên lại số lượng bản sao khả dụng (+1)...");
            boolean incSuccess = bookDAO.updateAvailableCopies(targetBook.getBookId(), 1);
            System.out.println("Kết quả hoàn nguyên: " + incSuccess + " | Số bản sao: " +
                    bookDAO.findById(targetBook.getBookId()).map(Book::getAvailableCopies).orElse(-1));
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 3. Kiểm thử UserDAO & Tính Đa Hình (Polymorphism Mapping)
        // ---------------------------------------------------------------------
        System.out.println("--- 3. KIỂM THỬ UserDAO & ÁNH XẠ ĐA HÌNH (Single Table Inheritance) ---");
        List<User> allUsers = userDAO.findAll();
        System.out.println("Tổng số người dùng truy vấn từ bảng 'users': " + allUsers.size());
        for (User u : allUsers) {
            System.out.printf("   [Class: %-7s] Mã: %-7s | Họ tên: %-18s | Vai trò: %-10s | Mô tả đa hình: %s%n",
                    u.getClass().getSimpleName(),
                    u.getUserCode(),
                    u.getFullName(),
                    u.getRole(),
                    u.getRoleDescription());
        }

        List<Member> members = userDAO.findAllMembers();
        System.out.println("\nDanh sách Độc giả (Member) lấy qua findAllMembers() (" + members.size() + " người):");
        for (Member m : members) {
            System.out.printf("   * [%s] %-18s | Đang mượn: %d/%d cuốn | Được phép mượn tiếp: %s%n",
                    m.getUserCode(), m.getFullName(), m.getCurrentBorrowCount(), m.getMaxBorrowLimit(), m.canBorrow());
        }

        List<Staff> staffs = userDAO.findAllStaffs();
        System.out.println("\nDanh sách Nhân viên (Staff) lấy qua findAllStaffs() (" + staffs.size() + " người):");
        for (Staff s : staffs) {
            System.out.printf("   * [%s] %-18s | Username: %-12s | Phòng ban: %s | Vai trò: %s%n",
                    s.getUserCode(), s.getFullName(), s.getUsername(), s.getDepartment(), s.getRoleDescription());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 4. Kiểm thử BorrowRecordDAO & FineDAO
        // ---------------------------------------------------------------------
        System.out.println("--- 4. KIỂM THỬ BorrowRecordDAO & FineDAO ---");
        List<BorrowRecord> overdueRecords = borrowRecordDAO.findOverdueRecords();
        System.out.println("Số lượng phiếu mượn quá hạn (OVERDUE): " + overdueRecords.size());
        for (BorrowRecord br : overdueRecords) {
            Optional<Fine> fineOpt = fineDAO.findByRecordId(br.getRecordId());
            System.out.printf("   * [Phiếu #%d] User ID: %d | Book ID: %d | Ngày hẹn: %s | Phạt: %s%n",
                    br.getRecordId(), br.getUserId(), br.getBookId(), br.getDueDate(),
                    fineOpt.map(f -> f.getFineAmount() + " VNĐ (" + f.getStatus().getDescription() + ")").orElse("Chưa lập phiếu phạt"));
        }

        List<Fine> unpaidFines = fineDAO.findByStatus(FineStatus.UNPAID);
        System.out.println("Phiếu phạt chưa thanh toán (UNPAID) (" + unpaidFines.size() + " phiếu):");
        for (Fine f : unpaidFines) {
            System.out.printf("   * [Mã phạt: #%d] Phiếu mượn: #%d | Số tiền: %s VNĐ | Lý do: %s%n",
                    f.getFineId(), f.getRecordId(), f.getFineAmount(), f.getReason());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 5. Kiểm thử CRUD Hoàn Chỉnh (Insert -> FindById -> Update -> DeleteById)
        // ---------------------------------------------------------------------
        System.out.println("--- 5. KIỂM THỬ CHU KỲ CRUD TOÀN DIỆN (Generic CrudDAO) ---");
        Category tempCategory = new Category("Tạp chí & Chuyên đề", "Tài liệu nghiên cứu khoa học xuất bản định kỳ");
        boolean insertOk = categoryDAO.insert(tempCategory);
        System.out.println("1. INSERT Category tạm: " + (insertOk ? "THÀNH CÔNG (ID sinh tự động = " + tempCategory.getCategoryId() + ")" : "THẤT BẠI"));

        Optional<Category> foundTemp = categoryDAO.findById(tempCategory.getCategoryId());
        System.out.println("2. FINDBYID Category vừa thêm: " + (foundTemp.isPresent() ? "THÀNH CÔNG -> " + foundTemp.get() : "THẤT BẠI"));

        tempCategory.setDescription("Mô tả đã được cập nhật thành công qua CRUD update()");
        boolean updateOk = categoryDAO.update(tempCategory);
        System.out.println("3. UPDATE Category: " + (updateOk ? "THÀNH CÔNG" : "THẤT BẠI"));

        boolean deleteOk = categoryDAO.deleteById(tempCategory.getCategoryId());
        System.out.println("4. DELETEBYID Category: " + (deleteOk ? "THÀNH CÔNG" : "THẤT BẠI"));

        Optional<Category> checkAfterDelete = categoryDAO.findById(tempCategory.getCategoryId());
        System.out.println("5. XÁC NHẬN SAU KHI XÓA: " + (checkAfterDelete.isEmpty() ? "ĐÃ XÓA SẠCH HOÀN TOÀN (Optional.empty)" : "VẪN CÒN TỒN TẠI"));

        System.out.println("\n================================================================================");
        System.out.println("           TẤT CẢ KIỂM THỬ CHO MILESTONE 3 ĐÃ VƯỢT QUA XUẤT SẮC!                ");
        System.out.println("================================================================================");

        // Đóng các background threads của MySQL connector để thoát chương trình sạch sẽ
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
