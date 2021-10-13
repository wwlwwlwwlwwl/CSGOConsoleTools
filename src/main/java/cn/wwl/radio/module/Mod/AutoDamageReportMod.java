package cn.wwl.radio.module.Mod;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.module.Module;
import cn.wwl.radio.utils.Timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoDamageReportMod extends Module {

    private boolean enabled = false;

    @Override
    public void onHookCommand(String commands) {
        if (commands.contains("Given")) {
            ConsoleManager.getConsole().printToConsole(commands);
            String[] split = commands.split("\"");
            String target = split[1].replace("\"", "");
            String damage = split[2].substring(3, split[2].length() - 9).trim();
            int dmg = Integer.parseInt(damage);
            if (dmg <= 100) {
                addDamageGiven(target, dmg);
            }
        } else {
            if (enabled) {
                echo("AutoDamageReport now is Disabled");
                enabled = false;
            } else {
                echo("AutoDamageReport now is Enabled");
                enabled = true;
            }
        }
    }

    private final Timer timer = new Timer();
    private boolean printed = true;
    private final Map<String, Integer> dmgMap = new HashMap<>();
    private String latest = "";

    private long getTime() {
        return System.currentTimeMillis();
    }

    private void printDamageList() {
        printed = true;
        StringBuilder builder = new StringBuilder();
        dmgMap.forEach((key, value) -> builder.append(key).append(" -").append(value).append(","));
        if (enabled && (latest.equals("") || !builder.toString().equals(latest))) {
            pushToConsole("say_team " + builder.substring(0, builder.length() - 1));
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

    @Override
    public void onTick() {
        if (!printed && timer.isReachedTime(1000)) {
            printDamageList();
        }
    }

    @Override
    public List<String> hookedCommands() {
        return List.of("Damage Given to", "autoDamage", "autodamage");
    }
}
