package client.controllers.admin;

import client.clientWork.Connect;
import client.clientWork.Users;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class UserManagementController {

    @FXML private TableView<Users> userTable;
    @FXML private TableColumn<Users, Integer> idColumn;
    @FXML private TableColumn<Users, String> loginColumn;
    @FXML private TableColumn<Users, String> roleColumn;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button backButton;

    private ObservableList<Users> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (userTable == null || idColumn == null || loginColumn == null || roleColumn == null ||
                addUserButton == null || editUserButton == null || deleteUserButton == null || backButton == null) {
            System.err.println("Ошибка: Один или несколько элементов FXML не инициализированы!");
            System.err.println("userTable: " + userTable);
            System.err.println("idColumn: " + idColumn);
            System.err.println("loginColumn: " + loginColumn);
            System.err.println("roleColumn: " + roleColumn);
            System.err.println("addUserButton: " + addUserButton);
            System.err.println("editUserButton: " + editUserButton);
            System.err.println("deleteUserButton: " + deleteUserButton);
            System.err.println("backButton: " + backButton);
            return;
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));

        userTable.setItems(userList);
        loadUsers();
    }

    private void loadUsers() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connect.client.sendMessage("getUsers");
                    Object response = Connect.client.readObject();
                    if (response instanceof ArrayList<?>) {
                        ArrayList<Users> users = (ArrayList<Users>) response;
                        javafx.application.Platform.runLater(() -> {
                            userList.setAll(users);
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить пользователей: " + response);
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка связи с сервером: " + e.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    void addUser(ActionEvent event) {
        Dialog<Users> dialog = new Dialog<>();
        dialog.setTitle("Добавить пользователя");
        dialog.setHeaderText("Введите данные нового пользователя");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField loginField = new TextField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField passwordField = new TextField();
        ChoiceBox<String> roleField = new ChoiceBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleField.setValue("USER");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("Логин:"), loginField);
        grid.addRow(1, new Label("Имя:"), firstNameField);
        grid.addRow(2, new Label("Фамилия:"), lastNameField);
        grid.addRow(3, new Label("Пароль:"), passwordField);
        grid.addRow(4, new Label("Роль:"), roleField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Проверка", "Логин и пароль не могут быть пустыми.");
                    return null;
                }

                Users newUser = new Users();
                newUser.setLogin(loginField.getText());
                newUser.setFirstname(firstNameField.getText());
                newUser.setLastname(lastNameField.getText());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleField.getValue());
                return newUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Connect.client.sendMessage("registrationUser");
                        Connect.client.sendObject(user);
                        Object response = Connect.client.readObject();
                        if ("OK".equals(response)) {
                            Connect.client.readObject(); // Пропускаем Role
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь добавлен.");
                                loadUsers();
                            });
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить пользователя: " + response);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при добавлении пользователя: " + e.getMessage());
                    }
                    return null;
                }
            };
            new Thread(task).start();
        });
    }

    @FXML
    void editUser(ActionEvent event) {
        Users selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Выберите пользователя для редактирования.");
            return;
        }

        Dialog<Users> dialog = new Dialog<>();
        dialog.setTitle("Редактировать пользователя");
        dialog.setHeaderText("Измените данные пользователя");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField loginField = new TextField(selectedUser.getLogin());
        TextField firstNameField = new TextField(selectedUser.getFirstname());
        TextField lastNameField = new TextField(selectedUser.getLastname());
        TextField passwordField = new TextField(selectedUser.getPassword());
        ChoiceBox<String> roleField = new ChoiceBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleField.setValue(selectedUser.getRole() != null ? selectedUser.getRole() : "USER");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("Логин:"), loginField);
        grid.addRow(1, new Label("Имя:"), firstNameField);
        grid.addRow(2, new Label("Фамилия:"), lastNameField);
        grid.addRow(3, new Label("Пароль:"), passwordField);
        grid.addRow(4, new Label("Роль:"), roleField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Проверка", "Логин и пароль не могут быть пустыми.");
                    return null;
                }
                selectedUser.setLogin(loginField.getText());
                selectedUser.setFirstname(firstNameField.getText());
                selectedUser.setLastname(lastNameField.getText());
                selectedUser.setPassword(passwordField.getText());
                selectedUser.setRole(roleField.getValue());
                return selectedUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Connect.client.sendMessage("editUser");
                        Connect.client.sendObject(user);
                        Object response = Connect.client.readObject();
                        if ("OK".equals(response)) {
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь обновлен.");
                                loadUsers();
                            });
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить пользователя: " + response);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при редактировании пользователя: " + e.getMessage());
                    }
                    return null;
                }
            };
            new Thread(task).start();
        });
    }

    @FXML
    void deleteUser(ActionEvent event) {
        Users selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Выберите пользователя для удаления.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connect.client.sendMessage("deleteUser");
                    Connect.client.sendObject(selectedUser);
                    Object response = Connect.client.readObject();
                    if ("OK".equals(response)) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь удален.");
                            loadUsers();
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить пользователя: " + response);
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при удалении пользователя: " + e.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    void back(ActionEvent event) {
        backButton.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/admin/menuAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Меню администратора");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить меню администратора: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}