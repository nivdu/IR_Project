package Model;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;

public class Model {
    private Indexer indexer;
    private boolean toStem;


    /**
     *constructor
     */
    public Model(){
        this.toStem=false;
    }

    /**
     * creating the dictionary and the posting of the inverted index
     */
    public boolean generateInvertedIndex(String pathFrom,String pathTo,boolean toStem){//todo verify paths + called from "play"
        indexer = new Indexer(pathFrom,pathTo,toStem);

        long Stime = System.currentTimeMillis();
        boolean succGenerate=indexer.createPostingAndDic(toStem);
        long Ftime = System.currentTimeMillis();
        if(succGenerate){
            showWhenFinishIndexing(Ftime-Stime);
        }
        return succGenerate;
    }

    public void showWhenFinishIndexing(long indexRunTime){
        int indexedDocNumber = indexer.getIndexedDocNumber();
        int uniqueTermsNumber = indexer.getUniqueTermsNumber();
        System.out.println("runtime by seconds: " + indexRunTime/1000);
        System.out.println("number Indexed docs: " + indexedDocNumber);
        System.out.println("number unique Terms: " + uniqueTermsNumber);
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

        String pathFrom = "C:\\Users\\user\\Desktop\\אוניברסיטה\\שנה ג\\שנה ג - סמסטר א\\אחזור\\עבודות\\מנוע חלק א";
        String pathTo = "C:\\Users\\user\\Desktop\\אוניברסיטה\\שנה ג\\שנה ג - סמסטר א\\אחזור\\עבודות\\מנוע חלק א\\output";
        Model model = new Model();
//        model.reset();
        model.generateInvertedIndex(pathFrom,pathTo,false);
//        model.loadDictionaryFromDiskToMemory(false);
//        Queue<String> queue = model.displayDictionary(false);
//        int size = queue.size();
//        for (int i = 0; i < size; i++)
//        {
//            if(queue.peek().contains("morning"))
//                System.out.println("blala");
//            System.out.println(queue.remove());
//        }
    }
}
