package Controller;

import Model.Model;
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


    public void RunQuery(String query, boolean toStem, String pathTo, String pathFrom, List<String> citiesChosen, boolean semantic) {
        model.runQuery(query, toStem,pathTo, pathFrom, citiesChosen, semantic);
    }

    public HashSet<String> setCities() {
        return model.setCities();
    }

    public HashMap<String,Double> getEntities(String docID, String pathTo, boolean toStem){
        return model.getEntities(docID,pathTo,toStem);
    }
}
