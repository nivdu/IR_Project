package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        String string = "-1,234.15";
//        boolean numeric = true;
//
//        numeric = string.matches("-?\\d+(\\.\\d+)?");
//
//        if(numeric)
//            System.out.println(string + " is a number");
//        else
//            System.out.println(string + " is not a number");

        ReadFile readFile = new ReadFile("C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל\\corpus");
        readFile.SpiltFileIntoSeparateDocs();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
