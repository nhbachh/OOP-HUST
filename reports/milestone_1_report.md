# BÁO CÁO MILESTONE 1 — KHỞI TẠO CẤU TRÚC DỰ ÁN

- **Dự án:** Library Management System (Quản lý Thư viện)
- **Đường dẫn tuyệt đối:** `/Users/apple/Documents/LibraryManagementSystem`
- **Ngày thực hiện:** 18/10/2026
- **Môi trường:** JDK 27 · Apache Maven 3.10.0 · MySQL 26.7.0 (local)
- **Trạng thái tổng thể:** ✅ HOÀN THÀNH 100%

---

## 1. Checklist Các Nhiệm Vụ

| # | Nhiệm vụ | Trạng thái | Ghi chú |
| :-: | :--- | :-: | :--- |
| 1 | Tạo thư mục gốc `~/Documents/LibraryManagementSystem` | ✅ | `/Users/apple/Documents/LibraryManagementSystem` |
| 2 | Tạo `pom.xml` — Java 17, UTF-8, `mysql-connector-j:8.3.0` | ✅ | `maven-compiler-plugin 3.13.0` |
| 3 | Tạo thư mục `src/main/java/com/library/config` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 4 | Tạo thư mục `src/main/java/com/library/model` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 5 | Tạo thư mục `src/main/java/com/library/dao` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 6 | Tạo thư mục `src/main/java/com/library/service` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 7 | Tạo thư mục `src/main/java/com/library/view` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 8 | Tạo thư mục `sql/` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 9 | Tạo thư mục `reports/` + `.gitkeep` | ✅ | Đã xác nhận tồn tại |
| 10 | Tạo `README.md` với đầy đủ nội dung OOP, ERD, bảng CSDL | ✅ | Bao gồm Mermaid ERD, bảng mô tả 5 bảng |
| 11 | Tạo `sql/schema.sql` — 5 bảng, đủ ràng buộc, STI design | ✅ | Đã thực thi thành công trên MySQL |
| 12 | Tạo `sql/seed.sql` — dữ liệu mẫu tiếng Việt | ✅ | Đủ 5 bảng, đủ 3 trạng thái borrow/fine |
| 13 | `mvn validate` → `BUILD SUCCESS` | ✅ | 0.040s |
| 14 | `mvn clean compile` → `BUILD SUCCESS` | ✅ | 1.339s |
| 15 | Thực thi `schema.sql` + `seed.sql` trên MySQL cục bộ | ✅ | 5 bảng, đầy đủ dữ liệu |
| 16 | Kiểm thử ràng buộc `CHECK (available_copies <= total_copies)` | ✅ | Trả về `ERROR 3819` đúng như kỳ vọng |

---

## 2. Cây Thư Mục Thực Tế Đã Khởi Tạo

```
/Users/apple/Documents/LibraryManagementSystem
├── pom.xml
├── README.md
├── reports/
│   ├── .gitkeep
│   └── milestone_1_report.md       ← file này
├── sql/
│   ├── .gitkeep
│   ├── schema.sql
│   └── seed.sql
└── src/
    └── main/
        └── java/
            └── com/
                └── library/
                    ├── config/
                    │   └── .gitkeep
                    ├── dao/
                    │   └── .gitkeep
                    ├── model/
                    │   └── .gitkeep
                    ├── service/
                    │   └── .gitkeep
                    └── view/
                        └── .gitkeep
```

---

## 3. Kết Quả Maven — `mvn validate` và `mvn clean compile`

### `mvn validate`

```
[INFO] Scanning for projects...
[INFO] ----------------< com.library:LibraryManagementSystem >-----------------
[INFO] Building LibraryManagementSystem 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  0.040 s
[INFO] Finished at: 2026-10-18T10:34:25+07:00
```

### `mvn clean compile`

```
[INFO] Scanning for projects...
[INFO] ----------------< com.library:LibraryManagementSystem >-----------------
[INFO] Building LibraryManagementSystem 1.0.0
[INFO] --- clean:3.5.0:clean --- Deleting /target
[INFO] --- resources:3.5.0:resources --- skip non existing resourceDirectory
[INFO] --- compiler:3.13.0:compile --- Nothing to compile - all classes are up to date.
[INFO] BUILD SUCCESS
[INFO] Total time:  1.339 s
[INFO] Finished at: 2026-10-18T10:34:27+07:00
```

---

## 4. Kết Quả Thực Thi SQL trên MySQL Server

### 4.1. Lệnh thực thi
```bash
mysql -u root < sql/schema.sql   # → OK
mysql -u root < sql/seed.sql     # → OK
```

### 4.2. Kiểm tra số lượng bản ghi

| Bảng | Số bản ghi | Đáp ứng yêu cầu tối thiểu |
| :--- | :---: | :--- |
| `categories` | **5** | ✅ ≥ 4 |
| `books` | **10** | ✅ ≥ 8 |
| `users` | **6** | ✅ ≥ 6 (1 ADMIN, 1 LIBRARIAN, 4 MEMBER) |
| `borrow_records` | **6** | ✅ ≥ 5 (BORROWED × 2, RETURNED × 2, OVERDUE × 2) |
| `fines` | **2** | ✅ ≥ 2 (1 PAID, 1 UNPAID) |

### 4.3. Kiểm tra tính toàn vẹn quan hệ (JOIN đa bảng)

```
+-----------+--------------------+-------------------------------------------------------+---------------+-------------+-------------+
| record_id | member             | title                                                 | borrow_status | fine_amount | fine_status |
+-----------+--------------------+-------------------------------------------------------+---------------+-------------+-------------+
|         1 | Phạm Minh Tuấn     | Lập Trình Java Căn Bản và Nâng Cao                    | BORROWED      |        NULL | NULL        |
|         2 | Lê Khánh Linh      | Nhà Giả Kim                                           | BORROWED      |        NULL | NULL        |
|         3 | Đặng Quốc Huy      | Thiết Kế Mẫu (Design Patterns) Thực Chiến             | RETURNED      |        NULL | NULL        |
|         4 | Phạm Minh Tuấn     | Đắc Nhân Tâm                                          | RETURNED      |        NULL | NULL        |
|         5 | Lê Khánh Linh      | Cấu Trúc Dữ Liệu và Giải Thuật với Java               | OVERDUE       |    38000.00 | PAID        |
|         6 | Đặng Quốc Huy      | Clean Code — Mã Sạch Trong Lập Trình                  | OVERDUE       |    66000.00 | UNPAID      |
+-----------+--------------------+-------------------------------------------------------+---------------+-------------+-------------+
```

### 4.4. Kiểm thử ràng buộc toàn vẹn (CHECK Constraint)

Thử chèn bản ghi vi phạm `available_copies > total_copies`:

```sql
INSERT INTO books (..., total_copies, available_copies) VALUES (..., 5, 10);
```

**Kết quả MySQL:**
```
ERROR 3819 (HY000): Check constraint 'chk_books_copies' is violated.
```
→ Ràng buộc `CHECK (available_copies >= 0 AND available_copies <= total_copies)` hoạt động **chính xác**.

---

## 5. Kết Luận

Milestone 1 đã hoàn thành toàn diện, đúng phạm vi, không vượt sang Milestone tiếp theo. Toàn bộ hạ tầng dự án (cấu trúc thư mục Maven, tài liệu README, script CSDL và dữ liệu mẫu) đã sẵn sàng làm nền tảng cho **Milestone 2: Triển khai tầng Model & DAO**.
