package client.controllers.user;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Connect;
import client.clientWork.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

public class TransactionHistoryController {
    private static final Logger LOGGER = Logger.getLogger(TransactionHistoryController.class.getName());

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Timestamp> dateColumn;
    @FXML private TableColumn<Transaction, String> accountColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Account> accountComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    private ArrayList<Transaction> transactions;

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toLocalDateTime().toLocalDate().toString() + " " +
                            date.toLocalDateTime().toLocalTime().toString());
                }
            }
        });
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f BYN", amount));
                }
            }
        });
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String description, boolean empty) {
                super.updateItem(description, empty);
                setText(empty || description == null ? null : description);
            }
        });

        loadAccounts();
        loadCategories();
        typeComboBox.setItems(FXCollections.observableArrayList("Все типы", "Доход", "Расход"));
        typeComboBox.getSelectionModel().select("Все типы");

        loadTransactions(new HashMap<>());
    }

    private void loadAccounts() {
        Connect.client.sendMessage("getUserAccounts");
        Connect.client.sendObject(Connect.id);
        Object response = Connect.client.readObject();
        if (response instanceof ArrayList) {
            ArrayList<Account> accounts = (ArrayList<Account>) response;
            accountComboBox.setItems(FXCollections.observableArrayList(accounts));
            accountComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Account account, boolean empty) {
                    super.updateItem(account, empty);
                    setText(empty || account == null ? null : account.getName());
                }
            });
            accountComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Account account, boolean empty) {
                    super.updateItem(account, empty);
                    setText(empty || account == null ? "Все счета" : account.getName());
                }
            });
            LOGGER.info("Загружено " + accounts.size() + " счетов для фильтрации");
        } else {
            LOGGER.warning("Неверный ответ для getUserAccounts: " + response);
            showAlert("Ошибка", "Не удалось загрузить счета: неверный ответ сервера");
        }
    }

    private void loadCategories() {
        Connect.client.sendMessage("getAllCategories");
        Connect.client.sendObject(Connect.id);
        Object response = Connect.client.readObject();
        if (response instanceof ArrayList) {
            ArrayList<Category> categories = (ArrayList<Category>) response;
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    setText(empty || category == null ? null : category.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    setText(empty || category == null ? "Все категории" : category.getName());
                }
            });
            LOGGER.info("Loaded " + categories.size() + " categories for filtering");
        } else {
            LOGGER.warning("Неверный ответ для getAllCategories: " + response);
            showAlert("Ошибка", "Не удалось загрузить категории: неверный ответ сервера");
        }
    }

    private void loadTransactions(HashMap<String, Object> filters) {
        LOGGER.info("Отправка getTransactionHistory с фильтрами: " + filters);
        Connect.client.sendMessage("getTransactionHistory");
        Connect.client.sendObject(Connect.id);
        Connect.client.sendObject(filters);
        Object response = Connect.client.readObject();
        if (response instanceof ArrayList) {
            transactions = (ArrayList<Transaction>) response;
            transactionsTable.setItems(FXCollections.observableArrayList(transactions));
            LOGGER.info("Загружено " + transactions.size() + " транзакций для пользователя " + Connect.id + " с фильтрами: " + filters);
            if (transactions.isEmpty()) {
                showAlert("Информация", "Транзакции не найдены для заданных фильтров.");
            }
        } else {
            LOGGER.warning("Неверный ответ для getTransactionHistory: " + response);
            showAlert("Ошибка", "Не удалось загрузить транзакции: неверный ответ сервера");
        }
    }

    @FXML
    private void applyFilters() {
        try {
            HashMap<String, Object> filters = new HashMap<>();

            if (startDatePicker.getValue() != null) {
                filters.put("startDate", Timestamp.valueOf(startDatePicker.getValue().atStartOfDay()));
            }
            if (endDatePicker.getValue() != null) {
                filters.put("endDate", Timestamp.valueOf(endDatePicker.getValue().plusDays(1).atStartOfDay()));
            }

            if (accountComboBox.getValue() != null) {
                filters.put("accountId", accountComboBox.getValue().getId());
            }

            if (categoryComboBox.getValue() != null) {
                filters.put("categoryId", categoryComboBox.getValue().getId());
            }

            String selectedType = typeComboBox.getValue();
            if (selectedType != null && !selectedType.equals("Все типы")) {
                filters.put("categoryType", selectedType.equals("Доход") ? "INCOME" : "EXPENSE");
            }

            if (!descriptionField.getText().isEmpty()) {
                filters.put("description", descriptionField.getText().trim());
            }

            if (!minAmountField.getText().isEmpty()) {
                try {
                    filters.put("minAmount", Double.parseDouble(minAmountField.getText().trim()));
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Некорректный формат минимальной суммы.");
                    return;
                }
            }
            if (!maxAmountField.getText().isEmpty()) {
                try {
                    filters.put("maxAmount", Double.parseDouble(maxAmountField.getText().trim()));
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Некорректный формат максимальной суммы.");
                    return;
                }
            }

            LOGGER.info("Applying filters: " + filters);
            loadTransactions(filters);
        } catch (Exception e) {
            LOGGER.severe("Error in applyFilters: " + e.getMessage());
            showAlert("Ошибка", "Ошибка при применении фильтров: " + e.getMessage());
        }
    }

    @FXML
    private void clearFilters() {
        try {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            accountComboBox.getSelectionModel().clearSelection();
            categoryComboBox.getSelectionModel().clearSelection();
            typeComboBox.getSelectionModel().select("Все типы");
            descriptionField.clear();
            minAmountField.clear();
            maxAmountField.clear();
            LOGGER.info("Очистка фильтров");
            loadTransactions(new HashMap<>());
        } catch (Exception e) {
            LOGGER.severe("Ошибка в clearFilters: " + e.getMessage());
            showAlert("Ошибка", "Ошибка при сбросе фильтров: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        try {
            Stage stage = (Stage) transactionsTable.getScene().getWindow();
            stage.close();
            LOGGER.info("Окно истории транзакций закрыто");
        } catch (Exception e) {
            LOGGER.severe("Ошибка в closeWindow: " + e.getMessage());
            showAlert("Ошибка", "Ошибка при закрытии окна: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
