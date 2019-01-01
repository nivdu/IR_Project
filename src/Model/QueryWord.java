package Model;

import java.util.HashMap;

public class QueryWord {
    private String wordText;
    private HashMap<String,int[]> docsOfWord;
    private int numOfWordInQuery;
    private int df;

    public QueryWord(String wordText, HashMap<String, int[]> docsOfWord, int numOfWordInQuery, int df) {
        this.wordText = wordText;
        this.docsOfWord = docsOfWord;
        this.numOfWordInQuery=numOfWordInQuery;
        this.df=df;
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
