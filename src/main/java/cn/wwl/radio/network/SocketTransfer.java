package cn.wwl.radio.network;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.console.impl.gui.ManagerPanel;
import cn.wwl.radio.console.impl.gui.MinimizeTrayConsole;
import cn.wwl.radio.console.impl.gui.TrayMessageCallback;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.executor.functions.CustomMusicFunction;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.SteamUtils;
import cn.wwl.radio.music.BackgroundMusic;
import cn.wwl.radio.network.task.ListenerTask;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketTransfer {

    public static final String ECHO_HEAD = "ConsoleTools";
    public static final String NEW_CLIENT_LOGIN = "onNewClientLogin-";
    private static final SocketTransfer socketTransfer = new SocketTransfer();

    private Socket socket;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private static ConsoleListener listener = new ConsoleListener();
    private BufferedOutputStream outputStream = null;
    private long bootTimestamp = 0L;
    private static String userName;

    private static boolean isReboot = false;

    private SocketTransfer(){}

    public void start() {
        int CONNECT_PORT = ConfigLoader.getConfigObject().getGamePort();
        userName = ConfigLoader.getConfigObject().getPreviousName();
        ConsoleManager.getConsole().printToConsole("System Charset: " + Charset.defaultCharset() + ", Config Charset: " + ConfigLoader.getConfigCharset());
        ConsoleManager.getConsole().printToConsole("Watching Port: " + CONNECT_PORT + ", Waiting Game start...");
        if (!isReboot) {
            SteamUtils.patchCSGOLaunchLine();
        }
        if (ConfigLoader.getConfigObject().isLobbyMusic()) {
            BackgroundMusic.init();

            if (ConfigLoader.getConfigObject().isUsingLauncher()) {
                BackgroundMusic.playBackgroundMusic(false);
            }
        }
        boolean connected = false;
        while (!connected) {
            try {
                // Linux???????????????Bug
                // 1. ???????????????????????????????????? ?????????????????????
                // 2. ???????????????????????????????????? ???????????????
                // ??????????????????????????????????????????????????????????????? ???????????????????????????????????????

                //TODO ??????????????????Socket?????? ??????????????? ?????????
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(CONNECT_PORT));

                if (socket == null || !socket.isConnected() || socket.isClosed()) {
                    throw new IOException();
                }

                ConsoleManager.getConsole().printToConsole("Successfully Connect to Game!");
                connected = true;
            } catch (IOException ignored) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }

        bootTimestamp = System.currentTimeMillis();
        ConsoleManager.getConsole().printToConsole("Start Console Listener thread...");
        if (isReboot) {
            listener = new ConsoleListener();
        }
        executor.execute(listener);
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try get OutputStream Throw Exception!");
            ConsoleManager.getConsole().printException(e);
            try {
                Thread.sleep(500);
                outputStream = new BufferedOutputStream(socket.getOutputStream());
            } catch (Exception omg) {
                ConsoleManager.getConsole().printError("get OutputStream Throw Exception again...");
                omg.printStackTrace();
                System.exit(1);
            }
        }

        addListenerTask("NewClientChecker", read -> {
            if (read.contains(SocketTransfer.NEW_CLIENT_LOGIN)) {
                try {
                    long timeStamp = Long.parseLong(read.split("-")[1].trim());
                    if (timeStamp == SocketTransfer.getInstance().getBootTimestamp()) { //is my self
                        return;
                    }
                    ConsoleManager.getConsole().printToConsole("New Client connected to game. Current Session closed.");
                    SocketTransfer.getInstance().shutdown(false);
                } catch (Exception ignored) {} //????????????????????????????????? ??????????????????????????????
            }
        });

        getPlayerName();
        echoLogin();

        if (!isReboot) {
            registerCtrlCHook();
            FunctionExecutor.registerGameHook();
            SocketConsole.createRemoteListener();
            ConsoleManager.getConsole().startConsole();
        } else {
            FunctionExecutor.callRebootHook();
        }
    }

    private void registerCtrlCHook() {
        try {
            Signal signal = new Signal("INT");
            SignalHandler handler = (s) -> {
                ConsoleManager.getConsole().printToConsole("Got Ctrl+C! Calling shutdown...");
                shutdown(true);
            };
            Signal.handle(signal,handler);
        } catch (Exception e) {
            ConsoleManager.getConsole().printToConsole("register Ctrl+C hook Failed. Ignoring.");
        }
    }

    public void shutdown(boolean echoShutdown) {
        if (socket == null || !socket.isConnected()) {
            System.exit(0);
            return;
        }

        try {
            ConsoleManager.getConsole().printToConsole("Start Shutdown now...");
            if (echoShutdown) {
                if (!socket.isClosed())
                        echoDisconnect();
            }
            executor.shutdown();
            socket.close();
        } catch (Exception e) {
            ConsoleManager.getConsole().printException(e);
        }
        System.exit(0);
    }

    public void restart() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {}
            socket = null;
        }
        MinimizeTrayConsole.createTrayMessage("Game is Closed, App will Waiting Game back.\nClick to Show the GUI.\nDouble click this to Close App.", (type) -> {
            switch (type) {
                case TrayMessageCallback.SINGLE_CLICK -> ManagerPanel.showManagerPanel();
                case TrayMessageCallback.DOUBLE_CLICK -> SocketTransfer.getInstance().shutdown(false);
            }
        });
        if (!ConfigLoader.getConfigObject().isUsingLauncher()) {
            BackgroundMusic.stopBackgroundMusic(false);
        }
        ConsoleManager.getConsole().printToConsole("Start reboot Application...");
        isReboot = true;
        start();
    }

    private void echoLogin() {
        String prefix = ConfigLoader.getConfigObject().getPrefix();
        pushToConsole("echo " + NEW_CLIENT_LOGIN + bootTimestamp);
        pushToConsole("status;" + //????????????????????????,??????LobbyMusic
                "name;" + //????????????ID ????????????ID??????
                "con_filter_enable 2;" +
                "con_filter_text_out \"Unknown\";" + //??????????????????,??????Unknown,??????Unknown command??????
                "clear;" + //???????????????,??????????????????????????????
                "showconsole"); //???????????????
        pushToConsole("echo .......##.####.##......##.########.####..");
        pushToConsole("echo .......##..##..##..##..##.##........##...");
        pushToConsole("echo .......##..##..##..##..##.##........##...");
        pushToConsole("echo .......##..##..##..##..##.######....##...");
        pushToConsole("echo .##....##..##..##..##..##.##........##...");
        pushToConsole("echo .##....##..##..##..##..##.##........##...");
        pushToConsole("echo ..######..####..###..###..########.####..");
        echoToConsole("Hello world! Hooked cmd!");
        echoToConsole("Cmd List: ");
        ConfigLoader.getConfigObject().getModuleList().forEach(module -> {
            String full_cmd = prefix + "_" + module.getCommand();
            //alias jw_happy "echo HookExecute jw_happy"
            String aliasBuilder = "alias " +
                    full_cmd +
                    " \"echo " +
                    FunctionExecutor.HOOK_HEAD +
                    " " +
                    module.getCommand() +
                    "\"";
//        ConsoleManager.getConsole().printToConsole("Alias: " + aliasBuilder);
            SocketTransfer.getInstance().pushToConsole(aliasBuilder);
            echoToConsole("Module: " + module.getName() + ", Function: " + module.getFunction() + ", " + prefix + "_" + module.getCommand());
        });
        ConsoleManager.getConsole().printToConsole("Register commands done.");
        if (ConfigLoader.getConfigObject().isLobbyMusic()) {
            BackgroundMusic.playBackgroundMusic(false);
        }
    }

    private void echoDisconnect() {
        pushToConsole("echo .########..##....##.########....##......##..#######..########..##.......########..####");
        pushToConsole("echo .##.....##..##..##..##..........##..##..##.##.....##.##.....##.##.......##.....##.####");
        pushToConsole("echo .##.....##...####...##..........##..##..##.##.....##.##.....##.##.......##.....##.####");
        pushToConsole("echo .########.....##....######......##..##..##.##.....##.########..##.......##.....##..##.");
        pushToConsole("echo .##.....##....##....##..........##..##..##.##.....##.##...##...##.......##.....##.....");
        pushToConsole("echo .##.....##....##....##..........##..##..##.##.....##.##....##..##.......##.....##.####");
        pushToConsole("echo .########.....##....########.....###..###...#######..##.....##.########.########..####");
        echoToConsole("ConsoleTools Disconnected now!");
    }

    public void addListenerTask(String name, ListenerTask task) {
        listener.addListener(name, task);
    }

    private boolean isRegisterTask = false;
    /**
     * ?????????????????????ID
     * @Warning ????????????ID?????????????????????????????????????????????
     * @return ?????????ID, ??????????????????????????????????????????ID
     */
    public String getPlayerName() {
        if (!isRegisterTask) {
            isRegisterTask = true;
            addListenerTask("GetPlayerName", message -> {
                if (message.contains("\"name\" = ") && message.contains("unnamed")) {
                    String name = message.substring(10, message.indexOf("\" ( def. \"unnamed\" )"));
                    String previousName = ConfigLoader.getConfigObject().getPreviousName();
                    if (!previousName.equals(name)) {
                        ConsoleManager.getConsole().printToConsole("Update player name: " + name);
                        userName = name;
                        ConfigLoader.getConfigObject().setPreviousName(userName);
                        ConfigLoader.writeConfigObject();
                    }
                }
            });
        }
        return userName;
    }

    public static SocketTransfer getInstance() {
        return socketTransfer;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return outputStream != null && (socket != null && socket.isConnected() && !socket.isClosed());
    }

    public long getBootTimestamp() {
        return bootTimestamp;
    }

    /**
     * ??????????????????????????????????????? ?????????????????????????????????????????????
     * @param command ??????????????????
     */
    public void pushToConsole(String command) {
        if (!isConnected()) {
            ConsoleManager.getConsole().printToConsole("Send command: [" + command + "] To game failed! Socket not connected!");
            return;
        }
        try {
//            outputStream.write(command + "\n");
            outputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Write to Game Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
    }

    public void echoToConsole(String str, boolean prefix) {
        if (str == null || str.length() == 0) {
            pushToConsole("echo \r\n");
            return;
        }
        if (prefix) {
            pushToConsole("echo \"" + ECHO_HEAD + " > " + str + "\"");
        } else {
            pushToConsole("echo \"" + str + "\"");
        }
    }

    public void echoToConsole(String str) {
        echoToConsole(str, true);
    }
}
