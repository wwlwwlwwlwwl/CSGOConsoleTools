package cn.wwl.radio.executor.functions;

import cn.wwl.radio.SocketTransfer;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.utils.TextMarker;

import java.util.List;
import java.util.Locale;

public class DebugFunction implements ConsoleFunction {

    @Override
    public boolean isRequireTicking() {
        return false;
    }

    @Override
    public boolean isRequireParameter() {
        return true;
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        String cmd = parameter.get(0);
        String marker = TextMarker.replaceHumanCode(cmd);
        SocketTransfer.getInstance().pushToConsole(marker);
    }
}
