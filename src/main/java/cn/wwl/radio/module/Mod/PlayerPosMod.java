package cn.wwl.radio.module.Mod;

import cn.wwl.radio.module.Module;
import cn.wwl.radio.player.PlayerController;

import java.util.List;

@Deprecated
public class PlayerPosMod extends Module {

    private boolean enabled = false;


    @Override
    public void onHookCommand(String commands) {
        if (commands.contains("playerpos")) {
            if (enabled) {
                echo("PlayerPosMod now is Disabled");
                enabled = false;
            } else {
                echo("PlayerPosMod now is Enabled");
                enabled = true;
            }
            return;
        }
        //setpos -1827.192261 2384.714355 167.093811;setang -0.961741 -89.087463 0.000000
        if (commands.contains("setpos") && commands.contains("setang")) {
            double x = PlayerController.getPlayer().getPlayerX();
            double y = PlayerController.getPlayer().getPlayerY();
            double z = PlayerController.getPlayer().getPlayerZ();
            double pitch = PlayerController.getPlayer().getPlayerPitch();
            double yaw = PlayerController.getPlayer().getPlayerYaw();
            double roll = PlayerController.getPlayer().getPlayerRoll();
            String[] set = commands.split(";");
            for (String s : set) {
                String[] data = s.split(" ");
                if (data[0].startsWith("setpos")) {
                    x = Double.parseDouble(data[1]);
                    y = Double.parseDouble(data[2]);
                    z = Double.parseDouble(data[3]);
                } else if (data[0].startsWith("setang")) {
                    pitch = Double.parseDouble(data[1]);
                    yaw = Double.parseDouble(data[2]);
                    roll = Double.parseDouble(data[3]);
                }
            }

            PlayerController.updatePlayerPos(x,y,z,pitch,yaw,roll);
        }
    }

    @Override
    public void onTick() {
        if (enabled) {
            pushToConsole("getpos");
            PlayerController.updateTick();
        }
    }

    @Override
    public List<String> hookedCommands() {
        return List.of("setpos","playerpos");
    }
}
