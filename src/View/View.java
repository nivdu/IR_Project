package View;

import Controller.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

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

    private Controller controller = new Controller();//todo how to do??????


    @FXML
    private void Commit(ActionEvent event) throws IOException {
        String from = pathFrom.getText();
        String to = pathTo.getText();
        boolean stem = stemming.isSelected();
        boolean isSucceed = controller.commit(from,to,stem);

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
