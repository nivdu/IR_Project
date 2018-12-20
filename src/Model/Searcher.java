package Model;

import java.io.*;
import java.util.*;

public class Searcher {
    private Parse parse;
    private HashMap<String,String[]> dictionaryPosting; //df,tf_overall,pointerToPosting


    public Searcher(Parse parse){
        this.parse = parse;
        this.dictionaryPosting=new HashMap<>();
    }

    public List<String[]> runQuery(String query, boolean toStem, String pathTo) {
        Query queryDoc = new Query(query,"111");
        ArrayList<QueryWord> listOfWords = new ArrayList<>();
        queryDoc.setQuerySplited(null);//todo
        HashMap<String,int[]> queryTermsTF = parse.parseMainFunc(null,queryDoc);
        HashSet<String> allRelevantDocsInPosting = new HashSet<>();
        boolean isLoad = loadDictionaryFromDisk(toStem,pathTo);
        if(!isLoad)
            System.out.println("problem");
        String pathToCreate;
        if (toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        } else pathToCreate = pathTo + "\\WithoutStemming";
        File filePosting = new File(pathToCreate + "/Postings/unitedPosting.txt");
        if(!filePosting.exists())
            System.out.println("problem");
        Set<String> keys = queryTermsTF.keySet();
        //foreach word in query
        for (String term : keys) {
            String[] dfTfPointer = dictionaryPosting.get(term);
            String pointerToPosting = dfTfPointer[2];
            long pointer = Long.valueOf(pointerToPosting).longValue();
            String line = "";
            try {
                RandomAccessFile raf = new RandomAccessFile(pathToCreate + "/Postings/unitedPosting.txt","rw");
                raf.seek(pointer);
                String linePosting = raf.readLine();
                String[] lineSplitedByTerm= linePosting.split(":");
                String[] docsSplitedInLine = lineSplitedByTerm[1].split(";");
                HashMap<String,int[]> docsOfWord = new HashMap<>();
                for (String doc:docsSplitedInLine) {
                    String[] docIDTFTitle = doc.split(",");
                    if(docIDTFTitle.length<3)
                        System.out.println("problem");
                    int[] tfTitle = {Integer.parseInt(docIDTFTitle[1]),Integer.parseInt(docIDTFTitle[2])};
                    docsOfWord.put(docIDTFTitle[0],tfTitle);//docID,TF,Title
                    //Insert docID to HashSet
                    allRelevantDocsInPosting.add(docIDTFTitle[0]);
                }
                QueryWord queryWord = new QueryWord(term,docsOfWord,queryTermsTF.get(term)[0],Integer.parseInt(dfTfPointer[0]));
                listOfWords.add(queryWord);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        //todo call to ranker function with arraylist and hashset
        return null;
    }

    public boolean loadDictionaryFromDisk(boolean toStem,String pathTo) {
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
}
