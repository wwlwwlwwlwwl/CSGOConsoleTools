package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RadioFileManager {

    private static volatile RadioFileManager instance;
    private static VDFNode root;
    private static File radioFile;
    private static File backupFile;

    private static final Map<RadioGroup, Map<String,RadioObject>> TITLE_KEY_MAP = new HashMap<>();
    private static final Map<String, ConfigObject.ModuleObject> ALIAS_FORMAT_MAP = new HashMap<>();
    private static final Map<RadioObject, RadioGroup> GROUP_MAP = new HashMap<>();

    private RadioFileManager() {init();}

    private void init() {
        File csgoPath = SteamUtils.getCsgoPath();
        File uiPath = new File(csgoPath, "csgo/resource/ui");
        radioFile = new File(uiPath, "radiopanel.txt");
        backupFile = new File(uiPath, "radiopanel.backup.txt");

        if (!radioFile.exists()) {
            ConsoleManager.getConsole().printError("Config CSGOLocation is Wrong! Relocate Game...");
            ConfigLoader.getConfigObject().setGamePath("NULL");
            ConfigLoader.writeConfigObject();
            return;
        }

        if (!backupFile.exists()) {
            try {
                makeBackup();
            } catch (IOException e) {
                ConsoleManager.getConsole().printError("Make radio backup Failed!");
                return;
            }
        }

        readRadioTree();
        cacheAliasMap();
    }

    private void makeBackup() throws IOException {
        if (!backupFile.createNewFile()) {
            ConsoleManager.getConsole().printError("Try create Backup file Failed!");
            throw new IOException();
        }
        Files.copy(radioFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    private void readRadioTree() {
        String radio = ConfigLoader.fileListToString(ConfigLoader.readFile(radioFile));
        VDFParser parser = new VDFParser();
        root = parser.parse(radio);
        VDFNode groups = root.getSubNode("RadioPanel.txt").getSubNode("Groups");
        for (Map.Entry<String, Object[]> entry : groups.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue()[0];
            Map<String, RadioObject> map = new LinkedHashMap<>();
            RadioGroup group = RadioGroup.getByName(key);
            TITLE_KEY_MAP.put(group, map);
            parseRadioCommands(group, (VDFNode) value, map);
        }
    }

    private void cacheAliasMap() {
        ALIAS_FORMAT_MAP.clear();
        for (ConfigObject.ModuleObject moduleObject : ConfigLoader.getConfigObject().getModuleList()) {
            String command = "0radio" + moduleObject.getCommand();
            ALIAS_FORMAT_MAP.put(command,moduleObject);
        }

        Map<String, String> autoReplaceCommand = ConfigLoader.getConfigObject().getAutoReplaceCommand();
        autoReplaceCommand.clear();
        for (ConfigObject.ModuleObject moduleObject : ConfigLoader.getConfigObject().getModuleList()) {
            if (moduleObject.getFunction().equals("CustomRadio")) {
                String command = "0radio" + moduleObject.getCommand();
                String gameCommand = ConfigLoader.getConfigObject().getPrefix() + "_" + moduleObject.getCommand();
                autoReplaceCommand.put(command, gameCommand);
            }
        }
        ConfigLoader.writeConfigObject();
    }

    private void parseRadioCommands(RadioGroup group,VDFNode titleTree, Map<String, RadioObject> objectMap) {
        VDFNode commands = titleTree.getSubNode("Commands");
        for (Map.Entry<String, Object[]> entry : commands.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue()[0];
            String hotkey = ((VDFNode) value).getString("hotkey");
            String label = ((VDFNode) value).getString("label");
            String cmd = ((VDFNode) value).getString("cmd");
            RadioObject radioObject = new RadioObject();
            radioObject.setCmd(cmd).setLabel(label).setHotkey(hotkey);
            GROUP_MAP.put(radioObject,group);
            objectMap.put(key,radioObject);
        }
    }

    public void updateVDFTree(RadioGroup group) {
        Map<String, RadioObject> objectMap = TITLE_KEY_MAP.get(group);
        VDFNode groups = root.getSubNode("RadioPanel.txt").getSubNode("Groups").getSubNode(group.getGroupName());
        VDFNode commands = groups.getSubNode("Commands");
        commands.clear();
        for (Map.Entry<String, RadioObject> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            RadioObject value = entry.getValue();
            VDFNode node = new VDFNode();
            updateVDFObject(value, node);
            commands.setValue(key,node);
        }
    }

    public String getAliasByModuleName(String moduleName) {
        for (Map.Entry<String, ConfigObject.ModuleObject> entry : ALIAS_FORMAT_MAP.entrySet()) {
            if (entry.getValue().getName().equals(moduleName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getModuleNameByAlias(String moduleAlias) {
        for (Map.Entry<String, ConfigObject.ModuleObject> entry : ALIAS_FORMAT_MAP.entrySet()) {
            if (entry.getKey().equals(moduleAlias)) {
                return entry.getValue().getName();
            }
        }
        return null;
    }

    private void updateVDFObject(RadioObject radio,VDFNode node) {
        String cmd = radio.getCmd();
        String hotkey = radio.getHotkey();
        String label = radio.getLabel();
        if (cmd == null || cmd.length() == 0) {
            ConsoleManager.getConsole().printError("Put object in VDF Tree failed! cmd is Null!");
            return;
        }

        if (hotkey == null || hotkey.length() == 0) {
            ConsoleManager.getConsole().printError("Put object in VDF Tree failed! hotkey is Null!");
            return;
        }

        if (label == null || label.length() == 0) {
            ConsoleManager.getConsole().printError("Put object in VDF Tree failed! label is Null!");
            return;
        }

        node.setValue("cmd",cmd);
        node.setValue("hotkey",hotkey);
        node.setValue("label",label);
    }

    public void saveRadioConfig() {
        try {
            System.out.println("Save Radio Config.");
            ConfigLoader.writeFile(root.toVDFConfig(), radioFile);
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try save Radio config Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
    }

    public void putRadioCommand(RadioGroup group, String name, RadioObject object) {
        Map<String, RadioObject> map = TITLE_KEY_MAP.get(group);
        if (map.containsKey(name)) {
            ConsoleManager.getConsole().printError("Try register RadioObject: " + object + " Failed! Map already Have same Name Object!");
            return;
        }

        int key = Integer.parseInt(object.getHotkey());
        if (key <= 0 || key > 9) {
            ConsoleManager.getConsole().printError("Try Register RadioObject: " + object + " Failed! Object HotKey Out of Bound!");
            return;
        }

        boolean alreadyUsed = false;
        for (RadioObject value : map.values()) {
            if (value.getHotkey().equals(object.getHotkey())) {
                alreadyUsed = true;
                break;
            }
        }

        if (alreadyUsed) {
            ConsoleManager.getConsole().printError("Try Register RadioObject: " + object + " Failed! Object Key Already Registered.");
            return;
        }

        map.put(name,object);
        GROUP_MAP.put(object,group);
        updateVDFTree(group);
    }

    public void updateObject(RadioObject object) {
        if (!GROUP_MAP.containsKey(object)) {
            ConsoleManager.getConsole().printError("RadioObject: [" + object + "] Not registered in GROUP map! Please use putRadioCommand function!");
            return;
        }

        updateVDFTree(GROUP_MAP.get(object));
    }

    public void removeObject(RadioObject object) {
        if (!GROUP_MAP.containsKey(object)) {
            ConsoleManager.getConsole().printError("Remove Object in Tree Failed! Not Found Object Group!");
            return;
        }

        RadioGroup radioGroup = GROUP_MAP.get(object);
        Map<String, RadioObject> map = TITLE_KEY_MAP.get(radioGroup);
        String targetKey = "";

        for (Map.Entry<String, RadioObject> entry : map.entrySet()) {
            if (entry.getValue() == object) {
                targetKey = entry.getKey();
                break;
            }
        }

        if (targetKey.equals("")) {
            ConsoleManager.getConsole().printError("Remove Object in Tree Failed! Item Key not Found!");
            return;
        }

        map.remove(targetKey);
        updateVDFTree(radioGroup);
    }

    public Map<String,RadioObject> getObjectsByGroup(RadioGroup group) {
        return TITLE_KEY_MAP.get(group);
    }

    public void updateGroupTitle(RadioGroup group, String title) {
        VDFNode groups = root.getSubNode("RadioPanel.txt").getSubNode("Groups").getSubNode(group.getGroupName());
        groups.setValue("title",title);
    }

    public String getGroupTitle(RadioGroup group) {
        VDFNode groups = root.getSubNode("RadioPanel.txt").getSubNode("Groups").getSubNode(group.getGroupName());
        return groups.getString("title");
    }

    public RadioObject getRadioObjectByName(String name) {
        return getRadioObjectByName(null,name);
    }

    public RadioObject getRadioObjectByName(RadioGroup group, String name) {
        if (name == null || name.length() == 0) {
            return null;
        }

        if (group == null) {
            for (Map<String, RadioObject> map : TITLE_KEY_MAP.values()) {
                if (map.containsKey(name)) {
                    return map.get(name);
                }
            }
        } else {
            for (Map.Entry<String, RadioObject> entry : TITLE_KEY_MAP.get(group).entrySet()) {
                if (entry.getKey().equals(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static RadioFileManager getInstance() {
        if (instance == null) {
            synchronized (RadioFileManager.class) {
                if (instance == null) {
                    instance = new RadioFileManager();
                }
            }
        }
        return instance;
    }

    public enum RadioGroup {
        COMMON("common"),
        STANDARD("standard"),
        GROUP("group"),
        REPORT("report");

        private final String groupName;

        RadioGroup(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupName() {
            return groupName;
        }

        public static RadioGroup getByName(String name) {
            if (name == null || name.length() == 0) {
                return null;
            }
            for (RadioGroup value : values()) {
                if (name.toLowerCase(Locale.ROOT).equals(value.name().toLowerCase(Locale.ROOT))) {
                    return value;
                }
            }
            return null;
        }
    }

    public static class RadioObject {

        private String hotkey; //用于标记按哪个键来执行
        private String label; //显示在列表中的文字
        private String cmd; //点击后执行的命令

        public RadioObject() {}

        public RadioObject(String hotkey,String label,String cmd) {
            this.hotkey = hotkey;
            this.label = label;
            this.cmd = cmd;
        }

        public String getHotkey() {
            return hotkey;
        }

        public RadioObject setHotkey(String hotkey) {
            this.hotkey = hotkey;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public RadioObject setLabel(String label) {
            this.label = label;
            return this;
        }

        public String getCmd() {
            return cmd;
        }

        public RadioObject setCmd(String cmd) {
            this.cmd = cmd;
            return this;
        }

        @Override
        public String toString() {
            return "RadioObject{" +
                    ", hotkey='" + hotkey + '\'' +
                    ", label='" + label + '\'' +
                    ", cmd='" + cmd + '\'' +
                    '}';
        }
    }
}
