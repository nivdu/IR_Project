package sample;

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
}
