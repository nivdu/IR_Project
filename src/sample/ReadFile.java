package sample;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReadFile {
    Parse parse;

    /**
     *
     * @param path - Path of main directory
     */
    public void SpiltFileIntoSeparateDocs(String path){
        final File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            LinkedList<String> docsInFile;
            for (final File fileOfDocs: fileEntry.listFiles()){
                docsInFile = splitFileByDocs(fileOfDocs);
                parse.parseDoc(docsInFile);
            }
        }
    }

    /**
     *
     * @param splitMe - File to split into separate docs.
     * @return - LinkedList of docs
     */
    private LinkedList<String> splitFileByDocs(File splitMe) {
        LinkedList<String> docsInFile = new LinkedList<String>();
        //get into the File and split by docs.
        return docsInFile;
    }
}
