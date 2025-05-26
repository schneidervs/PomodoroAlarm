module com.github.schneidervs.pomodoro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.github.schneidervs.pomodoro to javafx.fxml;
    exports com.github.schneidervs.pomodoro;
}