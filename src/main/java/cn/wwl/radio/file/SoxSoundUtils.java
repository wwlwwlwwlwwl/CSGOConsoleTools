package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SoxSoundUtils {

    private static final ThreadPoolExecutor POOL = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final File MUSIC_DIR = new File(SteamUtils.getCsgoPath(),"music");
    private static final File CACHE_DIR = new File(MUSIC_DIR, "cache");
    private static final List<File> CACHED_MUSICS = new ArrayList<>();
    private static File soxExecuteFile = null;

    /**
     * Use SoX Format the Music to Game Readable Format, and Put music to cached list.
     * @param music Not cached Music
     */
    public static void cacheMusic(File music) {
        cacheMusic(music,CACHED_MUSICS::add);
    }

    /**
     * Use SoX Format the Music to Game Readable Format, and Call the callback.
     * @param music Not cached Music
     * @param callback Music callback, Variable is Cached Music.
     */
    public static void cacheMusic(File music,cacheMusicCallback callback) {
        File saveDir = new File(CACHE_DIR,  splitSuffix(music.getName()) + ".wav");
        if (saveDir.exists()) {
            ConsoleManager.getConsole().printToConsole("Music " + saveDir.getName() + " Already cached. use cache.");
            callback.handle(saveDir);
            return;
        }
        ConsoleManager.getConsole().printToConsole("Start cache Music: " + music.getName());
        POOL.execute(() -> {
            try {
                String cmdLine = soxExecuteFile.getAbsolutePath() +
                        " \"" +
                        music.getAbsolutePath() +
                        "\" -r 22050 -c 1 -b 16 --multi-threaded -V1 \"" +
                        saveDir.getAbsolutePath() +
                        "\"";
                //sox.exe music.mp3 -r 22050 -c 1 -b 16 --multi-threaded -V1 voice_input.wav
                Process process = Runtime.getRuntime().exec(cmdLine);

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    callback.handle(saveDir);
                    ConsoleManager.getConsole().printToConsole("Music " + saveDir.getName() + " Cache done.");
                } else {
                    ConsoleManager.getConsole().printError("Music " + saveDir.getName() + " Cache Return Code == " + exitCode + "! Cache Failed!");
                }
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Cache music " + music.getName() + " Throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        });
    }

    public static List<File> getCachedMusics() {
        return CACHED_MUSICS;
    }

    public static File getMusicDir() {
        return MUSIC_DIR;
    }

    public static File getCacheDir() {
        return CACHE_DIR;
    }

    private static void initMusicDir() {
        if (!MUSIC_DIR.exists()) {
            if (MUSIC_DIR.mkdir()) {
                ConsoleManager.getConsole().printToConsole("MusicDir created. Place music to " + MUSIC_DIR.getAbsolutePath());
                if (CACHE_DIR.mkdir()) {
                    ConsoleManager.getConsole().printError("Try create Cache dir Failed!");
                }
            } else {
                ConsoleManager.getConsole().printToConsole("Make musicPath failed!");
            }
        }
    }

    /**
     * Get the File name, Removed Suffix
     * @param name File name
     * @return File name Without Suffix
     */
    public static String splitSuffix(String name) {
        if (!name.contains(".")) {
            return name;
        }
        return name.substring(0, name.lastIndexOf("."));
    }

    public static boolean initSox(File gamePath) {
        if (gamePath == null) {
            ConsoleManager.getConsole().printError("InitSox GamePath is Null!");
            return false;
        }

        initMusicDir();
        File soxPath = new File(gamePath,"sox");
        File soxZip = new File(gamePath, "sox.zip");
        if (!soxPath.exists()) {
            if (!soxPath.mkdir()) {
                ConsoleManager.getConsole().printToConsole("Try make Sox Path failed!");
                return false;
            }
        }

        File soxFile = new File(soxPath,"sox.exe");
        if (!soxFile.exists()) {
            try {
                downloadSox(soxZip);
                unzipSox(soxZip, soxPath);
            } catch (Exception e) {
                ConsoleManager.getConsole().printToConsole("Try Deploy Sox throw Exception!");
                ConsoleManager.getConsole().printException(e);
                return false;
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(soxFile.getAbsolutePath() + " --version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.contains("SoX")) {
                    soxExecuteFile = soxFile;
                    ConsoleManager.getConsole().printToConsole("Test Sox Success.");
                    reader.close();
                    return true;
                }
            }
        } catch (Exception e) {
            ConsoleManager.getConsole().printToConsole("Try Check Sox file Throw Exception!");
            ConsoleManager.getConsole().printException(e);
            return false;
        }
        return false;
    }

    public static void downloadSox(File savePath) throws IOException {
        if (savePath.exists()) {
            if (savePath.delete()) {
                ConsoleManager.getConsole().printError("Try delete Cached Sox Failed!");
                return;
            }
        }
        ConsoleManager.getConsole().printToConsole("Sox not Found! Starting Download Sox...");
        Connection.Response response = Jsoup.connect("https://wwlwwl.xyz/sox.zip")
                .ignoreContentType(true)
                .followRedirects(true)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) SoxDownloader")
                .maxBodySize(Integer.MAX_VALUE)
                .execute();

        FileOutputStream out = new FileOutputStream(savePath);
        out.write(response.bodyAsBytes());
        out.flush();
        out.close();
        ConsoleManager.getConsole().printToConsole("Sox Download done.");
    }

    private static void unzipSox(File soxZip,File soxPath) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(soxZip));
        ZipEntry entry = zipInputStream.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            File file = new File(soxPath,name);
            ConsoleManager.getConsole().printToConsole("Unzipping " + file.getName() + "...");
            FileOutputStream outputStream = new FileOutputStream(file);
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }
            outputStream.flush();
            outputStream.close();
            zipInputStream.closeEntry();
            entry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        ConsoleManager.getConsole().printToConsole("Unzip done.");
    }

    public interface cacheMusicCallback {
        void handle(File cachedMusic);
    }
}
