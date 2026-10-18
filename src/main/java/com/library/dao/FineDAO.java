package com.library.dao;

import com.library.model.Fine;
import com.library.model.FineStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO dành cho thực thể Fine (Phiếu phạt quá hạn).
 * Kế thừa CrudDAO<Fine, Integer> và cung cấp các hàm tra cứu theo phiếu mượn, độc giả, trạng thái đóng tiền.
 */
public interface FineDAO extends CrudDAO<Fine, Integer> {

    /**
     * Tìm phiếu phạt gắn với một phiếu mượn cụ thể.
     *
     * @param recordId Mã phiếu mượn
     * @return Optional chứa Fine nếu tồn tại
     */
    Optional<Fine> findByRecordId(int recordId);

    /**
     * Lấy danh sách toàn bộ phiếu phạt của một độc giả theo userId.
     *
     * @param userId Mã định danh độc giả
     * @return Danh sách phiếu phạt
     */
    List<Fine> findByUserId(int userId);

    /**
     * Lọc danh sách phiếu phạt theo trạng thái nộp tiền (UNPAID hoặc PAID).
     *
     * @param status Trạng thái đóng phạt
     * @return Danh sách phiếu phạt khớp trạng thái
     */
    List<Fine> findByStatus(FineStatus status);
}
