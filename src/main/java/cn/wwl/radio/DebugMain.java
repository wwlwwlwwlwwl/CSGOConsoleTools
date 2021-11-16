package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.music.MusicManager;
import cn.wwl.radio.music.MusicResult;
import cn.wwl.radio.music.MusicSource;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class DebugMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Start debug...");
        ConsoleManager.initConsole(new String[] {"tray"});
        ConfigLoader.loadConfigObject(false);

        System.out.print("Enter music name: ");
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");
        String next = scanner.next();
        System.out.println("Searching: " + next);
        List<MusicResult> musicResults = MusicManager.getMusicSource().searchMusic(next);
        System.out.println("Result: " + musicResults);
        System.out.print("Enter which wanna playing: ");
        MusicResult musicResult = musicResults.get(scanner.nextInt());
        System.out.println("Selected: " + musicResult);
        File file = MusicManager.getMusicSource().downloadMusic(musicResult);
        if (file == MusicSource.NEED_PAY_FILE) {
            System.out.println("Download Failed! NEED_PAY");
            return;
        }
        System.out.println("Downloaded to " + file.getAbsolutePath());
        System.out.println("Debug end.");
    }
}
