package client.controllers;

import client.clientWork.Connect;
import client.controllers.admin.MenuAdminController;
import client.controllers.user.MenuUserController;
import client.util.ClientDialog;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import models.Authorization;
import server.SystemOrg.Role;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.logging.Logger;


public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    @FXML
    private TextField  login;
    @FXML
    private TextField  password;
    @FXML
    private Button  enterButton;
    @FXML
    private Button  registrationButton;

    @FXML
    void authorization(ActionEvent event) {
        if (checkInput()) {
            LOGGER.warning("Поля логина или пароля пусты");
            ClientDialog.showAlertWithNullInput();
            return;
        }

        if (Connect.client == null) {
            LOGGER.severe("Connect.client не инициализирован");
            ClientDialog.showAlert("Ошибка", "Клиент не инициализирован. Проверьте соединение с сервером.");
            return;
        }

        LOGGER.info("Отправка запроса на авторизацию, login: " + login.getText());
        Connect.client.sendMessage("authorization");
        Authorization auth = new Authorization();
        auth.setLogin(login.getText().trim());
        auth.setPassword(password.getText().trim());
        Connect.client.sendObject(auth);

        Object response = Connect.client.readObject();
        if ("There is no data!".equals(response)) {
            LOGGER.warning("Авторизация не удалась: данные отсутствуют");
            ClientDialog.showAlertWithNoLogin();
            return;
        }

        if (!"OK".equals(response)) {
            LOGGER.warning("Неожиданный ответ сервера: " + response);
            ClientDialog.showAlert("Ошибка", "Ошибка авторизации: " + response);
            return;
        }

        Object roleResponse = Connect.client.readObject();
        if (!(roleResponse instanceof Role)) {
            LOGGER.warning("Неожиданный тип ответа для Role: " + (roleResponse != null ? roleResponse.getClass().getName() : "null"));
            ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при авторизации.");
            return;
        }

        Role role = (Role) roleResponse;
        Connect.id = role.getId();
        Connect.role = role.getRole();
        LOGGER.info("Авторизация успешна: id=" + Connect.id + ", role=" + Connect.role);

        if (Connect.id == 0 || Connect.role.isEmpty()) {
            LOGGER.warning("Некорректные данные авторизации: id=" + Connect.id + ", role=" + Connect.role);
            ClientDialog.showAlert("Ошибка", "Некорректные данные авторизации.");
            return;
        }

        String fxmlPath = switch (Connect.role) {
            case "USER" -> "/client/menu.fxml";
            case "ADMIN" -> "/client/menuAdmin.fxml";
            case "CHILD" -> "/client/menuChild.fxml";
            default -> {
                LOGGER.warning("Неизвестная роль: " + Connect.role);
                ClientDialog.showAlert("Ошибка", "Неизвестная роль пользователя: " + Connect.role);
                yield null;
            }
        };

        if (Objects.equals(fxmlPath, "/client/menu.fxml")) {
            try {
                Stage stage = (Stage) enterButton.getScene().getWindow();
                MenuUserController controller = MenuUserController.openMenuUserController(stage);
                controller.setClient(Connect.client);
                controller.setCurrentUserId(Connect.id);
                LOGGER.info("Передача client и currentUserId в MenUserController: id=" + Connect.id);
            } catch (Exception e) {
                LOGGER.severe("Ошибка : " + e.getMessage());
            }
        } else if (Objects.equals(fxmlPath, "/client/menuAdmin.fxml")) {
            try {
                Stage stage = (Stage) enterButton.getScene().getWindow();
                MenuAdminController.openMenuAdminController(stage);
                LOGGER.info("Передача client и currentUserId в MenuAdminController: id=" + Connect.id);
            } catch (Exception e) {
                LOGGER.severe("Ошибка : " + e.getMessage());
            }
        } else {
            try {
                Stage stage = (Stage) enterButton.getScene().getWindow();
                MenuChild controller = MenuChild.openMenuChild(stage);
                controller.setClient(Connect.client);
                controller.setCurrentUserId(Connect.id);
                LOGGER.info("Передача client и currentUserId в MenuChildController: id=" + Connect.id);
            } catch (Exception e) {
                LOGGER.severe("Ошибка : " + e.getMessage());
            }
        }
    }

    @FXML
    void registration(ActionEvent event) {

    }

    @FXML
    void initialize() {
        registrationButton.setOnAction(event -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/client/registration.fxml"));

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Parent root = loader.getRoot();
            Stage stage = (Stage) enterButton.getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Screen bounds set: X=" + screenBounds.getMinX() +
                    ", Y=" + screenBounds.getMinY() + ", Width=" + screenBounds.getWidth() +
                    ", Height=" + screenBounds.getHeight());


            stage.setMaximized(true);
            stage.setScene(new Scene((root)));
            stage.show();
        });
    }

    private boolean checkInput() {
        try {
            return login.getText().equals("") || password.getText().equals("");
        }
        catch (Exception e) {
            System.out.println("Error");
            return true;
        }
    }
}