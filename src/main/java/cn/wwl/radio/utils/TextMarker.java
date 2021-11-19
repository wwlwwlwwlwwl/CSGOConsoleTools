package cn.wwl.radio.utils;

import cn.wwl.radio.file.ConfigLoader;

import java.awt.*;
import java.util.List;
import java.util.Random;

public enum TextMarker {
    白色("\u0001", "#white#", new Color(255,255,255)),
    红色("\u0002", "#red#", new Color(255,0,0)),
    灰蓝色("\u0003","#ctblue#", new Color(162,198,255)),
    绿色("\u0004", "#green#", new Color(64,255,64)),
    淡绿色("\u0005", "#lightgreen#", new Color(191,255,144)),
    浅绿色("\u0006", "#lowgreen#", new Color(162, 255, 71)),
    淡红色("\u0007", "#lightred#", new Color(255, 64, 64)),
    灰色("\u0008", "#grey#", new Color(197, 202, 208)),
    灰金色("\u0009","#tgold#", new Color(237, 228, 122)),
    金色("\u0010", "#gold#", new Color(228, 174, 57)),
    淡蓝色("\u000B", "#lightblue#", new Color(94, 152, 217)),
    蓝色("\u000C", "#blue#", new Color(75, 105, 255)),
    紫色("\u000E", "#purple#", new Color(211, 44, 230)),
/*
    白色("", "#white#"),
    绿色("", "#green#"),
    淡蓝色("", "#lightblue#"),
    蓝色("\f", "#blue#"),
    红色("", "#red#"),
    金色("", "#gold#"),
    灰色("", "#grey#"),
    淡绿色("", "#lightgreen#"),
    淡红色("", "#lightred#"),
    浅绿色("", "#lowgreen#"),
    浅紫色("", "#lowpurple#"),
    浅红色("", "#lowred#"),
*/
    换行("\u2028", "#next#"),
    随机颜色(白色.code, "#random#"),

    未知("", "#unknown#");

    private final String code;
    private final String humanCode;
    private final Color color;

    TextMarker(String code, String humanCode) {
        this.code = code;
        this.humanCode = humanCode;
        this.color = new Color(255,255,255);
    }

    TextMarker(String code, String humanCode,Color color) {
        this.code = code;
        this.humanCode = humanCode;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public String getHumanCode() {
        return humanCode;
    }

    public Color getColor() {
        return color;
    }

    public static TextMarker getAsHumanCode(String humanCode) {
        for (TextMarker value : values()) {
            if (value.getHumanCode().equals(humanCode)) {
                return value;
            }
        }
        return null;
    }

    public static List<TextMarker> availableColors() {
        return List.of(
                白色,
                绿色,
                淡蓝色,
                灰蓝色,
                蓝色,
                红色,
                金色,
                灰色,
                淡绿色,
                淡红色,
                浅绿色,
                紫色
        );
    }

    public static String replaceHumanCode(String str) {
        while (str.contains(随机颜色.getHumanCode())) {
            str = str.replaceFirst(随机颜色.getHumanCode(), getRandomColor().getCode());
        }

        for (TextMarker value : values()) {
            str = str.replace(value.humanCode, value.code);
        }

        return str;
    }

    public static TextMarker getRandomColor() {
        List<TextMarker> colors = ConfigLoader.getConfigObject().getRandomColors();
        Random random = new Random();
        return colors.get(random.nextInt(colors.size() - 1));
    }
}
