package cn.wwl.radio.executor.functions;

import cn.wwl.radio.adnmb.bean.AdnmbPost;
import cn.wwl.radio.adnmb.bean.AdnmbThread;
import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.adnmb.AdnmbClient;
import cn.wwl.radio.adnmb.bean.AdnmbForumID;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.network.SocketTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdnmbClientFunction implements ConsoleFunction {

    public static final String SELECT_FORUM = "#forum";
    public static final String GET_THREAD = "#thread";
    public static final String GET_MORE = "#more";

    public static final String PREFIX = "ADNMB > ";

    private final List<AdnmbForumID> allForum = new ArrayList<>();
    private final Map<Integer, AdnmbThread> posts = new HashMap<>();
    private AdnmbThread targetThread;
    private AdnmbForumID targetForum;
    private boolean enable = true;
//    private int threadCount = 1;
    private int pageCount = 1;

    @Override
    public void onInit() {
        Map<String, List<AdnmbForumID>> forumLists = AdnmbClient.getInstance().getForumLists();
        int forums = 0;
        for (Map.Entry<String, List<AdnmbForumID>> entry : forumLists.entrySet()) {
            forums += entry.getValue().size();
            allForum.addAll(entry.getValue());
        }

        if (forums != 0) {
            ConsoleManager.getConsole().printToConsole("Loaded " + forums + " Forum from Adnmb!");
            SocketTransfer.getInstance().echoToConsole("Adnmb Ready. Loaded " + forums + " Forums. Use cmd " + SELECT_FORUM + " List all Forums.");
        }
    }

    @Override
    public List<String> isHookSpecialMessage() {
        return List.of(SELECT_FORUM, GET_THREAD, GET_MORE);
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        if (enable) {
            SocketTransfer.getInstance().echoToConsole("Adnmb client now is Off");
            enable = false;
        } else {
            SocketTransfer.getInstance().echoToConsole("Adnmb client now is On");
            enable = true;
        }
    }

    @Override
    public void onHookSpecialMessage(String message) {
        if (!enable) {
            return;
        }

        if (allForum.isEmpty()) {
            echoWithPrefix("Adnmb is Not ready! Please wait Sec.");
            return;
        }

        if (message.startsWith(PREFIX)) {
            return;
        }

        message = FunctionExecutor.removeUnknownCommandTag(message);
        if (message.contains(SELECT_FORUM)) {
            if (message.equals(SELECT_FORUM)) {
                if (targetThread != null) {
                    targetThread = null;
                    echoWithPrefix("Return to Forum.");
                    pageCount = 1;
                    renderForums();
                    return;
                }

                echoWithPrefix("Adnmb Forums: ");
                allForum.forEach(forum -> {
                    echoWithPrefix("   " + forum.toString());
                });
                printBottom();
                return;
            }

            try {
                String str = message.substring(SELECT_FORUM.length() + 1);
                targetForum = AdnmbClient.getInstance().getForumByID(Integer.parseInt(str));
                echoWithPrefix("Select Forum: " + targetForum.toString());
                posts.clear();
                pageCount = 1;
            } catch (Exception e) {
                SocketTransfer.getInstance().echoToConsole("Cannot Parse the Forum cmd! Please Try again!");
                ConsoleManager.getConsole().printError("Parse Forum ERROR!");
                ConsoleManager.getConsole().printException(e);
            }
            return;
        }

        if (message.contains(GET_MORE)) {
            if (targetForum == null) {
                echoWithPrefix("Please Choose Forum First!");
                return;
            }
            SocketTransfer.getInstance().echoToConsole("\r\n\r\n\r\n", false);
            if (targetThread == null) {
                renderForums();
            } else {
                targetThread.setCurrentPage(targetThread.getCurrentPage() + 1);
                List<AdnmbPost> post = AdnmbClient.getInstance().getReplys(targetThread);
                renderThread(post);
            }
            printBottom();
            return;
        }

        if (message.contains(GET_THREAD)) {
            if (message.equals(GET_THREAD)) {
                echoWithPrefix("Usage: " + GET_THREAD + "_[ID] Get Thread data.");
                printBottom();
                return;
            }

            try {
                Integer threadID = Integer.parseInt(message.substring(GET_THREAD.length() + 1));
                if (posts.containsKey(threadID)) {
                    targetThread = posts.get(threadID);
                    List<AdnmbPost> post = AdnmbClient.getInstance().getReplys(targetThread);
                    renderThread(post);
                }
//              else {
//                    List<AdnmbPost> post = AdnmbClient.getInstance().getReplys(threadID, threadCount++, false);
//                    renderThread(post);
//                }
                printBottom();
                return;
            } catch (Exception e) {
                SocketTransfer.getInstance().echoToConsole("Try process Thread Failed!");
                ConsoleManager.getConsole().printError("Try process Thread Failed!");
                ConsoleManager.getConsole().printException(e);
                return;
            }
        }
    }

    // CSGO中文缓冲区 大约160左右
    private void processEchoContent(String s) {
        if (s.contains("\n")) {
            String[] split = s.split("\n");
            for (String str : split) {
                SocketTransfer.getInstance().echoToConsole(str, false);
            }
        } else {
            if (s.length() > 150) {
                if (s.contains("，")) {
                    String[] split = s.split("，");
                    for (String str : split) {
                        processEchoContent(str);
                    }
                    return;
                }

                if (s.contains("。")) {
                    String[] split = s.split("。");
                    for (String str : split) {
                        processEchoContent(str);
                    }
                    return;
                }

                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    buffer.append(s.charAt(i));
                    if (i % 150 == 0) {
                        SocketTransfer.getInstance().echoToConsole(buffer.toString(), false);
                        buffer.setLength(0);
                    }
                }
                SocketTransfer.getInstance().echoToConsole(buffer.toString(), false);
            } else {
                SocketTransfer.getInstance().echoToConsole(s, false);
            }
        }
    }

    private void renderThread(List<AdnmbPost> data) {
        if (data == null || data.size() == 0) {
            echoWithPrefix("No more Threads!");
            return;
        }
        data.forEach(post -> {
            String str = post.getCookie() + "  ---  " + post.getTime() + "  --- ID: " + post.getId();
            SocketTransfer.getInstance().echoToConsole(str, false);
            processEchoContent(post.getContent());
            if (!post.getImage().equals("")) {
                SocketTransfer.getInstance().echoToConsole("图片: " + post.getImage(), false);
            }
            SocketTransfer.getInstance().echoToConsole("\r\n", false);
        });
    }

    private void renderForums() {
        List<AdnmbThread> threads = AdnmbClient.getInstance().getThreads(targetForum, pageCount++);
        if (threads.isEmpty()) {
            SocketTransfer.getInstance().echoToConsole("Get new Page Failed! Please Try again!");
            return;
        }

        threads.forEach(thread -> {
            posts.put(thread.getId(), thread);
            String str = thread.getCookie() + "  ---  " + thread.getTime() + "  --- 串号: " + thread.getId() + " --- 回复: " + thread.getTotalReply();
            SocketTransfer.getInstance().echoToConsole(str, false);
            processEchoContent(thread.getContent());
            if (!thread.getImage().equals("")) {
                SocketTransfer.getInstance().echoToConsole("图片: " + thread.getImage(), false);
            }
            SocketTransfer.getInstance().echoToConsole("\r\n\r\n", false);
        });
    }

    private void printBottom() {
        echoWithPrefix("cmd: " + SELECT_FORUM + " -> List Forums, " + SELECT_FORUM + "_[ID] Choose Forums.");
        echoWithPrefix("cmd: " + GET_THREAD + "_[ThreadID] Fetch Thread.");
        echoWithPrefix("cmd: " + GET_MORE + " -> MORE Threads, MORE Reply!");
    }

    private void echoWithPrefix(String str) {
        SocketTransfer.getInstance().echoToConsole(PREFIX + str, false);
    }
}
