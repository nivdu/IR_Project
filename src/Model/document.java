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
    private String[] docTitle;
    private int docLength;
    private int[] docSplitDate;

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }

    public void setDocSplitDate(int[] docSplitDate) {
        this.docSplitDate = docSplitDate;
    }

    public int[] getDocSplitDate() {

        return docSplitDate;
    }

    public int getDocLength() {

        return docLength;
    }

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
    

    public document(){
        this.documentID="";
        this.maxTf=0;
        this.numOfUniqueWords=0;
        this.city="";
        this.publishDate="";
        this.dicDoc=null;
        this.locationOfCitiesAtCurrDoc=null;
        this.docSplited=null;
        this.docTitle=null;
        this.docSplitDate=null;
    }
//
//    public document(String documentID, int maxTf, int numOfUniqueWords, String city, HashMap<String,int[]> tempDic, HashMap locationOfCitiesAtCurrDoc) {
////        this.locationOfCitiesAtCurrDoc = new HashMap<>(locationOfCitiesAtCurrDoc);
//        this.locationOfCitiesAtCurrDoc = locationOfCitiesAtCurrDoc;
//        this.maxTf = maxTf;
//        this.numOfUniqueWords = numOfUniqueWords;
//        this.city = city;
//        this.dicDoc = tempDic;
//        this.documentID=documentID;
//    }

    public HashMap<String,ArrayList<Integer>> getLocationOfCities() {
        return locationOfCitiesAtCurrDoc;
    }

    public void removeDic(){ dicDoc.clear(); }

    public void removeDocSplitedArr(){ docSplited=null; }

    public void removeLocationOfCities(){ locationOfCitiesAtCurrDoc.clear();}

    public void setDocTitle(String[] docTitle) {
        this.docTitle = docTitle;
    }

    public String[] getDocSplited() {
        return docSplited;
    }

    public void setDocSplited(String[] docSplited) {
        this.docSplited = docSplited;
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

    public HashMap<String, int[]> getDicDoc() {
        return dicDoc;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setPublishDate(String date) { this.publishDate = date; }

    public String[] getDocTitle() {
        return docTitle;
    }

    public void setDicDoc(HashMap<String, int[]> dicDoc) {
        this.dicDoc = dicDoc;
    }
}