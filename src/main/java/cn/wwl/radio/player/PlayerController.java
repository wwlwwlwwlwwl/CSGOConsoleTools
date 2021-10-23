package cn.wwl.radio.player;

import cn.wwl.radio.network.SocketTransfer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * I'm sure CSGO Official not allowed this
 */
public class PlayerController {

    private static final GamePlayer player = new GamePlayer();
    private static final ThreadPoolExecutor keyClickPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);

    public static void holdKey(ControlKeys keys) {
        if (keys == null || keys == ControlKeys.None) {
            return;
        }

        if (keys.isHolding()) {
            return;
        }
        //因为是按下 所以要把*替换为+
        String cmd = keys.setHolding(true).getControlCommand().replace("*", "+");
        SocketTransfer.getInstance().pushToConsole(cmd);
    }

    public static void releaseKey(ControlKeys keys) {
        if (keys == null || keys == ControlKeys.None) {
            return;
        }

        if (!keys.isHolding()) {
            return;
        }
        String cmd = keys.setHolding(false).getControlCommand().replace("*", "-");
        SocketTransfer.getInstance().pushToConsole(cmd);
    }

    public static void clickKey(ControlKeys keys,int holdTime) {
        if (keys == null || keys == ControlKeys.None) {
            return;
        }

        if (keys.getControlCommand().contains("*")) {
            keyClickPool.execute(() -> {
                holdKey(keys);
                try {
                    Thread.sleep(holdTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                releaseKey(keys);
            });
        } else {
            SocketTransfer.getInstance().pushToConsole(keys.getControlCommand());
        }
    }

    public static void updatePlayerPos(double x,double y,double z,double pitch,double yaw,double roll) {
        player.updatePlayerPos(x,y,z,pitch,yaw,roll);
    }

    public static GamePlayer getPlayer() {
        return player;
    }

    public static void updateTick() {
        if (rotatePlayer(0)) {
            Set<MoveType> type = getTargetPosMoveType(-1810.421021,2373.072754,167.093811); //沙2 B包 大木箱上
            moveAsMoveType(type);
            //TODO 确认当前地图是否为目标地图 自动绕开障碍物
        }
    }

    public static void moveAsMoveType(Set<MoveType> types) {
        releaseKey(ControlKeys.Forward);
        releaseKey(ControlKeys.Back);
        releaseKey(ControlKeys.GoLeft);
        releaseKey(ControlKeys.GoRight);
        releaseKey(ControlKeys.Jump);

        types.forEach(type -> holdKey(type.keys));
    }

    public static Set<MoveType> getTargetPosMoveType(double x, double y, double z) {
        final int maxDiffPos = 20;
        Set<MoveType> types = new HashSet<>();
        double diffX = x - player.getPlayerX();
        double diffY = y - player.getPlayerY();
        double diffZ = z - player.getPlayerZ();
        //Y正 前进 Y负 后退
        //X正 往右 X负 往右
        //Z负数 无视 Z正数 跳跃
        if (diffX >= maxDiffPos) {
            //D
            types.add(MoveType.Right);
        } else if (diffX <= -maxDiffPos){
            //A
            types.add(MoveType.Left);
        }

        if (diffY >= maxDiffPos) {
            //W
            types.add(MoveType.Forward);
        } else if (diffY <= -maxDiffPos) {
            //S
            types.add(MoveType.Back);
        }

        if (diffZ >= 10) {
            types.add(MoveType.Jump);
        }
        return types;
    }

    public static boolean rotatePlayer(double yaw) {
        releaseKey(ControlKeys.MouseLeft);
        releaseKey(ControlKeys.MouseRight);
        double playerYaw = player.getCircleYaw();
        double rotateYaw = (playerYaw - yaw);
        double diffYaw = rotateYaw;
        if (rotateYaw > 180) {
            rotateYaw = 180 - (rotateYaw - 180);
            diffYaw = rotateYaw;
            rotateYaw = -rotateYaw;
        }

        if (diffYaw < 3) {
            releaseKey(ControlKeys.MouseLeft);
            releaseKey(ControlKeys.MouseRight);
            return true;
        }

        if (rotateYaw < 0) {
            //负数 鼠标右移
            holdKey(ControlKeys.MouseRight);
        } else {
            //正数 鼠标左移
            holdKey(ControlKeys.MouseLeft);
        }
        return false;
    }

    public enum MoveType {
        Forward(ControlKeys.Forward),
        Back(ControlKeys.Back),
        Left(ControlKeys.GoLeft),
        Right(ControlKeys.GoRight),
        Jump(ControlKeys.Jump),
        None(null);

        private final ControlKeys keys;
        MoveType(ControlKeys keys) {
            this.keys = keys;
        }

        public ControlKeys getKeys() {
            return keys;
        }
    }

    public enum ControlKeys {
        Forward("*forward","前进"),
        Back("*back","后退"),
        GoLeft("*moveleft","向左走"),
        GoRight("*moveright","向右走"),
        Duck("*duck","下蹲"),
        Speed("*speed","静步"),
        MouseLeft("*left","鼠标向左"),
        MouseRight("*right","鼠标向右"),
        Fire("*attack","鼠标左键"),
        Fire2("*attack2","鼠标右键"),
        Inspect("*inspect","检视"),
        Reload("*reload","换弹"),
        Jump("*jump","跳跃"),
        Use("*use","交互"),

        PrimaryWeapon("slot1","主武器"),
        SecondaryWeapon("slot2","副武器"),
        KniveWeapon("slot3","近战武器"),
        GrenadeWeapon("slot4","投掷物"),
        BuyMenu("buymenu","购买菜单"),
        DropWeapon("drop","丢弃武器"),
        None("","无");

        private final String controlCommand;
        private final String displayKeys;
        private boolean isHolding;
        ControlKeys(String controlCommand,String displayKeys) {
            this.controlCommand = controlCommand;
            this.displayKeys = displayKeys;
            this.isHolding = false;
        }

        public boolean isHolding() {
            return isHolding;
        }

        public ControlKeys setHolding(boolean holding) {
            isHolding = holding;
            return this;
        }

        public String getControlCommand() {
            return controlCommand;
        }

        public String getDisplayKeys() {
            return displayKeys;
        }

        public static ControlKeys getKeyAsCommand(String command) {
            for (ControlKeys value : values()) {
                if (value.getControlCommand().contains(command.toLowerCase(Locale.ROOT))) {
                    return value;
                }
            }
            return null;
        }

        public static ControlKeys getKeyAsDisplayName(String name) {
            for (ControlKeys value : values()) {
                if (value.getDisplayKeys().equals(name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
