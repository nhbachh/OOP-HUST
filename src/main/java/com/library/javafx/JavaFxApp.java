package com.library.javafx;

import com.library.config.DatabaseConfig;
import com.library.javafx.views.*;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Ứng dụng Quản Lý Thư Viện HUST Lib Version 2 (JavaFX).
 * Kết nối 100% với các tầng nghiệp vụ BookService, UserService, BorrowService, FineService.
 * Hỗ trợ chế độ chạy chính thức (Production flow: Đăng nhập -> Phân quyền Dashboard theo Role)
 * và chế độ Demo (--demo / --snapshot) để kiểm thử và chụp màn hình tức thì.
 */
public class JavaFxApp extends Application {

    private Stage primaryStage;
    private BorderPane mainContainer;

    // Backend Services
    private CategoryService categoryService;
    private BookService bookService;
    private UserService userService;
    private FineService fineService;
    private BorrowService borrowService;

    // Active views & components
    private boolean isDemoMode = false;
    private Button btnNavLogin;
    private Button btnNavMember;
    private Button btnNavLibrarian;
    private Button btnNavAdmin;

    @Override
    public void init() {
        // Khởi tạo các dịch vụ nghiệp vụ thực tế
        try {
            this.categoryService = new CategoryServiceImpl();
            this.bookService = new BookServiceImpl();
            this.userService = new UserServiceImpl();
            this.fineService = new FineServiceImpl();
            this.borrowService = new BorrowServiceImpl();
        } catch (Exception ex) {
            System.err.println("Cảnh báo khi khởi tạo dịch vụ nghiệp vụ: " + ex.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("HUST Lib — Hệ Thống Quản Lý Thư Viện ĐHBK Hà Nội (v2 JavaFX)");

        mainContainer = new BorderPane();

        // Kiểm tra tham số dòng lệnh
        boolean hasSnapshot = getParameters().getRaw().contains("--snapshot");
        boolean hasDemo = getParameters().getRaw().contains("--demo");
        this.isDemoMode = hasSnapshot || hasDemo;

        if (isDemoMode) {
            // Hiển thị thanh điều khiển Demo trên cùng
            HBox demoBar = createDemoControlBar();
            mainContainer.setTop(demoBar);
            showDemoLogin();
        } else {
            // Mặc định chạy quy trình chuẩn: Đăng nhập người dùng
            showLoginView();
        }

        Scene scene = new Scene(mainContainer, 1040, 660);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);

        // Hiển thị cửa sổ
        primaryStage.show();

        // Cảnh báo nếu chưa kết nối CSDL MySQL (chạy ngầm không chặn UI)
        new Thread(() -> {
            try {
                if (!DatabaseConfig.isConnectionValid()) {
                    Platform.runLater(() -> {
                        JavaFxTheme.showError("Cảnh Báo CSDL MySQL",
                                "Không thể kết nối đến máy chủ MySQL (library_db)!\n\n" +
                                "Hệ thống vẫn đang khởi chạy giao diện. Nếu cần đăng nhập và thực hiện mượn trả thật,\n" +
                                "vui lòng khởi động MySQL (VD: brew services start mysql hoặc bật qua XAMPP).");
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();

        // Nếu là snapshot tự động, thực hiện chụp rồi đóng
        if (hasSnapshot) {
            Platform.runLater(this::captureAllScreens);
        }
    }

    // =========================================================================
    // QUY TRÌNH CHUYỂN MÀN HÌNH CHÍNH THỨC (PRODUCTION NAVIGATION)
    // =========================================================================

    /**
     * Hiển thị màn hình Đăng nhập kết nối UserService.
     */
    public void showLoginView() {
        primaryStage.setTitle("HUST Lib — Đăng Nhập Hệ Thống");
        LoginView loginView = new LoginView(primaryStage, userService, this::onLoginSuccess);
        mainContainer.setCenter(loginView);
    }

    /**
     * Điều hướng người dùng sau khi xác thực đăng nhập thành công theo vai trò (Role).
     */
    private void onLoginSuccess(User user) {
        if (user == null) return;

        if (primaryStage != null && (primaryStage.getWidth() < 1140 || primaryStage.getHeight() < 720)) {
            primaryStage.setWidth(1140);
            primaryStage.setHeight(720);
            primaryStage.centerOnScreen();
        }

        if (user.getRole() == Role.ADMIN) {
            showAdminDashboard((Staff) user);
        } else if (user.getRole() == Role.LIBRARIAN) {
            showLibrarianDashboard((Staff) user);
        } else {
            showMemberDashboard((Member) user);
        }
    }

    /**
     * Hiển thị Cổng thông tin Độc giả.
     */
    public void showMemberDashboard(Member member) {
        primaryStage.setTitle("HUST Lib — Cổng Thông Tin Bạn Đọc (" + member.getFullName() + ")");
        MemberDashboardView memberView = new MemberDashboardView(
                member,
                bookService,
                categoryService,
                borrowService,
                fineService,
                userService,
                this::showLoginView
        );
        mainContainer.setCenter(memberView);
    }

    /**
     * Hiển thị Bàn làm việc Thủ thư.
     */
    public void showLibrarianDashboard(Staff staff) {
        primaryStage.setTitle("HUST Lib — Bàn Làm Việc Thủ Thư (" + staff.getFullName() + ")");
        LibrarianDashboardView librarianView = new LibrarianDashboardView(
                staff,
                bookService,
                categoryService,
                borrowService,
                fineService,
                userService,
                this::showLoginView
        );
        mainContainer.setCenter(librarianView);
    }

    /**
     * Hiển thị Bảng điều khiển Quản trị viên.
     */
    public void showAdminDashboard(Staff admin) {
        primaryStage.setTitle("HUST Lib — Bảng Điều Khiển Quản Trị Viên (" + admin.getFullName() + ")");
        AdminDashboardView adminView = new AdminDashboardView(
                admin,
                userService,
                bookService,
                borrowService,
                fineService,
                this::showLoginView
        );
        mainContainer.setCenter(adminView);
    }

    // =========================================================================
    // DEMO CONTROLLER & SCREENSHOT UTILITIES
    // =========================================================================

    private HBox createDemoControlBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: #0F172A;" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 0 0 1 0;"
        );

        Label lblBadge = new Label("JAVAFX v2 DEMO:");
        lblBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblBadge.setStyle("-fx-text-fill: #38BDF8;");

        btnNavLogin = createDemoNavButton("1. Đăng Nhập", this::showDemoLogin);
        btnNavMember = createDemoNavButton("2. Độc Giả (Member)", this::showDemoMember);
        btnNavLibrarian = createDemoNavButton("3. Thủ Thư (Librarian)", this::showDemoLibrarian);
        btnNavAdmin = createDemoNavButton("4. Quản Trị (Admin)", this::showDemoAdmin);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSnap = new Button("📸 Chụp ảnh màn hình");
        btnSnap.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
        btnSnap.setOnAction(e -> captureAllScreens());

        bar.getChildren().addAll(lblBadge, btnNavLogin, btnNavMember, btnNavLibrarian, btnNavAdmin, spacer, btnSnap);
        return bar;
    }

    private Button createDemoNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 12 5 12; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void showDemoLogin() {
        LoginView lv = new LoginView(primaryStage, userService, this::onLoginSuccess);
        mainContainer.setCenter(lv);
        updateNavState(btnNavLogin);
    }

    private void showDemoMember() {
        Member activeMember = null;
        if (userService != null) {
            try {
                User u = userService.getUserByCode("MBR001");
                if (u instanceof Member m) activeMember = m;
            } catch (Exception ignored) {}
        }
        if (activeMember == null) {
            activeMember = new Member("MBR001", "member", "hash", "Phạm Minh Tuấn", "tuan.pm@student.edu.vn", "0901234567");
            activeMember.setUserId(3);
        }
        MemberDashboardView mv = new MemberDashboardView(activeMember, bookService, categoryService, borrowService, fineService, userService, this::showDemoLogin);
        mainContainer.setCenter(mv);
        updateNavState(btnNavMember);
    }

    private void showDemoLibrarian() {
        Staff activeLib = null;
        if (userService != null) {
            try {
                User u = userService.getUserByCode("LIB001");
                if (u instanceof Staff s) activeLib = s;
            } catch (Exception ignored) {}
        }
        if (activeLib == null) {
            activeLib = new Staff("LIB001", "thuthu_mai", "hash", "Trần Tuyết Mai", "mai.tt@library.edu.vn", "0988000002", Role.LIBRARIAN, "Ban Thủ Thư");
            activeLib.setUserId(2);
        }
        LibrarianDashboardView lv = new LibrarianDashboardView(activeLib, bookService, categoryService, borrowService, fineService, userService, this::showDemoLogin);
        mainContainer.setCenter(lv);
        updateNavState(btnNavLibrarian);
    }

    private void showDemoAdmin() {
        Staff activeAdmin = null;
        if (userService != null) {
            try {
                User u = userService.getUserByCode("ADM001");
                if (u instanceof Staff s) activeAdmin = s;
            } catch (Exception ignored) {}
        }
        if (activeAdmin == null) {
            activeAdmin = new Staff("ADM001", "admin", "hash", "Nguyễn Quản Trị", "admin@library.edu.vn", "0988000001", Role.ADMIN, "Trung tâm CNTT");
            activeAdmin.setUserId(1);
        }
        AdminDashboardView av = new AdminDashboardView(activeAdmin, userService, bookService, borrowService, fineService, this::showDemoLogin);
        mainContainer.setCenter(av);
        updateNavState(btnNavAdmin);
    }

    private void updateNavState(Button activeBtn) {
        if (btnNavLogin == null) return;
        Button[] all = {btnNavLogin, btnNavMember, btnNavLibrarian, btnNavAdmin};
        for (Button b : all) {
            if (b == activeBtn) {
                b.setStyle("-fx-background-color: " + JavaFxTheme.COLOR_PRIMARY_RED + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 12 5 12; -fx-background-radius: 6;");
            } else {
                b.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: 500; -fx-padding: 5 12 5 12; -fx-background-radius: 6;");
            }
        }
    }

    /**
     * Chụp ảnh màn hình 4 giao diện sang artifact directory.
     */
    private void captureAllScreens() {
        String outDir = "/Users/apple/.gemini/antigravity/brain/0a25110d-66fc-4e21-9e8b-df80e15ccbd6";
        new Thread(() -> {
            try {
                LoginView lv = new LoginView(primaryStage, userService, this::onLoginSuccess);
                Platform.runLater(() -> {
                    lv.getTxtUsername().setText("admin");
                    lv.getTxtPassword().setText("Admin@123");
                });
                Thread.sleep(100);
                captureSingleView(lv, outDir + "/javafx_login.png");

                // Bật xem mật khẩu để chụp ảnh minh họa
                Platform.runLater(() -> lv.getBtnTogglePassword().fire());
                Thread.sleep(120);
                captureSingleView(lv, outDir + "/javafx_login_show_password.png");

                Member realMember = null;
                Staff realLib = null;
                Staff realAdmin = null;
                if (userService != null) {
                    try {
                        User u1 = userService.getUserByCode("MBR001");
                        if (u1 instanceof Member m) realMember = m;
                        User u2 = userService.getUserByCode("LIB001");
                        if (u2 instanceof Staff s) realLib = s;
                        User u3 = userService.getUserByCode("ADM001");
                        if (u3 instanceof Staff a) realAdmin = a;
                    } catch (Exception ignored) {}
                }
                if (realMember == null) {
                    realMember = new Member("MBR001", "member", "hash", "Phạm Minh Tuấn", "tuan.pm@student.edu.vn", "0901234567");
                    realMember.setUserId(3);
                }
                if (realLib == null) {
                    realLib = new Staff("LIB001", "thuthu_mai", "hash", "Trần Tuyết Mai", "mai.tt@library.edu.vn", "0988000002", Role.LIBRARIAN, "Ban Thủ Thư");
                    realLib.setUserId(2);
                }
                if (realAdmin == null) {
                    realAdmin = new Staff("ADM001", "admin", "hash", "Nguyễn Quản Trị", "admin@library.edu.vn", "0988000001", Role.ADMIN, "Trung tâm CNTT");
                    realAdmin.setUserId(1);
                }

                captureSingleView(new MemberDashboardView(realMember, bookService, categoryService, borrowService, fineService, userService, null), outDir + "/javafx_member.png");
                captureSingleView(new LibrarianDashboardView(realLib, bookService, categoryService, borrowService, fineService, userService, null), outDir + "/javafx_librarian.png");
                captureSingleView(new AdminDashboardView(realAdmin, userService, bookService, borrowService, fineService, null), outDir + "/javafx_admin.png");

                System.out.println("Tất cả 4 ảnh chụp JavaFX đã được cập nhật thành công vào artifact directory!");

                if (getParameters().getRaw().contains("--snapshot")) {
                    Platform.exit();
                    System.exit(0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void captureSingleView(Node view, String path) throws Exception {
        final Object lock = new Object();
        Platform.runLater(() -> {
            mainContainer.setCenter(view);
            primaryStage.getScene().getRoot().applyCss();
            primaryStage.getScene().getRoot().layout();

            WritableImage image = view.snapshot(null, null);
            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            PixelReader pr = image.getPixelReader();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    bimg.setRGB(x, y, pr.getArgb(x, y));
                }
            }
            try {
                ImageIO.write(bimg, "png", new File(path));
                System.out.println("Đã lưu: " + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                lock.notify();
            }
        });
        synchronized (lock) {
            lock.wait();
        }
        Thread.sleep(150);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
