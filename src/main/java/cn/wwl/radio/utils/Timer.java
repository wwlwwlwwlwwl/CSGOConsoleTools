package cn.wwl.radio.utils;

public class Timer {
    private long previousTime;
    public Timer() {
        reset();
    }

    public boolean isReachedTime(long elapsedTime) {
        long current = getTime();
        return (current - elapsedTime) >= previousTime;
    }

    public void reset() {
        this.previousTime = getTime();
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }
}
