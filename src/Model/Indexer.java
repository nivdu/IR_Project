package Model;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Indexer {

    //list of all the documents
    private ArrayList<document> docList;
    //readFile class
    private ReadFile readFile;
    //parse Class
    private Parse parse;
    //dictionary contain all the terms from the docs
    private HashSet<String> dictionary;
    //dictionary contain all the terms from the docs -  term ; tf,posting
    private HashMap<String, String[]> dictionaryPosting;
    private int tempPostingCounter;
    private int filesPostedCounter;
    private String pathFrom;
    private String pathTo;


    /**
     * Constructor
     */
    Indexer(String pathFrom, String pathTo ,boolean toStem) {
        this.filesPostedCounter = 0;
        readFile = new ReadFile(pathFrom);
        parse = new Parse(toStem,pathFrom);
        docList = new ArrayList<>();
        dictionaryPosting = new HashMap<>();
        tempPostingCounter = 0;
        this.pathFrom = pathFrom;
        this.pathTo = pathTo;
    }


    public boolean createPostingAndDic(boolean toStem) {
        String pathToCreate ="";
        if(toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        }
        else pathToCreate =pathTo + "\\WithoutStemming";
        File theDir = new File(pathToCreate);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
                ArrayList<String> allFilesPaths = readFile.readCorpus(pathFrom + "\\corpus");//todo check
                for (String path : allFilesPaths) {
                    parseFile(readFile.spiltFileIntoSeparateDocs2(path),pathToCreate);
                }
                unitAllTempPostingsToOnePostingInDisk(pathToCreate+"/Postings");
                movingDictionaryToDisk(pathToCreate);
            } catch (SecurityException se) {
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void movingDictionaryToDisk(String pathToCreate) {
        File dicOfDictionaries = new File(pathToCreate + "/Dictionaries");
        if(!dicOfDictionaries.exists())
            dicOfDictionaries.mkdir();
        File fileOfDictionary = new File(pathToCreate + "/Dictionaries/Dictionary.txt");
        if(!fileOfDictionary.exists()) {
            try {
                fileOfDictionary.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PriorityQueue<String> dictionarySorted = copyDicIntoPQByAscii();
            FileWriter fw = new FileWriter(fileOfDictionary);
            Set<String> keys = dictionaryPosting.keySet();
            for(String term: keys){
                if(fw==null) break;
                fw.write(dictionarySorted.remove());
            }
            fw.flush();
            fw.close();
        } catch (IOException e) { e.printStackTrace(); }
    }


    class compareLexicographically2 implements Comparator<String[]>{
        // Overriding compare()method of Comparator
        // compare string lexicographically.
        public int compare(String[] s1, String[] s2) {
            return compareLexicographically.compare(s1[0],s2[0]);
        }
    }

    /**
     * cut the term from string (cut the chars before the first ":" and return it by its lower case
     * @param s - string to cut the term from
     * @return - String of the term (lower case of the term).
     */
    private String cutTheTerm(String s){
        String term = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c!=':')
                term += c;
            else break;
        }
        return term.toLowerCase();
    }

    public void parseFile(ArrayList<String[]> filesToParse, String pathToCreate) {
        String city = "";
        String docId = "";
        int index = 0;
        filesPostedCounter++;
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
                    double minMemory = totalMemory*0.1;
//                    if(freeMemory<=minMemory){//todo check
                    if(filesPostedCounter==40){
                        filesPostedCounter=0;
                        saveAndDeletePosition(dictionaryPosting,pathToCreate);
                    }
                    combineDicDocAndDictionary(currDoc);
                    docList.add(currDoc);
                    currDoc.removeDic();
                }
                index++;
            }
        }
        filesToParse.clear();
