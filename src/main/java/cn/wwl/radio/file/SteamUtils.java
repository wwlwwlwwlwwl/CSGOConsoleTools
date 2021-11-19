package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SteamUtils {
    private static File csgoPath;

    public static File getCsgoPath() {
        if (csgoPath == null) {
            initCSGODir();
        }
        return csgoPath;
    }

    public static synchronized boolean initCSGODir() {
        if (csgoPath != null) {
            return true;
        }
        ConfigObject configObject = ConfigLoader.getConfigObject();
        if (!configObject.getGamePath().equals("NULL")) {
            csgoPath = new File(configObject.getGamePath());
            if (!csgoPath.exists()) {
                ConsoleManager.getConsole().printError("Wrong Exist CSGOLocation!");
            } else {
                ConsoleManager.getConsole().printToConsole("Using Exist CSGOLocation: " + csgoPath.getAbsolutePath());
                return true;
            }
        }

        String csgoLocation = SteamUtils.getFileLocation("csgo.exe").replace("csgo.exe","");
        if (csgoLocation.equals("")) {
            ConsoleManager.getConsole().printError("Get CSGO Path failed!");
            return false;
        }

        csgoPath = new File(csgoLocation);
        configObject.setGamePath(csgoLocation);
        ConfigLoader.writeConfigObject();
        return true;
    }

    public static void patchCSGOLaunchLine() {
        String steamPath = getFileLocation("steam.exe");
        if (steamPath.equals("")) {
            ConsoleManager.getConsole().printToConsole("Could not Find steam! Maybe you need Manual add LaunchLine.");
            return;
        }

        steamPath = steamPath.substring(0, steamPath.lastIndexOf("\\"));
        File userData = new File(steamPath, "userdata");
        if (!userData.exists()) {
            ConsoleManager.getConsole().printError("Could not find Steam Userdata Path!");
            return;
        }

        File[] files = userData.listFiles((dir, name) -> !name.equals("ac"));
        if (files == null || files.length == 0) {
            ConsoleManager.getConsole().printError("Could not find Any Userdata Users!");
            return;
        }

        ConsoleManager.getConsole().printToConsole("Available Users: " + Arrays.toString(files));
        for (File file : files) {
            File userConfig = new File(file, "config/localconfig.vdf");
            if (!userConfig.exists()) {
                ConsoleManager.getConsole().printError("User " + file.getName() + " Could not Find LocalConfig!");
                continue;
            }

            try {
                patchLocalConfig(userConfig);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try parse User " + file.getName() + " Config Throw Exception!");
                e.printStackTrace();
            }
        }
        ConsoleManager.getConsole().printToConsole("Check user Config done.");
    }

    /* //Work not well, Have some Format bug. Need fix.
    private static void patchLocalConfig(File config) throws Exception {
        String cfgString = ConfigLoader.fileListToString(ConfigLoader.readFile(config));
        VDFParser parser = new VDFParser();
        VDFNode root = parser.parse(cfgString);
        VDFNode steam = root
                .getSubNode("UserLocalConfigStore")
                .getSubNode("Software")
                .getSubNode("Valve")
                .getSubNode("Steam");
        VDFNode apps = steam.containsKey("apps") ? steam.getSubNode("apps") : steam.getSubNode("Apps");
        if (!apps.containsKey("730")) {
            ConsoleManager.getConsole().printToConsole("UserConfig: " + config.getAbsolutePath() + " Not found CSGO Games!");
            return;
        }

        VDFNode csgo = apps.getSubNode("730");
        if (csgo.containsKey("LaunchOptions")) {
            String launchOptions = csgo.getString("LaunchOptions");
            if (!launchOptions.contains("-netconport")) {
                ConsoleManager.getConsole().printToConsole("Updated UserConfig: " + config.getAbsolutePath() + ", Reason: Added netConPort.");
                csgo.setValue("LaunchOptions",launchOptions + " -netconport " + ConfigLoader.getConfigObject().getGamePort());
            } else {
                if (launchOptions.contains(String.valueOf(ConfigLoader.getConfigObject().getGamePort()))) {
                    return;
                }

                ConsoleManager.getConsole().printToConsole("Updated UserConfig: " + config.getAbsolutePath() + ", Reason: Update netConPort.");
                StringBuilder newLine = new StringBuilder();
                List<String> split = Arrays.stream(launchOptions.split(" ")).toList();
                boolean patchNext = false;
                for (String s : split) {
                    if (patchNext) {
                        s = String.valueOf(ConfigLoader.getConfigObject().getGamePort());
                        patchNext = false;
                    }
                    if (s.equals("-netconport")) {
                        patchNext = true;
                    }
                    newLine.append(s).append(" ");
                }
                csgo.setValue("LaunchOptions", newLine.toString().trim());
            }
        } else {
            ConsoleManager.getConsole().printToConsole("Updated UserConfig: " + config.getAbsolutePath() + ", Reason: Added LaunchOptions.");
            csgo.setValue("LaunchOptions","-netconport " + ConfigLoader.getConfigObject().getGamePort());
        }

        ConfigLoader.writeFile(root.toVDFConfig(), new File(".","DEBUG" + Math.random() + ".txt"), ConfigLoader.getFileCharset(config));
    }
*/

    /*
     * TODO Bad Performance, need fix
     */
    private static void patchLocalConfig(File userConfig) throws Exception {
        int startLine = -1;
        int endLine = -1;
        int count = 1;
        int launchLine = -1;
        boolean startMatch = false;
        boolean patched = false;
        boolean findCSGO = false;

        BufferedReader reader = new BufferedReader(new FileReader(userConfig));
        Map<Integer, String> strMap = new HashMap<>();
        Map<Integer, String> gameMap = new HashMap<>();
        AtomicInteger tempCount = new AtomicInteger(0);
        reader.lines().forEach(s -> strMap.put(tempCount.getAndIncrement(), s));

        for (Map.Entry<Integer, String> entry : strMap.entrySet()) {
            int i = entry.getKey();
            String s = entry.getValue();
            if (s.contains("\"Steam\"")) {
                int tmp = 3;
                while (true) {
                    String str = strMap.get(i + tmp);
                    if (str.trim().equals("{")) {
                        break;
                    }
                    tmp++;
                }

                startLine = i + tmp;
                continue;
            }

            if (startLine == -1) {
                continue;
            }

            if (i == startLine) {
                startMatch = true;
                continue;
            }

            if (startMatch) {
                String trim = s.trim();
                if (trim.equals("\"730\"")) {
                    findCSGO = true;
                    gameMap.put(i, s);
                    continue;
                }

                if (findCSGO) {
                    gameMap.put(i, s);
                }

                if (trim.contains("LaunchOptions") && trim.contains("netconport")) {
                    if (!trim.contains(String.valueOf(ConfigLoader.getConfigObject().getGamePort()))) {
                        launchLine = i;
                    }
//                    ConsoleManager.getConsole().printToConsole("UserConfig: " + userConfig.getAbsolutePath() + " Already Patched.");
                    patched = true;
                    break;
                }

                if (trim.equals("{")) {
                    count++;
                } else if (trim.equals("}")) {
                    count--;
                    if (findCSGO) {
                        if (count == 1) {
                            findCSGO = false;
                            break;
                        }
                    }
                    if (count <= 0) {
                        endLine = i;
                        break;
                    }
                }
            }
        }

        if (gameMap.isEmpty()) {
            ConsoleManager.getConsole().printError("ConfigFile: " + userConfig.getAbsolutePath() + " Could not Find CSGO Running History!");
            return;
        }

        if (patched) {
            if (launchLine != -1) {
                String userLine = strMap.get(launchLine).split("\"\t\t\"")[1].replace("\"","");
                String newLine = "";
                List<String> split = Arrays.stream(userLine.split(" ")).toList();
                boolean patchNext = false;
                for (int i = 0; i < split.size(); i++) {
                    String s = split.get(i);
                    if (patchNext) {
                        s = String.valueOf(ConfigLoader.getConfigObject().getGamePort());
                        patchNext = false;
                    }
                    if (s.equals("-netconport")) {
                        patchNext = true;
                    }
                    newLine += s + " ";
                }
                String result = "\t\t\t\t\t\t\"LaunchOptions\"\t\t\"" + newLine.trim() + "\"";
                strMap.put(launchLine, result);
                ConsoleManager.getConsole().printToConsole("Updated ConfigFile: " + userConfig.getAbsolutePath() + ", Reason: Change Port.");
                saveUserConfig(userConfig, strMap);
            }
            return;
        }

//        System.out.println("START OF " + userConfig.getAbsolutePath());
        for (Map.Entry<Integer, String> entry : gameMap.entrySet()) {
            String s = entry.getValue();
            if (s.contains("\"LaunchOptions\"")) {
                launchLine = entry.getKey();
                break;
            }
        }

        if (launchLine == -1) {
            int lastPlay = -1;
            for (Map.Entry<Integer, String> entry : gameMap.entrySet()) {
                String s = entry.getValue();
                if (s.contains("\"LastPlayed\"")) {
                    lastPlay = entry.getKey();
                    break;
                }
            }

            if (lastPlay == -1) {
                ConsoleManager.getConsole().printError("Could not Find config LastPlayed! are you Sure ConfigUser:" + userConfig.getAbsolutePath() + " Have game?");
                return;
            }

            boolean isAppend = false;
            Map<Integer, String> fixedMap = new HashMap<>();
            for (Map.Entry<Integer, String> entry : strMap.entrySet()) {
                int key = entry.getKey();
                String value = entry.getValue();

                if (key == lastPlay) {
                    fixedMap.put(key, value);
                    String line = "\t\t\t\t\t\t\"LaunchOptions\"\t\t\"-netconport " + ConfigLoader.getConfigObject().getGamePort() + "\"";
                    fixedMap.put(key + 1, line);
                    isAppend = true;
                    continue;
                }

                if (isAppend) {
                    key += 2;
                }
                
                fixedMap.put(key,value);
            }

            ConsoleManager.getConsole().printToConsole("Updated ConfigFile: " + userConfig.getAbsolutePath() + ", Reason: Added Launch line.");
            saveUserConfig(userConfig,fixedMap);
            return;
        }

        String userLine = strMap.get(launchLine).split("\"\t\t\"")[1].replace("\"","");
        String patch = " -netconport " + ConfigLoader.getConfigObject().getGamePort();
        String result = "\t\t\t\t\t\t\"LaunchOptions\"\t\t\"" + userLine + patch + "\"";
        strMap.put(launchLine, result);
        ConsoleManager.getConsole().printToConsole("Updated ConfigFile: " + userConfig.getAbsolutePath() + ", Reason: Append port.");
        saveUserConfig(userConfig, strMap);
//        System.out.println("===========END===========");
//        System.out.println("Config: " + userConfig.getAbsolutePath() + ", Start: " + (startLine + 1) + ", [" + strList.get(startLine) + "], End: " + (endLine + 1) + ", [" + strList.get(endLine) + "]");
    }

    private static void saveUserConfig(File configFile,Map<Integer, String> configMap) {
        try {
//            File debugFile = new File(".", Math.random() + configFile.getName());
//            debugFile.createNewFile();
//            System.out.println("DEBUG: Write file: [" + configFile.getAbsolutePath() + "] is Temp redirected to Write:[" + debugFile + "]");
//            configFile = debugFile;
            StringBuilder builder = new StringBuilder();
            configMap.forEach((i,s) -> builder.append(s).append("\r\n"));
            ConfigLoader.writeFile(builder.toString().trim(), configFile);
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try write ConfigFile: " + configFile.getAbsolutePath() + " Throw Excpetion!");
            e.printStackTrace();
        }
    }

    private static String getFileLocation(String name) {
        try {
//            Process process = Runtime.getRuntime().exec("wmic process where \"name='steam.exe'\" get CommandLine");

            Process process = Runtime.getRuntime().exec("powershell " +
                    "\"Get-WmiObject -Query \\\"select * from Win32_Process where name='" + name + "'\\\"" +
                    " | Format-List -Property \\\"Path\\\"\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = "";
            while ((s = reader.readLine()) != null) {
                if (s.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))) {
                    //Path : C:\Program Files (x86)\Tencent\QQ\tin\QQ.exe
                    return s.substring(7);
                }
            }
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Throw Exception while getFileLocation!");
            e.printStackTrace();
        }
        return "";
    }

    private static boolean isWindowsSystem() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
