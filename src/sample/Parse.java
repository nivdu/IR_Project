package sample;

import javax.xml.soap.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Parse {
    //hashSet for all the stop words.
    HashSet<String> stopWords;
    //dictionary contain all the terms from the docs
    HashSet<String> dictionary;
    //HashSet for month names
    HashMap<String,String> months;
    /**
     * Parse Constructor.
     */
    public Parse() {
        this.stopWords = new HashSet<String>();
        this.dictionary = new HashSet<String>();
        this.months = new HashSet<String>();
        createMonthHS();
    }

    /**
     * add to the month HS all the months names.
     */
    private void createMonthHS(){
        months.put("January","01");
        months.put("JANUARY","01");
        months.put("February","02");
        months.put("FEBRUARY","02");
        months.put("March","03");
        months.put("MARCH","03");
        months.put("April","04");
        months.put("APRIL","04");
        months.put("MAY","05");
        months.put("May","05");
        months.put("June","06");
        months.put("JUNE","06");
        months.put("July","07");
        months.put("JULY","07");
        months.put("August","08");
        months.put("AUGUST","08");
        months.put("September","09");
        months.put("SEPTEMBER","09");
        months.put("October","10");
        months.put("OCTOBER","10");
        months.put("November","11");
        months.put("NOVEMBER","11");
        months.put("December","12");
        months.put("DECEMBER","12");
    }


    public void parseDoc(String[] currDoc) {
        for (String doc:currDoc) {
            //temporary dictionary to find the max tf in current doc.
            HashMap<String, Integer> FindMaxTf;//todo at the end of the loop check the tf and everything
            //split by spaces
            String[] splitedDoc = doc.split(" ");

            //loop over all the tokens in current doc.
            boolean isAddToDic=false;
            for (int currDocIndex = 0; currDocIndex < splitedDoc.length ; currDocIndex++) {
                //if current word is stop word continue to the next word.
                String currToken = splitedDoc[currDocIndex];
                if (deleteStopWords(currToken))
                    continue;
                //get prev and next tokens if there isn't next token (the end of the array) return nextToken="", if index==0 return prevToken="".
                String nextToken = getNextToken(splitedDoc, currDocIndex);
                String nextNextToken = getNextToken(splitedDoc, currDocIndex+1);
                String prevToken = getNextToken(splitedDoc, currDocIndex);
                //check percentageTerms function
                isAddToDic = percentageTerm(nextToken, currToken);
                if (isAddToDic && nextToken.equals("percentage") || nextToken.equals("percent")) {
                    currDocIndex++;
                    continue;
                }
                else if(isAddToDic)
                    continue;

                //check DollarTerm function.
                if(DollarTerm(nextNextToken, nextNextToken, currToken));
                //check numberKBMTerms function.
                if(numberKMB(currToken,nextToken))
                    continue;

                //todo use stemmer.
            }
        }
    }

    /**
     * get the stopWords from the stopWords file and insert them into stopWords hashSet.
     */
    private void getStopWordsIntoHastSet(){
        //insert all the stop words from stop words file into HashSet.
        //scanner function from link: "https://stackoverflow.com/questions/30011400/splitting-textfile-at-whitespace"
        String stopWordsPath = "";//todo what is the path for stopWords text file
        File file = new File(stopWordsPath);
        try {
            Scanner scanner = new Scanner(file);
            //now read the file line by line...
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] currStopWord = line.split(" ");//todo maybe handle the '/n' i didn't check it.
                stopWords.add(currStopWord[0]);
            } }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
     * @return - true if the tokens/token are percentage token type.
     */
    private boolean percentageTerm(String nextT, String token){//todo check for %-number
        if(token==null || token.length()<=0)//todo what to do if the token is null?
            return false;
        //cases like 6%
        if(token.charAt(token.length()-1)=='%'){
            //substring from number% to number (6% to 6) todo check if length-2 its ok.
            String checkIfNumber = token.substring(0,token.length()-2);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if(checkIfOnlyDigitsDotsComma(checkIfNumber)) {
                dictionary.add(token);
                return true;
            }
        }
        //cases like "6 percent", "6 percentage"
        else if(nextT!=null && nextT!="" &&(nextT.equals("percent") || nextT.equals("percentage"))){
            //if before the percent/percentage there is only digits and docs add to the dictionary the whole token combine with '%'.
            if(checkIfOnlyDigitsDotsComma(token)) {
                dictionary.add(token + '%');
                return true;
            }
        }
        return false;
    }

    /**
     * check whether a token is date term
     * @param nextT - the next token in the doc
     * @param token - curr token in the doc
     * @return - true if the token are date token type.
     */
    private boolean DateTerm(String nextT, String token) {
        //todo check if token is empty null and shit
        if (checkIfOnlyDigitsDotsComma(token) && token.length()<4 && months.containsKey(nextT)) {//todo maby check if only numbers without digits and dots.
            String toAdd = months.get(nextT) + "-" + token;
            dictionary.add(toAdd);
            return true;
        }
        else if(checkIfOnlyDigitsDotsComma(nextT) && token.length()<4 && months.containsKey(token)){//todo maby check if only numbers without digits and dots.
            String toAdd = months.get(token) + "-" + nextT;
            dictionary.add(toAdd);
            return true;
        }
        else if(months.containsKey(token) && checkIfOnlyDigitsDotsComma(nextT)) {//todo maby check if only numbers without digits and dots.
            String toAdd = nextT + "-" + months.get(token);
            dictionary.add(toAdd);
            return true;
        }
        return false;
    }


    /**
     * check if a string is a legal fraction
     * @param token - string to check
     * @return - true if legal fraction else false.
     */
    private boolean checkIfLegalFraction(String token){
        int slashCounter=0;
        if(!Character.isDigit(token.charAt(0)))
            return false;
        for (int i = 0; i < token.length() ; i++) {
            char c = token.charAt(i);
            if(c=='/') {
                slashCounter++;
                if(slashCounter!=1)
                    return false;
                if (i+1>=token.length() || !Character.isDigit(token.charAt(i + 1)))
                    return false;
                continue;
            }
            else if(!Character.isDigit(c))
                return false;
        }
        return true;
    }

    /**
     * check whether a given string contain only digits(numbers)
     * @param number - string to check
     * @return - true if contain only numbers else false.
     */
    private boolean checkIfOnlyDigitsDotsComma(String number){
        for (Character c:number.toCharArray()){
            if(!Character.isDigit(c) && !(c.equals('.')) && !(c.equals(',')))
                return false;
        }
        return true;
    }

    /**
     * return the next token from currDoc
     * @param currDoc - the curr doc
     * @param index - current token index
     * @return - String type of the token at location index +1. if index is the last of the array return empty string.
     */
    private String getNextToken(String[] currDoc, int index){
        if(index+1>=currDoc.length)
            return "";
        return currDoc[index+1];
    }

    /**
     * return the prev token from currDoc
     * @param currDoc - the curr doc
     * @param index - current token index
     * @return - String type of the token at location index -1. if index ==0 return empty string.
     */
    private String getPrevToken(String[] currDoc, int index){
        if(index-1<0)
            return "";
        return currDoc[index-1];
    }

    /**
     * creating terms of numbers according to:
     *
     * @param token
     */
    public boolean numberKMB(String token, String nextToken){
        String number="";
        String numberAfterDot="";
        boolean isDouble = false;
        boolean isNumber=false;
        int countCommas=0;
        for (int i = 0; i < token.length(); i++) {
            if(!isDouble && Character.isDigit(token.charAt(i))){
                number+=token.charAt(i);
                isNumber=true;
            }
            else if(token.charAt(i)==','){
                countCommas++;
            }
            else if(token.charAt(i)=='.'){
                isDouble=true;
            }
            else if(isDouble && Character.isDigit(token.charAt(i))){
                numberAfterDot+=token.charAt(i);
            }
            else{
                isNumber=false;
            }
        }
        if(isNumber){
            String term="";
            String kind="";
            //less than 1000
            if(number.length()>=1 && number.length()<=3){
                term=number;
                if(isDouble){
                    term+='.'+numberAfterDot;
                }
            }
            else {
                //Thousand(included) to Million(not included)
                if (number.length() >= 4 && number.length() <= 6) {
                    kind = "K";
                    term=CreatingNumberForDictionary(number,3);
                    if(!isDouble)
                        term = reduceZeros(term,3);
                }
                //Million(included) to Billion(not included)
                else if (number.length() >= 7 && number.length() <= 9) {
                    kind = "M";
                    term=CreatingNumberForDictionary(number,6);
                    if(!isDouble)
                        term = reduceZeros(term,6);
                }
                //more than Billion(included)
                else if (number.length() >= 10) {
                    kind = "B";
                    term=CreatingNumberForDictionary(number,9);
                    if(!isDouble)
                        term = reduceZeros(term,9);
                }
                if (isDouble) {
                    for (int i = 0; i < numberAfterDot.length(); i++) {
                        term += numberAfterDot.charAt(i);
                    }
                }
                term += kind;
            }
            System.out.println(term);
        }
        return isNumber;
    }

    private String reduceZeros(String term, int reduce) {
        String ans=term;
        for (int i = term.length()-1; i >= term.length()-reduce; i--) {
            if(term.charAt(i)=='0')
                ans=term.substring(0,i);
            if(i==term.length()-reduce && term.charAt(i)=='0')
                ans=term.substring(0,i-1);
        }
        return ans;
    }

    private String CreatingNumberForDictionary(String number,int reduce){
        String term="";
        for (int i = 0; i < number.length(); i++) {
            if (i == number.length() - reduce) {
                term += '.';
            }
            term += number.charAt(i);
        }
        return term;
    }

    public static void main(String[] args){
        Parse parse=new Parse();
//        String test="123";
//        System.out.println(test.substring(0,2));
        parse.percentageTerm("percentage","6");
        for (String s:parse.dictionary){
            System.out.println(s);
        }

        /*
        parse.numberKMB("123");
        parse.numberKMB("123.23");
        //check k
        System.out.println();
        parse.numberKMB("1000");
        parse.numberKMB("1234");
        parse.numberKMB("1234.56");
        parse.numberKMB("1,234.56");
        parse.numberKMB("123,456");
        parse.numberKMB("12,345.67");
        //check m
        System.out.println();
        parse.numberKMB("1000000");
        parse.numberKMB("123456789");
        parse.numberKMB("123,456,789");
        parse.numberKMB("123,456,789.12");
        parse.numberKMB("1,234,567.56");
        //check B
        System.out.println();
        parse.numberKMB("123456789123456");
        parse.numberKMB("1234567891.23");
        */
    }
}
