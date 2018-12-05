package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class View {
    @FXML
    private TextField pathFrom;
    @FXML
    private TextField pathTo;
    @FXML
    private Button BrowseFrom;
    @FXML
    private Button BrowseTo;
    @FXML
    private CheckBox stemming;
    @FXML
    private ComboBox languages;
    @FXML
    private Button display;
    @FXML
    private Button load;
    @FXML
    private Button invertIndex;
    @FXML
    private Button reset;

    private Controller controller = new Controller();//todo how to do??????


    @FXML
    private void Commit(ActionEvent event) throws IOException {
        String from = pathFrom.getText();
        String to = pathTo.getText();
        boolean stem = stemming.isSelected();
        Alert isSucceed = controller.commit(from, to, stem);
        isSucceed.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);
        alert.showAndWait();
    }

    @FXML
    private void Reset(ActionEvent event) throws IOException {
        boolean isSucceed = controller.Reset();
        if(isSucceed) {
            showAlert(Alert.AlertType.INFORMATION,"RESET","Reset succeed!");
            display.setDisable(true);
            load.setDisable(true);
            reset.setDisable(true);
        }
        else{
            showAlert(Alert.AlertType.ERROR, "RESET","Reset Failed!");
        }
    }

    @FXML
    private void Load(ActionEvent event) throws IOException {
        boolean isSucceed = controller.Load(stemming.isSelected());
        if(isSucceed) {
            showAlert(Alert.AlertType.INFORMATION,"Load","Load succeed!");
        }
        else{
            showAlert(Alert.AlertType.ERROR, "Load","Load Failed!");
        }
    }

    @FXML
    private void Display(ActionEvent event) throws IOException {
        List<String> dictionary= controller.Display(stemming.isSelected());
        if(dictionary == null)
            showAlert(Alert.AlertType.ERROR, "Display","Display Failed!");
        ListView<String> listView = new ListView<String>();
        listView.getItems().setAll(FXCollections.observableList(dictionary));
        Stage stage = new Stage();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(listView);
        AnchorPane.setRightAnchor(listView,0.0);
        AnchorPane.setLeftAnchor(listView,0.0);
        anchorPane.setPrefWidth(500.0);
        anchorPane.setPrefHeight(400.0);
        Scene scene = new Scene(anchorPane,500,400);
        stage.setScene(scene);
        stage.show();
    }
}
