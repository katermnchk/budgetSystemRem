package client.controllers.user;

import client.clientWork.Connect;
import client.controllers.MainController;
import com.sun.javafx.binding.DoubleConstant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ExpenseChartController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ExpenseChartController.class.getName());

    @FXML
    private PieChart expenseChart;

    @FXML
    private ComboBox<String> periodComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshChart();
    }

    @FXML
    private void refreshChart() {
        Connect.client.sendMessage("getExpenseChartData");
        Connect.client.sendObject(Connect.id);

        Object response = Connect.client.readObject();
        if (!(response instanceof HashMap)) {
            showAlert("Ошибка данных", "Сервер вернул некорректные данные.");
            return;
        }

        HashMap<String, Double> expenseData = (HashMap<String, Double>) response;
        if (expenseData.isEmpty()) {
            showAlert("Нет данных", "Нет расходов для отображения.");
            expenseChart.setData(FXCollections.observableArrayList());
            return;
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
            String label = entry.getKey() + " (" + df.format(entry.getValue()) + " BYN)";
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        //данные в график
        expenseChart.setData(pieChartData);
        expenseChart.setTitle("Расходы по категориям");
        expenseChart.setLabelsVisible(true); //подписи на графике
        expenseChart.setLegendVisible(true); //легенда
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) expenseChart.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void filterChart() {
        String period = periodComboBox.getValue();
        Connect.client.sendMessage("filterExpenseData");
        Connect.client.sendObject(new Object[]{Connect.id, period});
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        Object response = Connect.client.readObject();
        if (response instanceof ObservableList) {
            pieChartData.addAll((ObservableList<PieChart.Data>) response);
            expenseChart.setData(pieChartData);
            LOGGER.info("[" + LocalDate.now() + " " + LocalTime.now() + "] График отфильтрован по периоду: " + period);
        } else {
            showAlert("Ошибка", "Не удалось отфильтровать данные: " + response);
            LOGGER.warning("[" + LocalDate.now() + " " + LocalTime.now() + "] Ошибка фильтрации данных: " + response);
        }
    }
}