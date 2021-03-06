package Model;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranker {

    private HashMap<String, document> docs;
    private double avrLengh;

    /**
     * contractor
     */
    public Ranker(HashMap<String, document> docs) {
        this.docs = docs;
        avrLengh = calculateAverageOfDocsLengths();
    }

    /**
     * CALLED from the constructor and claculate the avr length of corpus documents
     * @return - double of the avr length
     */
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

    /**
     * use all the alghoritems for calc the rank of documents for the current query.
     * @param wordsFromQuery - all the words from the given query
     * @param docsID         - docs that have at least one appearnce of query word
     * @return - sorted hashmap (sort from big to small by the double) contain all the docs id's and their ranks
     */
    public HashMap<String, Double> RankQueryDocs(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID) {
        if (wordsFromQuery == null || docsID == null)
            return null;
        ArrayList<HashMap<String, Double>> docsRanks = new ArrayList<>();
        docsRanks.add(BM25(wordsFromQuery, docsID));
        return sortHashMapByValues(docsRanks.get(0));
    }

    public HashMap<String, Double> sortHashMapByValues(HashMap<String, Double> passedMap) {
        //Sorting the HashMap by values
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(passedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        HashMap<String, Double> sortedHashMap50 = new LinkedHashMap<String, Double>();
        double max = 0;
        double prevMax = Integer.MAX_VALUE;
        String currMaxDoc = "";
        int numOfDocs = passedMap.size();
        if (numOfDocs > 50)
            numOfDocs = 50;
        while (sortedHashMap50.size() < numOfDocs) {
            for (Map.Entry<String, Double> aa : list) {
                if (aa.getValue() > max && aa.getValue() <= prevMax && !sortedHashMap50.containsKey(aa.getKey())) {
                    max = aa.getValue();
                    currMaxDoc = aa.getKey();
                }
            }
            sortedHashMap50.put(currMaxDoc, max);
            prevMax = max;
            max = 0;
            currMaxDoc = "";
        }
        //for reduce memory use and runtime return only the first 50 docs.
        return sortedHashMap50;
    }

    /**
     * for each doc calc is rank by is publish date
     * @param docsID - all the docs contain at least one word of the query
     *               return - Hash map key - docId , value - count of the number of word from the query placed at the doc title
     */
    private HashMap<String, Double> publishDataRank(HashSet<String> docsID) {
        if (docsID != null)
            return new HashMap<>();
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
                        docsRank.put(docID, ((double) 1 / difference));
                    }
                }
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
    private HashMap<String, Double> combineArrayByWights(ArrayList<HashMap<String, Double>> docsRanks) {
        HashMap<String, Double> combinedDocsRank = new HashMap<>();
        double[] weights = new double[docsRanks.size()];//+1 for the describe query, sometimes contained and sometimes not.
        weights[0] = 0.9;//bm25
        for (int j = 0; j < docsRanks.size(); j++) {
            int order = 0;
            double temp2 = 0;
            HashMap<String, Double> currDocsRank = docsRanks.get(j);
            List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(currDocsRank.entrySet());
            for (Map.Entry<String, Double> aa : list) {
                String docID = aa.getKey();
                double temp = aa.getValue();
                if (temp != temp2 || order == 0) {
                    order++;
                    temp2 = temp;
                }
                if (!combinedDocsRank.containsKey(docID))
                    combinedDocsRank.put(docID, weights[j] * (1 / (double) order));
                else combinedDocsRank.put(docID, (weights[j] * (1 / (double) order)) + combinedDocsRank.get(docID));
            }
        }
        return sortHashMapByValues(combinedDocsRank);
    }

    /**
     * for each doc count the number of word from the query placed at the doc title.
     * @param wordsFromQuery - the words from the query
     * @param docsID         - all the docs contain at least one word of the query
     *                       return - Hash map key - docId , value - count of the number of word from the query placed at the doc title
     */
    private HashMap<String, Double> inTitleCalc(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID) {
        if (wordsFromQuery != null)
            return new HashMap<>();
        HashMap<String, Double> docsRank = new HashMap<>();
        for (QueryWord qw : wordsFromQuery) {
            HashMap<String, int[]> docsOfWord = qw.getDocsOfWord();
            Set<String> keys = docsOfWord.keySet();
            for (String docID : keys) {
                if (docsOfWord.get(docID)[1] == 1) {
                    if (docsRank.containsKey(docID))
                        docsRank.put(docID, docsRank.get(docID) + 1);
                    else
                        docsRank.put(docID, 1.0);
                }
            }
        }
        return docsRank;
    }

    public HashMap<String, document> getDocs() {
        return docs;
    }

    /**
     * bm25 algorithem for rank docs for queries
     * @param wordsFromQuery - all the words from the current query
     * @param docsID         - all the docs from corpus that at least one query word found on them.
     * @return - HashMap contains all the docs and their rank (double) from bm25 algho.
     */
    public HashMap<String, Double> BM25(ArrayList<QueryWord> wordsFromQuery, HashSet<String> docsID) {
        double k = 1.2;
        double b = 0.5;
        double avdl = avrLengh;
        int m = docs.size();
        //best match doc will be the first, second be the after him.....
        HashMap<String, Double> docsRank = new HashMap<>();
        //foreach word from query (sigma)
        for (QueryWord Qword : wordsFromQuery) {
            HashMap<String, int[]> docsOfWord = Qword.getDocsOfWord();
            if (docsOfWord == null || docsOfWord.size() == 0)
                continue;
            //foreach doc
            Set<String> keys = docsOfWord.keySet();
            for (String docID : keys) {
                double rankOfDocQuery = 0;
                //C(w,q)
                int wordAppearanceAtQuery = Qword.getNumOfWordInQuery();
                int tfAtCurrDoc = docsOfWord.get(docID)[0];//C(w,d)
                //word from title = 2 word from text
                if (docsOfWord.get(docID)[1] == 1) {
                    tfAtCurrDoc++;
                }
                int df = Qword.getDf();
                int d;
                d = docs.get(docID).getDocLength();
                //formula
                //d/avdl
                double temp1 = d / avdl;
                //mehane
                double mehane = (tfAtCurrDoc + k * (1 - b + b * (temp1)));
                //log
                double log = Math.log10((m - df + 0.5) / (df + 0.5));
                double log1 = Math.log10((m + 1) / (df));
                //mone
                double mone = ((k + 1) * tfAtCurrDoc) * wordAppearanceAtQuery * log;
                double mone1 = ((k + 1) * tfAtCurrDoc) * wordAppearanceAtQuery * log1;
                double currWordCalc = (mone / mehane) + (mone1 / mehane);
                rankOfDocQuery += currWordCalc;
                rankOfDocQuery += (0.5 * (tfAtCurrDoc / Integer.parseInt(Qword.getTfOverAll())));
                if (docsRank.containsKey(docID))
                    docsRank.put(docID, rankOfDocQuery + docsRank.get(docID));
                else docsRank.put(docID, rankOfDocQuery);
            }
        }
        return docsRank;
    }


    /**
     * Get the entities of a given doc
     * @param docID  - ID od current doc
     * @param pathTo - The path to create to
     * @param toStem - true if to do with stemming, else false.
     */
    public HashMap<String, Double> getEntities(String docID, String pathTo, boolean toStem) {
        document docEntities = docs.get(docID);
        HashMap<String, Double> entitiesTF = new HashMap<>();
        String path = "";
        if (toStem) {
            path = pathTo + "\\WithStemming\\Entities.txt";
        } else path = pathTo + "\\WithoutStemming\\Entities.txt";
        File fileEntities = new File(path);
        if (!fileEntities.exists()) {
            System.out.println("line 285 ranker class");
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(path, "rw");
            raf.seek(docEntities.getPointerToEntities());
            String line = raf.readLine();
            String[] lineSplited = line.split(":");
            if (lineSplited.length < 2)
                System.out.println("line 211 Ranker");
            String[] lineSplitedByEnt = lineSplited[1].split(";");
            for (String entity : lineSplitedByEnt) {
                String[] currEntityTF = entity.split(",");
                if (currEntityTF.length < 2)
                    System.out.println("line 217 Ranker");
                double tf = Double.parseDouble(currEntityTF[1]);
                entitiesTF.put(currEntityTF[0], (tf / docEntities.getMaxTf()));
            }
            //Sorting the HashMap by values
            List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(entitiesTF.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
            HashMap<String, Double> entitiesList = new LinkedHashMap<String, Double>();
            for (Map.Entry<String, Double> aa : list) {
                entitiesList.put(aa.getKey(), aa.getValue());
            }
            return entitiesList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