//        unitAllTempPostingsToOnePostingInDisk();
    }

    private void saveAndDeletePosition(HashMap<String,String[]> dictionaryPosting, String pathToCreate) {
        try {
            File dirOfPostings = new File(pathToCreate + "\\Postings");//todo counter for number
            if (!dirOfPostings.exists())
                dirOfPostings.mkdir();
            File f = new File(pathToCreate + "\\Postings\\posting" + tempPostingCounter + ".txt");//todo counter for number
            if (!f.exists())
                f.createNewFile();
            FileWriter fw = new FileWriter(f);
            PriorityQueue<String> tempPosting = copyPostingIntoString(dictionaryPosting);
            int PQsize= tempPosting.size();
            for (int i = 0; i < PQsize; i++) {
                fw.write(tempPosting.remove());
            }
            fw.flush();
            fw.close();
            tempPostingCounter++;
        }
        catch (IOException e) {
            System.out.println("problem in saveanddeletefunction indexer");//todo delete this
            e.printStackTrace();
        }
    }

    Comparator<String> compareLexicographically = new Comparator<String>() {
        //letters first (ignoring differences in case), digits are second and marks (like &%^*%) are the last in priority.
        @Override
        public int compare(String s1, String s2) {
            if (s1 == null || s1.equals("") || s2 == null || s2.equals(""))//todo check better
                return 0;
            for (int i = 0; i < s1.length() && i < s2.length(); i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                int n1 = letterIsBetter(c1);
                int n2 = letterIsBetter(c2);
                //if n1 should be first
                if (n2 < n1)
                    return 1;
                //if n2 should be first
                if (n2 > n1)
                    return -1;
            }
            //case like : aa wins aaa (s1 is better)
            if(s2.length() < s1.length())
                return 1;
            //case like : s2 is better
            if(s2.length() > s1.length())
                return -1;
            //equals
            return 0;

        }
        private int letterIsBetter(char c) {
            if (Character.isLetter(c))
                return Character.valueOf(Character.toLowerCase(c));
            if (Character.isDigit(c))
                return (Character.valueOf(c) + 75);
            else return (Character.valueOf(Character.valueOf(c)) + 140);
        }
    };

    /**
     * takes all the postings of terms and create one long String and delete the postings from memory
     * @param dictionaryPosting - dictionary of term and postings
     * @return - string of all postings combined
     */
    private PriorityQueue<String> copyPostingIntoString(HashMap<String,String[]> dictionaryPosting) {
//        sortIt(dictionaryPosting);
        PriorityQueue<String> sortTermsQ = new PriorityQueue<>(compareLexicographically);
        Set<String> keys = dictionaryPosting.keySet();
        for(String term: keys){
            //term already exists in dictionary
            String[] dfAndPosting = dictionaryPosting.get(term);
            if(dfAndPosting[1].equals(""))
                continue;
            sortTermsQ.add(term + ":" + dfAndPosting[1] + "\n");
            dfAndPosting[1] = "";
            dictionaryPosting.put(term, dfAndPosting);
        }
        return sortTermsQ;
    }

    private PriorityQueue<String> copyDicIntoPQByAscii() {
        PriorityQueue<String> sortTermsQ = new PriorityQueue<>();
        Set<String> keys = dictionaryPosting.keySet();
        for(String term: keys){
            //term already exists in dictionary
            String[] dfAndPosting = dictionaryPosting.get(term);
            sortTermsQ.add(term+":"+dfAndPosting[0]+";"+dfAndPosting[2]);
        }
        return sortTermsQ;
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
                int tfInDoc = dicDoc.get(term);
                if(dfPosting[1].equals(""))
                    dfPosting[1]=currDoc.getDocumentID()+","+tfInDoc;
                else
                    dfPosting[1]+=";"+currDoc.getDocumentID()+","+tfInDoc;
                int tfOverall = Integer.parseInt(dfPosting[2]);
                dfPosting[2]=""+(tfOverall+tfInDoc);
                dictionaryPosting.put(term,dfPosting);
            }
            //term doesn't exist in dictionary
            else{
                String[] dfPosting = new String[3];
                dfPosting[0]="1";
                String tf=""+dicDoc.get(term);
                dfPosting[1]=currDoc.getDocumentID()+","+tf;
                dfPosting[2]=tf;
                dictionaryPosting.put(term,dfPosting);
            }
        }
    }

    /**
     *
     */
    private void unitAllTempPostingsToOnePostingInDisk(String pathToCreate) {
        int currDir = 0;
        File unitedPosting;
        //create file for each temp posting and insert to the temp posting array
        PriorityQueue<String[]> linesPQ = new PriorityQueue<>(new compareLexicographically2());//todo repair the compare function
        File[] tempPostingFiles = (new File(pathToCreate)).listFiles();
        BufferedReader[] br = new BufferedReader[tempPostingFiles.length];
        //create new text file for united posting
        try{
            unitedPosting = new File(pathToCreate+"/Postings/unitedPosting.txt");
            unitedPosting.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(unitedPosting));
            int i = 0;
            for (File f : tempPostingFiles) {
                br[i] = new BufferedReader(new FileReader(f));
                i++;
            }
            //insert one line from each temp posting to the PQ - "first init of the PQ"
            for (int j = 0; j < tempPostingFiles.length; j++) {
                insertLine2PQ(br,j,linesPQ);
            }
            //loop over the temp posting files and combine them into the united posting(to disk)
            String insertToOnePostDisk=dequeueAndInsert2UnitedPosting(linesPQ, br);
            while (!insertToOnePostDisk.equals("")) {
                bw.write(insertToOnePostDisk);
                insertToOnePostDisk = dequeueAndInsert2UnitedPosting(linesPQ, br);
            }
            bw.flush();
            bw.close();
        }
        catch (IOException e) { }


    }

    /**
     * dequeue the min line from pq and write the line to the united post file.
     * @param linesPQ - pq of lines
     * @param br - buffer reader for all the temp postings
     * return- combined line.
     */
    private String dequeueAndInsert2UnitedPosting(PriorityQueue<String[]> linesPQ, BufferedReader[] br) {
        if(linesPQ==null || br==null)
            return "";
        boolean equalTerms=true;
        //indexes of the used posting, need to take another line from each br index in this array list
        ArrayList<Integer> indexesOfUsedPostings= new ArrayList<>();
        String newLine;
        String nextLine;
        String[]nextLineArr;
        if(linesPQ.size()==0)
            return "";
        String[]newLineArr = linesPQ.remove();
        if(newLineArr==null)
            return "";
        insertLine2PQ(br,Integer.parseInt(newLineArr[1]),linesPQ);
        newLine = newLineArr[0];
        while(equalTerms){
            nextLineArr = linesPQ.peek();
            if(nextLineArr==null)
                return newLine +"\n";
            nextLine = nextLineArr[0];
            String temp1 = cutTheTerm(newLine);
            String temp2 = cutTheTerm(nextLine);
//            if(temp1.equals("loren") || temp1.equals("LOREN") || temp2.equals("LOREN") || temp2.equals("loren"))
//                System.out.println("hi");
            if(cutTheTerm(newLine).toLowerCase().equals(cutTheTerm(nextLine).toLowerCase())){
                newLine = combineLines(newLine,nextLine);
                insertLine2PQ(br,Integer.parseInt(nextLineArr[1]),linesPQ);
                linesPQ.remove();//remove the peeked one after the combine.
            }
            else equalTerms=false;
        }
        return newLine + "\n";
    }

    /**
     * combine 2 string of posting into one posting
     * @param l1 - first line
     * @param l2 - second line
     */
    private String combineLines(String l1, String l2) {
        if(l1==null||l2==null||l1.equals("")||l2.equals(""))
            return "";//todo at the call function handle ""
        //remove the term from the posting line.
        String T1 = cutTheTerm(l1);
        String T2 = cutTheTerm(l2);

        if(Pattern.matches("[a-zA-Z]+", T1) && Pattern.matches("[a-zA-Z]+", T2)){
//            if((T1.charAt(0)<=122 && T1.charAt(0)>=97)){
//                l2 = l2.substring(cutTheTerm(l2).length()+1);
//                return l1 + ";" + l2;
//            }
            if((T2.charAt(0)<=122 && T2.charAt(0)>=97)){
                l1 = l1.substring(cutTheTerm(l1).length()+1);
                return l2 + ";" + l1;
            }
        }
        l2 = l2.substring(cutTheTerm(l2).length()+1);//todo check
        return l1 + ";" + l2;
    }

    /**
     * insert to the PQ the line from the br array at index (index).
     * @param br - array of buffer readers
     * @param index -
     */
    private boolean insertLine2PQ(BufferedReader[] br, int index, PriorityQueue<String[]> linesPQ) {
        String line=null;
        try { line = br[index].readLine(); } catch (IOException e) { e.printStackTrace(); }
        if(line!=null){
            String [] ans = new String[2];
            ans[0] = line;
            ans[1] = ""+index;
            linesPQ.add(ans);
            return true;
        }
        return false;
    }

    /**
     * reset the posting ,the dictionary file and the memory of the program
     * @return true if the reset succeed' else return false
     */
    public boolean reset() {
        File fileTo = new File(pathTo);
        File[] filesInPathTo = fileTo.listFiles();
        boolean succDelete = false;
        for (File file : filesInPathTo) {
            if (file.isDirectory() && file.listFiles().length>0)
                recDelete(file.listFiles());
            file.delete();
        }
        System.gc();
        return true;
    }


    private void recDelete(File[] files) {
        for (File f:files){
            if(f.isDirectory() && f.listFiles().length>0)
                recDelete(f.listFiles());
            f.delete();
        }
    }


    /**
     * Dictionary of the terms : term, tf overall
     * @return the dictionary of the terms
     */
    public Queue<String> displayDictionary(boolean toStem) {

        Queue<String> dictionaryFromDisk = new LinkedList<>();
        dictionaryFromDisk.add("Term:number of documents;number of instances");
        String pathToCreate ="";
        if(toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        }
        else pathToCreate =pathTo + "\\WithoutStemming";
        File fileDictionary = new File(pathToCreate+"/Dictionaries/Dictionary.txt");
        try {
            String line="";
            BufferedReader bf = new BufferedReader(new FileReader(fileDictionary));
            line = bf.readLine();
            while(line!=null && line!=""){
                dictionaryFromDisk.add(line);
                line = bf.readLine();
            }
            return dictionaryFromDisk;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * load the dictionary from the disk to the memory
     * @return true if the loading succeed, else retutn false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean toStem) {
        String pathToCreate ="";
        if(toStem) {
            pathToCreate = pathTo + "\\WithStemming";
        }
        else pathToCreate =pathTo + "\\WithoutStemming";
        File fileDictionary = new File(pathToCreate+"/Dictionaries/Dictionary.txt");
        try {
            String line="";
            BufferedReader bf = new BufferedReader(new FileReader(fileDictionary));
            line = bf.readLine();
            while(line!=null && line!=""){
                String[] splitedLineInDictionary = line.split(";");
                if(splitedLineInDictionary==null) return false;
                String[] dfPostingTF = new String[3];
                dfPostingTF[0]=splitedLineInDictionary[1];
                dfPostingTF[1]="";
                dfPostingTF[2]=splitedLineInDictionary[2];
                dictionaryPosting.put(splitedLineInDictionary[0],dfPostingTF);
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

    public static void main(String[] args){
        Comparator<String> compareLexicographically1 = new Comparator<String>() {
            //letters first (ignoring differences in case), digits are second and marks (like &%^*%) are the last in priority.
            @Override
            public int compare(String s1, String s2) {
                if (s1 == null || s1.equals("") || s2 == null || s2.equals(""))//todo check better
                    return 0;
                for (int i = 0; i < s1.length() && i < s2.length(); i++) {
                    char c1 = s1.charAt(i);
                    char c2 = s2.charAt(i);
                    int n1 = letterIsBetter(c1);
                    int n2 = letterIsBetter(c2);
                    //if n1 should be first
                    if (n2 < n1)
                        return 1;
                    //if n2 should be first
                    if (n2 > n1)
                        return -1;
                }
                //case like : aa wins aaa (s1 is better)
                if(s2.length() < s1.length())
                    return 1;
                //case like : s2 is better
                if(s2.length() > s1.length())
                    return -1;
                //equals
                return 0;

            }
            private int letterIsBetter(char c) {
                if (Character.isLetter(c))
                    return Character.valueOf(Character.toLowerCase(c));
                if (Character.isDigit(c))
                    return (Character.valueOf(c) + 75);
                else return (Character.valueOf(Character.valueOf(c)) + 140);
            }
        };

        ReadFile readFile = new ReadFile("C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל\\cor");
        Parse parse = new Parse(false, "C:\\Users\\nivdu\\Desktop\\StopWords");//todo change to boolean stemmer and stop word path from the user
        Indexer indexer = new Indexer("", "",false);
        PriorityQueue<String> pq = new PriorityQueue<>(compareLexicographically1);
        String s2 = "abolish";
        String s1 = "abolishing";
        String s3 = "abolishingfdf";
        String s4 = "abolishingfd";
        String s5 = "abolishingfdf2";
        String s6 = "ab";
        String s7 = "a";
        String s8 = "abolis";
//        String s4 = "ba";
//        String s5 = "12";
//        String s6 = "1";
//        String s7 = "*";
        pq.add(s1);
        pq.add(s2);
        pq.add(s3);
        pq.add(s4);
        pq.add(s5);
        pq.add(s6);
        pq.add(s7);
        pq.add(s8);
        int size = pq.size();
//        for (int i = 0; i < size ; i++) {
//            System.out.println(pq.peek());
//            System.out.println(pq.remove());
//        }
        String currToken = "sup]3[/]He:Ar:Xe";
        String[] toAdd = currToken.split(" |\\/|\\.|\\?|!|:|;|#|@|^|\\+|\\&|\\{\\}\\*|\\|\\>|\\=\\<|\\(|\\)|\\[|\\]");
        for (String s:toAdd){
            System.out.println(s);
        }

    }


}
