package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class View {
    @FXML
    private TextField pathFrom;
    @FXML
    private TextField pathTo;
    @FXML
    private TextField queryText;
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
    private Button reset;
    @FXML
    private ListView<String> cities;
    @FXML
    private CheckBox semantic;
    @FXML
    private Button runQuery;
    @FXML
    private Button chooseFile;


    private Controller controller = new Controller();


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
            //chooseFile.setDisable(false);
            semantic.setDisable(false);
            //runQuery.setDisable(false);
            reset.setDisable(false);
            load.setDisable(false);
            display.setDisable(false);
            pathFrom.setDisable(false);
            pathTo.setDisable(false);
            BrowseFrom.setDisable(false);
            BrowseTo.setDisable(false);
            languages.setDisable(false);
            setLanguages();
            setCities();
        }
        else {
            pathFrom.setDisable(false);
            pathTo.setDisable(false);
            BrowseFrom.setDisable(false);
            BrowseTo.setDisable(false);
        }
    }

    private void showAlert(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);
        alert.showAndWait();
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
        if(isSucceed) {
            showAlert(Alert.AlertType.INFORMATION,"RESET","Reset succeed!");
            display.setDisable(true);
            load.setDisable(true);
            reset.setDisable(true);
            runQuery.setDisable(true);
            chooseFile.setDisable(true);
            languages.setDisable(true);
            semantic.setDisable(true);
        }
    }

    @FXML
    private void Load(ActionEvent event) throws IOException {
        boolean isSucceed = controller.Load(stemming.isSelected(),pathTo.getText(), pathFrom.getText());
        if(isSucceed) {
            showAlert(Alert.AlertType.INFORMATION,"Load","Load succeed!");
            runQuery.setDisable(false);//todo here the set disable queryfile to
            chooseFile.setDisable(false);
        }
//        else{
//            showAlert(Alert.AlertType.ERROR, "Load","Load Failed!");
//        }
    }

    @FXML
    private void Display(ActionEvent event) throws IOException {
        List<String> dictionary= controller.Display(stemming.isSelected(),pathTo.getText());
        if(dictionary == null){
//            showAlert(Alert.AlertType.ERROR, "Display","Display Failed!");
            return;
        }
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

    private void setLanguages(){
        HashSet<String> list = controller.languages();
        ObservableList<String> obList = FXCollections.observableArrayList();
        for (String lg: list) {
            obList.add(lg);
        }
        obList.sort(String::compareToIgnoreCase);
        languages.setItems(obList);
    }

    private void setCities(){
        HashSet<String> listOfCities = controller.setCities(pathTo.getText(),stemming.isSelected());
        ObservableList<String> citiesObservable;
        citiesObservable = FXCollections.observableArrayList();
        for (String city: listOfCities) {
            citiesObservable.add(city);
        }
        citiesObservable.sort(String::compareToIgnoreCase);
        cities.setItems(citiesObservable);
        cities.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }


    @FXML
    private void RunQuery(ActionEvent event) throws IOException {
        ObservableList<String> citiesFromView = cities.getSelectionModel().getSelectedItems();
        List<String> citiesFromViewList = new ArrayList<>();
        if(citiesFromView!= null) {
            for (String city : citiesFromView) {
                citiesFromViewList.add(city);
            }
        }
        HashMap<String,Double> docsAndRank = controller.RunQuery(queryText.getText(),stemming.isSelected(), pathTo.getText(), pathFrom.getText(), citiesFromViewList, semantic.isSelected());
        ObservableList<String> docsObservable = FXCollections.observableArrayList();
        Set<String> keys = docsAndRank.keySet();
        for (String doc : keys) {
            docsObservable.add(doc);
        }
        docsObservable.sort(String::compareToIgnoreCase);
        TableView tableView = new TableView();

        TableColumn firstNameCol = new TableColumn("Query ID");
        TableColumn secondNameCol = new TableColumn("Doc ID");
        TableColumn thirdNameCol = new TableColumn("Entities");

        tableView.getColumns().addAll(firstNameCol,secondNameCol,thirdNameCol);


        tableView.setItems(docsObservable);

        Stage stage = new Stage();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(tableView);
        AnchorPane.setRightAnchor(tableView,0.0);
        AnchorPane.setLeftAnchor(tableView,0.0);
        anchorPane.setPrefWidth(500.0);
        anchorPane.setPrefHeight(400.0);
        Scene scene = new Scene(anchorPane,500,400);
        stage.setScene(scene);
        stage.show();
    }

    public void getEntities(ActionEvent event){
//        HashMap<String,Double> entities = controller.getEntities(docID,pathTo,stemming.isSelected());
        //todo ******take the first five entities only!!****
        //todo the function - show the five entities in another fxml


    }
}
