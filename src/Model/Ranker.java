package Model;

import java.util.*;

public class Ranker {

    private HashMap<String,Integer> docsLenghsNoStem;
    private HashMap<String,Integer> docsLenghsStem;
    private boolean toStem;
    private int numOfDocs;
    private double avrLengh;
    private double k;
    private double b;
    /**
     * contractor
     * @param toStem - true if need to stem else false.
     */
    public Ranker(boolean toStem){
        this.toStem = toStem;
        numOfDocs = 0;
        docsLenghsNoStem=null;
        docsLenghsStem=null;
        avrLengh = 0;
        k=0;
        b=0;
    }

    public void Test(){
        System.out.println(calculateAverageOfDocsLengths());
    }

    public void setDocsLenghsNoStem(HashMap<String, Integer> docsLenghsNoStem) {
        this.docsLenghsNoStem = docsLenghsNoStem;
    }

    public void setDocsLenghsStem(HashMap<String, Integer> docsLenghsStem) {
        this.docsLenghsStem = docsLenghsStem;
    }

    public void setNumOfDocs(int numOfDocs) {
        this.numOfDocs = numOfDocs;
    }

    private double calculateAverageOfDocsLengths(){
        double avrLengh = 0;
        if(toStem) {
            if(docsLenghsStem!=null && docsLenghsStem.size()>0) {
                Set<String> keys = docsLenghsStem.keySet();
                for (String docID : keys) {
                    avrLengh += docsLenghsStem.get(docID);
                }
                avrLengh = avrLengh / docsLenghsStem.size();
            }
        }
        else {
            if(docsLenghsNoStem!=null && docsLenghsNoStem.size()>0) {
                Set<String> keys = docsLenghsNoStem.keySet();
                for (String docID : keys) {
                    avrLengh += docsLenghsNoStem.get(docID);
                }
                avrLengh = avrLengh / docsLenghsNoStem.size();
            }
        }
        return avrLengh;
    }

    public HashMap<String,Integer> rankDocQuery(ArrayList<String> wordsFromQuery, HashSet<String> docsID){
        double avdl = calculateAverageOfDocsLengths();
        HashMap<String, int[]> docsOfWord = null;//todo take it from word object tf of each word at each doc
        int m = numOfDocs;
        QueryWord qw = new QueryWord();
        double rankOfDocQuery = 0;
        //best match doc will be the first, second be the after him.....
        HashMap<String,Double> docsRank = new HashMap<>();
        //foreach doc
        for (String docID : docsID) {
            rankOfDocQuery = 0;
            //foreach word from query (sigma)
            for (String word : wordsFromQuery) {
                //C(w,q)
                int wordAppearanceAtQuery = qw.getApearanceAtQuery();
                int tfAtCurrDoc = docsOfWord.get(docID)[0];//C(w,d)
                int df = qw.getdf();
                int d = 0;
                if(toStem)
                    d = docsLenghsStem.get(docID);
                else d = docsLenghsNoStem.get(docID);
                //formula
                //d/avdl
                double temp1 = d/avdl;
                //mehane
                double mehane = (tfAtCurrDoc+k*(1-b+b*(temp1)));
                //log
                double log = Math.log10((m+1)/(df));
                //mone
                double mone = ((k+1)*tfAtCurrDoc)*wordAppearanceAtQuery*log;
                double currWordCalc = mone/mehane;
                rankOfDocQuery+=currWordCalc;
            }
            docsRank.put(docID,rankOfDocQuery);
        }
            return null;
    }

}
