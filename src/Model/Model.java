package Model;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;

public class Model {
    private Indexer indexer;
    private Searcher searcher;
    private boolean toStem;
    private Parse parse;
    private Ranker ranker;


    /**
     *constructor
     */
    public Model(){
        this.toStem=false;
    }

    /**
     * creating the dictionary and the posting of the inverted index
     */

    public boolean generateInvertedIndex(String pathFrom,String pathTo,boolean toStem){
        //check the inserted path from.
        if(!checkIfLegalPaths(pathFrom,pathTo))
            return false;
        parse = new Parse(toStem, pathFrom);
        indexer = new Indexer(pathFrom,pathTo, parse);
        long Stime = System.currentTimeMillis();
        boolean succGenerate=indexer.createPostingAndDic(toStem);
        long Ftime = System.currentTimeMillis();
        Alert alert;
            if(succGenerate){
            long indexRunTime = Ftime-Stime;
            int indexedDocNumber = indexer.getIndexedDocNumber();
            int uniqueTermsNumber = indexer.getUniqueTermsNumber();
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Commit succeed!");
            alert.setContentText("runtime by seconds: " + indexRunTime/1000 + "\n" +
                    "number Indexed docs: " + indexedDocNumber + "\n" +
                    "number unique Terms: " + uniqueTermsNumber);
          alert.show();
          return true;
        }
        else{
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Commit");
            alert.setContentText("Commit Failed!");
            alert.show();
            return false;
        }
    }

    public void showWhenFinishIndexing(long indexRunTime){
        int indexedDocNumber = indexer.getIndexedDocNumber();
        int uniqueTermsNumber = indexer.getUniqueTermsNumber();
        System.out.println("runtime by seconds: " + indexRunTime/1000);
        System.out.println("number Indexed docs: " + indexedDocNumber);
        System.out.println("number unique Terms: " + uniqueTermsNumber);
    }
    /**
     * reset the posting ,the dictionary file and the memory of the program
     * @return true if the reset succeed' else return false
     */
    public boolean reset(){
        return indexer.reset();
    }


    /**
     * take the dictionary of the terms from the indexer : term, tf overall
     * @return the dictionary of the terms
     */
    public List<String> displayDictionary(boolean isStem, String pathTo){
        File checkPathTo = new File(pathTo);
        if(!checkPathTo.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("Error with path to");
            chooseFile.setContentText("The path you selected is not legal or not a path of directory. please choose another one. :)");
            chooseFile.show();
            return null;
        }
        else{
            if(isStem){
                File withStem = new File(pathTo + "\\withStemming");
                if(!withStem.exists()){
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withStemming directory!");
                    chooseFile.show();
                    return null;
                }
            }
            else{
                File withoutStem = new File(pathTo + "\\withoutStemming");
                if(!withoutStem.exists()){
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("Error with Stemming");
                    chooseFile.setContentText("The path you selected does not contain the withoutStemming directory!");
                    chooseFile.show();
                    return null;
                }
            }
        }
        List<String> dictionary = indexer.displayDictionary(isStem);
        if(dictionary == null){
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
     * @return true if the loading succeed, else retutn false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean isStem, String pathTo){
        if(!checkIfDirectoryWithOrWithoutStemExist(isStem, pathTo))
            return false;
        boolean bl=indexer.loadDictionaryFromDiskToMemory(isStem);
        if(bl) return true;
        else{
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

    public HashSet<String> languages(){
        return indexer.languages();
    }

    /**
     * can call only after inverted index is created//todo disable this functions (buttons) untill create inverted index.
     * @param query
     * @return
     */
    public boolean runQuery(String query,boolean toStem, String pathTo, String pathFrom, List<String> citiesChosen, boolean semantic){
//        runQueryFile("", pathFrom,pathTo);
//        int numberOfDocsAtCorpus = indexer.getIndexedDocNumber();
        //check the inserted path from.
        if(!checkIfLegalPaths(pathFrom,pathTo))
            return false;
        if(!checkIfDirectoryWithOrWithoutStemExist(toStem, pathTo))
            return false;
        Parse parse1 = new Parse(toStem, pathFrom);
        HashMap<String,document> docsHash = new HashMap<>();
        try {
            FileInputStream fis;
            if(toStem)
                fis = new FileInputStream(pathTo + "/WithStemming/docsData.txt");
            else
                fis = new FileInputStream(pathTo + "/WithoutStemming/docsData.txt");
            ObjectInputStream objIS = new ObjectInputStream(fis);
            docsHash =(HashMap) objIS.readObject();
            objIS.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (IOException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (ClassNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        }
        ranker = new Ranker(docsHash);
        searcher = new Searcher(parse1, ranker);
        //if semantic checkBox have V.
        if(semantic){
            //call to api
            String[] queryWords = query.split(" ");
            LinkedList<String> queryListWords = new LinkedList<>();
            if(queryWords.length>0) {
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
                                while (s != null && i<s.length() && s.charAt(i) != '\"') {
                                    if(Character.isLetter(s.charAt(i)))
                                        currWord += s.charAt(i);
                                    i++;
                                }
                                if (currWord != null && currWord.length() > 0) {
                                    queryListWords.add(currWord);
                                    if(queryListWords.size()%2==0)
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
            for (String s:queryListWords){
                query = query + " " + s;
            }
        }
        Query currQuery = new Query(query, "111", null);
        List<String[]> list = searcher.runQuery(currQuery, toStem, pathTo,null);//todo maybe object of queryAns
        return false;
    }

    public boolean runQueryFile(String pathQueryFile, String pathFrom, String pathTo){
//        pathQueryFile = "C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל\\מנוע חלק ב" + "\\queries.txt";//todo need to take from the user
        if(!checkIfLegalPaths(pathFrom,pathTo))
            return false;
        if(!checkIfDirectoryWithOrWithoutStemExist(toStem, pathTo))
            return false;
        ReadFile readfile = new ReadFile(pathQueryFile);
            ArrayList<Query> queriesArr = readfile.readQueryFile(pathQueryFile);
        Parse parse1 = new Parse(toStem, pathFrom);
        HashMap<String,document> docsHash = new HashMap<>();
        try {
            FileInputStream fis;
            if(toStem)
                fis = new FileInputStream(pathTo + "/WithStemming/docsData.txt");
            else
                fis = new FileInputStream(pathTo + "/WithoutStemming/docsData.txt");
            ObjectInputStream objIS = new ObjectInputStream(fis);
            docsHash =(HashMap) objIS.readObject();
            objIS.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (IOException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        } catch (ClassNotFoundException e) {
            System.out.println("problem in writeDocsDataToDisk function (Model");
        }
        ranker = new Ranker(docsHash);
        searcher = new Searcher(parse1, ranker);
        for (Query query:queriesArr){
            List<String[]> list = searcher.runQuery(query, toStem, pathTo,null);//todo maybe object of queryAns
            //todo insert into priority Q and every iteration at loop write to fileAt pathTo : queryID:docID1,docID2,....,docIDN
            //todo do something with the list because the next loop will override it.
        }
        return false;//todo
    }

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

    public HashSet<String> setCities() {
        return searcher.setCities();
    }

    public HashMap<String,Double> getEntities(String docID, String pathTo, boolean toStem){
        return ranker.getEntities(docID,pathTo,toStem);
    }

}
