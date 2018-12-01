package sample;

import org.omg.SendingContext.RunTime;

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
    //contain all the cities from tags <F P=104> (if the city have two words, save the first one.
    private HashSet<String> citiesFromTags;
    private HashMap<String,String[]> dictionaryCities;
//    private int tempCitiesPostingCounter;


    /**
     * Constructor
     */
    Indexer(ReadFile rf, Parse p) {
        this.filesPostedCounter = 0;
        readFile = rf;
        parse = p;
        docList = new ArrayList<>();
        dictionaryPosting = new HashMap<>();
        tempPostingCounter = 0;
        citiesFromTags = new HashSet<>();
        dictionaryCities = new HashMap<>();
    }


    public void createPostingAndDic(String corpusPath) {
        File theDirTerms = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\termsPosting");
        File theDirCities = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\citiesPosting");
        // if the directory does not exist, create it
        if (!theDirTerms.exists() || !theDirCities.exists()) {
            try {
                if(!theDirCities.exists())
                    theDirCities.mkdir();
                if(!theDirTerms.exists())
                    theDirTerms.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }
        ArrayList<String> allFilesPaths = readFile.readCorpus(corpusPath);
        for (String path : allFilesPaths) {
            HashSet<String> currDocCities = readFile.generateSetOfCities(path);
            if(currDocCities.size()>0)
                //add all the city names from current doc into the full(from all the corpus) HashSet of cities.
                for (String city:currDocCities){
                    citiesFromTags.add(city);
                }
        }
        for (String path : allFilesPaths) {
            parseFile(readFile.spiltFileIntoSeparateDocs2(path));
        }
        unitAllTempPostingsToOnePostingInDisk();
        unitAllTempCityPostingsToOnePostingInDisk();
    }

//    class compareLexicographically implements Comparator<String> {
//        // Overriding compare()method of Comparator
//        // compare string lexicographically.
//        public int compare(String s1, String s2) {
//            s1 = cutTheTerm(s1);
//            s2 = cutTheTerm(s2);
//            int comp = s1.compareTo(s2);
//            if (comp < 0)
//                return -1;
//            if (comp > 0)
//                return 1;
//            else return 0;
//        }
//    }

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

    public void parseFile(ArrayList<String[]> filesToParse) {
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
                    document currDoc = parse.parseDoc(currentDoc, city, docId, citiesFromTags);
                    if(filesPostedCounter==40){
                        filesPostedCounter=0;
                        saveAndDeletePosition(dictionaryPosting);
                        saveAndDeleteCitiesPosition(dictionaryCities);
                        tempPostingCounter++;
                    }
                    combineDicDocAndDictionary(currDoc);
                    combineCitiesFromDoc(currDoc);
                    currDoc.removeDic();
                    currDoc.removeLocationOfCities();
                    docList.add(currDoc);
                }
                index++;
            }
        }
        filesToParse.clear();
    }

    private void saveAndDeleteCitiesPosition(HashMap<String,String[]> dictionaryCities) {
        try {
            File f = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\citiesPosting/postingCities" + tempPostingCounter + ".txt");//todo counter for number
            if (!f.exists())
                f.createNewFile();
            FileWriter fw = new FileWriter(f);
            PriorityQueue<String> tempCitiesPosting = copyCityPostingIntoString(dictionaryCities);
            int PQsize= tempCitiesPosting.size();
            for (int i = 0; i < PQsize; i++) {
                fw.write(tempCitiesPosting.remove());
            }
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            System.out.println("problem in saveanddeletefunction indexer");//todo delete this
            e.printStackTrace();
        }
    }

    private void saveAndDeletePosition(HashMap<String,String[]> dictionaryPosting) {
        try {
            File f = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\termsPosting/posting" + tempPostingCounter + ".txt");//todo counter for number
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
        PriorityQueue<String> sortTermsQ = new PriorityQueue<>(compareLexicographically);
        Set<String> keys = dictionaryPosting.keySet();
        for(String term: keys){
            //term already exists in dictionary
            String[] dfAndPosting = dictionaryPosting.get(term);
            if(dfAndPosting.length<2)
                System.out.println("hi");
            if(dfAndPosting[1].equals(""))
                continue;
            sortTermsQ.add(term + ":" + dfAndPosting[1] + "\n");
            dfAndPosting[1] = "";
            dictionaryPosting.put(term, dfAndPosting);
        }
        return sortTermsQ;
    }

    /**
     * takes all the postings of cities and create one long String and delete the postings from memory
     * @param dictionaryCities - dictionary of cities and locations
     * @return - string of all postings combined
     */
    private PriorityQueue<String> copyCityPostingIntoString(HashMap<String,String[]> dictionaryCities) {
        PriorityQueue<String> sortCityQ = new PriorityQueue<>(compareLexicographically);
        Set<String> keys = dictionaryCities.keySet();
        for(String city: keys){
            //term already exists in dictionary
            String[] cityPosting = dictionaryCities.get(city);
            if(cityPosting[0].equals(""))
                continue;
            sortCityQ.add(city + ":" + cityPosting[0] + "\n");
            cityPosting[0] = "";
            dictionaryCities.put(city, cityPosting);
        }
        return sortCityQ;
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
                if(dfPosting[1].equals(""))
                    dfPosting[1]=currDoc.getDocumentID()+","+dicDoc.get(term);
                else
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

    /**
     * combine the location of cities with the general dictionaryCities.
     * @param currDoc - the document which his dictionary will be combined.
     */
    private void combineCitiesFromDoc(document currDoc) {
        HashMap<String,ArrayList<Integer>> citiesFromDoc = currDoc.getLocationOfCities();
        Set<String> keys = citiesFromDoc.keySet();
        for(String term: keys){
            if(dictionaryCities.containsKey(term)) {
                String[] cityLocation = dictionaryCities.get(term);
                if (cityLocation[0].equals("")) {
                    cityLocation[0] = currDoc.getDocumentID();
                    for (int location : citiesFromDoc.get(term)) {
                        cityLocation[0] += "," + location;
                    }
                } else {
                    cityLocation[0] += ";" + currDoc.getDocumentID();
                    for (int location: citiesFromDoc.get(term)){
                        cityLocation[0] += "," + location;
                    }
                }
                dictionaryCities.put(term, cityLocation);
            }
            else{
                String[] cityLocation = new String[1];
                cityLocation[0]=currDoc.getDocumentID();
                for (int location : citiesFromDoc.get(term)) {
                    cityLocation[0] += "," + location;
                }
                dictionaryCities.put(term,cityLocation);
            }
        }
    }

    /**
     *
     */
    private void unitAllTempPostingsToOnePostingInDisk() {
        int currDir = 0;
        File unitedPosting;
        //create file for each temp posting and insert to the temp posting array
        PriorityQueue<String[]> linesPQ = new PriorityQueue<>(new compareLexicographically2());//todo repair the compare function
        File[] tempPostingFiles = (new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\termsPosting")).listFiles();
        BufferedReader[] br = new BufferedReader[tempPostingFiles.length];
        //create new text file for united posting
        try{
            unitedPosting = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\termsPosting/unitedPosting.txt");
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
     *
     */
    private void unitAllTempCityPostingsToOnePostingInDisk() {
        int currDir = 0;
        File unitedPosting;
        //create file for each temp posting and insert to the temp posting array
        PriorityQueue<String[]> linesPQ = new PriorityQueue<>(new compareLexicographically2());//todo repair the compare function
        File[] tempPostingFiles = (new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\citiesPosting")).listFiles();
        BufferedReader[] br = new BufferedReader[tempPostingFiles.length];
        //create new text file for united posting
        try{
            unitedPosting = new File("C:\\Users\\nivdu\\Desktop\\bimbamtirasham\\citiesPosting/unitedPosting.txt");
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
        Indexer indexer = new Indexer(readFile, parse);
        PriorityQueue<String> pq = new PriorityQueue<>(compareLexicographically1);
        String s2 = "sup]3[/]He:Ar:Xe";
        String s1 = "HaW";
        String s3 = "$@%#$%";
        String s4 = ".";
        String s5 = " ";
        String s6 = "FGF23fdfdcf%#Ttrh!'";
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
        int size = pq.size();
        for (int i = 0; i < size ; i++) {
            System.out.println(pq.remove().toLowerCase());
        }
//        String currToken = "sup]3[/]He:Ar:Xe";
//        currToken.toLowerCase();
//        String[] toAdd = currToken.split(" |\\/|\\.|\\?|!|:|;|#|@|^|\\+|\\&|\\{\\}\\*|\\|\\>|\\=\\<|\\(|\\)|\\[|\\]");
//        for (String s:toAdd){
//            System.out.println(s);
//        }

    }


}
