package cn.wwl.radio.music;

public class MusicResult {
    private String author;
    private String name;
    private String data;

    public String getAuthor() {
        return author;
    }

    public MusicResult setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getName() {
        return name;
    }

    public MusicResult setName(String name) {
        this.name = name;
        return this;
    }

    public String getData() {
        return data;
    }

    public MusicResult setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "MusicResult{" +
                "author='" + author + '\'' +
                ", name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
