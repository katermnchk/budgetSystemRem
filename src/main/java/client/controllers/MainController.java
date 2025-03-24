package client.controllers;

import client.clientWork.Connect;
import client.util.ClientDialog;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;


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
        if (checkInput())
            ClientDialog.showAlertWithNullInput();
        else {
            Connect.client.sendMessage("authorization");
            Authorization auth = new Authorization();
            auth.setLogin(login.getText());
            auth.setPassword(password.getText());
            Connect.client.sendObject(auth);

            String mes = " ";
            try {
                mes = Connect.client.readMessage();
            } catch (IOException e) {
                System.out.println("Error with reading");
            }
            if (mes.equals("There is no data!"))
                ClientDialog.showAlertWithNoLogin();
            else {
                enterButton.getScene().getWindow().hide();
                FXMLLoader loader = new FXMLLoader();
            }
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
