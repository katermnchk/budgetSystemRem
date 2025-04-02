package client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class WindowChanger {
    public static void changeWindow(Class className, Button button, String fname, String title, boolean ismodal) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Thread.currentThread().getContextClassLoader().getResource("client/" + fname));
        // fxmlLoader.setLocation(className.getClassLoader().getResource("client/" + fname));
       // fxmlLoader.setLocation(className.getResource("src/main/resources/client/" + fname));
        fxmlLoader.load();
        Parent root = fxmlLoader.getRoot();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        if (ismodal) {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        else {
            button.getScene().getWindow().hide();
        }
        stage.show();
    }
}
