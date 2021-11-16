package cn.wwl.radio.console;

import cn.wwl.radio.console.impl.CMDConsole;
import cn.wwl.radio.console.impl.gui.MinimizeTrayConsole;
import cn.wwl.radio.network.SocketTransfer;

import java.util.Locale;

public class ConsoleManager {

    private static GameConsole console;

    public static void initConsole(String[] arg) {
        if (console != null) {
            return;
        }

        for (String s : arg) {
            if (s.toLowerCase(Locale.ROOT).contains("nogui")) { //只有传入nogui指令时才会以命令行控制台启动
                console = new CMDConsole();
                break;
            }
        }


        if (console == null) {
            console = new MinimizeTrayConsole();
        }

        SocketTransfer.getInstance().addListenerTask("redirectGameConsole",console::redirectGameConsole);
        console.init();

        try {
            // Why you in here?
            Thread.currentThread().wait();
        } catch (Exception ignored) {}
    }


    public static GameConsole getConsole() {
        return console;
    }
}
