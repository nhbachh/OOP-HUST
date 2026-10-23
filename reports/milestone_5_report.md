# BÁO CÁO KẾT QUẢ THỰC HIỆN MILESTONE 5: MODERN SWING GUI — HUST RED EDITION
### Hệ Thống Quản Lý Thư Viện — HUST Lib (Lấy cảm hứng từ ĐH Bách Khoa Hà Nội)

- **Dự án:** Hệ thống Quản lý Thư viện (Library Management System — HUST Lib)
- **Học phần:** Lập trình Hướng đối tượng (Java + MySQL)
- **Đường dẫn thư mục dự án:** `/Users/apple/Documents/LibraryManagementSystem`
- **Ngày thực hiện:** 23/10/2026
- **Trạng thái tổng thể:** ✅ HOÀN THÀNH 100% (Giao diện đồ họa phẳng hiện đại HUST Red Edition, Bố cục Sidebar Navigation, Thẻ KPI, Status Pills bo tròn và liên kết Loose Coupling)

---

## 1. Bảng Kiểm Tra Tiến Độ Các Hạng Mục (Checklist)

| STT | Thành phần / Lớp | Vị trí Package | Trạng thái | Ghi chú kỹ thuật |
| :---: | :--- | :--- | :---: | :--- |
| 1 | `HustTheme.java` | `com.library.view` | **HOÀN THÀNH** | Hệ thống màu Bách Khoa: `PRIMARY_RED` (`#B91C1C`), `SIDEBAR_BG` (`#18181B`), `ACCENT_GOLD` (`#F59E0B`), cấu hình bo góc `Button.arc=12`, `Component.arc=10`, chiều cao dòng bảng 36px |
| 2 | `UIUtils.java` | `com.library.view` | **HOÀN THÀNH** | Nâng cấp `StatusPillRenderer` (vẽ viên thuốc trạng thái bo tròn), nút chính HUST Red `createPrimaryButton`, nút phụ `createSecondaryButton`, thẻ KPI `createStatCard`, format tiền tệ VNĐ |
| 3 | `LoginFrame.java` | `com.library.view` | **HOÀN THÀNH** | Split-Screen Card (820x520): Cột trái gradient Đỏ Bách Khoa có logo Sách & Bánh răng phát sáng; Cột phải form đăng nhập trắng tinh gọn kèm chip chọn nhanh tài khoản mẫu (Admin, Thủ thư, Độc giả) |
| 4 | `LibrarianDashboardFrame.java` | `com.library.view` | **HOÀN THÀNH** | Bàn làm việc Thủ thư: Bố cục Sidebar bên trái (Dark Burgundy), thanh Header trên với breadcrumb và thời gian, vùng nội dung dùng `CardLayout` chuyển đổi mượt mà (Tổng quan KPI, Quản lý Sách, Mượn ACID, Trả sách, Thu phạt) |
| 5 | `AdminDashboardFrame.java` | `com.library.view` | **HOÀN THÀNH** | Kế thừa `LibrarianDashboardFrame`: Bổ sung thêm 2 Card trên Sidebar (Quản trị Người Dùng & Thẻ, Báo cáo Thống kê toàn diện hệ thống với 6 thẻ KPI) |
| 6 | `MemberDashboardFrame.java` | `com.library.view` | **HOÀN THÀNH** | Cổng bạn đọc Bách Khoa: Header đỏ Bách Khoa sang trọng, tra cứu sách với Status Pills, quản lý phiếu mượn & vé phạt cá nhân, thẻ hồ sơ sinh viên |
| 7 | `Main.java` | `com.library` | **HOÀN THÀNH** | Gọi `HustTheme.setupFlatLaf()`, khởi chạy `LoginFrame` an toàn trong EDT; hỗ trợ cờ `--cli` cho ai thích dòng lệnh |
| 8 | Quản lý phiên bản Git | Repository gốc | **HOÀN THÀNH** | Commit toàn bộ thay đổi mã nguồn mới và cập nhật trạng thái làm việc sạch (`working tree clean`) |

---

## 2. Điểm Nhấn Nhận Diện Thương Hiệu Bách Khoa (HUST Brand Identity)

1. **Bảng màu nhận diện HUST (Color Palette):**
   - **Màu Đỏ Bách Khoa chủ đạo (`#B91C1C` / `#DC2626`):** Áp dụng cho nút CTA chính, đường kẻ viền nhấn, mục active trên Sidebar, và huy hiệu Admin.
   - **Màu Đen Đô Burgundy (`#18181B`):** Tạo nền thanh Sidebar sang trọng, tương phản cao, làm nổi bật các biểu tượng điều hướng.
   - **Màu Vàng Bánh Răng (`#F59E0B`):** Điểm xuyết tinh tế ở các logo, chỉ số KPI bản sách in và họa tiết compa kỹ thuật.
2. **Huy hiệu viên thuốc trạng thái (Status Pills):**
   - 🟢 `Còn sách` / `ACTIVE` / `PAID`: Xanh ngọc dịu (`#ECFDF5` / `#059669`)
   - 🟡 `Đang mượn`: Vàng hổ phách (`#FEF3C7` / `#D97706`)
   - 🔴 `Hết sách` / `LOCKED` / `OVERDUE` / `UNPAID`: Đỏ Bách Khoa nhạt (`#FEE2E2` / `#B91C1C`)
   - 🔴 `ADMIN`: Đỏ đô đậm (`#991B1B` chữ trắng)

---

## 3. Kiến Trúc Hướng Đối Tượng & Nguyên Tắc Loose Coupling

```
+-------------------------------------------------------------+
|                         VIEW LAYER                          |
|  LoginFrame (HUST Split Card)                               |
|       │                                                     |
|       ├──> MemberDashboardFrame (Cổng bạn đọc HUST)         |
|       └──> LibrarianDashboardFrame (Sidebar Dark Burgundy)   |
|                 ▲                                           |
|                 │ (Kế thừa - Inheritance)                   |
|            AdminDashboardFrame (+ Quản trị Người dùng & KPI)|
+-------------------------------------------------------------+
                              │
                              │ (Chỉ gọi Interface trừu tượng)
                              ▼
+-------------------------------------------------------------+
|                        SERVICE LAYER                        |
|  BookService, CategoryService, UserService,                 |
|  BorrowService, FineService                                 |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
|                          DAO LAYER                          |
|  BookDAO, CategoryDAO, UserDAO, BorrowRecordDAO, FineDAO    |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
|                      DATABASE (MySQL)                       |
|  library_db (InnoDB, Foreign Keys, Transactions)            |
+-------------------------------------------------------------+
```

---

## 4. Hướng Dẫn Khởi Chạy Ứng Dụng Mới

```bash
cd /Users/apple/Documents/LibraryManagementSystem
./run.sh
```
Hoặc bằng lệnh Maven:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home mvn -o exec:java -Dexec.mainClass="com.library.Main"
```

### Tài khoản mẫu thử nghiệm sẵn có trên màn hình Đăng nhập:
- **Admin:** `admin` / `Admin@123` (hoặc bấm vào chip `Admin` để tự động điền)
- **Thủ thư:** `thuthu_mai` / `Lib@2025` (hoặc bấm vào chip `Thủ thư`)
- **Độc giả:** `MBR001` (hoặc bấm vào chip `Độc giả`)
