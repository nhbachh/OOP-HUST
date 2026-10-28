package com.library.view;

import com.library.exception.BorrowException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.model.Role;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.service.UserService;

/**
 * Màn hình Đăng nhập và Xác thực người dùng (AuthView).
 */
public class AuthView {

    private final UserService userService;

    public AuthView(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị giao diện đăng nhập và trả về đối tượng User sau khi xác thực thành công.
     *
     * @return User đối tượng người dùng đã đăng nhập, hoặc null nếu chọn quay lại
     */
    public User showLogin() {
        while (true) {
            ConsoleUtils.printHeader("ĐĂNG NHẬP HỆ THỐNG THƯ VIỆN");
            System.out.println("  1. Đăng nhập Tài khoản Nhân viên / Quản trị viên (Username & Password)");
            System.out.println("  2. Đăng nhập Thẻ Độc giả (Mã thẻ bạn đọc MBRxxx hoặc MSSV)");
            System.out.println("  3. Đăng ký Thẻ Độc giả mới (Tự đăng ký trực tuyến)");
            System.out.println("  0. Quay lại Menu chính");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(">> Chọn chức năng: ", 0, 3);

            switch (choice) {
                case 1 -> {
                    User staff = loginStaff();
                    if (staff != null) return staff;
                }
                case 2 -> {
                    User member = loginMember();
                    if (member != null) return member;
                }
                case 3 -> registerMemberCli();
                case 0 -> {
                    return null;
                }
            }
        }
    }

    private User loginStaff() {
        ConsoleUtils.printSubHeader("ĐĂNG NHẬP NHÂN VIÊN / QUẢN TRỊ VIÊN");
        String username = ConsoleUtils.readString("Nhập Tên tài khoản (username): ", false);
        String password = ConsoleUtils.readString("Nhập Mật khẩu (password): ", false);

        try {
            User user = userService.authenticate(username, password);
            ConsoleUtils.printSuccess("Đăng nhập thành công! Xin chào " + user.getFullName() +
                    " (" + user.getRoleDescription() + ")");
            ConsoleUtils.pressEnterToContinue();
            return user;
        } catch (ValidationException | BorrowException e) {
            ConsoleUtils.printError(e.getMessage());
            ConsoleUtils.pressEnterToContinue();
            return null;
        }
    }

    private User loginMember() {
        ConsoleUtils.printSubHeader("ĐĂNG NHẬP THẺ ĐỘC GIẢ");
        System.out.println("(Gợi ý mã mẫu từ hệ thống: MBR001, MBR002, MBR003)");
        String code = ConsoleUtils.readString("Nhập Mã thẻ bạn đọc (userCode): ", false);

        try {
            User user = userService.getUserByCode(code);
            if (user.getRole() != Role.MEMBER) {
                ConsoleUtils.printError("Mã người dùng này không thuộc về Độc giả thư viện.");
                ConsoleUtils.pressEnterToContinue();
                return null;
            }

            if (user.getStatus() == UserStatus.LOCKED) {
                ConsoleUtils.printError("Thẻ thư viện của bạn hiện đang bị KHÓA. Vui lòng liên hệ thủ thư!");
                ConsoleUtils.pressEnterToContinue();
                return null;
            }

            ConsoleUtils.printSuccess("Đăng nhập độc giả thành công! Chào mừng " + user.getFullName() + " đến với thư viện.");
            ConsoleUtils.pressEnterToContinue();
            return user;
        } catch (ResourceNotFoundException e) {
            ConsoleUtils.printError("Không tìm thấy thẻ bạn đọc với mã '" + code + "'. Vui lòng kiểm tra lại!");
            ConsoleUtils.pressEnterToContinue();
            return null;
        }
    }

    private void registerMemberCli() {
        ConsoleUtils.printSubHeader("ĐĂNG KÝ THẺ ĐỘC GIẢ TRỰC TUYẾN");
        System.out.println("Vui lòng nhập các thông tin bắt buộc (*):");
        String userCode = ConsoleUtils.readString("Nhập Mã SV / Mã thẻ bạn đọc (*): ", false);
        String fullName = ConsoleUtils.readString("Nhập Họ và tên độc giả (*): ", false);
        String username = ConsoleUtils.readString("Nhập Tên tài khoản (username) (*): ", false);
        String password = ConsoleUtils.readString("Nhập Mật khẩu (tối thiểu 6 ký tự) (*): ", false);
        String confirmPassword = ConsoleUtils.readString("Xác nhận lại Mật khẩu (*): ", false);

        if (!password.equals(confirmPassword)) {
            ConsoleUtils.printError("Mật khẩu xác nhận không khớp! Hủy bỏ quá trình đăng ký.");
            ConsoleUtils.pressEnterToContinue();
            return;
        }

        String email = ConsoleUtils.readString("Nhập Email liên hệ (bỏ trống nếu không có): ", true);
        String phone = ConsoleUtils.readString("Nhập Số điện thoại (bỏ trống nếu không có): ", true);

        try {
            Member member = userService.registerMember(userCode, username, password, fullName, email, phone);
            ConsoleUtils.printSuccess("Chúc mừng! Đăng ký Thẻ Độc giả thành công.");
            System.out.println("  • Mã thẻ / MSSV : " + member.getUserCode());
            System.out.println("  • Tên độc giả   : " + member.getFullName());
            System.out.println("  • Tên đăng nhập : " + member.getUsername());
            System.out.println("  • Hạn mức mượn  : " + member.getMaxBorrowLimit() + " cuốn sách");
            System.out.println("\nBạn có thể đăng nhập ngay bây giờ bằng Tên đăng nhập hoặc Mã thẻ!");
            ConsoleUtils.pressEnterToContinue();
        } catch (ValidationException e) {
            ConsoleUtils.printError(e.getMessage());
            ConsoleUtils.pressEnterToContinue();
        } catch (Exception e) {
            ConsoleUtils.printError("Lỗi hệ thống khi đăng ký thẻ bạn đọc: " + e.getMessage());
            ConsoleUtils.pressEnterToContinue();
        }
    }
}
