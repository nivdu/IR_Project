package sample;

import javax.xml.soap.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

public class Parse {
    //hashSet for all the stop words.
    HashSet<String> stopWords;
    //dictionary contain all the terms from the docs
    HashSet<String> dictionary;

    /**
     * Parse Constructor.
     */
    public Parse() {
        this.stopWords = new HashSet<String>();
        this.dictionary = new HashSet<String>();
    }

    public void parseDoc(String[] currDoc) {
        //pinter to the curr index in currDoc array.
        for (String doc:currDoc) {
            //temporary dictionary to find the max tf in current doc.
            HashMap<String, Integer> FindMaxTf;
            //split by spaces
            String[] splitedDoc = doc.split(" ");
            //loop over all the tokens in current doc.
            for (int currDocIndex = 0; currDocIndex < splitedDoc.length ; currDocIndex++) {
                //todo use stemmer.
                //if current word is stop word continue to the next word.
                if (deleteStopWords(splitedDoc[0]))
                    continue;

                //get prev and next tokens if there isn't next token (the end of the array) return nextToken="", if index==0 return prevToken="".
                String nextToken = getNextToken(currDoc, currDocIndex);
                String prevToken = getNextToken(currDoc, currDocIndex);
                //check percentageTerms function
                if (percentageTerm(nextToken, splitedDoc[currDocIndex]) && nextToken.equals("percentage") || nextToken.equals("percent")) {
                    currDocIndex++;
                    continue;
                }
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
    private boolean percentageTerm(String nextT, String token){
        if(token==null || token.length()<=0)//todo what to do if the token is null?
            return false;
        //cases like 6%
        if(token.charAt(token.length()-1)=='%'){
            //substring from number% to number (6% to 6) todo check if length-2 its ok.
            String checkIfNumber = token.substring(0,token.length()-2);
            //if before the '%' char there is only digits and docs add to the dictionary the whole token.
            if(checkIfOnlyDigitsAndDots(checkIfNumber)) {
                dictionary.add(token);
                return true;
            }
        }
        //cases like "6 percent", "6 percentage"
        else if(nextT!=null && nextT!="" &&(nextT.equals("percent") || nextT.equals("percentage"))){
            //if before the percent/percentage there is only digits and docs add to the dictionary the whole token combine with '%'.
            if(checkIfOnlyDigitsAndDots(token)) {
                dictionary.add(token + '%');
                return true;
            }
        }
        return false;
    }

    /**
     * check whether a given string contain only digits(numbers)
     * @param number - string to check
     * @return - true if contain only numbers else false.
     */
    private boolean checkIfOnlyDigitsAndDots(String number){
        for (Character c:number.toCharArray()){
            if(!Character.isDigit(c) && !(c.equals('.')))
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
        if(index-1<=0)
            return "";
        return currDoc[index-1];
    }

    /**
     * creating terms of numbers according to:
     *
     * @param token
     */
    public void numberKMB(String token){
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
    }
}
