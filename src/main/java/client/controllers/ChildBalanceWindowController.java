package client.controllers;

import client.clientWork.Account;
import client.clientWork.Client;
import client.util.ClientDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ChildBalanceWindowController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ChildBalanceWindowController.class.getName());
    private Client client;
    private int userId;
    private final ObservableList<Account> accountsList = FXCollections.observableArrayList();

    @FXML
    private Label totalBalanceLabel;

    @FXML
    private TableView<Account> accountsTable;

    @FXML
    private TableColumn<Account, Integer> accountIdColumn;

    @FXML
    private TableColumn<Account, String> accountNameColumn;

    @FXML
    private TableColumn<Account, Double> accountBalanceColumn;

    @FXML
    private TableColumn<Account, Boolean> accountBlockedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Инициализация ChildBalanceWindowController, location: " + location);
        if (accountIdColumn == null || accountNameColumn == null || accountBalanceColumn == null ||
                accountBlockedColumn == null || accountsTable == null || totalBalanceLabel == null) {
            LOGGER.severe("Одно или несколько FXML-полей не инициализированы: " +
                    "accountIdColumn=" + accountIdColumn + ", accountNameColumn=" + accountNameColumn +
                    ", accountBalanceColumn=" + accountBalanceColumn + ", accountBlockedColumn=" + accountBlockedColumn +
                    ", accountsTable=" + accountsTable + ", totalBalanceLabel=" + totalBalanceLabel);
            return;
        }

        accountIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        accountBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        accountBlockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        accountsTable.setItems(accountsList);
        LOGGER.info("TableView успешно инициализирован");

        if (client != null && userId != 0) {
            LOGGER.info("Вызов refreshBalance из initialize, так как client и userId уже установлены");
            refreshBalance();
        }
    }

    public void setClient(Client client) {
        LOGGER.info("Установка клиента: " + client);
        this.client = client;
        if (this.client != null && this.userId != 0) {
            LOGGER.info("Вызов refreshBalance из setClient");
            refreshBalance();
        }
    }

    public void setUserId(int userId) {
        LOGGER.info("Установка userId: " + userId);
        this.userId = userId;
        if (this.client != null && this.userId != 0) {
            LOGGER.info("Вызов refreshBalance из setUserId");
            refreshBalance();
        }
    }

    @FXML
    private void refreshBalance() {
        LOGGER.info("Начало refreshBalance для userId: " + userId);
        if (client == null || userId == 0) {
            LOGGER.warning("Клиент или userId не инициализированы: client=" + client + ", userId=" + userId);
            ClientDialog.showAlert("Ошибка", "Клиент или пользователь не инициализированы.");
            return;
        }

        try {
            LOGGER.info("Запрос getAccounts для userId: " + userId);
            ArrayList<Account> accounts = client.getAccounts(userId);
            LOGGER.info("Получено счетов: " + (accounts != null ? accounts.size() : "null"));
            accountsList.clear();

            if (accounts == null || accounts.isEmpty()) {
                LOGGER.warning("Счета не найдены для userId: " + userId);
                ClientDialog.showAlert("Информация", "У вас нет счетов для отображения.");
                totalBalanceLabel.setText("Общий бюджет: 0.00 BYN");
                return;
            }

            DecimalFormat df = new DecimalFormat("#0.00");
            double totalBalance = 0.0;

            for (Account account : accounts) {
                LOGGER.info("Добавление счета: id=" + account.getId() + ", name=" + account.getName() +
                        ", balance=" + account.getBalance() + ", blocked=" + account.isBlocked());
                accountsList.add(account);
                totalBalance += account.getBalance();
            }

            totalBalanceLabel.setText("Общий бюджет: " + df.format(totalBalance) + " BYN");
            accountsTable.refresh();
            LOGGER.info("Загружено " + accountsList.size() + " счетов для userId: " + userId);
        } catch (Exception e) {
            LOGGER.severe("Ошибка при загрузке счетов: " + e.getMessage());
            e.printStackTrace();
            ClientDialog.showAlert("Ошибка", "Не удалось загрузить счета: " + e.getMessage());
            accountsList.clear();
            totalBalanceLabel.setText("Общий бюджет: 0.00 BYN");
        }
    }

    @FXML
    private void closeWindow() {
        LOGGER.info("Закрытие окна баланса");
        Stage stage = (Stage) totalBalanceLabel.getScene().getWindow();
        stage.close();
    }
}