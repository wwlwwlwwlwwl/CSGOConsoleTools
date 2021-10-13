package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;

public class Main {

    public static void main(String[] args) {
        ConsoleManager.initConsole(args);
        ConfigLoader.loadConfigObject(false);
        FunctionExecutor.initFunctions();
        SocketTransfer.getInstance().start();
    }
}
