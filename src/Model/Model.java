package Model;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Model {
    private Indexer indexer;
    private String pathFrom;
    private String pathTo;
    private boolean toStem;


    /**
     *constructor
     * @param pathFrom - the path of the corpus and stop-words
     * @param pathTo - save the posting and dictionary to this path
     * @param toStem - if true do stemming, else don't
     */
    public Model(String pathFrom,String pathTo,boolean toStem){
        indexer = new Indexer(pathFrom,pathTo,toStem);
        this.pathFrom=pathFrom;
        this.pathTo=pathTo;
        this.toStem=toStem;
    }

    /**
     * creating the dictionary and the posting of the inverted index
     */
    public boolean generateInvertedIndex(){//todo verify paths + called from "play"
        boolean succGenerate=indexer.createPostingAndDic(toStem);
        return succGenerate;
    }

    /**
     * reset the posting ,the dictionary file and the memory of the program
     * @return true if the reset succeed' else return false
     */
    public boolean reset(){
        return indexer.reset();
    }


    /**
     * take the dictionary of the terms from the indexer : term, tf overall
     * @return the dictionary of the terms
     */
    public Queue<String> displayDictionary(boolean isStem){
        //todo take the dictionary from indexer
        //todo check that the dictionary isnwt empty
        Queue<String> dictionary = indexer.displayDictionary(isStem);
        if(dictionary == null)
            return null; //todo handke in the controller
        return dictionary;
    }


    /**
     * load the dictionary from the disk to the memory
     * @return true if the loading succeed, else retutn false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean isStem){
        return indexer.loadDictionaryFromDiskToMemory(isStem);
    }


    public static void main(String[] args){
        long Stime = System.currentTimeMillis();
        String pathFrom = "C:\\Users\\nivdu\\Desktop\\אחזור\\פרוייקט גוגל";
        String pathTo = "C:\\Users\\nivdu\\Desktop\\bimbamtirasham";
        Model model = new Model(pathFrom,pathTo,false);
//        model.reset();
        model.generateInvertedIndex();
        long Ftime = System.currentTimeMillis();
        System.out.println(Ftime-Stime);
    }
}
