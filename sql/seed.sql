-- ============================================================
-- Library Management System — Dữ liệu mẫu (Tiếng Việt)
-- Môn: Lập trình Hướng đối tượng | Milestone 1
-- ============================================================

USE library_db;

-- ============================================================
-- 1. Thể loại sách (≥ 4 bản ghi)
-- ============================================================
INSERT INTO categories (name, description) VALUES
('Công nghệ thông tin',
 'Lập trình, kiến trúc phần mềm, cơ sở dữ liệu, trí tuệ nhân tạo và an ninh mạng'),
('Văn học — Tiểu thuyết',
 'Tác phẩm văn học kinh điển Việt Nam và thế giới, tiểu thuyết, truyện ngắn'),
('Kinh tế — Quản trị',
 'Tài chính doanh nghiệp, quản trị kinh doanh, marketing và đầu tư tài chính'),
('Khoa học tự nhiên',
 'Vật lý, thiên văn học, toán học ứng dụng, hoá học và sinh học phân tử'),
('Kỹ năng sống — Phát triển bản thân',
 'Tư duy lãnh đạo, quản lý thời gian, giao tiếp hiệu quả và phát triển bản thân');

-- ============================================================
-- 2. Đầu sách (≥ 8 bản ghi)
-- ============================================================
INSERT INTO books (isbn, title, author, category_id, publish_year, total_copies, available_copies) VALUES
-- CNTT
('978-604-01-2301-1', 'Lập Trình Java Căn Bản và Nâng Cao',                   'Nguyễn Văn An',       1, 2022, 5, 4),
('978-604-01-2302-8', 'Cấu Trúc Dữ Liệu và Giải Thuật với Java',             'Trần Thị Bích',       1, 2021, 4, 3),
('978-604-01-2303-5', 'Thiết Kế Mẫu (Design Patterns) Thực Chiến',           'Lê Hoàng Nam',        1, 2023, 3, 3),
('978-604-01-2304-2', 'Clean Code — Mã Sạch Trong Lập Trình',                'Robert C. Martin',    1, 2021, 4, 4),
-- Văn học
('978-604-02-1101-2', 'Nhà Giả Kim',                                          'Paulo Coelho',        2, 2020, 6, 5),
('978-604-02-1102-9', 'Dế Mèn Phiêu Lưu Ký',                                 'Tô Hoài',             2, 2019, 5, 5),
-- Kỹ năng sống
('978-604-05-5501-9', 'Đắc Nhân Tâm',                                         'Dale Carnegie',       5, 2021, 8, 7),
('978-604-05-5502-6', 'Tư Duy Nhanh và Chậm',                                 'Daniel Kahneman',     5, 2022, 5, 5),
-- Khoa học
('978-604-04-4401-7', 'Vũ Trụ Trong Vỏ Hạt Dẻ',                              'Stephen Hawking',     4, 2018, 3, 3),
-- Kinh tế
('978-604-03-3301-4', 'Kinh Tế Học Hài Hước',                                 'Steven D. Levitt',    3, 2020, 4, 4);

-- ============================================================
-- 3. Người dùng — Single Table Inheritance (≥ 6 người dùng)
--    password_hash mẫu (SHA-256):
--      'Admin@123'      → 6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090
--      'Lib@2025'       → 7e2a37e90b2b9f3f25c67a7b4d0b3d5a6e8c3f1d9b2e4a6c8d0e2f4a6b8c0d
--      (Giá trị trên là minh họa, dùng SHA2() trong production)
-- ============================================================
INSERT INTO users (user_code, username, password_hash, full_name, email, phone, role, status) VALUES
-- ADMIN (1 người)
('ADM001', 'admin',
 SHA2('Admin@123', 256),
 'Nguyễn Quản Trị', 'admin@library.edu.vn', '0988000001', 'ADMIN', 'ACTIVE'),

-- LIBRARIAN (1 người)
('LIB001', 'thuthu_mai',
 SHA2('Lib@2025', 256),
 'Trần Tuyết Mai', 'mai.tt@library.edu.vn', '0988000002', 'LIBRARIAN', 'ACTIVE'),

-- MEMBER (4 người)
('MBR001', NULL, NULL, 'Phạm Minh Tuấn',   'tuan.pm@student.edu.vn', '0901234567', 'MEMBER', 'ACTIVE'),
('MBR002', NULL, NULL, 'Lê Khánh Linh',    'linh.lk@student.edu.vn', '0912345678', 'MEMBER', 'ACTIVE'),
('MBR003', NULL, NULL, 'Đặng Quốc Huy',    'huy.dq@student.edu.vn',  '0923456789', 'MEMBER', 'ACTIVE'),
('MBR004', NULL, NULL, 'Vũ Bích Hạnh',     'hanh.vb@student.edu.vn', '0934567890', 'MEMBER', 'LOCKED');

-- ============================================================
-- 4. Phiếu mượn sách (≥ 5 phiếu, có đủ 3 trạng thái)
--    user_id  : ID của MEMBER (3=Tuấn, 4=Linh, 5=Huy, 6=Hạnh)
--    staff_id : ID của nhân viên xử lý (2=Mai)
-- ============================================================
INSERT INTO borrow_records (user_id, book_id, staff_id, borrow_date, due_date, return_date, status) VALUES
-- Đang mượn — BORROWED
(3, 1, 2, '2026-10-10', '2026-10-24', NULL,         'BORROWED'),
(4, 5, 2, '2026-10-12', '2026-10-26', NULL,         'BORROWED'),

-- Đã trả — RETURNED
(5, 3, 2, '2026-09-20', '2026-10-04', '2026-10-02', 'RETURNED'),
(3, 7, 2, '2026-09-25', '2026-10-09', '2026-10-07', 'RETURNED'),

-- Quá hạn — OVERDUE (chưa trả, quá ngày due_date)
(4, 2, 2, '2026-09-15', '2026-09-29', NULL,         'OVERDUE'),
(5, 4, 2, '2026-09-01', '2026-09-15', NULL,         'OVERDUE');

-- ============================================================
-- 5. Phiếu phạt (≥ 2 vé phạt, gắn với phiếu OVERDUE)
--    record_id 5 → OVERDUE (Lê Khánh Linh, book_id=2)
--    record_id 6 → OVERDUE (Đặng Quốc Huy,  book_id=4)
-- ============================================================
INSERT INTO fines (record_id, user_id, overdue_days, fine_amount, reason, status, paid_at) VALUES
-- Đã đóng phạt — PAID
(5, 4, 19, 38000.00,
 'Quá hạn trả sách 19 ngày (2.000 VNĐ/ngày)',
 'PAID', '2026-10-15 09:30:00'),

-- Chưa đóng phạt — UNPAID
(6, 5, 33, 66000.00,
 'Quá hạn trả sách 33 ngày (2.000 VNĐ/ngày)',
 'UNPAID', NULL);
