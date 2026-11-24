#!/bin/bash
# ==============================================================================
# HUST Lib — Script Khởi Chạy Ứng Dụng Quản Lý Thư Viện (JavaFX Version 2)
# ==============================================================================

cd "$(dirname "$0")" || exit 1

echo ">> Khởi chạy HUST Lib [Giao diện JavaFX Version 2 Hiện đại]..."
exec ./run-javafx-demo.sh "$@"
