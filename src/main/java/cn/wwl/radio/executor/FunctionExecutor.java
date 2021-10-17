package cn.wwl.radio.executor;

import cn.wwl.radio.SocketTransfer;
import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.functions.*;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.ConfigObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FunctionExecutor {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicBoolean isStartTickThread = new AtomicBoolean(false);
    private static final Map<String, ConfigObject.ModuleObject> modules = new HashMap<>();
    private static final Map<String, ConsoleFunction> functions = new HashMap<>();
    public static final String HOOK_HEAD = "HookExecute";

    public static void executeFunction(String cmd) {
        String func;
        if (!(cmd.startsWith(FunctionExecutor.HOOK_HEAD) || cmd.startsWith("Unknown"))) {
            return;
        }

        if (cmd.startsWith("Unknown")) {
            /*两种输出方式:
            1. 主菜单 : Unknown command "hello"
            2. 游戏内 : Unknown command : hello
            真就V社爆改
             */
            try {
                int firstIndex = cmd.indexOf("\"") + 1; //包含引号
                int lastIndex = cmd.lastIndexOf("\"");
                func = cmd.substring(firstIndex, lastIndex);
            } catch (Exception e) {
                func = cmd.split(":")[1].trim(); //如果抛出异常 说明不是新版 则使用老版分割
            }

            String newCommand = commandReplace(func);
            if (func.equalsIgnoreCase(newCommand)) { //替换完了还是一样 说明这个不是目标
                return;
            }
            SocketTransfer.getInstance().echoToConsole("Cmd [" + func + "] has been Replace to [" + newCommand + "] .");
            func = newCommand;
        } else {
            func = cmd.substring(FunctionExecutor.HOOK_HEAD.length() + 1).trim();
        }

        if (!modules.containsKey(func)) {
            ConsoleManager.getConsole().printError("got call for [" + func + "] But not find correct Module in Map!");
            return;
        }
        ConfigObject.ModuleObject moduleConfig = modules.get(func);
        ConsoleManager.getConsole().printToConsole("ModuleCall : [M: " + moduleConfig.getName() + " ,F: " + moduleConfig.getFunction() + " ,P: " + moduleConfig.getParameter() + "]");
        functions.get(moduleConfig.getFunction()).onExecuteFunction(moduleConfig.getParameter());
    }

    public static void executeMessageHook(ConsoleFunction function, String message) {
        ConsoleManager.getConsole().printToConsole("FunctionMessageHook : [F: " + function.getClass().getName() + " ,M: " + message + "]");
        function.onHookSpecialMessage(message);
    }


    public static void printHelp() {
        SocketTransfer.getInstance().echoToConsole("Help list : ");
        modules.forEach((str, obj) -> {
            SocketTransfer.getInstance().echoToConsole("cmd : " + str + " , {F: " + obj.getFunction() + "}");
        });
    }

    public static void registerGameHook() {
        String prefix = ConfigLoader.getConfigObject().getPrefix();
        StringBuilder aliasBuilder = new StringBuilder();
        modules.forEach((name, moduleObject) -> {
            String full_cmd = prefix + "_" + moduleObject.getCommand();
            aliasBuilder  //alias jw_happy "echo HookExecute jw_happy"
                    .append("alias ")
                    .append(full_cmd)
                    .append(" \"echo ")
                    .append(HOOK_HEAD)
                    .append(" ")
                    .append(moduleObject.getCommand())
                    .append("\";");
        });
        ConsoleManager.getConsole().printToConsole("Alias : " + aliasBuilder);
        SocketTransfer.getInstance().pushToConsole(aliasBuilder.toString());
        ConsoleManager.getConsole().printToConsole("Register commands done.");

        if (!isStartTickThread.get()) {
            startTickThread();
        }
    }

    public static void reloadModules() {
        modules.clear();

        initFunctions();
    }

    public static void initFunctions() {
        loadFunctions();
        checkConfigFunctions();
    }

    private static void startTickThread() {
        isStartTickThread.set(true);
        executor.scheduleAtFixedRate(() -> {
            functions.forEach((name, func) -> {
                if (func.isRequireTicking()) {
                    func.onTick();
                }
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private static String commandReplace(String oldCommand) {
        for (Map.Entry<String, String> entry : ConfigLoader.getConfigObject().getAutoReplaceCommand().entrySet()) {
            if (entry.getKey().equals(oldCommand)) {
                return entry.getValue();
            }
        }
        return oldCommand;
    }

    private static void loadFunctions() {
        if (!functions.isEmpty()) {
            return;
        }

        functions.put("CustomRadio", new CustomRadioFunction());
        functions.put("CustomChat",new CustomChatFunction());
        functions.put("AutoChat", new AutoChatFunction());
        functions.put("help", new HelpFunction());
        functions.put("ReloadConfig", new ReloadConfigFunction());
        functions.put("FakeOpenCase",new FakeCaseOpenFunction());
        functions.put("Debug",new DebugFunction());
        //TODO 反射寻找其他模块来进行注册
    }

    public static Map<String, ConsoleFunction> getFunctions() {
        return functions;
    }

    private static void checkConfigFunctions() {
        List<ConfigObject.ModuleObject> moduleList = ConfigLoader.getConfigObject().getModuleList();
        if (moduleList == null || moduleList.size() == 0) {
            ConsoleManager.getConsole().printError("The Config not have any Functions! Tools will not work!");
            return;
        }

        for (ConfigObject.ModuleObject moduleObject : moduleList) {
            if (!moduleObject.isEnabled()) {
                continue;
            }

            if (modules.containsKey(moduleObject.getCommand())) {
                ConsoleManager.getConsole().printError("Module [" + moduleObject.getName() + "] Register Command [" + moduleObject.getCommand() + "] But Map already have it! Check config Command block!");
                continue;
            }

            if (!functions.containsKey(moduleObject.getFunction())) {
                ConsoleManager.getConsole().printError("Module [" + moduleObject.getName() + "] Used unknown Function [" + moduleObject.getFunction() + "] ! Check Function!");
                continue;
            }

            if (functions.get(moduleObject.getFunction()).isRequireParameter() && (moduleObject.getParameter() == null || moduleObject.getParameter().size() == 0)) {
                ConsoleManager.getConsole().printError("Function [" + moduleObject.getFunction() + "] is Require Parameter but not set! Please Set the Parameter!");
                continue;
            }

            modules.put(moduleObject.getCommand(), moduleObject);
        }
    }
}
