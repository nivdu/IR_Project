package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Parse {
    //hashSet for all the stop words.
    HashSet<String> stopWords;
    //HashSet for month names
    HashMap<String, String> months;
    //stop Word Path
    String stopWordPath;
    //true if stem is needed.
    boolean stemmer;
    /**
     * Parse Constructor.
     */
    public Parse(boolean stemmer, String stopWordPath) {
        this.stemmer=stemmer;
        this.stopWordPath = stopWordPath;
        this.stopWords = new HashSet<String>();//todo in the constructor build from the StopWords File the HashSet of stop words.
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


    public document parseDoc(String[] splitedDoc, String city, String docId) {
        int jump = 0;
        //temporary dictionary to find the max tf in current doc.
        HashMap<String, Integer> dicDoc = new HashMap<>();//todo at the end of the loop check the tf and everything
        //loop over all the tokens in current doc.
        boolean isAddToDic = false;
        //delete start and end char punctutations from the terms.
        for (int j = 0; j < splitedDoc.length; j++) {
            splitedDoc[j] = deletePunctutations(splitedDoc[j]);
        }
        for (int currDocIndex = 0; currDocIndex < splitedDoc.length; currDocIndex++) {
            if (splitedDoc[currDocIndex].equals("") || splitedDoc[currDocIndex].equals("\n"))
                continue;
            String currToken = splitedDoc[currDocIndex];
            //continue to next token when it stop Word
            if (deleteStopWords(currToken))
                continue;
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
                currDocIndex+=jump;
                continue;
            }

            jump = DollarTermMoreThanMillion(currToken, nextNextToken, nextNextToken, nextNextNextToken, dicDoc);
            if (jump != -1) {
                currDocIndex+=jump;
                continue;
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

            //check regularNumberTerms function.
            jump = regularNumberTerms(currToken, nextToken, dicDoc);
            if (jump != -1) {
                currDocIndex+=jump;
                continue;
            }

            //check bigsmall letter cases
            jump = addOnlyLettersWords(currToken, dicDoc);
            if (jump != -1) {
                currDocIndex+=jump;
                continue;
            }

            //add token that didn't match any case. (like f16 and a lot more cases).
            if(currToken.length()>1)
                addToDicDoc(dicDoc, currToken);

            //cases like word/word or word/word/word or word.word or word.[word].
//            String[] toAdd = currToken.split("[?!:;#@^+&{}*|<=/>\"\\.]");
            String[] toAdd = currToken.split(" |\\/|\\.");
            if(toAdd.length>1)
                for (String s:toAdd){
                s = deletePunctutations(s);
                if(s.length()>1)
                    addToDicDoc(dicDoc,s);
                }




            //todo use stemmer.
        }
        int tf = getMaxTF(dicDoc);
        int maxUnique = getMaxUnique(dicDoc);
        return new document(docId, tf, maxUnique, city, dicDoc);
    }

    /**
     * add term into the current document dictionary and increase by 1 the tf value in dicDoc.
     * @param dicDoc - doc dictionary
     * @param term - term to add to dicDoc
     */
    private void addToDicDoc(HashMap<String,Integer> dicDoc, String term){
        if(dicDoc.containsKey(term)){
            int tf = dicDoc.get(term);
            dicDoc.put(term,tf+1);
        }
        else{
            dicDoc.put(term,1);
        }
    }

    /**
     * iterator over the dicDoc hashmap and find the max tf.
     * Link : https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
     */
    private int getMaxTF(HashMap<String,Integer> dicDoc){//todo check this function
        int max = 0;
        Iterator it = dicDoc.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            int temp = (int)pair.getValue();
            if(temp > max)
                max = temp;
//            it.remove(); // avoids a ConcurrentModificationException
            }
            return max;
    }

    /**
     * return the size of dicDoc - the number of unique words from current dictionary of document.
     */
    private int getMaxUnique(HashMap<String, Integer> dicDoc){
        return dicDoc.size();
    }

    /**
     * from url: https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
     * read file to String line by line
     * @param file - The path of the file to read into String
     * @return - String of the context of the file
     */
    private String file2String(File file)
    {
        StringBuilder contentBuilder = new StringBuilder();
        {
            String content = "";

            try
            {
                content = new String ( Files.readAllBytes( Paths.get(file.toPath().toString()) ) );
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
        String stopWordsPath = "C:\\Users\\nivdu\\Desktop\\StopWords";//todo what is the path for stopWords text file
        File file = new File(stopWordsPath);
        String wholeFileString="";
        for (final File fileOfDocs: file.listFiles())
            wholeFileString = file2String(fileOfDocs);
        if(!wholeFileString.equals("")) {
            String[] currStopWord = wholeFileString.split("\r\n");
            for (String SW : currStopWord)
                stopWords.add(SW);
        }
    }


    private String deletePunctutations(String toCheck){
        if(toCheck!=null && toCheck!="" && toCheck.length()>1) {
            if(toCheck.equals("U.S."))
                return toCheck;
            int toCheckLength = toCheck.length() - 1;
            if (toCheck.charAt(0) == '\n' || toCheck.charAt(0) == '"'|| toCheck.charAt(0) == '(' || toCheck.charAt(0) == ',' || toCheck.charAt(0) == ':' || toCheck.charAt(0) == '.' || toCheck.charAt(0) == '-' || toCheck.charAt(0) == '|' || toCheck.charAt(0) == '`' || toCheck.charAt(0) == '\'' || toCheck.charAt(0) == '[' || toCheck.charAt(0) == ']' || toCheck.charAt(0) == ';' || toCheck.charAt(0) == '?' || toCheck.charAt(0) == '/' || toCheck.charAt(0) == '<') {
                toCheck = toCheck.substring(1);
                toCheck = deletePunctutations(toCheck);
            }
            toCheckLength = toCheck.length() - 1;
            if (toCheck != "" && toCheck.length()>0 && (toCheck.charAt(toCheckLength) == ',' || toCheck.charAt(toCheckLength)=='"' ||  toCheck.charAt(toCheckLength) == ')' || toCheck.charAt(toCheckLength) == '.' || toCheck.charAt(toCheckLength) == ':' || toCheck.charAt(toCheckLength) == '"' || toCheck.charAt(toCheckLength) == '-' || toCheck.charAt(toCheckLength) == '|' || toCheck.charAt(toCheckLength) =='`' || toCheck.charAt(toCheckLength) ==']' || toCheck.charAt(toCheckLength) =='[' || toCheck.charAt(toCheckLength) =='\'' || toCheck.charAt(toCheckLength) ==';' || toCheck.charAt(toCheckLength) =='?' || toCheck.charAt(toCheckLength) =='/' || toCheck.charAt(toCheckLength) =='>')) {
                toCheck = toCheck.substring(0, toCheckLength);
                toCheck = deletePunctutations(toCheck);
            }
        }
        return toCheck;
    }

    /**
     * the function check if a word is stop word or not from stopWords HastSet.
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
     * @param nextT - the next token to check in the curr doc.
     * @param token - the curr token to check in the curr doc.
     * @return - -1 if not added to the dic else return the number of words used from the doc
     */

    private int percentageTerm(String nextT, String token, HashMap dicDoc) {//todo check for %-number
        if (token == null || token.length() <= 0)//todo what to do if the token is null?
            return -1;
        //cases like 6%
        if (token.charAt(token.length() - 1) == '%') {
            //substring from number% to number (6% to 6)
            String checkIfNumber = token.substring(0, token.length() - 1);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if (checkIfOnlyDigitsDotsComma(checkIfNumber)) {
                addToDicDoc(dicDoc,token);
                return 0;
            }
        }
        //cases like "6 percent", "6 percentage"
        else if (nextT != null && nextT != "" && (nextT.equals("percent") || nextT.equals("percentage"))) {
            //if before the percent/percentage there is only digits and docs add to the dictionary the whole token combine with '%'.

            if (checkIfOnlyDigitsDotsComma(token)) {
                addToDicDoc(dicDoc,token + '%');
                return 1;
            }
        }
        return -1;
    }

    /**
     * check whether a token is date term.
     * @param nextT - the next token in the doc
     * @param token - curr token in the doc
     * @return - true if the token are date token type.
     */
    private int DateTerm(String nextT, String token, HashMap dicDoc) {
        //todo check if token is empty null and shit
        if (checkIfOnlyDigitsDotsComma(token) && token.length()<4 && months.containsKey(nextT)) {//todo maby check if only numbers without digits and dots.
            if(token.length()==1)
                token = "0" + token;
            String toAdd = months.get(nextT) + "-" + token;
            addToDicDoc(dicDoc,toAdd);
            return 1;
        }
        else if(checkIfOnlyDigitsDotsComma(nextT) && token.length()<4 && months.containsKey(token)){//todo maby check if only numbers without digits and dots.
            if(nextT.length()==1)
                nextT = "0" + nextT;
            String toAdd = months.get(token) + "-" + nextT;
            addToDicDoc(dicDoc,toAdd);
            return 1;
        } else if (months.containsKey(token) && checkIfOnlyDigitsDotsComma(nextT)) {//todo maby check if only numbers without digits and dots.
            String toAdd = nextT + "-" + months.get(token);
            addToDicDoc(dicDoc,toAdd);
            return 1;
        }
        return -1;
    }


    /**
     * check if a string is a legal fraction
     * @param token - string to check
     * @return - true if legal fraction else false.
     */
    private boolean checkIfLegalFraction(String token) {
        if(token==null || token.equals(""))
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
        if(slashCounter==1)
            return true;
        return false;
    }

    /**
     * check whether a given string contain only digits(numbers)
     * @param number - string to check
     * @return - true if contain only numbers else false.
     */

    private boolean checkIfOnlyDigitsDotsComma(String number) {
        if(number==null || number.equals(""))
            return false;
        for (Character c : number.toCharArray()) {
            if (!Character.isDigit(c) && !(c.equals('.')) && !(c.equals(',')))
                return false;
        }
        return true;
    }

    /**
     * return the next token from currDoc
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
        if(number.length()>=4 && number.length()<=6)
            return "K";
        if(number.length()>=7 && number.length()<=9)
            return "M";
        if(number.length()>=10)
            return "B";
        return "U";//less than thousand
    }

    /**
     * every price which is less than million dollars will represent as number Dollars
     * @param nextT - the next token
     * @param nextNextT - the next next token
     * @param token - the current token
     * @return - true if the term keep the given laws
     */
    private int DollarTermLessThanMillion(String token ,String nextT, String nextNextT, HashMap dicDoc) {
        if (token == null || token.length() <= 0)//todo what to do if the token is null?
            return -1;
        //cases like $1000
        if (token.charAt(0) == '$') {
            String checkIfNumber = token.substring(1);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if (checkIfOnlyDigitsDotsComma(checkIfNumber)) {
                addToDicDoc(dicDoc,checkIfNumber + " " + "Dollars");
                return 0;
            }
        }
        String kind = checkIfKMBU(token);
        if(!checkIfOnlyDigitsDotsComma(token) || !(kind.equals("K") || kind.equals("U")))
            return -1;
        //cases like 123 Dollars
        if(nextT.equals("Dollars")){
            addToDicDoc(dicDoc,token+" "+nextT);
            return 1;
        }
        //cases like 123 3/4 Dollars
        if(checkIfLegalFraction(nextT)&&nextNextT.equals("Dollars")){
            addToDicDoc(dicDoc,token+" "+nextT+" "+nextNextT);
            return 2;
        }
        return -1;
    }

    /**
     * every price which is more than million dollars will represent as number M Dollars
     * @param token - the curren token
     * @param nextT - the next token
     * @param nextNextT- the next next token
     * @param nextNextNextT- the next next next token
     * @return- true if the term keep the given laws
     */
    private int DollarTermMoreThanMillion(String token, String nextT, String nextNextT, String nextNextNextT, HashMap dicDoc){
        if(token.equals(""))
            return -1;
        boolean tokenIsNumber = checkIfOnlyDigitsDotsComma(token);
        boolean tokenIsMoreThanMillion=false;
        if(tokenIsNumber)
            tokenIsMoreThanMillion=isMoreThanMillion(token);
        String term = "";
        //cases like _____ Dollars
        if(nextT.equals("Dollars")){
            //cases like 1000000 Dollars
            if(tokenIsNumber && tokenIsMoreThanMillion){
                term = regularNumberTerms2(token,nextT);
                term = convertingToMillionForDollars(term);
                term+=" "+nextT;
                addToDicDoc(dicDoc, term);
                return 1;
            }
            //cases like 20.6m Dollars
            if(token.charAt(token.length()-1)=='m'){
                String tokenWithoutM = token.substring(0,token.length()-1);
                if(checkIfOnlyDigitsDotsComma(tokenWithoutM)){
                    term=tokenWithoutM+" M "+nextT;
                    addToDicDoc(dicDoc, term);
                    return 1;
                }
            }
            //cases like 100bn Dollars
            if(token.length()>=2 && token.charAt(token.length()-2)=='b' && token.charAt(token.length()-1)=='n'){
                String tokenWithoutBN = token.substring(0,token.length()-2);
                if(checkIfOnlyDigitsDotsComma(tokenWithoutBN)){
                    term=tokenWithoutBN+"000"+" M "+nextT;
                    addToDicDoc(dicDoc, term);
                    return 1;
                }
            }
        }
        if(token.charAt(0)=='$'){
            boolean tokenIsNumberWithout$ = checkIfOnlyDigitsDotsComma(token.substring(1));
            //cases like $100 million
            if(nextT.equals("million") && tokenIsNumberWithout$){
                String termWithoutCommas = deletingCommasFromNumbers(token.substring(1));
                addToDicDoc(dicDoc,termWithoutCommas+" M Dollars");
                return 1;
            }
            //cases like $100 billion
            if(nextT.equals("billion") && tokenIsNumberWithout$){
                String termWithoutCommas = deletingCommasFromNumbers(token.substring(1));
                addToDicDoc(dicDoc,termWithoutCommas+"000"+" M Dollars");
                return 1;
            }
            //cases like $1000000
            boolean tokenIsMoreThanMillionWithout$=false;
            if (tokenIsNumberWithout$ )
                tokenIsMoreThanMillionWithout$ =isMoreThanMillion(token.substring(1));
            if(tokenIsMoreThanMillionWithout$){
                term=regularNumberTerms2(token,nextT);
                term = convertingToMillionForDollars(term);
                addToDicDoc(dicDoc, term+" Dollars");
                return 0;
            }
        }
        //cases like number _____ U.S. dollars
        if(tokenIsNumber && nextNextT.equals("U.S.") && nextNextNextT.equals("dollars")){
            String tokenWithoutComma = deletingCommasFromNumbers(token);
            //cases like number million U.S. dollars
            if(nextT.equals("million")){
                addToDicDoc(dicDoc,tokenWithoutComma+" M Dollars");
                return 3;
            }
            //cases like number billion U.S. dollars
            if(nextT.equals("billion")){
                addToDicDoc(dicDoc, tokenWithoutComma+"000"+" M Dollars");
                return 3;
            }
            //cases like number trillion U.S. dollars
            if(nextT.equals("trillion")){
                addToDicDoc(dicDoc,tokenWithoutComma+"000000"+" M Dollars");
                return 3;
            }
        }
        return -1;
    }

    /**
     * deleting commas from number
     * @param number - the number with commas
     * @return the number without commas
     */
    private String deletingCommasFromNumbers(String number) {
        String ans="";
        for (int i = 0; i < number.length(); i++) {
            if(number.charAt(i)==',')
                continue;
            ans+=number.charAt(i);
        }
        return ans;
    }


    private boolean isMoreThanMillion(String token){
        String kind = checkIfKMBU(token);
        if(kind.equals("M") || kind.equals("B")){
            return true;
        }
        return false;
    }

    private String convertingToMillionForDollars(String term) {
        if(term.charAt(term.length()-1)=='M'){
            String ans=term.substring(0,term.length()-1);
            return ans+" M";
        }
        else if(term.charAt(term.length()-1)=='B'){
            String ans=term.substring(0,term.length()-1);
            if(!ans.contains(".")){
                ans+="000";
            }
            else{
                String beforeDot="";
                String afterDot="";
                boolean after=false;
                for (int i = 0; i < ans.length(); i++) {
                    if(!after && ans.charAt(i)=='.')
                        after=true;
                    else if(!after && Character.isDigit(ans.charAt(i))){
                        beforeDot+=ans.charAt(i);
                    }
                    else if(after &&  Character.isDigit(ans.charAt(i))){
                        afterDot+=ans.charAt(i);
                    }
                }
                ans=beforeDot;
                if(3-afterDot.length()>=0){
                    ans+=afterDot;
                    for (int i = 0; i < 3-afterDot.length(); i++) {
                        ans+="0";
                    }
                }
                else{
                    for (int i = 0; i < afterDot.length(); i++) {
                        ans+=afterDot.charAt(i);
                        if(i==2){
                            ans+=".";
                        }
                    }
                }
            }
            return ans+" M";
        }
        return "";
    }


    /**
     * split the number to two parts - before dot and after dot
     * @param token - the token in the document
     * @return true if legal number
     */
    private int regularNumberTerms(String token, String nextToken, HashMap dicDoc) {
        String term = regularNumberTerms2(token,nextToken);
        if(!term.equals("")){
            if(nextToken.equals("Thousand") || nextToken.equals("Million") ||nextToken.equals("Billion") ||nextToken.equals("Trillion")){
                addToDicDoc(dicDoc, term);
                return 1;
            }
            addToDicDoc(dicDoc, term);
            return 0;
        }
        return -1;
    }


    private String regularNumberTerms2(String token, String nextToken) {
        String number = "";
        String numberAfterDot = "";
        boolean isDouble = false;
        boolean isNumber = false;
        boolean isNegative=false;
        int countCommas = 0;
        for (int i = 0; i < token.length(); i++) {
            if (i == 0 && token.charAt(0) == '-') {
                isNegative=true;
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

        if(isNumber && isNegative)
            return "-"+term;
        else if(isNumber && !isNegative)
            return term;
        return "";
    }

    /**
     * reduce non-relevant zeros from the end
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
    private int addOnlyLettersWords(String token, HashMap dicDoc){
        if (token!=null && token!="" && (Pattern.matches("[a-zA-Z]+", token))){
            //if the first char of the token upper case.
            if(token.charAt(0)<=90 && token.charAt(0)>=65){
                if(!dicDoc.containsKey(token.toLowerCase())) {
                    addToDicDoc(dicDoc, token.toUpperCase());

                }
            }
            else if(token.equals(token.toLowerCase()))
                if(dicDoc.containsKey(token.toUpperCase())) {
                    dicDoc.remove(token.toUpperCase());
                    addToDicDoc(dicDoc, token) ;
                }
                else addToDicDoc(dicDoc, token);
            return 0;
        }
        return -1;
    }

    /**
     * convert number to number K/M/B
     * @param number
     * @param numberAfterDot
     * @param isDouble
     * @param nextToken
     * @return the converted number
     */
    private String numberToKMB(String number, String numberAfterDot, boolean isDouble, String nextToken) {
        String term="";
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
    private boolean isItNumberFromSectionA(String s){
        if(s==null || s.equals(""))
            return false;
        //if is a legal fraction
        if(checkIfLegalFraction(s))
            return true;
        //if is one of this words
        if(s.equals("Thousand") || s.equals("Million") || s.equals("Billion") || s.equals("Trillion"))
            return true;
        return false;
    }


    /**
     * Ranges / expressions with hyphen will be added to the dictionary as a single term.
     * @param nextT
     * @param currT
     * @return
     */
    private int expressionAndRangesTerm(String nextT, String currT, String nextnextT, int currIndex, String[] currDoc, HashMap dicDoc){
        String[] exp;
        //like 123 Thousand-124, 123 Thousand-124 Thousand, 123 Thousand-word
        if(checkIfOnlyDigitsDotsComma(currT) && nextT.contains("-")) {
            exp = nextT.split("-");//thousand,124
            String leftExp;
            //if the currT (current token) related to the "- expression"
            if (exp!=null && exp.length>0 && isItNumberFromSectionA(exp[0])) {
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
        else if(currT.contains("-")) {
            exp = currT.split("-");
            //if like 123/123 T
            if(exp.length==2 && isItNumberFromSectionA(nextT)){
                String rightExp = regularNumberTerms2(exp[1],nextT);
                addToDicDoc(dicDoc, exp[0] + "-" + rightExp);
                return 2;
            }
        }
        //like: word-word, word-word-word
        else if(currT.contains("-")){
            exp = currT.split("-");
            if (exp.length==2 && checkIfOnlyLetters(exp[0]) && checkIfOnlyLetters(exp[1])){
                addToDicDoc(dicDoc, currT);
                return 0;
            }
            if(exp.length==3 && checkIfOnlyLetters(exp[0]) && checkIfOnlyLetters(exp[1]) && checkIfOnlyLetters(exp[2])){
                addToDicDoc(dicDoc, currT);
                return 0;
            }
        }
        //like Between number1 and number2
        if(currT.equals("Between") && checkIfOnlyDigitsDotsComma(nextT)) {
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
                    addToDicDoc(dicDoc, Number1 + "-" + Number2);//todo check how much jumps to do in the parsdoc function! the number of jumps saved in integer jumps.
                    return jumps;
                }
            }
        }
        //regular word-word, number-word, word-number
        else if(currT.contains("-") && currT.length()>= 3){
            addToDicDoc(dicDoc, currT);
            return 0;
        }
        return -1;
    }

    /**
     * taken from url :https://stackoverflow.com/questions/5238491/check-if-string-contains-only-letters
     * check whether a string combine only from letters.
     * @param toCheck string to check
     * @return if only letters true else false.
     */
    private boolean checkIfOnlyLetters(String toCheck){
        char[] chars = toCheck.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
//        Parse parse = new Parse();
//        String[] text = new String[2];
//        text[0]="-";
//        text[1]="";
//        parse.parseDoc(text);

//        parse.DollarTermMoreThanMillion("1000000","Dollars","","");
//        parse.DollarTermMoreThanMillion("20.6m","Dollars","","");
//        parse.DollarTermMoreThanMillion("100bn","Dollars","","");
//        parse.DollarTermMoreThanMillion("$4500000","Dollars","","");
//        parse.DollarTermMoreThanMillion("$100","million","","");
//        parse.DollarTermMoreThanMillion("$100","billion","","");
//        parse.DollarTermMoreThanMillion("100","billion","U.S.","dollars");
//        parse.DollarTermMoreThanMillion("320","million","U.S.","dollars");
//        parse.DollarTermMoreThanMillion("1","trillion","U.S.","dollars");
//        for (String term:parse.dictionary) {
//            System.out.println(term);
//        }
//
//        String ans = parse.convertingToMillionForDollars("12.3456B");
//        System.out.println(ans);

      //        String test="123";
//        System.out.println(test.substring(0,2));
//        parse.percentageTerm("percentage","6");
//        parse.DateTerm("MAY","14");
//        parse.DateTerm("May","4");
//        parse.DateTerm("15","MAY");
//        parse.DateTerm("1994","April");
//        parse.addOnlyLettersWords("Loren");

//        parse.addOnlyLettersWords("Loren");
//
//        String testSW = "``c-";
//        testSW = parse.deletePunctutations(testSW);
//        System.out.println(testSW);
//        parse.getStopWordsIntoHastSet();
//        parse.deleteStopWords(testSW);
//        String[] currDoc = "Between 123 and 123 Thousand".split(" ");
//        parse.expressionAndRangesTerm("126", "125-126", "Thousand", 0,currDoc);
//        for (String s:parse.dictionary){
//            System.out.println(s);
//        }




/*
//        parse.regularNumberTerms("123","456/2345");
        parse.DollarTermLessThanMillion("l","wer","$123");
        parse.DollarTermLessThanMillion("Dollars","wer","123.2");
        parse.DollarTermLessThanMillion("3/4","Dollars","123");
        parse.DollarTermLessThanMillion("loren","Dollars","123");
        for (String term:parse.dictionary) {
            System.out.println(term);
        }
*/
///    parse.numberKMB("1000","3/4");
//        parse.numberKMB("-50","Thousand");
//        parse.numberKMB("50.2","Thousand");
//
//
//
//        parse.numberKMB("123","loren");
//        parse.numberKMB("123.23","loren");
//        //check k
//        System.out.println();
//        parse.numberKMB("-1000","loren");
//        parse.numberKMB("1234","loren");
//        parse.numberKMB("-1234.56","loren");
//        parse.numberKMB("1,234.56","loren");
//        parse.numberKMB("123,456","loren");
//        //check m
//        System.out.println();
//        parse.numberKMB("-1000000","loren");
//        parse.numberKMB("123456789");
//        parse.numberKMB("123,456,789");
//        parse.numberKMB("123,456,789.12");
//        parse.numberKMB("1,234,567.56");
//        //check B
//        System.out.println();
//        parse.numberKMB("123456789123456");
//        parse.numberKMB("1234567891.23");
//


   
    }
}