package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketTransfer {

    public static final String ECHO_HEAD = "ConsoleTools";
    public static final int CONNECT_PORT = 10090;
    private static final SocketTransfer socketTransfer = new SocketTransfer();

    private Socket socket;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private final ConsoleListener listener = new ConsoleListener();
    private BufferedOutputStream outputStream = null;
    private long bootTimestamp = 0L;

    private SocketTransfer(){}

    public void start() {
        ConsoleManager.getConsole().printToConsole("System Charset: " + Charset.defaultCharset() + ", Config Charset: " + ConfigLoader.getConfigCharset());
        ConsoleManager.getConsole().printToConsole("Waiting Game start...");
        boolean connected = false;
        while (!connected) {
            try {
//                System.out.println("Debug > Start connect try...");
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(CONNECT_PORT));

                if (!socket.isConnected() || socket.isClosed()) {
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
        ConsoleManager.getConsole().printToConsole("Start Console Listen thread...");
        executor.execute(listener);
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            ConsoleManager.getConsole().printError("Try get OutputStream Throw Exception!");
            e.printStackTrace();
            try {
                Thread.sleep(500);
                outputStream = new BufferedOutputStream(socket.getOutputStream());
            } catch (Exception omg) {
                ConsoleManager.getConsole().printError("get OutputStream Throw Exception again...");
                e.printStackTrace();
                System.exit(1);
            }
        }
        registerCtrlCHook();
        FunctionExecutor.registerGameHook();
        echoLogin();
        ConsoleManager.getConsole().startConsole();
    }

    private void registerCtrlCHook() {
        Signal signal = new Signal("INT");
        SignalHandler handler = (s) -> {
            ConsoleManager.getConsole().printToConsole("Got Ctrl+C! Calling shutdown...");
            shutdown(true);
        };
        Signal.handle(signal,handler);
    }

    public void shutdown(boolean echoShutdown) {
        try {
            ConsoleManager.getConsole().printToConsole("Start Shutdown now...");
            if (echoShutdown) {
                echoDisconnect();
            }
            executor.shutdown();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static final String NEW_CLIENT_LOGIN = "onNewClientLogin-";

    private void echoLogin() {
        //TODO 发个ESC试下?
        pushToConsole("escape");
        pushToConsole("echo " + NEW_CLIENT_LOGIN + bootTimestamp);
        pushToConsole("showconsole;clear");
        pushToConsole("echo .......##.####.##......##.########.####..");
        pushToConsole("echo .......##..##..##..##..##.##........##...");
        pushToConsole("echo .......##..##..##..##..##.##........##...");
        pushToConsole("echo .......##..##..##..##..##.######....##...");
        pushToConsole("echo .##....##..##..##..##..##.##........##...");
        pushToConsole("echo .##....##..##..##..##..##.##........##...");
        pushToConsole("echo ..######..####..###..###..########.####..");
        echoToConsole("Hello world! Hooked cmd!");
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

    public static SocketTransfer getInstance() {
        return socketTransfer;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getBootTimestamp() {
        return bootTimestamp;
    }

    /**
     * 将命令输出到游戏内的控制台 类似于玩家在游戏内控制台的输入
     * @param command 要输出的命令
     */
    public void pushToConsole(String command) {
        if (outputStream == null) {
            ConsoleManager.getConsole().printToConsole("Socket not connected!");
            return;
        }
        try {
//            outputStream.write(command + "\n");
            outputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            ConsoleManager.getConsole().printToConsole("Write to Game Throw Exception!");
            e.printStackTrace();
        }
    }
/*
    public void debug() {
        try {
            for (Charset value : Charset.availableCharsets().values()) {
                try {
                    value.newEncoder();
                } catch (UnsupportedOperationException e) {
                    System.out.println("!!!Unsupported Encode " + value + " , Ignored now.");
                    continue;
                }
                System.out.println("Start debug charset : " + value);
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress(CONNECT_PORT));
                System.out.println("Temp socket connect success.");
                BufferedWriter stream = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),value));
                stream.write("echo 中文测试 > Encode : " + value + "\n");
                stream.flush();
                System.out.println("Data flushed. Encode : " + value);
                stream.close();
                sock.close();
                System.out.println("Work done. Sleep 500ms");
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public void echoToConsole(String str) {
        pushToConsole("echo " + ECHO_HEAD + " > " + str);
    }

    /**
     * 循环监听游戏内控制台的输出 并重定向至虚拟的控制台
     */
    public static class ConsoleListener implements Runnable {

        private int discCount = 0;
        @Override
        public void run() {
            Socket socket = SocketTransfer.getInstance().getSocket();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
                while (!Thread.currentThread().isInterrupted()) {
                    String read = "";
                    try {
                        read = reader.readLine();
                    } catch (Exception e) {
                        continue;
                    }

                    //控制台在整活 多半游戏已经关了
                    if (discCount >= 20) {
                        ConsoleManager.getConsole().printToConsole("Disconnected from game. Game closed.");
                        SocketTransfer.getInstance().shutdown(false);
                    }

                    if (read == null || read.isEmpty()) {
                        discCount++;
                        continue;
                    }


                    if (read.contains(SocketTransfer.ECHO_HEAD)) { //不要重复抓取
                        continue;
                    }

                    if (read.contains(NEW_CLIENT_LOGIN)) {
                        try {
                        long timeStamp = Long.parseLong(read.split("-")[1].trim());
//                        ConsoleManager.getConsole().printToConsole("Current Boot : " + SocketTransfer.getInstance().getBootTimestamp() + " , newClient : " + timeStamp);
                        if (timeStamp == SocketTransfer.getInstance().getBootTimestamp()) { //is my self
                            continue;
                        }
                        ConsoleManager.getConsole().printToConsole("New Client connected to game. Current Session closed.");
                        SocketTransfer.getInstance().shutdown(false);
                        break;
                        } catch (Exception e) { //鬼知道会出现什么异常呢 如果解析失败就忽略吧
                            continue;
                        }
                    }

                    ConsoleFunction function = isContainHookedMessage(read);
                    if (function != null) {
                        FunctionExecutor.executeMessageHook(function,read);
                        continue;
                    }

                    discCount = 0;
                    try {
                        FunctionExecutor.executeFunction(read);
                    } catch (Exception e) {
                        ConsoleManager.getConsole().printToConsole("Throw Exception on Execute Command : " + read);
                    }
                    ConsoleManager.getConsole().redirectGameConsole(read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private final Map<String, ConsoleFunction> func = new HashMap<>();

        private ConsoleFunction isContainHookedMessage(String message) {
            if (func.isEmpty()) {
                Map<String, ConsoleFunction> functions = FunctionExecutor.getFunctions();
                for (Map.Entry<String, ConsoleFunction> entry : functions.entrySet()) {
                    List<String> hookMessage = entry.getValue().isHookSpecialMessage();
                    if (hookMessage == null || hookMessage.isEmpty()) {
                        continue;
                    }
                    hookMessage.forEach(s -> func.put(s,entry.getValue()));
                }
            }

            for (Map.Entry<String, ConsoleFunction> entry : func.entrySet()) {
                if (message.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }

            return null;
        }
    }
}
