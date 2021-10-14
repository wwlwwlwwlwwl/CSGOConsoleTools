package cn.wwl.radio.file;

import cn.wwl.radio.utils.TextMarker;

import java.util.*;

public class ConfigObject {
    private String prefix = "jw";
    private boolean autoReloadConfig = true;
    private List<TextMarker> randomColors = TextMarker.availableColors();
    private Map<String, String> autoReplaceCommand = Map.of(
            "0radioYes", "agree"
            , "0radioNo", "disagree"
            , "0radioThanks", "thank"
            , "0radioNice", "nice"
            , "0radioGoodzz", "zzyyds"
            , "0radioGoGo", "go"
            , "0radioWhat", "what"
            , "0radioVerynice", "wow"
    );

    private List<ModuleObject> moduleList = List.of(ModuleObject.getTemplate());

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

    public boolean isAutoReloadConfig() {
        return autoReloadConfig;
    }

    public static class ModuleObject {
        private String name;
        private boolean enabled;
        private String note;
        private String command;
        private String function;
        private List<String> parameter;

        private ModuleObject() {
        }

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
