package cn.wwl.radio.network.task;

public interface ListenerTask {
    /**
     * 事件的回调
     * @param message 从控制台接收到的信息
     */
    void listen(String message);

    /**
     * 是否应该移除该监听器
     * @return 如果返回true, 那么将会在下次回调之后删除掉该监听器
     */
    default boolean isShouldRemove() {
        return false;
    }
}
