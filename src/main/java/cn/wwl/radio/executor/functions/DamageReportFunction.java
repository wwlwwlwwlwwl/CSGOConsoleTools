package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;

import java.util.List;

public class DamageReportFunction implements ConsoleFunction {

    @Override
    public boolean isRequireTicking() {
        return true;
    }

    @Override
    public List<String> isHookSpecialMessage() {
        return List.of("Damage Given to");
    }

    @Override
    public void onTick() {
        //TODO 移动AutoDamageReportMod
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {

    }

    @Override
    public void onHookSpecialMessage(String message) {

    }
}
