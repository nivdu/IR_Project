package sample;

import java.util.HashMap;

public class document {
    private String documentID;
    private int maxTf;
    private int numOfUniqueWords;
    private String city;
    private HashMap<String,Integer> dicDoc;

    public document(String documentID, int maxTf, int numOfUniqueWords, String city, HashMap<String,Integer> tempDic) {
        this.maxTf = 0;
        this.numOfUniqueWords = numOfUniqueWords;
        this.city = city;
        this.dicDoc = tempDic;
        this.documentID=documentID;
    }

    public int getMaxTf() {
        return maxTf;
    }

    public int getNumOfUniqueWords() {
        return numOfUniqueWords;
    }

    public String getCity() {
        return city;
    }

    public HashMap<String, Integer> getDicDoc() {
        return dicDoc;
    }

    public String getDocumentID() {
        return documentID;
    }
}