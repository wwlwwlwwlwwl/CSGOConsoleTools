package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.utils.Timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageReportFunction implements ConsoleFunction {

    private static boolean isEnabled = false;
    private final Timer timer = new Timer();
    private boolean printed = true;
    private final Map<String, Integer> dmgMap = new HashMap<>();
    private String latest = "";

    @Override
    public boolean isRequireTicking() {
        return true;
    }

    @Override
    public List<String> isHookSpecialMessage() {
        return List.of("Damage Given to");
    }

    @Override
    public void onTick() {
        if (!isEnabled) {
            return;
        }

        if (!printed && timer.isReachedTime(1000)) {
            printDamageList();
        }
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        if (isEnabled) {
            SocketTransfer.getInstance().echoToConsole("Auto Damage Report is Disabled.");
            isEnabled = false;
        } else {
            SocketTransfer.getInstance().echoToConsole("Auto Damage Report is Enabled.");
            isEnabled = true;
        }
    }

    @Override
    public void onHookSpecialMessage(String message) {
        if (!isEnabled) {
            return;
        }

        String[] split = message.split("\"");
        String target = split[1].replace("\"", "");
        if (target.equalsIgnoreCase("world")) {
            return; //跳楼找摔不算嗷
        }
        String damage = split[2].substring(3, split[2].length() - 9).trim();
        if (damage.contains("*")) {
            return; // 憨批反和谐
        }
        int dmg = Integer.parseInt(damage);
        if (dmg <= 100) {
            addDamageGiven(target, dmg);
        }
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    private void printDamageList() {
        printed = true;
        StringBuilder builder = new StringBuilder();
        dmgMap.forEach((key, value) -> builder.append(key).append(" -").append(value).append(","));
        if (latest.equals("") || !builder.toString().equals(latest)) {
            SocketTransfer.getInstance().pushToConsole("say_team \"" + builder.substring(0, builder.length() - 1) + "\"");
        }
        latest = builder.toString();
        dmgMap.clear();
    }

    public void addDamageGiven(String player, int damage) {
//        ConsoleManager.getConsole().printToConsole("Add data > p : " + player + " , D : " + damage);
        dmgMap.put(player, damage);
        timer.reset();
        printed = false;
    }
}
