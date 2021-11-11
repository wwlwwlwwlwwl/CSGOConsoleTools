package cn.wwl.radio.executor.functions;

import cn.wwl.radio.music.MusicManager;
import cn.wwl.radio.music.MusicResult;
import cn.wwl.radio.music.NeteaseMusicSource;
import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.utils.SoxSoundUtils;
import cn.wwl.radio.utils.SteamUtils;
import cn.wwl.radio.utils.TextMarker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CustomMusicFunction implements ConsoleFunction {

    private static String volumeKey = "K";
    private static boolean isPlaying = false;
    private static boolean isBootFailed = false;
    private static boolean isInited = false;

    private static boolean isEnableSearch = true;
    private static boolean isLocalVersion = ConfigLoader.getConfigObject().isMusicNetworkSearch();
    private static final List<MusicResult> previusResult = new ArrayList<>();

    @Override
    public boolean isRequireTicking() {
        return true;
    }

    @Override
    public boolean isRequireParameter() {
        return false;
    }

    @Override
    public List<String> isHookSpecialMessage() {
        return List.of("select","voicerecord","StopMusicPlaying");
    }

    @Override
    public boolean isHookPlayerChat() {
        return true;
    }

    @Override
    public void onTick() {
        if (!isInited) {
            init();
        }

        if (isBootFailed) {
            return;
        }
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        if (isBootFailed) {
            SocketTransfer.getInstance().echoToConsole("Init Format Tool Failed!");
            return;
        }

        if (!isLocalVersion) {
            if (isEnableSearch) {
                SocketTransfer.getInstance().echoToConsole("SearchMusic is Disabled.");
                isEnableSearch = false;
            } else {
                SocketTransfer.getInstance().echoToConsole("SearchMusic is Enabled.");
                isEnableSearch = true;
            }
            return;
        }

        if (SoxSoundUtils.getCachedMusics().isEmpty()) {
            SocketTransfer.getInstance().echoToConsole("You not have any music! Put music in Music dir!");
        }

        SocketTransfer.getInstance().echoToConsole("Music List: ");
        for (int i = 0; i < SoxSoundUtils.getCachedMusics().size(); i++) {
            File music = SoxSoundUtils.getCachedMusics().get(i);
            SocketTransfer.getInstance().echoToConsole(i + ". " + music.getName());
        }
        SocketTransfer.getInstance().echoToConsole("Use : select0-" + (SoxSoundUtils.getCachedMusics().size() - 1) + " to Play music!");
    }

    @Override
    public void onHookSpecialMessage(String message) {
        if (message.contains("cancelselect") || message.contains("bind")) {
            return;
        }

        if (message.contains("voicerecord")) {
//            "v" = "+voicerecord"
            volumeKey = message.split("\" = \"")[0].replace("\"","");
            ConsoleManager.getConsole().printToConsole("Find Volume Key: " + volumeKey);
            return;
        }else if (message.contains("StopMusicPlaying") && !message.contains("echo")) {
            stopMusic();
        } else if (message.contains("select")) {
            int musicCount = Integer.parseInt(FunctionExecutor.removeUnknownHead(message).substring(6));
            File music = SoxSoundUtils.getCachedMusics().get(musicCount);
            playMusic(music);
        }
    }

    @Override
    public void onHookPlayerChat(String name, String content) {
        if (!(
                content.contains("!search") ||
                content.contains("!play") ||
                content.contains("!stop")
        )) {
            return;
        }

        if (!isEnableSearch) {
            sayTeam(TextMarker.红色.getHumanCode() + "点歌功能已被禁用.");
            return;
        }

        SocketTransfer.getInstance().pushToConsole("key_listboundkeys;clear");
        if (content.equals("!search")) {
            sayTeam("请输入歌曲名称! (!search 音乐名称)");
            return;
        } else if (content.contains("!search")) {
            String searchName = content.substring(7);
            ConsoleManager.getConsole().printToConsole("Start Call search for: " + searchName);
            List<MusicResult> musicResults = MusicManager.getMusicSource().searchMusic(searchName);
            if (musicResults.isEmpty()) {
                sayTeam("搜索返回内容为0!");
                return;
            }

            previusResult.addAll(musicResults);
            int count = 0;
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < musicResults.size(); i++) {
                MusicResult musicResult = musicResults.get(i);
                builder.append(TextMarker.红色.getHumanCode()).append(i).append(".")
                        .append(TextMarker.金色.getHumanCode()).append(musicResult.getName())
                        .append(TextMarker.灰色.getHumanCode()).append("--")
                        .append(TextMarker.蓝色.getHumanCode()).append(musicResult.getAuthor())
                        .append(TextMarker.换行.getHumanCode());
//            System.out.println("Debug: c>" + count);
                if (++count == 3) {
//                System.out.println("Debug: print");
                    sayTeam(builder.toString());
                    builder = new StringBuilder();
                    count = 0;
                }
            }
            SocketTransfer.getInstance().pushToConsole("say_team 请打开聊天查看所有内容");
        } else if (content.equals("!play")) {
            if (previusResult.isEmpty()) {
                sayTeam(TextMarker.红色.getHumanCode() + "请先使用!search搜索后再进行播放!");
            } else {
                sayTeam(TextMarker.红色.getHumanCode() + "请使用!play [歌曲序号]来进行播放!");
            }
        } else if (content.contains("!play")) {
            int musicCount = Integer.parseInt(content.substring(5).trim());
            sayTeam(TextMarker.金色.getHumanCode() + "正在缓存...请稍后.");

            List<File> temp_list = new ArrayList<>();
            File downloadMusic = MusicManager.getMusicSource().downloadMusic(previusResult.get(musicCount));
            int retryCount = 0;
            while (downloadMusic == null) {
                if (++retryCount >= 5) {
                    sayTeam(TextMarker.红色.getHumanCode() + "音乐下载失败!请重试!");
                    return;
                }
                downloadMusic = MusicManager.getMusicSource().downloadMusic(previusResult.get(musicCount));
            }
            SoxSoundUtils.cacheMusic(downloadMusic,temp_list);
            while (temp_list.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
            ConsoleManager.getConsole().printToConsole("Music " + downloadMusic.getName() + " Ready.");
            playMusic(temp_list.get(0));
        } else if (content.equals("!stop")) {
            if (isPlaying) {
                stopMusic();
            }
        }
    }

    private static void sayTeam(String s) {
        CustomRadioFunction.sendCustomRadio(s,false);
    }

    public static void playMusic(File cachedMusic) {
        if (isPlaying || !isInited) {
            return;
        }

        File inputFile = new File(SteamUtils.getCsgoPath(),"voice_input.wav");
        if (inputFile.exists()) {
            inputFile.delete();
        }

        try {
            Files.copy(cachedMusic.toPath(), inputFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try copy File Throw Exception!");
            e.printStackTrace();
        }

        SocketTransfer.getInstance().pushToConsole(
                "unbind " + volumeKey + ";" +
                "bind " + volumeKey + " \"echo StopMusicPlaying\"");
        SocketTransfer.getInstance().pushToConsole(
                "voice_loopback 1;" +
                        "voice_inputfromfile 1;" +
                        "voice_forcemicrecord 0;" +
                        "+voicerecord;" +
                        "clear");
        isPlaying = true;
        SocketTransfer.getInstance().echoToConsole("Now start Playing: " + cachedMusic.getName());
    }

    public static void stopMusic() {
        SocketTransfer.getInstance().pushToConsole(
                "-voicerecord;" +
                        "unbind " + volumeKey + ";" +
                        "bind " + volumeKey + " \"+voicerecord\";" +
                        "voice_loopback 0;" +
                        "voice_inputfromfile 0;" +
                        "voice_forcemicrecord 1;" +
                        "clear"
        );
        isPlaying = false;
        SocketTransfer.getInstance().echoToConsole("Music play now Stop.");
    }

    private void init() {
        isInited = true;
        ConsoleManager.getConsole().printToConsole("Start init CustomMusic function...");
        if (SteamUtils.getCsgoPath() == null) {
            if (!SteamUtils.initCSGODir()) {
                isBootFailed = true;
                return;
            }
        }

        if (!SoxSoundUtils.initSox(SteamUtils.getCsgoPath())) {
            isBootFailed = true;
            return;
        }

        File[] musics = SoxSoundUtils.getMusicDir().listFiles((file) -> !file.isDirectory());
        if (musics == null || musics.length == 0) {
            ConsoleManager.getConsole().printToConsole("CanNot find any Music!");
            return;
        }

        for (File music : musics) {
            ConsoleManager.getConsole().printToConsole("Start cache Music: " + music.getName());
            SoxSoundUtils.cacheMusic(music);
        }
    }
}
