package sample;

import java.io.File;
import java.util.ArrayList;

public class Indexer {

    ArrayList<document> docList;
    ReadFile readFile;
    Parse parse;

    /**
     * Constructor
     */
    Indexer(ReadFile rf, Parse p) {
        readFile = rf;
        parse = p;
        docList = new ArrayList<>();
    }


    public void createPostingAndDic(String corpusPath, boolean useStem, String stopWordsPath) {
        ArrayList<String> allFilesPaths = readFile.readCorpus(corpusPath);
        for (String path : allFilesPaths) {
            parseFile(readFile.spiltFileIntoSeparateDocs2(path));
            //todo separate the for contents into another function and run it by multi threads
        }
    }


    public void parseFile(ArrayList<String[]> filesToParse) {
        for (String[] currentDoc : filesToParse) {
            document currDoc = parse.parseDoc(currentDoc);
            docList.add(currDoc);
        }
    }
}