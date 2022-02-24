package cn.wwl.radio.network;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.network.task.ListenerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 循环监听游戏内控制台的输出 并重定向至虚拟的控制台
 */
public class ConsoleListener implements Runnable {

    private static final Map<String, ListenerTask> tasks = new ConcurrentHashMap<>();
    public void addListener(String name, ListenerTask listener) {
        if (tasks.containsKey(name)) {
            ConsoleManager.getConsole().printError("Tasks Map already have Key: " + name + "! Check the task Register!");
            return;
        }

        tasks.put(name, listener);
    }

    public boolean haveListener(String name) {
        return tasks.containsKey(name);
    }

    private int discCount = 0;

    @Override
    public void run() {
        Socket socket = SocketTransfer.getInstance().getSocket();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8), 81920);
            while (!Thread.currentThread().isInterrupted()) {
                String read;
                try {
                    read = reader.readLine();
                } catch (Exception e) {
                    continue;
                }

                //控制台在整活 多半游戏已经关了
                if (discCount >= 20) {
                    ConsoleManager.getConsole().printToConsole("Disconnected from game. Game closed.");
//                    SocketTransfer.getInstance().shutdown(false);
                    SocketTransfer.getInstance().restart();
                    break;
                }

                if (read == null || read.isEmpty()) {
                    discCount++;
                    continue;
                }

                if (read.contains(SocketTransfer.ECHO_HEAD)) { //不要重复抓取
                    continue;
                }
                for (Map.Entry<String, ListenerTask> entry : tasks.entrySet()) {
                    ListenerTask task = entry.getValue();
                    try {
                        task.listen(read);
                    } catch (Exception e) {
                        ConsoleManager.getConsole().printError("Try execute Listen task: " + entry.getKey() + " throw Exception!");
                        ConsoleManager.getConsole().printException(e);
                    }
                    if (task.isShouldRemove()) {
                        tasks.remove(entry.getKey());
                    }
                }

                if (discCount != 0)
                    discCount = 0;
            }
        } catch (Exception e) {
            ConsoleManager.getConsole().printException(e);
        }
    }
}