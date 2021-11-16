package cn.wwl.radio.file;

import cn.wwl.radio.utils.TextMarker;

import java.util.*;

public class ConfigObject {
    private String prefix = "jw";
    private boolean autoReloadConfig = true;
    private int gamePort = 10090;
    private int remoteConsolePort = 29999;
    private boolean musicNetworkSearch = true;
    private String APIToken = "None";
    private String musicSource = "Netease";
    private boolean lobbyMusic = true;
    private List<TextMarker> randomColors = TextMarker.availableColors();
    private Map<String, String> autoReplaceCommand = new HashMap<>(Map.of(
            "0radioYes", "agree"
            , "0radioNo", "disagree"
            , "0radioThanks", "thank"
            , "0radioNice", "nice"
            , "0radioGoodzz", "zzyyds"
            , "0radioGoGo", "go"
            , "0radioWhat", "what"
            , "0radioVerynice", "wow"
    ));

    private List<ModuleObject> moduleList = new ArrayList<>(List.of(ModuleObject.getTemplate()));

    public String getPrefix() {
        return prefix;
    }

    public List<ModuleObject> getModuleList() {
        return moduleList;
    }

    public List<TextMarker> getRandomColors() {
        return randomColors;
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

    public String getAPIToken() {
        return APIToken;
    }

    public boolean isMusicNetworkSearch() {
        return musicNetworkSearch;
    }

    public boolean isLobbyMusic() {
        return lobbyMusic;
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

    public ConfigObject setAPIToken(String APIToken) {
        this.APIToken = APIToken;
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
