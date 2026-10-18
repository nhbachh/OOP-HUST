package com.library.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lớp tiện ích hỗ trợ nhập liệu an toàn và định dạng giao diện Console (CLI).
 */
public final class ConsoleUtils {

    public static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Mã màu ANSI hỗ trợ làm đẹp Terminal (tự động tương thích với macOS/Linux/Windows Terminal hiện đại)
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    private ConsoleUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔════════════════════════════════════════════════════════════════════════════════╗" + RESET);
        int totalWidth = 80;
        int titleLen = title.length();
        int padLeft = (totalWidth - titleLen - 2) / 2;
        int padRight = totalWidth - titleLen - 2 - padLeft;
        System.out.printf(CYAN + BOLD + "║%" + padLeft + "s%s%" + padRight + "s║%n" + RESET, "", title, "");
        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    public static void printSubHeader(String title) {
        System.out.println();
        System.out.println(BLUE + BOLD + "--- " + title + " ---" + RESET);
    }

    public static void printDivider() {
        System.out.println(CYAN + "────────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + "✔ [THÀNH CÔNG] " + RESET + GREEN + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + BOLD + "✖ [LỖI] " + RESET + RED + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + BOLD + "⚠ [CẢNH BÁO] " + RESET + YELLOW + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ [THÔNG TIN] " + RESET + message);
    }

    /**
     * Nhập số nguyên an toàn với giới hạn [min, max].
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                printWarning(String.format("Giá trị phải nằm trong khoảng từ %d đến %d. Vui lòng nhập lại!", min, max));
            } catch (NumberFormatException e) {
                printError("Dữ liệu nhập vào phải là số nguyên hợp lệ! Vui lòng thử lại.");
            }
        }
    }

    /**
     * Nhập chuỗi ký tự an toàn.
     */
    public static String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            if (allowEmpty || !input.isEmpty()) {
                return input;
            }
            printWarning("Trường này không được để trống! Vui lòng nhập lại.");
        }
    }

    /**
     * Nhập ngày theo định dạng yyyy-MM-dd.
     */
    public static LocalDate readDate(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            if (allowEmpty && input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                printError("Định dạng ngày không hợp lệ! Vui lòng nhập theo chuẩn yyyy-MM-dd (VD: 2026-10-25).");
            }
        }
    }

    /**
     * Tạm dừng màn hình để người dùng đọc thông tin.
     */
    public static void pressEnterToContinue() {
        System.out.println();
        System.out.print(CYAN + ">> Nhấn [Enter] để tiếp tục..." + RESET);
        SCANNER.nextLine();
    }
}
