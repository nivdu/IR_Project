package View;

import Controller.Controller;
import Model.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    private CheckBox saveResults;
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
    private Button RunQueryFile;
    @FXML
    private TextField QueryFileText;
    @FXML
    private Button BrowseQueryFile;
    @FXML
    private TextField pathResults;
    @FXML
    private Button BrowseQueryPath;

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
            saveResults.setDisable(false);
            setLanguages();
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
    private void browseResults(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            pathResults.setText("No Directory selected");
        } else {
            pathResults.setText(selectedDirectory.getAbsolutePath());
        }
    }



    @FXML
    private void BrowseQueryFile(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            QueryFileText.setText("No Directory selected");
        } else {
            QueryFileText.setText(selectedFile.getAbsolutePath());
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
            BrowseQueryFile.setDisable(true);
            languages.setDisable(true);
            semantic.setDisable(true);
        }
    }

    @FXML
    private void Load(ActionEvent event) throws IOException, InterruptedException {
        boolean isSucceed = controller.Load(stemming.isSelected(),pathTo.getText(), pathFrom.getText());
        if(isSucceed) {
            setCities();
            showAlert(Alert.AlertType.INFORMATION,"Load","Load succeed!");
            runQuery.setDisable(false);//todo here the set disable queryfile to
            RunQueryFile.setDisable(false);
            BrowseQueryFile.setDisable(false);
            BrowseQueryPath.setDisable(false);
        }
    }

    @FXML
    private void Display(ActionEvent event) throws IOException {
        List<String> dictionary= controller.Display(stemming.isSelected(),pathTo.getText());
        if(dictionary == null){
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


    private void tableViewQueries(ObservableList<MyDataType> docsAndQueries){
        TableView<MyDataType> tableView = new TableView<MyDataType>();
        TableColumn queryCol = new TableColumn("Query ID");
        queryCol.setCellValueFactory(new PropertyValueFactory<MyDataType,String>("queryID"));
        TableColumn docCol = new TableColumn("Doc ID");
        docCol.setCellValueFactory(new PropertyValueFactory<MyDataType,String>("docID"));

        //*****************************************************************8
        TableColumn entitiesCol = new TableColumn("Entities");
        entitiesCol.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<MyDataType, String>, TableCell<MyDataType, String>> cellFactory
                = //
                new Callback<TableColumn<MyDataType, String>, TableCell<MyDataType, String>>() {
                    @Override
                    public TableCell call(final TableColumn<MyDataType, String> param) {
                        final TableCell<MyDataType, String> cell = new TableCell<MyDataType, String>() {

                            final Button btn = new Button("Show entities");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        MyDataType myDataType = getTableView().getItems().get(getIndex());
                                        String docToShowEntities = myDataType.getDocID();
                                        HashMap<String,Double> entities = controller.getEntities(docToShowEntities,pathTo.getText(),stemming.isSelected());
                                        List<String> fiveEntities = new ArrayList<>();

                                        int countEntities = 0;

                                        for (Map.Entry<String, Double> en : entities.entrySet()) {
                                            if(countEntities<5){
                                                fiveEntities.add(en.getKey());
                                                countEntities++;
                                            }
                                            else break;
                                        }


                                        ListView<String> listView = new ListView<String>();
                                        listView.getItems().setAll(FXCollections.observableList(fiveEntities));
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

                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
        entitiesCol.setCellFactory(cellFactory);
        tableView.setItems(docsAndQueries);
        tableView.getColumns().addAll(queryCol,docCol,entitiesCol);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(7,0,0,7));
        vbox.getChildren().addAll(tableView);


        Stage stage = new Stage();
//        AnchorPane anchorPane = new AnchorPane();
//        anchorPane.getChildren().addAll(tableView);
//        AnchorPane.setRightAnchor(tableView,0.0);
//        AnchorPane.setLeftAnchor(tableView,0.0);
//        anchorPane.setPrefWidth(500.0);
//        anchorPane.setPrefHeight(400.0);
//        Scene scene = new Scene(anchorPane,500,400);
        Scene scene = new Scene(vbox,500,400);
        stage.setScene(scene);
        stage.show();
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
        HashMap<String,Double> docsAndRank = controller.RunQuery(queryText.getText(),stemming.isSelected(), pathTo.getText(), pathFrom.getText(), citiesFromViewList, semantic.isSelected(),saveResults.isSelected(),pathResults.getText());
        final ObservableList<MyDataType> docsList = FXCollections.observableArrayList();
        Set<String> keys = docsAndRank.keySet();
        for (String doc : keys) {
            docsList.add(new MyDataType("111",doc));//todo random query ID
        }

        tableViewQueries(docsList);
    }

    @FXML
    private void RunQueryFile(ActionEvent event) throws IOException {
        ObservableList<String> citiesFromView = cities.getSelectionModel().getSelectedItems();
        List<String> citiesFromViewList = new ArrayList<>();
        if(citiesFromView!= null) {
            for (String city : citiesFromView) {
                citiesFromViewList.add(city);
            }
        }
        HashMap<Query, HashMap<String, Double>> queryAndDocID=controller.RunQueryFile(QueryFileText.getText(),stemming.isSelected(),pathFrom.getText(),pathTo.getText(),semantic.isSelected(),citiesFromViewList,saveResults.isSelected(),pathResults.getText());

        List<Map.Entry<Query, HashMap<String, Double>>> list = new LinkedList<>(queryAndDocID.entrySet());
        final ObservableList<MyDataType> docsAndQueries = FXCollections.observableArrayList();
        for (Map.Entry<Query, HashMap<String, Double>> aa : list) {
            HashMap<String,Double> docsAndRateOfCurrQuery=queryAndDocID.get(aa.getKey());
            Set<String> docs = docsAndRateOfCurrQuery.keySet();
            for (String doc:docs) {
                docsAndQueries.add(new MyDataType(aa.getKey().getQueryID(),doc));
            }

        }

        Set<Query> keys = queryAndDocID.keySet();
        for (Query query : keys) {
            HashMap<String,Double> docsAndRateOfCurrQuery=queryAndDocID.get(query);
            Set<String> docs = docsAndRateOfCurrQuery.keySet();
            for (String doc:docs) {
                docsAndQueries.add(new MyDataType(query.getQueryID(),doc));
            }
        }
        Comparator<MyDataType> compareData = new Comparator<MyDataType>() {
            @Override
            public int compare(MyDataType s1, MyDataType s2) {
                String n1 = s1.getQueryID();
                String n2 = s2.getQueryID();
                return n1.compareToIgnoreCase(n2);
            }
        };
        FXCollections.sort(docsAndQueries,compareData);
        //docsAndQueries.sort((str, str2) -> MyDataType.compareToIgnoreCase(str, str2));

        tableViewQueries(docsAndQueries);
    }



    public class MyDataType {
        private final String queryID ;
        private final String docID ;
        public MyDataType(String queryID, String docID) {
            this.queryID = queryID;
            this.docID = docID;
        }
        public String getQueryID() {
            return queryID ;
        }
        public String getDocID() {
            return docID ;
        }
    }

//    public void getEntities(ActionEvent event){
//        HashMap<String,Double> entities = controller.getEntities(docID,pathTo,stemming.isSelected());
//        //todo ******take the first five entities only!!****
//        //todo the function - show the five entities in another fxml
//
//
//    }
}
