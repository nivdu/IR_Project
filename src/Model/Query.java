package Model;

public class Query {
    private String queryID;
    private String queryData;

    public Query(){
        queryID="";
        queryData="";
    }

    public Query(String queryID, String queryData) {
        this.queryID = queryID;
        this.queryData = queryData;
    }

    public String getQueryID() {
        return queryID;
    }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    public String getQueryData() {
        return queryData;
    }

    public void setQueryData(String queryData) {
        this.queryData = queryData;
    }
}
