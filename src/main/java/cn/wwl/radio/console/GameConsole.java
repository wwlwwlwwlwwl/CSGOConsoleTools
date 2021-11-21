package cn.wwl.radio.console;

public interface GameConsole {

    /**
     * 初始化整个控制台
     */
    void init();

    /**
     * 将数据输出到定义的控制台中
     * @param data 要输出的数据
     */
    void printToConsole(String data);

    /**
     * 同'printToConsole',也是输出到控制台,不过为错误输出
     * @param data 要输出的数据
     */
    void printError(String data);

    /**
     * 来自游戏内控制台重定向的数据
     * @param data 游戏内的所有输出
     */
    void redirectGameConsole(String data);

    /**
     * Socket连接成功后 调用该方法
     */
    void startConsole();

    /**
     * 请求清空整个控制台
     */
    void clear();

    /**
     * 将异常输入向控制台
     * @param e 异常
     */
    void printException(Exception e);
}
