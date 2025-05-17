package client.controllers;

import java.io.IOException;
import java.util.logging.Logger;

import client.clientWork.Connect;
import client.clientWork.Users;
import client.controllers.user.MenuUserController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import client.util.ClientDialog;
import server.SystemOrg.Role;

import static client.util.ClientDialog.showAlert;

public class RegistrationController {
    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());
    @FXML
    private Button backButton;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Button registrationButton;


    @FXML
    void initialize() {
        Stage stage = (Stage) registrationButton.getScene().getWindow();
        stage.setMaximized(true);
    }

    @FXML
    void backToMain(ActionEvent event) {
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
            e.printStackTrace();
        }
    }

    @FXML
    void registrationUser(ActionEvent event) throws IOException {
        if (checkInput())
            ClientDialog.showAlertWithNullInput();
        else if(password.getText().length()< 6) {
            ClientDialog.showAlertWithSmallPassword();
        }
        else if(!password.getText().equals(confirmPassword.getText())) {
            ClientDialog.showAlertWithPassword(); }
        else {
                Users user = new Users();
                user.setFirstname(firstName.getText());
                user.setLastname(lastName.getText());
                user.setLogin(login.getText());
                user.setPassword(password.getText());

                Connect.client.sendMessage("registrationUser");
                Connect.client.sendObject(user);
                System.out.println("Запись отправлена");

                String mes = "";
                try {
                    mes = Connect.client.readMessage();
                } catch (IOException ex) {
                    System.out.println("Ошибка в чтении");
                }
            if (mes.equals("Этот пользователь уже существует")) {
                ClientDialog.showAlertWithExistLogin();
            }
            else {
               // Role r = (Role) Connect.client.readObject();
                Object roleResponse = Connect.client.readObject();
                if (!(roleResponse instanceof Role)) {
                    LOGGER.warning("Неожиданный тип ответа для Role: " + (roleResponse != null ? roleResponse.getClass().getName() : "null"));
                    ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при авторизации.");
                    return;
                }

                Role r = (Role) roleResponse;

                Connect.id = r.getId();
                Connect.role = r.getRole();

                if (Connect.id == 0 || Connect.role.isEmpty()) {
                    LOGGER.warning("Некорректные данные авторизации: id=" + Connect.id + ", role=" + Connect.role);
                    ClientDialog.showAlert("Ошибка", "Некорректные данные авторизации.");
                    return;
                }

                try {
                    Stage stage = (Stage) registrationButton.getScene().getWindow();
                    //MenuUserController.openMenuUserController(stage);
                    MenuUserController controller = MenuUserController.openMenuUserController(stage);
                    controller.setClient(Connect.client);
                    controller.setCurrentUserId(Connect.id);

                    controller.setClient(Connect.client);
                    controller.setCurrentUserId(Connect.id);
                    } catch (Exception e) {
                        LOGGER.severe("Ошибка : " + e.getMessage());
                    }
                }

        }
    }

    private boolean checkInput() {
        try {
            return firstName.getText().isEmpty() || lastName.getText().isEmpty() ||
                    login.getText().isEmpty() || password.getText().isEmpty();
        } catch (Exception e) {
            System.out.println("Ошибка в checkInput: " + e.getMessage());
            return true;
        }
    }
}