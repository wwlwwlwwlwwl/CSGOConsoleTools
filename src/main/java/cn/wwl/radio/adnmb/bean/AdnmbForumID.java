package cn.wwl.radio.adnmb.bean;

public class AdnmbForumID {
    private int id;
    private String name;
    private String showName;
    private int group;
    private boolean beitai;

    public int getId() {
        return id;
    }

    public AdnmbForumID setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AdnmbForumID setName(String name) {
        this.name = name;
        return this;
    }

    public String getShowName() {
        return showName;
    }

    public AdnmbForumID setShowName(String showName) {
        this.showName = showName;
        return this;
    }

    public int getGroup() {
        return group;
    }

    public AdnmbForumID setGroup(int group) {
        this.group = group;
        return this;
    }

    public boolean isBeitai() {
        return beitai;
    }

    public AdnmbForumID setBeitai(boolean beitai) {
        this.beitai = beitai;
        return this;
    }

    @Override
    public String toString() {
        String str = getName();
        if (!showName.equals("")) {
            str += "(" + showName + ")";
        }
        str += ", ID: " + getId();
        if (beitai) {
            str += ", 备胎岛";
        }
        return str;
    }
}
