package com.library.javafx.views;

import com.library.javafx.JavaFxTheme;
import com.library.model.*;
import com.library.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cổng Thông Tin Bạn Đọc JavaFX kết nối dữ liệu thực tế (MemberDashboardView).
 * Tự động đồng bộ với BookService, BorrowService, FineService, UserService.
 */
public class MemberDashboardView extends BorderPane {

    private final Member member;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final UserService userService;
    private final Runnable onLogout;

    private final Map<Integer, String> categoryMap = new HashMap<>();

    // Container 3 thẻ KPI
    private HBox kpiBox;
    private TableView<Book> bookTable;
    private TableView<BorrowRecord> currentBorrowTable;
    private TableView<BorrowRecord> historyTable;
    private TableView<Fine> fineTable;

    public MemberDashboardView(Member member,
                               BookService bookService,
                               CategoryService categoryService,
                               BorrowService borrowService,
                               FineService fineService,
                               UserService userService,
                               Runnable onLogout) {
        this.member = member;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.userService = userService;
        this.onLogout = onLogout;

        initCategories();
        setStyle("-fx-background-color: " + JavaFxTheme.COLOR_BG_LIGHT + ";");

        // Top App Bar
        setTop(createTopBar());

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-min-width: 130px; -fx-tab-min-height: 38px;");

        tabPane.getTabs().add(createDiscoverTab());
        tabPane.getTabs().add(createBorrowTab());
        tabPane.getTabs().add(createHistoryTab());
        tabPane.getTabs().add(createFinesTab());
        tabPane.getTabs().add(createProfileTab());

        setCenter(tabPane);
        loadAllData();
    }

    private void initCategories() {
        try {
            List<Category> cats = categoryService.getAllCategories();
            for (Category c : cats) {
                categoryMap.put(c.getCategoryId(), c.getName());
            }
        } catch (Exception ignored) {}
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

        Label lblLogo = new Label("📕");
        lblLogo.setFont(Font.font(20));

        Label lblAppName = new Label("HUST Lib | Cổng Bạn Đọc");
        lblAppName.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblAppName.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label badgeStatus = new Label("Thẻ: " + member.getStatus());
        boolean isActive = member.getStatus() == UserStatus.ACTIVE;
        badgeStatus.setStyle(
                "-fx-background-color: " + (isActive ? "#ECFDF5" : "#FEF2F2") + ";" +
                "-fx-text-fill: " + (isActive ? "#065F46" : "#991B1B") + ";" +
                "-fx-border-color: " + (isActive ? "#A7F3D0" : "#FECACA") + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 3 10 3 10;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        Label lblUser = new Label(member.getFullName() + " (" + member.getUserCode() + ")");
        lblUser.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        lblUser.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnLogout.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        topBar.getChildren().addAll(lblLogo, lblAppName, topSpacer, badgeStatus, lblUser, btnLogout);
        return topBar;
    }

    private Tab createDiscoverTab() {
        Tab tab = new Tab("🔍 Khám Phá Sách");
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));

        // 3 THẺ KPI CÁ NHÂN
        kpiBox = new HBox(14);
        refreshKpiCards();

