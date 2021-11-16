package cn.wwl.radio.executor.functions;

import cn.wwl.radio.executor.ConsoleFunction;
import cn.wwl.radio.network.SocketTransfer;
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
        FakeCaseManager.CSGOCaseDrop fakeCase = FakeCaseManager.openFakeCase(FakeCaseManager.CSGOCases.OPERATION_RIPTIDE_CASE);
        CustomRadioFunction.sendCustomRadio(SocketTransfer.getInstance().getPlayerName() + " #white#从武器箱中获得了: " + fakeCase.getColorSkinName(),true);
    }
}
