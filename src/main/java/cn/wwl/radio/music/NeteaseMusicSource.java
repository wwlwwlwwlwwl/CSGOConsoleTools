package cn.wwl.radio.music;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class NeteaseMusicSource implements MusicSource {

    //https://163.lpddr5.cn/ 502 Bad Gateway
    //https://api.music.imsyy.top/
    //https://v2.alapi.cn/api/music 需要API_KEY
    private static final String API_LINK = "https://api.music.imsyy.top";
    private static final String API_TOKEN = ConfigLoader.getConfigObject().getAPIToken();

    @Override
    public List<MusicResult> searchMusic(String name) {
        if (name == null || name.length() == 0 || name.equals(" ")) {
            return List.of();
        }

        try {
            Document page = Jsoup.connect(API_LINK + "/cloudsearch")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .data(
                            "token", API_TOKEN,
                            "type", "1",
                            "keywords", name,
                            "limit", "9"
                    )
                    .post();
            return parseSearchMusic(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Search Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
        return List.of();
    }

    @Override
    public String getMusicDownloadLink(MusicResult result) {
        try {
            Document page = Jsoup.connect(API_LINK + "/song/url")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .data(
                            "token", API_TOKEN,
                            "id", result.getData(),
                            "format","json"
                    )
                    .post();
            return getResultURL(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Get URL Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
        return "";
    }

    @Override
    public String getResultURL(String urlPage) {
        JsonObject mainTree = JsonParser.parseString(urlPage).getAsJsonObject();
        if (mainTree.get("code").getAsInt() != 200) {
            ConsoleManager.getConsole().printError("GetURL result != 200!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + urlPage + "]");
            return "";
        }

        JsonElement data = mainTree.get("data");
        JsonObject object;
        if (data.isJsonArray()) {
            object = data.getAsJsonArray().get(0).getAsJsonObject();
        } else {
            object = data.getAsJsonObject();
        }

        String url = object.get("url").getAsString();
        JsonElement freeTrialInfo = object.get("freeTrialInfo");
        if (!freeTrialInfo.isJsonNull()) {
            url = NEED_PAY;
        }
        return url;
    }

    private List<MusicResult> parseSearchMusic(String searchPage) {
        ArrayList<MusicResult> musicResults = new ArrayList<>();
        JsonObject mainTree = JsonParser.parseString(searchPage).getAsJsonObject();
        if (mainTree.get("code").getAsInt() != 200) {
            ConsoleManager.getConsole().printError("Search result != 200!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + searchPage + "]");
            return musicResults;
        }

        JsonArray songsArray = mainTree.get("result").getAsJsonObject().get("songs").getAsJsonArray();

        for (JsonElement element : songsArray) {
            MusicResult result = new MusicResult();
            JsonObject object = element.getAsJsonObject();
            int id = object.get("id").getAsInt();
            String name = object.get("name").getAsString();
            String author;
            JsonArray artists = object.get("ar").getAsJsonArray();
            if (artists.size() == 1) {
                author = artists.get(0).getAsJsonObject().get("name").getAsString();
            } else {
                StringBuilder names = new StringBuilder();
                for (JsonElement artist : artists) {
                    names.append(artist.getAsJsonObject().get("name").getAsString()).append(",");
                }
                author = names.substring(0,names.length() - 1);
            }

            result.setAuthor(author).setData(String.valueOf(id)).setName(name);
            musicResults.add(result);
        }
        return musicResults;
    }
}
