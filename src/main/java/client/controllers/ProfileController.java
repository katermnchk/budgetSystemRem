package client.controllers;

import client.clientWork.Connect;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import client.clientWork.Users;

import java.util.ArrayList;

public class ProfileController {
    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label loginLabel;

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
    void closeWindow() {
        Stage stage = (Stage) firstNameLabel.getScene().getWindow();
        stage.close();
    }
}
