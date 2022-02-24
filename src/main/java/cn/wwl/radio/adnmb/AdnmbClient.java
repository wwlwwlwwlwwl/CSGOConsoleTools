package cn.wwl.radio.adnmb;

import cn.wwl.radio.adnmb.bean.AdnmbPost;
import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.adnmb.bean.AdnmbForumID;
import cn.wwl.radio.adnmb.bean.AdnmbReply;
import cn.wwl.radio.adnmb.bean.AdnmbThread;
import com.google.gson.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdnmbClient {
    private static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String LUWEI_URL = "https://cover.acfunwiki.org/luwei.json";

    public static final String ADNMB_FORUMS = "https://nmb.fastmirror.org/Api/showf";
    public static final String ADNMB_THREADS = "https://nmb.fastmirror.org/Api/thread";
    public static final String ADNMB_IMAGE = "https://nmbimg.fastmirror.org/image/";

    public static final String BEITAI_FORUMS = "https://tnmb.org/Api/showf";
    public static final String BEITAI_THREADS = "https://tnmb.org/Api/thread";

    private Map<String, List<AdnmbForumID>> forumLists = new HashMap<>();
    private static AdnmbClient instance;
    private boolean error;

    private AdnmbClient() {
        initLuwei();
    }

    public static AdnmbClient getInstance() {
        if (instance == null) {
            synchronized (AdnmbClient.class) {
                if (instance == null) {
                    instance = new AdnmbClient();
                }
            }
        }
        return instance;
    }

    public List<AdnmbPost> getReplys(int id, int page, boolean beitai) {
        String url = (beitai ? BEITAI_THREADS : ADNMB_THREADS) + "?id=" + id + "&page=" + page;

        Connection connect = Jsoup.connect(url)
                .ignoreHttpErrors(true).ignoreContentType(true).followRedirects(true);
        Document document = null;
        try {
            document = connect.get();
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: get Reply in [ID: " + id + ", Page: " + page + "Beitai: " + beitai + "] failed!");
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }

        String content = cleanBefore(document.body().html());
        try {
            JsonObject object = JsonParser.parseReader(GSON.newJsonReader(new StringReader(content))).getAsJsonObject();
            AdnmbThread po = new AdnmbThread();
            parseThread(po, object);
            po.setCurrentPage(page);

            int replyCount = Integer.parseInt(object.get("replyCount").getAsString());
            if (replyCount == 0) {
                if (page == 1) {
                    return List.of(po);
                }
                return List.of();
            }

            JsonArray array = object.get("replys").getAsJsonArray();
            List<AdnmbPost> list = new ArrayList<>();
            if (page == 1)
                list.add(po);

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                AdnmbReply reply = new AdnmbReply();
                parseReply(reply, obj);
                list.add(reply);
            }
            if (page == 1 && list.size() == 0)
                return List.of(po);

            return list;
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: parse Thread body Failed!");
            ConsoleManager.getConsole().printError("ADNMB: Body: " + document.body().html());
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }
    }

    public List<AdnmbPost> getReplys(AdnmbThread thread) {
        if (thread == null) {
            ConsoleManager.getConsole().printError("ADNMB: Request Reply but Threads is Null!");
            return List.of();
        }
        String url = (thread.isFromBeitai() ? BEITAI_THREADS : ADNMB_THREADS) + "?id=" + thread.getId() + "&page=" + thread.getCurrentPage();

        Connection connect = Jsoup.connect(url)
                .ignoreHttpErrors(true).ignoreContentType(true).followRedirects(true);
        Document document = null;
        try {
            document = connect.get();
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: get Threads failed!");
            ConsoleManager.getConsole().printError("ADNMB: URL> " + url);
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }

        String content = cleanBefore(document.body().html());
        try {
            JsonObject object = JsonParser.parseReader(GSON.newJsonReader(new StringReader(content))).getAsJsonObject();
            AdnmbThread po = new AdnmbThread();
            parseThread(po, object);
            int replyCount = Integer.parseInt(object.get("replyCount").getAsString());
            if (replyCount == 0) {
                if (thread.getCurrentPage() == 1) {
                    return List.of(po);
                }
                return List.of();
            }

            thread.setTotalReply(replyCount);
            JsonArray array = object.get("replys").getAsJsonArray();
            List<AdnmbPost> list = new ArrayList<>();
            if (thread.getCurrentPage() == 1)
                list.add(po);

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                AdnmbReply reply = new AdnmbReply();
                parseReply(reply, obj);
                if (!isAD(reply.getId(), reply.getCookie(), reply.getCookie(), reply.getTime())) {
                    thread.getReplys().add(reply);
                    reply.setTargetThread(thread);
                    list.add(reply);
                }
            }
            if (thread.getCurrentPage() == 1 && list.isEmpty())
                return List.of(po);

            return list;
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: parse Thread body Failed!");
            ConsoleManager.getConsole().printError("ADNMB: Body: " + document.body().html());
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }
    }

    public List<AdnmbThread> getThreads(AdnmbForumID forum, int page) {
        if (error) {
            ConsoleManager.getConsole().printError("ADNMB: init Error! Can't Send Request!");
            return List.of();
        }
        if (forum == null) {
            ConsoleManager.getConsole().printError("ADNMB: Request Threads but forum is Null!");
            return List.of();
        }

        boolean beitai = isBeitai(forum);
        String url = (beitai ? BEITAI_FORUMS : ADNMB_FORUMS) + "?id=" + (forum.getId() - (beitai ? 1000 : 0)) + "&page=" + page;
        Connection connect = Jsoup.connect(url)
                .ignoreHttpErrors(true).ignoreContentType(true).followRedirects(true);
        Document document = null;
        try {
            document = connect.get();
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: get Forum failed!");
            ConsoleManager.getConsole().printError("ADNMB: URL> " + url);
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }

        String content = cleanBefore(document.body().html());
        try {
            List<AdnmbThread> list = new ArrayList<>();
            JsonArray array = JsonParser.parseReader(GSON.newJsonReader(new StringReader(content))).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                AdnmbThread threads = new AdnmbThread();
                parseThread(threads, object);
                threads.setFromBeitai(beitai);
                if (!isAD(threads.getId(), threads.getCookie(), threads.getContent(), threads.getTime()))
                    list.add(threads);
            }
            return list;
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: parse Forum body Failed!");
            ConsoleManager.getConsole().printError("ADNMB: Body: " + document.body().html());
            ConsoleManager.getConsole().printException(e);
            return List.of();
        }
    }

    private void parseReply(AdnmbReply target, JsonObject obj) {
        target.setId(Integer.parseInt(obj.get("id").getAsString()))
                .setTime(obj.get("now").getAsString())
                .setCookie(obj.get("userid").getAsString())
                .setContent(cleanAfter(obj.get("content").getAsString()))
                .setImage(obj.get("img").getAsString())
                .setFormat(obj.get("ext").getAsString());
    }

    private void parseThread(AdnmbThread target, JsonObject object) {
        target.setTotalReply(Integer.parseInt(object.get("replyCount").getAsString()))
                .setCurrentPage(1)
                .setAdmin(object.get("admin").getAsString().equals("1"))
                .setSage(object.get("sage").getAsString().equals("1"))
                .setCookie(object.get("userid").getAsString())
                .setImage(object.get("img").getAsString())
                .setFormat(object.get("ext").getAsString())
                .setId(Integer.parseInt(object.get("id").getAsString()))
                .setTime(object.get("now").getAsString())
                .setContent(cleanAfter(object.get("content").getAsString()));
    }

    private boolean isAD(int id, String name, String content, String time) {
        if (id == 9999999) {
            return true;
        }

        if (name.equals("芦苇") || name.equals("ATM")) {
            return true;
        }

        if (time.startsWith("2099")) {
            return true;
        }

        return false;
    }

    private String cleanBefore(String data) {
        return Jsoup.clean(data
                        .replace("</font>", "")
                        .replace("<br>","")
                , Safelist.none());
    }

    private String cleanAfter(String data) {
        if (data.contains("#789922")) {
            data = data.replace("<font color=\"#789922\">","[引用:")
                    .replace("</font>", "]");
        }
        return data.replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&amp;", "&")
                .replace("&nbsp;"," ");
    }

    private boolean isBeitai(AdnmbForumID forum) {
        return forumLists.get("beitai").contains(forum);
    }

    private void initLuwei() {
        if (!forumLists.isEmpty()) {
            return;
        }
        Document document = null;
        try {
            Connection connect = Jsoup.connect(LUWEI_URL)
                    .ignoreHttpErrors(true).ignoreContentType(true).followRedirects(true);
            document = connect.get();
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: get Luwei failed!");
            ConsoleManager.getConsole().printException(e);
            error = true;
            return;
        }

        String content = Jsoup.clean(document.body().html(), Safelist.none());
        try {
            JsonObject jsonObject = JsonParser.parseReader(GSON.newJsonReader(new StringReader(content))).getAsJsonObject();
            forumLists.put("adnmb", parseForums(jsonObject.get("forum").getAsJsonArray(), false));
            forumLists.put("beitai", parseForums(jsonObject.get("beitaiForum").getAsJsonArray(), true));
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("ADNMB: Parse Luwei failed!");
            ConsoleManager.getConsole().printException(e);
            ConsoleManager.getConsole().printError("DEBUG: " + document.body().html());
            error = true;
            return;
        }
        ConsoleManager.getConsole().printToConsole("ADNMB: init done.");
    }

    private List<AdnmbForumID> parseForums(JsonElement forumArray, boolean beitai) {
        if (!(forumArray instanceof JsonArray)) {
            return List.of();
        }

        List<AdnmbForumID> list = new ArrayList<>();
        JsonArray array = forumArray.getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            AdnmbForumID obj = new AdnmbForumID();
            obj.setGroup(Integer.parseInt(object.get("fgroup").getAsString()))
                    .setId(Integer.parseInt(object.get("id").getAsString()) + (beitai ? 1000 : 0))
                    .setName(cleanAfter(object.get("name").getAsString()))
                    .setBeitai(beitai);
            if (object.has("showName")) {
                obj.setShowName(object.get("showName").getAsString());
            } else {
                obj.setShowName("");
            }
            list.add(obj);
        }
        return list;
    }

    public Map<String, List<AdnmbForumID>> getForumLists() {
        return forumLists;
    }

    public AdnmbForumID getForumByName(String name) {
        for (Map.Entry<String, List<AdnmbForumID>> entry : getForumLists().entrySet()) {
            for (AdnmbForumID adnmbForumID : entry.getValue()) {
                if (adnmbForumID.getName().equals(name)) {
                    return adnmbForumID;
                }
            }
        }
        return null;
    }

    public AdnmbForumID getForumByID(int id) {
        for (Map.Entry<String, List<AdnmbForumID>> entry : getForumLists().entrySet()) {
            for (AdnmbForumID adnmbForumID : entry.getValue()) {
                if (adnmbForumID.getId() == id) {
                    return adnmbForumID;
                }
            }
        }
        return null;
    }
}
