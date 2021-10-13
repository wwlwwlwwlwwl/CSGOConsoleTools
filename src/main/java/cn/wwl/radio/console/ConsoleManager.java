package cn.wwl.radio.console;

import cn.wwl.radio.console.impl.CMDConsole;
import cn.wwl.radio.console.impl.GUIConsole;

import java.util.Locale;

public class ConsoleManager {

    private static GameConsole console;

    public static void initConsole(String[] arg) {
        for (String s : arg) {
            if (s.toLowerCase(Locale.ROOT).contains("gui")) {
                console = new GUIConsole();
                break;
            }
        }
        //TODO GUI控制台 不过似乎不需要?


        if (console == null) {
            console = new CMDConsole();
        }

        console.init();
    }


    public static GameConsole getConsole() {
        return console;
    }
}
