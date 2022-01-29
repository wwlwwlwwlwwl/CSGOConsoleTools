package cn.wwl.radio.file;

import cn.wwl.radio.utils.TextMarker;

import java.util.*;

public class ConfigObject {

    private boolean musicNetworkSearch = true;
    private boolean autoReloadConfig = true;
    private boolean lobbyMusic = true;
    private boolean usingLauncher = false;

    private int gamePort = 10090;
    private int remoteConsolePort = 29999;

    private String prefix = "jw";
    private String musicSource = "Netease";
    private String gamePath = "NULL";
    private String previousName = "NULL";

    private static final Map<String, String> _TIP_MAP = Map.of(
            "musicNetworkSearch", "开启为使用在线API搜索音乐,关闭则为使用本地音乐",
            "autoReloadConfig", "在检测到配置文件修改时自动重读",
            "lobbyMusic", "是否使用背景音乐,使用时请关闭游戏内主界面音乐盒音量",
            "usingLauncher", "使用启动器模式,在游戏退出时仍会播放LobbyMusic",
            "prefix", "所有命令的前缀,在游戏控制台直接输入时的前缀",
            "musicSource","在线搜索音乐的来源,可以为网易云[Netease]或QQ音乐[QQ]",
            "gamePath", "游戏所在的位置,音乐缓存会存储在那里,如果无法自动定位可尝试手动设置",
            "previousName", "上次启动游戏时的玩家ID, 用于在某些需要玩家ID的地方显示",
            "gamePort", "游戏-netconport的端口,一般无需修改",
            "remoteConsolePort", "游戏内netconport的重定向,可以通过telnet等连接"
    );

    public static String getTip(String title) {
        if (_TIP_MAP.containsKey(title)) {
            return _TIP_MAP.get(title);
        }
        return "[Unknown Setting]";
    }

    private Map<String, String> autoReplaceCommand = new HashMap<>();

    private List<ModuleObject> moduleList = new ArrayList<>(List.of(ModuleObject.getTemplate()));

    public String getPrefix() {
        return prefix;
    }

    public List<ModuleObject> getModuleList() {
        return moduleList;
    }

    public Map<String, String> getAutoReplaceCommand() {
        return autoReplaceCommand;
    }

    public int getRemoteConsolePort() {
        return remoteConsolePort;
    }

    public int getGamePort() {
        return gamePort;
    }

    public boolean isAutoReloadConfig() {
        return autoReloadConfig;
    }

    public String getMusicSource() {
        return musicSource;
    }


    public boolean isMusicNetworkSearch() {
        return musicNetworkSearch;
    }

    public boolean isLobbyMusic() {
        return lobbyMusic;
    }

    public boolean isUsingLauncher() {
        return usingLauncher;
    }

    public ConfigObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ConfigObject setAutoReloadConfig(boolean autoReloadConfig) {
        this.autoReloadConfig = autoReloadConfig;
        return this;
    }

    public ConfigObject setGamePort(int gamePort) {
        this.gamePort = gamePort;
        return this;
    }

    public ConfigObject setRemoteConsolePort(int remoteConsolePort) {
        this.remoteConsolePort = remoteConsolePort;
        return this;
    }

    public ConfigObject setMusicNetworkSearch(boolean musicNetworkSearch) {
        this.musicNetworkSearch = musicNetworkSearch;
        return this;
    }

    public ConfigObject setMusicSource(String musicSource) {
        this.musicSource = musicSource;
        return this;
    }

    public ConfigObject setLobbyMusic(boolean lobbyMusic) {
        this.lobbyMusic = lobbyMusic;
        return this;
    }

    public String getGamePath() {
        return gamePath;
    }

    public ConfigObject setGamePath(String gamePath) {
        this.gamePath = gamePath;
        return this;
    }

    public String getPreviousName() {
        return previousName;
    }

    public ConfigObject setPreviousName(String previousName) {
        this.previousName = previousName;
        return this;
    }

    public static class ModuleObject {
        private String name;
        private boolean enabled;
        private String note;
        private String command;
        private String function;
        private List<String> parameter;

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getNote() {
            return note;
        }

        public String getCommand() {
            return command;
        }

        public String getFunction() {
            return function;
        }

        public List<String> getParameter() {
            return parameter;
        }

        public ModuleObject setParameter(List<String> parameter) {
            this.parameter = parameter;
            return this;
        }

        public ModuleObject setName(String name) {
            this.name = name;
            return this;
        }

        public ModuleObject setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ModuleObject setNote(String note) {
            this.note = note;
            return this;
        }

        public ModuleObject setCommand(String command) {
            this.command = command;
            return this;
        }

        public ModuleObject setFunction(String function) {
            this.function = function;
            return this;
        }

        @Override
        public String toString() {
            return "ModuleObject{" +
                    "name='" + name + '\'' +
                    ", enabled=" + enabled +
                    ", note='" + note + '\'' +
                    ", command='" + command + '\'' +
                    ", function='" + function + '\'' +
                    ", parameter=" + parameter +
                    '}';
        }

        public static ModuleObject create() {
            ModuleObject moduleObject = new ModuleObject();
            moduleObject.name = "New Module " + new Random().nextInt(10000);
            moduleObject.enabled = true;
            moduleObject.note = "在这里可以填写任意内容,用于备忘";
            moduleObject.command = "在游戏里调用这里的命令";
            moduleObject.function = "CustomRadio";
            moduleObject.parameter = List.of("ThrillEmote", "#gold#Hello world!");
            return moduleObject;
        }

        public static ModuleObject getTemplate() {
            StringBuilder builder = new StringBuilder("Color: ");
            for (TextMarker value : TextMarker.availableColors()) {
                builder.append(value.getHumanCode()).append(value.getHumanCode().replace("#", "")).append(" ");
            }
            builder.append("#random# random");
            String colorExample = builder.toString().trim();

            ModuleObject moduleObject = new ModuleObject();
            moduleObject.name = "ColorExample";
            moduleObject.enabled = true;
            moduleObject.note = "这是一个演示使用的Module,用于演示如何配置模板,这一个Module将会在执行时输出所有可用的无线电颜色.";
            moduleObject.command = "color";
            moduleObject.function = "CustomRadio";
            moduleObject.parameter = List.of("ThrillEmote", colorExample);
            return moduleObject;
        }
    }
}
