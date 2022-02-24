package cn.wwl.radio;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;

public class DebugMain {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long endTime;
        boolean initConsole = true;
        boolean guiConsole = false;
        System.out.println("Start debug...");
        if (initConsole) {
            if (guiConsole)
                ConsoleManager.initConsole(new String[] {"tray"});
            else
                ConsoleManager.initConsole(new String[] {});

            ConfigLoader.loadConfigObject(false);
        }


        endTime = System.currentTimeMillis();
        System.out.println("Debug end.Used time: " + (endTime - startTime) + "ms");
    }
    // 使用rhino执行JavaScript, 可以用于自定义函数
    //        String javaScript = """
//                var test = () => {
//                    ConsoleManager.printToConsole("Print From JavaScript!")
//                }
//                test()
//        """;
//
//        Context context = Context.enter();
////        Script script = context.compileString(javaScript, "TEST", 0, null);
//
//        Scriptable scope = context.initStandardObjects();
//        ScriptableObject.putProperty(scope, "SocketTransfer", Context.javaToJS(SocketTransfer.getInstance(), scope));
//        ScriptableObject.putProperty(scope, "ConsoleManager", Context.javaToJS(ConsoleManager.getConsole(), scope));
//        try {
////            script.exec(context, scope);
//            context.evaluateString(scope, javaScript, "TEST", 0, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
}
