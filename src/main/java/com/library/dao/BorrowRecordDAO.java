package com.library.dao;

import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;

import java.util.List;

/**
 * Interface DAO dành cho thực thể BorrowRecord (Phiếu mượn / trả sách).
 * Kế thừa CrudDAO<BorrowRecord, Integer> và cung cấp các phương thức lọc theo độc giả, trạng thái.
 */
public interface BorrowRecordDAO extends CrudDAO<BorrowRecord, Integer> {

    /**
     * Lấy lịch sử tất cả các phiếu mượn của một độc giả theo userId.
     *
     * @param userId Mã định danh độc giả
     * @return Danh sách các phiếu mượn
     */
    List<BorrowRecord> findByUserId(int userId);

    /**
     * Lọc danh sách phiếu mượn theo trạng thái (BORROWED, RETURNED, OVERDUE).
     *
     * @param status Trạng thái phiếu mượn
     * @return Danh sách phiếu mượn khớp trạng thái
     */
    List<BorrowRecord> findByStatus(BorrowStatus status);

    /**
     * Lấy danh sách toàn bộ các phiếu mượn đang bị quá hạn (status = 'OVERDUE').
     *
     * @return Danh sách các phiếu mượn quá hạn
     */
    List<BorrowRecord> findOverdueRecords();
}
