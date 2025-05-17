package client.controllers.user;

import client.clientWork.Client;
import client.clientWork.Connect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;

public class MenuUserController {
    private static final Logger LOGGER = Logger.getLogger(MenuUserController.class.getName());

    @FXML
    public Button addIncomeButton;

    @FXML
    public Button addExpenseButton;

    @FXML
    public Button viewBalanceButton;

    @FXML
    private Button backButton;

    @Setter
    private Stage stage;

    @FXML
    private Button addAccountButton;

    @FXML
    public Button manageGoalsButton;

    private Client client;
    private int currentUserId;

    @FXML
    private void initialize() {
        LOGGER.info("Инициализация MenuUserController, currentUserId: " + currentUserId + ", client: " + (client != null ? client.toString() : "null"));
        if (currentUserId == 0 || client == null) {
            LOGGER.warning("MenuUserController: currentUserId равен 0 или client null при инициализации");
        }
    }

    public void setClient(Client client) {
        this.client = client;
        LOGGER.info("MenuUserController: Установлено соединение Client: " + (client != null ? client.toString() : "null"));
        if (client == null) {
            LOGGER.warning("MenuUserController: Получен null в setClient");
        }
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
        LOGGER.info("MenuUserController: Установлен currentUserId: " + currentUserId);
        if (currentUserId == 0) {
            LOGGER.warning("MenuUserController: Получен currentUserId = 0");
        }
    }

    @FXML
    void backToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.setTitle("Авторизация");
            stage.show();
            backButton.getScene().getWindow().hide();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно авторизации: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна авторизации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void persInf(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Профиль");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть профиль: " + e.getMessage());
            LOGGER.severe("Ошибка открытия профиля: " + e.getMessage());
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
            showAlert("Ошибка", "Не удалось открыть окно добавления дохода: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна добавления дохода: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void addExpense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/addExpense.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Добавление расхода");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно добавления расхода: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна добавления расхода: " + e.getMessage());
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
            LOGGER.severe("Ошибка открытия окна баланса: " + e.getMessage());
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
            LOGGER.severe("Ошибка открытия окна графика расходов: " + e.getMessage());
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
            LOGGER.severe("Ошибка открытия окна управления категориями: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void viewHistory() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            TransactionHistoryController.openTransactionHistory(stage);

        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть историю транзакций: " + e.getMessage());
            LOGGER.severe("Ошибка открытия истории транзакций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void addAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/addAccount.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Добавление счета");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно добавления счета: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна добавления счета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void manageGoals(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/goals.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление целями");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно управления целями: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна управления целей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openAccountManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/AccountManagement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление счетами");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть управление счетами: " + e.getMessage());
            LOGGER.severe("Ошибка открытия окна управления счетами: " + e.getMessage());
            e.printStackTrace();
        }
    }

      @FXML
    private void manageChildAccounts() {
          try {
              LOGGER.info("Открытие окна управления счетами детей, currentUserId: " + currentUserId + ", client: " + (client != null ? client.toString() : "null") + ", Connect.client: " + (Connect.client != null ? Connect.client.toString() : "null"));
              if (currentUserId == 0) {
                  LOGGER.warning("Попытка открыть окно управления детьми без авторизации, currentUserId: " + currentUserId);
                  showAlert("Ошибка", "Пользователь не авторизован. Пожалуйста, войдите в систему.");
                  return;
              }
              if (client == null) {
                  LOGGER.warning("Соединение Client не инициализировано в manageChildAccounts, используя Connect.client");
                  client = Connect.client;
                  if (client == null) {
                      LOGGER.severe("Connect.client не инициализирован");
                      showAlert("Ошибка", "Соединение с сервером не установлено.");
                      return;
                  }
              }

              FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/child_account_management.fxml"));
              Parent root = loader.load();
              ChildAccountManagementController controller = loader.getController();
              LOGGER.info("Создан контроллер ChildAccountManagementController: " + controller.toString());
              controller.setClient(client);
              controller.setCurrentUserId(currentUserId);
              LOGGER.info("Передача currentUserId в ChildAccountManagementController: " + currentUserId);

              Stage stage = new Stage();
              stage.setTitle("Управление счетами детей");

              Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
              stage.setX(screenBounds.getMinX());
              stage.setY(screenBounds.getMinY());
              stage.setWidth(screenBounds.getWidth());
              stage.setHeight(screenBounds.getHeight());
              LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Screen bounds set: X=" + screenBounds.getMinX() +
                      ", Y=" + screenBounds.getMinY() + ", Width=" + screenBounds.getWidth() +
                      ", Height=" + screenBounds.getHeight());


              stage.setMaximized(true);

              stage.setScene(new Scene(root));
              stage.show();
          } catch (IOException e) {
              showAlert("Ошибка", "Не удалось открыть управление счетами детей: " + e.getMessage());
              LOGGER.severe("Ошибка открытия окна управления счетами детей: " + e.getMessage());
              e.printStackTrace();
          }

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static MenuUserController openMenuUserController(Stage primaryStage) throws IOException {
        String fxmlPath = "/client/menu.fxml";
        LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Пытаемся загрузить FXML из: " + fxmlPath);

        java.net.URL location = MenuUserController.class.getResource(fxmlPath);
        if (location == null) {
            LOGGER.severe("[" + LocalDate.now() + " " + LocalTime.now() + "] Файл FXML не найден по пути: " + fxmlPath);
            throw new IOException("Не удаётся найти FXML файл по пути: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        MenuUserController controller = loader.getController();
        Scene scene = new Scene(root);
        Stage stage = primaryStage != null ? primaryStage : new Stage();
        stage.setScene(scene);

        controller.setStage(stage);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Установлены границы экрана: X=" + screenBounds.getMinX() +
                ", Y=" + screenBounds.getMinY() + ", Width=" + screenBounds.getWidth() +
                ", Height=" + screenBounds.getHeight());

        stage.setMaximized(true);
        stage.setTitle("Меню пользователя");
        stage.show();
        LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] Окно меню пользователя открыто на весь экран");

        return controller;
    }
}