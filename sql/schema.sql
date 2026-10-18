-- ============================================================
-- Library Management System — Database Schema
-- Môn: Lập trình Hướng đối tượng | Nhóm: [Tên nhóm]
-- Phiên bản: Milestone 1 — Single Table Inheritance Design
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS fines;
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. Bảng categories — Thể loại sách
-- ============================================================
CREATE TABLE categories (
    category_id  INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Thể loại / Danh mục sách';

-- ============================================================
-- 2. Bảng books — Đầu sách trong thư viện
-- ============================================================
CREATE TABLE books (
    book_id          INT          NOT NULL AUTO_INCREMENT,
    isbn             VARCHAR(20)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(150) NOT NULL,
    category_id      INT          NOT NULL,
    publish_year     INT,
    total_copies     INT          NOT NULL DEFAULT 0,
    available_copies INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT fk_books_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_books_copies
        CHECK (available_copies >= 0 AND available_copies <= total_copies)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Thông tin chi tiết các đầu sách';

-- ============================================================
-- 3. Bảng users — Single Table Inheritance: ADMIN | LIBRARIAN | MEMBER
--    * user_code  : mã định danh nghiệp vụ (VD: ADM001, LIB001, MBR001)
--    * username   : tên đăng nhập (chỉ ADMIN và LIBRARIAN dùng)
--    * password_hash : mật khẩu băm SHA-256 (NULL cho MEMBER thuần)
-- ============================================================
CREATE TABLE users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    user_code     VARCHAR(20)  NOT NULL,
    username      VARCHAR(50),
    password_hash VARCHAR(255),
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100),
    phone         VARCHAR(20),
    role          ENUM('ADMIN','LIBRARIAN','MEMBER') NOT NULL DEFAULT 'MEMBER',
    status        ENUM('ACTIVE','LOCKED')            NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_user_code UNIQUE (user_code),
    CONSTRAINT uq_users_username  UNIQUE (username),
    CONSTRAINT uq_users_email     UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Single Table Inheritance: ADMIN, LIBRARIAN, MEMBER dùng chung 1 bảng';

-- ============================================================
-- 4. Bảng borrow_records — Phiếu mượn / trả sách
--    * user_id  : độc giả (MEMBER) thực hiện mượn sách
--    * staff_id : nhân viên (ADMIN/LIBRARIAN) duyệt / xử lý phiếu
-- ============================================================
CREATE TABLE borrow_records (
    record_id   INT      NOT NULL AUTO_INCREMENT,
    user_id     INT      NOT NULL,
    book_id     INT      NOT NULL,
    staff_id    INT      NOT NULL,
    borrow_date DATE     NOT NULL,
    due_date    DATE     NOT NULL,
    return_date DATE,
    status      ENUM('BORROWED','RETURNED','OVERDUE') NOT NULL DEFAULT 'BORROWED',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_borrow_records PRIMARY KEY (record_id),
    CONSTRAINT fk_borrow_member
        FOREIGN KEY (user_id)  REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_borrow_book
        FOREIGN KEY (book_id)  REFERENCES books (book_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_borrow_staff
        FOREIGN KEY (staff_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Giao dịch mượn / trả sách';

-- ============================================================
-- 5. Bảng fines — Quản lý tiền phạt quá hạn
--    * record_id : phiếu mượn phát sinh vi phạm (OVERDUE)
--    * user_id   : độc giả bị phạt (để truy vấn nhanh theo người dùng)
-- ============================================================
CREATE TABLE fines (
    fine_id     INT            NOT NULL AUTO_INCREMENT,
    record_id   INT            NOT NULL,
    user_id     INT            NOT NULL,
    overdue_days INT           NOT NULL DEFAULT 0,
    fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    reason      VARCHAR(255),
    status      ENUM('UNPAID','PAID') NOT NULL DEFAULT 'UNPAID',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at     TIMESTAMP,

    CONSTRAINT pk_fines PRIMARY KEY (fine_id),
    CONSTRAINT fk_fines_record
        FOREIGN KEY (record_id) REFERENCES borrow_records (record_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_fines_user
        FOREIGN KEY (user_id)   REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_fines_amount
        CHECK (fine_amount >= 0),
    CONSTRAINT chk_fines_overdue_days
        CHECK (overdue_days >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Khoản phạt quá hạn gắn với từng phiếu mượn';
