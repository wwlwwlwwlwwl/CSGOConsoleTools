package cn.wwl.radio.utils;

import cn.wwl.radio.console.ConsoleManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SteamUtils {
    private static File csgoPath;

    public static File getCsgoPath() {
        return csgoPath;
    }

    public static boolean initCSGODir() {
        if (csgoPath != null) {
            return true;
        }

        String csgoLocation = SteamUtils.getCSGOLocation().replace("csgo.exe","");
        if (csgoLocation.equals("")) {
            ConsoleManager.getConsole().printToConsole("Get CSGO Path failed!");
            return false;
        }
        csgoPath = new File(csgoLocation);
        return true;
    }

    private static String getCSGOLocation() {
        try {
//            Process process = Runtime.getRuntime().exec("wmic process where \"name='steam.exe'\" get CommandLine");

            Process process = Runtime.getRuntime().exec("powershell " +
                    "\"Get-WmiObject -Query \\\"select * from Win32_Process where name='csgo.exe'\\\"" +
                    " | Format-List -Property \\\"Path\\\"\"");
            //Fuck you Microsoft
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = "";
            while ((s = reader.readLine()) != null) {
                if (s.contains("csgo.exe")) {
                    return s.substring(7);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleManager.getConsole().printError("Throw Exception while Try getCSGOLocation!");
        }
        return "";
    }

    private static boolean isWindowsSystem() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
