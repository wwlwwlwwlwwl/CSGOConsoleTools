package cn.wwl.radio.utils;

import cn.wwl.radio.file.ConfigLoader;

import java.util.List;
import java.util.Random;

public enum TextMarker {
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
                蓝色,
                红色,
                金色,
                灰色,
                淡绿色,
                淡红色,
                浅绿色,
                浅紫色,
                浅红色
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
