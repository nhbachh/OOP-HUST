package com.library.javafx.views;

import com.library.javafx.JavaFxTheme;
import com.library.model.*;
import com.library.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Bàn Làm Việc Thủ Thư JavaFX kết nối nghiệp vụ thực tế (LibrarianDashboardView).
 * Tích hợp đầy đủ: Quản lý kho sách (CRUD), Nghiệp vụ Mượn/Trả sách (ACID), Xử lý nộp tiền phạt.
 */
public class LibrarianDashboardView extends BorderPane {

    private final Staff staff;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;
    private final Runnable onLogout;

    private final Map<Integer, String> categoryMap = new HashMap<>();

    // Components tab Sách
    private TableView<Book> bookTable;
    private ObservableList<Book> bookList;
    private TextField txtIsbn;
    private TextField txtTitle;
    private TextField txtAuthor;
    private ComboBox<CategoryItem> cboCategory;
    private TextField txtYear;
    private TextField txtCopies;
    private Button btnAddBook;
    private Button btnUpdateBook;
    private Button btnDeleteBook;
    private Label lblBookCount;
    private Book selectedBook = null;

    // Components tab Mượn / Trả
    private TextField txtBorrowMemberCode;
    private TextField txtBorrowBookIdOrIsbn;
    private Spinner<Integer> spinBorrowDays;
    private TableView<BorrowRecord> activeBorrowTable;
    private ObservableList<BorrowRecord> activeBorrowList;
    private TextField txtReturnRecordId;

    // Components tab Tiền Phạt
    private TableView<Fine> fineTable;
    private ObservableList<Fine> fineList;
    private Label lblTotalUnpaidFine;

    /**
     * Lớp hiển thị Thể loại trong ComboBox
     */
    public static class CategoryItem {
        private final int id;
        private final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    /**
     * Constructor đầy đủ tham số khi chạy trong hệ thống thật.
     */
    public LibrarianDashboardView(Staff staff,
                                  BookService bookService,
                                  CategoryService categoryService,
                                  BorrowService borrowService,
                                  FineService fineService,
                                  UserService userService,
                                  Runnable onLogout) {
        this.staff = staff != null ? staff : createDefaultMockStaff();
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;
        this.onLogout = onLogout;

        setStyle("-fx-background-color: " + JavaFxTheme.COLOR_BG_LIGHT + ";");
        loadCategoryCache();

        // 1. Top App Bar
        setTop(createTopBar());

        // 2. TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-min-width: 150px; -fx-tab-min-height: 38px;");

        tabPane.getTabs().add(createBookManagementTab());
        tabPane.getTabs().add(createBorrowReturnTab());
        tabPane.getTabs().add(createFinesTab());

        setCenter(tabPane);

        // Nạp dữ liệu ban đầu
        refreshBooks();
        refreshActiveBorrows();
        refreshFines();
    }

    /**
     * Constructor không tham số phục vụ Demo Runner.
     */
    public LibrarianDashboardView() {
        this(null, null, null, null, null, null, null);
    }

    private static Staff createDefaultMockStaff() {
        Staff s = new Staff("LIB001", "librarian", "hash", "Trần Thị Thu Thư", "librarian@hust.edu.vn", "0912345678", Role.LIBRARIAN, "Ban Thủ Thư");
        s.setUserId(2);
        return s;
    }

