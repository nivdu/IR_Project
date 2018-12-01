package Model;

public class term {
    private String name;
    private int df;

    public term(String name){
        this.name=name;
        df=1;
    }

    public String getName() {
        return name;
    }

    public int getDf() {
        return df;
    }

    public void IncreaseDfByOne() {
        this.df++;
    }
}
