package client.controllers.admin;

import client.clientWork.Connect;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AdminStatisticsController {

    @FXML private Text totalUsers;
    @FXML private Text totalAccounts;
    @FXML private Text totalBalance;
    @FXML private Text totalTransactions;

    @FXML private TableView<RoleStat> roleTable;
    @FXML private TableColumn<RoleStat, String> roleColumn;
    @FXML private TableColumn<RoleStat, Integer> countColumn;

    @FXML private TableView<CategoryStat> categoryTable;
    @FXML private TableColumn<CategoryStat, String> categoryNameColumn;
    @FXML private TableColumn<CategoryStat, String> categoryTypeColumn;
    @FXML private TableColumn<CategoryStat, Integer> transactionCountColumn;
    @FXML private TableColumn<CategoryStat, Double> totalAmountColumn;

    @FXML private Button backButton;

    private ObservableList<RoleStat> roleStats = FXCollections.observableArrayList();
    private ObservableList<CategoryStat> categoryStats = FXCollections.observableArrayList();

    public static class RoleStat {
        private final SimpleStringProperty role;
        private final SimpleIntegerProperty count;

        public RoleStat(String role, int count) {
            this.role = new SimpleStringProperty(role);
            this.count = new SimpleIntegerProperty(count);
        }

        public String getRole() { return role.get(); }
        public int getCount() { return count.get(); }
    }

    public static class CategoryStat {
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleIntegerProperty transactionCount;
        private final SimpleDoubleProperty totalAmount;

        public CategoryStat(String name, String type, int transactionCount, double totalAmount) {
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.transactionCount = new SimpleIntegerProperty(transactionCount);
            this.totalAmount = new SimpleDoubleProperty(totalAmount);
        }

        public String getName() { return name.get(); }
        public String getType() { return type.get(); }
        public int getTransactionCount() { return transactionCount.get(); }
        public double getTotalAmount() { return totalAmount.get(); }
    }

    @FXML
    public void initialize() {
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().role);
        countColumn.setCellValueFactory(cellData -> cellData.getValue().count.asObject());
        roleTable.setItems(roleStats);

        categoryNameColumn.setCellValueFactory(cellData -> cellData.getValue().name);
        categoryTypeColumn.setCellValueFactory(cellData -> cellData.getValue().type);
        transactionCountColumn.setCellValueFactory(cellData -> cellData.getValue().transactionCount.asObject());
        totalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmount.asObject());
        categoryTable.setItems(categoryStats);

        loadStatistics();
    }

    private void loadStatistics() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connect.client.sendMessage("getStatistics");
                    Object response = Connect.client.readObject();
                    if (response instanceof HashMap<?, ?>) {
                        HashMap<String, Object> stats = (HashMap<String, Object>) response;
                        javafx.application.Platform.runLater(() -> {
                            totalUsers.setText(String.valueOf(stats.get("totalUsers")));
                            totalAccounts.setText(String.valueOf(stats.get("totalAccounts")));
                            totalBalance.setText(String.format("%.2f", stats.get("totalBalance")));
                            totalTransactions.setText(String.valueOf(stats.get("totalTransactions")));

                            ArrayList<HashMap<String, Object>> roleData = (ArrayList<HashMap<String, Object>>) stats.get("roleStats");
                            roleStats.clear();
                            for (HashMap<String, Object> role : roleData) {
                                roleStats.add(new RoleStat(
                                        (String) role.get("role"),
                                        ((Number) role.get("count")).intValue()
                                ));
                            }

                            ArrayList<HashMap<String, Object>> categoryData = (ArrayList<HashMap<String, Object>>) stats.get("categoryStats");
                            categoryStats.clear();
                            for (HashMap<String, Object> category : categoryData) {
                                categoryStats.add(new CategoryStat(
                                        (String) category.get("name"),
                                        (String) category.get("type"),
                                        ((Number) category.get("transactionCount")).intValue(),
                                        ((Number) category.get("totalAmount")).doubleValue()
                                ));
                            }
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить статистику: " + response);
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
    void back(ActionEvent event) {
        backButton.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/menuAdmin.fxml"));
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