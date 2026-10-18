package com.library.exception;

/**
 * Ngoại lệ ném ra khi dữ liệu đầu vào không hợp lệ hoặc vi phạm quy tắc định dạng.
 */
public class ValidationException extends LibraryException {

    public ValidationException(String message) {
        super(message);
    }
}
