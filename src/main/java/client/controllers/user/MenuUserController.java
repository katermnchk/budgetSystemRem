package client.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
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
    private Button addAccountButton;

   /* @FXML
    private Button personalInfButton;

  public MenuUserController() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/budgetsystem", "postgres", "postgresql");
            this.userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            showAlert("Ошибка подключения к БД", "Проверьте подключение к базе данных.");
        }
    }*/

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
       /* try {
            Connect.client.sendMessage("userInf");
            Role r = new Role();
            r.setId(Connect.id);
            Connect.client.sendObject(r);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/client/profile.fxml"));
            //WindowChanger.changeWindow(getClass(), personalInfButton, "profile.fxml", "profile", false);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть профиль: " + e.getMessage());
            e.printStackTrace();
        }*/
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Профиль");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть профиль: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void addIncome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/addIncome.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Добавление дохода");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно добавления дохода.");
            e.printStackTrace();
        }
    }

    @FXML
    void addExpense() throws SQLException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/addExpense.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Добавление расхода");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно добавления расхода.");
            e.printStackTrace();
        }
    }

    @FXML
    void viewBalance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/balanceWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Текущий баланс");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно баланса: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void viewChart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/expenseChart.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("График расходов");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно графика расходов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void manageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/categoryManager.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление категориями");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно управления категориями: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void viewHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/transactionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("История транзакций");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть истории транзакций: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    @FXML
    void addAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/AddAccount.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Добавление счета");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно добавления счета: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
