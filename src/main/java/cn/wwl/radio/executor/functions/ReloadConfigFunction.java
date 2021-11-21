package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.network.SocketTransfer;

import java.util.List;

public class ReloadConfigFunction implements ConsoleFunction {
    @Override
    public void onExecuteFunction(List<String> parameter) {
        SocketTransfer.getInstance().echoToConsole("Reloading Config...");

        ConfigLoader.loadConfigObject(true);

        SocketTransfer.getInstance().echoToConsole("Done.");
    }
}
