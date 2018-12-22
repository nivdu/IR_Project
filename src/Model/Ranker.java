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

    public HashMap<String,Double> RankQueryDocs(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID){
        ArrayList<HashMap<String,Double>> docsRanks = new ArrayList<>();
        docsRanks.add(inTitleCalc(wordsFromQuery, docsID));
        docsRanks.add(BM25(wordsFromQuery, docsID));
        return combineArrayByWights(docsRanks);
    }

    /**
     * combine the docs ranks from all the functions to one Hashset by the wight of each function
     * @param docsRanks - array of HashSet of docs Ranks from all function
     */
    private HashMap<String,Double> combineArrayByWights(ArrayList<HashMap<String,Double>> docsRanks) {
        HashMap<String,Double> combinedDocsRank = new HashMap<>();
        int i = 0;
        double[] weights = new double[docsRanks.size()];
        weights[0] = 0.1;//todo from here
        weights[1] = 0.1;
        weights[2] = 0.1;
        weights[3] = 0.1;
        weights[4] = 0.1;//todo until here
        for (HashMap<String,Double> currDocsRank: docsRanks) {

            Set<String> keys = currDocsRank.keySet();
            for (String docID : keys) {
                if(!combinedDocsRank.containsKey(docID))
                    combinedDocsRank.put(docID, weights[i]*currDocsRank.get(docID));
                else combinedDocsRank.put(docID, weights[i]*currDocsRank.get(docID) + combinedDocsRank.get(docID));
            }
            i++;
        }
        return combinedDocsRank;
    }

    /**
     * for each doc count the number of word from the query placed at the doc title.
     * @param wordsFromQuery - the words from the query
     * @param docsID - all the docs contain at least one word of the query
     * return - Hash map key - docId , value - count of the number of word from the query placed at the doc title
     */
    private HashMap<String,Double> inTitleCalc(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID) {
        HashMap<String,Double> docsRank = new HashMap<>();
        for (QueryWord qw:wordsFromQuery){
            HashMap<String,int[]> docsOfWord = qw.getDocsOfWord();
            Set<String> keys = docsOfWord.keySet();
            for (String docID : keys) {
                if(docsOfWord.get(docID)[1]==1) {
                    if(docsRank.containsKey(docID))
                        docsRank.put(docID,docsRank.get(docID)+1);
                    else
                        docsRank.put(docID, 1.0);
                }
                else if(!docsOfWord.containsKey(docID))
                    docsRank.put(docID,0.0);
            }
        }
        return docsRank;
    }

    public HashMap<String,Double> BM25(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID){
        double avdl = calculateAverageOfDocsLengths();
        int m = numOfDocs;
        double rankOfDocQuery;
        //best match doc will be the first, second be the after him.....
        HashMap<String,Double> docsRank = new HashMap<>();
        //foreach doc
        for (String docID : docsID) {
            rankOfDocQuery = 0;
            //foreach word from query (sigma)
            for (QueryWord Qword : wordsFromQuery) {
                HashMap<String, int[]> docsOfWord = Qword.getDocsOfWord();//todo take it from word object tf of each word at each doc
                //C(w,q)
                int wordAppearanceAtQuery = Qword.getNumOfWordInQuery();
                int tfAtCurrDoc = docsOfWord.get(docID)[0];//C(w,d)
                int df = Qword.getDf();
                int d;
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
        return docsRank;
    }

}