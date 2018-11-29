package sample;

import org.omg.SendingContext.RunTime;

import java.io.*;
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
                if (index % 3 == 0)
                    city = currentDoc[0];
                else if (index % 3 == 1)
                    docId = currentDoc[0];
                else {
                    document currDoc = parse.parseDoc(currentDoc, city, docId);
                    Long freeMemory = Runtime.getRuntime().freeMemory();
                    Long totalMemory = Runtime.getRuntime().totalMemory();
                    double minMemory = totalMemory*0.3;
                    if(freeMemory<=minMemory)//todo check
                        saveAndDeletePosition(dictionaryPosting);
                    combineDicDocAndDictionary(currDoc);
                    docList.add(currDoc);
                    currDoc.removeDic();
                }
                index++;
            }
        }
        filesToParse.clear();
    }

    private void saveAndDeletePosition(HashMap<String,String[]> dictionaryPosting) {
        int number = 0;
        copyPostingIntoArrayList(dictionaryPosting);
        try {
        File f = new File("resources/posting"+ number +".txt");//todo counter for number
        if(!f.exists()) {
                f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write("loren");
            fw.flush();
            fw.close();
        }}
        catch (IOException e) {
            System.out.println("problem in saveanddeletefunction indexer");//todo delete this
            e.printStackTrace();
        }


        /*byte[] data = "loran atafa".getBytes();
        int number =0;

        OutputStream out = null;
        try {
            out = new FileOutputStream(new File("resources/posting" +number+ ".txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        //        try {
//            File tempFile = File.createTempFile("post" + number,"txt",new File("C:\\Users\\nivdu\\Documents\\GitHub\\IR_Project\\resources"));
//            System.out.println(tempFile);//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        int temp=0;
    }

    private void copyPostingIntoArrayList(HashMap<String,String[]> dictionaryPosting) {
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