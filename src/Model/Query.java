package Model;

public class Query {
    private String data;
    private String queryID;
    private String[] querySplited;

    public Query(String data, String queryID) {
        this.data = data;
        queryID = queryID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getQueryID() {
        return queryID;
    }

    public void setQueryID(String queryID) {
        queryID = queryID;
    }

    public String[] getDocSplited() {
        return querySplited;
    }

    public void setQuerySplited(String[] querySplited) {
        this.querySplited = querySplited;
    }
}
