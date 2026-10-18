package com.library.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên trong hệ thống (Sách, Độc giả, Phiếu mượn, Vé phạt...).
 */
public class ResourceNotFoundException extends LibraryException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("Không tìm thấy %s với mã định danh: %s", resourceName, identifier));
    }
}
