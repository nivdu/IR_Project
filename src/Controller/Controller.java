package Controller;

import Model.Model;

public class Controller {
    private Model model = new Model();

    public boolean commit(String from, String to, boolean stem) {
        return model.generateInvertedIndex(from,to,stem);
    }

    public boolean Reset() {
        return model.reset();
    }

    public boolean Load(boolean stem) {
        return model.loadDictionaryFromDiskToMemory(stem);
    }
}