    private void loadCategoryCache() {
        categoryMap.clear();
        if (categoryService != null) {
            try {
                List<Category> cats = categoryService.getAllCategories();
                for (Category c : cats) {
                    categoryMap.put(c.getCategoryId(), c.getName());
                }
            } catch (Exception ignored) {
            }
        }
        if (categoryMap.isEmpty()) {
            categoryMap.put(1, "Công nghệ thông tin");
            categoryMap.put(2, "Toán - Tin ứng dụng");
            categoryMap.put(3, "Khoa học máy tính");
            categoryMap.put(4, "Kinh tế & Quản lý");
            categoryMap.put(5, "Ngoại ngữ - Tiếng Anh");
        }
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(16);
        topBar.setPadding(new Insets(12, 24, 12, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-border-color: " + JavaFxTheme.COLOR_BORDER + ";" +
                "-fx-border-width: 0 0 1 0;"
        );

        Label lblLogo = new Label("📚");
        lblLogo.setFont(Font.font("Segoe UI Emoji, Apple Color Emoji", 22));

        VBox brandBox = new VBox(2);
        Label lblAppName = new Label("HUST Lib — Bàn Làm Việc Thủ Thư");
        lblAppName.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblAppName.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Label lblSub = new Label("Quản lý ấn phẩm, điều phối mượn trả & thu nợ thư viện");
        lblSub.setFont(Font.font("System", 11));
        lblSub.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");
        brandBox.getChildren().addAll(lblAppName, lblSub);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label badgeRole = new Label("THỦ THƯ");
        badgeRole.setStyle(
                "-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-border-color: #BFDBFE; " +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 3 10 3 10; -fx-font-size: 11px; -fx-font-weight: bold;"
        );

        Label lblUser = new Label(staff.getFullName() + " (" + staff.getUserCode() + ")");
        lblUser.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        lblUser.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnLogout.setOnAction(e -> {
            if (onLogout != null) {
                if (JavaFxTheme.showConfirm("Xác nhận đăng xuất", "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
                    onLogout.run();
                }
            }
        });

        topBar.getChildren().addAll(lblLogo, brandBox, topSpacer, badgeRole, lblUser, btnLogout);
        return topBar;
    }

    // =========================================================================
    // TAB 1: QUẢN LÝ KHO SÁCH (CRUD)
    // =========================================================================
    private Tab createBookManagementTab() {
        Tab tab = new Tab("📚 Quản Lý Kho Sách");
        HBox root = new HBox(16);
        root.setPadding(new Insets(16, 20, 16, 20));

        // CỘT TRÁI: FORM NHẬP LIỆU CHI TIẾT SÁCH (340px)
        VBox formCard = new VBox(10);
        formCard.setPrefWidth(340);
        formCard.setStyle(JavaFxTheme.CSS_CARD);

        Label lblFormHead = new Label("CHI TIẾT ĐẦU SÁCH");
        lblFormHead.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblFormHead.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        txtIsbn = new TextField();
        txtIsbn.setPromptText("VD: 978-604-01-2345-6");
        txtIsbn.setStyle(JavaFxTheme.CSS_INPUT);

        txtTitle = new TextField();
        txtTitle.setPromptText("VD: Lập trình Java cơ bản");
        txtTitle.setStyle(JavaFxTheme.CSS_INPUT);

        txtAuthor = new TextField();
        txtAuthor.setPromptText("VD: Nguyễn Văn A");
        txtAuthor.setStyle(JavaFxTheme.CSS_INPUT);

        cboCategory = new ComboBox<>();
        cboCategory.setMaxWidth(Double.MAX_VALUE);
        cboCategory.setStyle(JavaFxTheme.CSS_INPUT);
        populateCategoriesCombo();

        txtYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        txtYear.setPromptText("Năm xuất bản");
        txtYear.setStyle(JavaFxTheme.CSS_INPUT);

        txtCopies = new TextField("5");
        txtCopies.setPromptText("Tổng số bản in");
        txtCopies.setStyle(JavaFxTheme.CSS_INPUT);

        btnAddBook = new Button("+ Thêm Đầu Sách Mới");
        btnAddBook.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnAddBook.setMaxWidth(Double.MAX_VALUE);
        btnAddBook.setOnAction(e -> handleAddBook());

        HBox updateDeleteBox = new HBox(8);
        btnUpdateBook = new Button("Cập Nhật");
        btnUpdateBook.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnUpdateBook.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnUpdateBook, Priority.ALWAYS);
        btnUpdateBook.setDisable(true);
        btnUpdateBook.setOnAction(e -> handleUpdateBook());

        btnDeleteBook = new Button("Xóa Sách");
        btnDeleteBook.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-border-color: #FECACA; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 7 12 7 12; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDeleteBook.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDeleteBook, Priority.ALWAYS);
        btnDeleteBook.setDisable(true);
        btnDeleteBook.setOnAction(e -> handleDeleteBook());

        updateDeleteBox.getChildren().addAll(btnUpdateBook, btnDeleteBook);

        Button btnClearForm = new Button("Làm Mới Form");
        btnClearForm.setMaxWidth(Double.MAX_VALUE);
        btnClearForm.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnClearForm.setOnAction(e -> clearBookForm());

        formCard.getChildren().addAll(
                lblFormHead,
                createFieldLabel("Mã ISBN:"), txtIsbn,
                createFieldLabel("Tiêu đề sách:"), txtTitle,
                createFieldLabel("Tác giả:"), txtAuthor,
                createFieldLabel("Thể loại:"), cboCategory,
                createFieldLabel("Năm xuất bản:"), txtYear,
                createFieldLabel("Tổng bản in:"), txtCopies,
                new Region() {{ setMinHeight(4); }},
                btnAddBook,
                updateDeleteBox,
                btnClearForm
        );

        // CỘT PHẢI: BẢNG DANH MỤC SÁCH
        VBox tableCard = new VBox(10);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        tableCard.setStyle(JavaFxTheme.CSS_CARD);

        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Gõ tên sách, tác giả hoặc ISBN để lọc tức thì...");
        txtSearch.setStyle(JavaFxTheme.CSS_INPUT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, oldV, newV) -> filterBooks(newV));

        lblBookCount = new Label("Tổng cộng: 0 đầu sách");
        lblBookCount.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button btnReloadBooks = new Button("Tải Lại");
        btnReloadBooks.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnReloadBooks.setOnAction(e -> refreshBooks());

        searchBar.getChildren().addAll(txtSearch, lblBookCount, btnReloadBooks);

        bookTable = new TableView<>();
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        TableColumn<Book, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBookId())));
        colId.setPrefWidth(50);

        TableColumn<Book, String> colIsbn = new TableColumn<>("Mã ISBN");
        colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        colIsbn.setPrefWidth(120);

        TableColumn<Book, String> colTitle = new TableColumn<>("Tiêu đề sách");
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colTitle.setPrefWidth(240);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Tác giả");
        colAuthor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        colAuthor.setPrefWidth(140);

        TableColumn<Book, String> colCat = new TableColumn<>("Thể loại");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(categoryMap.getOrDefault(d.getValue().getCategoryId(), "Chung")));
        colCat.setPrefWidth(130);

        TableColumn<Book, String> colYear = new TableColumn<>("Năm");
        colYear.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPublishYear())));
        colYear.setPrefWidth(60);

        TableColumn<Book, String> colCopies = new TableColumn<>("Tồn / Tổng");
        colCopies.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAvailableCopies() + " / " + d.getValue().getTotalCopies()));
        colCopies.setPrefWidth(90);

        TableColumn<Book, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAvailable() ? "Sẵn sàng" : "Hết sách"));
        colStatus.setPrefWidth(90);

        bookTable.getColumns().addAll(colId, colIsbn, colTitle, colAuthor, colCat, colYear, colCopies, colStatus);

        bookList = FXCollections.observableArrayList();
        bookTable.setItems(bookList);

        // Click hàng -> đưa dữ liệu vào form bên trái
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldB, b) -> {
            if (b != null) {
                selectedBook = b;
                txtIsbn.setText(b.getIsbn());
                txtTitle.setText(b.getTitle());
                txtAuthor.setText(b.getAuthor());
                txtYear.setText(String.valueOf(b.getPublishYear()));
                txtCopies.setText(String.valueOf(b.getTotalCopies()));

                for (CategoryItem item : cboCategory.getItems()) {
                    if (item.getId() == b.getCategoryId()) {
                        cboCategory.setValue(item);
                        break;
                    }
                }
                btnAddBook.setDisable(true);
                btnUpdateBook.setDisable(false);
                btnDeleteBook.setDisable(false);
            }
        });

        tableCard.getChildren().addAll(searchBar, bookTable);
        root.getChildren().addAll(formCard, tableCard);
        tab.setContent(root);
        return tab;
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");
        return lbl;
    }

    private void populateCategoriesCombo() {
        cboCategory.getItems().clear();
        for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
            cboCategory.getItems().add(new CategoryItem(entry.getKey(), entry.getValue()));
        }
        if (!cboCategory.getItems().isEmpty()) {
            cboCategory.getSelectionModel().select(0);
        }
    }

    private void clearBookForm() {
        selectedBook = null;
        txtIsbn.clear();
        txtTitle.clear();
        txtAuthor.clear();
        txtYear.setText(String.valueOf(LocalDate.now().getYear()));
        txtCopies.setText("5");
        btnAddBook.setDisable(false);
        btnUpdateBook.setDisable(true);
        btnDeleteBook.setDisable(true);
        bookTable.getSelectionModel().clearSelection();
    }

    private void handleAddBook() {
        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        CategoryItem cat = cboCategory.getValue();
        String yearStr = txtYear.getText().trim();
        String copiesStr = txtCopies.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || cat == null) {
            JavaFxTheme.showError("Lỗi nhập liệu", "Vui lòng nhập đầy đủ Mã ISBN, Tiêu đề, Tác giả và Thể loại!");
            return;
        }

        int year;
        int copies;
        try {
            year = Integer.parseInt(yearStr);
            copies = Integer.parseInt(copiesStr);
            if (copies <= 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            JavaFxTheme.showError("Lỗi số liệu", "Năm xuất bản và số bản in phải là số nguyên dương hợp lệ!");
            return;
        }

        if (bookService != null) {
            try {
                bookService.createBook(isbn, title, author, cat.getId(), year, copies);
                JavaFxTheme.showInfo("Thành công", "Đã thêm mới đầu sách: " + title);
                refreshBooks();
                clearBookForm();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi thêm sách", ex.getMessage());
            }
        } else {
            // Mock mode
            Book mock = new Book(bookList.size() + 1, isbn, title, author, cat.getId(), year, copies, copies);
            bookList.add(mock);
            lblBookCount.setText("Tổng cộng: " + bookList.size() + " đầu sách");
            JavaFxTheme.showInfo("Demo", "Đã thêm đầu sách mô phỏng: " + title);
            clearBookForm();
        }
    }

    private void handleUpdateBook() {
        if (selectedBook == null) return;
        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        CategoryItem cat = cboCategory.getValue();
        String yearStr = txtYear.getText().trim();
        String copiesStr = txtCopies.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || cat == null) {
            JavaFxTheme.showError("Lỗi nhập liệu", "Vui lòng nhập đầy đủ các trường thông tin!");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int newTotal = Integer.parseInt(copiesStr);

            selectedBook.setIsbn(isbn);
            selectedBook.setTitle(title);
            selectedBook.setAuthor(author);
            selectedBook.setCategoryId(cat.getId());
            selectedBook.setPublishYear(year);

            // Cập nhật số lượng
            int diff = newTotal - selectedBook.getTotalCopies();
            int newAvailable = selectedBook.getAvailableCopies() + diff;
            if (newAvailable < 0) {
                JavaFxTheme.showError("Lỗi số lượng", "Tổng số sách mới không thể nhỏ hơn số sách đang khả dụng hiện tại!");
                return;
            }
            selectedBook.setCopies(newTotal, newAvailable);

            if (bookService != null) {
                bookService.updateBook(selectedBook);
            }
            JavaFxTheme.showInfo("Thành công", "Đã cập nhật thông tin sách thành công!");
            refreshBooks();
            clearBookForm();
        } catch (Exception ex) {
            JavaFxTheme.showError("Lỗi cập nhật", ex.getMessage());
        }
    }

    private void handleDeleteBook() {
        if (selectedBook == null) return;
        boolean confirm = JavaFxTheme.showConfirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa đầu sách '" + selectedBook.getTitle() + "' khỏi hệ thống?");
        if (!confirm) return;

        if (bookService != null) {
            try {
                bookService.deleteBook(selectedBook.getBookId());
                JavaFxTheme.showInfo("Thành công", "Đã xóa đầu sách thành công!");
                refreshBooks();
                clearBookForm();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi xóa sách", ex.getMessage());
            }
        } else {
            bookList.remove(selectedBook);
            lblBookCount.setText("Tổng cộng: " + bookList.size() + " đầu sách");
            clearBookForm();
        }
    }

    private void filterBooks(String query) {
        if (bookService != null) {
            try {
                List<Book> filtered;
                if (query == null || query.trim().isEmpty()) {
                    filtered = bookService.getAllBooks();
                } else {
                    filtered = bookService.searchBooks(query.trim());
                }
                bookList.setAll(filtered);
                lblBookCount.setText("Tổng cộng: " + bookList.size() + " đầu sách");
            } catch (Exception ignored) {
            }
        }
    }

    private void refreshBooks() {
        if (bookService != null) {
            try {
                List<Book> all = bookService.getAllBooks();
                bookList.setAll(all);
                lblBookCount.setText("Tổng cộng: " + all.size() + " đầu sách");
            } catch (Exception ex) {
                lblBookCount.setText("Lỗi nạp sách");
            }
        } else {
            // Mock sample
            bookList.setAll(
                    new Book(1, "978-604-01-2", "Giải Tích 1 — ĐHBK", "Nguyễn Đình Trí", 2, 2023, 10, 8),
                    new Book(2, "978-604-01-3", "Vật Lý Đại Cương I", "Lương Duyên Bình", 2, 2022, 15, 12),
                    new Book(3, "978-604-01-4", "Lập Trình Hướng Đối Tượng Java", "Trần Đình Quế", 1, 2023, 5, 5)
            );
            lblBookCount.setText("Tổng cộng: " + bookList.size() + " đầu sách");
        }
    }

    // =========================================================================
    // TAB 2: NGHIỆP VỤ MƯỢN / TRẢ SÁCH
    // =========================================================================
    private Tab createBorrowReturnTab() {
        Tab tab = new Tab("🔄 Mượn & Trả Sách");
        VBox root = new VBox(16);
        root.setPadding(new Insets(16, 20, 16, 20));

        // 2 Khối trên: Thẻ Tạo Phiếu Mượn + Thẻ Tiếp Nhận Trả Sách
        HBox formsBox = new HBox(16);

        // THẺ MƯỢN SÁCH
        VBox borrowCard = new VBox(10);
        borrowCard.setStyle(JavaFxTheme.CSS_CARD);
        HBox.setHgrow(borrowCard, Priority.ALWAYS);

        Label lblHead1 = new Label("TẠO PHIẾU MƯỢN MỚI");
        lblHead1.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblHead1.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        txtBorrowMemberCode = new TextField();
        txtBorrowMemberCode.setPromptText("Mã thẻ / MSSV độc giả (VD: SV20201234)");
        txtBorrowMemberCode.setStyle(JavaFxTheme.CSS_INPUT);

        txtBorrowBookIdOrIsbn = new TextField();
        txtBorrowBookIdOrIsbn.setPromptText("Mã ISBN hoặc ID sách cần mượn");
        txtBorrowBookIdOrIsbn.setStyle(JavaFxTheme.CSS_INPUT);

        HBox daysBox = new HBox(8);
        daysBox.setAlignment(Pos.CENTER_LEFT);
        Label lblDays = new Label("Thời hạn mượn:");
        lblDays.setFont(Font.font("System", FontWeight.BOLD, 11));
        spinBorrowDays = new Spinner<>(1, 60, 14);
        spinBorrowDays.setStyle(JavaFxTheme.CSS_INPUT);
        spinBorrowDays.setPrefWidth(80);
        Label lblDaysUnit = new Label("ngày (mặc định 14 ngày)");
        lblDaysUnit.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + "; -fx-font-size: 11px;");
        daysBox.getChildren().addAll(lblDays, spinBorrowDays, lblDaysUnit);

        Button btnBorrow = new Button("✓ Xác Nhận Cho Mượn");
        btnBorrow.setMaxWidth(Double.MAX_VALUE);
        btnBorrow.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnBorrow.setOnAction(e -> handleBorrowBook());

        borrowCard.getChildren().addAll(
                lblHead1,
                createFieldLabel("Mã độc giả / MSSV:"), txtBorrowMemberCode,
                createFieldLabel("Mã tài liệu / ISBN:"), txtBorrowBookIdOrIsbn,
                daysBox,
                new Region() {{ setMinHeight(4); }},
                btnBorrow
        );

        // THẺ TRẢ SÁCH
        VBox returnCard = new VBox(10);
        returnCard.setStyle(JavaFxTheme.CSS_CARD);
        HBox.setHgrow(returnCard, Priority.ALWAYS);

        Label lblHead2 = new Label("TIẾP NHẬN TRẢ SÁCH VÀO KHO");
        lblHead2.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblHead2.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_SUCCESS_GREEN + ";");

        txtReturnRecordId = new TextField();
        txtReturnRecordId.setPromptText("Nhập Mã phiếu mượn (#Record ID) hoặc chọn từ bảng bên dưới");
        txtReturnRecordId.setStyle(JavaFxTheme.CSS_INPUT);

        Label lblReturnGuide = new Label("• Nếu quá hạn trả, hệ thống sẽ tự động tính phí 2.000đ/ngày và sinh vé phạt.\n• Chọn trực tiếp phiếu mượn ở bảng bên dưới để điền nhanh mã ID.");
        lblReturnGuide.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + "; -fx-font-size: 11px;");
        lblReturnGuide.setWrapText(true);

        Button btnReturn = new Button("✓ Hoàn Tất Trả Sách Vào Kho");
        btnReturn.setMaxWidth(Double.MAX_VALUE);
        btnReturn.setStyle("-fx-background-color: " + JavaFxTheme.COLOR_SUCCESS_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnReturn.setOnAction(e -> handleReturnBook());

        returnCard.getChildren().addAll(
                lblHead2,
                createFieldLabel("Mã phiếu mượn (#ID):"), txtReturnRecordId,
                lblReturnGuide,
                new Region() {{ setMinHeight(6); }},
                btnReturn
        );

        formsBox.getChildren().addAll(borrowCard, returnCard);

        // KHỐI DƯỚI: BẢNG DANH SÁCH ĐANG MƯỢN (ACTIVE BORROW RECORDS)
        VBox activeBorrowCard = new VBox(10);
        activeBorrowCard.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(activeBorrowCard, Priority.ALWAYS);

        HBox subHead = new HBox(12);
        subHead.setAlignment(Pos.CENTER_LEFT);
        Label lblActiveTitle = new Label("DANH SÁCH PHIẾU ĐANG MƯỢN (CHƯA TRẢ)");
        lblActiveTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblActiveTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Region spacerActive = new Region();
        HBox.setHgrow(spacerActive, Priority.ALWAYS);

        Button btnReloadBorrows = new Button("Làm Mới Danh Sách");
        btnReloadBorrows.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnReloadBorrows.setOnAction(e -> refreshActiveBorrows());

        subHead.getChildren().addAll(lblActiveTitle, spacerActive, btnReloadBorrows);

        activeBorrowTable = new TableView<>();
        VBox.setVgrow(activeBorrowTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colRecId = new TableColumn<>("Mã Phiếu");
        colRecId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRecId.setPrefWidth(90);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("Mã Độc Giả (ID)");
        colUser.setCellValueFactory(d -> new SimpleStringProperty(getUserLabel(d.getValue().getUserId())));
        colUser.setPrefWidth(160);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Sách (ID)");
        colBook.setCellValueFactory(d -> new SimpleStringProperty(getBookLabel(d.getValue().getBookId())));
        colBook.setPrefWidth(240);

        TableColumn<BorrowRecord, String> colBDate = new TableColumn<>("Ngày Mượn");
        colBDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBorrowDate().toString()));
        colBDate.setPrefWidth(110);

        TableColumn<BorrowRecord, String> colDDate = new TableColumn<>("Hạn Trả");
        colDDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDueDate().toString()));
        colDDate.setPrefWidth(110);

        TableColumn<BorrowRecord, String> colDaysLeft = new TableColumn<>("Thời Gian");
        colDaysLeft.setCellValueFactory(d -> {
            LocalDate due = d.getValue().getDueDate();
            LocalDate now = LocalDate.now();
            long diff = ChronoUnit.DAYS.between(now, due);
            if (diff < 0) {
                return new SimpleStringProperty("QUÁ HẠN " + Math.abs(diff) + " ngày");
            } else if (diff == 0) {
                return new SimpleStringProperty("HÔM NAY");
            } else {
                return new SimpleStringProperty("Còn " + diff + " ngày");
            }
        });
        colDaysLeft.setPrefWidth(130);

        activeBorrowTable.getColumns().addAll(colRecId, colUser, colBook, colBDate, colDDate, colDaysLeft);
        activeBorrowList = FXCollections.observableArrayList();
        activeBorrowTable.setItems(activeBorrowList);

        // Click hàng -> điền ID vào form trả sách
        activeBorrowTable.getSelectionModel().selectedItemProperty().addListener((obs, oldR, r) -> {
            if (r != null) {
                txtReturnRecordId.setText(String.valueOf(r.getRecordId()));
            }
        });

        activeBorrowCard.getChildren().addAll(subHead, activeBorrowTable);

        root.getChildren().addAll(formsBox, activeBorrowCard);
        tab.setContent(root);
        return tab;
    }

    private String getUserLabel(int userId) {
        if (userService != null) {
            try {
                User u = userService.getUserById(userId);
                if (u != null) return u.getUserCode() + " (" + u.getFullName() + ")";
            } catch (Exception ignored) {
            }
        }
        return "User #" + userId;
    }

    private String getBookLabel(int bookId) {
        if (bookService != null) {
            try {
                Book b = bookService.getBookById(bookId);
                if (b != null) return b.getTitle();
            } catch (Exception ignored) {
            }
        }
        return "Book #" + bookId;
    }

    private void handleBorrowBook() {
        String memberCode = txtBorrowMemberCode.getText().trim();
        String bookCode = txtBorrowBookIdOrIsbn.getText().trim();
        int days = spinBorrowDays.getValue();

        if (memberCode.isEmpty() || bookCode.isEmpty()) {
            JavaFxTheme.showError("Thiếu thông tin", "Vui lòng nhập Mã độc giả và Mã sách/ISBN!");
            return;
        }

        if (borrowService == null || userService == null || bookService == null) {
            JavaFxTheme.showInfo("Demo", "Mô phỏng mượn sách thành công cho " + memberCode);
            return;
        }

        try {
            // 1. Tìm độc giả
            User user = userService.getUserByCode(memberCode);
            if (user == null) {
                try {
                    int uid = Integer.parseInt(memberCode);
                    user = userService.getUserById(uid);
                } catch (NumberFormatException ignored) {}
            }
            if (user == null) {
                JavaFxTheme.showError("Không tìm thấy", "Không tìm thấy bạn đọc với mã: " + memberCode);
                return;
            }

            // 2. Tìm sách
            Book book = bookService.getBookByIsbn(bookCode);
            if (book == null) {
                try {
                    int bid = Integer.parseInt(bookCode);
                    book = bookService.getBookById(bid);
                } catch (NumberFormatException ignored) {}
            }
            if (book == null) {
                JavaFxTheme.showError("Không tìm thấy", "Không tìm thấy đầu sách với mã/ISBN: " + bookCode);
                return;
            }

            // 3. Thực hiện mượn
            BorrowRecord rec = borrowService.borrowBook(user.getUserId(), book.getBookId(), staff.getUserId(), days);
            JavaFxTheme.showInfo("Mượn sách thành công",
                    "Đã tạo phiếu mượn #" + rec.getRecordId() + "\n" +
                    "• Độc giả: " + user.getFullName() + " (" + user.getUserCode() + ")\n" +
                    "• Đầu sách: " + book.getTitle() + "\n" +
                    "• Hạn trả: " + rec.getDueDate());

            txtBorrowMemberCode.clear();
            txtBorrowBookIdOrIsbn.clear();
            refreshBooks();
            refreshActiveBorrows();
        } catch (Exception ex) {
            JavaFxTheme.showError("Không thể tạo phiếu mượn", ex.getMessage());
        }
    }

    private void handleReturnBook() {
        String recIdStr = txtReturnRecordId.getText().trim();
        if (recIdStr.isEmpty()) {
            JavaFxTheme.showError("Thiếu thông tin", "Vui lòng nhập Mã phiếu mượn hoặc chọn một dòng từ bảng bên dưới!");
            return;
        }

        int recId;
        try {
            recId = Integer.parseInt(recIdStr.replace("#", ""));
        } catch (Exception ex) {
            JavaFxTheme.showError("Lỗi định dạng", "Mã phiếu mượn phải là số nguyên!");
            return;
        }

        if (borrowService == null) {
            JavaFxTheme.showInfo("Demo", "Mô phỏng trả sách thành công cho phiếu #" + recId);
            return;
        }

        try {
            borrowService.returnBook(recId);
            Fine fine = fineService != null ? fineService.getFineByRecordId(recId) : null;

            if (fine != null && fine.getStatus() == FineStatus.UNPAID) {
                JavaFxTheme.showInfo("Đã tiếp nhận trả sách (CÓ PHẠT)",
                        "Tiếp nhận trả sách thành công!\n" +
                        "⚠️ Phiếu mượn này quá hạn " + fine.getOverdueDays() + " ngày.\n" +
                        "Hệ thống đã phát sinh vé phạt: " + JavaFxTheme.formatCurrency(fine.getFineAmount()) + ".\n" +
                        "Vui lòng chuyển qua tab 'Xử Lý Tiền Phạt' để thu phí từ bạn đọc.");
            } else {
                JavaFxTheme.showInfo("Trả sách thành công", "Đã hoàn tất trả sách và nhập lại kho ấn phẩm!");
            }

            txtReturnRecordId.clear();
            refreshBooks();
            refreshActiveBorrows();
            refreshFines();
        } catch (Exception ex) {
            JavaFxTheme.showError("Không thể trả sách", ex.getMessage());
        }
    }

    private void refreshActiveBorrows() {
        if (borrowService != null) {
            try {
                List<BorrowRecord> all = borrowService.getAllBorrowRecords();
                activeBorrowList.clear();
                for (BorrowRecord r : all) {
                    if (r.getStatus() == BorrowStatus.BORROWED) {
                        activeBorrowList.add(r);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================================
    // TAB 3: XỬ LÝ TIỀN PHẠT
    // =========================================================================
    private Tab createFinesTab() {
        Tab tab = new Tab("💳 Xử Lý Tiền Phạt");
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));

        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblHead = new Label("QUẢN LÝ TIỀN PHẠT QUÁ HẠN & BỒI THƯỜNG");
        lblHead.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblHead.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Label lblSub = new Label("Thu tiền phạt phát sinh do trễ hạn trả sách hoặc làm hỏng/mất tài liệu");
        lblSub.setFont(Font.font("System", 11));
        lblSub.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(lblHead, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblTotalUnpaidFine = new Label("Tổng phạt chưa thu: 0 VNĐ");
        lblTotalUnpaidFine.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 10 4 10; -fx-background-color: #FEE2E2; -fx-background-radius: 6;");

        Button btnPayFine = new Button("💰 Thu Tiền Phạt");
        btnPayFine.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnPayFine.setOnAction(e -> handlePayFine());

        Button btnReloadFines = new Button("Tải Lại");
        btnReloadFines.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnReloadFines.setOnAction(e -> refreshFines());

        headerBox.getChildren().addAll(titleBox, spacer, lblTotalUnpaidFine, btnPayFine, btnReloadFines);

        fineTable = new TableView<>();
        VBox.setVgrow(fineTable, Priority.ALWAYS);

        TableColumn<Fine, String> colFineId = new TableColumn<>("Mã Phạt");
        colFineId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getFineId()));
        colFineId.setPrefWidth(80);

        TableColumn<Fine, String> colRecId = new TableColumn<>("Mã Phiếu Mượn");
        colRecId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRecId.setPrefWidth(120);

        TableColumn<Fine, String> colUser = new TableColumn<>("Độc Giả");
        colUser.setCellValueFactory(d -> new SimpleStringProperty(getUserLabel(d.getValue().getUserId())));
        colUser.setPrefWidth(180);

        TableColumn<Fine, String> colDays = new TableColumn<>("Quá Hạn");
        colDays.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOverdueDays() + " ngày"));
        colDays.setPrefWidth(90);

        TableColumn<Fine, String> colAmount = new TableColumn<>("Số Tiền Phạt");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(JavaFxTheme.formatCurrency(d.getValue().getFineAmount())));
        colAmount.setPrefWidth(120);

        TableColumn<Fine, String> colReason = new TableColumn<>("Lý Do");
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason() != null ? d.getValue().getReason() : "Trễ hạn quy định"));
        colReason.setPrefWidth(220);

        TableColumn<Fine, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus() == FineStatus.PAID ? "Đã nộp" : "CHƯA NỘP"));
        colStatus.setPrefWidth(110);

        fineTable.getColumns().addAll(colFineId, colRecId, colUser, colDays, colAmount, colReason, colStatus);
        fineList = FXCollections.observableArrayList();
        fineTable.setItems(fineList);

        root.getChildren().addAll(headerBox, fineTable);
        tab.setContent(root);
        return tab;
    }

    private void handlePayFine() {
        Fine sel = fineTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JavaFxTheme.showError("Chưa chọn vé phạt", "Vui lòng chọn một khoản phạt chưa nộp trong bảng để thu tiền!");
            return;
        }

        if (sel.getStatus() == FineStatus.PAID) {
            JavaFxTheme.showInfo("Thông báo", "Khoản phạt này đã được nộp trước đó.");
            return;
        }

        boolean confirm = JavaFxTheme.showConfirm("Xác nhận thu tiền",
                "Thu số tiền: " + JavaFxTheme.formatCurrency(sel.getFineAmount()) + "\n" +
                "Từ độc giả: " + getUserLabel(sel.getUserId()) + "?");
        if (!confirm) return;

        if (fineService != null) {
            try {
                fineService.payFine(sel.getFineId());
                JavaFxTheme.showInfo("Thành công", "Đã thu nộp số tiền " + JavaFxTheme.formatCurrency(sel.getFineAmount()) + " thành công!");
                refreshFines();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi thanh toán", ex.getMessage());
            }
        } else {
            sel.markAsPaid();
            fineTable.refresh();
            JavaFxTheme.showInfo("Demo", "Đã đánh dấu thanh toán mô phỏng!");
        }
    }

    private void refreshFines() {
        if (fineService != null) {
            try {
                List<Fine> all = fineService.getAllFines();
                fineList.setAll(all);

                BigDecimal totalUnpaid = BigDecimal.ZERO;
                for (Fine f : all) {
                    if (f.getStatus() == FineStatus.UNPAID) {
                        totalUnpaid = totalUnpaid.add(f.getFineAmount());
                    }
                }
                lblTotalUnpaidFine.setText("Tổng phạt chưa thu: " + JavaFxTheme.formatCurrency(totalUnpaid));
            } catch (Exception ignored) {
            }
        }
    }
}