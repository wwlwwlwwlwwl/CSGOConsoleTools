package cn.wwl.radio.music;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;

public class MusicManager {

    private static volatile MusicSource source;

    public static MusicSource getMusicSource() {
        if (source == null) {
            synchronized (MusicManager.class) {
                if (source == null) {
                    String musicSource = ConfigLoader.getConfigObject().getMusicSource();
                    switch (musicSource) {
                        case "Netease" -> source = new NeteaseMusicSource();
                        case "QQ" -> source = new QQMusicSource();
                        default -> {
                            ConsoleManager.getConsole().printError("Unknown MusicSource! Check the config! Use Default value Netease!");
                            source = new NeteaseMusicSource();
                        }
                    }
                }
            }
        }
        return source;
    }
}
