#!/bin/bash
# ==============================================================================
# Script tiện ích khởi chạy Hệ thống Quản lý Thư viện (Library Management System)
# ==============================================================================

# Ưu tiên sử dụng JDK 17+ (JDK 27 trên máy)
if [ -d "/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home"
fi

cd "$(dirname "$0")" || exit 1

if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
    echo ">> Khởi chạy ứng dụng ở chế độ Giao diện Dòng lệnh (Console CLI)..."
    mvn -o exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="--cli"
else
    echo ">> Khởi chạy ứng dụng ở chế độ Giao diện Đồ họa Hiện đại (Swing FlatLaf GUI)..."
    mvn -o exec:java -Dexec.mainClass="com.library.Main"
fi
