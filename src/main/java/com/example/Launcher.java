package com.example;

/**
 * Lớp khởi chạy không dùng JavaFX.
 * Cần thiết để bỏ qua kiểm tra module JavaFX khi chạy từ fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
