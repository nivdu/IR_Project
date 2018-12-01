package Model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        long Stime = System.currentTimeMillis();
        ReadFile readFile = new ReadFile("C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל\\corpus");
        Parse parse = new Parse(true, "C:\\Users\\nivdu\\Desktop\\StopWords");//todo change to boolean stemmer and stop word path from the user
        Indexer indexer = new Indexer(readFile, parse);
        indexer.createPostingAndDic("C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל\\corpus");//todo take from the user the corpus path.
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        long Ftime = System.currentTimeMillis();
        System.out.println(Ftime-Stime);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
