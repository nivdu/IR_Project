package Model;

import java.util.HashMap;

public class Query {
    private HashMap<String,Integer> docsLenghsNoStem;
    private HashMap<String,Integer> docsLenghsStem;
    public String[] getDocSplited() {
        return null;
    }

    public void setDocsLenghsNoStem(HashMap<String, Integer> docsLenghsNoStem) {
        this.docsLenghsNoStem = docsLenghsNoStem;
    }

    public void setDocsLenghsStem(HashMap<String, Integer> docsLenghsStem) {
        this.docsLenghsStem = docsLenghsStem;
    }
}
