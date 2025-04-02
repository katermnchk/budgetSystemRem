package client.main;

import client.clientWork.Client;
import client.clientWork.Connect;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/client/main.fxml"));
        if (fxmlLoader.getLocation() == null) {
            throw new IllegalStateException("FXML файл не найден!");
        }

        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        stage.setTitle("Budget System Rem");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Connect.client = new Client("127.0.0.1", "9006");
        launch();
    }
}
