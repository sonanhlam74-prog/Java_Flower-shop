package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

public class SplashController {

    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus;

    private App mainApp;
    private Runnable onComplete;
    private String loginUsername;

    public void setOnComplete(Runnable onComplete) { this.onComplete = onComplete; }
    public void setUsername(String username) { this.loginUsername = username; }

    public void setApp(App app) {
        this.mainApp = app;
        startInitialization();
    }

    private void startInitialization() {
        boolean isLoginMode = loginUsername != null;

        Task<Void> initTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isLoginMode) {
                    updateMessage("Đang xác thực tài khoản...");
                    updateProgress(0.2, 1.0);
                    Thread.sleep(5000);

                    updateMessage("Đang tải dữ liệu...");
                    updateProgress(0.5, 1.0);
                    Thread.sleep(5000);

                    updateMessage("Chuẩn bị Dashboard...");
                    updateProgress(0.8, 1.0);
                    Thread.sleep(5000);
                } else {
                    updateMessage("Chuẩn bị giao diện...");
                    updateProgress(0.2, 1.0);
                    Thread.sleep(5000);

                    updateMessage("Tải dữ liệu cửa hàng...");
                    updateProgress(0.5, 1.0);
                    Thread.sleep(5000);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                updateProgress(1.0, 1.0);
                try {
                    if (onComplete != null) {
                        onComplete.run();
                    } else {
                        mainApp.showShopScene();
                    }
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

        progressBar.progressProperty().bind(initTask.progressProperty());
        if (lblStatus != null) {
            lblStatus.textProperty().bind(initTask.messageProperty());
        }

        new Thread(initTask, "InitThread").start();
    }
}
