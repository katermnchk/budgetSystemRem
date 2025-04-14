package client.controllers.user;

import client.clientWork.Category;
import client.clientWork.Connect;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class GoalsController implements Initializable {

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField targetAmountField;
    @FXML private Button addGoalButton;
    @FXML private Button cancelButton;
    @FXML private TableView<Goal> goalsTable;
    @FXML private TableColumn<Goal, String> categoryColumn;
    @FXML private TableColumn<Goal, String> typeColumn;
    @FXML private TableColumn<Goal, Double> targetAmountColumn;
    @FXML private TableColumn<Goal, Double> actualAmountColumn;
    @FXML private TableColumn<Goal, Boolean> statusColumn;
    @FXML private TextArea recommendationsArea;

    private ArrayList<Category> categories;

    public static class Goal {
        private final SimpleStringProperty categoryName;
        private final SimpleStringProperty type;
        private final SimpleDoubleProperty targetAmount;
        private final SimpleDoubleProperty actualAmount;
        private final SimpleBooleanProperty achieved;

        public Goal(String categoryName, String type, double targetAmount, double actualAmount, boolean achieved) {
            this.categoryName = new SimpleStringProperty(categoryName);
            this.type = new SimpleStringProperty(type);
            this.targetAmount = new SimpleDoubleProperty(targetAmount);
            this.actualAmount = new SimpleDoubleProperty(actualAmount);
            this.achieved = new SimpleBooleanProperty(achieved);
        }

        public String getCategoryName() { return categoryName.get(); }
        public String getType() { return type.get(); }
        public double getTargetAmount() { return targetAmount.get(); }
        public double getActualAmount() { return actualAmount.get(); }
        public boolean isAchieved() { return achieved.get(); }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryName);
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().type);
        targetAmountColumn.setCellValueFactory(cellData -> cellData.getValue().targetAmount.asObject());
        actualAmountColumn.setCellValueFactory(cellData -> cellData.getValue().actualAmount.asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().achieved);
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean achieved, boolean empty) {
                super.updateItem(achieved, empty);
                if (empty || achieved == null) {
                    setText(null);
                } else {
                    setText(achieved ? "Выполнено" : "Не выполнено");
                }
            }
        });

        typeComboBox.getSelectionModel().selectFirst();

        loadCategories();
        loadGoals();
        loadRecommendations();
    }

    private void loadCategories() {
        Connect.client.sendMessage("getAllCategories");
        Connect.client.sendObject(Connect.id);
        categories = (ArrayList<Category>) Connect.client.readObject();
        categoryComboBox.setItems(FXCollections.observableArrayList(
                categories.stream().map(Category::getName).toList()
        ));
        if (!categories.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    private void loadGoals() {
        Connect.client.sendMessage("getGoals");
        Connect.client.sendObject(Connect.id);
        ArrayList<HashMap<String, Object>> goalData = (ArrayList<HashMap<String, Object>>) Connect.client.readObject();
        goalsTable.setItems(FXCollections.observableArrayList(
                goalData.stream().map(goal -> new Goal(
                        (String) goal.get("categoryName"),
                        (String) goal.get("type"),
                        ((Number) goal.get("targetAmount")).doubleValue(),
                        ((Number) goal.get("actualAmount")).doubleValue(),
                        (Boolean) goal.get("achieved")
                )).toList()
        ));
    }

    private void loadRecommendations() {
        Connect.client.sendMessage("getRecommendations");
        Connect.client.sendObject(Connect.id);
        ArrayList<String> recommendations = (ArrayList<String>) Connect.client.readObject();
        recommendationsArea.setText(String.join("\n", recommendations));
    }

    @FXML
    private void addGoal() {
        String categoryName = categoryComboBox.getValue();
        String type = typeComboBox.getValue();
        String amountText = targetAmountField.getText().trim();

        if (categoryName == null || type == null || amountText.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля.");
            return;
        }

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(amountText);
            if (targetAmount <= 0) {
                showAlert("Ошибка", "Сумма цели должна быть больше 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите числовое значение.");
            return;
        }

        int categoryId = categories.stream()
                .filter(c -> c.getName().equals(categoryName))
                .findFirst()
                .map(Category::getId)
                .orElse(-1);
        if (categoryId == -1) {
            showAlert("Ошибка", "Категория не найдена.");
            return;
        }

        try {
            HashMap<String, Object> goalData = new HashMap<>();
            goalData.put("userId", Connect.id);
            goalData.put("categoryId", categoryId);
            goalData.put("type", type);
            goalData.put("targetAmount", targetAmount);
            goalData.put("period", "MONTHLY");

            Connect.client.sendMessage("addGoal");
            Connect.client.sendObject(goalData);

            String response = Connect.client.readMessage();
            if ("OK".equals(response)) {
                showAlert("Успех", "Цель добавлена: " + targetAmount + " BYN");
                targetAmountField.clear();
                loadGoals();
                loadRecommendations();
                // Не закрываем окно, чтобы пользователь мог добавить ещё цели
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось добавить цель: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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