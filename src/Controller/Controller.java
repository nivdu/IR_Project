package Controller;

import Model.Model;
import Model.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Controller {
    private Model model = new Model();

    public boolean commit(String from, String to, boolean stem) {
        return model.generateInvertedIndex(from,to,stem);
    }

    public boolean Reset() {
        return model.reset();
    }

    public boolean Load(boolean stem,String pathTo,String pathFrom) throws InterruptedException {
        return model.loadDictionaryFromDiskToMemory(stem,pathTo,pathFrom);
    }

    public List<String> Display(boolean stem, String pathTo) {
        return model.displayDictionary(stem, pathTo);
    }

    public HashSet<String> languages(){return model.languages();}


    public HashMap<String,Double> RunQuery(String query, boolean toStem, String pathTo, String pathFrom, List<String> citiesChosen, boolean semantic, boolean toSaveResults,String pathForResults) {
        return model.runQuery(query, toStem,pathTo, pathFrom, citiesChosen, semantic,toSaveResults,pathForResults);
    }

    public HashSet<String> setCities(String pathTo, boolean toStem) {
        return model.setCities(pathTo,toStem);
    }

    public HashMap<String,Double> getEntities(String docID, String pathTo, boolean toStem){
        return model.getEntities(docID,pathTo,toStem);
    }

    public HashMap<Query, HashMap<String, Double>> RunQueryFile(String pathQueryFile, boolean toStem, String pathFrom, String pathTo, boolean semantic,List<String> citiesFromViewList,boolean toSaveResults,String pathForResults) {
        try {
            return model.runQueryFile(pathQueryFile,toStem,pathFrom,pathTo,semantic,citiesFromViewList,toSaveResults,pathForResults);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
