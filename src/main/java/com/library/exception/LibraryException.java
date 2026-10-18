package com.library.exception;

/**
 * Lớp cha cơ sở cho toàn bộ các ngoại lệ nghiệp vụ trong hệ thống Quản lý Thư viện.
 * Kế thừa từ RuntimeException để hỗ trợ unchecked exceptions trong tầng Service.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
