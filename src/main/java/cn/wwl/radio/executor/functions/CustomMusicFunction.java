package cn.wwl.radio.executor.functions;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.console.impl.gui.MinimizeTrayConsole;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.SoxSoundUtils;
import cn.wwl.radio.file.SteamUtils;
import cn.wwl.radio.music.BackgroundMusic;
import cn.wwl.radio.music.MusicManager;
import cn.wwl.radio.music.MusicResult;
import cn.wwl.radio.music.MusicSource;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.utils.TextMarker;
import cn.wwl.radio.utils.TimerUtils;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.PausablePlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CustomMusicFunction implements ConsoleFunction {

    private static String volumeKey = "K";
    private static boolean isPlaying = false;
    private static boolean isBootFailed = false;

    //在控制台出现flare的时候肯定是结算界面 但是同时也会出现globe 而这时候玩家可能还没有退出游戏 所以写一个定时器在获取到flare之后暂时禁用globe的获取
    private static boolean disableGlobe = false;

    private static boolean isEnableSearch = true;
    private static final boolean isLocalVersion = !ConfigLoader.getConfigObject().isMusicNetworkSearch();
    private static final boolean enableLobbyMusic = ConfigLoader.getConfigObject().isLobbyMusic();
    private static List<MusicResult> previousResult = new ArrayList<>();

    public static final String SELECT_COMMAND = "#select";
    public static final String SEARCH_COMMAND = "#search";
    public static final String PLAY_COMMAND = "#play";
    public static final String STOP_COMMAND = "#stop";
    public static final String HELP_COMMAND = "#music";

    public static final String LOBBY_START_COMMAND = "music_start";
    public static final String LOBBY_PLAY_COMMAND = "music_play";
    public static final String LOBBY_PAUSE_COMMAND = "music_pause";
    public static final String LOBBY_STOP_COMMAND = "music_stop";
    private static final String LOBBY_MUSIC_WATCHER_HEAD = "LobbyMusicWatcher";

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
        return List.of("voicerecord","StopMusicPlaying",
                // 游戏内自动播放音乐 钩子
                "已连接","Not connected to server","materials","uniqueid","music", LOBBY_MUSIC_WATCHER_HEAD,
                //控制台搜索音乐,选择音乐
                SELECT_COMMAND, SEARCH_COMMAND, PLAY_COMMAND,
                // 背景音乐控制
                LOBBY_START_COMMAND, LOBBY_PLAY_COMMAND, LOBBY_PAUSE_COMMAND, LOBBY_STOP_COMMAND
        );
    }

    @Override
    public boolean isHookPlayerChat() {
        return true;
    }

    @Override
    public void onInit() {
        ConsoleManager.getConsole().printToConsole("Start init CustomMusic function...");

        if (SteamUtils.getCsgoPath() == null) {
            isBootFailed = true;
            return;
        }

        if (!SoxSoundUtils.initSox(SteamUtils.getCsgoPath())) {
            isBootFailed = true;
            return;
        }

        if (enableLobbyMusic) {
            registerCommandHook();
        }

        if (!isLocalVersion) {
            return;
        }

        File[] musics = SoxSoundUtils.getMusicDir().listFiles((file) -> !file.isDirectory());
        if (musics == null || musics.length == 0) {
            ConsoleManager.getConsole().printToConsole("CanNot find any Music!");
            return;
        }

        for (File music : musics) {
            SoxSoundUtils.cacheMusic(music);
        }    }

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
            SocketTransfer.getInstance().echoToConsole("Use cmd " + SEARCH_COMMAND + "_[name] In console to Search music.");
            return;
        }

        if (SoxSoundUtils.getCachedMusics().isEmpty()) {
            SocketTransfer.getInstance().echoToConsole("You not have any music! Put music in Music dir!");
            return;
        }

        SocketTransfer.getInstance().pushToConsole("con_filter_text_out \"=\";key_listboundkeys");
        SocketTransfer.getInstance().echoToConsole("Music List: ");
        for (int i = 0; i < SoxSoundUtils.getCachedMusics().size(); i++) {
            File music = SoxSoundUtils.getCachedMusics().get(i);
            SocketTransfer.getInstance().echoToConsole(i + ". " + music.getName());
        }
        SocketTransfer.getInstance().echoToConsole("Use : " + SELECT_COMMAND + "0-" + (SoxSoundUtils.getCachedMusics().size() - 1) + " to Play music!");
    }

    @Override
    public void onHookSpecialMessage(String message) {
        if (ConfigLoader.getConfigObject().isLobbyMusic()) {
                String playerName = SocketTransfer.getInstance().getPlayerName();
                if ((playerName == null || message.contains(playerName)) && message.contains("已连接")) { //玩家连接入服务器后的文字
                    BackgroundMusic.stopBackgroundMusic(false);
                    disableGlobe = false;
                    return;
                }

                if (message.contains("Not connected to server")) { //Status 获取玩家不在服务器中
                    BackgroundMusic.playBackgroundMusic(false);
                    return;
                }

                if (message.contains("materials/panorama/images/ui_textures/flare.png")) { //获取资源失败 结束应该会有这个吧...
                    BackgroundMusic.playBackgroundMusic(false);
                    //只要出现结算 那么就一直忽略这个指令 直到玩家重新连接入新游戏为止
                    disableGlobe = true;
                    return;
                }

                if (!disableGlobe) {
                    if (message.contains("materials\\panorama\\images\\icons\\ui\\globe.svg")) { //断开连接似乎会出现这个 !暂停也会出现!
                        SocketTransfer.getInstance().pushToConsole("status");
                        return;
                    }
                }

                if (message.contains("# userid name uniqueid")) { //Status 玩家在游戏中
                    BackgroundMusic.stopBackgroundMusic(true);
                    return;
                }

                if (message.contains(LOBBY_MUSIC_WATCHER_HEAD)) {
                    String command = message.substring(LOBBY_MUSIC_WATCHER_HEAD.length() + 1).trim();
//                System.out.println("Command: " + command);
                    if (BackgroundMusic.isReady()) {
                        switch (command) {
                            case "MusicPlay" -> {
                                ConsoleManager.getConsole().printToConsole("LobbyMusic Start");
                                SocketTransfer.getInstance().echoToConsole("LobbyMusic now Start.");
                                BackgroundMusic.playBackgroundMusic(false);
                            }
                            case "MusicPause" -> {
                                ConsoleManager.getConsole().printToConsole("LobbyMusic Pause");
                                SocketTransfer.getInstance().echoToConsole("LobbyMusic now Paused.");
                                BackgroundMusic.pauseBackgroundMusic();
                            }
                            case "MusicStop" -> {
                                ConsoleManager.getConsole().printToConsole("LobbyMusic Stop");
                                SocketTransfer.getInstance().echoToConsole("LobbyMusic now Stopped.");
                                BackgroundMusic.stopBackgroundMusic(false);
                            }
                        }
                    }
                }
        }

        if (message.contains("cancelselect") || message.contains("bind")) {
            return;
        }

        if (message.contains("\"+voicerecord\"")) {
//            "v" = "+voicerecord"
            volumeKey = message.split("\" = \"")[0].replace("\"","").trim();
            ConsoleManager.getConsole().printToConsole("Find Volume Key: " + volumeKey);
            SocketTransfer.getInstance().pushToConsole("con_filter_text_out \"Unknown\"");
            return;
        }

        if (message.contains("StopMusicPlaying") && !message.contains("echo")) {
            stopMusic();
            return;
        }

        if (message.contains("Unknown") && (message.toLowerCase(Locale.ROOT).contains("command") || message.contains("*******"))) {
            message = FunctionExecutor.removeUnknownCommandTag(message).replace("_"," ");
            if (message.toLowerCase(Locale.ROOT).contains("music")) {
                try {
                    float volume = Float.parseFloat(message.substring(5));
                    BackgroundMusic.setVolume(volume, true);
                    ConsoleManager.getConsole().printToConsole("Music volume Set to " + volume);
                    SocketTransfer.getInstance().echoToConsole("Music volume Set to " + volume);
                } catch (Exception e) {
                    SocketTransfer.getInstance().echoToConsole("Wrong Music cmd Usage. Do you mean music[(+/-)Volume]?");
                }
            }

            if (message.equals(SEARCH_COMMAND)) {
                SocketTransfer.getInstance().echoToConsole("Wrong usage! Use [_] to Replace the Blank!");
                return;
            }

            if (message.contains(STOP_COMMAND)) {
                if (isPlaying) {
                    stopMusic();
                    return;
                }
            }

            if (message.contains(SEARCH_COMMAND)) {
                String title = message.substring(SEARCH_COMMAND.length());
                SocketTransfer.getInstance().echoToConsole("Search: " + title + ", Please wait...");
                ConsoleManager.getConsole().printToConsole("MusicSearch: [API: " + ConfigLoader.getConfigObject().getMusicSource() + ",Sender: Console,Name: " + title + "]");
                List<MusicResult> musicResults = MusicManager.getMusicSource().searchMusic(title,30);
                if (musicResults.isEmpty()) {
                    SocketTransfer.getInstance().echoToConsole("Search Result is Empty!");
                    return;
                }

                previousResult = musicResults;
                SocketTransfer.getInstance().pushToConsole("con_filter_text_out \"=\";key_listboundkeys");
                SocketTransfer.getInstance().echoToConsole("Music List: ");
                for (int i = 0; i < previousResult.size(); i++) {
                    MusicResult musicResult = previousResult.get(i);
                    SocketTransfer.getInstance().echoToConsole(i + ". Music: " + musicResult.getName() + " By: " + musicResult.getAuthor());
                }
                SocketTransfer.getInstance().echoToConsole("Use : " + SELECT_COMMAND + "0-" + (previousResult.size() - 1) + " to Play music!");

            }

            if (message.contains(SELECT_COMMAND)) {
                int musicCount;
                try {
                    musicCount = Integer.parseInt(message.substring(SELECT_COMMAND.length()).trim());
                } catch (Exception e) {
                    SocketTransfer.getInstance().echoToConsole("Couldn't parse the Select Command: [" + message + "]! Please check the Input!");
                    return;
                }

                File music;
                if (isLocalVersion) {
                    music = SoxSoundUtils.getCachedMusics().get(musicCount);
                    playMusic(music);
                } else {
                    MusicResult result = previousResult.get(musicCount);
                    SocketTransfer.getInstance().echoToConsole("Selected " + musicCount + ". " + result.getName() + " - " + result.getAuthor() + ", Download and cache now...");
                    File file = MusicManager.getMusicSource().downloadMusic(result);
                    if (file == null) {
                        SocketTransfer.getInstance().echoToConsole("Music download Failed! Please Try again!");
                        return;
                    }
                    if (file == MusicSource.NEED_PAY_FILE) {
                        SocketTransfer.getInstance().echoToConsole("Music Need pay! Please select Another music!");
                        return;
                    }
                    SoxSoundUtils.cacheMusic(file, CustomMusicFunction::playMusic);
                }
            }
        }
    }

    @Override
    public void onHookPlayerChat(String name, String content) {
        if (!(content.startsWith(SEARCH_COMMAND) ||
                content.startsWith(PLAY_COMMAND) ||
                content.startsWith(STOP_COMMAND) ||
                content.startsWith(SELECT_COMMAND) ||
                content.startsWith(HELP_COMMAND))) {
            return;
        }

        if (isLocalVersion) {
            return;
        }

        if (!isEnableSearch) {
            sayTeamRadio(TextMarker.Red.getHumanCode() + "点歌功能已被禁用.");
            return;
        }

        SocketTransfer.getInstance().pushToConsole("con_filter_text_out \"=\";key_listboundkeys");
        if (content.contains(SEARCH_COMMAND)) {
            if (content.equals(SEARCH_COMMAND)) {
                sayTeamRadio(TextMarker.Gold.getHumanCode() + "请输入歌曲名称! (" + SEARCH_COMMAND + " 音乐名称)");
                return;
            }
            String searchName = content.substring(7);
            ConsoleManager.getConsole().printToConsole("MusicSearch: [API: " + ConfigLoader.getConfigObject().getMusicSource() + ",Sender: " + name + ",Name: " + searchName + "]");
            if (!SocketTransfer.getInstance().getPlayerName().equals(name)) { // 避免因自己发送搜索命令后再发送信息导致发送过快无法发送聊天
                sayTeamMessage("正在搜索,请稍等...");
            } else {
                SocketTransfer.getInstance().echoToConsole("Searching...");
            }
            List<MusicResult> musicResults = MusicManager.getMusicSource().searchMusic(searchName);
            if (musicResults.isEmpty()) {
                sayTeamRadio(TextMarker.Red.getHumanCode() + "搜索返回内容为0!");
                return;
            }

            previousResult = musicResults;
            int count = 0;
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < musicResults.size(); i++) {
                MusicResult musicResult = musicResults.get(i);
                builder.append(TextMarker.Red.getHumanCode()).append(i).append(".")
                        .append(TextMarker.Gold.getHumanCode()).append(musicResult.getName())
                        .append(TextMarker.Grey.getHumanCode()).append("--")
                        .append(TextMarker.Blue.getHumanCode()).append(musicResult.getAuthor())
                        .append(TextMarker.Wrap.getHumanCode());
//            System.out.println("Debug: c>" + count);
                if (++count == 3) {
//                System.out.println("Debug: print");
                    sayTeamRadio(builder.toString());
                    builder = new StringBuilder();
                    count = 0;
                }
            }
            sayTeamMessage("请打开聊天查看所有内容,使用" + PLAY_COMMAND + " [序号] 来播放.");
        } else if (content.contains(PLAY_COMMAND)) {
            if (content.equals(PLAY_COMMAND)) {
                if (previousResult.isEmpty()) {
                    sayTeamRadio(TextMarker.Red.getHumanCode() + "请先使用" + SEARCH_COMMAND + " [歌曲名称] 搜索后再进行播放!");
                } else {
                    sayTeamRadio(TextMarker.Red.getHumanCode() + "请使用" + PLAY_COMMAND + " [歌曲序号]来进行播放!");
                }
                return;
            }
            int musicCount;
            try {
                musicCount = Integer.parseInt(content.substring(PLAY_COMMAND.length()).trim());
            } catch (Exception e) {
                sayTeamRadio(TextMarker.Red.getHumanCode() + "解析序号失败!");
                return;
            }
            sayTeamRadio(TextMarker.Blue.getHumanCode() + "正在缓存...请稍后.");

            File downloadMusic = MusicManager.getMusicSource().downloadMusic(previousResult.get(musicCount));
            int retryCount = 0;
            while (downloadMusic == null) {
                if (++retryCount >= 5) {
                    sayTeamRadio(TextMarker.Red.getHumanCode() + "音乐下载失败!请重试!");
                    return;
                }
                downloadMusic = MusicManager.getMusicSource().downloadMusic(previousResult.get(musicCount));
            }

            if (downloadMusic == MusicSource.NEED_PAY_FILE) {
                sayTeamMessage(TextMarker.Red.getHumanCode() + "该音乐拥有版权,无法下载!请更换其他音乐!");
                return;
            }

            File finalDownloadMusic = downloadMusic;
            SoxSoundUtils.cacheMusic(downloadMusic,(music) -> {
                ConsoleManager.getConsole().printToConsole("Music " + finalDownloadMusic.getName() + " Ready.");
                if (isPlaying)
                    isPlaying = false;
                playMusic(music);
            });
        } else if (content.equals(STOP_COMMAND)) {
            if (isPlaying) {
                stopMusic();
            }
        }
    }

    private void registerCommandHook() {
        SocketTransfer.getInstance().pushToConsole("alias " + LOBBY_PLAY_COMMAND + " \"echo " + LOBBY_MUSIC_WATCHER_HEAD + "_MusicPlay\"");
        SocketTransfer.getInstance().pushToConsole("alias " + LOBBY_START_COMMAND + " \"echo " + LOBBY_MUSIC_WATCHER_HEAD + "_MusicPlay\"");
        SocketTransfer.getInstance().pushToConsole("alias " + LOBBY_PAUSE_COMMAND + " \"echo " + LOBBY_MUSIC_WATCHER_HEAD + "_MusicPause\"");
        SocketTransfer.getInstance().pushToConsole("alias " + LOBBY_STOP_COMMAND + " \"echo " + LOBBY_MUSIC_WATCHER_HEAD + "_MusicStop\"");
    }

    @Override
    public void onApplicationReboot() {
        registerCommandHook();
    }

    private static void sayTeamMessage(String s) {
        SocketTransfer.getInstance().pushToConsole("say_team \"" + s + "\"");
    }

    private static void sayTeamRadio(String s) {
        CustomRadioFunction.sendCustomRadio(s);
    }

    public static void playMusic(File cachedMusic) {
        if (isPlaying) {
            return;
        }

        File inputFile = new File(SteamUtils.getCsgoPath(),"voice_input.wav");
        if (inputFile.exists()) {
            if (!inputFile.delete()) {
                ConsoleManager.getConsole().printError("Try delete Exist VoiceInput failed!");
                return;
            }
        }

        try {
            Files.copy(cachedMusic.toPath(), inputFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try copy File Throw Exception!");
            ConsoleManager.getConsole().printException(e);
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
}
