package client.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;

public class MenuAdminController {
    private static final Logger LOGGER = Logger.getLogger(MenuAdminController.class.getName());

    @FXML
    private void openUserManagement() {
        openWindow("/client/userManagement.fxml", "Управление пользователями");
    }

    @FXML
    private void openCategoryManagement() {
        openWindow("/client/categoryManager.fxml", "Управление категориями");
    }

    @FXML
    private void openStatistics() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            AdminStatisticsController.openAdminStatistics(stage);

        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть историю транзакций: " + e.getMessage());
            LOGGER.severe("Ошибка открытия истории транзакций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private Button backButton;

    @Setter
    private Stage stage;

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

    private void openWindow(String fxmlPath, String title) {
       try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void openMenuAdminController(Stage primaryStage) throws IOException {
        String fxmlPath = "/client/menuAdmin.fxml";
        LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Пытаемся загрузить FXML из: " + fxmlPath);

        java.net.URL location = MenuAdminController.class.getResource(fxmlPath);
        if (location == null) {
            LOGGER.severe("[" + LocalDate.now() + " " + LocalTime.now() + "] Файл FXML не найден по пути: " + fxmlPath);
            throw new IOException("Не удаётся найти FXML файл по пути: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        MenuAdminController controller = loader.getController();
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
        LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Окно меню администратора открыто на весь экран");

    }

}