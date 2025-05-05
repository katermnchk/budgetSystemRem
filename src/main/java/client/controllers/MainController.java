package client.controllers;

import client.clientWork.Connect;
import client.controllers.user.MenuUserController;
import client.util.ClientDialog;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Authorization;
import server.SystemOrg.Role;

import java.io.IOException;
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

   /* @FXML
    void authorization(ActionEvent event) throws IOException {
        if (checkInput()) {
            ClientDialog.showAlertWithNullInput();
            return;
        }

        LOGGER.info("Отправка запроса на авторизацию, login: " + login.getText());
        Connect.client.sendMessage("authorization");
        Authorization auth = new Authorization();
        auth.setLogin(login.getText());
        auth.setPassword(password.getText());
        Connect.client.sendObject(auth);

        String mes = "";
        try {
            mes = Connect.client.readMessage();
        } catch (IOException ex) {
            System.out.println("Ошибка в чтении");
        }
        if (mes.equals("Данные отсутствуют!"))
            ClientDialog.showAlertWithNoLogin();
        else {
            Role r = (Role) Connect.client.readObject();
            Connect.id = r.getId();
            Connect.role = r.getRole();
            LOGGER.info("Авторизация успешна: id=" + Connect.id + ", role=" + Connect.role);

            enterButton.getScene().getWindow().hide();
            System.out.println(Connect.role);
            FXMLLoader loader = new FXMLLoader();


            if (Objects.equals(Connect.role, "USER")) {
                System.out.println("Окно пользователя");
                loader.setLocation(getClass().getResource("/client/menu.fxml"));
            }
            else if(Objects.equals(Connect.role, "ADMIN"))
                loader.setLocation(getClass().getResource("/client/menuAdmin.fxml"));
            else
                loader.setLocation(getClass().getResource("/client/menuAdmin.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ("USER".equals(Connect.role)) {
                MenuUserController controller = loader.getController();
                controller.setClient(Connect.client);
                controller.setCurrentUserId(Connect.id);
                LOGGER.info("Sending client and currentUserId in MenuUserController: id=" + Connect.id);
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene((root)));
            stage.show();
        }
    }
*/

    @FXML
    void authorization(ActionEvent event) {
        if (checkInput()) {
            LOGGER.warning("Поля логина или пароля пусты");
            ClientDialog.showAlertWithNullInput();
            return;
        }

        try {
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

            enterButton.getScene().getWindow().hide();

            String fxmlPath = switch (Connect.role) {
                case "USER" -> "/client/menu.fxml";
                case "ADMIN" -> "/client/menuAdmin.fxml";
                default -> {
                    LOGGER.warning("Неизвестная роль: " + Connect.role);
                    ClientDialog.showAlert("Ошибка", "Неизвестная роль пользователя: " + Connect.role);
                    yield null;
                }
            };

            if (fxmlPath != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                if ("USER".equals(Connect.role)) {
                    MenuUserController controller = loader.getController();
                    controller.setClient(Connect.client);
                    controller.setCurrentUserId(Connect.id);
                    LOGGER.info("Передача client и currentUserId в MenuUserController: id=" + Connect.id);
                }

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Меню " + Connect.role);
                stage.show();
            }
        } catch (IOException e) {
            LOGGER.severe("Ошибка при авторизации: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Ошибка при авторизации: " + e.getMessage());
        }
    }

    @FXML
    void registration(ActionEvent event) {

    }

    @FXML
    void initialize() {
        registrationButton.setOnAction(event -> {
            registrationButton.getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/client/registration.fxml"));

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene((root)));
            stage.show();
        });
    }

    private void openNewWindow(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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