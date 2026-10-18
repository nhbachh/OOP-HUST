package com.library.service;

import com.library.model.BorrowRecord;

import java.util.List;

/**
 * Giao diện dịch vụ nghiệp vụ mượn/trả sách, quản lý giao dịch CSDL (JDBC Transactions).
 */
public interface BorrowService {

    /**
     * Mượn sách (giao dịch ACID gồm trừ tồn kho sách và tạo phiếu mượn).
     *
     * @param memberId   Mã ID độc giả mượn sách
     * @param bookId     Mã ID đầu sách
     * @param staffId    Mã ID nhân viên xử lý phiếu (có thể null)
     * @param borrowDays Số ngày mượn sách (mặc định thường là 14 ngày)
     * @return Đối tượng BorrowRecord vừa được tạo
     * @throws com.library.exception.BorrowException           nếu vi phạm quy tắc mượn hoặc giao dịch thất bại
     * @throws com.library.exception.ResourceNotFoundException nếu không tìm thấy độc giả hoặc sách
     */
    BorrowRecord borrowBook(int memberId, int bookId, Integer staffId, int borrowDays);

    /**
     * Trả sách (giao dịch ACID gồm cộng lại tồn kho, cập nhật phiếu mượn, và tự động sinh vé phạt nếu quá hạn).
     *
     * @param recordId Mã phiếu mượn cần trả
     * @return Đối tượng BorrowRecord sau khi cập nhật
     * @throws com.library.exception.BorrowException           nếu phiếu mượn đã trả hoặc giao dịch lỗi
     * @throws com.library.exception.ResourceNotFoundException nếu không tìm thấy phiếu mượn
     */
    BorrowRecord returnBook(int recordId);

    /**
     * Phương thức phục vụ kiểm thử Transaction Rollback:
     * Giả lập ngoại lệ phát sinh ngay sau khi trừ số lượng sách, nhằm xác minh conn.rollback() hoàn nguyên dữ liệu.
     *
     * @param memberId   Mã ID độc giả
     * @param bookId     Mã ID sách
     * @param staffId    Mã ID nhân viên
     * @param borrowDays Số ngày mượn
     * @return Không bao giờ trả về bình thường (ném BorrowException)
     */
    BorrowRecord borrowBookWithSimulatedFailure(int memberId, int bookId, Integer staffId, int borrowDays);

    /**
     * Lấy phiếu mượn theo mã ID.
     *
     * @param recordId Mã phiếu mượn
     * @return Đối tượng BorrowRecord
     */
    BorrowRecord getBorrowRecordById(int recordId);

    /**
     * Lấy danh sách phiếu mượn của một độc giả.
     *
     * @param userId Mã độc giả
     * @return Danh sách phiếu mượn
     */
    List<BorrowRecord> getBorrowRecordsByUserId(int userId);

    /**
     * Lấy toàn bộ phiếu mượn đang bị quá hạn.
     *
     * @return Danh sách phiếu mượn quá hạn
     */
    List<BorrowRecord> getOverdueBorrowRecords();

    /**
     * Lấy toàn bộ lịch sử phiếu mượn trong hệ thống.
     *
     * @return Danh sách toàn bộ phiếu mượn
     */
    List<BorrowRecord> getAllBorrowRecords();
}
