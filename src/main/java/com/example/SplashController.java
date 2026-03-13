package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

public class SplashController {

    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label lblStatus;

    private App mainApp;

    public void setApp(App app) {
        this.mainApp = app;
        startInitialization();
    }

    private void startInitialization() {
        Task<Void> initTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Chuẩn bị UI ban đầu (nhanh)
                updateMessage("Chuẩn bị giao diện...");
                updateProgress(0.2, 1.0);
                Thread.sleep(200); // Giả lập chuẩn bị UI

                // Tải shop scene (nhanh)
                updateMessage("Tải dữ liệu cửa hàng...");
                updateProgress(0.5, 1.0);
                Thread.sleep(300);

                // Sau 2-4 giây, hiển thị shop scene với dữ liệu cơ bản
                return null;
            }

            @Override
            protected void succeeded() {
                updateProgress(1.0, 1.0);
                // Hiển thị shop scene ngay (không đợi hết tải)
                try {
                    mainApp.showShopScene();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void failed() {
                updateMessage("Lỗi khởi động!");
                Thread.dumpStack();
            }
        };

        // Cập nhật progress bar
        progressBar.progressProperty().bind(initTask.progressProperty());
        if (lblStatus != null) {
            lblStatus.textProperty().bind(initTask.messageProperty());
        }

        new Thread(initTask, "InitThread").start();
    }
}
