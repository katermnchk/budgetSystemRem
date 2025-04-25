package client.controllers.user;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Connect;
import models.TransactionRequest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddExpenseController implements Initializable {
    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> accountComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    private ArrayList<Account> accounts;
    private ArrayList<Category> categories;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAccountsAndCategories();
    }

    private void loadAccountsAndCategories() {
        Connect.client.sendMessage("getUserAccounts");
        Connect.client.sendObject(Connect.id);
        accounts = (ArrayList<Account>) Connect.client.readObject();
        accountComboBox.setItems(FXCollections.observableArrayList(
                accounts.stream().map(Account::getName).toList()
        ));
        if (!accounts.isEmpty()) {
            accountComboBox.getSelectionModel().select(0);
        }

        Connect.client.sendMessage("getExpenseCategories");
        categories = (ArrayList<Category>) Connect.client.readObject();
        categoryComboBox.setItems(FXCollections.observableArrayList(
                categories.stream().map(Category::getName).toList()
        ));
        if (!categories.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    @FXML
    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert("Ошибка", "Введите положительную сумму.");
                return;
            }

            int accountIndex = accountComboBox.getSelectionModel().getSelectedIndex();
            int categoryIndex = categoryComboBox.getSelectionModel().getSelectedIndex();
            if (accountIndex < 0 || categoryIndex < 0) {
                showAlert("Ошибка", "Выберите счет и категорию.");
                return;
            }

            Connect.client.sendMessage("addExpense");
            Connect.client.sendObject(new TransactionRequest(
                    Connect.id,
                    accounts.get(accountIndex).getId(),
                    categories.get(categoryIndex).getId(),
                    amount,
                    "Расход"
            ));

            String response = Connect.client.readMessage();
            if ("OK".equals(response)) {
                showAlert("Успех", "Расход добавлен: " + amount + " BYN");
                closeWindow();
            } else {
                showAlert("Ошибка", response);
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите числовое значение.");
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось связаться с сервером: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
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