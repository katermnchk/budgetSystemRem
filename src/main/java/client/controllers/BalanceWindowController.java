package client.controllers;

import client.clientWork.Connect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class BalanceWindowController implements Initializable {
    @FXML
    private Label balanceLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshBalance(); // Загружаем баланс при открытии окна
    }

    @FXML
    private void refreshBalance() {
        try {
            Connect.client.sendMessage("getBalance");
            Connect.client.sendObject(Connect.id);

            String response = Connect.client.readMessage();
            double balance = Double.parseDouble(response);

            DecimalFormat df = new DecimalFormat("#0.00");
            balanceLabel.setText(df.format(balance) + " BYN");
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректный формат баланса от сервера.");
            e.printStackTrace();
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось связаться с сервером: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) balanceLabel.getScene().getWindow();
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