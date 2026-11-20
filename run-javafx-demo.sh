#!/bin/bash
# Script khởi chạy JavaFX Version 2 cho HUST Library Management System
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Tạo file .classpath.txt nếu chưa có
if [ ! -f "$DIR/.classpath.txt" ]; then
    JAVA_HOME="$JAVA_HOME" mvn dependency:build-classpath -Dmdep.outputFile="$DIR/.classpath.txt" -q
fi

FX_PATH="$HOME/.m2/repository/org/openjfx"
FX_JARS="$FX_PATH/javafx-base/21.0.2/javafx-base-21.0.2-mac-aarch64.jar:$FX_PATH/javafx-graphics/21.0.2/javafx-graphics-21.0.2-mac-aarch64.jar:$FX_PATH/javafx-controls/21.0.2/javafx-controls-21.0.2-mac-aarch64.jar:$FX_PATH/javafx-fxml/21.0.2/javafx-fxml-21.0.2-mac-aarch64.jar"
APP_CP="$DIR/target/classes:$(cat "$DIR/.classpath.txt")"

"$JAVA_HOME/bin/java" \
    --module-path "$FX_JARS" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "$APP_CP" \
    com.library.Main "$@"
