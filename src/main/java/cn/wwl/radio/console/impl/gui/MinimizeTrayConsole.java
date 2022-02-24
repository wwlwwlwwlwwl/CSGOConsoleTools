package cn.wwl.radio.console.impl.gui;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.console.GameConsole;
import cn.wwl.radio.executor.functions.CustomMusicFunction;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.ConfigObject;
import cn.wwl.radio.music.BackgroundMusic;
import cn.wwl.radio.network.SocketTransfer;
import cn.wwl.radio.utils.TimerUtils;
import javazoom.jl.player.advanced.PausablePlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MinimizeTrayConsole implements GameConsole {

    private static final Map<TrayMessageCallback, ActionListener> ACTION_LISTENER_MAP = new HashMap<>();
    public static final ImageIcon IMAGE_ICON = new ImageIcon(Objects.requireNonNull(MinimizeTrayConsole.class.getResource("/icon.png")));
    public static final Image IMAGE = IMAGE_ICON.getImage().getScaledInstance(IMAGE_ICON.getIconWidth(), IMAGE_ICON.getIconHeight(), Image.SCALE_SMOOTH);
    public static final List<Image> IMAGE_LIST = List.of(
            IMAGE.getScaledInstance(16,16,Image.SCALE_SMOOTH),
            IMAGE.getScaledInstance(32,32,Image.SCALE_SMOOTH),
            IMAGE.getScaledInstance(64,64,Image.SCALE_SMOOTH),
            IMAGE.getScaledInstance(128,128,Image.SCALE_SMOOTH),
            IMAGE //Current default is 256x256
    );
    private static TrayIcon trayIcon;
    private final ManagerPanel panel = new ManagerPanel();

    private static Font placeHolderFont;
    private static Font trayFont;

    static {
        try {
            placeHolderFont = new Font("Verdana", Font.PLAIN, 10);
        } catch (Exception ignored) {}

        try {
            trayFont = new Font("微软雅黑", Font.PLAIN, 13);
        } catch (Exception ignored) {}
    }

    @Override
    public void init() {
        ManagerPanel.initManagerPanel();
//        ManagerPanel.showManagerPanel();
        startTray();
    }

    @Override
    public void printToConsole(String data) {
        panel.addPrintMessage(data);
    }

    @Override
    public void printError(String data) {
        System.err.println(data);
        panel.addErrorMessage(data);
    }

    @Override
    public void redirectGameConsole(String data) {
        panel.addRedirectMessage(data);
    }

    @Override
    public void startConsole() {
    }

    @Override
    public void printException(Exception e) {
        printError(e.toString());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            printError("\t" + stackTraceElement);
        }
    }

    private void startTray() {
        try {
            if (!SystemTray.isSupported()) {
                ConsoleManager.getConsole().printError("System Tray is not Supported!");
                return;
            }
            SystemTray tray = SystemTray.getSystemTray();
            Dimension trayIconSize = tray.getTrayIconSize();
            trayIcon = new TrayIcon(IMAGE.getScaledInstance((int) trayIconSize.getWidth(), (int) trayIconSize.getHeight(), Image.SCALE_SMOOTH));
            tray.add(trayIcon);
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try added System Tray throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }

        trayIcon.setToolTip("CSGOConsoleTools Tray");
        trayIcon.addActionListener(e -> {
            if (e.getModifiers() == 0 && trayIcon.getActionListeners().length == 1) {
                //只有这一个监听器的时候才会双击显示主界面 否则由其他的监听器来执行
                if (!ManagerPanel.isShowing()) {
                    ManagerPanel.showManagerPanel();
                }
            }
            //点击 Notification 或者双击托盘 产生Modifiers=0的事件
            //双击Notification Modifiers=1024
        });

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //Create PopupMenu when RightClick Tray
                updatePopupMenu(trayIcon);
            }
        });

        createTrayMessage("CSGOConsoleTools is Started at Tray Mode!\nClick the Tray to Show the GUI!");
    }

    public static void updatePopupMenu() {
        if (trayIcon == null) {
            return;
        }
        updatePopupMenu(trayIcon);
    }

    private static void updatePopupMenu(TrayIcon trayIcon) {
        PopupMenu menu = new PopupMenu("Menu");
        menu.add(createPlaceHolder("LobbyMusic"));
        ConfigObject configObject = ConfigLoader.getConfigObject();
        if (configObject != null) {
            if (configObject.isLobbyMusic()) {
                if (BackgroundMusic.isPlaying()) {
                    menu.add(createItem("Pause Music", e -> BackgroundMusic.pauseBackgroundMusic()));
                    menu.add(createItem("Stop Music", e -> BackgroundMusic.stopBackgroundMusic(false)));
                } else {
                    menu.add(createItem("Play Music", e -> {
                        BackgroundMusic.playBackgroundMusic(false);
                    }));
                }

                menu.add(createItem("Music Volume", e -> {
                    int musicGain = (int) BackgroundMusic.getVolume();
                    Dimension dimension = new Dimension(400, 125);
                    JFrame musicFrame = new JFrame();
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel();
                    JSlider slider = new JSlider();

                    musicFrame.setTitle("Change MusicVolume");
                    musicFrame.setMinimumSize(dimension);
                    musicFrame.setPreferredSize(dimension);
                    musicFrame.setResizable(false);
                    musicFrame.setLocationRelativeTo(musicFrame.getOwner());

                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setText(String.valueOf(musicGain));
                    slider.setMinimum(-50);
                    slider.setMaximum(50);
                    slider.setValue(musicGain);
                    slider.setMajorTickSpacing(1);
                    slider.setForeground(ManagerPanel.FOREGROUND_COLOR);
                    slider.setBackground(ManagerPanel.BACKGROUND_COLOR);
                    slider.addChangeListener(event -> {
                        int value = slider.getValue();
                        label.setText(String.valueOf(value));
                        BackgroundMusic.setVolume(value, false);
                    });

                    panel.setLayout(ManagerPanel.VERTICAL_LAYOUT);
                    panel.setOpaque(true);
                    panel.add(label);
                    panel.add(slider);
                    musicFrame.add(panel);
                    musicFrame.setVisible(true);
                }));
            }
        } else {
            //Waiting Config load done.
            TimerUtils.callMeLater(200, MinimizeTrayConsole::updatePopupMenu);
        }

        menu.addSeparator();

        menu.add(createItem("Exit", e -> SocketTransfer.getInstance().shutdown(true)));

//        menu.add(createItem("Debug", e -> {
//            createTrayMessage("Caption", "Debug Text Debug", TrayIcon.MessageType.ERROR, (type) -> {
//                switch (type) {
//                    case TrayMessageCallback.SINGLE_CLICK -> System.out.println("SingleClick");
//                    case TrayMessageCallback.DOUBLE_CLICK -> System.out.println("DoubleClick");
//                }
//            });
//        }));
        trayIcon.setPopupMenu(menu);
    }

    private static MenuItem createItem(String text, ActionListener action) {
        MenuItem item = new MenuItem(text);
        if (trayFont != null)
            item.setFont(trayFont);

        item.addActionListener(e -> {
            updatePopupMenu(trayIcon);
            action.actionPerformed(e);
        });
        return item;
    }

    private static MenuItem createPlaceHolder(String text) {
        MenuItem item = new MenuItem(text);
        if (placeHolderFont != null)
            item.setFont(placeHolderFont);

        item.setEnabled(false);
        return item;
    }

    @Override
    public void clear() {
        panel.clean();
    }

    public static void createTrayMessage(String message) {
        createTrayMessage(null,message,null,null);
    }

    public static void createTrayMessage(String message,TrayMessageCallback callback) {
        createTrayMessage(null,message,null,callback);
    }

    public static void createTrayMessage(String caption, String message, TrayIcon.MessageType type, TrayMessageCallback callback) {
        if (trayIcon == null) {
            ConsoleManager.getConsole().printError("Try push TrayMessage Failed: Tray not inited.");
            return;
        }
        if (caption == null || caption.length() == 0) {
            caption = "CSGOConsoleTools";
        }
        if (message == null || message.length() == 0) {
            ConsoleManager.getConsole().printError("Try push TrayMessage Failed: Message is Null");
            return;
        }
        if (type == null) {
            type = TrayIcon.MessageType.INFO;
        }

        if (callback != null) {
            ActionListener listener = e -> {
                switch (e.getModifiers()) {
                    case 0 -> callback.clickMessage(TrayMessageCallback.SINGLE_CLICK);
                    case 1024 -> callback.clickMessage(TrayMessageCallback.DOUBLE_CLICK);
                }
                removeTrayTask(callback);
            };
            ACTION_LISTENER_MAP.put(callback, listener);
            trayIcon.addActionListener(listener);
            // 如果10秒也没有触发 默认用户划掉了推送 取消掉这个任务
            TimerUtils.callMeLater(10000, () -> removeTrayTask(callback));
        }

        trayIcon.displayMessage(caption, message, type);
    }

    private static void removeTrayTask(TrayMessageCallback callback) {
        if (callback == null) {
             return;
        }

        for (Map.Entry<TrayMessageCallback, ActionListener> entry : ACTION_LISTENER_MAP.entrySet()) {
            TrayMessageCallback key = entry.getKey();
            ActionListener value = entry.getValue();
            if (key == callback) {
                trayIcon.removeActionListener(value);
                break;
            }
        }
    }
}
