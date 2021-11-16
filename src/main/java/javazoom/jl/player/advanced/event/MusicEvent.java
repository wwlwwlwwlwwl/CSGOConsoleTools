package javazoom.jl.player.advanced.event;

public class MusicEvent {

    private EventType type;
    private float volume;
    private boolean canceled;

    public MusicEvent(float volume, EventType type) {
        this.volume = volume;
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public static enum EventType {
        MUSIC_START,
        SETTING_VOLUME;
    }
}
