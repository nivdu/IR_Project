package Model;

import javafx.scene.control.Alert;
import sun.awt.Mutex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Parse {
    //hashSet for all the stop words.
    HashSet<String> stopWords;
    //HashSet for month names
    HashMap<String, String> months;
    //stop Word Path
    String stopWordPath;
    //true if stem is needed.
    boolean toStem;
    //Stemmer class
    Stemmer stemmer;
    //all the cities from document tags (F P=104)
    HashSet<String> citiesFromTags;
    /**
     * Parse Constructor.
     */
    public Parse(boolean toStem, String pathFrom) {
        this.stemmer = new Stemmer();
        this.toStem = toStem;
        this.stopWordPath = pathFrom;
        this.stopWords = new HashSet<String>();
        getStopWordsIntoHastSet();
        this.months = new HashMap<String, String>();
        createMonthHS();
    }

    /**
     * add to the month HS all the months names.
     */
    private void createMonthHS() {
        months.put("January", "01");
        months.put("Jan", "01");
        months.put("JANUARY", "01");
        months.put("JAN", "01");
        months.put("February", "02");
        months.put("Feb", "02");
        months.put("February", "02");
        months.put("FEB", "02");
        months.put("March", "03");
        months.put("Mar", "03");
        months.put("MARCH", "03");
        months.put("MAR", "03");
        months.put("April", "04");
        months.put("Apr", "04");
        months.put("APRIL", "04");
        months.put("APR", "04");
        months.put("MAY", "05");
        months.put("May", "05");
        months.put("June", "06");
        months.put("Jun", "06");
        months.put("JUNE", "06");
        months.put("JUN", "06");
        months.put("July", "07");
        months.put("Jul", "07");
        months.put("JULY", "07");
        months.put("JUL", "07");
        months.put("August", "08");
        months.put("Aug", "08");
        months.put("AUGUST", "08");
        months.put("AUG", "08");
        months.put("September", "09");
        months.put("Sep", "09");
        months.put("SEPTEMBER", "09");
        months.put("SEP", "09");
        months.put("October", "10");
        months.put("Oct", "10");
        months.put("OCTOBER", "10");
        months.put("OCT", "10");
        months.put("November", "11");
        months.put("Nov", "11");
        months.put("NOVEMBER", "11");
        months.put("NOV", "11");
        months.put("December", "12");
        months.put("Dec", "12");
        months.put("DECEMBER", "12");
        months.put("DEC", "12");
    }

    public document parseDoc(document doc2Parse) {
        if(doc2Parse==null) {//todo delete this if
            System.out.println("parser line 91");
            return null;
        }
        String city = doc2Parse.getCity();
        doc2Parse.setCity("");
        //locations in the doc of appearances of the cities from tags , String - city name, ArrayList - locations indexes
        createDocDate(doc2Parse);
        HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc = new HashMap<>();
        if (city!=null && !city.equals("")) {
            ArrayList<Integer> locationArray = new ArrayList<>();
            locationArray.add(-1);
            locationOfCitiesAtCurrDoc.put(city.toLowerCase(), locationArray);
        }
        doc2Parse.setLocationOfCitiesAtCurrDoc(locationOfCitiesAtCurrDoc);
        //temporary dictionary to find the max tf in current doc.
        HashMap<String, int[]> dicDoc;
        //in case of title
        if(doc2Parse.getDocTitle()!=null && !doc2Parse.getDocTitle().equals("")) {
            document documentOfTitle = new document();
            documentOfTitle.setLocationOfCitiesAtCurrDoc(locationOfCitiesAtCurrDoc);
            documentOfTitle.setDocSplited(doc2Parse.getDocTitle());
            dicDoc = parseMainFunc(documentOfTitle, null);
            //insert the title terms into the dicdoc dictionary - dicdicintarr[1] = 1; marks that that term comes from the title - > for the indexer posting function.
            Set<String> keys = dicDoc.keySet();
            for (String titleTerm : keys) {
                if (titleTerm.equals(""))
                    continue;
                int[] dicDocIntArr = new int[2];
                dicDocIntArr[0] = 1;
                dicDocIntArr[1] = 1;
                dicDoc.put(titleTerm, dicDocIntArr);
            }
            if(dicDoc.size()>0) {
                doc2Parse.setDicDoc(dicDoc);
            }
            if(documentOfTitle.getLocationOfCities()!=null && documentOfTitle.getLocationOfCities().size()>0){
                doc2Parse.setLocationOfCitiesAtCurrDoc(documentOfTitle.getLocationOfCitiesAtCurrDoc());
            }
        }
        //parse the full split document and return the dicDoc dictionary contain the terms from it.
        dicDoc = parseMainFunc(doc2Parse, null);
        doc2Parse.setDicDoc(dicDoc);
        //delete start and end char punctutations from the terms.
        int tf = getMaxTF(dicDoc);
        doc2Parse.setMaxTf(tf);
        if(doc2Parse==null)
            System.out.println("line 138 parse");
        return doc2Parse;
    }

    private void createDocDate(document doc2Parse) {
        String date = doc2Parse.getPublishDate();
        doc2Parse.setPublishDate("");
        String[] dateByParts;
        String Syear = null, Smonth = null, Sday = null;
        if (date != null && !date.equals("")) {
            dateByParts = date.split(" ");
            //if legal date
            if (dateByParts != null && dateByParts.length == 3) {
                for (String currPart : dateByParts) {
                    if (currPart.length() == 2 && checkIfOnlyDigitsDotsComma(currPart)) {
                        int day = Integer.parseInt(currPart);
                        if (day >= 0 && day <= 31)
                            Sday = currPart;
                    } else if (currPart.length() == 4 && checkIfOnlyDigitsDotsComma(currPart))
                        Syear = currPart;
                    else if (months.containsKey(currPart))
                        Smonth = months.get(currPart);
                }
                if (Sday != null && Smonth != null && Syear != null) {
                    int[] dateByPartsInt = new int[3];
                    dateByPartsInt[0] = Integer.parseInt(Syear);
                    dateByPartsInt[1] = Integer.parseInt(Smonth);
                    dateByPartsInt[2] = Integer.parseInt(Sday);
                    doc2Parse.setDocSplitDate(dateByPartsInt);
                    doc2Parse.setPublishDate("");
                }
            }
        }
    }
//todo change the eara
    /**
     * parse main function - parse the full split document and return the dicDoc dictionary contain the terms from it.
//     * @param splitedDoc - the current doc split to tokens
//     * @param citiesFromTags - the city names from all corpus tags ->(<F P=104>)
//     * @param locationOfCitiesAtCurrDoc - saves each city term from the current document with the location of the city from the document file.
//     * @param dicDoc - the current document dictionary
     * @return - the dicDoc dictionary contain the terms from the current given array.
     */

    public HashMap<String, int[]> parseMainFunc(document doc2Parse, Query query2Parse){
        Mutex m1 = new Mutex();
        if(doc2Parse==null && query2Parse==null) {
            System.out.println("parse line 148");
            return null;
        }
        HashMap<String, int[]> dicDoc = new HashMap<>();
        String[] splitedDoc;
        HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc;
        if(doc2Parse==null && query2Parse!=null){
            splitedDoc = query2Parse.getDocSplited();
            if(splitedDoc==null) {
                System.out.println("line 192 parseMain Parse class");
            }
            locationOfCitiesAtCurrDoc=null;
        }
        else{
            splitedDoc = doc2Parse.getDocSplited();
            if(splitedDoc==null && doc2Parse.getDicDoc()==null) {
                return dicDoc;
            }
            else if(splitedDoc==null) return doc2Parse.getDicDoc();
            if(doc2Parse.getDicDoc()!=null && doc2Parse.getDicDoc().size()>0)
                dicDoc = doc2Parse.getDicDoc();
            locationOfCitiesAtCurrDoc = doc2Parse.getLocationOfCitiesAtCurrDoc();
        }
        int jump = 0;
        for (int j = 0; j < splitedDoc.length; j++) {
            splitedDoc[j] = deletePunctutations(splitedDoc[j]);
        }
        for (int currDocIndex = 0; currDocIndex < splitedDoc.length; currDocIndex++) {
            if (splitedDoc[currDocIndex].equals("") || splitedDoc[currDocIndex].equals("\n"))
                continue;
            String currToken = splitedDoc[currDocIndex];
            //continue to next token whens it stop Word
            if (deleteStopWords(currToken.toLowerCase()))
                continue;
            //add the currdocindex to the array list of this city location at the this doc
            if(doc2Parse!=null)
                checkIfCityToken(currToken.toLowerCase(), locationOfCitiesAtCurrDoc, citiesFromTags, currDocIndex);
            //stem if needed
            m1.lock();
            if (toStem) {
                stemmer.setTerm(currToken);
                stemmer.stem();
                currToken = stemmer.getTerm();
                currToken = deletePunctutations(currToken);
            }
            m1.unlock();
            //get prev and next tokens if there isn't next token (the end of the array) return nextToken="", if index==0 return prevToken="".
            String nextToken = getNextToken(splitedDoc, currDocIndex);
            String nextNextToken = getNextToken(splitedDoc, currDocIndex + 1);
            String nextNextNextToken = getNextToken(splitedDoc, currDocIndex + 2);

            //check percentageTerms function
            jump = percentageTerm(nextToken, currToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //check DollarTerm function less than miliion.
            jump = DollarTermLessThanMillion(currToken, nextNextToken, nextNextToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }
            try {
                jump = DollarTermMoreThanMillion(currToken, nextNextToken, nextNextToken, nextNextNextToken, dicDoc);
                if (jump != -1) {
                    currDocIndex += jump;
                    continue;
                }
            }
            catch (Exception e){
                System.out.println("dollar problem need more from them");
            }
            jump = DateTerm(nextToken, currToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            jump = expressionAndRangesTerm(nextToken, currToken, nextNextToken, currDocIndex, splitedDoc, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //our law1
            jump = ourLaw1(currToken, nextToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //our law2
            jump = ourLaw2(currToken, nextToken, nextNextToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //check regularNumberTerms function.
            jump = regularNumberTerms(currToken, nextToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //check bigsmall letter cases
            jump = addOnlyLettersWords(currToken, dicDoc);
            if (jump != -1) {
                currDocIndex += jump;
                continue;
            }

            //add token that didn't match any case. (like f16 and a lot more cases).
            if (currToken.length() > 1) {
                addToDicDoc(dicDoc, currToken);
            }
        }
        if(doc2Parse!=null)
            doc2Parse.setLocationOfCitiesAtCurrDoc(locationOfCitiesAtCurrDoc);
        return dicDoc;
    }

    //add the currdocindex to the array list of this city location at the this doc
    private void checkIfCityToken(String checkIfCityToken, HashMap<String, ArrayList<Integer>> locationOfCitiesAtCurrDoc, HashSet<String> citiesFromTags, int currDocIndex) {
        if(locationOfCitiesAtCurrDoc==null)
            return;
        if (citiesFromTags.contains(checkIfCityToken)) {
            //add the currdocindex to the array list of this city location at the this doc
            if (locationOfCitiesAtCurrDoc.containsKey(checkIfCityToken)) {
                locationOfCitiesAtCurrDoc.get(checkIfCityToken).add(currDocIndex);
            } else {
                ArrayList<Integer> locationList = new ArrayList<>();
                locationOfCitiesAtCurrDoc.put(checkIfCityToken, locationList);
                locationOfCitiesAtCurrDoc.get(checkIfCityToken).add(currDocIndex);
            }
        }
    }

    /**
     * combine two token into one term with "-" between them and add them to the dictionary if the first one is legal number and the second is kg, kilogram or kilograms
     *
     * @param currToken - the current token
     * @param nextT     - the next token in the doc
     * @param dicDoc    - the dictionary of the document
     * @return - int 1 if add to dictionary or -1 if didn't.
     */
    private int ourLaw1(String currToken, String nextT, HashMap<String, int[]> dicDoc) {
        if (nextT == null || nextT.equals("") || currToken == null || currToken.equals(""))
            return -1;
        //if the next token is kg and the current one is number. save them as 1 term number-kg
        if (nextT.toLowerCase().equals("kg") || nextT.toLowerCase().equals("kilogram") || nextT.toLowerCase().equals("kilograms")) {
            if (checkIfOnlyDigitsDotsComma(currToken)) {
                String newTerm = currToken + "-kg";
                addToDicDoc(dicDoc, newTerm);
                return 1;
            }
        }
        return -1;
    }

    /**
     * 1. in cases like number kilometer/kilometers/km/mile/miles combine the tokens into 1 term number-km/mile
     * 2. in cases like number square kilometer/kilometers/km/mile/miles combine the tokens into 1 term number-square-km/mile
     *
     * @param currToken - current token from the document
     * @param nextT     - next token from the document
     * @param nextnextT - next next token from the document
     * @param dicDoc    - the dictionary
     * @return - int of the number of tokens combined to one term, (the number of used tokens from the document. if didn't use any of them return -1.
     */
    private int ourLaw2(String currToken, String nextT, String nextnextT, HashMap<String, int[]> dicDoc) {
        if (nextT == null || nextT.equals("") || currToken == null || currToken.equals(""))
            return -1;
        if (checkIfOnlyDigitsDotsComma(currToken)) {
            nextT = nextT.toLowerCase();
            boolean isSquare = false;
            int jump = 0;
            String newTerm = "";
            String square = "";
            if (nextnextT != null && !nextnextT.equals("") && nextT.equals("square")) {
                jump++;
                isSquare = true;
                nextT = nextnextT;
                square = "-square";
            }
            if (nextT.equals("kilometers") || nextT.equals("kilometer") || nextT.equals("km")) {
                newTerm = currToken + square + "-km";
                addToDicDoc(dicDoc, newTerm);
                return jump + 1;
            }
            if (nextT.equals("miles") || nextT.equals("mile")) {
                newTerm = currToken + square + "-mile";
                addToDicDoc(dicDoc, newTerm);
                return jump + 1;
            }
        }
        return -1;
    }

    /**
     * add term into the current document dictionary and increase by 1 the tf value in dicDoc.
     *
     * @param dicDoc - doc dictionary
     * @param term   - term to add to dicDoc
     */
    private void addToDicDoc(HashMap<String, int[]> dicDoc, String term) {
        if (dicDoc.containsKey(term)) {
            int[] dicIntArr = dicDoc.get(term);
            dicIntArr[0]++;
            dicDoc.put(term, dicIntArr);
        } else {
            int[] dicIntArr = new int[2];
            dicIntArr[0] = 1;
            dicIntArr[1] = 0;
            dicDoc.put(term, dicIntArr);
        }
    }

    /**
     * iterator over the dicDoc hashmap and find the max tf.
     * @param dicDoc - the dictionary of the document
     */
    private int getMaxTF(HashMap<String, int[]> dicDoc) {
        int max = 0;
        Iterator it = dicDoc.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            int[] temp = (int[]) pair.getValue();
            if (temp[0] > max)
                max = temp[0];
        }
        return max;
    }

    /**
     * read file to String line by line
     *
     * @param file - The path of the file to read into String
     * @return - String of the context of the file
     */
    private String file2String(File file) {
        {
            String content = "";
            try {
                content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            } catch (IOException e) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setContentText("The folder in the path you selected does not contain a text file named stop_words, please choose a new path and try again. (:");
                chooseFile.show();
                return "";
            }
            return content;
        }
    }

    /**
     * get the stopWords from the stopWords file and insert them into stopWords hashSet.
     */
    private void getStopWordsIntoHastSet() {
        //insert all the stop words from stop words file into HashSet.
        //scanner function from link: "https://stackoverflow.com/questions/30011400/splitting-textfile-at-whitespace"
        String stopWordsPath = stopWordPath;
        File file = new File(stopWordsPath + "/stop_words.txt");
        String wholeFileString = "";
        wholeFileString = file2String(file);
        if (!wholeFileString.equals("")) {
            String[] currStopWord = wholeFileString.split("\r\n|\\\n");
            for (String SW : currStopWord)
                stopWords.add(SW);
        }
    }

    /**
     * Deletes initial and final characters if they are punctuation marks
     *
     * @param toCheck - string to check
     * @return - string without the unneeded punctuation
     */
    private String deletePunctutations(String toCheck) {
        if (toCheck == null || toCheck.equals(""))
            return "";
        if (toCheck.equals("U.S."))
            return toCheck;
        char char2Check = toCheck.charAt(0);
        while (toCheck.length() > 1 && ((char2Check >= 0 && char2Check <= 44) || char2Check == 46 || char2Check == 47 || (char2Check == 45 && !checkIfOnlyDigitsDotsComma(toCheck)) || (char2Check >= 58 && char2Check <= 64) || (char2Check >= 91 && char2Check <= 96) || (char2Check >= 123 && char2Check <= 127) || !(char2Check>=0 && char2Check<=127))) {
            toCheck = toCheck.substring(1);
            char2Check = toCheck.charAt(0);
        }
        char2Check = toCheck.charAt(toCheck.length() - 1);
        while (toCheck.length() > 1 && ((char2Check >= 0 && char2Check <= 44) || char2Check == 46 || char2Check == 47 || (char2Check == 45 && !checkIfOnlyDigitsDotsComma(toCheck)) || (char2Check >= 58 && char2Check <= 64) || (char2Check >= 91 && char2Check <= 96) || (char2Check >= 123 && char2Check <= 127) || !(char2Check>=0 && char2Check<=127))) {
            toCheck = toCheck.substring(0, toCheck.length() - 1);
            char2Check = toCheck.charAt(toCheck.length() - 1);
        }
        return toCheck;
    }

    /**
     * the function check if a word is stop word or not from stopWords HastSet.
     *
     * @param currToken - current word to check
     * @return - true is current word is stopWord. else false.
     */
    private boolean deleteStopWords(String currToken) {
        if (stopWords.contains(currToken))
            return true;
        return false;
    }

    /**
     * if the token is percentage token add it to the terms dictionary.
     *
     * @param nextT - the next token to check in the curr doc.
     * @param token - the curr token to check in the curr doc.
     * @return - -1 if not added to the dic else return the number of words used from the doc
     */
    private int percentageTerm(String nextT, String token, HashMap dicDoc) {
        if (token == null || token.length() <= 0)
            return -1;
        //cases like 6%
        if (token.charAt(token.length() - 1) == '%') {
            //substring from number% to number (6% to 6)
            String checkIfNumber = token.substring(0, token.length() - 1);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if (checkIfOnlyDigitsDotsComma(checkIfNumber)) {
                addToDicDoc(dicDoc, token);
                return 0;
            }
        }
        //cases like "6 percent", "6 percentage"
        else if (nextT != null && nextT != "" && (nextT.equals("percent") || nextT.equals("percentage"))) {
            //if before the percent/percentage there is only digits and docs add to the dictionary the whole token combine with '%'.

            if (checkIfOnlyDigitsDotsComma(token)) {
                addToDicDoc(dicDoc, token + '%');
                return 1;
            }
        }
        return -1;
    }

    /**
     * check whether a token is date term.
     *
     * @param nextT - the next token in the doc
     * @param token - curr token in the doc
     * @return - true if the token are date token type.
     */
    private int DateTerm(String nextT, String token, HashMap dicDoc) {
        if(nextT==null || token==null || dicDoc==null ||nextT.equals("")||token.equals(""))
            return -1;
        if (checkIfOnlyDigitsDotsComma(token) && token.length() < 4 && months.containsKey(nextT)) {
            if (token.length() == 1)
                token = "0" + token;
            String toAdd = months.get(nextT) + "-" + token;
            addToDicDoc(dicDoc, toAdd);
            return 1;
        } else if (checkIfOnlyDigitsDotsComma(nextT) && token.length() < 4 && months.containsKey(token)) {
            if (nextT.length() == 1)
                nextT = "0" + nextT;
            String toAdd = months.get(token) + "-" + nextT;
            addToDicDoc(dicDoc, toAdd);
            return 1;
        } else if (months.containsKey(token) && isNumber(nextT)) {//todo maby check if only numbers without digits and dots.
            String toAdd = nextT + "-" + months.get(token);
            addToDicDoc(dicDoc, toAdd);
            return 1;
        }
        return -1;
    }

    private boolean isNumber(String s){
        if(s!=null && !s.equals("")){
            for (Character c:s.toCharArray()){
                if(!Character.isDigit(c))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * check if a string is a legal fraction
     *
     * @param token - string to check
     * @return - true if legal fraction else false.
     */
    private boolean checkIfLegalFraction(String token) {
        if (token == null || token.equals(""))
            return false;
        int slashCounter = 0;
        if (!Character.isDigit(token.charAt(0)))
            return false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '/') {
                slashCounter++;
                if (slashCounter != 1)
                    return false;
                if (i + 1 >= token.length() || !Character.isDigit(token.charAt(i + 1)))
                    return false;
                continue;
            } else if (!Character.isDigit(c))
                return false;
        }
        if (slashCounter == 1)
            return true;
        return false;
    }

    /*
     * return the next token from currDoc
     *
     * @param currDoc - the curr doc
     * @param index   - current token index
     * @return - String type of the token at location index +1. if index is the last of the array return empty string.
     */
    private String getNextToken(String[] currDoc, int index) {
        if (index + 1 >= currDoc.length)
            return "";
        return currDoc[index + 1];
    }

    /**
     * check what is the size of the number in the string
     *
     * @param token - the string which is number
     * @return - return the kind of the number - K for thousand, M- for miliion, B for Billion and U for numbers
     * less than thousand
     */
    private String checkIfKMBU(String token) {
        String number = "";
        for (int i = 0; i < token.length(); i++) {
            if (i == 0 && token.charAt(0) == '-') {
                continue;
            } else if (Character.isDigit(token.charAt(i))) {
                number += token.charAt(i);
            } else if (token.charAt(i) == ',') {
                continue;
            } else if (token.charAt(i) == '.') {
                break;
            }
        }
        if (number.length() >= 4 && number.length() <= 6)
            return "K";
        if (number.length() >= 7 && number.length() <= 9)
            return "M";
        if (number.length() >= 10)
            return "B";
        return "U";//less than thousand
    }

    /**
     * every price which is less than million dollars will represent as number Dollars
     *
     * @param nextT     - the next token
     * @param nextNextT - the next next token
     * @param token     - the current token
     * @return - true if the term keep the given laws
     */
    private int DollarTermLessThanMillion(String token, String nextT, String nextNextT, HashMap dicDoc) {
        if (token == null || token.length() <= 0)
            return -1;
        //cases like $1000
        if (token.charAt(0) == '$') {
            String checkIfNumber = token.substring(1);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if (checkIfOnlyDigitsDotsComma(checkIfNumber)) {
                addToDicDoc(dicDoc, checkIfNumber + " " + "Dollars");
                return 0;
            }
        }
        String kind = checkIfKMBU(token);
        if (!checkIfOnlyDigitsDotsComma(token) || !(kind.equals("K") || kind.equals("U")))
            return -1;
        //cases like 123 Dollars
        if (nextT.equals("Dollars")) {
            addToDicDoc(dicDoc, token + " " + nextT);
            return 1;
        }
        //cases like 123 3/4 Dollars
        if (checkIfLegalFraction(nextT) && nextNextT.equals("Dollars")) {
            addToDicDoc(dicDoc, token + " " + nextT + " " + nextNextT);
            return 2;
        }
        return -1;
    }

    /**
     * every price which is more than million dollars will represent as number M Dollars
     *
     * @param token          - the curren token
     * @param nextT          - the next token
     * @param nextNextT-     the next next token
     * @param nextNextNextT- the next next next token
     * @return- true if the term keep the given laws
     */
    public int DollarTermMoreThanMillion(String token, String nextT, String nextNextT, String nextNextNextT, HashMap dicDoc) {
        if (token.equals(""))
            return -1;
        boolean tokenIsNumber = checkIfOnlyDigitsDotsComma(token);
        boolean tokenIsMoreThanMillion = false;
        if (tokenIsNumber)
            tokenIsMoreThanMillion = isMoreThanMillion(token);
        String term = "";
        //cases like _ Dollars
        if(nextT.equals("Dollars")) {
            //cases like 1000000 Dollars
            if (tokenIsNumber && tokenIsMoreThanMillion) {
                term = regularNumberTerms2(token, nextT);
                term = convertingToMillionForDollars(term);
                term += " " + nextT;
                addToDicDoc(dicDoc, term);
                return 1;
            }
        }
        //cases like number _ Dollars
        if(checkIfOnlyDigitsDotsComma(token) && nextNextT.equals("Dollars")) {
            //cases like 20.6 m Dollars
            if (nextT.equals("m")) {
                    term = token + " M " + nextNextT;
                    addToDicDoc(dicDoc, term);
                    return 2;
            }
            //cases like 100 bn Dollars
            if (nextT.equals("bn")) {
                    term = token + "000" + " M " + nextNextT;
                    addToDicDoc(dicDoc, term);
                    return 2;
            }
        }
        if (token.charAt(0) == '$') {
            boolean tokenIsNumberWithout$ = checkIfOnlyDigitsDotsComma(token.substring(1));
            //cases like $100 million
            if (nextT.equals("million") && tokenIsNumberWithout$) {
                String termWithoutCommas = deletingCommasFromNumbers(token.substring(1));
                addToDicDoc(dicDoc, termWithoutCommas + " M Dollars");
                return 1;
            }
            //cases like $100 billion
            if (nextT.equals("billion") && tokenIsNumberWithout$) {
                String termWithoutCommas = deletingCommasFromNumbers(token.substring(1));
                addToDicDoc(dicDoc, termWithoutCommas + "000" + " M Dollars");
                return 1;
            }
            //cases like $1000000
            boolean tokenIsMoreThanMillionWithout$ = false;
            if (tokenIsNumberWithout$)
                tokenIsMoreThanMillionWithout$ = isMoreThanMillion(token.substring(1));
            if (tokenIsMoreThanMillionWithout$) {
                term = regularNumberTerms2(token, nextT);
                term = convertingToMillionForDollars(term);
                addToDicDoc(dicDoc, term + " Dollars");
                return 0;
            }
        }
        //cases like number _ U.S. dollars
        if(tokenIsNumber && nextNextT.equals("U.S.") && nextNextNextT.equals("dollars")){
            String tokenWithoutComma = deletingCommasFromNumbers(token);
            //cases like number million U.S. dollars
            if (nextT.equals("million")) {
                addToDicDoc(dicDoc, tokenWithoutComma + " M Dollars");
                return 3;
            }
            //cases like number billion U.S. dollars
            if (nextT.equals("billion")) {
                addToDicDoc(dicDoc, tokenWithoutComma + "000" + " M Dollars");
                return 3;
            }
            //cases like number trillion U.S. dollars
            if (nextT.equals("trillion")) {
                addToDicDoc(dicDoc, tokenWithoutComma + "000000" + " M Dollars");
                return 3;
            }
        }
        return -1;
    }

    /**
     * deleting commas from number
     *
     * @param number - the number with commas
     * @return the number without commas
     */
    private String deletingCommasFromNumbers(String number) {
        String ans = "";
        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) == ',')
                continue;
            ans += number.charAt(i);
        }
        return ans;
    }

    /**
     * check wether a string is a number bigger then one million
     *
     * @param token - string to check
     * @return - true if bigger then mil else false
     */
    private boolean isMoreThanMillion(String token) {
        String kind = checkIfKMBU(token);
        if (kind.equals("M") || kind.equals("B")) {
            return true;
        }
        return false;
    }

    private String convertingToMillionForDollars(String term) {
        if (term.charAt(term.length() - 1) == 'M') {
            String ans = term.substring(0, term.length() - 1);
            return ans + " M";
        } else if (term.charAt(term.length() - 1) == 'B') {
            String ans = term.substring(0, term.length() - 1);
            if (!ans.contains(".")) {
                ans += "000";
            } else {
                String beforeDot = "";
                String afterDot = "";
                boolean after = false;
                for (int i = 0; i < ans.length(); i++) {
                    if (!after && ans.charAt(i) == '.')
                        after = true;
                    else if (!after && Character.isDigit(ans.charAt(i))) {
                        beforeDot += ans.charAt(i);
                    } else if (after && Character.isDigit(ans.charAt(i))) {
                        afterDot += ans.charAt(i);
                    }
                }
                ans = beforeDot;
                if (3 - afterDot.length() >= 0) {
                    ans += afterDot;
                    for (int i = 0; i < 3 - afterDot.length(); i++) {
                        ans += "0";
                    }
                } else {
                    for (int i = 0; i < afterDot.length(); i++) {
                        ans += afterDot.charAt(i);
                        if (i == 2) {
                            ans += ".";
                        }
                    }
                }
            }
            return ans + " M";
        }
        return "";
    }

    /**
     * split the number to two parts - before dot and after dot
     *
     * @param token - the token in the document
     * @return true if legal number
     */
    private int regularNumberTerms(String token, String nextToken, HashMap dicDoc) {
        String term = regularNumberTerms2(token, nextToken);
        if (!term.equals("")) {
            if (nextToken.equals("Thousand") || nextToken.equals("Million") || nextToken.equals("Billion") || nextToken.equals("Trillion")) {
                addToDicDoc(dicDoc, term);
                return 1;
            }
            addToDicDoc(dicDoc, term);
            return 0;
        }
        return -1;
    }

    public String regularNumberTerms2(String token, String nextToken) {
        String number = "";
        String numberAfterDot = "";
        boolean isDouble = false;
        boolean isNumber = false;
        boolean isNegative = false;
        int countCommas = 0;
        for (int i = 0; i < token.length(); i++) {
            if (i == 0 && token.charAt(0) == '-') {
                isNegative = true;
            } else if (!isDouble && Character.isDigit(token.charAt(i))) {
                number += token.charAt(i);
                isNumber = true;
            } else if (token.charAt(i) == ',') {
                countCommas++;
            } else if (token.charAt(i) == '.') {
                isDouble = true;
            } else if (isDouble && Character.isDigit(token.charAt(i))) {
                numberAfterDot += token.charAt(i);
            } else {
                isNumber = false;
            }
        }
        String term = "";
        if (nextToken.equals("Thousand") && isNumber) {
            term = token + "K";
        } else if (nextToken.equals("Million") && isNumber) {
            term = token + "M";
        } else if (nextToken.equals("Billion") && isNumber) {
            term = token + "B";
        } else if (nextToken.equals("Trillion") && isNumber) {
            term = token + "000" + "B";
        } else if (isNumber) {
            term = numberToKMB(number, numberAfterDot, isDouble, nextToken);
        }
        if (isNumber && isNegative)
            return "-" + term;
        else if (isNumber && !isNegative)
            return term;
        return "";
    }

    /**
     * reduce non-relevant zeros from the end
     *
     * @param term
     * @param reduce
     * @return
     */
    private String reduceZeros(String term, int reduce) {
        String ans = term;
        for (int i = term.length() - 1; i >= term.length() - reduce; i--) {
            if (term.charAt(i) == '0')
                ans = term.substring(0, i);
            if (i == term.length() - reduce && term.charAt(i) == '0')
                ans = term.substring(0, i - 1);
        }
        return ans;
    }

    /**
     * adding dot in the appropriate location
     *
     * @param number - the term in document
     * @param reduce - the location of the dot from the end
     * @return
     */
    private String CreatingNumberForDictionary(String number, int reduce) {
        String term = "";
        for (int i = 0; i < number.length(); i++) {
            if (i == number.length() - reduce) {
                term += '.';
            }
            term += number.charAt(i);
        }
        return term;
    }

    /**
     * Words whose first letter is always a large letter, throughout the corpus, will be preserved with letters
     * Only large. On the other hand, if a word appears sometimes with a large letter and sometimes without a large letter we will save it
     * With only lowercase letters.
     */
    private int addOnlyLettersWords(String token, HashMap<String, int[]> dicDoc){
        if (token!=null && token!="" && ((Pattern.matches("[a-zA-Z]+", token)) || ( token.length()>=2  && token.charAt(token.length()-2) == '\'' && (Pattern.matches("[a-zA-Z]+", token.substring(token.length()-2)) && Character.isLetter(token.charAt(token.length()-1)) )))){
            //if the first char of the token upper case.
            if (token.charAt(0) <= 90 && token.charAt(0) >= 65) {
                if (!dicDoc.containsKey(token.toLowerCase())) {
                    addToDicDoc(dicDoc, token.toUpperCase());
                }
                else addToDicDoc(dicDoc, token.toLowerCase());
            }
            //if all chars in token are lower case
            else if(token.charAt(0)>=97 && token.charAt(0)<=122) {
                if (dicDoc.containsKey(token.toUpperCase())) {
                    int[] valueOfTermInDicDoc = dicDoc.get(token.toUpperCase());
                    valueOfTermInDicDoc[0]++;
                    dicDoc.put(token.toLowerCase(), valueOfTermInDicDoc);
                    dicDoc.remove(token.toUpperCase());
                } else addToDicDoc(dicDoc, token.toLowerCase());
            }
            return 0;
        }
        return -1;
    }

    /**
     * convert number to number K/M/B
     *
     * @param number
     * @param numberAfterDot
     * @param isDouble
     * @param nextToken
     * @return the converted number
     */
    private String numberToKMB(String number, String numberAfterDot, boolean isDouble, String nextToken) {
        String term = "";
        String kind = "";
        //less than 1000
        if (number.length() >= 1 && number.length() <= 3) {
            term = number;
            if (isDouble) {
                term += '.' + numberAfterDot;
            } else if (checkIfLegalFraction(nextToken)) {
                term += " " + nextToken;
            }
        } else {
            //Thousand(included) to Million(not included)
            if (number.length() >= 4 && number.length() <= 6) {
                kind = "K";
                term = CreatingNumberForDictionary(number, 3);
                if (!isDouble)
                    term = reduceZeros(term, 3);
            }
            //Million(included) to Billion(not included)
            else if (number.length() >= 7 && number.length() <= 9) {
                kind = "M";
                term = CreatingNumberForDictionary(number, 6);
                if (!isDouble)
                    term = reduceZeros(term, 6);
            }
            //more than Billion(included)
            else if (number.length() >= 10) {
                kind = "B";
                term = CreatingNumberForDictionary(number, 9);
                if (!isDouble)
                    term = reduceZeros(term, 9);
            }
            if (isDouble) {
                for (int i = 0; i < numberAfterDot.length(); i++) {
                    term += numberAfterDot.charAt(i);
                }
            }
            term += kind;
        }
        return term;
    }

    //check if string is one of this options: thousands million billion franction(3/4)
    private boolean isItNumberFromSectionA(String s) {
        if (s == null || s.equals(""))
            return false;
        //if is a legal fraction
        if (checkIfLegalFraction(s))
            return true;
        //if is one of this words
        if (s.equals("Thousand") || s.equals("Million") || s.equals("Billion") || s.equals("Trillion"))
            return true;
        return false;
    }

    /**
     * Ranges / expressions with hyphen will be added to the dictionary as a single term.
     *
     * @param nextT
     * @param currT
     * @return
     */
    private int expressionAndRangesTerm(String nextT, String currT, String nextnextT, int currIndex, String[] currDoc, HashMap dicDoc) {
        String[] exp;
        //like 123 Thousand-124, 123 Thousand-124 Thousand, 123 Thousand-word
        if (checkIfOnlyDigitsDotsComma(currT) && nextT.contains("-")) {
            exp = nextT.split("-");//thousand,124
            String leftExp;
            //if the currT (current token) related to the "- expression"
            if (exp != null && exp.length > 0 && isItNumberFromSectionA(exp[0])) {
                leftExp = regularNumberTerms2(currT, exp[0]);
                //like 123 T-124 thousand
                if (exp.length == 2 && regularNumberTerms2(exp[1], nextnextT) != regularNumberTerms2(exp[1], "stam")) {
                    String rightExp = regularNumberTerms2(exp[1], nextnextT);
                    addToDicDoc(dicDoc, leftExp + "-" + rightExp);
                    return 2;
                    //like 123 T-124 or 123 T-word.
                } else {
                    if (exp.length == 2) {
                        addToDicDoc(dicDoc, leftExp + "-" + exp[1]);
                        return 1;
                    }
                }
            }
        }
        //like 123-123 2/3
        else if (currT.contains("-")) {
            exp = currT.split("-");
            //if like 123/123 T
            if (exp.length == 2 && isItNumberFromSectionA(nextT)) {
                String rightExp = regularNumberTerms2(exp[1], nextT);
                addToDicDoc(dicDoc, exp[0] + "-" + rightExp);
                return 2;
            }
        }
        //like: word-word, word-word-word
        else if (currT.contains("-")) {
            exp = currT.split("-");
            if (exp.length == 2 && checkIfOnlyLetters(exp[0]) && checkIfOnlyLetters(exp[1])) {
                addToDicDoc(dicDoc, currT);
                return 0;
            }
            if (exp.length == 3 && checkIfOnlyLetters(exp[0]) && checkIfOnlyLetters(exp[1]) && checkIfOnlyLetters(exp[2])) {
                addToDicDoc(dicDoc, currT);
                return 0;
            }
        }
        //like Between number1 and number2
        if (currT.equals("Between") && checkIfOnlyDigitsDotsComma(nextT)) {
            int jumps = 3;
            String Number1;
            String Number2;
            String Number2first;
            String Number2Second;
            boolean and = false;
            //if number1 is two words: 123 Thousand or 123 2/3...
            if (isItNumberFromSectionA(nextnextT)) {// Between 123 Thousand and number
                jumps++;
                Number1 = regularNumberTerms2(nextT, nextnextT);
            }
            //if number1 is one word
            else {
                Number1 = nextT;
            }
            //if it really Between number and number expression
            if (jumps == 3 && nextnextT.equals("and") || jumps == 4 && getNextToken(currDoc, currIndex + 2).equals("and")) {
                if (jumps == 3) {//if number1 is one word
                    Number2first = getNextToken(currDoc, currIndex + 2);
                    Number2Second = getNextToken(currDoc, currIndex + 3);
                } else {//if number 1 is two words
                    Number2first = getNextToken(currDoc, currIndex + 3);
                    Number2Second = getNextToken(currDoc, currIndex + 4);
                }
                //if number2 is two words
                if (checkIfOnlyDigitsDotsComma(Number2first)) {
                    if (isItNumberFromSectionA(Number2Second)) {
                        jumps++;
                        Number2 = regularNumberTerms2(Number2first, Number2Second);
                    }
                    //if number 2 is one word number
                    else
                        Number2 = Number2first;
                    addToDicDoc(dicDoc, Number1 + "-" + Number2);
                    return jumps;
                }
            }
        }
        //regular word-word, number-word, word-number
        else if (currT.contains("-") && currT.length() >= 3) {
            addToDicDoc(dicDoc, currT);
            return 0;
        }
        return -1;
    }

    /**
     * taken from url :https://stackoverflow.com/questions/5238491/check-if-string-contains-only-letters
     * check whether a string combine only from letters.
     *
     * @param toCheck string to check
     * @return if only letters true else false.
     */
    private boolean checkIfOnlyLetters(String toCheck) {
        char[] chars = toCheck.toCharArray();
        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /*
     * check whether a given string contain only digits, dots, commas and slashes (if its a legal number).
     * @param number - string to check
     * @return - true if contain only numbers else false.
     */
    private boolean checkIfOnlyDigitsDotsComma(String number) {
        int dot = 0, slash = 0, minus = 0, comma = 0, digits = 0;
        boolean legalNumber = false;
        int i = 0;
        if (number == null || number.equals(""))
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
            } else if (c == '.') {
                dot++;
                legalNumber = false;
            } else if (c == '/') {
                slash++;
                legalNumber = false;
            } else if (c == '-') {
                minus++;
                legalNumber = false;
            } else if (c == ',') {
                comma++;
                legalNumber = false;
            }
            if (dot > 1 || slash > 1 || minus > 1)
                return false;
        }
        if (comma > 0 && digits / comma < 3)
            return false;
        if (!legalNumber)
            return false;
        return true;
    }


    public void setCitiesFromTags(HashSet<String> citiesFromTags) {
        this.citiesFromTags = citiesFromTags;
    }
}

