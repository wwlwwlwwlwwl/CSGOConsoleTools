package cn.wwl.radio.network;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 老是莫名其妙的卡按键 按任何按键都没用
 * 增加一个远程退出游戏的钩子 解决卡按键的NT操作
 */
public class SocketConsole {

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private static final Map<Socket, RemoteListenerRunnable> SOCKET_MAP = new HashMap<>();
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
                        ConsoleManager.getConsole().printException(e);
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
        //Push message to Remote client.
        SocketTransfer.getInstance().addListenerTask("RemoteClientHook", SocketConsole::onGameConsoleMessage);
        while (socket.isBound()) {
            Socket accept = socket.accept();
            ConsoleManager.getConsole().printToConsole("New Remote client connect from: " + accept.getInetAddress() + ":" + accept.getPort());
            try {
                RemoteListenerRunnable command = new RemoteListenerRunnable(accept);
                SOCKET_MAP.put(accept,command);
                executor.execute(command);
            } catch (IOException e) {
                ConsoleManager.getConsole().printError("Try service " + accept + " Throw exception!");
                ConsoleManager.getConsole().printException(e);
            }
        }
    }

    public static void onGameConsoleMessage(String msg) {
        for (Map.Entry<Socket, RemoteListenerRunnable> entry : SOCKET_MAP.entrySet()) {
            try {
                entry.getValue().pushToRemote(msg);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try push Message to " + entry.getKey().toString() + " Failed!");
                try {
                    entry.getValue().closeConnection();
                } catch (Exception ignored) {}
            }
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

        public void closeConnection() {
            try {
                writer.flush();
                writer.close();
            } catch (Exception ignored) {}
            try {
                reader.close();
            } catch (Exception ignored) {}
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}
