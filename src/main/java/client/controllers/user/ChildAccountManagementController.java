package client.controllers.user;

import client.clientWork.Client;
import client.clientWork.Account;
import client.clientWork.Users;
import client.util.ClientDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

public class ChildAccountManagementController {
    private static final Logger LOGGER = Logger.getLogger(ChildAccountManagementController.class.getName());
    private Client client;
    private int currentUserId;
    private final ObservableList<Users> childrenList = FXCollections.observableArrayList();
    private final ObservableList<Account> accountsList = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Users> childComboBox;
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
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField loginField;
    @FXML
    private TextField passwordField;

    private Stage stage;

    @FXML
    public void initialize() {
        LOGGER.info("Инициализация контроллера, currentUserId: " + currentUserId + ", client: " + client + ", this: " + this);
        if (currentUserId == 0) {
            LOGGER.warning("currentUserId равен 0");
        }
        if (client == null) {
            LOGGER.warning("Клиент не инициализирован");
        }
        //stage.setMaximized(true);

        childComboBox.setItems(childrenList);
        childComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Users user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        });
        childComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Users user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        });

        accountIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        accountBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        accountBlockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        accountsTable.setItems(accountsList);

        childComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadChildAccounts(newValue.getId());
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (stage != null && !stage.isMaximized()) {
            stage.setMaximized(true);
        }
    }

    public void setClient(Client client) {
        LOGGER.info("Установка клиента Client: " + client + ", this: " + this);
        if (client == null) {
            LOGGER.warning("Клиент null в setClient");
        }
        this.client = client;
        if (this.client != null && this.currentUserId != 0) {
            initData();
        }
    }

    public void setCurrentUserId(int currentUserId) {
        LOGGER.info("Установка currentUserId: " + currentUserId + ", client: " + client + ", this: " + this);
        this.currentUserId = currentUserId;
        if (currentUserId == 0) {
            LOGGER.warning("currentUserId равен 0");
        }
        if (this.client != null && this.currentUserId != 0) {
            initData();
        }
    }

    private void initData() {
        LOGGER.info("Инициализация данных: currentUserId=" + currentUserId + ", client=" + client);
        if (client == null || currentUserId == 0) {
            LOGGER.warning("Невозможно загрузить данные: client=" + client + ", currentUserId=" + currentUserId);
            ClientDialog.showAlert("Ошибка", "Клиент или пользователь не инициализированы.");
            return;
        }
        loadChildren();
    }

    private void loadChildren() {
        try {
            LOGGER.info("Запрос getChildren для userId: " + currentUserId);
            client.sendMessage("getChildren");
            client.sendObject(currentUserId);
            Object response = client.readObject();
            if (response instanceof ArrayList<?> responseList) {
                childrenList.setAll((ArrayList<Users>) responseList);
                LOGGER.info("Загружено " + childrenList.size() + " детей");
                if (!childrenList.isEmpty()) {
                    childComboBox.getSelectionModel().selectFirst();
                }
            } else if (response instanceof String errorMessage) {
                LOGGER.warning("Ошибка сервера при getChildren: " + errorMessage);
                ClientDialog.showAlert("Ошибка", "Не удалось загрузить список детей: " + errorMessage);
                childrenList.clear();
            } else {
                LOGGER.warning("Неожиданный тип ответа для getChildren: " + (response != null ? response.getClass().getName() : "null"));
                ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при загрузке детей.");
                childrenList.clear();
            }
        } catch (Exception e) {
            LOGGER.severe("Ошибка при загрузке детей: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Ошибка при загрузке списка детей: " + e.getMessage());
            childrenList.clear();
        }
    }


    @FXML
    private void addChild() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || login.isEmpty() || password.isEmpty()) {
            LOGGER.warning("Попытка добавить ребенка с пустыми полями");
            ClientDialog.showAlert("Ошибка", "Все поля должны быть заполнены.");
            return;
        }

        try {
            LOGGER.info("Отправка addChild для parentId: " + currentUserId + ", login: " + login);
            client.sendMessage("addChild");
            Users newChild = new Users();
            newChild.setFirstname(firstName);
            newChild.setLastname(lastName);
            newChild.setLogin(login);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            newChild.setPassword(hashedPassword);
            newChild.setRole("CHILD");
            newChild.setStatus("ACTIVE");
            client.sendObject(newChild);
            client.sendObject(currentUserId);
            Object response = client.readObject();
            if ("OK".equals(response)) {
                childrenList.add(newChild);
                firstNameField.clear();
                lastNameField.clear();
                loginField.clear();
                passwordField.clear();
                LOGGER.info("Ребенок успешно добавлен: login=" + login);
                ClientDialog.showAlert("Успех", "Ребенок успешно добавлен.");
            } else if (response instanceof String errorMessage) {
                LOGGER.warning("Ошибка сервера при addChild: " + errorMessage);
                ClientDialog.showAlert("Ошибка", "Не удалось добавить ребенка: " + errorMessage);
            } else {
                LOGGER.warning("Неожиданный тип ответа для addChild: " + (response != null ? response.getClass().getName() : "null"));
                ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при добавлении ребенка.");
            }
        } catch (Exception e) {
            LOGGER.severe("Ошибка при добавлении ребенка: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Ошибка при добавлении ребенка: " + e.getMessage());
        }
    }

    @FXML
    private void toggleBlockAccount() {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            LOGGER.warning("Попытка блокировки/разблокировки без выбранного счета");
            ClientDialog.showAlert("Ошибка", "Выберите счет для блокировки/разблокировки.");
            return;
        }

        try {
            LOGGER.info("Запрос toggleBlockAccount для accountId: " + selectedAccount.getId());
            client.sendMessage("toggleBlockAccount");
            client.sendObject(selectedAccount.getId());
            Object response = client.readObject();
            if ("OK".equals(response)) {
                selectedAccount.setBlocked(!selectedAccount.isBlocked());
                accountsTable.refresh();
                LOGGER.info("Счет успешно заблокирован/разблокирован: id=" + selectedAccount.getId());
                ClientDialog.showAlert("Успех", "Счет успешно " + (selectedAccount.isBlocked() ? "заблокирован" : "разблокирован") + ".");
            } else if (response instanceof String errorMessage) {
                LOGGER.warning("Ошибка сервера при toggleBlockAccount: " + errorMessage);
                ClientDialog.showAlert("Ошибка", "Не удалось изменить статус счета: " + errorMessage);
            } else {
                LOGGER.warning("Неожиданный тип ответа для toggleBlockAccount: " + (response != null ? response.getClass().getName() : "null"));
                ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при изменении статуса счета.");
            }
        } catch (Exception e) {
            LOGGER.severe("Ошибка при блокировке/разблокировке счета: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Ошибка при блокировке/разблокировке счета: " + e.getMessage());
        }
    }

    private void loadChildAccounts(int childId) {
        try {
            LOGGER.info("Запрос getChildAccounts для childId: " + childId);
            client.sendMessage("getChildAccounts");
            client.sendObject(childId);
            Object response = client.readObject();
            if (response instanceof ArrayList<?> responseList) {
                accountsList.setAll((ArrayList<Account>) responseList);
                LOGGER.info("Загружено " + accountsList.size() + " счетов для childId=" + childId);
            } else if (response instanceof String errorMessage) {
                LOGGER.warning("Ошибка сервера при getChildAccounts: " + errorMessage);
                ClientDialog.showAlert("Ошибка", "Не удалось загрузить счета ребенка: " + errorMessage);
                accountsList.clear();
            } else {
                LOGGER.warning("Неожиданный тип ответа для getChildAccounts: " + (response != null ? response.getClass().getName() : "null"));
                ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при загрузке счетов.");
                accountsList.clear();
            }
        } catch (Exception e) {
            LOGGER.severe("Ошибка при загрузке счетов ребенка: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Ошибка при загрузке счетов ребенка: " + e.getMessage());
            accountsList.clear();
        }
    }

    @FXML
    private void loadChildAccounts(ActionEvent actionEvent) {
        Users selectedChild = childComboBox.getSelectionModel().getSelectedItem();
        if (selectedChild == null) {
            LOGGER.warning("Попытка загрузки счетов без выбранного ребенка");
            ClientDialog.showAlert("Ошибка", "Выберите ребенка для загрузки счетов.");
            return;
        }
        loadChildAccounts(selectedChild.getId());
    }

    @FXML
    private void addAccount(ActionEvent actionEvent) {
        Users selectedChild = childComboBox.getSelectionModel().getSelectedItem();
        if (selectedChild == null) {
            LOGGER.warning("Попытка добавления счета без выбранного ребенка");
            ClientDialog.showAlert("Ошибка", "Выберите ребенка для добавления счета.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить счет");
        dialog.setHeaderText("Введите название счета для ребенка: " + selectedChild.getFullName());
        dialog.setContentText("Название счета:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                LOGGER.warning("Попытка добавить счет с пустым названием");
                ClientDialog.showAlert("Ошибка", "Название счета не может быть пустым.");
                return;
            }

            try {
                LOGGER.info("Отправка addAccount для childId: " + selectedChild.getId() + ", name: " + name);
                client.sendMessage("addAccount");
                Account newAccount = new Account();
                newAccount.setName(name.trim());
                newAccount.setBalance(0.0);
                newAccount.setBlocked(false);
                client.sendObject(newAccount);
                client.sendObject(selectedChild.getId());
                Object response = client.readObject();
                if ("OK".equals(response)) {
                    accountsList.add(newAccount);
                    accountsTable.refresh();
                    LOGGER.info("Счет успешно добавлен: name=" + name + ", childId=" + selectedChild.getId());
                    ClientDialog.showAlert("Успех", "Счет успешно добавлен.");
                } else if (response instanceof String errorMessage) {
                    LOGGER.warning("Ошибка сервера при addAccount: " + errorMessage);
                    ClientDialog.showAlert("Ошибка", "Не удалось добавить счет: " + errorMessage);
                } else {
                    LOGGER.warning("Неожиданный тип ответа для addAccount: " + (response != null ? response.getClass().getName() : "null"));
                    ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при добавлении счета.");
                }
            } catch (Exception e) {
                LOGGER.severe("Ошибка при добавлении счета: " + e.getMessage());
                ClientDialog.showAlert("Ошибка", "Ошибка при добавлении счета: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editAccount(ActionEvent actionEvent) {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            LOGGER.warning("Попытка редактирования без выбранного счета");
            ClientDialog.showAlert("Ошибка", "Выберите счет для редактирования.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedAccount.getName());
        dialog.setTitle("Редактировать счет");
        dialog.setHeaderText("Введите новое название счета:");
        dialog.setContentText("Название счета:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                LOGGER.warning("Попытка редактирования счета с пустым названием");
                ClientDialog.showAlert("Ошибка", "Название счета не может быть пустым.");
                return;
            }

            try {
                LOGGER.info("Отправка editAccount для accountId: " + selectedAccount.getId() + ", newName: " + name);
                client.sendMessage("editAccount");
                client.sendObject(selectedAccount.getId());
                client.sendObject(name.trim());
                Object response = client.readObject();
                if ("OK".equals(response)) {
                    selectedAccount.setName(name.trim());
                    accountsTable.refresh();
                    LOGGER.info("Счет успешно отредактирован: id=" + selectedAccount.getId() + ", newName=" + name);
                    ClientDialog.showAlert("Успех", "Счет успешно отредактирован.");
                } else if (response instanceof String errorMessage) {
                    LOGGER.warning("Ошибка сервера при editAccount: " + errorMessage);
                    ClientDialog.showAlert("Ошибка", "Не удалось отредактировать счет: " + errorMessage);
                } else {
                    LOGGER.warning("Неожиданный тип ответа для editAccount: " + (response != null ? response.getClass().getName() : "null"));
                    ClientDialog.showAlert("Ошибка", "Неожиданный ответ от сервера при редактировании счета.");
                }
            } catch (Exception e) {
                LOGGER.severe("Ошибка при редактировании счета: " + e.getMessage());
                ClientDialog.showAlert("Ошибка", "Ошибка при редактировании счета: " + e.getMessage());
            }
        });
    }

    @FXML
    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) accountsTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void topUpAccount() {
        Account selectedChildAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedChildAccount == null) {
            LOGGER.warning("Попытка пополнить счет без выбора счета");
            ClientDialog.showAlert("Ошибка", "Выберите счет ребенка");
            return;
        }
        if (selectedChildAccount.isBlocked()) {
            LOGGER.warning("Попытка пополнить заблокированный счет: id=" + selectedChildAccount.getId());
            ClientDialog.showAlert("Ошибка", "Нельзя пополнить заблокированный счет");
            return;
        }
        try {
            ArrayList<Account> parentAccounts = client.getParentAccounts(currentUserId);
            if (parentAccounts.isEmpty()) {
                LOGGER.warning("У родителя с id=" + currentUserId + " нет счетов");
                ClientDialog.showAlert("Ошибка", "У вас нет счетов для списания средств");
                return;
            }
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Пополнить счет");
            dialog.setHeaderText("Пополнение счета: " + selectedChildAccount.getName());
            ButtonType confirmButtonType = new ButtonType("Пополнить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            ComboBox<Account> parentAccountComboBox = new ComboBox<>();
            parentAccountComboBox.setItems(FXCollections.observableArrayList(parentAccounts));
            parentAccountComboBox.setPromptText("Выберите ваш счет");
            parentAccountComboBox.setCellFactory(lv -> new ListCell<Account>() {
                @Override
                protected void updateItem(Account account, boolean empty) {
                    super.updateItem(account, empty);
                    setText(empty || account == null ? "" : account.getName() + " (Баланс: " + account.getBalance() + ")");
                }
            });
            parentAccountComboBox.setButtonCell(new ListCell<Account>() {
                @Override
                protected void updateItem(Account account, boolean empty) {
                    super.updateItem(account, empty);
                    setText(empty || account == null ? "" : account.getName() + " (Баланс: " + account.getBalance() + ")");
                }
            });
            TextField amountField = new TextField();
            amountField.setPromptText("Введите сумму");
            grid.add(new Label("Ваш счет:"), 0, 0);
            grid.add(parentAccountComboBox, 1, 0);
            grid.add(new Label("Сумма:"), 0, 1);
            grid.add(amountField, 1, 1);
            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    return amountField.getText();
                }
                return null;
            });
            dialog.showAndWait().ifPresent(amountStr -> {
                try {
                    double amount = Double.parseDouble(amountStr);
                    Account selectedParentAccount = parentAccountComboBox.getSelectionModel().getSelectedItem();
                    if (selectedParentAccount == null) {
                        LOGGER.warning("Счет родителя не выбран");
                        ClientDialog.showAlert("Ошибка", "Выберите ваш счет");
                        return;
                    }
                    if (amount <= 0) {
                        LOGGER.warning("Недопустимая сумма пополнения: " + amount);
                        ClientDialog.showAlert("Ошибка", "Сумма должна быть положительной");
                        return;
                    }
                    LOGGER.info("Отправка topUpChildAccount: childAccountId=" + selectedChildAccount.getId() +
                            ", parentAccountId=" + selectedParentAccount.getId() + ", amount=" + amount);
                    String result = client.topUpChildAccount(
                            selectedChildAccount.getId(), selectedParentAccount.getId(), amount);
                    if ("OK".equals(result)) {
                        loadChildAccounts(childComboBox.getSelectionModel().getSelectedItem().getId());
                        LOGGER.info("Счет успешно пополнен: childAccountId=" + selectedChildAccount.getId() +
                                ", parentAccountId=" + selectedParentAccount.getId() + ", amount=" + amount);
                    } else {
                        LOGGER.warning("Ошибка сервера при пополнении счета: " + result);
                        ClientDialog.showAlert("Ошибка", result);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Недопустимый формат суммы: " + amountStr);
                    ClientDialog.showAlert("Ошибка", "Введите корректную сумму");
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.severe("Ошибка пополнения счета: " + e.getMessage());
                    ClientDialog.showAlert("Ошибка", "Не удалось пополнить счет: " + e.getMessage());
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe("Ошибка загрузки счетов родителя: " + e.getMessage());
            ClientDialog.showAlert("Ошибка", "Не удалось загрузить ваши счета: " + e.getMessage());
        }
    }
}
