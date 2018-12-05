package Controller;

import Model.Model;
import javafx.scene.control.Alert;

import java.util.List;

public class Controller {
    private Model model = new Model();

    public Alert commit(String from, String to, boolean stem) {
        return model.generateInvertedIndex(from,to,stem);
    }

    public boolean Reset() {
        return model.reset();
    }

    public boolean Load(boolean stem) {
        return model.loadDictionaryFromDiskToMemory(stem);
    }

    public List<String> Display(boolean stem) {
        return model.displayDictionary(stem);
    }
}
