package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ReadFile {

    private String corpusPath;

    /**
     * Constructor
     * @param corpusPath - path of the corpus file
     */
    public ReadFile(String corpusPath) {
        this.corpusPath=corpusPath;
    }

    /**
     * Read All file from corpus and return path to each file isn't a directory
     * @param corpusPath - path of the corpus directory
     * @return - array list of Strings, each String is a path to Text File
     */
    public ArrayList<String> readCorpus(String corpusPath){
        ArrayList<String> filePaths = new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] corpusFiles = corpusFolder.listFiles();
        for (File f:corpusFiles){
            addFilesPath(filePaths, f);
        }
        return filePaths;
    }

    /**
     * Called from function "readCorpus". Insert file paths into array list from the given file, if the file is directory, open it and take the paths of the files inside it.
     * @param filePaths - current file/directory path
     */
    private void addFilesPath(ArrayList<String> filePaths, File f){
        if(filePaths==null || f==null)
            return;
        for (File fileFromF:f.listFiles()){
            if(fileFromF.isDirectory())
                addFilesPath(filePaths, fileFromF);
            else filePaths.add(fileFromF.getAbsolutePath());
        }
    }

  /*
    public void SpiltFileIntoSeparateDocs(){
        long Stime = System.currentTimeMillis();
        final File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            for (final File fileOfDocs: fileEntry.listFiles()){
                String wholeFileString = file2String(fileOfDocs);
                //file split by <Doc>
                String[] splitByDoc = splitBySpecificString(wholeFileString,"<DOC>\n");
                //file split by <TEXT> //todo need to delete the <</TEXT> at parse.
                for (String currentDoc:splitByDoc){
                    if(currentDoc.equals(""))
                        continue;
                    String[] splitByText = splitBySpecificString(currentDoc,"<TEXT>\n");
                    if(splitByText.length<2)
                        continue;
                        String[] splitByEndText = splitBySpecificString(splitByText[1], "</TEXT>\n");
                        String docSplitBySpaces[] = splitByEndText[0].split(" |\\\n|\\--");
                        parse.parseDoc(docSplitBySpaces);
                }
            }
        }
        long Ftime = System.currentTimeMillis();
        System.out.println(Ftime-Stime);
        int temp=-10;
    }
*/
    /**
     * Open file from the given path, split it by Docs tags, by Text tags and split by spaces and \n.
     * @param path - path of text file from the corpus.
     */
    public ArrayList<String[]> spiltFileIntoSeparateDocs2(String path) {
        ArrayList<String[]> docsFromFile = new ArrayList<>();
        String wholeFileString = file2String(path);
        //file split by <Doc>
        String[] splitByDoc = splitBySpecificString(wholeFileString, "<DOC>\n");
        //file split by <TEXT> //todo need to delete the <</TEXT> at parse.
        for (String currentDoc : splitByDoc) {
            if (!isDocLegal(currentDoc))
                continue;
            String[] splitByCity = currentDoc.split("<F P=104>");
            //if there isn't city in doc
            if(splitByCity.length==1){
                String[] empty = new String[1];
                empty[0]="";
                docsFromFile.add(empty);
            }
            //if there is a city in doc
            else{
                String[] splitByEndCity = splitByCity[1].split("</F>");
                String city = splitByCity[0];
                city = deleteSpaces(city);
                String[] splitCityBySpaces=city.split(" ");
                //if city contains mor than one word
                if(splitCityBySpaces.length>1){
                    String[] cityUpperCase=new String[1];
                    cityUpperCase[0]=splitCityBySpaces[0].toUpperCase();
                    docsFromFile.add(cityUpperCase);
                }
                //if city contains exactly one word
                else if(splitCityBySpaces.length==1){
                    docsFromFile.add(splitByCity);
                }
                else{
                    String[] empty = new String[1];
                    empty[0]="";
                    docsFromFile.add(empty);
                }
            }
            String[] splitDocByDocID = currentDoc.split("<DOCNO>");
            if(splitDocByDocID.length<2) {
                System.out.println("problem in spiltFileIntoSeparateDocs2 function readfile");//todo delete it
            }
            String[] splitByEndDocID = splitDocByDocID[1].split("</DOCNO>");
            String docIDWithoutSpaces = deleteSpaces(splitByEndDocID[0]);
            String[] docNO = new String[1];
            docNO[0] = docIDWithoutSpaces;
            docsFromFile.add(docNO);
            String[] splitByText = splitBySpecificString(currentDoc, "<TEXT>\n");
            if (splitByText.length < 2)
                continue;
            String[] splitByEndText = splitBySpecificString(splitByText[1], "</TEXT>\n");
            String docSplitBySpaces[] = splitByEndText[0].split(" |\\\n|\\--");
            docsFromFile.add(docSplitBySpaces);
        }
        return docsFromFile;
    }

    /**
     * check whether a string is legale doc
     * @param doc - doc to check
     * @return - true if legal else false.
     */
    private boolean isDocLegal(String doc){
        if(doc==null||doc.equals("") || doc.length()<5)
            return false;
        for (int i = 0; i < doc.length(); i++) {
            if(doc.charAt(i)!='\n')
                return true;
        }
        return true;
    }

    /**
     * Delete spacecs from string at the beginning and the end.
     * @param string - string to delete spaces from
     * @return - string without spaces.
     */
    private String deleteSpaces(String string) {
        while(string.charAt(0)==' '){
            string = string.substring(1);
        }
        while (string.charAt(string.length()-1)==' '){
            string = string.substring(0,string.length()-1);
        }
        return string;
    }

        /**
         * from url: https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
         * read file to String line by line
         * @param path - The path of the file to read into String
         * @return - String of the context of the file
         */
    private String file2String(String path)
    {
        StringBuilder contentBuilder = new StringBuilder();
        {
            String content = "";

            try
            {
                content = new String ( Files.readAllBytes( Paths.get(path) ) );
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return content;
        }
    }

    /**
     * split file by specificString
     * @param file2Split - file to split
     * @return - String array of the split text file
     */
    private String[] splitBySpecificString(String file2Split,String splitBy){
        String[] docs = file2Split.split(splitBy);
        return docs;
    }
}
