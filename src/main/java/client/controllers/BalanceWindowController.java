package client.controllers;

import client.clientWork.Connect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BalanceWindowController implements Initializable {
    @FXML
    private Label totalBalanceLabel;

    @FXML
    private TableView<AccountBalance> accountsTable;

    @FXML
    private TableColumn<AccountBalance, String> accountNameColumn;

    @FXML
    private TableColumn<AccountBalance, String> balanceColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        refreshBalance();
    }

    @FXML
    private void refreshBalance() {
        Connect.client.sendMessage("getAccountBalances");
        Connect.client.sendObject(Connect.id);

        Object response = Connect.client.readObject();
        if (response instanceof HashMap) {
            HashMap<String, Double> balances = (HashMap<String, Double>) response;

            accountsTable.getItems().clear();

            DecimalFormat df = new DecimalFormat("#0.00");
            double totalBalance = 0.0;

            for (Map.Entry<String, Double> entry : balances.entrySet()) {
                accountsTable.getItems().add(new AccountBalance(entry.getKey(), df.format(entry.getValue())));
                totalBalance += entry.getValue();
            }

            totalBalanceLabel.setText("Общий бюджет: " + df.format(totalBalance) + " BYN");
        } else {
            showAlert("Ошибка", "Не удалось загрузить данные о балансах: " + response);
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) totalBalanceLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class AccountBalance {
        private final String accountName;
        private final String balance;

        public AccountBalance(String accountName, String balance) {
            this.accountName = accountName;
            this.balance = balance;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getBalance() {
            return balance;
        }
    }
}