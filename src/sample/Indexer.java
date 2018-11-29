package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Indexer {

    //list of all the documents
    ArrayList<document> docList;
    //readFile class
    ReadFile readFile;
    //parse Class
    Parse parse;
    //dictionary contain all the terms from the docs
    HashSet<String> dictionary;
    //dictionary contain all the terms from the docs -  term ; tf,posting
    private HashMap<String,String[]> dictionaryPosting;

    /**
     * Constructor
     */
    Indexer(ReadFile rf, Parse p) {
        readFile = rf;
        parse = p;
        docList = new ArrayList<>();
        dictionaryPosting=new HashMap<>();
    }


    public void createPostingAndDic(String corpusPath) {
        ArrayList<String> allFilesPaths = readFile.readCorpus(corpusPath);
        for (String path : allFilesPaths) {
            parseFile(readFile.spiltFileIntoSeparateDocs2(path));
            //todo separate the for contents into another function and run it by multi threads
        }
    }


    public void parseFile(ArrayList<String[]> filesToParse) {
        String city = "";
        String docId = "";
        int index = 0;
        for (String[] currentDoc : filesToParse) {
            if (currentDoc != null && currentDoc.length > 0) {
//                if (index % 3 == 0)
//                    city = currentDoc[0];
//                else if (index % 3 == 1)
//                    docId = currentDoc[0];
//                else {
                    document currDoc = parse.parseDoc(currentDoc, city, docId);
//                    combineDicDocAndDictionary(currDoc);
                    currDoc.removeDic();
                    docList.add(currDoc);
//                }
                index++;
            }
        }
        filesToParse.clear();
    }

    /**
     * combine the dictionary of document with the general dictionary
     * @param currDoc - the document which his dictionary will be combined
     */
    private void combineDicDocAndDictionary(document currDoc) {
        HashMap<String,Integer> dicDoc = currDoc.getDicDoc();
        Set<String> keys = dicDoc.keySet();
        for(String term: keys){
            //term already exists in dictionary
            if(dictionaryPosting.containsKey(term)){
                String[] dfPosting = dictionaryPosting.get(term);
                int df = Integer.parseInt(dfPosting[0]);
                dfPosting[0]=""+(df+1);
                dfPosting[1]+=";"+currDoc.getDocumentID()+","+dicDoc.get(term);
                dictionaryPosting.put(term,dfPosting);
            }
            //term doesn't exist in dictionary
            else{
                String[] dfPosting = new String[2];
                dfPosting[0]="1";
                dfPosting[1]=currDoc.getDocumentID()+","+dicDoc.get(term);
                dictionaryPosting.put(term,dfPosting);
            }
        }
    }
}