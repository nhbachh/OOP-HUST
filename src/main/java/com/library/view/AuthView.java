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
            System.out.println("  2. Đăng nhập Thẻ Độc giả (Mã thẻ bạn đọc MBRxxx)");
            System.out.println("  0. Quay lại Menu chính");
            ConsoleUtils.printDivider();

            int choice = ConsoleUtils.readInt(">> Chọn phương thức đăng nhập: ", 0, 2);

            switch (choice) {
                case 1 -> {
                    User staff = loginStaff();
                    if (staff != null) return staff;
                }
                case 2 -> {
                    User member = loginMember();
                    if (member != null) return member;
                }
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
}
