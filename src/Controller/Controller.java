package Controller;

import Model.Model;

public class Controller {
    private Model model;

    public void commit(String from, String to, boolean stem) {
        model=new Model();
        model.generateInvertedIndex(from,to,stem);
    }
}
