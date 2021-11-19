package cn.wwl.radio.executor.functions;

import cn.wwl.radio.console.impl.gui.MinimizeTrayConsole;
import cn.wwl.radio.music.MusicManager;
import cn.wwl.radio.music.MusicResult;
import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.music.MusicSource;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.file.SoxSoundUtils;
import cn.wwl.radio.file.SteamUtils;
import cn.wwl.radio.utils.TextMarker;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.PausablePlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class CustomMusicFunction implements ConsoleFunction {

    private static String volumeKey = "K";
    private static boolean isPlaying = false;
    private static boolean isBootFailed = false;
    private static boolean isInited = false;

    private static boolean cacheFailed = false;
    private static boolean isAllowPlayMusic;
    private static boolean isHookedMusicCommand = false;

    //在控制台出现flare的时候肯定是结算界面 但是同时也会出现globe 而这时候玩家可能还没有退出游戏 所以写一个定时器在获取到flare之后暂时禁用globe的获取
    private static boolean disableGlobe = false;

    private static boolean isEnableSearch = true;
    private static boolean isLocalVersion = !ConfigLoader.getConfigObject().isMusicNetworkSearch();
    private static boolean enableLobbyMusic = ConfigLoader.getConfigObject().isLobbyMusic();
    private static final List<MusicResult> previusResult = new ArrayList<>();
    private static final List<File> lobbyMusics = new ArrayList<>();
    private static final List<File> playedMusics = new ArrayList<>();
    private static PausablePlayer player;

    private static float volume = -20.0F;
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
        return List.of(LOCALVERSION_SELECT,"voicerecord","StopMusicPlaying");
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

        if (enableLobbyMusic) {
            playLobbyMusic();
        }

//        if (isBootFailed) {
//            return;
//        }
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
            return;
        }

        SocketTransfer.getInstance().pushToConsole("key_listboundkeys;clear");
        SocketTransfer.getInstance().echoToConsole("Music List: ");
        for (int i = 0; i < SoxSoundUtils.getCachedMusics().size(); i++) {
            File music = SoxSoundUtils.getCachedMusics().get(i);
            SocketTransfer.getInstance().echoToConsole(i + ". " + music.getName());
        }
        SocketTransfer.getInstance().echoToConsole("Use : " + LOCALVERSION_SELECT + "0-" + (SoxSoundUtils.getCachedMusics().size() - 1) + " to Play music!");
    }

    @Override
    public void onHookSpecialMessage(String message) {
        if (message.contains("cancelselect") || message.contains("bind")) {
            return;
        }

        if (message.contains("\"+voicerecord\"")) {
//            "v" = "+voicerecord"
            volumeKey = message.split("\" = \"")[0].replace("\"","").trim();
            ConsoleManager.getConsole().printToConsole("Find Volume Key: " + volumeKey);
            return;
        }

        if (message.contains("StopMusicPlaying") && !message.contains("echo")) {
            stopMusic();
            return;
        }

        if (message.contains(LOCALVERSION_SELECT)) {
            String removeUnknownHead = FunctionExecutor.removeUnknownHead(message);
            int musicCount = Integer.parseInt(removeUnknownHead.substring(LOCALVERSION_SELECT.length()).trim());
            File music = SoxSoundUtils.getCachedMusics().get(musicCount);
            playMusic(music);
        }
    }

    private static final String LOCALVERSION_SELECT = "!select";
    private static final String SEARCH_COMMAND = "#search";
    private static final String PLAY_COMMAND = "#play";
    private static final String STOP_COMMAND = "#stop";

    @Override
    public void onHookPlayerChat(String name, String content) {
        if (!(content.startsWith(SEARCH_COMMAND) ||
                content.startsWith(PLAY_COMMAND) ||
                content.startsWith(STOP_COMMAND))) {
            return;
        }

        if (isLocalVersion) {
            return;
        }

        if (!isEnableSearch) {
            sayTeamRadio(TextMarker.红色.getHumanCode() + "点歌功能已被禁用.");
            return;
        }

        SocketTransfer.getInstance().pushToConsole("key_listboundkeys;clear");
        if (content.contains(SEARCH_COMMAND)) {
            if (content.equals(SEARCH_COMMAND)) {
                sayTeamRadio(TextMarker.金色.getHumanCode() + "请输入歌曲名称! (" + SEARCH_COMMAND + " 音乐名称)");
                return;
            }
            String searchName = content.substring(7);
            ConsoleManager.getConsole().printToConsole("Call " + ConfigLoader.getConfigObject().getMusicSource() + " API Search : " + searchName);
            if (!SocketTransfer.getInstance().getPlayerName().equals(name)) {
                sayTeamMessage("正在搜索,请稍等...");
            }
            List<MusicResult> musicResults = MusicManager.getMusicSource().searchMusic(searchName);
            if (musicResults.isEmpty()) {
                sayTeamRadio(TextMarker.红色.getHumanCode() + "搜索返回内容为0!");
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
                    sayTeamRadio(builder.toString());
                    builder = new StringBuilder();
                    count = 0;
                }
            }
            sayTeamMessage("请打开聊天查看所有内容,使用" + PLAY_COMMAND + " [序号] 来播放.");
        } else if (content.contains(PLAY_COMMAND)) {
            if (content.equals(PLAY_COMMAND)) {
                if (previusResult.isEmpty()) {
                    sayTeamRadio(TextMarker.红色.getHumanCode() + "请先使用" + SEARCH_COMMAND + " [歌曲名称] 搜索后再进行播放!");
                } else {
                    sayTeamRadio(TextMarker.红色.getHumanCode() + "请使用" + PLAY_COMMAND + " [歌曲序号]来进行播放!");
                }
                return;
            }
            int musicCount;
            try {
                musicCount = Integer.parseInt(content.substring(PLAY_COMMAND.length()).trim());
            } catch (Exception e) {
                sayTeamRadio(TextMarker.红色.getHumanCode() + "解析序号失败!");
                return;
            }
            sayTeamRadio(TextMarker.蓝色.getHumanCode() + "正在缓存...请稍后.");

            List<File> temp_list = new ArrayList<>();
            File downloadMusic = MusicManager.getMusicSource().downloadMusic(previusResult.get(musicCount));
            int retryCount = 0;
            while (downloadMusic == null) {
                if (++retryCount >= 5) {
                    sayTeamRadio(TextMarker.红色.getHumanCode() + "音乐下载失败!请重试!");
                    return;
                }
                downloadMusic = MusicManager.getMusicSource().downloadMusic(previusResult.get(musicCount));
            }

            if (downloadMusic == MusicSource.NEED_PAY_FILE) {
                sayTeamMessage(TextMarker.红色.getHumanCode() + "该音乐拥有版权,无法下载!请更换其他音乐!");
                return;
            }

            SoxSoundUtils.cacheMusic(downloadMusic,temp_list);
            while (temp_list.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
            ConsoleManager.getConsole().printToConsole("Music " + downloadMusic.getName() + " Ready.");
            playMusic(temp_list.get(0));
        } else if (content.equals(STOP_COMMAND)) {
            if (isPlaying) {
                stopMusic();
            }
        }
    }

    private void playLobbyMusic() {
        registerMusicHook();
        if (lobbyMusics.isEmpty() && !cacheFailed) {
            cacheLobbyMusic();
        }

        if (cacheFailed) {
            return;
        }

        if (!isAllowPlayMusic) {
            if (player != null) {
                stopLobbyMusic();
            }
            return;
        }

        if (player != null) {
            switch (player.getPlayerStatus()) {
                case PausablePlayer.FINISHED -> {
                    player = null;
                    startLobbyMusic();
                }
                case PausablePlayer.PAUSED -> player.fadeResume();
                case PausablePlayer.NOTSTARTED -> {
                    try {
                        player.play();
                    } catch (Exception e) {
                        startLobbyMusic();
                    }
                }
            }
        } else {
            startLobbyMusic();
        }
    }

    public static void startLobbyMusic() {
        ConsoleManager.getConsole().printToConsole("Start Lobby Music.");
        Random random = new Random();
        if (lobbyMusics.size() == playedMusics.size()) {
            playedMusics.clear();
        }
        File music = null;
        while (music == null) {
            File tempMusic = lobbyMusics.get(random.nextInt(lobbyMusics.size() - 1));
//            System.out.println("Random new Music: " + tempMusic.getName());
             if (!playedMusics.contains(tempMusic)) {
                 playedMusics.add(tempMusic);
                 music = tempMusic;
             }
        }
        try {
            if (player != null) {
                player.fadeExit();
                player = null;
            }
            player = new PausablePlayer(new FileInputStream(music));
            player.play();
            SocketTransfer.getInstance().echoToConsole("Now playing Music: " + music.getName());
            ConsoleManager.getConsole().printToConsole("Now playing Music: " + music.getName());
            MinimizeTrayConsole.updatePopupMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopLobbyMusic() {
        stopLobbyMusic(true);
    }

    public static void stopLobbyMusic(boolean fade) {
        if (player == null) {
            return;
        }

        if (fade) {
            player.fadeExit();
        } else {
            player.stop();
            player.close();
        }
        player = null;
        isAllowPlayMusic = false;
        MinimizeTrayConsole.updatePopupMenu();
    }

    public static void pauseLobbyMusic() {
        if (player == null) {
            return;
        }

        if (player.getPlayerStatus() == PausablePlayer.PLAYING) {
            player.fadePause();
        } else if (player.getPlayerStatus() == PausablePlayer.PAUSED) {
            player.fadeResume();
        } else {
            stopLobbyMusic();
            startLobbyMusic();
        }
    }

    public static void setLobbyMusicGain(float targetVolume,boolean fade) {
        if (player == null) {
            return;
        }
        volume = targetVolume;
        if (fade) {
            player.fadeSetGain(volume);
        } else {
            player.setGain(volume);
        }
    }

    public static void setLobbyMusicGain(float targetVolume) {
        setLobbyMusicGain(targetVolume,true);
    }

    public static float getLobbyMusicGain() {
        if (player == null) {
            return volume;
        }
        return player.getGain();
    }

    public static int getLobbyMusicStatus() {
        if (player == null) {
            return PausablePlayer.NOTINIT;
        }
        return player.getPlayerStatus();
    }

    private void registerMusicHook() {
        if (isHookedMusicCommand) {
            return;
        }
        isHookedMusicCommand = true;
        SocketTransfer.getInstance().pushToConsole("alias music_play \"echo LobbyMusicWatcher_MusicPlay\"");
        SocketTransfer.getInstance().pushToConsole("alias music_pause \"echo LobbyMusicWatcher_MusicPause\"");
        SocketTransfer.getInstance().pushToConsole("alias music_stop \"echo LobbyMusicWatcher_MusicStop\"");
        SocketTransfer.getInstance().pushToConsole("status");
//        SocketTransfer.getInstance().pushToConsole("alias music_random \"echo LobbyMusicWatcher_MusicRandom\"");
        JavaSoundAudioDevice.listenEvent(e -> e.setVolume(volume));
    }

    private void cacheLobbyMusic() {
        File musicDir = SoxSoundUtils.getMusicDir();
        File lobbyDir = new File(musicDir,"lobby");
        if (!lobbyDir.exists()) {
            if (!lobbyDir.mkdir()) {
                ConsoleManager.getConsole().printError("Make lobbyMusic dir Failed!");
                cacheFailed = true;
                return;
            }
        }

        File[] files = lobbyDir.listFiles();
        if (files == null || files.length == 0) {
            ConsoleManager.getConsole().printToConsole("LobbyMusic dir is Empty! Please place music In folder!");
            cacheFailed = true;
            return;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".mp3")) {
                ConsoleManager.getConsole().printError("LobbyMusic " + file + " is Unknown Format! please Format to MP3!");
                continue;
            }
            lobbyMusics.add(file);
        }
    }

    private static void sayTeamMessage(String s) {
        SocketTransfer.getInstance().pushToConsole("say_team \"" + s + "\"");
    }

    private static void sayTeamRadio(String s) {
        CustomRadioFunction.sendCustomRadio(s);
    }

    public static void playMusic(File cachedMusic) {
        if (isPlaying || !isInited) {
            return;
        }

        File inputFile = new File(SteamUtils.getCsgoPath(),"voice_input.wav");
        if (inputFile.exists()) {
            if (inputFile.delete()) {
                ConsoleManager.getConsole().printError("Try delete Exist VoiceInput failed!");
                return;
            }
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
        if (ConfigLoader.getConfigObject().isLobbyMusic()) {
            SocketTransfer.getInstance().addListenerTask("LobbyMusicWatcher",str -> {
                String playerName = SocketTransfer.getInstance().getPlayerName();

                if ((playerName == null || str.contains(playerName)) && str.contains("已连接")) { //玩家连接入服务器后的文字
                    isAllowPlayMusic = false;
                    disableGlobe = false;
                    return;
                }

                if (str.contains("Not connected to server")) { //Status 获取玩家不在服务器中
                    isAllowPlayMusic = true;
                    return;
                }

                if (str.contains("materials/panorama/images/ui_textures/flare.png")) { //获取资源失败 结束应该会有这个吧...
                    isAllowPlayMusic = true;
                    //只要出现结算 那么就一直忽略这个指令 直到玩家重新连接入新游戏为止
                    disableGlobe = true;
                    return;
                }

                if (!disableGlobe) {
                    if (str.contains("materials\\panorama\\images\\icons\\ui\\globe.svg")) { //断开连接似乎会出现这个 !暂停也会出现!
                        SocketTransfer.getInstance().pushToConsole("status");
                        return;
                    }
                }

                if (str.contains("# userid name uniqueid")) { //Status 玩家在游戏中
                    isAllowPlayMusic = false;
                    return;
                }

                if (str.contains("Unknown") && str.toLowerCase(Locale.ROOT).contains("music")) {
                    String newStr = FunctionExecutor.removeUnknownHead(str);
//                System.out.println("str: " + str + ", newStr: " + newStr);
                    try {
                        volume = Float.parseFloat(newStr.substring(5));
                        player.fadeSetGain(volume);
                        ConsoleManager.getConsole().printToConsole("Music volume Set to " + volume);
                        SocketTransfer.getInstance().echoToConsole("Music volume Set to " + volume);
                    } catch (Exception e) {
//                    ConsoleManager.getConsole().printError("Try parse Volume value: [" + str + "] Throw exception!");
//                    e.printStackTrace();
                        SocketTransfer.getInstance().echoToConsole("Wrong Music command. Do you mean music[(+/-)Volume]?");
                    }
                }

                if (str.toLowerCase(Locale.ROOT).contains("lobbymusicwatcher_")) {
                    String command = str.substring(18).trim();
//                System.out.println("Command: " + command);
                    switch (command) {
                        case "MusicPlay" -> {
                            if (player != null && player.getPlayerStatus() == PausablePlayer.PAUSED) {
                                ConsoleManager.getConsole().printToConsole("LobbyMusic Resume");
                                SocketTransfer.getInstance().echoToConsole("LobbyMusic now Resume.");
                                player.fadeResume();
                            }
                            if (!isAllowPlayMusic) {
                                ConsoleManager.getConsole().printToConsole("ForceUnlock lobbyMusic");
                                SocketTransfer.getInstance().echoToConsole("Force Unlocking LobbyMusic...");
                                isAllowPlayMusic = true;
                                player = null;
                            }
                        }
                        case "MusicPause" -> {
                            if (player != null && player.getPlayerStatus() == PausablePlayer.PLAYING) {
                                ConsoleManager.getConsole().printToConsole("LobbyMusic Pause");
                                SocketTransfer.getInstance().echoToConsole("LobbyMusic now Paused.");
                                pauseLobbyMusic();
                            }
                        }
                        case "MusicStop" -> {
                            ConsoleManager.getConsole().printToConsole("LobbyMusic Stop");
                            SocketTransfer.getInstance().echoToConsole("LobbyMusic now Stopped.");
                            isAllowPlayMusic = false;
                            stopLobbyMusic();
                        }
                    }
                }
            });
        }

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
        }
    }
}
