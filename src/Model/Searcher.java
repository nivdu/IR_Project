package Model;

import java.io.*;
import java.util.*;

public class Searcher {
    private Parse parse;
    private Ranker ranker;
    private HashMap<String,String[]> dictionaryPosting; //df,tf_overall,pointerToPosting


    public Searcher(Parse parse){
        this.parse = parse;
    }

    public HashMap<String,Double> runQuery(Query query, boolean toStem, String pathTo, List<String> chosenCities) {
        HashSet<String> citiesDocs = docsOfCities(chosenCities,toStem,pathTo);
        ArrayList<QueryWord> listOfWords = new ArrayList<>();
        query.setQuerySplited(query.getData().split(" "));//todo
        HashMap<String, int[]> queryTermsTF = parse.parseMainFunc(null, query);
        HashSet<String> allRelevantDocsInPosting = new HashSet<>();
        boolean isLoad = dictionaryPosting!=null;//todo check
        if (!isLoad){
            loadDictionaryFromDisk(toStem, pathTo);
        }
        String pathToCreate;
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        Set<String> keys = queryTermsTF.keySet();
        //foreach word in query
        for (String term : keys) {
            if(!dictionaryPosting.containsKey(term))
                continue;
            String[] dfTfPointer = dictionaryPosting.get(term);
            String pointerToPosting = dfTfPointer[2];
            long pointer = Long.valueOf(pointerToPosting).longValue();
            try {
                RandomAccessFile raf = new RandomAccessFile(pathToCreate + "/Postings/unitedPosting.txt", "rw");
                raf.seek(pointer);
                String linePosting = raf.readLine();
                String[] lineSplitedByTerm = linePosting.split(":");
                String[] docsSplitedInLine = lineSplitedByTerm[1].split(";");
                HashMap<String, int[]> docsOfWord = new HashMap<>();
                for (String doc : docsSplitedInLine) {
                    String[] docIDTFTitle = doc.split(",");
                    if (docIDTFTitle.length < 3)
                        System.out.println("problem line 48 Searcher");
                    int[] tfTitle = {Integer.parseInt(docIDTFTitle[1]), Integer.parseInt(docIDTFTitle[2])};//TF overall, title
                    docsOfWord.put(docIDTFTitle[0], tfTitle);//docID,TF,Title
                    //Insert docID to HashSet
                    if (citiesDocs == null)
                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
                    else if (citiesDocs.contains(docIDTFTitle[0]))
                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
                }
                QueryWord queryWord = new QueryWord(term, docsOfWord, queryTermsTF.get(term)[0], Integer.parseInt(dfTfPointer[0]));
                listOfWords.add(queryWord);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HashMap<String,Double> funcQueryDesc=null;
        if(query.getDesc()!= null && !query.getQueryID().equals("desc") && !query.getDesc().equals("desc")) {
            Query queryDesc = new Query(query.getDesc(), "desc", "desc");
            funcQueryDesc = runQuery(queryDesc, toStem, pathTo, chosenCities);
        }
        HashMap<String,Double> test = ranker.RankQueryDocs(listOfWords, allRelevantDocsInPosting, funcQueryDesc);//if query like niv and loren (the campus dont contain it) this will return null. todo handle it
        return test;
    }

    public HashMap<String, String[]> getDictionaryPosting() {
        return dictionaryPosting;
    }

//    private HashMap<String,Double> func1(Query query, boolean toStem, String pathTo, List<String> chosenCities) {
//        HashSet<String> citiesDocs = docsOfCities(chosenCities,toStem,pathTo);
//        ArrayList<QueryWord> listOfWords = new ArrayList<>();
//        query.setQuerySplited(query.getData().split(" "));//todo
//        HashMap<String, int[]> queryTermsTF = parse.parseMainFunc(null, query);
//        HashSet<String> allRelevantDocsInPosting = new HashSet<>();
//        boolean isLoad = dictionaryPosting!=null;//todo check
//        if (!isLoad)
//            System.out.println("problem");
//        String pathToCreate;
//        if (toStem) {
//            pathToCreate = pathTo + "\\WithStemming";
//        } else pathToCreate = pathTo + "\\WithoutStemming";
//        Set<String> keys = queryTermsTF.keySet();
//        //foreach word in query
//        for (String term : keys) {
//            if(!dictionaryPosting.containsKey(term))
//                continue;
//            String[] dfTfPointer = dictionaryPosting.get(term);
//            String pointerToPosting = dfTfPointer[2];
//            long pointer = Long.valueOf(pointerToPosting).longValue();
//            try {
//                RandomAccessFile raf = new RandomAccessFile(pathToCreate + "/Postings/unitedPosting.txt", "rw");
//                raf.seek(pointer);
//                String linePosting = raf.readLine();
//                String[] lineSplitedByTerm = linePosting.split(":");
//                String[] docsSplitedInLine = lineSplitedByTerm[1].split(";");
//                HashMap<String, int[]> docsOfWord = new HashMap<>();
//                for (String doc : docsSplitedInLine) {
//                    String[] docIDTFTitle = doc.split(",");
//                    if (docIDTFTitle.length < 3)
//                        System.out.println("problem");
//                    int[] tfTitle = {Integer.parseInt(docIDTFTitle[1]), Integer.parseInt(docIDTFTitle[2])};//TF overall, title
//                    docsOfWord.put(docIDTFTitle[0], tfTitle);//docID,TF,Title
//                    //Insert docID to HashSet
//                    if (citiesDocs == null)
//                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
//                    else if (citiesDocs.contains(docIDTFTitle[0]))
//                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
//                }
//                QueryWord queryWord = new QueryWord(term, docsOfWord, queryTermsTF.get(term)[0], Integer.parseInt(dfTfPointer[0]));
//                listOfWords.add(queryWord);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return ranker.RankQueryDocs(listOfWords, allRelevantDocsInPosting, null);//if query like niv and loren (the campus dont contain it) this will return null. todo handle it
//    }

    private HashSet<String> docsOfCities(List<String> chosenCities, boolean toStem, String pathTo) {
        if (chosenCities == null || chosenCities.size() == 0)
            return null;
        String pathToCreate;
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        HashMap<String, String> citiesAndPointer = loadCitiesDictionaryFromDisk(toStem, pathTo);
        HashSet<String> docsInCities = null;
        Set<String> keys = citiesAndPointer.keySet();
        //foreach relevant city
        for (String city : keys) {
            String pointer = citiesAndPointer.get(city);
            long pointerLong = Long.valueOf(pointer).longValue();
            String line = "";
            try {
                RandomAccessFile raf = new RandomAccessFile(pathToCreate + "/citiesPostings/unitedPosting.txt", "rw");
                raf.seek(pointerLong);
                String linePosting = raf.readLine();
                String[] lineSplitedByCity = linePosting.split(":");
                if (lineSplitedByCity.length < 2)
                    System.out.println("line 92 Searcher");
                String[] docsSplitedInLine = lineSplitedByCity[1].split(";");
                docsInCities = new HashSet<>();
                for (String doc : docsSplitedInLine) {
                    String[] docIDTFTitle = doc.split(",");
                    if (docIDTFTitle.length < 3)
                        System.out.println("line 98 Searcher");
                    docsInCities.add(docIDTFTitle[0]);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return docsInCities;
    }


    /**
     * First String is name of city
     * Second string is pointer to cityPosting
     *
     * @param toStem
     * @param pathTo
     * @return
     */
    private HashMap<String, String> loadCitiesDictionaryFromDisk(boolean toStem, String pathTo) {
        HashMap<String,String> cityAndPointer = new HashMap<>();
        String pathToCreate = "";
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        File fileDictionary = new File(pathToCreate + "/Dictionaries/cityDictionary.txt");
        try {
            String line = "";
            BufferedReader bf = new BufferedReader(new FileReader(fileDictionary));
            line = bf.readLine();
            while (line != null && line != "") {
                String[] splitedLineInDicByTerm = line.split(":|\\;");
                if(splitedLineInDicByTerm.length<2)
                    System.out.println("problem in size of cityDictionary");
                cityAndPointer.put(splitedLineInDicByTerm[0],splitedLineInDicByTerm[splitedLineInDicByTerm.length-1]);//city,pointer
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityAndPointer;
    }

    public boolean loadDictionaryFromDisk(boolean toStem,String pathTo) {
        dictionaryPosting = new HashMap<>();
        String pathToCreate = "";
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        File fileDictionary = new File(pathToCreate + "/Dictionaries/Dictionary.txt");
        try {
            String line = "";
            BufferedReader bf = new BufferedReader(new FileReader(fileDictionary));
            line = bf.readLine();
            while (line != null && line != "") {
                String[] splitedLineInDictionaryByTerm = line.split(":");
                if(splitedLineInDictionaryByTerm.length<2)
                    System.out.println("problem line 216 Searcher");
                String[] splitedLineInDictionary = splitedLineInDictionaryByTerm[1].split(";");
                if (splitedLineInDictionary == null) return false;
                String[] dfPostingTF = new String[3];
                dfPostingTF[0] = splitedLineInDictionary[0];//df
                dfPostingTF[1] = splitedLineInDictionary[1];//tf_overall
                dfPostingTF[2] = splitedLineInDictionary[2];//pointer to posting
                dictionaryPosting.put(splitedLineInDictionaryByTerm[0], dfPostingTF);
                line = bf.readLine();
            }
            HashMap<String, document> docsHash = loadDocsFile(toStem, pathTo);
            this.ranker = new Ranker(docsHash);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private HashMap<String,document> loadDocsFile(boolean toStem, String pathTo) {
        HashMap<String, document> docsHash = new HashMap<>();
        try {
            FileInputStream fis;
            if (toStem)
                fis = new FileInputStream(pathTo + "/WithStemming/docsData.txt");
            else
                fis = new FileInputStream(pathTo + "/WithoutStemming/docsData.txt");
            ObjectInputStream objIS = new ObjectInputStream(fis);
            docsHash = (HashMap) objIS.readObject();
            objIS.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (IOException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (ClassNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        }
        return docsHash;
    }

    /**
     * cut the Query string by tags
     * @param query - Query to cut
     * @return - String array of the Query contents.
     */
    private document queryCut(String query){
        if(query==null || query.equals(""))
            return null;
        document queryDoc = null;
        return queryDoc;
    }


    public HashSet<String> setCities() {//todo function
        HashSet<String> cities = new HashSet<>();



        return  cities;
    }
}
