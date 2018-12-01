package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class document {
    private String documentID;
    private int maxTf;
    private int numOfUniqueWords;
    private String city;
    private HashMap<String,Integer> dicDoc;
    private HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc;

    public document(String documentID, int maxTf, int numOfUniqueWords, String city, HashMap<String,Integer> tempDic, HashMap locationOfCitiesAtCurrDoc) {
        this.locationOfCitiesAtCurrDoc = new HashMap<>(locationOfCitiesAtCurrDoc);
        this.maxTf = maxTf;
        this.numOfUniqueWords = numOfUniqueWords;
        this.city = city;
        this.dicDoc = tempDic;
        this.documentID=documentID;
    }

    public HashMap<String,ArrayList<Integer>> getLocationOfCities() {
        return locationOfCitiesAtCurrDoc;
    }

    public void removeDic(){ dicDoc.clear(); }
    public void removeLocationOfCities(){ locationOfCitiesAtCurrDoc.clear();}

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