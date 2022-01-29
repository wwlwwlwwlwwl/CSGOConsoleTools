package cn.wwl.radio.music;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.console.impl.gui.MinimizeTrayConsole;
import cn.wwl.radio.file.SoxSoundUtils;
import cn.wwl.radio.network.SocketTransfer;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.PausablePlayer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundMusic {

    private static final List<File> lobbyMusics = new ArrayList<>();
    private static final List<File> playedMusics = new ArrayList<>();

    private static PausablePlayer player;
    private static float volume = -20.0F;

    private static boolean inited, failed, ready = false;

    private static boolean playerStop = false;

    public static void playBackgroundMusic(boolean forcePlay) {
        if (!isReady()) {
            return;
        }

        if (getPlayerStatus() == PausablePlayer.PAUSED) {
            player.fadeResume();
        }

        playerStop = false;
        _startMusic(forcePlay);
    }

    private static void _startMusic(boolean forcePlay) {
        Random random = new Random();
        if (lobbyMusics.size() == playedMusics.size()) {
            playedMusics.clear();
        }
        File music = null;
        while (music == null) {
            if (lobbyMusics.size() == playedMusics.size()) {
                playedMusics.clear();
            }
            File tempMusic = lobbyMusics.get(random.nextInt(lobbyMusics.size() - 1));
//            System.out.println("Random new Music: " + tempMusic.getName());
            if (!playedMusics.contains(tempMusic)) {
                playedMusics.add(tempMusic);
                music = tempMusic;
            }
        }
        try {
            if (player != null) {
                if (!forcePlay) {
                    return;
                }
                player.fadeExit();
                player = null;
            }
            player = new PausablePlayer(new FileInputStream(music));
            player.fadePlay(volume);
            player.addCallback(() -> {
                if (!playerStop) {
                    playBackgroundMusic(true);
                }
            });
            if (SocketTransfer.getInstance().isConnected())
                SocketTransfer.getInstance().echoToConsole("Now playing Music: " + music.getName());

            ConsoleManager.getConsole().printToConsole("Now playing Music: " + music.getName());
            MinimizeTrayConsole.updatePopupMenu();
        } catch (Exception e) {
            ConsoleManager.getConsole().printException(e);
        }
    }

    public static void stopBackgroundMusic(boolean instantStop) {
        if (!isReady()) {
            return;
        }

        if (player == null) {
            return;
        }

        playerStop = true;
        if (instantStop) {
            player.close();
        } else {
            player.fadeExit();
        }
        player = null;
    }

    public static void pauseBackgroundMusic() {
        if (!isReady()) {
            return;
        }

        if (player == null) {
            return;
        }

        if (player.getPlayerStatus() == PausablePlayer.PLAYING) {
            player.fadePause();
        }
    }

    public static boolean isReady() {
        return inited && !failed && ready;
    }

    public static boolean isPlaying() {
        return player != null && player.getPlayerStatus() == PausablePlayer.PLAYING;
    }

    public static int getPlayerStatus() {
        return player == null ? PausablePlayer.NOTINIT : player.getPlayerStatus();
    }

    public static void setVolume(float v, boolean fade) {
        volume = v;
        if (fade) {
            player.fadeSetGain(volume);
        } else {
            player.setGain(volume);
        }
    }

    public static float getVolume() {
        return volume;
    }

    public static void init() {
        if (inited) {
            return;
        }
        inited = true;
        JavaSoundAudioDevice.listenEvent(e -> e.setVolume(volume));
        cacheLobbyMusic();
    }


    private static void cacheLobbyMusic() {
        File musicDir = SoxSoundUtils.getMusicDir();
        if (musicDir == null) {
            ConsoleManager.getConsole().printError("MusicDir Not found! Cannot init LobbyMusic!");
            return;
        }

        File lobbyDir = new File(musicDir,"lobby");
        if (!lobbyDir.exists()) {
            if (!lobbyDir.mkdir()) {
                ConsoleManager.getConsole().printError("Make lobbyMusic dir Failed!");
                failed = true;
                return;
            }
        }

        File[] files = lobbyDir.listFiles();
        if (files == null || files.length == 0) {
            ConsoleManager.getConsole().printToConsole("LobbyMusic dir is Empty! Please place music In folder!");
            failed = true;
            return;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".mp3")) {
                ConsoleManager.getConsole().printError("LobbyMusic " + file + " is Unknown Format! please Format to MP3!");
                continue;
            }
            lobbyMusics.add(file);
        }

        ready = true;
    }
}
