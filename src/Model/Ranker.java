package Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranker {

    private HashMap<String,document> docs;
    private double avrLengh;
    private double k;
    private double b;

    /**
     * contractor
     */
    public Ranker(HashMap<String,document> docs){
        this.docs=docs;
        avrLengh = 0;
        k=1.5;
        b=0.75;
    }

    private double calculateAverageOfDocsLengths() {
        double avrLengh = 0;
        if (docs != null && docs.size() > 0) {
            Set<String> keys = docs.keySet();
            for (String docID : keys) {
                document currDoc = docs.get(docID);
                if (currDoc != null) {
                    avrLengh += currDoc.getDocLength();
                }
            }
            avrLengh = avrLengh / docs.size();
        }
        return avrLengh;
    }

    public HashMap<String,Double> RankQueryDocs(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID, HashMap<String,Double> queryDesc){
        if(wordsFromQuery==null || docsID == null)
            return null;
        avrLengh = calculateAverageOfDocsLengths();
        ArrayList<HashMap<String,Double>> docsRanks = new ArrayList<>();
        ArrayList<HashMap<String,Double>> docsRanks2Return = new ArrayList<>();

        //run all the rank functions
        docsRanks.add(inTitleCalc(wordsFromQuery, docsID));
        docsRanks.add(BM25(wordsFromQuery, docsID));
        try { docsRanks.add(publishDataRank(docsID)); } catch (Exception e){}
        if(queryDesc!=null && queryDesc.size()>0)
            docsRanks.add(queryDesc);//function ranked by description
        //sort and normalize docs ranks
        for (HashMap hm:docsRanks){
            docsRanks2Return.add(sortHashMapByValues(hm));
        }
        for (HashMap<String,Double> hm:docsRanks2Return) {
            Set<String> keys = hm.keySet();
            int order = 0;
            double temp2 = 0;
            for (String currDoc : keys) {
                double temp = hm.get(currDoc);
                if (temp != temp2)
                    order++;
                if (order == 0)
                    order++;
                hm.put(currDoc, (1 / (double) order));
            }
        }

        return combineArrayByWights(docsRanks2Return);
    }

    public LinkedHashMap<String, Double> sortHashMapByValues(
            HashMap<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Double> sortedMap =
                new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();
            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;
                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    /**
     * for each doc calc is rank by is publish date
     * @param docsID - all the docs contain at least one word of the query
     * return - Hash map key - docId , value - count of the number of word from the query placed at the doc title
     */
    private HashMap<String,Double> publishDataRank(HashSet<String> docsID) {
        HashMap<String, Double> docsRank = new HashMap<>();
        for (String docID : docsID) {
            document currDoc = docs.get(docID);
            if (currDoc != null) {
                int[] docDate = currDoc.getDocSplitDate();
                if (docDate != null && docDate.length == 3) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    Date date = new Date();
                    String currDate = dateFormat.format(date);
                    if (currDate != null) {
                        int[] currDateInt = getCurrDateInt(currDate);
                        int difference = ((currDateInt[0] - docDate[0]) * 365);
                        //bad publish date year for currDoc , cant be bigger than today's date
                        if (difference < 0)
                            difference = 0;
                        difference += (Math.abs(currDateInt[1] - docDate[1]) * 30);
                        difference += (Math.abs(currDateInt[2] - docDate[2]));
                        if (difference == 0)
                            difference = 1;
                        docsRank.put(docID, ((double) 1 / difference));//todo find better calc for difference between dates.
                    }
                }
                if(!docsRank.containsKey(docID))
                    docsRank.put(docID,0.0);
            }
        }
        return docsRank;
    }

    private int[] getCurrDateInt(String date) {
        String[] dateByParts;
        int[] dateByPartsInt = new int[3];
        if (date != null) {
            dateByParts = date.split("/");
            dateByPartsInt[0] = Integer.parseInt(dateByParts[0]);
            dateByPartsInt[1] = Integer.parseInt(dateByParts[1]);
            dateByPartsInt[2] = Integer.parseInt(dateByParts[2]);
        }
        return dateByPartsInt;
    }

    /**
     * combine the docs ranks from all the functions to one Hashset by the wight of each function
     * @param docsRanks - array of HashSet of docs Ranks from all function
     */
    private HashMap<String,Double> combineArrayByWights(ArrayList<HashMap<String,Double>> docsRanks) {
        HashMap<String,Double> combinedDocsRank = new HashMap<>();
        int i = 0;
        double[] weights = new double[docsRanks.size()];
        //todo from here
        weights[0] = 1;//bm25
        weights[1] = 0.1;//title
        weights[2] = 0.1;//date
        if(weights.length==4)
            weights[3] = 0.1;//by description
//        weights[4] = 0.1;
        // todo until here
        for (HashMap<String,Double> currDocsRank: docsRanks) {
            Set<String> keys = currDocsRank.keySet();
            for (String docID : keys) {
                if(!combinedDocsRank.containsKey(docID))
                    combinedDocsRank.put(docID, weights[i]*currDocsRank.get(docID));
                else combinedDocsRank.put(docID, weights[i]*currDocsRank.get(docID) + combinedDocsRank.get(docID));
            }
            i++;
        }
        return sortHashMapByValues(combinedDocsRank);
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
                else if(!docsRank.containsKey(docID))
                    docsRank.put(docID,0.0);
            }
        }
        return docsRank;
    }

    public HashMap<String,Double> BM25(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID){
        double avdl = avrLengh;
        int m = docs.size();
        double rankOfDocQuery;
        //best match doc will be the first, second be the after him.....
        HashMap<String,Double> docsRank = new HashMap<>();
        //foreach word from query (sigma)
        for (QueryWord Qword : wordsFromQuery) {
            rankOfDocQuery = 0;
            HashMap<String, int[]> docsOfWord = Qword.getDocsOfWord();
            if(docsOfWord==null || docsOfWord.size()==0)
                continue;
            //foreach doc
            Set<String> keys = docsOfWord.keySet();
            for (String docID : keys) {
//                HashMap<String, int[]> docsOfWord = Qword.getDocsOfWord();//todo take it from word object tf of each word at each doc
                //C(w,q)
                int wordAppearanceAtQuery = Qword.getNumOfWordInQuery();
                int tfAtCurrDoc = docsOfWord.get(docID)[0];//C(w,d)
                int df = Qword.getDf();
                int d;
                d = docs.get(docID).getDocLength();
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
                if(docsRank.containsKey(docID))
                    docsRank.put(docID, rankOfDocQuery + docsRank.get(docID));
                else docsRank.put(docID, rankOfDocQuery);
            }
        }
            return docsRank;
    }

}
