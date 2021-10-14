package cn.wwl.radio.file;

import cn.wwl.radio.console.ConsoleManager;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigWatcher implements Runnable {

    private static final ScheduledExecutorService watcherExecutor = Executors.newSingleThreadScheduledExecutor();
    private static long previousEditTime;

    public static void startWatchConfig() {
        if (!ConfigLoader.getConfigObject().isAutoReloadConfig()) {
            return;
        }

        if (previousEditTime == 0L) {
            ConsoleManager.getConsole().printToConsole("Start ConfigWatcher Thread...");
            watcherExecutor.scheduleAtFixedRate(new ConfigWatcher(),0,1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        if (!ConfigLoader.getConfigObject().isAutoReloadConfig()) {
            watcherExecutor.shutdown();
            return;
        }

        File configFile = ConfigLoader.CONFIG_FILE;
        long currentModified = configFile.lastModified();
        if (previousEditTime == 0L) {
            previousEditTime = currentModified;
            return;
        }

        if (previousEditTime != currentModified) {
            previousEditTime = currentModified;
            ConsoleManager.getConsole().printToConsole("ConfigFile has been changed! Reloading Config...");
            if (ConfigLoader.loadConfigObject(true)) {
                ConsoleManager.getConsole().printToConsole("Reload done.");
            } else {
                ConsoleManager.getConsole().printToConsole("Reload Failed!");
            }
        }
    }
}
