package client.controllers.user;

import client.clientWork.Category;
import client.clientWork.Connect;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CategoryManagerController implements Initializable {
    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, String> nameColumn;

    @FXML
    private TableColumn<Category, String> typeColumn;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> typeComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));

        typeComboBox.getSelectionModel().selectFirst();

        refreshCategories();
    }

    @FXML
    private void addCategory() {
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();

        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название категории.");
            return;
        }

        try {
            Connect.client.sendMessage("addCategory");
            Connect.client.sendObject(new Category(0, name, type));

            String response = Connect.client.readMessage();
            if ("OK".equals(response)) {
                showAlert("Успех", "Категория '" + name + "' добавлена.");
                nameField.clear();
                refreshCategories();
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось добавить категорию: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("Ошибка", "Выберите категорию для удаления.");
            return;
        }

        try {
            Connect.client.sendMessage("deleteCategory");
            Connect.client.sendObject(selectedCategory.getId());

            String response = Connect.client.readMessage();
            if ("OK".equals(response)) {
                showAlert("Успех", "Категория '" + selectedCategory.getName() + "' удалена.");
                refreshCategories();
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка связи", "Не удалось удалить категорию: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshCategories() {
        Connect.client.sendMessage("getAllCategories");
        Connect.client.sendObject(Connect.id);

        ArrayList<Category> categories = (ArrayList<Category>) Connect.client.readObject();
        categoryTable.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) categoryTable.getScene().getWindow();
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