package Model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import sun.awt.Mutex;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class Model {
    private Indexer indexer;
    private Searcher searcher;
    private boolean loadedStem;
    private boolean loadedWithoutStem;

    /**
     * creating the dictionary and the posting of the inverted index
     */
    public boolean generateInvertedIndex(String pathFrom, String pathTo, boolean toStem) {
        //check the inserted path from.
        if (!checkIfLegalPaths(pathFrom, pathTo))
            return false;
        Parse parse = new Parse(toStem, pathFrom);
        indexer = new Indexer(pathFrom, pathTo, parse);
        long Stime = System.currentTimeMillis();
        boolean succGenerate = indexer.createPostingAndDic(toStem);
        long Ftime = System.currentTimeMillis();
        Alert alert;
        if (succGenerate) {
            long indexRunTime = Ftime - Stime;
            int indexedDocNumber = indexer.getIndexedDocNumber();
            int uniqueTermsNumber = indexer.getUniqueTermsNumber();
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Commit succeed!");
            alert.setContentText("runtime by seconds: " + indexRunTime / 1000 + "\n" +
                    "number Indexed docs: " + indexedDocNumber + "\n" +
                    "number unique Terms: " + uniqueTermsNumber);
            alert.show();
            return true;
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Commit");
            alert.setContentText("Commit Failed!");
            alert.show();
            return false;
        }
    }

    /**
     * reset the posting ,the dictionary file and the memory of the program
     *
     * @return true if the reset succeed' else return false
     */
    public boolean reset() {
        Alert verification = new Alert(Alert.AlertType.CONFIRMATION);
        verification.setHeaderText("verification:");
        verification.setContentText("Are You Sure You Want To Reset?");
        verification.showAndWait();
        ButtonType s = verification.getResult();
        if (s.getButtonData().getTypeCode().equals("C")) {
            return false;
        }

        if (indexer.reset())
            return true;
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("RESET");
            alert.setContentText("Reset Failed!");
            alert.showAndWait();
            return false;
        }
    }


    /**
     * take the dictionary of the terms from the indexer : term, tf overall
     *
     * @return the dictionary of the terms
     */
    public List<String> displayDictionary(boolean isStem, String pathTo) {
        File checkPathTo = new File(pathTo);
        if (!checkPathTo.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path to");
            chooseFile.setContentText("The path you selected is not legal or not a path of directory. please choose another one. :)");
            chooseFile.show();
            return null;
        } else {
            if (isStem) {
                File withStem = new File(pathTo + "\\withStemming");
                if (!withStem.exists()) {
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withStemming directory!");
                    chooseFile.show();
                    return null;
                }
            } else {
                File withoutStem = new File(pathTo + "\\withoutStemming");
                if (!withoutStem.exists()) {
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withoutStemming directory!");
                    chooseFile.show();
                    return null;
                }
            }
        }
        List<String> dictionary = indexer.displayDictionary(isStem);
        if (dictionary == null) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with Dictionary");
            chooseFile.setContentText("The dictionary is not exist");
            chooseFile.show();
            return null;
        }
        return dictionary;
    }


    /**
     * load the dictionary from the disk to the memory
     *
     * @return true if the loading succeed, else retutn false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean isStem, String pathTo, String pathFrom) throws InterruptedException {
        if (!checkIfDirectoryWithOrWithoutStemExist(isStem, pathTo))
            return false;
        if ((loadedStem && !isStem) || (loadedWithoutStem && isStem)) {
            searcher.clearDic();
        }
        loadedStem = isStem;
        loadedWithoutStem = !isStem;
        Parse parse1 = new Parse(isStem, pathFrom);
        searcher = new Searcher(parse1);
        long Stime = System.currentTimeMillis();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                searcher.loadDocsFile(isStem, pathTo);
            }
        });
        t1.start();
        boolean bl = searcher.loadDictionaryFromDisk(isStem, pathTo);
        t1.join();
        long Ftime = System.currentTimeMillis();
        System.out.println((Ftime - Stime) / 1000);
        if (bl) {
            return true;
        } else {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setContentText("Loading failed!");
            chooseFile.show();
            return false;
        }
    }

    private boolean checkIfDirectoryWithOrWithoutStemExist(boolean isStem, String pathTo) {
        File checkPathTo = new File(pathTo);
        if (!checkPathTo.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path to");
            chooseFile.setContentText("The path you selected is not legal or not a path of directory. please choose another one. :)");
            chooseFile.show();
            return false;
        } else {
            if (isStem) {
                File withStem = new File(pathTo + "\\withStemming");
                if (!withStem.exists()) {
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withStemming directory!");
                    chooseFile.show();
                    return false;
                }
            } else {
                File withoutStem = new File(pathTo + "\\withoutStemming");
                if (!withoutStem.exists()) {
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withoutStemming directory!");
                    chooseFile.show();
                    return false;
                }
            }
        }
        return true;
    }

    public HashSet<String> languages() {
        return indexer.languages();
    }

    private String addSemanticWords(String query) {
        //call to api
        String[] queryWords = query.split(" ");
        LinkedList<String> queryListWords = new LinkedList<>();
        if (queryWords.length > 0) {
            for (String word : queryWords) {
                URL url = null;
                try {
                    url = new URL("https://api.datamuse.com/words?ml=" + word);
                    //make connection
                    URLConnection urlc = url.openConnection();
                    //use post mode
                    urlc.setDoOutput(true);
                    urlc.setAllowUserInteraction(false);
                    //get result
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
                    String currLine = br.readLine();
                    if (currLine != null && !currLine.equals("")) {
                        String[] firstLineArr = currLine.split("\"word\":\"");
                        for (String s : firstLineArr) {
                            int i = 0;
                            String currWord = "";
                            while (s != null && i < s.length() && s.charAt(i) != '\"') {
                                if (Character.isLetter(s.charAt(i)))
                                    currWord += s.charAt(i);
                                i++;
                            }
                            if (currWord != null && currWord.length() > 0) {
                                queryListWords.add(currWord);
                                if (queryListWords.size() % 2 == 0)
                                    break;
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String s : queryListWords) {
            query = query + " " + s;
        }
        return query;
    }

    /**
     * can call only after inverted index is created, main function of rank query.
     *
     * @param query - query string from the user.
     * @return -  Return the docs order by ranks
     */
    public HashMap<String,Double> runQuery(String query, boolean toStem, String pathTo, String pathFrom, List<String> citiesChosen, boolean semantic,boolean toSaveResults,String pathForResults) {
        try {
            long Stime = System.currentTimeMillis();
            if (toStem && !loadedStem) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("load dictionary before query");
                chooseFile.setContentText("You must load again after choose withStem and then run a query!");
                chooseFile.show();
                return null;
            }
            if (!toStem && !loadedWithoutStem) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("load dictionary before query");
                chooseFile.setContentText("You must load again after choose withoutStem and then run a query!");
                chooseFile.show();
                return null;
            }
            //runQueryFile("", toStem, pathFrom, pathTo, semantic);
            long Ftime = System.currentTimeMillis();
            System.out.println((Ftime - Stime) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        int numberOfDocsAtCorpus = indexer.getIndexedDocNumber();
        //check the inserted path from.
        if (!checkIfLegalPaths(pathFrom, pathTo))
            return null;
        if (!checkIfDirectoryWithOrWithoutStemExist(toStem, pathTo))
            return null;
        //if semantic checkBox have V.
        if (semantic) {
            query += addSemanticWords(query);
        }
        Query currQuery = new Query(query, "111");
        HashMap<String, Double> queryResults = searcher.runQuery(currQuery, toStem, pathTo, citiesChosen);

        if(toSaveResults){
            try {
                File file = new File(pathForResults);
                if (!file.exists()){
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with path for saving results");
                    chooseFile.setContentText("The path you selected is not legal or not a path of directory. please choose another one. :)");
                    chooseFile.show();
                } else {
                    File resultsFile = new File(pathForResults + "\\QueryResults.txt");
                    if (resultsFile.exists())
                        resultsFile.delete();
                    writeToRes(pathForResults, queryResults, currQuery);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //todo view results in gui
        return queryResults;
    }

    /**
     * can call only after inverted index is created, main function of rank query.
     *
     * @param pathQueryFile - path of the query file.
     * @return -  Return the docs order by ranks
     */
    public HashMap<Query, HashMap<String, Double>> runQueryFile(String pathQueryFile,boolean toStem, String pathFrom, String pathTo, boolean semantic,List<String> citiesFromViewList,boolean toSaveResults,String pathForResults) throws IOException, InterruptedException {
        if (!checkIfLegalPaths(pathFrom, pathTo))
            return null;
        if (!checkIfDirectoryWithOrWithoutStemExist(toStem, pathTo))
            return null;
        ReadFile readfile = new ReadFile(pathQueryFile);
        //HashMap<String,String> queriesAndDocs = new HashMap<>();
        ArrayList<Query> queriesArr = readfile.readQueryFile(pathQueryFile, semantic);
        final ExecutorService executor = Executors.newFixedThreadPool(4); // it's just an arbitrary number
        final List<Future<?>> futures = new ArrayList<>();
        HashMap<Query, HashMap<String, Double>> ttt = new HashMap<Query, HashMap<String, Double>>();
        Queue<Query> QQ = new LinkedList<Query>();
        for (Query query : queriesArr) {
            ((LinkedList<Query>) QQ).add(query);
            Future<?> future = executor.submit(() -> {
                HashMap<String, Double> queryResults = searcher.runQuery(query, toStem, pathTo, citiesFromViewList);//todo maybe object of queryAns
                ttt.put(query, queryResults);
            });
            futures.add(future);
        }
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        futures.clear();
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
        if(toSaveResults) {
            File file = new File(pathForResults);
            if (!file.exists()){
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("Error with path for saving results");
                chooseFile.setContentText("The path you selected is not legal or not a path of directory. please choose another one. :)");
                chooseFile.show();
            } else {
                File resultsFile = new File(pathForResults + "\\QueryResults.txt");
                if (resultsFile.exists())
                    resultsFile.delete();
                int size = queriesArr.size();
                for (int i = 0; i < size; i++) {
                    Query tempQ = QQ.remove();
                    writeToRes(pathForResults, ttt.get(tempQ), tempQ);

                }
            }
        }
        //
        return ttt;
    }
  
  
    /**
     * Write to text file the 50 best match document for each query, write it for trecval style.
     */
    
    public void writeToRes(String pathForResults, HashMap<String, Double> queryResults, Query query) throws IOException {
                File resultsFile;
                resultsFile = new File(pathForResults + "\\QueryResults.txt");
                if (!resultsFile.exists()) {
                    resultsFile.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile, true));
                //todo pointer to entities
                //todo create line of entity : DocId:rank\n
                int count = 0;
                for (Map.Entry<String, Double> aa : queryResults.entrySet()) {
                    String writeMe = query.getQueryID() +" 0 " + aa.getKey() + " 1 42.38 mt\n";
                    count++;
                    //write only the first 50 docs by rank order
                    if (count <= 50) {//todo cancel the "//"
                        bw.write(writeMe);
                    }
                }
                bw.flush();
                bw.close();
            //todo write it to the gui or something
            //todo insert into priority Q and every iteration at loop write to fileAt pathTo : queryID:docID1,docID2,....,docIDN. V
            //todo do something with the list because the next loop will override it. V
        }
      

    /**
     * if one of the paths isn't legal show alert and return false, else return true.
     *
     * @param pathFrom - path to take from
     * @param pathTo   - path to write result to
     * @return - true if both of the strings are legal paths, else false.
     */
    private boolean checkIfLegalPaths(String pathFrom, String pathTo) {
        File checkStop_Words = new File(pathFrom + "//stop_words.txt");
        if (!checkStop_Words.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path from");
            chooseFile.setContentText("The folder in the path from you selected does not contain a text file named stop_words, please choose a new path and try again. (:");
            chooseFile.show();
            return false;
        }
        //check the inserted path to.
        File checkPathTo = new File(pathTo);
        if (!checkPathTo.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path to");
            chooseFile.setContentText("The path you selected to save at is not legal or not a path of directory. please choose another one before use the commit button :)");
            chooseFile.show();
            return false;
        }
        return true;
    }

    public HashSet<String> setCities(String pathTo, boolean toStem) {
        return searcher.setCities(pathTo, toStem);
    }
  
    public HashMap<String,Double> getEntities(String docID, String pathTo, boolean toStem){
        return searcher.getEntities(docID,pathTo,toStem);
    }
}
