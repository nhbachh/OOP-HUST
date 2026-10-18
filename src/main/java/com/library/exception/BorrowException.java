package com.library.exception;

/**
 * Ngoại lệ ném ra khi vi phạm các quy tắc nghiệp vụ mượn/trả sách trong thư viện:
 * - Tài khoản độc giả bị khóa (LOCKED).
 * - Độc giả đã mượn tối đa 5 cuốn sách.
 * - Sách đã hết bản sao khả dụng (availableCopies == 0).
 * - Độc giả đang nợ tiền phạt chưa thanh toán (UNPAID).
 * - Phiếu mượn không hợp lệ hoặc đã được trả trước đó.
 */
public class BorrowException extends LibraryException {

    public BorrowException(String message) {
        super(message);
    }

    public BorrowException(String message, Throwable cause) {
        super(message, cause);
    }
}
