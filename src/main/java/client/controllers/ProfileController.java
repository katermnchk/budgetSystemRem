package client.controllers;

import client.clientWork.Connect;
import client.clientWork.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {
    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label loginLabel;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        Connect.client.sendMessage("userInf");
        Connect.client.sendObject(Connect.id);

        Object response = Connect.client.readObject();
        if (response instanceof Users) {
            Users user = (Users) response;
            firstNameLabel.setText(user.getFirstname() != null ? user.getFirstname() : "Не указано");
            lastNameLabel.setText(user.getLastname() != null ? user.getLastname() : "Не указано");
            loginLabel.setText(user.getLogin() != null ? user.getLogin() : "Не указано");
        } else {
            showAlert("Ошибка", "Не удалось загрузить данные пользователя: " + response);
        }
    }

    @FXML
    void backToMain(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}