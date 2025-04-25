package client.controllers;

import client.clientWork.Account;
import client.clientWork.Connect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountManagementController {

    @FXML private ComboBox<Account> accountComboBox;
    @FXML private TextField newAccountNameField;
    @FXML private TextField addAccountNameField;
    @FXML private VBox accountInfoPane;
    @FXML private Label accountNameLabel;
    @FXML private Label balanceLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Text categoryStatsText;
    @FXML private Button backButton;

    @FXML
    private void initialize() {
        loadAccounts();
    }

    private void loadAccounts() {
        Connect.client.sendMessage("getUserAccounts");
        Connect.client.sendObject(Connect.id);
        ArrayList<Account> accounts = (ArrayList<Account>) Connect.client.readObject();
        accountComboBox.getItems().clear();
        accountComboBox.getItems().addAll(accounts);
    }

    @FXML
    private void addAccount() {
        String name = addAccountNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название счета.");
            return;
        }
        try {
            Connect.client.sendMessage("addAccount");
            Connect.client.sendObject(new Account(0, name));
            Connect.client.sendObject(Connect.id);
            String response = Connect.client.readMessage();
            showAlert(response.equals("OK") ? "Успех" : "Ошибка", response.equals("OK") ? "Счет добавлен." : response);
            loadAccounts();
            addAccountNameField.clear();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось добавить счет: " + e.getMessage());
        }
    }

    @FXML
    private void deleteAccount() {
        Account selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Ошибка", "Выберите счет для удаления.");
            return;
        }
        try {
            Connect.client.sendMessage("deleteAccount");
            Connect.client.sendObject(selectedAccount.getId());
            String response = Connect.client.readMessage();
            showAlert(response.equals("OK") ? "Успех" : "Ошибка", response.equals("OK") ? "Счет удален." : response);
            loadAccounts();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось удалить счет: " + e.getMessage());
        }
    }

    @FXML
    private void editAccount() {
        Account selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        String newName = newAccountNameField.getText().trim();
        if (selectedAccount == null || newName.isEmpty()) {
            showAlert("Ошибка", "Выберите счет и введите новое название.");
            return;
        }
        try {
            Connect.client.sendMessage("editAccount");
            HashMap<String, Object> accountData = new HashMap<>();
            accountData.put("accountId", selectedAccount.getId());
            accountData.put("newName", newName);
            Connect.client.sendObject(accountData);
            String response = Connect.client.readMessage();
            showAlert(response.equals("OK") ? "Успех" : "Ошибка", response.equals("OK") ? "Счет обновлен." : response);
            loadAccounts();
            newAccountNameField.clear();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось обновить счет: " + e.getMessage());
        }
    }

    @FXML
    private void viewAccountInfo() {
        Account selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Ошибка", "Выберите счет для просмотра.");
            return;
        }
        try {
            Connect.client.sendMessage("getAccountInfo");
            Connect.client.sendObject(selectedAccount.getId());
            String response = Connect.client.readMessage();
            if (response.equals("OK")) {
                HashMap<String, Object> accountInfo = (HashMap<String, Object>) Connect.client.readObject();
                accountNameLabel.setText("Счет: " + accountInfo.get("accountName"));
                balanceLabel.setText("Баланс: " + accountInfo.get("balance") + " BYN");
                transactionCountLabel.setText("Количество транзакций: " + accountInfo.get("transactionCount"));
                categoryStatsText.setText("Статистика по категориям: " + accountInfo.get("categoryStats").toString());
                accountInfoPane.setVisible(true);
            } else {
                showAlert("Ошибка", response);
                accountInfoPane.setVisible(false);
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось загрузить информацию о счете: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void backToMenu(ActionEvent actionEvent) {
        Stage stage = (Stage) accountComboBox.getScene().getWindow();
        stage.close();
    }
}