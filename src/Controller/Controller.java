package Controller;

import Model.Model;
import javafx.scene.control.Alert;

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

    public boolean Load(boolean stem,String pathTo) {
        return model.loadDictionaryFromDiskToMemory(stem,pathTo);
    }

    public List<String> Display(boolean stem, String pathTo) {
        return model.displayDictionary(stem, pathTo);
    }

    public HashSet<String> languages(){return model.languages();}

    public void RunQuery(String query, boolean toStem, String pathTo) {
        model.runQuery(query, toStem,pathTo);
    }
}
