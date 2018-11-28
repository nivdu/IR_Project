package sample;

import java.util.HashMap;

public class document {
    private int maxTf;
    private int numOfUniqueWords;
    private String city;
    private HashMap<String,Integer> docDic;

    public document(int maxTf, int numOfUniqueWords, String city, HashMap<String,Integer> tempDic) {
        this.maxTf = 0;
        this.numOfUniqueWords = numOfUniqueWords;
        this.city = city;
        this.docDic = tempDic;
    }

}
