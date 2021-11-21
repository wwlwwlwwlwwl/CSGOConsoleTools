package cn.wwl.radio.console.impl.gui;

public interface TrayMessageCallback {
    int SINGLE_CLICK = 0;
    int DOUBLE_CLICK = 1;
    void clickMessage(int clickType);
}
