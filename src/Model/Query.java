package Model;

public class Query {
    private String data;
    private String queryID;
    private String[] querySplited;
    private String desc;

    public Query(String data, String queryID, String description) {
        this.data = data;
        this.queryID = queryID;
        this.desc = description;
    }

    public String getDesc() {
        return desc;
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
