package client.util;

import javafx.scene.control.Alert;

public class ClientDialog {
    static public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    static public void showAlertWithNullInput(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Ввод данных");
        alert.setContentText("Заполните пустые поля");
        alert.showAndWait();
    }

    static public void showAlertWithExistLogin(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Регистрация");
        alert.setContentText("Такой пользователь уже существует");
        alert.showAndWait();
    }

    static public void showAlertWithNoLogin(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Введите правильно логин или пароль");
        alert.setContentText("Такой пользователь не найден в системе");
        alert.showAndWait();
    }

    static public void showAlertWithData(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Сбой задачи");
        alert.setContentText("Проверьте введнные параметры");
        alert.showAndWait();
    }

    static public void correctOperation(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Correct");
        alert.setHeaderText("");
        alert.setContentText("Операция прошла успешно");
        alert.showAndWait();
    }

    static public void showAlertWithDouble(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка 500: Ввод двоичных чисел");
        alert.setContentText("Заполните правильно цену/вес");
        alert.showAndWait();
    }

    public static void showAlertWithPassword() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Пароли не совпадают");
        alert.setContentText("Повторите ввод пароля");
        alert.showAndWait();
    }

    public static void showAlertWithSmallPassword() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: Пароль должен состоять минимум из 6 символов");
        alert.setContentText("Повторите ввод пароля");
        alert.showAndWait();
    }

    public static void correctMonitoring() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка: КОРРЕКТН МОНІТОРІНГ");
        alert.setContentText("Повторите ввод пароля");
        alert.showAndWait();
    }
}

