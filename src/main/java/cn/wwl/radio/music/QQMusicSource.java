package cn.wwl.radio.music;

import cn.wwl.radio.console.ConsoleManager;
import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class QQMusicSource implements MusicSource {

    private static final String API_LINK = "https://api.zsfmyz.top/music/";
    //这个API的证书过期了 但是找不到靠谱的了 暂时忽略证书使用
    private static SSLSocketFactory sslSocketFactory;

    public static SSLSocketFactory getSSLSocketFactory() {
        if (sslSocketFactory == null) {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null,
                        new X509TrustManager[] {new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }},
                        new SecureRandom());
                sslSocketFactory = context.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException ignored) {}
        }

        return sslSocketFactory;
    }

    @Override
    public List<MusicResult> searchMusic(String name) {
        if (name == null || name.length() == 0 || name.equals(" ")) {
            return List.of();
        }

        try {
            Document page = Jsoup.connect(API_LINK + "/list")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .maxBodySize(Integer.MAX_VALUE)
                    .sslSocketFactory(getSSLSocketFactory())
                    .data(
                            "p", "1",
                            "w", name,
                            "n", "9"
                    )
                    .post();
            return parseSearchMusic(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Search Throw Exception!");
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public String getMusicDownloadLink(MusicResult result) {
        try {
            Document page = Jsoup.connect(API_LINK + "/url")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44")
                    .sslSocketFactory(getSSLSocketFactory())
                    .maxBodySize(Integer.MAX_VALUE)
                    .data(
                            "songmid", result.getData(),
                            "guid", "123456"
                    )
                    .post();
            return getResultURL(page.body().html());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try Get URL Throw Exception!");
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getResultURL(String urlPage) {
        JsonObject mainTree = JsonParser.parseString(urlPage).getAsJsonObject();
        if (!mainTree.get("code").getAsString().equals("0")) {
            ConsoleManager.getConsole().printError("GetURL result != 0!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + urlPage + "]");
            return "";
        }

        return mainTree.get("data").getAsJsonObject().get("musicUrl").getAsString();
    }

    private List<MusicResult> parseSearchMusic(String searchPage) {
        ArrayList<MusicResult> musicResults = new ArrayList<>();
        JsonObject mainTree = JsonParser.parseString(searchPage).getAsJsonObject();
        if (!mainTree.get("code").getAsString().equals("0")) {
            ConsoleManager.getConsole().printError("Search result != 0!");
            ConsoleManager.getConsole().printToConsole("DEBUG: [" + searchPage + "]");
            return musicResults;
        }

        JsonArray songsArray = mainTree.get("data").getAsJsonObject().get("list").getAsJsonArray();

        for (JsonElement element : songsArray) {
            MusicResult result = new MusicResult();
            JsonObject object = element.getAsJsonObject();
            String name = object.get("songname").getAsString();
            String singer = object.get("singer").getAsJsonObject().get("name").getAsString();
            String songmid = object.get("songmid").getAsString();


            result.setAuthor(singer).setData(songmid).setName(name);
            musicResults.add(result);
        }
        return musicResults;
    }
}
