package com.library.service;

import com.library.model.Fine;

import java.math.BigDecimal;
import java.util.List;

/**
 * Giao diện dịch vụ nghiệp vụ xử lý Tiền phạt và Phiếu phạt quá hạn.
 */
public interface FineService {

    /**
     * Mức phí phạt mặc định: 2.000 VNĐ / ngày quá hạn.
     */
    BigDecimal DAILY_FINE_RATE = new BigDecimal("2000.00");

    /**
     * Tạo phiếu phạt mới cho một phiếu mượn quá hạn.
     *
     * @param recordId    Mã phiếu mượn
     * @param userId      Mã độc giả bị phạt
     * @param overdueDays Số ngày trễ hạn
     * @param reason      Lý do phạt
     * @return Đối tượng Fine đã được tạo
     */
    Fine createFine(int recordId, int userId, int overdueDays, String reason);

    /**
     * Xử lý nộp tiền phạt cho một phiếu phạt.
     *
     * @param fineId Mã phiếu phạt
     * @return true nếu thanh toán thành công
     * @throws com.library.exception.ResourceNotFoundException nếu không tìm thấy phiếu phạt
     * @throws com.library.exception.ValidationException       nếu phiếu phạt đã được nộp trước đó
     */
    boolean payFine(int fineId);

    /**
     * Kiểm tra xem độc giả có đang nợ bất kỳ khoản phạt chưa đóng (UNPAID) nào không.
     *
     * @param userId Mã độc giả
     * @return true nếu độc giả có vé phạt chưa đóng
     */
    boolean hasUnpaidFines(int userId);

    /**
     * Lấy danh sách phiếu phạt của một độc giả.
     *
     * @param userId Mã độc giả
     * @return Danh sách phiếu phạt
     */
    List<Fine> getFinesByUserId(int userId);

    /**
     * Lấy danh sách toàn bộ các phiếu phạt chưa nộp (UNPAID).
     *
     * @return Danh sách phiếu phạt chưa đóng
     */
    List<Fine> getUnpaidFines();

    /**
     * Lấy thông tin phiếu phạt theo ID.
     *
     * @param fineId Mã phiếu phạt
     * @return Đối tượng Fine
     */
    Fine getFineById(int fineId);

    /**
     * Lấy phiếu phạt tương ứng với một phiếu mượn.
     *
     * @param recordId Mã phiếu mượn
     * @return Đối tượng Fine
     */
    Fine getFineByRecordId(int recordId);

    /**
     * Lấy toàn bộ danh sách phiếu phạt trong hệ thống.
     *
     * @return Danh sách Fine
     */
    List<Fine> getAllFines();
}
