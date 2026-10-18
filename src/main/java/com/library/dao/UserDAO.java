package com.library.dao;

import com.library.model.Member;
import com.library.model.Role;
import com.library.model.Staff;
import com.library.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO dành cho thực thể User (áp dụng mô hình Single Table Inheritance).
 * Kế thừa CrudDAO<User, Integer> và cung cấp các phương thức tra cứu theo mã, tài khoản, vai trò.
 */
public interface UserDAO extends CrudDAO<User, Integer> {

    /**
     * Tìm người dùng theo mã định danh nghiệp vụ (user_code).
     *
     * @param userCode Mã người dùng (VD: ADM001, LIB001, MBR001)
     * @return Optional chứa User (Member hoặc Staff tương ứng)
     */
    Optional<User> findByUserCode(String userCode);

    /**
     * Tìm nhân viên theo tên tài khoản đăng nhập (username).
     *
     * @param username Tên đăng nhập
     * @return Optional chứa User nếu tìm thấy
     */
    Optional<User> findByUsername(String username);

    /**
     * Lấy danh sách người dùng theo vai trò cụ thể (ADMIN, LIBRARIAN, MEMBER).
     *
     * @param role Vai trò người dùng
     * @return Danh sách User
     */
    List<User> findByRole(Role role);

    /**
     * Lấy danh sách toàn bộ độc giả (Member).
     *
     * @return Danh sách các đối tượng Member
     */
    List<Member> findAllMembers();

    /**
     * Lấy danh sách toàn bộ nhân viên thư viện và quản trị viên (Staff).
     *
     * @return Danh sách các đối tượng Staff
     */
    List<Staff> findAllStaffs();
}
