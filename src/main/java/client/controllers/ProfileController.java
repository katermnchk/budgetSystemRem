package client.controllers;

import client.clientWork.Connect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import client.clientWork.Users;

import java.io.IOException;
import java.util.ArrayList;

public class ProfileController {
    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label loginLabel;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() throws ClassNotFoundException {
        Connect.client.sendMessage("userInf");
        Connect.client.sendObject(Connect.id);

        ArrayList<Users> usersList = (ArrayList<Users>) Connect.client.readObject();
        if (!usersList.isEmpty()) {
            Users user = usersList.get(0);
            firstNameLabel.setText(user.getFirstname());
            lastNameLabel.setText(user.getLastname());
            loginLabel.setText(user.getLogin());
        }
    }

    @FXML
    void backToMain(ActionEvent event) {
        backButton.getScene().getWindow().hide();

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
