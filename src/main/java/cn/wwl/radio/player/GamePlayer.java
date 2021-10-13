package cn.wwl.radio.player;

public class GamePlayer {
    private double playerX;
    private double playerY;
    private double playerZ;
    private double playerPitch;
    private double playerYaw;
    private double playerRoll;

    private double circleYaw;

    private WeaponType currentWeapon;

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public double getPlayerZ() {
        return playerZ;
    }

    public double getPlayerPitch() {
        return playerPitch;
    }

    public double getPlayerYaw() {
        return playerYaw;
    }

    public double getPlayerRoll() {
        return playerRoll;
    }

    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }

    public void updatePlayerPos(double x,double y,double z,double pitch,double yaw,double roll) {
        this.playerX = x;
        this.playerY = y;
        this.playerZ = z;
        this.playerPitch = pitch;
        this.playerYaw = yaw;
        this.playerRoll = roll;

        setCircleYaw();
    }

    private void setCircleYaw() {
        double calc = playerYaw - 90;
        if (calc > 0) {
            calc = (calc - 90) - 270;
        }
        this.circleYaw = -calc;
    }

    public double getCircleYaw() {
        return circleYaw;
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "playerX=" + playerX +
                ", playerY=" + playerY +
                ", playerZ=" + playerZ +
                ", playerPitch=" + playerPitch +
                ", playerYaw=" + playerYaw +
                ", playerRoll=" + playerRoll +
                ", currentWeapon=" + currentWeapon +
                '}';
    }

    public enum WeaponType {
        Primary,
        Secondary,
        Knive,
        Grenade,
        C4
    }
}
