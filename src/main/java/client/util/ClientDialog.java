package client.util;

import javafx.scene.control.Alert;

public class ClientDialog {
    static public void showAlertWithNullInput(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Ввод данных");
        alert.setContentText("Заполните пустые поля");
        alert.showAndWait();
    }

    static public void showAlertWithNoLogin(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Введите правильно логин или пароль");
        alert.setContentText("Такой пользователь не найден в системе");
        alert.showAndWait();
    }
}
