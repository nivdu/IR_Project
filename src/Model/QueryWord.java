package Model;

import java.util.HashMap;

public class QueryWord {
    private String tfOverAll;
    private String wordText;
    private HashMap<String,int[]> docsOfWord;
    private int numOfWordInQuery;
    private int df;

    public QueryWord(String wordText, HashMap<String, int[]> docsOfWord, int numOfWordInQuery, int df, String tfOverAll) {
        this.wordText = wordText;
        this.docsOfWord = docsOfWord;
        this.numOfWordInQuery=numOfWordInQuery;
        this.df=df;
        this.tfOverAll = tfOverAll;
    }

    public String getTfOverAll() {
        return tfOverAll;
    }

    public HashMap<String, int[]> getDocsOfWord() {
        return docsOfWord;
    }

    public int getNumOfWordInQuery() {
        return numOfWordInQuery;
    }

    public int getDf() {
        return df;
    }
}
