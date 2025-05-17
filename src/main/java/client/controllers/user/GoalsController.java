package client.controllers.user;

import client.clientWork.Category;
import client.clientWork.Connect;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class GoalsController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(GoalsController.class.getName());

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField targetAmountField;
    @FXML private TableView<HashMap<String, Object>> goalsTable;
    @FXML private TableColumn<HashMap<String, Object>, String> categoryColumn;
    @FXML private TableColumn<HashMap<String, Object>, String> typeColumn;
    @FXML private TableColumn<HashMap<String, Object>, String> targetAmountColumn;
    @FXML private TableColumn<HashMap<String, Object>, String> actualAmountColumn;
    @FXML private TableColumn<HashMap<String, Object>, String> statusColumn;
    @FXML private TableColumn<HashMap<String, Object>, Void> actionColumn;
    @FXML private TextArea recommendationsArea;

    private ObservableList<Category> categories;
    private ObservableList<HashMap<String, Object>> goals;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("[" + LocalDateTime.now() + "] Инициализация GoalsController");
        categories = FXCollections.observableArrayList();
        goals = FXCollections.observableArrayList();

        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("categoryName").toString()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("type").toString()));
        targetAmountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().get("targetAmount"))));
        actualAmountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().get("actualAmount"))));
        statusColumn.setCellValueFactory(cellData -> {
            Boolean achieved = (Boolean) cellData.getValue().get("achieved");
            return new SimpleStringProperty(achieved ? "Достигнута" : "Не достигнута");
        });

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Удалить");

            {
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5px;");
                deleteButton.setOnAction(event -> {
                    HashMap<String, Object> goal = getTableView().getItems().get(getIndex());
                    deleteGoal(goal);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        goalsTable.setItems(goals);

        loadCategories();
        loadGoals();
        loadRecommendations();
    }

    private void loadCategories() {
        try {
            Connect.client.sendMessage("getAllCategories");
            Connect.client.sendObject(Connect.id);
            ArrayList<Category> categoryList = (ArrayList<Category>) Connect.client.readObject();
            categories.setAll(categoryList);
            categoryComboBox.setItems(FXCollections.observableArrayList(categories.stream().map(Category::getName).toList()));
            if (!categories.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            }
            LOGGER.info("[" + LocalDateTime.now() + "] Загружено категорий: " + categoryList.size());
        } catch (Exception e) {
            LOGGER.severe("[" + LocalDateTime.now() + "] Ошибка загрузки категорий: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить категории: " + e.getMessage());
        }
    }

    private void loadGoals() {
        try {
            Connect.client.sendMessage("getGoals");
            Connect.client.sendObject(Connect.id);
            ArrayList<HashMap<String, Object>> goalList = (ArrayList<HashMap<String, Object>>) Connect.client.readObject();
            goals.setAll(goalList);
            LOGGER.info("[" + LocalDateTime.now() + "] Загружено целей: " + goalList.size());
        } catch (Exception e) {
            LOGGER.severe("[" + LocalDateTime.now() + "] Ошибка загрузки целей: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить цели: " + e.getMessage());
        }
    }

    private void loadRecommendations() {
        try {
            Connect.client.sendMessage("getRecommendations");
            Connect.client.sendObject(Connect.id);
            ArrayList<String> recommendations = (ArrayList<String>) Connect.client.readObject();
            recommendationsArea.setText(String.join("\n", recommendations));
            LOGGER.info("[" + LocalDateTime.now() + "] Загружено рекомендаций: " + recommendations.size());
        } catch (Exception e) {
            LOGGER.severe("[" + LocalDateTime.now() + "] Ошибка загрузки рекомендаций: " + e.getMessage());
            showAlert("Ошибка", "Не удалось загрузить рекомендации: " + e.getMessage());
        }
    }

    @FXML
    private void addGoal() {
        try {
            String categoryName = categoryComboBox.getValue();
            String type = typeComboBox.getValue();
            String targetAmountText = targetAmountField.getText();

            if (categoryName == null || type == null || targetAmountText.isEmpty()) {
                showAlert("Ошибка", "Заполните все поля.");
                return;
            }

            double targetAmount;
            try {
                targetAmount = Double.parseDouble(targetAmountText);
                if (targetAmount <= 0) {
                    showAlert("Ошибка", "Сумма цели должна быть больше 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Ошибка", "Введите корректную сумму.");
                return;
            }

            int categoryId = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Категория не найдена"))
                    .getId();

            HashMap<String, Object> goalData = new HashMap<>();
            goalData.put("userId", Connect.id);
            goalData.put("categoryId", categoryId);
            goalData.put("type", type);
            goalData.put("targetAmount", targetAmount);
            goalData.put("period", "MONTHLY");

            Connect.client.sendMessage("addGoal");
            Connect.client.sendObject(goalData);
            String response = (String) Connect.client.readObject();
            if ("OK".equals(response)) {
                loadGoals();
                loadRecommendations();
                clearFields();
                showAlert("Успех", "Цель успешно добавлена!");
                LOGGER.info("[" + LocalDateTime.now() + "] Цель добавлена: category=" + categoryName + ", type=" + type + ", target=" + targetAmount);
            } else {
                showAlert("Ошибка", response);
            }
        } catch (Exception e) {
            LOGGER.severe("[" + LocalDateTime.now() + "] Ошибка добавления цели: " + e.getMessage());
            showAlert("Ошибка", "Не удалось добавить цель: " + e.getMessage());
        }
    }

    private void deleteGoal(HashMap<String, Object> goal) {
        try {
            Integer goalId = (Integer) goal.get("id");
            Connect.client.sendMessage("deleteGoal");
            Connect.client.sendObject(goalId);
            String response = (String) Connect.client.readObject();
            if ("OK".equals(response)) {
                loadGoals();
                loadRecommendations();
                showAlert("Успех", "Цель успешно удалена!");
                LOGGER.info("[" + LocalDateTime.now() + "] Цель удалена: id=" + goalId);
            } else {
                showAlert("Ошибка", response);
            }
        } catch (Exception e) {
            LOGGER.severe("[" + LocalDateTime.now() + "] Ошибка удаления цели: " + e.getMessage());
            showAlert("Ошибка", "Не удалось удалить цель: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        clearFields();
        LOGGER.info("[" + LocalDateTime.now() + "] Очистка полей формы");
    }

    private void clearFields() {
        categoryComboBox.getSelectionModel().selectFirst();
        typeComboBox.getSelectionModel().selectFirst();
        targetAmountField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}