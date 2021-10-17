package cn.wwl.radio.utils;

import cn.wwl.radio.file.ConfigLoader;

import java.util.List;
import java.util.Random;

public enum TextMarker {
    白色("\u0001", "#white#"),
    红色("\u0002", "#red#"),
    灰蓝色("\u0003","#ctblue#"),
    绿色("\u0004", "#green#"),
    淡绿色("\u0005", "#lightgreen#"),
    浅绿色("\u0006", "#lowgreen#"),
    淡红色("\u0007", "#lightred#"),
    灰色("\u0008", "#grey#"),
    灰金色("\u0009","#tgold#"),
    金色("\u0010", "#gold#"),
    淡蓝色("\u000B", "#lightblue#"),
    蓝色("\u000C", "#blue#"),
    紫色("\u000E", "#purple#"),
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

    TextMarker(String code, String humanCode) {
        this.code = code;
        this.humanCode = humanCode;
    }

    public String getCode() {
        return code;
    }

    public String getHumanCode() {
        return humanCode;
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
