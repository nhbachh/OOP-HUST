#!/bin/bash
# ==============================================================================
# HUST Lib — Script Khởi Chạy Ứng Dụng Quản Lý Thư Viện (Ready for Users)
# ==============================================================================

# Ưu tiên sử dụng JDK 17+ (Dự án yêu cầu tối thiểu Java 17, máy hiện có JDK 27)
if [ -d "/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home"
elif [ -x "/usr/libexec/java_home" ]; then
    JDK17_HOME=$(/usr/libexec/java_home -v 17+ 2>/dev/null)
    if [ -n "$JDK17_HOME" ]; then
        export JAVA_HOME="$JDK17_HOME"
    fi
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

cd "$(dirname "$0")" || exit 1

JAR_FILE="target/LibraryManagementSystem-1.0.0.jar"

# 1. Nếu đã đóng gói Fat JAR độc lập, ưu tiên chạy trực tiếp (nhanh, không cần Maven)
if [ -f "$JAR_FILE" ]; then
    if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
        echo ">> Khởi chạy HUST Lib [Chế độ Dòng lệnh Console CLI] từ JAR..."
        "$JAVA_CMD" -jar "$JAR_FILE" --cli
    else
        echo ">> Khởi chạy HUST Lib [Chế độ Giao diện Đồ họa Hiện đại Swing GUI] từ JAR..."
        "$JAVA_CMD" -jar "$JAR_FILE"
    fi
    exit 0
fi

# 2. Nếu chưa đóng gói, tự động gọi Maven
echo ">> Không tìm thấy file JAR đóng gói sẵn, khởi chạy qua Maven exec:java..."
if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
    mvn -o exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="--cli"
else
    mvn -o exec:java -Dexec.mainClass="com.library.Main"
fi
