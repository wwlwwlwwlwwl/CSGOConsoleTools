package cn.wwl.radio.console.impl.gui;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.network.SocketTransfer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManagerPanel {
    private JButton normalInput;
    private JButton errorInput;
    private JButton consoleInput;
    private JPanel ButtonPanel;
    private JPanel InputPanel;
    private JTextField inputBox;
    private JButton submitButton;
    private JTextPane consolePane;

    private JPanel mainPanel;
    private JButton settingButton;
    private JScrollPane scrollPane;

    private static final List<String> normalList = new ArrayList<>() {{
        add("Normal Message console.");
    }};
    private static final List<String> errorList = new ArrayList<>() {{
        add("Error Message console.");
    }};
    private static final List<String> consoleList = new ArrayList<>() {{
        add("Console Message console(lol).");
    }};

    private enum MessageType {
        NORMAL,
        ERROR,
        CONSOLE
    }

    private static final List<List<String>> LISTS = List.of(normalList, errorList, consoleList);
    private final List<JButton> BUTTONS;
    private static SimpleAttributeSet prevAttribute = null;

    public static final Color NORMAL_MESSAGE_COLOR = new Color(188, 210, 238);
    public static final Color ERROR_MESSAGE_COLOR = new Color(255, 99, 71);
    public static final Color CONSOLE_MESSAGE_COLOR = new Color(0, 229, 229);

    public static final Color BACKGROUND_COLOR = new Color(33, 37, 43);
    public static final Color FOREGROUND_COLOR = new Color(160, 167, 180);
    public static final Color ACTIVATE_COLOR = new Color(104, 217, 255);
    public static final Color NOTIFICATION_COLOR = new Color(52, 89, 153);
    public static final Color CAROT_COLOR = new Color(0, 191, 255, 200);

    public static final VerticalFlowLayout VERTICAL_LAYOUT = new VerticalFlowLayout(VerticalFlowLayout.TOP, 15, 15, true);
    public static final FlowLayout DEFAULT_LAYOUT = new FlowLayout(FlowLayout.LEFT, 15, 15);

    private JButton activateButton;
    private static ManagerPanel instance;
    private static SettingsPanel settingsPanel;
    private static final JFrame frame = new JFrame("ManagerPanel");

    private static boolean minimizeTray = false;

    public ManagerPanel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("OptionPane.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("Panel.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("Button.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("TextField.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("Label.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("FormattedTextField.background", ManagerPanel.BACKGROUND_COLOR);
            UIManager.put("ScrollBar.background", ManagerPanel.BACKGROUND_COLOR);

            UIManager.put("OptionPane.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("Panel.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("Button.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("TextField.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("Label.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("FormattedTextField.foreground", ManagerPanel.FOREGROUND_COLOR);
            UIManager.put("ScrollBar.foreground", ManagerPanel.FOREGROUND_COLOR);

        } catch (Exception ignored) {
        }

        instance = this;
        settingsPanel = new SettingsPanel(this);
        BUTTONS = List.of(normalInput, errorInput, consoleInput);
        activateButton = normalInput;
        activateButton.setEnabled(false);
        DEFAULT_LAYOUT.setAlignOnBaseline(true);
        initCustomStyle();
        updateTextarea();
        scrollPane.setAutoscrolls(true);
        ActionListener buttonAction = e -> {
            activateButton.setEnabled(true);
            activateButton = (JButton) e.getSource();
            updateTextarea();
            activateButton.setEnabled(false);
        };

        normalInput.addActionListener(buttonAction);

        errorInput.addActionListener(buttonAction);

        consoleInput.addActionListener(buttonAction);

        settingButton.addActionListener(e -> openSettingsPanel());

        submitButton.addActionListener(e -> textSubmit());

        inputBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) { //Enter key
                    textSubmit();
                }
            }
        });
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static void initManagerPanel() {
        frame.setIconImages(MinimizeTrayConsole.IMAGE_LIST);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    SocketTransfer.getInstance().shutdown(true);
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (!minimizeTray) {
                    minimizeTray = true;
                    MinimizeTrayConsole.createTrayMessage("Application Now Minimize to Tray!\nDouble Click the TrayIcon to Show Again!");
                }
                hideManagerPanel();
            }
        });
        frame.setContentPane(instance.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(false);
        frame.setLocationRelativeTo(frame.getOwner());
    }

    public void openSettingsPanel() {
        settingsPanel.init();
        frame.setVisible(false);
        settingsPanel.getFrame().setVisible(true);
    }

    public void closeSettingsPanel() {
        frame.setVisible(true);
        settingsPanel.getFrame().setVisible(false);
    }

    public static void showManagerPanel() {
        frame.setVisible(true);
        frame.setAutoRequestFocus(true);
        frame.setFocusable(true);
        frame.toFront();
    }

    public static void hideManagerPanel() {
        frame.setVisible(false);
    }

    public static boolean isShowing() {
        return frame.isVisible();
    }

    private void initCustomStyle() {
        setSkin(normalInput);
        setSkin(errorInput);
        setSkin(consoleInput);
        setSkin(ButtonPanel);
        setSkin(InputPanel);
        setSkin(inputBox);
        setSkin(submitButton);
        setSkin(consolePane);
        setSkin(mainPanel);
        setSkin(settingButton);
        setSkin(scrollPane);
    }

    public static void setSkin(JComponent component) {
        try {
            component.setBackground(ManagerPanel.BACKGROUND_COLOR);
        } catch (Exception ignored) {
        }
        try {
            component.setForeground(ManagerPanel.FOREGROUND_COLOR);
        } catch (Exception ignored) {
        }

        if (component instanceof JTextComponent) {
            ((JTextComponent) component).setCaretColor(CAROT_COLOR);
        }
    }

    private void textSubmit() {
        String text = inputBox.getText();
        if (text == null || text.length() == 0) {
            return;
        }
        SocketTransfer.getInstance().pushToConsole(text);
        inputBox.setText("");
    }

    public void addPrintMessage(String s) {
        normalList.add(s);
        if (activateButton == normalInput) {
            updateText(s);
        } else {
            showAlert(normalInput);
        }
    }

    public void addErrorMessage(String s) {
        errorList.add(s);
        if (activateButton == errorInput) {
            updateText(s);
        } else {
            showAlert(errorInput);
        }
    }

    public void addRedirectMessage(String s) {
        consoleList.add(s);
        if (activateButton == consoleInput) {
            updateText(s);
        } else {
            showAlert(consoleInput);
        }
    }

    private void updateTextarea() {
        if (normalInput.equals(activateButton)) {
            updateTextareaText(MessageType.NORMAL);
        } else if (errorInput.equals(activateButton)) {
            updateTextareaText(MessageType.ERROR);
        } else if (consoleInput.equals(activateButton)) {
            updateTextareaText(MessageType.CONSOLE);
        }
    }

    private void showAlert(JButton button) {
        button.setBackground(NOTIFICATION_COLOR);
    }

    private void updateText(String s) {
        LISTS.forEach(list -> {
            if (list.size() >= 1000) {
                list.clear();
                System.gc();
            }
        });
        StyledDocument styledDocument = consolePane.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(), s + "\n", prevAttribute);
        } catch (Exception e) {
            ConsoleManager.getConsole().printException(e);
        }
    }

    private void updateTextareaText(MessageType type) {
        StyledDocument styledDocument = consolePane.getStyledDocument();
        Color color;
        List<String> list;
        BUTTONS.forEach(btn -> {
            if (btn.getBackground() == ACTIVATE_COLOR) {
                btn.setBackground(BACKGROUND_COLOR);
            }
        });
        activateButton.setBackground(ACTIVATE_COLOR);
        switch (type) {
            case NORMAL -> {
                color = NORMAL_MESSAGE_COLOR;
                list = normalList;
            }
            case ERROR -> {
                color = ERROR_MESSAGE_COLOR;
                list = errorList;
            }
            case CONSOLE -> {
                color = CONSOLE_MESSAGE_COLOR;
                list = consoleList;
            }
            default -> {
                //Should not happen
                color = Color.black;
                list = normalList;
            }
        }
        inputBox.setForeground(color);
        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        simpleAttributeSet.addAttribute(StyleConstants.ColorConstants.Foreground, color);
        simpleAttributeSet.addAttribute(StyleConstants.ColorConstants.Bold, true);
        prevAttribute = simpleAttributeSet;
        StringBuilder builder = new StringBuilder();
        list.forEach(str -> builder.append(str).append("\n"));
        try {
            styledDocument.remove(0, styledDocument.getLength());
            styledDocument.insertString(0, builder.toString(), simpleAttributeSet);
        } catch (Exception e) {
            ConsoleManager.getConsole().printException(e);
        }
    }

    public void clean() {
        LISTS.forEach(List::clear);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(900, 500));
        mainPanel.setOpaque(true);
        mainPanel.setPreferredSize(new Dimension(900, 500));
        ButtonPanel = new JPanel();
        ButtonPanel.setLayout(new GridLayoutManager(1, 4, new Insets(5, 5, 0, 5), -1, -1));
        mainPanel.add(ButtonPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        normalInput = new JButton();
        normalInput.setText("Normal");
        normalInput.setToolTipText("Application Default Output");
        ButtonPanel.add(normalInput, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        consoleInput = new JButton();
        consoleInput.setText("Console");
        consoleInput.setToolTipText("Game Console Output");
        ButtonPanel.add(consoleInput, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorInput = new JButton();
        errorInput.setText("Error");
        errorInput.setToolTipText("Application Error Output");
        ButtonPanel.add(errorInput, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingButton = new JButton();
        settingButton.setText("Settings");
        ButtonPanel.add(settingButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        InputPanel = new JPanel();
        InputPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 5, 5, 5), -1, -1));
        mainPanel.add(InputPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), null, 0, false));
        inputBox = new JTextField();
        Font inputBoxFont = UIManager.getFont("TextArea.font");
        if (inputBoxFont != null) inputBox.setFont(inputBoxFont);
        inputBox.setToolTipText("Enter Console command Here!");
        InputPanel.add(inputBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(150, 25), null, 0, false));
        submitButton = new JButton();
        submitButton.setText("Submit");
        InputPanel.add(submitButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), null, new Dimension(-1, 25), 0, false));
        scrollPane = new JScrollPane();
        mainPanel.add(scrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        consolePane = new JTextPane();
        consolePane.setEditable(false);
        Font consolePaneFont = UIManager.getFont("TextArea.font");
        if (consolePaneFont != null) consolePane.setFont(consolePaneFont);
        consolePane.setText("The message will Redirect to here.");
        consolePane.setToolTipText("The message will Redirect to here.");
        scrollPane.setViewportView(consolePane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
