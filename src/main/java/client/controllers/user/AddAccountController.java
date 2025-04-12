package client.controllers.user;

import client.clientWork.Account;
import client.clientWork.Connect;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AddAccountController {
    @FXML
    private TextField nameField;

    @FXML
    private void addAccount() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название счета.");
            return;
        }

        try {
            Account account = new Account(0, name);
            Connect.client.sendMessage("addAccount");
            Connect.client.sendObject(account);
            Connect.client.sendObject(Connect.id);

            String response = Connect.client.readMessage();
            if ("OK".equals(response)) {
                showAlert("Успех", "Счет '" + name + "' успешно добавлен.");
                closeWindow();
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось добавить счет: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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