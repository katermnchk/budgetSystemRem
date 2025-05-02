package client.controllers.user;

import client.clientWork.Account;
import client.clientWork.Connect;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

public class AccountManagementController {
    private static final Logger LOGGER = Logger.getLogger(AccountManagementController.class.getName());
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> nameColumn;
    @FXML private TableColumn<Account, Double> balanceColumn;

    private ArrayList<Account> accounts;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (balance == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f BYN", balance));
                }
            }
        });
        loadAccounts();
    }

    private void loadAccounts() {
        Connect.client.sendMessage("getUserAccounts");
        Connect.client.sendObject(Connect.id);
        Object response = Connect.client.readObject();
        if (response instanceof ArrayList) {
            accounts = (ArrayList<Account>) response;
            accountsTable.setItems(FXCollections.observableArrayList(accounts));
            LOGGER.info("Loaded " + accounts.size() + " accounts for user " + Connect.id);
            if (accounts.isEmpty()) {
                showAlert("Информация", "У вас пока нет счетов. Добавьте новый счет.");
            }
            for (Account account : accounts) {
                LOGGER.info("Account: id=" + account.getId() + ", name=" + account.getName() + ", balance=" + account.getBalance());
            }
        } else {
            LOGGER.warning("Invalid response for getUserAccounts: " + response);
            showAlert("Ошибка", "Не удалось загрузить счета: неверный ответ сервера");
        }
    }

    @FXML
    private void addAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить счет");
        dialog.setHeaderText("Введите название нового счета");
        dialog.setContentText("Название:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                Connect.client.sendMessage("addAccount");
                Connect.client.sendObject(new Account(0, name, 0.0));
                Connect.client.sendObject(Connect.id);
                String response = Connect.client.readMessage();
                if ("OK".equals(response)) {
                    showAlert("Успех", "Счет добавлен: " + name);
                    loadAccounts();
                } else {
                    showAlert("Ошибка", response);
                }
            } catch (IOException e) {
                LOGGER.severe("Error adding account: " + e.getMessage());
                showAlert("Ошибка", "Не удалось добавить счет: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editAccount() {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Ошибка", "Выберите счет для редактирования.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedAccount.getName());
        dialog.setTitle("Редактировать счет");
        dialog.setHeaderText("Введите новое название счета");
        dialog.setContentText("Название:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try {
                HashMap<String, Object> accountData = new HashMap<>();
                accountData.put("accountId", selectedAccount.getId());
                accountData.put("newName", newName);
                Connect.client.sendMessage("editAccount");
                Connect.client.sendObject(accountData);
                String response = Connect.client.readMessage();
                if ("OK".equals(response)) {
                    showAlert("Успех", "Счет обновлен: " + newName);
                    loadAccounts();
                } else {
                    showAlert("Ошибка", response);
                }
            } catch (IOException e) {
                LOGGER.severe("Error editing account: " + e.getMessage());
                showAlert("Ошибка", "Не удалось обновить счет: " + e.getMessage());
            }
        });
    }

    @FXML
    private void deleteAccount() {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Ошибка", "Выберите счет для удаления.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Подтверждение удаления");
        confirmDialog.setHeaderText("Удалить счет?");
        confirmDialog.setContentText("Вы уверены, что хотите удалить счет: " + selectedAccount.getName() + "?");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connect.client.sendMessage("deleteAccount");
                Connect.client.sendObject(selectedAccount.getId());
                String response = Connect.client.readMessage();
                if ("OK".equals(response)) {
                    showAlert("Успех", "Счет удален: " + selectedAccount.getName());
                    loadAccounts();
                } else {
                    showAlert("Ошибка", response);
                }
            } catch (IOException e) {
                LOGGER.severe("Error deleting account: " + e.getMessage());
                showAlert("Ошибка", "Не удалось удалить счет: " + e.getMessage());
            }
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) accountsTable.getScene().getWindow();
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