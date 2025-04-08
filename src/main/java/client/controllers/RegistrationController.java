package client.controllers;

import java.io.IOException;

import client.clientWork.Connect;
import client.clientWork.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import client.util.ClientDialog;
import server.SystemOrg.Role;

public class RegistrationController {

    @FXML
    private Button backButton;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Button registrationButton;

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
    void registrationUser(ActionEvent event) throws IOException {
        if (checkInput())
            ClientDialog.showAlertWithNullInput();
        else if(!password.getText().equals(confirmPassword.getText())) {
            ClientDialog.showAlertWithPassword(); }
        else {
                Users user = new Users();
                user.setFirstname(firstName.getText());
                user.setLastname(lastName.getText());
                user.setLogin(login.getText());
                user.setPassword(password.getText());
                Connect.client.sendMessage("registrationUser");
                Connect.client.sendObject(user);
                System.out.println("Запись отправлена");

                String mes = "";
                try {
                    mes = Connect.client.readMessage();
                } catch (IOException ex) {
                    System.out.println("Error in reading");
                }
            if (mes.equals("This user is already existed")) {
                ClientDialog.showAlertWithExistLogin();
            }
            else {
                Role r = (Role) Connect.client.readObject();
                Connect.id = r.getId();
                Connect.role = r.getRole();
                registrationButton.getScene().getWindow().hide();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/client/menu.fxml"));

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

        }
    }

    private boolean checkInput() {
        try {
            return firstName.getText().isEmpty() || lastName.getText().isEmpty() ||
                    login.getText().isEmpty() || password.getText().isEmpty();
        } catch (Exception e) {
            System.out.println("Error in checkInput: " + e.getMessage());
            return true;
        }
    }
}