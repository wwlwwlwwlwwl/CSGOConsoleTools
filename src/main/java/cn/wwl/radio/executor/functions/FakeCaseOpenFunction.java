package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.utils.FakeCaseManager;

import java.util.List;

public class FakeCaseOpenFunction implements ConsoleFunction {

    @Override
    public boolean isRequireTicking() {
        return false;
    }

    @Override
    public boolean isRequireParameter() {
        return false;
    }

    @Override
    public void onExecuteFunction(List<String> parameter) {
        //TODO 用name或者status获取玩家名称
        String userName = "wwl";
        FakeCaseManager.CSGOCaseDrop fakeCase = FakeCaseManager.openFakeCase(FakeCaseManager.CSGOCases.OPERATION_RIPTIDE_CASE);
        CustomRadioFunction.sendCustomRadio(userName + " #white#从武器箱中获得了: " + fakeCase.getColorSkinName(),true);
    }
}
