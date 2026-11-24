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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Bảng Điều Khiển Quản Trị Viên JavaFX kết nối nghiệp vụ thực tế (AdminDashboardView).
 * Tích hợp thống kê KPI thời gian thực, quản lý & phân quyền tài khoản (User Management),
 * và báo cáo giám sát vi phạm quá hạn trả sách.
 */
public class AdminDashboardView extends BorderPane {

    private final Staff adminStaff;
    private final UserService userService;
    private final BookService bookService;
    private final BorrowService borrowService;
    private final FineService fineService;
    private final Runnable onLogout;

    // KPI Cards
    private VBox cardMembers;
    private VBox cardStaffs;
    private VBox cardOverdue;
    private VBox cardUnpaidFine;
    private HBox kpiBox;

    // User Table
    private TableView<User> userTable;
    private ObservableList<User> userList;
    private List<User> allUsersCache = new ArrayList<>();
    private TextField txtUserSearch;
    private ComboBox<String> cboRoleFilter;
    private Button btnToggleStatus;

    // Overdue Table
    private TableView<BorrowRecord> overdueTable;
    private ObservableList<BorrowRecord> overdueList;

    public AdminDashboardView(Staff adminStaff,
                              UserService userService,
                              BookService bookService,
                              BorrowService borrowService,
                              FineService fineService,
                              Runnable onLogout) {
        this.adminStaff = adminStaff != null ? adminStaff : createDefaultMockAdmin();
        this.userService = userService;
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.fineService = fineService;
        this.onLogout = onLogout;

        setStyle("-fx-background-color: " + JavaFxTheme.COLOR_BG_LIGHT + ";");

        // Top App Bar
        setTop(createTopBar());

        // Center Content
        VBox centerBox = new VBox(16);
        centerBox.setPadding(new Insets(16, 20, 16, 20));

        // 1. KPI Cards
        kpiBox = createKpiSection();
        centerBox.getChildren().add(kpiBox);

        // 2. TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-min-width: 150px; -fx-tab-min-height: 38px;");

        tabPane.getTabs().add(createUserManagementTab());
        tabPane.getTabs().add(createOverdueReportTab());

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        centerBox.getChildren().add(tabPane);

        setCenter(centerBox);

        // Load Initial Data
        reloadAllData();
    }

    public AdminDashboardView() {
        this(null, null, null, null, null, null);
    }

    private static Staff createDefaultMockAdmin() {
        Staff s = new Staff("ADM001", "admin", "hash", "Nguyễn Quản Trị", "admin@hust.edu.vn", "0901234567", Role.ADMIN, "Trung tâm CNTT");
        s.setUserId(1);
        return s;
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

        Label lblLogo = new Label("🛡️");
        lblLogo.setFont(Font.font("Segoe UI Emoji, Apple Color Emoji", 22));

        VBox brandBox = new VBox(2);
        Label lblAppName = new Label("HUST Lib — Bảng Điều Khiển Quản Trị Viên");
        lblAppName.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblAppName.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Label lblSub = new Label("Quản lý tài khoản, phân quyền hệ thống & giám sát số liệu vận hành");
        lblSub.setFont(Font.font("System", 11));
        lblSub.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");
        brandBox.getChildren().addAll(lblAppName, lblSub);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label badgeAdmin = new Label("ADMIN ROOT");
        badgeAdmin.setStyle(
                "-fx-background-color: #FAF5FF;" +
                "-fx-text-fill: #6B21A8;" +
                "-fx-border-color: #E9D5FF;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 3 10 3 10;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        Label lblUser = new Label(adminStaff.getFullName() + " (" + adminStaff.getUserCode() + ")");
        lblUser.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        lblUser.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_PRIMARY + ";");

        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnLogout.setOnAction(e -> {
            if (onLogout != null) {
                if (JavaFxTheme.showConfirm("Xác nhận đăng xuất", "Bạn có chắc chắn muốn đăng xuất tài khoản quản trị?")) {
                    onLogout.run();
                }
            }
        });

        topBar.getChildren().addAll(lblLogo, brandBox, topSpacer, badgeAdmin, lblUser, btnLogout);
        return topBar;
    }

