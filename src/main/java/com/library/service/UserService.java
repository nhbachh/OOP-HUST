package com.library.service;

import com.library.model.Member;
import com.library.model.Role;
import com.library.model.Staff;
import com.library.model.User;

import java.util.List;

/**
 * Giao diện dịch vụ nghiệp vụ quản lý Người dùng (Độc giả, Nhân viên, Quản trị viên).
 */
public interface UserService {

    /**
     * Xác thực thông tin đăng nhập cho nhân viên và quản trị viên.
     *
     * @param username    Tên tài khoản
     * @param rawPassword Mật khẩu chưa mã hóa
     * @return Đối tượng User tương ứng nếu đăng nhập thành công
     * @throws com.library.exception.ValidationException nếu thông tin sai
     * @throws com.library.exception.BorrowException     nếu tài khoản bị khóa
     */
    User authenticate(String username, String rawPassword);

    /**
     * Lấy người dùng theo mã định danh ID.
     *
     * @param userId Mã ID
     * @return Đối tượng User
     */
    User getUserById(int userId);

    /**
     * Lấy người dùng theo mã thẻ bạn đọc hoặc mã nhân viên (userCode).
     *
     * @param userCode Mã người dùng (VD: MBR001, ADM001)
     * @return Đối tượng User
     */
    User getUserByCode(String userCode);

    /**
     * Lấy người dùng theo tên tài khoản đăng nhập.
     *
     * @param username Tên đăng nhập
     * @return Đối tượng User
     */
    User getUserByUsername(String username);

    /**
     * Chuyển đổi trạng thái tài khoản (ACTIVE <-> LOCKED).
     *
     * @param userId Mã người dùng
     * @return true nếu chuyển đổi thành công
     */
    boolean toggleUserStatus(int userId);

    /**
     * Lấy danh sách toàn bộ độc giả (Member).
     *
     * @return Danh sách Member
     */
    List<Member> getAllMembers();

    /**
     * Lấy danh sách toàn bộ nhân viên thư viện và quản trị viên (Staff).
     *
     * @return Danh sách Staff
     */
    List<Staff> getAllStaffs();

    /**
     * Thêm mới tài khoản nhân viên / quản trị viên.
     */
    Staff createStaff(String userCode, String username, String rawPassword,
                      String fullName, String email, String phone,
                      Role role, String department);

    /**
     * Thêm mới độc giả vào hệ thống (do Quản trị viên thực hiện).
     */
    Member createMember(String userCode, String fullName, String email, String phone);

    /**
     * Chức năng tự đăng ký tài khoản thẻ bạn đọc trực tuyến dành cho Độc giả.
     *
     * @param userCode    Mã sinh viên / Mã thẻ bạn đọc (bắt buộc, không trùng lặp)
     * @param username    Tên đăng nhập (bắt buộc, không trùng lặp)
     * @param rawPassword Mật khẩu chưa mã hóa (bắt buộc, tối thiểu 6 ký tự)
     * @param fullName    Họ và tên độc giả (bắt buộc)
     * @param email       Email liên hệ (tùy chọn)
     * @param phone       Số điện thoại (tùy chọn)
     * @return Đối tượng Member sau khi tạo thành công
     * @throws com.library.exception.ValidationException nếu thông tin không hợp lệ hoặc bị trùng lặp
     */
    Member registerMember(String userCode, String username, String rawPassword,
                          String fullName, String email, String phone);

    /**
     * Lấy toàn bộ người dùng trong hệ thống.
     *
     * @return Danh sách User
     */
    List<User> getAllUsers();

    /**
     * Thay đổi mật khẩu người dùng.
     *
     * @param userId      Mã người dùng
     * @param oldPassword Mật khẩu hiện tại
     * @param newPassword Mật khẩu mới (tối thiểu 6 ký tự)
     * @return true nếu thay đổi mật khẩu thành công
     * @throws com.library.exception.ValidationException nếu mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ
     */
    boolean changePassword(int userId, String oldPassword, String newPassword);

    /**
     * Cập nhật thông tin hồ sơ người dùng (họ tên, email, số điện thoại).
     *
     * @param userId   Mã người dùng
     * @param fullName Họ và tên mới
     * @param email    Email mới (không trùng lặp)
     * @param phone    Số điện thoại mới
     * @return true nếu cập nhật thành công
     * @throws com.library.exception.ValidationException nếu thông tin không hợp lệ hoặc email trùng lặp
     */
    boolean updateProfile(int userId, String fullName, String email, String phone);
}
