package cn.wwl.radio.executor;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.functions.*;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.ConfigObject;
import cn.wwl.radio.file.RadioFileManager;
import cn.wwl.radio.network.SocketTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FunctionExecutor {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static boolean isStartTickThread = false;
    private static final Map<String, ConfigObject.ModuleObject> modules = new HashMap<>();
    private static final Map<String, ConsoleFunction> functions = new HashMap<>();
    private static final Map<String, ConsoleFunction> messageHookMap = new HashMap<>();
    private static boolean isRegistered = false;
    public static final String HOOK_HEAD = "HookExecute";

    public static void executeFunction(String cmd) {
        String prefix = ConfigLoader.getConfigObject().getPrefix();
        if (cmd.startsWith("Unknown")) {
            if (!cmd.contains(prefix)) {
                return;
            }
        } else if (cmd.contains(ConfigLoader.getConfigObject().getPrefix()) || cmd.contains(HOOK_HEAD)) {
            if (cmd.startsWith(HOOK_HEAD)) {
                cmd = cmd.replace(ConfigLoader.getConfigObject().getPrefix() + "_", "");
            } else {
                String command = cmd.substring(prefix.length() + 1);
                System.out.println("Internal Execution: " + command);
                cmd = command;
            }
        } else {
            return;
        }

        String func;
        try {
            func = removeUnknownCommandTag(cmd);
        } catch (Exception e) {
            func = cmd;
        }

        if (!modules.containsKey(func)) {
            if (func.contains(prefix)) {
                String wrong = "Wrong function Usage.";
                String substring = func.equals(prefix) ? "" : func.substring(prefix.length() + 1);
                List<String> guessList = new ArrayList<>();
                for (Map.Entry<String, ConfigObject.ModuleObject> entry : modules.entrySet()) {
                    if (entry.getKey().contains(substring)) {
                        guessList.add(prefix + "_" + entry.getKey());
                    }
                }
                if (!guessList.isEmpty()) {
                    wrong += " do you mean " + guessList + "?";
                }
                SocketTransfer.getInstance().echoToConsole(wrong);
            }
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

    /**
     * Remove the "Unknown command : ***" tags
     * @param cmd message
     * @return message without "Unknown ***"
     */
    public static String removeUnknownCommandTag(String cmd) {
        if (cmd.startsWith("Unknown")) {
            /*??????????????????:
            1. ????????? : Unknown command "hello"
            2. ????????? : Unknown command : hello
            ??????V?????????
             */
            try {
                int firstIndex = cmd.indexOf("\"") + 1; //????????????
                int lastIndex = cmd.lastIndexOf("\"");
                return cmd.substring(firstIndex, lastIndex);
            } catch (Exception e) {
                return cmd.split(":")[1].trim(); //?????????????????? ?????????????????? ?????????????????????
            }
        } else {
            return cmd.substring(FunctionExecutor.HOOK_HEAD.length() + 1).trim();
        }
    }

    public static void printHelp() {
        SocketTransfer.getInstance().echoToConsole("Help list : ");
        modules.forEach((str, obj) -> SocketTransfer.getInstance().echoToConsole("cmd : " + str + " , {F: " + obj.getFunction() + "}"));
    }

    public static void registerGameHook() {
        registerMessageHook();

        if (!isStartTickThread) {
            startTickThread();
        }

        functions.forEach((name, func) -> func.onInit());
        RadioFileManager.getInstance();
    }

    public static void callRebootHook() {
        functions.forEach((s,f) -> f.onApplicationReboot());
    }

    private static void registerMessageHook() {
        if (isRegistered) {
            return;
        }
        isRegistered = true;
        //Special message Hook
        SocketTransfer.getInstance().addListenerTask("SpecialMessageHook", message -> {
            for (Map.Entry<String, ConsoleFunction> entry : FunctionExecutor.getMessageHookMap().entrySet()) {
                if (message.contains(entry.getKey())) {
                    FunctionExecutor.executeMessageHook(entry.getValue() ,message);
                    break;
                }
            }
        });

        //Function Hook
        SocketTransfer.getInstance().addListenerTask("FunctionHook", FunctionExecutor::executeFunction);

        //Player Chat Hook
        SocketTransfer.getInstance().addListenerTask("PlayerChatHook", message -> {
            try {
                //??????????????????wwl??? @ ????????????????????? ??? 123123
                //???****???wwl??? @ ****????????? ??? 123123
                //??????????????????wwl??? ??? #music
                if (message.contains("??????????????????") || message.contains("???****???") || message.contains("??????????????????")) {
                    String playerName = message.substring(6, (
                            message.contains("@") ? message.indexOf("@") : message.indexOf(" ??? ")
                    )).trim();
                    String[] split = message.split(" ??? ");
                    String talkMessage = split[split.length - 1].trim();
                    ConsoleManager.getConsole().printToConsole("ChatHook : [P: " + playerName + " ,M: " + talkMessage + "]");

                    for (Map.Entry<String, ConsoleFunction> entry : functions.entrySet()) {
                        ConsoleFunction function = entry.getValue();
                        if (function.isHookPlayerChat()) {
                            function.onHookPlayerChat(playerName, talkMessage);
                        }
                    }
                }
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Execute PlayerChat throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        });

        SocketTransfer.getInstance().addListenerTask("CommandReplace", str -> {
            if (!str.contains("Unknown")) {
                return;
            }

            String unknownHead = removeUnknownCommandTag(str);
            String newCommand = commandReplace(unknownHead);
            if (!unknownHead.equalsIgnoreCase(newCommand)) { //?????????????????????????????????
                SocketTransfer.getInstance().echoToConsole("Cmd [" + str + "] has been Replace to [" + newCommand + "] .");
//                System.out.println("CMD: " + str + " -> " + newCommand);
                SocketTransfer.getInstance().pushToConsole(newCommand);
            }
        });
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
        isStartTickThread = true;
        executor.scheduleAtFixedRate(() ->
                functions.forEach((name, func) -> {
                    if (func.isRequireTicking()) {
                        try {
                            func.onTick();
                        } catch (Exception e) {
                            ConsoleManager.getConsole().printError("Try Tick function: " + name + " Throw Exception!");
                            ConsoleManager.getConsole().printException(e);
                        }
                    }
        }), 0, 10, TimeUnit.MILLISECONDS);
    }

    private static String commandReplace(String oldCommand) {
        for (Map.Entry<String, String> entry : ConfigLoader.getConfigObject().getAutoReplaceCommand().entrySet()) {
            if (entry.getKey().equals(oldCommand)) {
                return entry.getValue();
            }
        }
        return oldCommand;
    }

    public static Map<String, ConsoleFunction> getMessageHookMap() {
        return messageHookMap;
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
        functions.put("CustomMusic",new CustomMusicFunction());
        functions.put("DamageReport",new DamageReportFunction());
        functions.put("AdnmbClient", new AdnmbClientFunction());
        //TODO ???????????????????????????????????????
        functions.forEach((s,func) -> {
            List<String> specialMessage = func.isHookSpecialMessage();
            if (!specialMessage.isEmpty()) {
                specialMessage.forEach(str -> messageHookMap.put(str,func));
            }
        });
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
