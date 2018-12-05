package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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
     * @return - array list of Strings, each String is a path to Text File
     */
    public ArrayList<String> readCorpus(){
        ArrayList<String> filePaths = new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] corpusFiles = corpusFolder.listFiles();
        for (File f:corpusFiles){
            addFilesPath(filePaths, f);
        }
        return filePaths;
    }

    /**
     * make a hashset of strings contain all the city first word found after the tag<F P=104> at the current
     * file in the given path(save the cities names as lower case).
     * @param path - path of the doc to cut the city tag from
     * @return - HashSet of string contain all the first word of city names found in the tag <F P=104>
     */
    public HashSet<String> generateSetOfCities(String path) {
        HashSet<String> citiesFromTags = new HashSet<>();
        //convert the text file into one string
        String wholeFileString = file2String(path);
        String[] splitByDoc = splitBySpecificString(wholeFileString, "<DOC>\n");
        //file split by <TEXT> //todo need to delete the <</TEXT> at parse.
        for (String currentDoc : splitByDoc) {
            if (!isDocLegal(currentDoc))
                continue;
            //add the city saved in tag fp=104 if there isn't a city add empty string
            String city = cutTheCityTag(currentDoc);
            if(city!=null && !city.equals("")){
                if(city.charAt(0)=='<')
                    System.out.println("loren");
                citiesFromTags.add(city.toLowerCase());
            }
        }
        return citiesFromTags;
    }
    /*
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
            //add the city saved in tag fp=104 if there isn't a city add empty string
            String city = cutTheCityTag(currentDoc);
            String[] cityAtArr = {city};
            docsFromFile.add(cityAtArr);
            //add the serial number of the current document
            String[] splitDocByDocID = currentDoc.split("<DOCNO>");
            if (splitDocByDocID.length < 2) {
                System.out.println("problem in spiltFileIntoSeparateDocs2 function readfile");//todo delete it
            }
            String[] splitByEndDocID = splitDocByDocID[1].split("</DOCNO>");
            String docIDWithoutSpaces = deletePunctutations(splitByEndDocID[0]);
            String[] docNO = new String[1];
            docNO[0] = docIDWithoutSpaces;
            docsFromFile.add(docNO);

            //add the date in tag <DATE1>
            String date = cutTheDate1Tag(currentDoc);
            String[] dateAtArr = {date};
            docsFromFile.add(dateAtArr);

            //add the title in tag <TI>
            String title = cutTheTitleTag(currentDoc);
            String[] titleAtArr = {title};
            docsFromFile.add(titleAtArr);

            String[] splitByText = splitBySpecificString(currentDoc, "<TEXT>\n");
            if (splitByText.length < 2)
            {
                String[] splitBySpace = new String[1];
                splitBySpace[0]="";
                docsFromFile.add(splitBySpace);
                continue;
            }
            String[] splitByEndText = splitBySpecificString(splitByText[1], "</TEXT>\n");
            String docSplitBySpaces[] = splitByEndText[0].split(" |\\\n|\\--|\\(|\\)|\\[|\\]|\\)|\\(|\\}|\\{|\\&|\\}|\\:|\\||\\<|\\>|\\?|\\!|\\}|\\_|\\@|\\'\'|\\;");
            ArrayList<String> docSplit2Return = new ArrayList<>();
            String s2 = "";
            boolean containDot=false;
            //change . and , into space if its not a number.
            for (String s : docSplitBySpaces) {
                containDot=false;
                if ((s.contains(".") || s.contains(",") || s.contains("/")) && !checkIfOnlyDigitsDotsComma(s)) {
                    containDot=true;
                    for (int j = 0; j < s.length(); j++) {
                        char c = s.charAt(j);
                        if (c != '.' && c != ',' && c != '/')
                            s2 += c;
                        else {
                            docSplit2Return.add(s2);
                            s2 = "";
                        }
                    }
                    if(!s2.equals(""))
                        docSplit2Return.add(s2);
                }
                if(!containDot) {
                    if (s.equals("") || s.equals(" ") || s.equals("\n"))
                        continue;
                    docSplit2Return.add(s);
                }
            }
            if(docSplit2Return.size()>0) {
                docSplitBySpaces = new String[docSplit2Return.size()];
                int i = 0;
                for (String s3 : docSplit2Return) {
                    docSplitBySpaces[i] = s3;
                    i++;
                }
                docsFromFile.add(docSplitBySpaces);
            }
            else{
                String[] s=new String[1];
                s[0]="";
                docsFromFile.add(s);
            }
        }
        return docsFromFile;
    }


    /**
     * cut the date that is in the tag <TI> </TI>
     * @param currentDoc - the document which we will cut the title from
     * @return string of the title
     */
    private String cutTheTitleTag(String currentDoc) {
        if(!isDocLegal(currentDoc))
            return "";
        String title="";
        String[] docSplitByTitle = currentDoc.split("<TI>");
        if(docSplitByTitle==null || docSplitByTitle.length<=1 || (docSplitByTitle.length>1 && docSplitByTitle[1].equals("")))
            return "";
        String[] docSplitByEndTitle = docSplitByTitle[1].split("</TI>");
        if(docSplitByEndTitle==null || docSplitByEndTitle.length==1 || docSplitByEndTitle[0].equals(""))
            return "";
        else title = deletePunctutations(docSplitByEndTitle[0]);
        return title;
    }


    /**
     * cut the date that is in the tag <DATE1> </DATE1>
     * @param currentDoc - the document which we will cut the date from
     * @return string of the date
     */
    private String cutTheDate1Tag(String currentDoc) {
        if(!isDocLegal(currentDoc))
            return "";
        String date="";
        String[] docSplitByDate = currentDoc.split("<DATE1>");
        if(docSplitByDate==null || docSplitByDate.length<=1 || (docSplitByDate.length>1 && docSplitByDate[1].equals("")))
            return "";
        String[] docSplitByEndDate = docSplitByDate[1].split("</DATE1>");
        if(docSplitByEndDate==null || docSplitByEndDate.length==1 || docSplitByEndDate[0].equals(""))
            return "";
        else date = deletePunctutations(docSplitByEndDate[0]);
        return date;
    }

    //check if a number combine only from digits, dots, commas and slash only.
    private boolean checkIfOnlyDigitsDotsComma(String number) {
        int dot=0,slash = 0,minus=0,comma=0,digits=0;
        boolean legalNumber = false;
        if(number==null || number.equals(""))
            return false;
        for (Character c : number.toCharArray()) {
            if (!Character.isDigit(c) && !(c.equals('.')) && !(c.equals('-')) && !(c.equals('/')) && !(c.equals(',')))
                return false;
            if (((c.equals('.')) || (c.equals('-')) || (c.equals('/'))) && !legalNumber)
                return false;
            if (Character.isDigit(c)) {
                legalNumber = true;
                digits++;
            }
            else if(c=='.') {
                dot++;
                legalNumber=false;
            }
            else if (c=='/') {
                slash++;
                legalNumber=false;
            }
            else if (c=='-') {
                minus++;
                legalNumber=false;
            }
            else if (c==',') {
                comma++;
                legalNumber=false;
            }
            if(dot>1 || slash>1 || minus>1)
                return false;
        }
        if(comma>0 && digits/comma<3)
            return false;
        if(!legalNumber)
            return false;
        return true;
    }

    /**
     * cut the first word of the city after <F P=104>.
     * @param currentDoc - document to cut from
     * @return - the first word of the city after the tag <F P=104> from the given doc, if there isn't a city or a tag return empty string
     */
    private String cutTheCityTag(String currentDoc){
        String[] splitByCity = currentDoc.split("<F P=104>");
        //if there isn't city in doc
        if (splitByCity.length == 1)
            return "";
            //if there is a city in doc
        else {
            String[] splitByEndCity = splitByCity[1].split("</F>");
            if(splitByEndCity[0].equals(""))
                return "";
            String city = splitByEndCity[0];
            city = deletePunctutations(city);
            String[] splitCityBySpaces = city.split(" ");
            //if city contains mor than one word
            if (splitCityBySpaces.length > 1) {
                return deletePunctutations(splitCityBySpaces[0]).toUpperCase();
            }
            //if city contains exactly one word
            else if (splitCityBySpaces.length == 1) {
                return deletePunctutations(splitCityBySpaces[0]);
            } else return "";
        }
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
    private String deletePunctutations(String string) {
        if(string==null || string.equals(""))
            return "";
        while(string.length()>0 && ((string.charAt(0)>=0 && string.charAt(0)<=47) || (string.charAt(0)>=58 && string.charAt(0)<=64) || (string.charAt(0)>=91 && string.charAt(0)<=96) || (string.charAt(0)>=123 && string.charAt(0)<=127) || (!(string.charAt(0)>=0 && string.charAt(0)<=127)))){
            string = string.substring(1);
        }
        while (string.length() >0 && ((string.charAt(string.length()-1)>=0 && string.charAt(string.length()-1)<=47) || (string.charAt(string.length()-1)>=58 && string.charAt(string.length()-1)<=64) || (string.charAt(string.length()-1)>=91 && string.charAt(string.length()-1)<=96) || (string.charAt(string.length()-1)>=123 && string.charAt(string.length()-1)<=127) || (!(string.charAt(string.length()-1)>=0 && string.charAt(string.length()-1)<=127)))){
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
            try { content = new String ( Files.readAllBytes( Paths.get(path) ) ); } catch (IOException e) {}
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

