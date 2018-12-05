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
    private Button commit;
    @FXML
    private Button reset;

    private Controller controller;//todo how to do??????


    @FXML
    private void Commit(ActionEvent event) throws IOException {
        controller = new Controller();
        String from = pathFrom.getText();
        String to = pathTo.getText();
        boolean stem = stemming.isSelected();
        controller.commit(from,to,stem);

    }


}
