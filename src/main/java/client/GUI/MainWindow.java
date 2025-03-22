package client.GUI;

import client.clientWork.Client;
import server.DB.UserDAO;
import server.DB.DatabaseConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.Connection;

public class MainWindow extends Application {

    private UserDAO userDAO;

    public MainWindow() {
        // Пустой конструктор
    }


    public MainWindow(Client client) {
    }

    @Override
    public void start(Stage primaryStage) {
        // Подключение к БД
        Connection connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);

        // Создаем элементы UI
        Label titleLabel = new Label("Вход в систему");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");

        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // Сетка
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(titleLabel, 0, 0, 2, 1);
        gridPane.add(new Label("Логин:"), 0, 1);
        gridPane.add(usernameField, 1, 1);
        gridPane.add(new Label("Пароль:"), 0, 2);
        gridPane.add(passwordField, 1, 2);
        gridPane.add(loginButton, 0, 3);
        gridPane.add(registerButton, 1, 3);
        gridPane.add(messageLabel, 0, 4, 2, 1);

        // Обработчики кнопок
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (userDAO.authenticateUser(username, password)) {
                messageLabel.setText("Вход выполнен!");
                messageLabel.setStyle("-fx-text-fill: green;");
                // TODO: открыть основное окно
            } else {
                messageLabel.setText("Неверный логин или пароль");
            }
        });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (userDAO.registerUser(username, password)) {
                messageLabel.setText("Регистрация успешна!");
                messageLabel.setStyle("-fx-text-fill: green;");
            } else {
                messageLabel.setText("Ошибка регистрации. Возможно, логин уже используется.");
            }
        });

        // Устанавливаем сцену
        Scene scene = new Scene(gridPane, 350, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Система учета бюджета");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
