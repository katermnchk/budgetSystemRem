package client.controllers;

import client.clientWork.Connect;
import client.util.ClientDialog;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Authorization;
import server.SystemOrg.Role;

import java.io.IOException;
import java.util.Objects;


public class MainController {
    @FXML
    private TextField  login;

    @FXML
    private TextField  password;

    @FXML
    private Button  enterButton;

    @FXML
    private Button  registrationButton;

    @FXML
    void authorization(ActionEvent event) throws IOException {
        if (checkInput()) {
            ClientDialog.showAlertWithNullInput();
        }
        Connect.client.sendMessage("authorization");
        Authorization auth = new Authorization();
        auth.setLogin(login.getText());
        auth.setPassword(password.getText());
        Connect.client.sendObject(auth);

        String mes = "";
        try {
            mes = Connect.client.readMessage();
        } catch (IOException ex) {
            System.out.println("Error in reading");
        }
        if (mes.equals("There is no data!"))
            ClientDialog.showAlertWithNoLogin();
        else {
            Role r = (Role) Connect.client.readObject();
            Connect.id = r.getId();
            Connect.role = r.getRole();
            enterButton.getScene().getWindow().hide();
            System.out.println(Connect.role);
            FXMLLoader loader = new FXMLLoader();

            if (Objects.equals(Connect.role, "USER")) {
                System.out.println("Окно пользователя");
                loader.setLocation(getClass().getResource("/client/menu.fxml"));
            }
            else if(Objects.equals(Connect.role, "ADMIN"))
                loader.setLocation(getClass().getResource("/client/menuAdmin.fxml"));
            else
                loader.setLocation(getClass().getResource("/client/menuAdmin.fxml"));
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

    @FXML
    void registration(ActionEvent event) {
        /*registrationButton.getScene().getWindow().hide();
        openNewWindow("/client/registration.fxml");*/
    }

    @FXML
    void initialize() {
        registrationButton.setOnAction(event -> {
            registrationButton.getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/client/registration.fxml"));

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene((root)));
            stage.show();
        });
    }

    private void openNewWindow(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean checkInput() {
        try {
            return login.getText().equals("") || password.getText().equals("");
        }
        catch (Exception e) {
            System.out.println("Error");
            return true;
        }
    }
}
