package client.controllers;

import client.clientWork.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;

import static client.util.ClientDialog.showAlert;

public class MenuChild {
    private static final Logger LOGGER = Logger.getLogger(MenuChild.class.getName());

    private Client client;
    private int currentUserId;

    @FXML
    private Button backButton;

    @Setter
    private Stage stage;

    public void setClient(Client client) {
        LOGGER.info("Установка клиента в MenuChild: " + (client != null ? client.toString() : "null"));
        this.client = client;
    }

    public void setCurrentUserId(int currentUserId) {
        LOGGER.info("Установка currentUserId в MenuChild: " + currentUserId);
        this.currentUserId = currentUserId;
    }

    public static MenuChild openMenuChild(Stage primaryStage) throws IOException {
            String fxmlPath = "/client/menuChild.fxml";
            LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Пытаемся загрузить FXML из: " + fxmlPath);

            java.net.URL location = MenuChild.class.getResource(fxmlPath);
            if (location == null) {
                LOGGER.severe("[" + LocalDate.now() + " " + LocalTime.now() + "] Файл FXML не найден по пути: " + fxmlPath);
                throw new IOException("Не удаётся найти FXML файл по пути: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            MenuChild controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = primaryStage != null ? primaryStage : new Stage();
            stage.setScene(scene);

            controller.setStage(stage);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Установлены границы экрана: X=" + screenBounds.getMinX() +
                    ", Y=" + screenBounds.getMinY() + ", Width=" + screenBounds.getWidth() +
                    ", Height=" + screenBounds.getHeight());

            stage.setMaximized(true);
            stage.setTitle("Меню администратора");
            stage.show();
            LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Окно меню ребенка открыто на весь экран");

        return controller;
    }

    @FXML
    void viewBalance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/childBalanceWindow.fxml"));
            Parent root = loader.load();
            ChildBalanceWindowController controller = loader.getController();
            LOGGER.info("Установка client и userId в ChildBalanceWindowController");
            controller.setClient(client);
            controller.setUserId(currentUserId);
            Stage stage = new Stage();
            stage.setTitle("Текущий баланс");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно баланса: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.setTitle("Авторизация");
            stage.show();
            backButton.getScene().getWindow().hide();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно авторизации: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна авторизации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void persInf(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Профиль");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть профиль: " + e.getMessage());
            LOGGER.severe("Ошибка открытия профиля: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
