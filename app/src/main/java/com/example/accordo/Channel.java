package com.example.accordo;

public class Channel {
    private String ctitle;
    private String mine;
    private String index;
    public static String MY_CHANNEL_INDEX = "mine", NO_LETTER_INDEX = "#";

    public void setCtitle(String ctitle) {
        this.ctitle = ctitle;
    }

    public void setMine(String mine) {
        this.mine = mine;
    }

    public String getCtitle() {
        return ctitle;
    }

    public String getMine() {
        return mine;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
