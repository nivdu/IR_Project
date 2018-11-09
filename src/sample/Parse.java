package sample;

import javax.xml.soap.Node;
import java.util.HashSet;
import java.util.LinkedList;

public class Parse {
    HashSet<String> dictionary;
    public void parseDoc(LinkedList<String> docsInFile) {
        for (String doc:docsInFile) {
            //todo create temporary dictionary to find the max tf in current doc.
            //todo split by spaces
            //todo use stemmer
            //todo create term accordance to the given laws
            //todo adding to dictionary
        }
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
