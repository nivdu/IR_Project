package Model;

import sun.awt.Mutex;

import java.io.*;
import java.util.*;

public class Searcher {
    private Parse parse;
    private Ranker ranker;
    private HashMap<String,String[]> dictionaryPosting; //df,tf_overall,pointerToPosting
    private Mutex mutex = new Mutex();


    public Searcher(Parse parse){
        this.parse = parse;
    }

    public HashMap<String,Double> runQuery(Query query, boolean toStem, String pathTo, List<String> chosenCities) {
        HashSet<String> citiesDocs = docsOfCities(chosenCities,toStem,pathTo);
        ArrayList<QueryWord> listOfWords = new ArrayList<>();
        query.setQuerySplited(query.getData().split(" "));
        mutex.lock();
        HashMap<String, int[]> queryTermsTF = parse.parseMainFunc(null, query);
        mutex.unlock();
        HashSet<String> allRelevantDocsInPosting = new HashSet<>();
        boolean isLoad = dictionaryPosting!=null;//todo check
        mutex.lock();
        if (!isLoad){
            loadDictionaryFromDisk(toStem, pathTo);
        }
        mutex.unlock();
        String pathToCreate;
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        Set<String> keys = queryTermsTF.keySet();
        String termLikeDic = "";
        //foreach word in query
        for (String term : keys) {
            if(dictionaryPosting.containsKey(term.toLowerCase())) {
                termLikeDic = term.toLowerCase();
            }
            else if(dictionaryPosting.containsKey(term.toUpperCase()))
                termLikeDic = term.toUpperCase();
            else continue;
            String[] dfTfPointer = dictionaryPosting.get(termLikeDic);
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

                    //Insert docID to HashSet
                    if (citiesDocs == null) {
                        docsOfWord.put(docIDTFTitle[0], tfTitle);//docID,TF,Title
                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
                    }
                    else if (citiesDocs.contains(docIDTFTitle[0])) {
                        docsOfWord.put(docIDTFTitle[0], tfTitle);//docID,TF,Title
                        allRelevantDocsInPosting.add(docIDTFTitle[0]);
                    }
                }
                QueryWord queryWord = new QueryWord(termLikeDic, docsOfWord, queryTermsTF.get(term)[0], Integer.parseInt(dfTfPointer[0]));
                listOfWords.add(queryWord);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        HashMap<String,Double> funcQueryDesc=null;
//        if(query.getDesc()!= null && !query.getQueryID().equals("desc") && !query.getDesc().equals("desc")) {
//            Query queryDesc = new Query(query.getDesc(), "desc", "desc");
//            funcQueryDesc = runQuery(queryDesc, toStem, pathTo, chosenCities);
//        }
        HashMap<String,Double> test = ranker.RankQueryDocs(listOfWords, allRelevantDocsInPosting);//if query like niv and loren (the campus dont contain it) this will return null. todo handle it
        return test;
    }

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
                RandomAccessFile raf = new RandomAccessFile(pathToCreate + "/citiesPosting/unitedPosting.txt", "rw");
                raf.seek(pointerLong);
                String linePosting = raf.readLine();
                String[] lineSplitedByCity = linePosting.split(":");
                if (lineSplitedByCity.length < 2)
                    System.out.println("line 92 Searcher");
                String[] docsSplitedInLine = lineSplitedByCity[1].split(";");
                docsInCities = new HashSet<>();
                for (String doc : docsSplitedInLine) {
                    String[] docIDTFTitle = doc.split(",");
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
                line=bf.readLine();
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
        try {
            String pathToCreate;
            if (toStem) {
                pathToCreate = pathTo + "\\WithStemming";
            } else pathToCreate = pathTo + "\\WithoutStemming";
            String line = "";
            File fileDictionary = new File(pathToCreate + "/Dictionaries/Dictionary.txt");
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
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public HashMap<String, document> loadDocsFile(boolean toStem, String pathTo) {
            String pathToCreate;
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        HashMap<String, document> docsHash = new HashMap<>();
        try {
            FileInputStream fis;
            fis = new FileInputStream(pathToCreate + "/docsData.txt");
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
        this.ranker = new Ranker(docsHash);
        return docsHash;
    }

    /**
     * remove the dictionary posting
     */
    public void clearDic(){
            dictionaryPosting.clear();
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


    public HashSet<String> setCities(String pathTo, boolean toStem) {//todo check function
        HashSet<String> cities = new HashSet<>();
        String path = "";
        if (toStem) {
            path= pathTo + "\\WithStemming\\Dictionaries\\cityDictionary.txt";
        } else path= pathTo + "\\WithoutStemming\\Dictionaries\\cityDictionary.txt";
        File fileCityDictionary = new File (path);
        if(!fileCityDictionary.exists()){
            System.out.println("line 261 searcher");
        }
        try {
            BufferedReader br= new BufferedReader(new FileReader(fileCityDictionary));
            String line = br.readLine();
            while (line != null && line != "") {
                String[] splitedLineByCity = line.split(":");
                if(splitedLineByCity.length<2)
                    System.out.println("problem in size of cityDictionary");
                cities.add(splitedLineByCity[0]);
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  cities;
    }


    /**
     * URL - "https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values" of sorting hashmap
     * @param docID
     * @param pathTo
     * @param toStem
     */
    public HashMap<String,Double> getEntities(String docID, String pathTo, boolean toStem){
        document docEntities = docsHash.get(docID);
        HashMap<String,Double> entitiesTF = new HashMap<>();
        String path = "";
        if (toStem) {
            path= pathTo + "\\WithStemming\\Entities.txt";
        } else path= pathTo + "\\WithoutStemming\\Entities.txt";
        File fileEntities = new File (path);
        if(!fileEntities.exists()){
            System.out.println("problem");
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(path,"rw");
            raf.seek(docEntities.getPointerToEntities());
            String line = raf.readLine();
            String[] lineSplited = line.split(":");
            if(lineSplited.length<2)
                System.out.println("line 211 Ranker");
            String[] lineSplitedByEnt = lineSplited[1].split(";");
            for (String entity:lineSplitedByEnt) {
                String[] currEntityTF = entity.split(",");
                if(currEntityTF.length<2)
                    System.out.println("line 217 Ranker");
                double tf = Double.parseDouble(currEntityTF[1]);
                entitiesTF.put(currEntityTF[0],(tf/docEntities.getMaxTf()));
            }
            //Sorting the HashMap by values
            List<Map.Entry<String, Double> > list = new LinkedList<Map.Entry<String, Double> >(entitiesTF.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
                {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
            //todo check the sorting is working
            HashMap<String, Double> entitiesList = new LinkedHashMap<String, Double>();
            for (Map.Entry<String, Double> aa : list) {
                entitiesList.put(aa.getKey(), aa.getValue());
            }
            return entitiesList;
//            int countEntities = 0;
//            HashMap<String,Double> fiveEntities = new HashMap<>();
//            for (Map.Entry<String, Double> en : entitiesList.entrySet()) {
//                if(countEntities<5){
//                    fiveEntities.put(en.getKey(),en.getValue());
//                    countEntities++;
//                }
//                else break;
//            }
//            return fiveEntities;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
