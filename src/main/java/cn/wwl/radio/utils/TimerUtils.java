package cn.wwl.radio.utils;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtils {
    private static final Timer timer = new Timer();

    public static TimerTask callMeLater(long ms, TimerTask callWhat) {
        timer.schedule(callWhat,ms);
        return callWhat;
    }
}
