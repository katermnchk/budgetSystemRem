package client.controllers;

import client.clientWork.Connect;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import models.Authorization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 9006;

    @FXML
    private void login() {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Заполните все поля!");
            return;
        }

        Connect.client.sendMessage("LOGIN");
        Authorization auth = new Authorization();
        auth.setLogin(usernameField.getText());
        auth.setPassword(passwordField.getText());
        Connect.client.sendObject(auth);

        try {
            String response = Connect.client.readMessage();
            showAlert(response);
        } catch (Exception e) {
            showAlert("Ошибка связи с сервером!");
        }    }

    @FXML
    private void register() {
        sendRequest("REGISTER " + usernameField.getText() + " " +
                passwordField.getText());
    }

    private void sendRequest(String request) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            String response = (String) in.readObject();

            showAlert(response);

        } catch (Exception e) {
            showAlert("Ошибка подключения к серверу!");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ответ сервера");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}