package client.controllers;

import client.controllers.admin.MenuAdminController;
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
    private static final Logger LOGGER = Logger.getLogger(MenuAdminController.class.getName());

    @FXML
    private Button backButton;

    @Setter
    private Stage stage;

    public static void openMenuChild(Stage primaryStage) throws IOException {
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

    }

    @FXML
    void viewBalance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/balanceWindow.fxml"));
            Parent root = loader.load();
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
