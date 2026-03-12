package com.example;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private ProgressBar progressBar;

    private App mainApp;

    public void setApp(App app) {
        this.mainApp = app;

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            try {
                mainApp.showShopScene();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }
}
