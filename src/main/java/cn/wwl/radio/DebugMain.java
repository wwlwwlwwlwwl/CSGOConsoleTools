package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import org.apache.commons.text.PatchStringEscapeUtils;

public class DebugMain {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long endTime = 0L;
        boolean initConsole = false;
        System.out.println("Start debug...");
        if (initConsole) {
            ConsoleManager.initConsole(new String[] {"tray"});
            ConfigLoader.loadConfigObject(false);
        }

        String test = "This is a \"Escape Java Test\", i Will Try Any Random Code in here. Like \\IDK\\ lol{^_^}";
        System.out.println("Original: " + test);
        String escape = PatchStringEscapeUtils.escapeJava(test);
        System.out.println("Escape: " + escape);
        String unEscape = PatchStringEscapeUtils.unescapeJava(test);
        System.out.println("UnEscape: " + escape);
        String escapeUnescape = PatchStringEscapeUtils.unescapeJava(escape);
        System.out.println("escape Unescape: " + escapeUnescape);

        endTime = System.currentTimeMillis();
        System.out.println("Debug end.Used time: " + (endTime - startTime) + "ms");
    }
}
