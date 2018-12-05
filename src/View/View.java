package View;

import Controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

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
    @FXML
    private Button commit;

    private Controller controller = new Controller();//todo how to do??????


    @FXML
    private void Commit(ActionEvent event) throws IOException {
        String from = pathFrom.getText();
//        commit.setDisable();
        if(from==null || from.equals("No Directory selected")||from.equals("")) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path from");
            chooseFile.setContentText("You must choose legal path of directory to load from before commit");
            chooseFile.show();
            return;
        }
        pathFrom.setDisable(true);
        BrowseFrom.setDisable(true);
        String to = pathTo.getText();
        if(to==null || to.equals("No Directory selected")|| to.equals("")) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path to");
            chooseFile.setContentText("You must choose legal path of directory to save to before commit");
            chooseFile.show();
            return;
        }
        pathTo.setDisable(true);
        BrowseTo.setDisable(true);
        boolean stem = stemming.isSelected();
        boolean isSucceed = controller.commit(from,to,stem);
        if(isSucceed) {
            reset.setDisable(false);
            load.setDisable(false);
            display.setDisable(false);
            pathFrom.setDisable(false);
            pathTo.setDisable(false);
            BrowseFrom.setDisable(false);
            BrowseTo.setDisable(false);
        }
        else {
            pathFrom.setDisable(false);
            pathTo.setDisable(false);
            BrowseFrom.setDisable(false);
            BrowseTo.setDisable(false);
        }
    }

    @FXML
    private void browseFrom(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            pathFrom.setText("No Directory selected");
        } else {
            pathFrom.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void browseTo(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            pathTo.setText("No Directory selected");
        } else {
            pathTo.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void Reset(ActionEvent event) throws IOException {
        boolean isSucceed = controller.Reset();
    }

    @FXML
    private void Load(ActionEvent event) throws IOException {
        boolean isSucceed = controller.Load(stemming.isSelected());
    }
}
