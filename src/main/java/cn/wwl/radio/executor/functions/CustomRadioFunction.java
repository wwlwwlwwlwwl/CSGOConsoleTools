package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.network.SocketTransfer;
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
        String message;
        if (parameter.size() == 1) {
            message = parameter.get(0);
        } else if (parameter.size() == 2) {
            radio = parameter.get(0);
            message = parameter.get(1);
        } else {
            int random = new Random().nextInt(parameter.size() - 1) + 1;
//            System.out.println("Random count : " + (parameter.size() - 1) + " , Now random : " + (random));
            radio = parameter.get(0);
            message = parameter.get(random);
        }
        sendCustomRadio(radio,message,false);
    }

    public static void sendCustomRadio(String radio,String message,boolean removeHead) {
        if (radio == null || message == null) {
            return;
        }

        if (removeHead) {
            message = "#next#" + message;
        }
//        System.out.println("Debug: " + message);
        String cmd = TextMarker.replaceHumanCode("playerradio " + radio + " \"" + message + "\"");
        SocketTransfer.getInstance().pushToConsole(cmd); //TODO 自动切断句子避免过长
    }

    public static void sendCustomRadio(String message,boolean removeHead) {
        sendCustomRadio("hi",message,removeHead);
    }

    public static void sendCustomRadio(String message) {
        sendCustomRadio("hi",message,false);
    }
}
