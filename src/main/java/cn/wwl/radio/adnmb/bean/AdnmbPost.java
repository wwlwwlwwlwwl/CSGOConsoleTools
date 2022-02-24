package cn.wwl.radio.adnmb.bean;

import cn.wwl.radio.adnmb.AdnmbClient;

public class AdnmbPost {
    private int id;
    private String time;
    private String cookie;
    private String content;
    private String image;
    private String format;

    public int getId() {
        return id;
    }

    public AdnmbPost setId(int id) {
        this.id = id;
        return this;
    }

    public String getTime() {
        return time;
    }

    public AdnmbPost setTime(String time) {
        this.time = time;
        return this;
    }

    public String getCookie() {
        return cookie;
    }

    public AdnmbPost setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    public String getContent() {
        return content;
    }

    public AdnmbPost setContent(String content) {
        this.content = content;
        return this;
    }

    public String getImage() {
        if (image == null || image.length() == 0 || format == null || format.length() == 0) {
            return "";
        }
        return AdnmbClient.ADNMB_IMAGE + image + format;
    }

    public AdnmbPost setImage(String image) {
        this.image = image;
        return this;
    }

    public AdnmbPost setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public String toString() {
        return "用户串{" +
                "串ID: " + id +
                ", 时间: '" + time + '\'' +
                ", 饼干: '" + cookie + '\'' +
                ", 内容: '" + content +
                '}';
    }
}
