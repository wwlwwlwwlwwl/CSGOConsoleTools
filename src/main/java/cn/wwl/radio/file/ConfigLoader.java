package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.FunctionExecutor;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    private static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().serializeNulls().create();
    public static final File CONFIG_FILE = new File(".", "Config.json");
    private static Charset CONFIG_CHARSET = Charset.defaultCharset();
    private static ConfigObject configObject = null;


    public static ConfigObject getConfigObject() {
        return configObject;
    }

    public static Charset getConfigCharset() {
        return CONFIG_CHARSET;
    }

    public static boolean loadConfigObject(boolean forceReload) {
        if (!forceReload && configObject != null) {
            return false;
        }

        checkConfigExists();
        CONFIG_CHARSET = getFileCharset(CONFIG_FILE);
        try {
            JsonElement element = JsonParser.parseReader(new FileReader(CONFIG_FILE, CONFIG_CHARSET));
            configObject = GSON.fromJson(element, ConfigObject.class);
            checkConfigUpdate(element, configObject);
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try parse the Config file Throw Exception! Check your config File!");
            e.printStackTrace();
            if (configObject == null) { //第一次加载直接抛出异常 退出程序
                System.exit(1);
            }
            return false;
        }

        ConfigWatcher.startWatchConfig();

        if (forceReload) {
            FunctionExecutor.reloadModules();
        }
        return true;
    }

    public static void writeConfigObject() {
        if (configObject == null) {
            ConsoleManager.getConsole().printToConsole("configObject is null!");
            return;
        }
        writeObject(true);
    }

    public static Charset getFileCharset(File file) {
        try {
            if (!file.isFile() || !file.exists()) { //判断文件是否存在
                return Charset.defaultCharset();
            }
            InputStream inputStream = new FileInputStream(file);
            byte[] head = new byte[3];
            inputStream.read(head);
            String code = "GBK";
            if (head[0] == -1 && head[1] == -2) {
                code = "UTF-16";
            } else if (head[0] == -2 && head[1] == -1) {
                code = "Unicode";
            } else if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
                code = "UTF-8";
            } else {
                byte[] text = new byte[(int) file.length()];
                System.arraycopy(head, 0, text, 0, 3);
                inputStream.read(text, 3, text.length - 3);
                for (int i = 0; i < text.length; i++) {
                    int a = text[i] & 0xFF;
                    int b = text[i + 1] & 0xFF;
                    if (a > 0x7F) {//排除开头的英文或者数字字符
                        if (0xE3 < a & a < 0xE9 && b > 0x7F && b < 0xC0) {//符合utf8
                            code = "UTF-8";
                            break;
                        } else break;
                    }
                }
            }
            return Charset.forName(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Charset.defaultCharset();
    }

    private static void checkConfigExists() {
        if (!CONFIG_FILE.exists() || CONFIG_FILE.length() == 0) {
            ConsoleManager.getConsole().printToConsole("Config file not exists!");
            try {
                CONFIG_FILE.createNewFile();
                writeObject(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkConfigUpdate(JsonElement element, ConfigObject object) {
        Field[] fields = object.getClass().getDeclaredFields();
        JsonObject jsonObject = element.getAsJsonObject();
        List<Field> updateFields = new ArrayList<>();
        for (Field field : fields) {
            if (!jsonObject.has(field.getName())) {
                ConsoleManager.getConsole().printToConsole("Config file not have Value : " + field.getName());
                updateFields.add(field);
            }
        }

        if (!updateFields.isEmpty()) {
            ConfigObject exampleObject = new ConfigObject();
            for (Field updateField : updateFields) {
                try {
                    Field exampleField = exampleObject.getClass().getDeclaredField(updateField.getName());
                    exampleField.setAccessible(true);
                    Object o = exampleField.get(exampleObject);

                    updateField.setAccessible(true);
                    updateField.set(object, o);
                } catch (Exception e) {
                    ConsoleManager.getConsole().printToConsole("Set value for " + updateField.getName() + " Failed!");
                    e.printStackTrace();
                }
            }

            configObject = object;
            writeObject(true);
            ConsoleManager.getConsole().printToConsole("Config file updated! Please check the config file!");
            System.exit(0);
        }
    }

    private static void writeObject(boolean writeCache) {
        try {
            ConfigWatcher.cancelOnce();
            String data = writeCache ? GSON.toJson(configObject) : GSON.toJson(new ConfigObject());
//            System.out.println("DEBUG: [" + data + "]");
            FileWriter writer = new FileWriter(CONFIG_FILE, CONFIG_CHARSET);
            writer.write(data);
            writer.flush();
            writer.close();
            ConsoleManager.getConsole().printToConsole("Config file saved.");
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try write config but throw Exception!");
            e.printStackTrace();
        }
    }
}
