package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;

public class DebugMain {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long endTime;
        boolean initConsole = false;
        System.out.println("Start debug...");
        if (initConsole) {
            ConsoleManager.initConsole(new String[] {"tray"});
            ConfigLoader.loadConfigObject(false);
        }

        endTime = System.currentTimeMillis();
        System.out.println("Debug end.Used time: " + (endTime - startTime) + "ms");
    }
}
