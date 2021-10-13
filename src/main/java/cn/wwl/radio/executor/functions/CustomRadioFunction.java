package cn.wwl.radio.executor.functions;

import cn.wwl.radio.SocketTransfer;
import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.utils.TextMarker;

import java.util.List;
import java.util.Random;

public class CustomRadioFunction implements ConsoleFunction {

    @Override
    public boolean isRequireParameter() {
        return true;
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        String radio = "hi";
        String message = null;
        if (parameter.size() == 1) {
            message = parameter.get(0);
        } else if (parameter.size() == 2) {
            radio = parameter.get(0);
            message = parameter.get(1);
        } else {
            int random = new Random().nextInt(parameter.size() - 1) + 1;
            System.out.println("Random count : " + (parameter.size() - 1) + " , Now random : " + (random));
            radio = parameter.get(0);
            message = parameter.get(random);
        }
        String cmd = TextMarker.replaceHumanCode("playerradio " + radio + " \"" + message + "\"");
        SocketTransfer.getInstance().pushToConsole(cmd);
    }
}
