package cn.wwl.radio.network;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.utils.Timer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 老是莫名其妙的卡按键 按任何按键都没用
 * 增加一个远程退出游戏的钩子 解决卡按键的NT操作
 */
public class SocketConsole {

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private static final Map<Integer, RemoteListenerRunnable> SOCKET_MAP = new HashMap<>();
    private static int port = -1;

    public static void createRemoteListener() {
        port = ConfigLoader.getConfigObject().getRemoteConsolePort();
        if (port != -1) {
            if (port >= 1 && port < 65535) {
                executor.execute(() -> {
                    try {
                        Thread.currentThread().setName("Remote Console Listener");
                        startListener();
                    } catch (IOException e) {
                        ConsoleManager.getConsole().printError("Try start RemoteListener Throw exception!");
                        e.printStackTrace();
                    }
                });
            } else {
                ConsoleManager.getConsole().printError("Port in wrong number! Check the config!");
            }
        }
    }

    private static void startListener() throws IOException {
        ServerSocket socket = new ServerSocket(port);
        ConsoleManager.getConsole().printToConsole("RemoteClient Listener start at port: " + port);
        while (socket.isBound()) {
            Socket accept = socket.accept();
            ConsoleManager.getConsole().printToConsole("New Remote client connect from: " + accept.getInetAddress() + ":" + accept.getPort());
            try {
                RemoteListenerRunnable command = new RemoteListenerRunnable(accept);
                SOCKET_MAP.put(accept.getLocalPort(),command);
                executor.execute(command);
            } catch (IOException e) {
                ConsoleManager.getConsole().printError("Try service " + accept + " Throw exception!");
                e.printStackTrace();
            }
        }
    }

    public static void onGameConsoleMessage(String msg) {
        for (Map.Entry<Integer, RemoteListenerRunnable> entry : SOCKET_MAP.entrySet()) {
            entry.getValue().onReceiveMessage(msg);
        }
    }

    public static class RemoteListenerRunnable implements Runnable {

        private final Socket socket;
        private final BufferedReader reader;
        private final BufferedWriter writer;

        public RemoteListenerRunnable(Socket target) throws IOException {
            try {
                this.socket = target;
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IOException("Try create IO Socket throw Exception!",e);
            }
        }

        public void onReceiveMessage(String msg) {
            try {
                pushToRemote(msg);
            } catch (IOException e) {
                ConsoleManager.getConsole().printError("Push Message to " + socket + " Failed! Because: " + e.getMessage());
            }
        }

        public void pushToRemote(String s) throws IOException {
            this.writer.write(s + "\r\n");
            this.writer.flush();
        }

        @Override
        public void run() {
            String socketIP = socket.getInetAddress() + ":" + socket.getPort();
            while (!socket.isClosed()) {
                try {
                    this.reader.lines().forEach(s -> {
                        ConsoleManager.getConsole().printToConsole("RemoteClient " + socketIP + " : " + s);
                        SocketTransfer.getInstance().pushToConsole(s);
                    });
                } catch (Exception e) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                }
            }
        }

        public Socket getSocket() {
            return socket;
        }
    }
}
