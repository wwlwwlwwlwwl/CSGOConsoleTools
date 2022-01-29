package cn.wwl.radio.utils;

import cn.wwl.radio.file.ConfigLoader;

import java.awt.*;
import java.util.List;
import java.util.Random;

public enum TextMarker {
    White("\u0001", "#white#", new Color(255,255,255)),
    Red("\u0002", "#red#", new Color(255,0,0)),
    CTBlue("\u0003","#ctblue#", new Color(162,198,255)),
    Green("\u0004", "#green#", new Color(64,255,64)),
    LightGreen("\u0005", "#lightgreen#", new Color(191,255,144)),
    LowGreen("\u0006", "#lowgreen#", new Color(162, 255, 71)),
    LightRed("\u0007", "#lightred#", new Color(255, 64, 64)),
    Grey("\u0008", "#grey#", new Color(197, 202, 208)),
    TerroristGold("\u0009","#tgold#", new Color(237, 228, 122)),
    Gold("\u0010", "#gold#", new Color(228, 174, 57)),
    LightBlue("\u000B", "#lightblue#", new Color(94, 152, 217)),
    Blue("\u000C", "#blue#", new Color(75, 105, 255)),
    Purple("\u000E", "#purple#", new Color(211, 44, 230)),

    Wrap("\u2028", "#next#"),
    Random(White.code, "#random#"),
    Playername(White.code, "#name#"),

    Unknown("", "#unknown#");

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
                White,
                Red,
                CTBlue,
                Green,
                LightGreen,
                LowGreen,
                LightRed,
                Grey,
                TerroristGold,
                Gold,
                LightBlue,
                Blue,
                Purple
        );
    }

    public static String replaceHumanCode(String str) {
        while (str.contains(Random.getHumanCode())) {
            str = str.replaceFirst(Random.getHumanCode(), getRandomColor().getCode());
        }

        for (TextMarker value : values()) {
            if (value == Playername) {
                str = str.replace(value.humanCode, ConfigLoader.getConfigObject().getPreviousName());
                continue;
            }
            str = str.replace(value.humanCode, value.code);
        }

        return str;
    }

    public static TextMarker getRandomColor() {
        List<TextMarker> colors = availableColors();
        return colors.get(new Random().nextInt(colors.size() - 1));
    }
}