        // BỘ LỌC TÌM KIẾM
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12, 16, 12, 16));
        filterBar.setStyle(JavaFxTheme.CSS_CARD);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Nhập tên sách, tác giả, ISBN để tìm...");
        txtSearch.setStyle(JavaFxTheme.CSS_INPUT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        ComboBox<String> cboCat = new ComboBox<>();
        cboCat.getItems().add("-- Tất cả thể loại --");
        for (String cName : categoryMap.values()) {
            cboCat.getItems().add(cName);
        }
        cboCat.setValue("-- Tất cả thể loại --");
        cboCat.setStyle(JavaFxTheme.CSS_INPUT);

        Button btnSearch = new Button("Tìm kiếm");
        btnSearch.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);

        Button btnReset = new Button("Làm mới");
        btnReset.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);

        Runnable performSearch = () -> {
            String kw = txtSearch.getText().trim();
            String selCat = cboCat.getValue();
            try {
                List<Book> list;
                if (!kw.isEmpty()) {
                    list = bookService.searchBooks(kw);
                } else {
                    list = bookService.getAllBooks();
                }

                if (selCat != null && !selCat.equals("-- Tất cả thể loại --")) {
                    Integer catId = categoryMap.entrySet().stream()
                            .filter(e -> e.getValue().equals(selCat))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                    if (catId != null) {
                        list = list.stream().filter(b -> b.getCategoryId() == catId).toList();
                    }
                }
                bookTable.setItems(FXCollections.observableArrayList(list));
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi tìm kiếm", ex.getMessage());
            }
        };

        btnSearch.setOnAction(e -> performSearch.run());
        txtSearch.setOnAction(e -> performSearch.run());
        btnReset.setOnAction(e -> {
            txtSearch.clear();
            cboCat.setValue("-- Tất cả thể loại --");
            loadBookData();
        });

        filterBar.getChildren().addAll(txtSearch, cboCat, btnSearch, btnReset);

        // BẢNG SÁCH
        bookTable = new TableView<>();
        bookTable.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        TableColumn<Book, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBookId())));
        colId.setPrefWidth(55);

        TableColumn<Book, String> colIsbn = new TableColumn<>("Mã ISBN");
        colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        colIsbn.setPrefWidth(130);

        TableColumn<Book, String> colTitle = new TableColumn<>("Tiêu đề sách");
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colTitle.setPrefWidth(280);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Tác giả");
        colAuthor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        colAuthor.setPrefWidth(160);

        TableColumn<Book, String> colCat = new TableColumn<>("Thể loại");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(categoryMap.getOrDefault(d.getValue().getCategoryId(), "Khác")));
        colCat.setPrefWidth(140);

        TableColumn<Book, String> colYear = new TableColumn<>("Năm XB");
        colYear.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPublishYear())));
        colYear.setPrefWidth(80);

        TableColumn<Book, String> colCopies = new TableColumn<>("Còn lại / Tổng");
        colCopies.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAvailableCopies() + " / " + d.getValue().getTotalCopies()));
        colCopies.setPrefWidth(110);

        TableColumn<Book, String> colStatus = new TableColumn<>("Tình trạng");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAvailable() ? "Còn sách" : "Hết sách"));
        colStatus.setPrefWidth(110);

        bookTable.getColumns().addAll(colId, colIsbn, colTitle, colAuthor, colCat, colYear, colCopies, colStatus);

        root.getChildren().addAll(kpiBox, filterBar, bookTable);
        tab.setContent(root);
        return tab;
    }

    private void refreshKpiCards() {
        if (kpiBox == null) return;
        kpiBox.getChildren().clear();

        int borrowed = member.getCurrentBorrowCount();
        int max = member.getMaxBorrowLimit();
        int remaining = Math.max(0, max - borrowed);

        boolean hasFines = false;
        BigDecimal unpaidTotal = BigDecimal.ZERO;
        try {
            List<Fine> fines = fineService.getFinesByUserId(member.getUserId());
            for (Fine f : fines) {
                if (f.getStatus() == FineStatus.UNPAID) {
                    hasFines = true;
                    unpaidTotal = unpaidTotal.add(f.getFineAmount());
                }
            }
        } catch (Exception ignored) {}

        VBox card1 = JavaFxTheme.createStatCard("Sách đang mượn", borrowed + " / " + max + " cuốn", JavaFxTheme.COLOR_ACCENT_GOLD, "📖");
        VBox card2 = JavaFxTheme.createStatCard("Hạn mức còn lại", remaining + " cuốn", JavaFxTheme.COLOR_SUCCESS_GREEN, "📗");
        VBox card3 = JavaFxTheme.createStatCard("Tiền nợ phạt", hasFines ? JavaFxTheme.formatCurrency(unpaidTotal) : "0 VNĐ (Không nợ)",
                hasFines ? JavaFxTheme.COLOR_PRIMARY_RED : JavaFxTheme.COLOR_SUCCESS_GREEN, "💰");

        kpiBox.getChildren().addAll(card1, card2, card3);
    }

    private Tab createBorrowTab() {
        Tab tab = new Tab("📖 Sách Đang Mượn");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20, 24, 20, 24));

        VBox header = JavaFxTheme.createHeaderSection(
                "DANH SÁCH SÁCH HIỆN ĐANG MƯỢN",
                "Theo dõi thời hạn trả để tránh phát sinh phí phạt trễ hạn (2.000 VNĐ / ngày trễ)"
        );

        currentBorrowTable = new TableView<>();
        currentBorrowTable.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(currentBorrowTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colRec = new TableColumn<>("Mã phiếu");
        colRec.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRec.setPrefWidth(90);

        TableColumn<BorrowRecord, String> colBookId = new TableColumn<>("Mã sách");
        colBookId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBookId())));
        colBookId.setPrefWidth(80);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Tên đầu sách");
        colBook.setCellValueFactory(d -> {
            Book b = bookService.getBookById(d.getValue().getBookId());
            return new SimpleStringProperty(b != null ? b.getTitle() : "Mã sách #" + d.getValue().getBookId());
        });
        colBook.setPrefWidth(280);

        TableColumn<BorrowRecord, String> colBorrowDate = new TableColumn<>("Ngày mượn");
        colBorrowDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBorrowDate())));
        colBorrowDate.setPrefWidth(120);

        TableColumn<BorrowRecord, String> colDueDate = new TableColumn<>("Hạn trả");
        colDueDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDueDate())));
        colDueDate.setPrefWidth(120);

        TableColumn<BorrowRecord, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStatus())));
        colStatus.setPrefWidth(110);

        TableColumn<BorrowRecord, String> colDays = new TableColumn<>("Thời hạn còn lại");
        colDays.setCellValueFactory(d -> {
            LocalDate today = LocalDate.now();
            LocalDate due = d.getValue().getDueDate();
            if (today.isAfter(due)) {
                long over = ChronoUnit.DAYS.between(due, today);
                return new SimpleStringProperty("Quá hạn " + over + " ngày!");
            } else {
                long left = ChronoUnit.DAYS.between(today, due);
                return new SimpleStringProperty("Còn " + left + " ngày");
            }
        });
        colDays.setPrefWidth(140);

        currentBorrowTable.getColumns().addAll(colRec, colBookId, colBook, colBorrowDate, colDueDate, colStatus, colDays);

        root.getChildren().addAll(header, currentBorrowTable);
        tab.setContent(root);
        return tab;
    }

    private Tab createHistoryTab() {
        Tab tab = new Tab("📜 Lịch Sử Mượn Trả");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20, 24, 20, 24));

        VBox header = JavaFxTheme.createHeaderSection(
                "NHẬT KÝ MƯỢN TRẢ TÀI LIỆU",
                "Toàn bộ các lần mượn và hoàn trả tài liệu của bạn tại thư viện"
        );

        historyTable = new TableView<>();
        historyTable.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colRec = new TableColumn<>("Mã phiếu");
        colRec.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRec.setPrefWidth(90);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Tên sách");
        colBook.setCellValueFactory(d -> {
            Book b = bookService.getBookById(d.getValue().getBookId());
            return new SimpleStringProperty(b != null ? b.getTitle() : "Mã sách #" + d.getValue().getBookId());
        });
        colBook.setPrefWidth(280);

        TableColumn<BorrowRecord, String> colBDate = new TableColumn<>("Ngày mượn");
        colBDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBorrowDate())));
        colBDate.setPrefWidth(120);

        TableColumn<BorrowRecord, String> colDueDate = new TableColumn<>("Hạn trả");
        colDueDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDueDate())));
        colDueDate.setPrefWidth(120);

        TableColumn<BorrowRecord, String> colRDate = new TableColumn<>("Ngày trả thực tế");
        colRDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReturnDate() != null ? String.valueOf(d.getValue().getReturnDate()) : "Chưa trả"));
        colRDate.setPrefWidth(130);

        TableColumn<BorrowRecord, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStatus())));
        colStatus.setPrefWidth(110);

        historyTable.getColumns().addAll(colRec, colBook, colBDate, colDueDate, colRDate, colStatus);

        root.getChildren().addAll(header, historyTable);
        tab.setContent(root);
        return tab;
    }

    private Tab createFinesTab() {
        Tab tab = new Tab("💰 Vé Phạt Của Tôi");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20, 24, 20, 24));

        VBox header = JavaFxTheme.createHeaderSection(
                "DANH SÁCH VÉ PHẠT QUÁ HẠN & BỒI THƯỜNG",
                "Độc giả đang có vé phạt chưa thanh toán sẽ tạm thời bị đình chỉ quyền mượn sách"
        );

        fineTable = new TableView<>();
        fineTable.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(fineTable, Priority.ALWAYS);

        TableColumn<Fine, String> colId = new TableColumn<>("Mã phạt");
        colId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getFineId()));
        colId.setPrefWidth(80);

        TableColumn<Fine, String> colRec = new TableColumn<>("Mã phiếu mượn");
        colRec.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRec.setPrefWidth(110);

        TableColumn<Fine, String> colDays = new TableColumn<>("Số ngày trễ");
        colDays.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOverdueDays() + " ngày"));
        colDays.setPrefWidth(100);

        TableColumn<Fine, String> colAmount = new TableColumn<>("Số tiền phạt");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(JavaFxTheme.formatCurrency(d.getValue().getFineAmount())));
        colAmount.setPrefWidth(140);

        TableColumn<Fine, String> colReason = new TableColumn<>("Lý do phạt");
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colReason.setPrefWidth(220);

        TableColumn<Fine, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStatus())));
        colStatus.setPrefWidth(110);

        fineTable.getColumns().addAll(colId, colRec, colDays, colAmount, colReason, colStatus);

        root.getChildren().addAll(header, fineTable);
        tab.setContent(root);
        return tab;
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("👤 Hồ Sơ Bạn Đọc");
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(16);
        card.setMaxWidth(560);
        card.setStyle(JavaFxTheme.CSS_CARD);

        Label lblHead = new Label("HỒ SƠ BẠN ĐỌC HUST");
        lblHead.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblHead.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        Label lblCodeVal = new Label(member.getUserCode());
        Label lblNameVal = new Label(member.getFullName());
        Label lblEmailVal = new Label(member.getEmail() != null ? member.getEmail() : "Chưa cập nhật");
        Label lblPhoneVal = new Label(member.getPhone() != null ? member.getPhone() : "Chưa cập nhật");
        Label lblLimitVal = new Label(member.getMaxBorrowLimit() + " cuốn sách");
        Label lblBorrowVal = new Label(member.getCurrentBorrowCount() + " cuốn đang mượn");
        Label lblStatusVal = new Label(String.valueOf(member.getStatus()));

        grid.add(new Label("Mã độc giả / MSSV:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 0);
        grid.add(lblCodeVal, 1, 0);

        grid.add(new Label("Họ và tên:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 1);
        grid.add(lblNameVal, 1, 1);

        grid.add(new Label("Email HUST:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 2);
        grid.add(lblEmailVal, 1, 2);

        grid.add(new Label("Số điện thoại:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 3);
        grid.add(lblPhoneVal, 1, 3);

        grid.add(new Label("Hạn mức tối đa:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 4);
        grid.add(lblLimitVal, 1, 4);

        grid.add(new Label("Sách đang mượn:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 5);
        grid.add(lblBorrowVal, 1, 5);

        grid.add(new Label("Trạng thái thẻ:") {{ setStyle("-fx-font-weight: bold;"); }}, 0, 6);
        grid.add(lblStatusVal, 1, 6);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnUpdate = new Button("Cập nhật thông tin");
        btnUpdate.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnUpdate.setOnAction(e -> showUpdateProfileDialog(lblNameVal, lblEmailVal, lblPhoneVal));

        Button btnChangePass = new Button("Đổi mật khẩu");
        btnChangePass.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnChangePass.setOnAction(e -> showChangePasswordDialog());

        btnBox.getChildren().addAll(btnUpdate, btnChangePass);

        card.getChildren().addAll(lblHead, grid, new Separator(), btnBox);
        root.getChildren().add(card);
        tab.setContent(root);
        return tab;
    }

    private void showUpdateProfileDialog(Label lblName, Label lblEmail, Label lblPhone) {
        Stage stage = (Stage) getScene().getWindow();
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Cập Nhật Thông Tin Cá Nhân");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        TextField txtName = new TextField(member.getFullName());
        TextField txtEmail = new TextField(member.getEmail() != null ? member.getEmail() : "");
        TextField txtPhone = new TextField(member.getPhone() != null ? member.getPhone() : "");

        Button btnSave = new Button("Lưu Thay Đổi");
        btnSave.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnSave.setOnAction(e -> {
            try {
                userService.updateProfile(member.getUserId(), txtName.getText().trim(), txtEmail.getText().trim(), txtPhone.getText().trim());
                member.setFullName(txtName.getText().trim());
                member.setEmail(txtEmail.getText().trim());
                member.setPhone(txtPhone.getText().trim());
                lblName.setText(member.getFullName());
                lblEmail.setText(member.getEmail());
                lblPhone.setText(member.getPhone());
                JavaFxTheme.showInfo("Thành công", "Đã cập nhật thông tin cá nhân!");
                dialog.close();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi cập nhật", ex.getMessage());
            }
        });

        root.getChildren().addAll(
                new Label("Họ và tên:"), txtName,
                new Label("Email:"), txtEmail,
                new Label("Số điện thoại:"), txtPhone,
                new Region() {{ setMinHeight(6); }},
                btnSave
        );

        dialog.setScene(new Scene(root, 360, 280));
        dialog.showAndWait();
    }

    private void showChangePasswordDialog() {
        Stage stage = (Stage) getScene().getWindow();
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Đổi Mật Khẩu");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        PasswordField txtOld = new PasswordField();
        PasswordField txtNew = new PasswordField();
        PasswordField txtConf = new PasswordField();

        Button btnSave = new Button("Đổi Mật Khẩu");
        btnSave.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnSave.setOnAction(e -> {
            String pOld = txtOld.getText();
            String pNew = txtNew.getText();
            String pConf = txtConf.getText();
            if (!pNew.equals(pConf)) {
                JavaFxTheme.showError("Lỗi", "Mật khẩu mới không trùng khớp!");
                return;
            }
            try {
                userService.changePassword(member.getUserId(), pOld, pNew);
                JavaFxTheme.showInfo("Thành công", "Đổi mật khẩu thành công!");
                dialog.close();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi đổi mật khẩu", ex.getMessage());
            }
        });

        root.getChildren().addAll(
                new Label("Mật khẩu hiện tại:"), txtOld,
                new Label("Mật khẩu mới:"), txtNew,
                new Label("Xác nhận mật khẩu mới:"), txtConf,
                new Region() {{ setMinHeight(6); }},
                btnSave
        );

        dialog.setScene(new Scene(root, 360, 280));
        dialog.showAndWait();
    }

    public void loadAllData() {
        loadBookData();
        loadBorrowData();
        loadFineData();
        refreshKpiCards();
    }

    private void loadBookData() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookTable.setItems(FXCollections.observableArrayList(books));
        } catch (Exception ex) {
            System.err.println("Lỗi loadBookData: " + ex.getMessage());
        }
    }

    private void loadBorrowData() {
        try {
            List<BorrowRecord> records = borrowService.getBorrowRecordsByUserId(member.getUserId());
            historyTable.setItems(FXCollections.observableArrayList(records));

            List<BorrowRecord> active = records.stream()
                    .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                    .toList();
            currentBorrowTable.setItems(FXCollections.observableArrayList(active));
        } catch (Exception ex) {
            System.err.println("Lỗi loadBorrowData: " + ex.getMessage());
        }
    }

    private void loadFineData() {
        try {
            List<Fine> fines = fineService.getFinesByUserId(member.getUserId());
            fineTable.setItems(FXCollections.observableArrayList(fines));
        } catch (Exception ex) {
            System.err.println("Lỗi loadFineData: " + ex.getMessage());
        }
    }
}
