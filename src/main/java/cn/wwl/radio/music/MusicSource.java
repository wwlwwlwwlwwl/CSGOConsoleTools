package cn.wwl.radio.music;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.utils.SoxSoundUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public interface MusicSource {
    File DOWNLOAD_DIR = new File(SoxSoundUtils.getMusicDir(),"downloads");
    Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();

    List<MusicResult> searchMusic(String name);
    String getMusicDownloadLink(MusicResult result);
    String getResultURL(String urlPage);

    default File downloadMusic(MusicResult result) {
        if (result == null) {
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

    default void downloadObject(String url, File savePath) {
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
}