    private HBox createKpiSection() {
        HBox box = new HBox(14);
        cardMembers = JavaFxTheme.createStatCard("Tổng Độc Giả", "0 bạn đọc", JavaFxTheme.COLOR_PRIMARY_RED, "👥");
        cardStaffs = JavaFxTheme.createStatCard("Cán Bộ / Thủ Thư", "0 nhân sự", JavaFxTheme.COLOR_ACCENT_GOLD, "📚");
        cardOverdue = JavaFxTheme.createStatCard("Phiếu Mượn Quá Hạn", "0 phiếu", "#0284C7", "⚠️");
        cardUnpaidFine = JavaFxTheme.createStatCard("Tiền Phạt Đang Nợ", "0 VNĐ", "#DC2626", "💰");

        HBox.setHgrow(cardMembers, Priority.ALWAYS);
        HBox.setHgrow(cardStaffs, Priority.ALWAYS);
        HBox.setHgrow(cardOverdue, Priority.ALWAYS);
        HBox.setHgrow(cardUnpaidFine, Priority.ALWAYS);

        box.getChildren().addAll(cardMembers, cardStaffs, cardOverdue, cardUnpaidFine);
        return box;
    }

    // =========================================================================
    // TAB 1: QUẢN LÝ NGƯỜI DÙNG & TÀI KHOẢN
    // =========================================================================
    private Tab createUserManagementTab() {
        Tab tab = new Tab("👥 Quản Lý Người Dùng & Phân Quyền");
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));

        VBox userCard = new VBox(12);
        userCard.setPadding(new Insets(16));
        userCard.setStyle(JavaFxTheme.CSS_CARD);
        VBox.setVgrow(userCard, Priority.ALWAYS);

        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        txtUserSearch = new TextField();
        txtUserSearch.setPromptText("Tìm theo họ tên, username, mã thẻ, email...");
        txtUserSearch.setStyle(JavaFxTheme.CSS_INPUT);
        HBox.setHgrow(txtUserSearch, Priority.ALWAYS);
        txtUserSearch.textProperty().addListener((obs, oldV, newV) -> filterUsers());

        cboRoleFilter = new ComboBox<>();
        cboRoleFilter.getItems().addAll("-- Tất Cả Vai Trò --", "MEMBER", "LIBRARIAN", "ADMIN");
        cboRoleFilter.setValue("-- Tất Cả Vai Trò --");
        cboRoleFilter.setStyle(JavaFxTheme.CSS_INPUT);
        cboRoleFilter.valueProperty().addListener((obs, oldV, newV) -> filterUsers());

        Button btnAddStaff = new Button("+ Thêm Cán Bộ");
        btnAddStaff.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnAddStaff.setOnAction(e -> showAddStaffDialog());

        Button btnAddMember = new Button("+ Thêm Độc Giả");
        btnAddMember.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnAddMember.setOnAction(e -> showAddMemberDialog());

        btnToggleStatus = new Button("🔒 Khóa / Mở Khóa");
        btnToggleStatus.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-border-color: #FDE68A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 7 12 7 12; -fx-font-weight: bold; -fx-cursor: hand;");
        btnToggleStatus.setDisable(true);
        btnToggleStatus.setOnAction(e -> handleToggleStatus());

        Button btnReload = new Button("Tải Lại");
        btnReload.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnReload.setOnAction(e -> reloadAllData());

        toolBar.getChildren().addAll(txtUserSearch, cboRoleFilter, btnAddStaff, btnAddMember, btnToggleStatus, btnReload);

        userTable = new TableView<>();
        VBox.setVgrow(userTable, Priority.ALWAYS);

        TableColumn<User, String> colCode = new TableColumn<>("Mã NV / MSSV");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserCode()));
        colCode.setPrefWidth(120);

        TableColumn<User, String> colName = new TableColumn<>("Họ và tên");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colName.setPrefWidth(180);

        TableColumn<User, String> colUsername = new TableColumn<>("Tên đăng nhập");
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colUsername.setPrefWidth(130);

        TableColumn<User, String> colEmail = new TableColumn<>("Email HUST");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail() != null ? d.getValue().getEmail() : "—"));
        colEmail.setPrefWidth(210);

        TableColumn<User, String> colPhone = new TableColumn<>("Điện thoại");
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone() != null ? d.getValue().getPhone() : "—"));
        colPhone.setPrefWidth(110);

        TableColumn<User, String> colRole = new TableColumn<>("Vai trò");
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        colRole.setPrefWidth(100);

        TableColumn<User, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus() == UserStatus.ACTIVE ? "HOẠT ĐỘNG" : "ĐÃ KHÓA"));
        colStatus.setPrefWidth(110);

        userTable.getColumns().addAll(colCode, colName, colUsername, colEmail, colPhone, colRole, colStatus);
        userList = FXCollections.observableArrayList();
        userTable.setItems(userList);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldU, u) -> {
            btnToggleStatus.setDisable(u == null);
            if (u != null) {
                if (u.getStatus() == UserStatus.ACTIVE) {
                    btnToggleStatus.setText("🔒 Khóa Tài Khoản");
                    btnToggleStatus.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-border-color: #FECACA; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 7 12 7 12; -fx-font-weight: bold; -fx-cursor: hand;");
                } else {
                    btnToggleStatus.setText("🔓 Mở Khóa Tài Khoản");
                    btnToggleStatus.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-border-color: #BBF7D0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 7 12 7 12; -fx-font-weight: bold; -fx-cursor: hand;");
                }
            }
        });

        userCard.getChildren().addAll(toolBar, userTable);
        root.getChildren().add(userCard);
        tab.setContent(root);
        return tab;
    }

    private void filterUsers() {
        String q = txtUserSearch.getText() != null ? txtUserSearch.getText().trim().toLowerCase() : "";
        String roleFilter = cboRoleFilter.getValue();

        List<User> filtered = new ArrayList<>();
        for (User u : allUsersCache) {
            // Check role filter
            if (roleFilter != null && !roleFilter.startsWith("--")) {
                if (!u.getRole().name().equalsIgnoreCase(roleFilter)) {
                    continue;
                }
            }
            // Check query
            if (!q.isEmpty()) {
                boolean match = (u.getFullName() != null && u.getFullName().toLowerCase().contains(q)) ||
                                (u.getUserCode() != null && u.getUserCode().toLowerCase().contains(q)) ||
                                (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
                if (!match) continue;
            }
            filtered.add(u);
        }
        userList.setAll(filtered);
    }

    private void handleToggleStatus() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.getUserId() == adminStaff.getUserId()) {
            JavaFxTheme.showError("Không được phép", "Bạn không thể khóa tài khoản quản trị hiện tại của chính mình!");
            return;
        }

        String action = selected.getStatus() == UserStatus.ACTIVE ? "khóa" : "mở khóa";
        boolean confirm = JavaFxTheme.showConfirm("Xác nhận thay đổi trạng thái",
                "Bạn có chắc muốn " + action + " tài khoản của '" + selected.getFullName() + "' (" + selected.getUserCode() + ")?");
        if (!confirm) return;

        if (userService != null) {
            try {
                userService.toggleUserStatus(selected.getUserId());
                JavaFxTheme.showInfo("Thành công", "Đã " + action + " tài khoản thành công!");
                reloadAllData();
            } catch (Exception ex) {
                JavaFxTheme.showError("Lỗi cập nhật", ex.getMessage());
            }
        } else {
            selected.setStatus(selected.getStatus() == UserStatus.ACTIVE ? UserStatus.LOCKED : UserStatus.ACTIVE);
            userTable.refresh();
        }
    }

    private void showAddMemberDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Thêm Độc Giả Mới");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle(JavaFxTheme.CSS_CARD);

        Label lblTitle = new Label("TẠO TÀI KHOẢN ĐỘC GIẢ MỚI");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        TextField txtCode = new TextField();
        txtCode.setPromptText("Mã thẻ / MSSV (VD: SV20240001)");
        txtCode.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtName = new TextField();
        txtName.setPromptText("Họ và tên độc giả");
        txtName.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email HUST (@hust.edu.vn)");
        txtEmail.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Số điện thoại liên hệ");
        txtPhone.setStyle(JavaFxTheme.CSS_INPUT);

        Button btnSave = new Button("Lưu Độc Giả");
        btnSave.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            if (code.isEmpty() || name.isEmpty()) {
                JavaFxTheme.showError("Thiếu thông tin", "Mã độc giả và Họ tên là bắt buộc!");
                return;
            }

            if (userService != null) {
                try {
                    userService.createMember(code, name, email, phone);
                    JavaFxTheme.showInfo("Thành công", "Đã thêm độc giả mới: " + name);
                    dlg.close();
                    reloadAllData();
                } catch (Exception ex) {
                    JavaFxTheme.showError("Lỗi thêm độc giả", ex.getMessage());
                }
            } else {
                dlg.close();
            }
        });

        root.getChildren().addAll(
                lblTitle,
                new Label("Mã thẻ / MSSV:"), txtCode,
                new Label("Họ và tên:"), txtName,
                new Label("Email:"), txtEmail,
                new Label("Số điện thoại:"), txtPhone,
                new Region() {{ setMinHeight(6); }},
                btnSave
        );

        dlg.setScene(new javafx.scene.Scene(root, 380, 420));
        dlg.showAndWait();
    }

    private void showAddStaffDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Thêm Cán Bộ / Nhân Viên Mới");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle(JavaFxTheme.CSS_CARD);

        Label lblTitle = new Label("TẠO TÀI KHOẢN CÁN BỘ / THỦ THƯ");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitle.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_PRIMARY_RED + ";");

        TextField txtCode = new TextField();
        txtCode.setPromptText("Mã nhân viên (VD: LIB009 / ADM009)");
        txtCode.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Tên đăng nhập (username)");
        txtUsername.setStyle(JavaFxTheme.CSS_INPUT);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Mật khẩu ban đầu (tối thiểu 6 ký tự)");
        txtPass.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtName = new TextField();
        txtName.setPromptText("Họ và tên cán bộ");
        txtName.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email liên hệ");
        txtEmail.setStyle(JavaFxTheme.CSS_INPUT);

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Số điện thoại");
        txtPhone.setStyle(JavaFxTheme.CSS_INPUT);

        ComboBox<Role> cboRole = new ComboBox<>();
        cboRole.getItems().addAll(Role.LIBRARIAN, Role.ADMIN);
        cboRole.setValue(Role.LIBRARIAN);
        cboRole.setStyle(JavaFxTheme.CSS_INPUT);
        cboRole.setMaxWidth(Double.MAX_VALUE);

        TextField txtDept = new TextField("Ban Thủ Thư");
        txtDept.setPromptText("Phòng ban / Bộ phận");
        txtDept.setStyle(JavaFxTheme.CSS_INPUT);

        Button btnSave = new Button("Lưu Cán Bộ");
        btnSave.setStyle(JavaFxTheme.CSS_BUTTON_PRIMARY);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> {
            String code = txtCode.getText().trim();
            String u = txtUsername.getText().trim();
            String p = txtPass.getText();
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            Role r = cboRole.getValue();
            String dept = txtDept.getText().trim();

            if (code.isEmpty() || u.isEmpty() || p.isEmpty() || name.isEmpty()) {
                JavaFxTheme.showError("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường bắt buộc!");
                return;
            }

            if (userService != null) {
                try {
                    userService.createStaff(code, u, p, name, email, phone, r, dept);
                    JavaFxTheme.showInfo("Thành công", "Đã tạo tài khoản cán bộ mới: " + name);
                    dlg.close();
                    reloadAllData();
                } catch (Exception ex) {
                    JavaFxTheme.showError("Lỗi tạo nhân viên", ex.getMessage());
                }
            } else {
                dlg.close();
            }
        });

        root.getChildren().addAll(
                lblTitle,
                new Label("Mã nhân viên:"), txtCode,
                new Label("Tên đăng nhập:"), txtUsername,
                new Label("Mật khẩu:"), txtPass,
                new Label("Họ và tên:"), txtName,
                new Label("Email:"), txtEmail,
                new Label("Số điện thoại:"), txtPhone,
                new Label("Vai trò phân quyền:"), cboRole,
                new Label("Phòng ban:"), txtDept,
                new Region() {{ setMinHeight(4); }},
                btnSave
        );

        dlg.setScene(new javafx.scene.Scene(new ScrollPane(root) {{ setFitToWidth(true); }}, 420, 560));
        dlg.showAndWait();
    }

    // =========================================================================
    // TAB 2: BÁO CÁO PHIẾU MƯỢN QUÁ HẠN
    // =========================================================================
    private Tab createOverdueReportTab() {
        Tab tab = new Tab("⚠️ Báo Cáo Sách Quá Hạn");
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));

        HBox head = new HBox(12);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblHead = new Label("GIÁM SÁT VI PHẠM MƯỢN TRẢ SÁCH TOÀN HỆ THỐNG");
        lblHead.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblHead.setStyle("-fx-text-fill: #DC2626;");

        Label lblSub = new Label("Danh sách các phiếu mượn đã quá hạn hẹn trả nhưng chưa hoàn trả vào kho");
        lblSub.setFont(Font.font("System", 11));
        lblSub.setStyle("-fx-text-fill: " + JavaFxTheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(lblHead, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReloadOverdue = new Button("Tải Lại Báo Cáo");
        btnReloadOverdue.setStyle(JavaFxTheme.CSS_BUTTON_SECONDARY);
        btnReloadOverdue.setOnAction(e -> reloadOverdueData());

        head.getChildren().addAll(titleBox, spacer, btnReloadOverdue);

        overdueTable = new TableView<>();
        VBox.setVgrow(overdueTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colRecId = new TableColumn<>("Mã Phiếu");
        colRecId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRecordId()));
        colRecId.setPrefWidth(90);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("Độc Giả (MSSV / Tên)");
        colUser.setCellValueFactory(d -> new SimpleStringProperty(getUserLabel(d.getValue().getUserId())));
        colUser.setPrefWidth(200);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Tiêu Đề Sách");
        colBook.setCellValueFactory(d -> new SimpleStringProperty(getBookLabel(d.getValue().getBookId())));
        colBook.setPrefWidth(250);

        TableColumn<BorrowRecord, String> colBDate = new TableColumn<>("Ngày Mượn");
        colBDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBorrowDate().toString()));
        colBDate.setPrefWidth(110);

        TableColumn<BorrowRecord, String> colDDate = new TableColumn<>("Hạn Trả");
        colDDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDueDate().toString()));
        colDDate.setPrefWidth(110);

        TableColumn<BorrowRecord, String> colDays = new TableColumn<>("Trễ Hạn");
        colDays.setCellValueFactory(d -> {
            long days = ChronoUnit.DAYS.between(d.getValue().getDueDate(), LocalDate.now());
            return new SimpleStringProperty(days > 0 ? days + " ngày" : "0 ngày");
        });
        colDays.setPrefWidth(100);

        TableColumn<BorrowRecord, String> colEstFine = new TableColumn<>("Ước Tính Phạt (2k/ngày)");
        colEstFine.setCellValueFactory(d -> {
            long days = ChronoUnit.DAYS.between(d.getValue().getDueDate(), LocalDate.now());
            if (days <= 0) return new SimpleStringProperty("0 VNĐ");
            BigDecimal est = new BigDecimal(days * 2000L);
            return new SimpleStringProperty(JavaFxTheme.formatCurrency(est));
        });
        colEstFine.setPrefWidth(160);

        overdueTable.getColumns().addAll(colRecId, colUser, colBook, colBDate, colDDate, colDays, colEstFine);
        overdueList = FXCollections.observableArrayList();
        overdueTable.setItems(overdueList);

        root.getChildren().addAll(head, overdueTable);
        tab.setContent(root);
        return tab;
    }

    private String getUserLabel(int userId) {
        if (userService != null) {
            try {
                User u = userService.getUserById(userId);
                if (u != null) return u.getUserCode() + " - " + u.getFullName();
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

    private void reloadOverdueData() {
        if (borrowService != null) {
            try {
                List<BorrowRecord> overdues = borrowService.getOverdueBorrowRecords();
                overdueList.setAll(overdues);
            } catch (Exception ignored) {
            }
        }
    }

    private void reloadAllData() {
        // Nạp người dùng
        if (userService != null) {
            try {
                allUsersCache = userService.getAllUsers();
                filterUsers();
            } catch (Exception ignored) {
            }
        } else {
            // Mock sample
            allUsersCache = new ArrayList<>();
            Staff s1 = new Staff("ADM001", "admin", "hash", "Nguyễn Quản Trị", "admin@hust.edu.vn", "0901234567", Role.ADMIN, "IT");
            s1.setUserId(1);
            Staff s2 = new Staff("LIB001", "librarian", "hash", "Trần Thị Thu Thư", "librarian@hust.edu.vn", "0912345678", Role.LIBRARIAN, "Thư viện");
            s2.setUserId(2);
            Member m1 = new Member("SV20201234", "member", "hash", "Nguyễn Văn An", "an.nv@hust.edu.vn", "0987654321");
            m1.setUserId(3);
            allUsersCache.add(s1);
            allUsersCache.add(s2);
            allUsersCache.add(m1);
            filterUsers();
        }

        // Cập nhật thẻ KPI
        int memberCount = 0;
        int staffCount = 0;
        if (userService != null) {
            try {
                memberCount = userService.getAllMembers().size();
                staffCount = userService.getAllStaffs().size();
            } catch (Exception ignored) {}
        } else {
            memberCount = 1420;
            staffCount = 6;
        }

        int overdueCount = 0;
        if (borrowService != null) {
            try {
                List<BorrowRecord> overdues = borrowService.getOverdueBorrowRecords();
                overdueCount = overdues.size();
                overdueList.setAll(overdues);
            } catch (Exception ignored) {}
        } else {
            overdueCount = 12;
        }

        BigDecimal unpaidFines = BigDecimal.ZERO;
        if (fineService != null) {
            try {
                List<Fine> unpaids = fineService.getUnpaidFines();
                for (Fine f : unpaids) {
                    unpaidFines = unpaidFines.add(f.getFineAmount());
                }
            } catch (Exception ignored) {}
        } else {
            unpaidFines = new BigDecimal("340000");
        }

        // Cập nhật giá trị lên KPI Cards
        updateCardValue(cardMembers, memberCount + " bạn đọc");
        updateCardValue(cardStaffs, staffCount + " nhân sự");
        updateCardValue(cardOverdue, overdueCount + " phiếu");
        updateCardValue(cardUnpaidFine, JavaFxTheme.formatCurrency(unpaidFines));
    }

    private void updateCardValue(VBox card, String text) {
        if (card != null && card.getChildren().size() > 1) {
            javafx.scene.Node node = card.getChildren().get(1);
            if (node instanceof Label lbl) {
                lbl.setText(text);
                return;
            }
            for (javafx.scene.Node n : card.getChildren()) {
                if (n instanceof Label lbl) {
                    lbl.setText(text);
                    return;
                }
            }
        }
    }
}
