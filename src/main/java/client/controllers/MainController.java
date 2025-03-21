package client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class MainController {
    @FXML
    private Button myButton;

    @FXML
    private void onButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщение");
        alert.setHeaderText(null);
        alert.setContentText("Кнопка нажата!");
        alert.showAndWait();
    }
}
