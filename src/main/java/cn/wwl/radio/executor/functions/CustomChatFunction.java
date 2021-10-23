package cn.wwl.radio.executor.functions;

import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.utils.TextMarker;

import java.util.List;
import java.util.Random;

public class CustomChatFunction implements ConsoleFunction {

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
        String s = parameter.get(0);
        if (s.equals("ClearChat")) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                builder.append(TextMarker.换行.getCode());
            }
            SocketTransfer.getInstance().pushToConsole("say " + builder.toString());
            return;
        }

        if (parameter.size() > 1) {
            Random random = new Random();
            s = parameter.get(random.nextInt(parameter.size() - 1));
        }

        SocketTransfer.getInstance().pushToConsole("say " + s);
    }
}
