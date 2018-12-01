package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class document {
    private String documentID;
    private int maxTf;
    private int numOfUniqueWords;
    private String city;
    private HashMap<String,Integer> dicDoc;
    ArrayList<Integer> locationOfCity;

    public document(String documentID, int maxTf, int numOfUniqueWords, String city, HashMap<String,Integer> tempDic, ArrayList<Integer> locationOfCity) {
        this.locationOfCity = new ArrayList<>(locationOfCity);
        this.maxTf = maxTf;
        this.numOfUniqueWords = numOfUniqueWords;
        this.city = city;
        this.dicDoc = tempDic;
        this.documentID=documentID;
    }

    public ArrayList<Integer> getLocationOfWords() {
        return locationOfCity;
    }

    public void removeDic(){
        dicDoc.clear();
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