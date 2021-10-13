package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.executor.FunctionExecutor;

import java.util.List;

public class HelpFunction implements ConsoleFunction {
    @Override
    public void onExecuteFunction(List<String> parameter) {
        FunctionExecutor.printHelp();
    }
}
