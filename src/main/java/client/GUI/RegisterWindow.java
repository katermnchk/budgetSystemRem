package client.GUI;

import client.clientWork.Client;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class RegisterWindow {
    private final Client client;

    public RegisterWindow(Client client) {
        this.client = client;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Регистрация");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label userLabel = new Label("Логин:");
        GridPane.setConstraints(userLabel, 0, 0);
        TextField userField = new TextField();
        GridPane.setConstraints(userField, 1, 0);

        Label passLabel = new Label("Пароль:");
        GridPane.setConstraints(passLabel, 0, 1);
        PasswordField passField = new PasswordField();
        GridPane.setConstraints(passField, 1, 1);

        Button registerButton = new Button("Зарегистрироваться");
        GridPane.setConstraints(registerButton, 1, 2);

        Button backButton = new Button("Назад");
        GridPane.setConstraints(backButton, 1, 3);

        Label messageLabel = new Label();
        GridPane.setConstraints(messageLabel, 1, 4);

        registerButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Заполните все поля!");
                return;
            }

            String response = client.sendMessage("REGISTER " + username + " " + password);
            if (response.equals("REGISTER_SUCCESS")) {
                messageLabel.setText("Регистрация успешна!");
                openLoginWindow(primaryStage);
            } else {
                messageLabel.setText("Ошибка регистрации!");
            }
        });

        backButton.setOnAction(e -> openLoginWindow(primaryStage));

        grid.getChildren().addAll(userLabel, userField, passLabel, passField, registerButton, backButton, messageLabel);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
    }

    private void openLoginWindow(Stage primaryStage) {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.start(primaryStage);
    }
}
