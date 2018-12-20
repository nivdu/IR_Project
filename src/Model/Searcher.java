package Model;

import java.util.List;

public class Searcher {
    private Parse parse;
    public Searcher(Parse parse){
        this.parse = parse;
    }

    public List<String[]> runQuery(String query) {
        document queryDoc = queryCut(query);
        queryDoc = parse.parseDoc(queryDoc);
        return null;
    }

    /**
     * cut the Query string by tags
     * @param query - Query to cut
     * @return - String array of the Query contents.
     */
    private document queryCut(String query){
        if(query==null || query.equals(""))
            return null;
        document queryDoc = null;
        return queryDoc;
    }
}
