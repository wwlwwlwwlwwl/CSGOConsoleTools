package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.utils.SoxSoundUtils;
import com.google.gson.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NeteaseMusicManager {

    private static final String API_LINK = "https://v2.alapi.cn/api/music";
    private static final String API_TOKEN = ConfigLoader.getConfigObject().getAPIToken();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();
    private static final File DOWNLOAD_DIR = new File(SoxSoundUtils.getMusicDir(),"downloads");

    public static List<MusicResult> searchMusic(String name) {
        if (name == null || name.length() == 0 || name.equals(" ")) {
            return List.of();
        }

        if (API_TOKEN.equals("None")) {
            ConsoleManager.getConsole().printError("API_KEY not Set! Cannot Search music!");
            return List.of();
        }
        try {
            Document page = Jsoup.connect(API_LINK + "/search")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .data(
                            "token", API_TOKEN,
                            "type", "1",
                            "keyword", name,
                            "limit", "9"
                    )
                    .post();
            return parseSearchMusic(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Search Throw Exception!");
            e.printStackTrace();
        }
        return List.of();
    }

    public static File downloadMusic(MusicResult result) {
        if (result == null) {
            return null;
        }

        if (API_TOKEN.equals("None")) {
            ConsoleManager.getConsole().printError("API_KEY not Set! Cannot Download music!");
            return null;
        }

        if (!DOWNLOAD_DIR.exists()) {
            DOWNLOAD_DIR.mkdir();
        }

        String name = result.getName() + " - " + result.getAuthor() + ".mp3";
        File downloadMusic = new File(DOWNLOAD_DIR,name);

        if (downloadMusic.exists()) {
            return downloadMusic;
        }

        String downloadLink = getMusicDownloadLink(result);
        downloadObject(downloadLink,downloadMusic);
        return downloadMusic;
    }

    private static String getMusicDownloadLink(MusicResult result) {
        try {
            Document page = Jsoup.connect(API_LINK + "/url")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .data(
                            "token", API_TOKEN,
                            "id", String.valueOf(result.getId()),
                            "format","json"
                    )
                    .post();
            return getResultURL(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Get URL Throw Exception!");
            e.printStackTrace();
        }
        return "";
    }

    private static void downloadObject(String url, File savePath) {
        if (savePath.exists()) {
            savePath.delete();
        }
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .execute();

            FileOutputStream out = new FileOutputStream(savePath);
            out.write(response.bodyAsBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try download Object from: " + url + " Throw exception!");
            e.printStackTrace();
        }
    }

    private static String getResultURL(String urlPage) {
        JsonObject mainTree = JsonParser.parseString(urlPage).getAsJsonObject();
        if (mainTree.get("code").getAsInt() != 200) {
            ConsoleManager.getConsole().printError("GetURL result != 200!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + urlPage + "]");
            return "";
        }

        return mainTree.get("data").getAsJsonObject().get("url").getAsString();
    }

    private static List<MusicResult> parseSearchMusic(String searchPage) {
        ArrayList<MusicResult> musicResults = new ArrayList<>();
        JsonObject mainTree = JsonParser.parseString(searchPage).getAsJsonObject();
        if (mainTree.get("code").getAsInt() != 200) {
            ConsoleManager.getConsole().printError("Search result != 200!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + searchPage + "]");
            return musicResults;
        }

        JsonArray songsArray = mainTree.get("data").getAsJsonObject().get("songs").getAsJsonArray();

        for (JsonElement element : songsArray) {
            MusicResult result = new MusicResult();
            JsonObject object = element.getAsJsonObject();
            int id = object.get("id").getAsInt();
            String name = object.get("name").getAsString();
            String author = "";
            JsonArray artists = object.get("artists").getAsJsonArray();
            if (artists.size() == 1) {
                author = artists.get(0).getAsJsonObject().get("name").getAsString();
            } else {
                StringBuilder names = new StringBuilder();
                for (JsonElement artist : artists) {
                    names.append(artist.getAsJsonObject().get("name").getAsString()).append(",");
                }
                author = names.substring(0,names.length() - 1);
            }

            result.setAuthor(author).setId(id).setName(name);
            musicResults.add(result);
        }
        return musicResults;
    }

    public static class MusicResult {
        private String author;
        private String name;
        private int id;

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

        public int getId() {
            return id;
        }

        public MusicResult setId(int id) {
            this.id = id;
            return this;
        }

        @Override
        public String toString() {
            return "MusicResult{" +
                    "author='" + author + '\'' +
                    ", name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }
    }
}
