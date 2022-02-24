package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.utils.TextMarker;
import cn.wwl.radio.utils.TimerUtils;

import java.util.List;
import java.util.Random;

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
        SocketTransfer.getInstance().echoToConsole("DEBUG: START");

//        SocketTransfer.getInstance().pushToConsole("say \"\u202EHello World!\"");

//        SocketTransfer.getInstance().pushToConsole("playerchatwheel CW.Debug \"\u202EHello World!\"");
//playerchatwheel CW.TwoEnemiesLeft Chatwheel_twoleft
        SocketTransfer.getInstance().echoToConsole("DEBUG: END");
    }
}
