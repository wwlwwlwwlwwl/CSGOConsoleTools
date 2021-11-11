package cn.wwl.radio.executor;

import java.util.List;

public interface ConsoleFunction {

    /**
     * 在将这里设置为True之后,每10ms会调用一次onTick方法
     * @return 如果不需要循环调用,返回False
     */
    default boolean isRequireTicking() {
        return false;
    }

    /**
     * 声明方法是否必须需要参数
     * @return 如果为True,模组在没有参数的情况下将不会被注册
     */
    default boolean isRequireParameter() {
        return false;
    }

    /**
     * 声明是否需要获取玩家的聊天内容
     * @return 如果为True 则会在玩家聊天时获取聊天内容触发事件
     */
    default boolean isHookPlayerChat() {return false;}

    /**
     * 声明想从控制台勾住特定信息 如果信息在控制台中出现 则会进行调用
     * @return 要包含的文字 以List的方法返回
     */
    default List<String> isHookSpecialMessage() {return List.of();}

    /**
     * 循环调用的方法,需要被覆写,如果不需要该功能可以将{@code isRequireTicking()}关闭
     */
    default void onTick() {}

    /**
     * 当指令被调用的时候执行
     * @param parameter 配置中定义的参数
     */
    default void onExecuteFunction(List<String> parameter) {}

    /**
     * 当勾住了特定的信息时调用
     * @param message 被勾住的信息
     */
    default void onHookSpecialMessage(String message) {}

    /**
     * 当声明勾住玩家信息时调用
     * @param name 玩家名称
     * @param content 玩家聊天的内容
     */
    default void onHookPlayerChat(String name,String content) {}
}
