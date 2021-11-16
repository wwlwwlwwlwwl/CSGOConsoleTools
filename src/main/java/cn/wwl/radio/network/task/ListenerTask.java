package cn.wwl.radio.network.task;

public interface ListenerTask {
    void listen(String message);

    default boolean isShouldRemove() {
        return false;
    }
}
