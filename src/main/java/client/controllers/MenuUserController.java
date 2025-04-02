package client.controllers;

import client.clientWork.Connect;
import client.util.WindowChanger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import server.DB.UserDAO;
import server.SystemOrg.Role;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class MenuUserController {
    @FXML
    public Button addIncomeButton;

    @FXML
    public Button addExpenseButton;

    @FXML
    public Button viewBalanceButton;

    @FXML
    private Button backButton;

    @FXML
    private Button personalInfButton;

   private UserDAO userDAO;

  public MenuUserController() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/budgetsystem", "postgres", "postgresql");
            this.userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            showAlert("Ошибка подключения к БД", "Проверьте подключение к базе данных.");
        }
    }

    @FXML
    void backToMain(ActionEvent event) {
        backButton.getScene().getWindow().hide();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/client/main.fxml"));

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene((root)));
        stage.show();
    }

    @FXML
    void persInf(ActionEvent event) throws IOException {
        try {
            Connect.client.sendMessage("userInf");
            Role r = new Role();
            r.setId(Connect.id);
            Connect.client.sendObject(r);
            WindowChanger.changeWindow(getClass(), personalInfButton, "profile.fxml", "profile", false);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть профиль: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void addIncome() throws SQLException {
        double amount = getAmountFromUser("Добавление дохода", "Введите сумму дохода:");
        if (amount > 0) {
            //userDAO.addTransaction(amount, "Доход");
            showAlert("Доход добавлен", "Вы добавили доход: " + amount + " BYN");
        }
    }

    @FXML
    void addExpense() throws SQLException {
        double amount = getAmountFromUser("Добавление расхода", "Введите сумму расхода:");
        if (amount > 0) {
            //userDAO.addTransaction(-amount, "Расход");
            showAlert("Расход добавлен", "Вы добавили расход: " + amount + " BYN");
        }
    }

    @FXML
    void viewBalance() {
        double balance = userDAO.getBalance();
        showAlert("Текущий баланс", "Ваш баланс: " + balance + " BYN");
    }

    @FXML
    void viewChart() {
        showAlert("График расходов", "Функция в разработке");
    }

    @FXML
    void manageCategories() {
        showAlert("Категории", "Функция управления категориями в разработке");
    }

    @FXML
    void viewHistory() {
        String history = userDAO.getTransactionHistory();
        showAlert("История операций", history);
    }

    /*@FXML
    void viewProfile(ActionEvent actionEvent) throws IOException {
        Connect.client.sendMessage("UserInf");
        Role r = new Role();
        r.setId(Connect.id);
        Connect.client.sendObject(r);
        WindowChanger.changeWindow(getClass(), backButton, "/client/profile.fxml", "Профиль", false);
    }*/


    private double getAmountFromUser(String title, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        try {
            return result.map(Double::parseDouble).orElse(0.0);
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное число");
            return 0.0;
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
