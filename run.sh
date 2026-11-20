#!/bin/bash
# ==============================================================================
# HUST Lib — Script Khởi Chạy Ứng Dụng Quản Lý Thư Viện (v2 JavaFX & Legacy)
# ==============================================================================

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

# 1. Chế độ CLI
if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
    echo ">> Khởi chạy HUST Lib [Chế độ Dòng lệnh Console CLI]..."
    "$JAVA_CMD" -cp "target/classes:$(cat .classpath.txt 2>/dev/null)" com.library.Main --cli
    exit 0
fi

# 2. Chế độ Swing cũ (Legacy Swing GUI)
if [ "$1" == "--swing" ] || [ "$1" == "-s" ]; then
    echo ">> Khởi chạy HUST Lib [Chế độ Giao diện Swing Legacy]..."
    "$JAVA_CMD" -cp "target/classes:$(cat .classpath.txt 2>/dev/null)" com.library.Main --swing
    exit 0
fi

# 3. Mặc định: Giao diện JavaFX v2 Hiện đại
echo ">> Khởi chạy HUST Lib [Giao diện JavaFX Version 2 Hiện đại]..."
exec ./run-javafx-demo.sh "$@"
