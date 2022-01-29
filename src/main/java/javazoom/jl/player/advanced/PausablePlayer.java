package javazoom.jl.player.advanced;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.Player;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PausablePlayer {

    public final static int NOTINIT = -1;
    public final static int NOTSTARTED = 0;
    public final static int PLAYING = 1;
    public final static int PAUSED = 2;
    public final static int FINISHED = 3;

    // the player actually doing all the work
    private final Player player;

    // locking object used to communicate with player thread
    private final Object playerLock = new Object();
    private final Executor fadeExecutor = Executors.newSingleThreadExecutor();
    private final List<Runnable> callbacks = new ArrayList<>();

    // status variable what player thread is doing/supposed to do
    private int playerStatus = NOTSTARTED;

    private float previusGain = 0F;
    private boolean isPausing = false;
    private static final float GAIN_SPEED = 0.05F;

    public PausablePlayer(final InputStream inputStream) throws JavaLayerException {
        this.player = new Player(inputStream);
    }

    public PausablePlayer(final InputStream inputStream, final AudioDevice audioDevice) throws JavaLayerException {
        this.player = new Player(inputStream, audioDevice);
    }

    /**
     * Starts playback (resumes if paused)
     */
    public void play() {
        isPausing = false;
        synchronized (playerLock) {
            switch (playerStatus) {
                case NOTSTARTED:
                    final Runnable r = this::playInternal;
                    final Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setPriority(Thread.MAX_PRIORITY);
                    t.setName("Music Decode Thread-" + new Random().nextInt(10000));
                    playerStatus = PLAYING;
                    t.start();
                    break;
                case PAUSED:
//                    resume();
                    fadeResume();
                    break;
                case PLAYING:
//                    pause();
                    fadePause();
                    break;
                default:
                    break;
            }
        }
    }

    public void addCallback(Runnable callback) {
        if (callback != null && !callbacks.contains(callback))
            callbacks.add(callback);
    }

    public void fadePlay(float volume) {
        fadeExecutor.execute(() -> {
            Thread.currentThread().setName("SoundFade Thread");
            synchronized (fadeExecutor) {
                play();
                float gain = volume - 30F;
                previusGain = volume;
                while (gain < previusGain) {
                    gain += GAIN_SPEED;
                    player.setGain(gain);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                player.setGain(previusGain);
            }
        });
    }

    public void fadeResume() {
        fadeExecutor.execute(() -> {
            Thread.currentThread().setName("SoundFade Thread");
            synchronized (fadeExecutor) {
                resume();
                float gain = getGain();
                while (gain < previusGain) {
                    gain += GAIN_SPEED;
                    player.setGain(gain);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                player.setGain(previusGain);
            }
        });
    }

    public void fadePause() {
        fadeExecutor.execute(() -> {
            isPausing = true;
            Thread.currentThread().setName("SoundFade Thread");
            synchronized (fadeExecutor) {
                previusGain = getGain();
                float gain = getGain();
                while (gain > -50.0F) {
                    gain -= GAIN_SPEED;
                    player.setGain(gain);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                player.setGain(-50.0F);
                pause();
            }
        });
    }

    public void fadeExit() {
        fadeExecutor.execute(() -> {
            Thread.currentThread().setName("SoundFade Thread");
            synchronized (fadeExecutor) {
                previusGain = getGain();
                float gain = getGain();
                while (gain > -50.0F) {
                    gain -= GAIN_SPEED;
                    player.setGain(gain);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                player.setGain(-50.0F);
                close();
            }
        });
    }

    /**
     * Pauses playback. Returns true if new state is PAUSED.
     */
    public boolean pause() {
        synchronized (playerLock) {
            if (playerStatus == PLAYING) {
                playerStatus = PAUSED;
            }
            return playerStatus == PAUSED;
        }
    }

    /**
     * Resumes playback. Returns true if the new state is PLAYING.
     */
    public boolean resume() {
        isPausing = false;
        synchronized (playerLock) {
            if (playerStatus == PAUSED) {
                playerStatus = PLAYING;
                playerLock.notifyAll();
            }
            return playerStatus == PLAYING;
        }
    }

    /**
     * Stops playback. If not playing, does nothing
     */
    public void stop() {
        synchronized (playerLock) {
            playerStatus = FINISHED;
            playerLock.notifyAll();
        }
    }

    private void playInternal() {
        while (playerStatus != FINISHED) {
            try {
                if (!player.play(1)) {
                    break;
                }
            } catch (final JavaLayerException e) {
                break;
            }
            // check if paused or terminated
            synchronized (playerLock) {
                while (playerStatus == PAUSED) {
                    try {
                        playerLock.wait();
                    } catch (final InterruptedException e) {
                        // terminate player
                        break;
                    }
                }
            }
        }
        callbacks.forEach(Runnable::run);
        close();
    }

    /**
     * Closes the player, regardless of current state.
     */
    public void close() {
        synchronized (playerLock) {
            playerStatus = FINISHED;
        }
        try {
            player.close();
        } catch (final Exception e) {
            // ignore, we are terminating anyway
        }
    }

    public float getGain() {
        return player.getGain();
    }

    public int getPosition() {
        return player.getPosition();
    }

    public void fadeSetGain(float val) {
        fadeExecutor.execute(() -> {
            Thread.currentThread().setName("SoundFade Thread");
            synchronized (fadeExecutor) {
                float gain = getGain();
                boolean isIncrease = val > gain;
                if (isIncrease) {
                    while (val > gain) {
                        gain += GAIN_SPEED;
                        setGain(gain);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    while (gain > val) {
                        gain -= GAIN_SPEED;
                        setGain(gain);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                setGain(val);
            }
        });
    }

    public void setGain(float val) {
        player.setGain(val);
    }

    public int getPlayerStatus() {
        if (isPausing) {
            return PAUSED;
        }
        return playerStatus;
    }

    //    // demo how to use
//    public static void main(String[] argv) {
//        try {
//            FileInputStream input = new FileInputStream("myfile.mp3");
//            PausablePlayer player = new PausablePlayer(input);
//
//            // start playing
//            player.play();
//
//            // after 5 secs, pause
//            Thread.sleep(5000);
//            player.pause();
//
//            // after 5 secs, resume
//            Thread.sleep(5000);
//            player.resume();
//        } catch (final Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}