package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动修改Steam游戏的启动行 来解决需要添加CSGO启动行的问题
 */
@Deprecated
public class SteamUserConfigManager {

    private static final Executor pool = Executors.newSingleThreadExecutor();

    public static void patchSteamLine() {
        if (!isWindowsSystem()) {
            ConsoleManager.getConsole().printToConsole("SteamLine Patch current not support Non Windows System!");
            return;
        }

        pool.execute(() -> {
            String steamPath = getSteamLocation().replace("\"","");
            if ("".equals(steamPath)) {
                ConsoleManager.getConsole().printToConsole("getSteamPath error! Maybe steam not running!");
                return;
            }
            steamPath = steamPath.substring(0,steamPath.indexOf("exe") - 6);
            List<File> steamUsers = getSteamUsers(steamPath);
            for (File steamUser : steamUsers) {
                if (steamUser.getName().equals("243781139")) {
                    patchUserLaunchLine(steamUser);
                }
            }
        });
    }

    private static boolean isWindowsSystem() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static void patchUserLaunchLine(File userFolder) {
        try {
            File configFile = new File(userFolder,"config/localconfig.vdf");
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(configFile));

            AtomicBoolean findSoftware = new AtomicBoolean(false);
            AtomicBoolean findValve = new AtomicBoolean(false);
            AtomicBoolean findSteam = new AtomicBoolean(false);
            AtomicBoolean findApps = new AtomicBoolean(false);
            AtomicBoolean findCSGO = new AtomicBoolean(false);
            AtomicBoolean endCSGO = new AtomicBoolean(false);
            AtomicBoolean hasLaunchOption = new AtomicBoolean(false);

            //Maybe have another option?

            reader.lines().forEach(s -> {
                if (s.contains("Software")) {
                    findSoftware.set(true);
                }

                if (findSoftware.get()) {
                    if (s.contains("Valve")) {
                        findValve.set(true);
                    }

                    if (findValve.get()) {
                        if (s.contains("Steam")) {
                            findSteam.set(true);
                        }

                        if (findSteam.get()) {
                            if (s.contains("apps")) {
                                findApps.set(true);
                            }

                            if (findApps.get()) {
                                if (s.contains("Software")) {
                                    findSoftware.set(true);
                                }

                                if (findSoftware.get()) {
                                    if (s.trim().equals("\"730\"")) {
                                        findCSGO.set(true);
                                    }

                                    if (findCSGO.get()) {
                                        if (s.contains("}")) {
                                            endCSGO.set(true);
                                        }

                                        if (s.contains("LaunchOptions")) {
                                            hasLaunchOption.set(true);
                                        }

                                        // TODO 可以在没有到结尾之前添加上启动行 或者直接修改默认启动行 不过这样平台似乎用不了吧emm 直接白给
                                    }
                                }
                            }
                        }
                    }
                }

                buffer.append(s).append("\r\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<File> getSteamUsers(String steamPath) {
        File file = new File(steamPath,"userdata");
        if (!file.exists() || !file.isDirectory()) {
            return List.of();
        }

        return Arrays.stream(file.listFiles((dir, name) -> !name.equals("ac"))).toList();
    }

     public static String getSteamLocation() {
        try {
//            Process process = Runtime.getRuntime().exec("wmic process where \"name='steam.exe'\" get CommandLine");
            Process process = Runtime.getRuntime().exec("powershell \"Get-WmiObject -Query \\\"select * from Win32_Process where name='csgo.exe'\\\" | Format-List -Property \\\"Path\\\"\""); //Fuck you Microsoft
            //Get-WmiObject -Query "select * from Win32_Process where name='csgo.exe'" | Format-List -Property "Path"
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = "";
            while ((s = reader.readLine()) != null) {
                if (s.contains("csgo.exe")) {
                    return s.substring(7);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleManager.getConsole().printError("Throw Exception while Try getSteamPath!");
        }
        return "";
    }
}
