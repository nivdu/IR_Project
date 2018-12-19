package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class document {
    private String documentID;
    private int maxTf;
    private int numOfUniqueWords;
    private String city;
    private String publishDate;
    private HashMap<String,int[]> dicDoc;
    private HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc;
    private String[] docSplited;
    private String docTitle;

    public String getPublishDate() {
        return publishDate;
    }

    public HashMap<String, ArrayList<Integer>> getLocationOfCitiesAtCurrDoc() {
        return locationOfCitiesAtCurrDoc;
    }

    public void setDocumentID(String documentID) {

        this.documentID = documentID;
    }

    public void setMaxTf(int maxTf) {
        this.maxTf = maxTf;
    }

    public void setNumOfUniqueWords(int numOfUniqueWords) {
        this.numOfUniqueWords = numOfUniqueWords;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLocationOfCitiesAtCurrDoc(HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc) {
        this.locationOfCitiesAtCurrDoc = locationOfCitiesAtCurrDoc;
    }

    public document(String documentID, int maxTf, int numOfUniqueWords, String city, HashMap<String,int[]> tempDic, HashMap locationOfCitiesAtCurrDoc) {
//        this.locationOfCitiesAtCurrDoc = new HashMap<>(locationOfCitiesAtCurrDoc);
        this.locationOfCitiesAtCurrDoc = locationOfCitiesAtCurrDoc;
        this.maxTf = maxTf;
        this.numOfUniqueWords = numOfUniqueWords;
        this.city = city;
        this.dicDoc = tempDic;//todo new hashmap
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

    public HashMap<String, int[]> getDicDoc() {
        return dicDoc;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setPublishDate(String date) { this.publishDate = date; }

    public void setDocSplited(String[] docSplited) { this.docSplited = docSplited; }

    public String[] getDocSplited() { return docSplited; }

    public void setDicDoc(HashMap<String, int[]> dicDoc) {
        this.dicDoc = dicDoc;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDocTitle() {
        return docTitle;
    }
}