package cn.wwl.radio.adnmb.bean;

import java.util.ArrayList;
import java.util.List;

public class AdnmbThread extends AdnmbPost {
    private String title;
    private boolean sage;
    private boolean admin;

    private List<AdnmbReply> replys = new ArrayList<>();
    private int totalReply;

    private int currentPage;
    private boolean fromBeitai;

    public String getTitle() {
        return title;
    }

    public AdnmbThread setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isSage() {
        return sage;
    }

    public AdnmbThread setSage(boolean sage) {
        this.sage = sage;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public AdnmbThread setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public List<AdnmbReply> getReplys() {
        return replys;
    }

    public int getTotalReply() {
        return totalReply;
    }

    public AdnmbThread setTotalReply(int totalReply) {
        this.totalReply = totalReply;
        return this;
    }

    public boolean isFromBeitai() {
        return fromBeitai;
    }

    public AdnmbThread setFromBeitai(boolean fromBeitai) {
        this.fromBeitai = fromBeitai;
        return this;
    }

    public AdnmbThread setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
