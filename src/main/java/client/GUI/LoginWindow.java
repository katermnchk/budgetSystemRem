package client.GUI;

import client.clientWork.Client;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginWindow extends Application {
    private Client client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        client = new Client("127.0.0.1", "9006"); // Подключаемся к серверу

        primaryStage.setTitle("Вход в систему");

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

        Button loginButton = new Button("Войти");
        GridPane.setConstraints(loginButton, 1, 2);

        Button registerButton = new Button("Регистрация");
        GridPane.setConstraints(registerButton, 1, 3);

        Label messageLabel = new Label();
        GridPane.setConstraints(messageLabel, 1, 4);

        loginButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Заполните все поля!");
                return;
            }

            String response = client.sendMessage("LOGIN " + username + " " + password);
            if (response.equals("LOGIN_SUCCESS")) {
                messageLabel.setText("Успешный вход!");
                openMainWindow(primaryStage);
            } else {
                messageLabel.setText("Ошибка входа!");
            }
        });

        registerButton.setOnAction(e -> openRegisterWindow(primaryStage));

        grid.getChildren().addAll(userLabel, userField, passLabel, passField, loginButton, registerButton, messageLabel);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openRegisterWindow(Stage primaryStage) {
        RegisterWindow registerWindow = new RegisterWindow(client);
        registerWindow.start(primaryStage);
    }

    private void openMainWindow(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(client);
        mainWindow.start(primaryStage);
    }
}
