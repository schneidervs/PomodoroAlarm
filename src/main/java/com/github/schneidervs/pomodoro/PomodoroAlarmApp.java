package com.github.schneidervs.pomodoro;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;

public class PomodoroAlarmApp extends Application {

    private TextField workField;
    private TextField restField;
    private TextField totalCycleField;
    private CheckBox usePauseCheckBox;
    private CheckBox useRing;
    private CheckBox useSystemRing;
    private CheckBox useShowNotification;
    private CheckBox showNotificationAlwaysOnTop;
    private Label statusLabel;
    private Slider slider;
    private PauseTransition pause;
    private int workMinutes;
    private int restMinutes;
    private int cyclesLeft;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pomodoro Alarm v0.1a");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        useRing = new CheckBox();
        useSystemRing = new CheckBox();
        useShowNotification = new CheckBox();

        CheckBox checkUseNotification1 = new CheckBox();
        CheckBox checkUseNotification2 = new CheckBox();

        checkUseNotification1.disableProperty().bind(useShowNotification.selectedProperty().not());
        checkUseNotification2.disableProperty().bind(useShowNotification.selectedProperty().not());

        showNotificationAlwaysOnTop = checkUseNotification1;
        usePauseCheckBox = checkUseNotification2;

        useRing.setSelected(true);

        // Взаимоисключающие чекбоксы
        useRing.setOnAction(e -> {
            if (useRing.isSelected()) useSystemRing.setSelected(false);
        });
        useSystemRing.setOnAction(e -> {
            if (useSystemRing.isSelected()) useRing.setSelected(false);
        });

        slider = new Slider(0.0, 1.0, 0.2);

        workField = new TextField("25");
        restField = new TextField("5");
        totalCycleField = new TextField("8");

        grid.add(new Label("Work interval (minutes):"), 0, 0);
        grid.add(workField, 1, 0);

        grid.add(new Label("Rest interval (minutes):"), 0, 1);
        grid.add(restField, 1, 1);

        grid.add(new Label("Total duration (cycles):"), 0, 2);
        grid.add(totalCycleField, 1, 2);

        grid.add(new Label("Use rings:"), 0, 3);
        grid.add(useRing, 1, 3);

        grid.add(new Label("Volume:"), 0, 4);
        grid.add(slider, 1, 4);

        grid.add(new Label("Use system rings:"), 0, 5);
        grid.add(useSystemRing, 1, 5);

        grid.add(new Label("Use show notification:"), 0, 6);
        grid.add(useShowNotification, 1, 6);

        grid.add(new Label("Notification always on top:"), 0, 7);
        grid.add(showNotificationAlwaysOnTop, 1, 7);

        grid.add(new Label("Use pause:"), 0, 8);
        grid.add(usePauseCheckBox, 1, 8);

        Button startButton = new Button("Start");
        grid.add(startButton, 0, 9);

        Button stopButton = new Button("Stop");
        grid.add(stopButton, 1, 9);

        statusLabel = new Label("Status: Waiting to start...");
        grid.add(statusLabel, 0, 10, 2, 1);

        startButton.setOnAction(e -> startPomodoro());
        stopButton.setOnAction(e -> stopPomodoro());

        Scene scene = new Scene(grid, 420, 370);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void stopPomodoro() {
        if (pause != null) {
            pause.stop();
            }

            statusLabel.setText("Status: Waiting to start...");
            workField.setText("25");
            restField.setText("5");
            totalCycleField.setText("8");
    }

    private void startPomodoro() {
        try {
            workMinutes = Integer.parseInt(workField.getText());
            restMinutes = Integer.parseInt(restField.getText());
            cyclesLeft = Integer.parseInt(totalCycleField.getText());
            startWorkPeriod();
        } catch (NumberFormatException _) {
            statusLabel.setText("Invalid input values!");
        }
    }

    private void startWorkPeriod() {
        if (cyclesLeft <= 0) {
            statusLabel.setText("Status: Done!");
            showNotification("Pomodoro session completed!", null);
            return;
        }

        statusLabel.setText("Status: Work period started");
        playSound("/com/github/schneidervs/pomodoro/AlarmWork.mp3");

        Runnable afterAlert = () -> {
            pause = new PauseTransition(Duration.minutes(workMinutes));
            pause.setOnFinished(e -> startRestPeriod());
            pause.play();
        };

        showNotification("Work period started", afterAlert);
    }

    private void startRestPeriod() {
        statusLabel.setText("Status: Rest period started");
        playSound("/com/github/schneidervs/pomodoro/AlarmRest.mp3");

        Runnable afterAlert = () -> {
            cyclesLeft--;
            pause = new PauseTransition(Duration.minutes(restMinutes));
            pause.setOnFinished(e -> startWorkPeriod());
            pause.play();
        };

        showNotification("Rest period started", afterAlert);
    }

    private void playSound(String resourcePath) {
        if (useSystemRing.isSelected()) java.awt.Toolkit.getDefaultToolkit().beep();
        if (useRing.isSelected()) {
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl != null) {
                AudioClip clip = new AudioClip(soundUrl.toString());
                clip.play(slider.getValue());
            }
        }
    }

    private void showNotification(String message, Runnable afterAlert) {
        if (useShowNotification.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pomodoro Notification");
            alert.setHeaderText(null);
            alert.setContentText(message);

            if (showNotificationAlwaysOnTop.isSelected()) {
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.setAlwaysOnTop(true);
            }

            if (usePauseCheckBox.isSelected() && afterAlert != null) {
                alert.setOnHidden(e -> afterAlert.run());
                alert.show();
            } else {
                alert.show();
                if (afterAlert != null) afterAlert.run();
            }
        } else {
            if (afterAlert != null) afterAlert.run();
        }
    }
}
