package cn.wwl.radio.module;

import cn.wwl.radio.network.SocketTransfer;

import java.util.List;

@Deprecated
public abstract class Module {

    /**
     * 当输入的命令命中时调用
     * @param commands 玩家输入的命令
     */
    public abstract void onHookCommand(String commands);

    /**
     * 要勾住的命令
     * @return 包含所有要勾住的命令的列表,比较时会使用contains进行检测
     */
    public abstract List<String> hookedCommands();

    /**
     * 当模组被初始化时调用(Socket连接成功,全部就绪)
     */
    public void onInit() {}

    /**
     * 循环触发事件
     */
    public void onTick() {}


    protected void echo(String str) {
        SocketTransfer.getInstance().echoToConsole(str);
    }
    /**
     * 将命令快速重定向至控制台
     * @param cmd 参考SocketTransfer中的同名方法
     */
    protected void pushToConsole(String cmd) {
        SocketTransfer.getInstance().pushToConsole(cmd);
    }
}
