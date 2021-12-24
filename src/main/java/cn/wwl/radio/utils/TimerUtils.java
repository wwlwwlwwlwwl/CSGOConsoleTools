package cn.wwl.radio.utils;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtils {
    private static final Timer timer = new Timer();

    public static void callMeLater(long ms, TimerTask callWhat) {
        timer.schedule(callWhat,ms);
    }

    public static void callMeLater(long ms, TimerCallTask callWhat) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                callWhat.call();
            }
        };
        timer.schedule(task,ms);
    }

    public interface TimerCallTask {
        void call();
    }
}
