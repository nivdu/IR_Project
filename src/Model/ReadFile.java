package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

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
            if(f.isDirectory())
                addFilesPath(filePaths, f);
        }
        return filePaths;
    }

    /**
     * create arrayList of queries from given path of file
     * @return - array list of queries from the give file path
     */
    public ArrayList<Query> readQueryFile(String path) {
        ArrayList<Query> queriesFromFile = new ArrayList<>();
        String queryTitle = "";
        String queryNumber = "";
        String wholeQueryString = file2String(path);//gonna be the query path when init the readFile for searcher/query
        String[] queryByLines = wholeQueryString.split("\r\n|\\\n");
        boolean tookDesc = false;
        boolean finish = false;
        String desc = "";
        if (queryByLines != null && queryByLines.length > 0) {
            for (String line : queryByLines) {
                if (line.equals("") || line.equals("\n"))
                    continue;
                if (line.length() >= 14 && line.substring(0, 14).equals("<num> Number: ")) {
                    queryNumber = line.substring(14);//todo check if 14
                    queryNumber = queryNumber.replace("\n", "");
                    queryNumber = queryNumber.replace(" ", "");
                    continue;
                }
                if (line.length() >= 7 && line.substring(0, 7).equals("<title>")) {
                    queryTitle = line.substring(8);
                    queryTitle = queryTitle.replace("\n", "");
                    continue;
                }
                if(line.length() >= 19 && line.substring(0,19).equals("<desc> Description:")) {
                    tookDesc = true;
                    continue;
                }
                if(line.length() >= 17 && line.substring(0,17).equals("<narr> Narrative:")) {
                    tookDesc = false;
                    finish = true;
                }
                if(tookDesc&&!finish)
                    desc+=line.replace("\n"," ");
                if(finish) {
                    queriesFromFile.add(new Query(queryTitle, queryNumber, desc));
                    queryNumber = "";
                    desc="";
                    queryTitle="";
                    finish=false;
                    tookDesc=false;
                }
            }
        }
        return queriesFromFile;
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
        for (String currentDoc : splitByDoc) {
            if (!isDocLegal(currentDoc))
                continue;
            //add the city saved in tag fp=104 if there isn't a city add empty string
            String city = cutTheCityTag(currentDoc);
            if(city!=null && !city.equals("")){
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
            else
                    filePaths.add(fileFromF.getAbsolutePath());

        }
    }

    /**
     * Open file from the given path, split it by Docs tags, by Text tags and split by spaces and \n.
     * @param path - path of text file from the corpus.
     */
    public ArrayList<document> spiltFileIntoSeparateDocs2(String path) {
        ArrayList<document> docsFromFile = new ArrayList<>();
        String wholeFileString = file2String(path);
        //file split by <Doc>
        String[] splitByDoc = splitBySpecificString(wholeFileString, "<DOC>\n");
        document doc=null;
        for (String currentDoc : splitByDoc) {
            if (!isDocLegal(currentDoc))
                continue;
            doc = new document();
            //add the city saved in tag fp=104 if there isn't a city add empty string
            String city = cutTheCityTag(currentDoc);
            doc.setCity(city);
            //add the serial number of the current document
            String[] splitDocByDocID = currentDoc.split("<DOCNO>");
            if (splitDocByDocID.length < 2) {
                System.out.println("problem in spiltFileIntoSeparateDocs2 function readfile");
            }
            String[] splitByEndDocID = splitDocByDocID[1].split("</DOCNO>");
            String docIDWithoutSpaces = deletePunctutations(splitByEndDocID[0]);
            doc.setDocumentID(docIDWithoutSpaces);

            //add the date in tag <DATE1>
            String date = cutTheDate1Tag(currentDoc);
            doc.setPublishDate(date);

            //add the title in tag <TI>
            String title = cutTheTitleTag(currentDoc);
            String[] titleAtArr = title.split(" |\\\n|\\--|\\(|\\)|\\[|\\]|\\)|\\(|\\}|\\{|\\&|\\}|\\:|\\||\\?|\\!|\\}|\\_|\\@|\\'\'|\\;|\\\"");
            doc.setDocTitle(titleAtArr);

            String[] splitByText = splitBySpecificString(currentDoc, "<TEXT>\n");
            if (splitByText.length < 2)
                continue;
            String[] splitByEndText = splitBySpecificString(splitByText[1], "</TEXT>\n");
            String docSplitBySpaces[] = splitByEndText[0].split(" |\\\n|\\--|\\(|\\)|\\[|\\]|\\)|\\(|\\}|\\{|\\&|\\}|\\:|\\||\\?|\\!|\\}|\\_|\\@|\\'\'|\\;|\\\"");
            ArrayList<String> docSplit2Return = new ArrayList<>();
            String s2 = "";
            boolean containDot=false;
            boolean makaf=false;
            String [] numberWordArr;
            //change . and , into space if its not a number.
            for (String s : docSplitBySpaces) {
                //remove empty strings
                if(s.equals(""))
                    continue;
                //remove the tags
                if(s.length()>2 && s.charAt(0)=='<' && s.charAt(s.length()-1)=='>')
                    continue;
                //split by < and >
                if(s.contains("<") || s.contains(">")){
                    String[] splitByBigSmall = s.split("<|\\>");
                    for (String bigSmall:splitByBigSmall){
                        if(!bigSmall.equals(""))
                            docSplit2Return.add(bigSmall);
                    }
                    continue;
                }
                //law 3 of parsing split the numberword to number word
                numberWordArr = checkIfnumberWord(s);
                if(numberWordArr!=null) {
                    docSplit2Return.add(numberWordArr[0]);
                    docSplit2Return.add(numberWordArr[1]);
                    continue;
                }
                containDot=false;
                makaf=false;
                if ((s.contains(".") || s.contains(",") || s.contains("/")) && !checkIfOnlyDigitsDotsComma(s)) {
                    containDot=true;
                    for (int j = 0; j < s.length(); j++) {
                        char c = s.charAt(j);
                        if (c != '.' && c != ',' && c != '/')
                            s2 += c;
                        else if((j==0 || j==(s.length()-1) || ((j!=0 && !Character.isDigit(s.charAt(j-1))) || ((j<(s.length()-1)) && !Character.isDigit(s.charAt(j+1)))))){
                            if(!s2.equals(""))
                                docSplit2Return.add(s2);
                            s2 = "";
                        }
                        else {
                            s2 = s;
                            break;
                        }
                    }
                    if(!s2.equals("")) {
                        docSplit2Return.add(s2);
                        s2="";
                    }
                }
                else
                if (s.length()>1 && s.substring(1).contains("-")){
                    makaf = true;
                    for (int j = 0; j < s.length(); j++) {
                        char c = s.charAt(j);
                        if (c != '-')
                            s2 += c;
                        else {
                            docSplit2Return.add(s2);
                            s2 = "";
                        }
                    }
                    if(!s2.equals("")) {
                        docSplit2Return.add(s2);
                        s2="";
                    }
                    docSplit2Return.add(s);
                }
                if(!containDot && !makaf) {
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
                doc.setDocSplited(docSplitBySpaces);
            }
            docsFromFile.add(doc);
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


    private String[] checkIfnumberWord(String s) {//12,980m niv/loren
        boolean isNumberWord = false;
        boolean isNumberWord2 = false;
        String s2 = "";
        String s3 = "";
        int i = 0;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || (isNumberWord && (s.charAt(i)==',' || s.charAt(i)=='.' ||s.charAt(i)=='/'))) && !isNumberWord2) {
            s2 += s.charAt(i);
            isNumberWord = true;
            i++;
        }
        while (i < s.length() && Character.isLetter(s.charAt(i))) {
            s3 += s.charAt(i);
            isNumberWord2 = true;
            i++;
        }
        if (isNumberWord && isNumberWord2 && (s2.length()+s3.length())==s.length()) {
            String[] toReturn = new String[2];
            toReturn[0] = s2;
            toReturn[1] = s3;
            return toReturn;
        }
        return null;
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
        int i = 0;
        if(number==null || number.equals(""))
            return false;
        for (Character c : number.toCharArray()) {
            if (!Character.isDigit(c) && !(c.equals('.')) && !(c.equals('-')) && !(c.equals('/')) && !(c.equals(',')))
                return false;
            if (((c.equals('.')) || (c.equals('-') && i!=0) || (c.equals('/'))) && !legalNumber)
                return false;
            i++;
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


    private String cutTheLanguageTag(String currentDoc){
        String[] splitByLanguage = currentDoc.split("<F P=105>");
        //if there isn't language in doc
        if (splitByLanguage.length == 1)
            return "";
            //if there is a languagein doc
        else {
            String[] splitByEndLanguage = splitByLanguage[1].split("</F>");
            if(splitByEndLanguage[0].equals(""))
                return "";
            String language = splitByEndLanguage[0];
            language = deletePunctutations(language);
            return language;
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

    public HashSet<String> getLanguages(String path) {
        HashSet<String> languages = new HashSet<>();
        String wholeFileString = file2String(path);
        String[] splitByDoc = splitBySpecificString(wholeFileString, "<DOC>\n");
        for (String currentDoc : splitByDoc) {
            if (!isDocLegal(currentDoc))
                continue;
            //add the city saved in tag fp=104 if there isn't a city add empty string
            String language = cutTheLanguageTag(currentDoc);
            if(language!=null && !language.equals("") &&!isLegalNumber(language)){
                languages.add(language.toLowerCase());
            }
        }
        return languages;
    }

    private boolean isLegalNumber(String language) {
        if(language==null || language.equals(""))
            return false;
        for (int i = 0; i < language.length(); i++) {
            if(Character.isDigit(language.charAt(i)))
                return true;
        }
        return false;
    }
}

