package cn.wwl.radio.console.impl.gui;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.utils.TextMarker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;

import static cn.wwl.radio.console.impl.gui.ManagerPanel.BACKGROUND_COLOR;
import static cn.wwl.radio.console.impl.gui.ManagerPanel.setSkin;

public class RadioPreviewPanel {
    private JButton saveButton;
    private JButton cancelButton;
    private JScrollPane textPanel;
    private JPanel colorPanel;
    private JPanel mainPanel;
    private JPanel buttonPane;
    private JTextPane textPane;
    private JScrollPane resultPanel;
    private JTextPane resultPane;

    private static final JFrame frame = new JFrame("RadioPreviewPanel");
    private static volatile RadioPreviewPanel instance;
    private static RadioCallback callback;
    private static boolean inited = false;

    private static final Map<TextMarker, AttributeSet> ATTRIBUTE_MAP = new HashMap<>();
    private static final List<TextMarker> availableColors = TextMarker.availableColors();

    private RadioPreviewPanel() {
        init();
    }

    public static RadioPreviewPanel getInstance() {
        if (instance == null) {
            synchronized (RadioPreviewPanel.class) {
                if (instance == null) {
                    instance = new RadioPreviewPanel();
                }
            }
        }
        return instance;
    }

    private void init() {
        if (inited) {
            return;
        }
        inited = true;
        frame.setIconImages(MinimizeTrayConsole.IMAGE_LIST);
        frame.setContentPane($$$getRootComponent$$$());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(frame.getOwner());
        frame.pack();

        initCustomStyle();
        registerColorButton();
        colorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        textPane.setEditable(true);
        resultPane.setText("");
        resultPane.setEditable(false);
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                renderPreviewPanel();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                renderPreviewPanel();
            }
        });

        cancelButton.addActionListener(e -> {
            frame.setVisible(false);
            callback.handle(null);
        });

        saveButton.addActionListener(e -> {
            frame.setVisible(false);
            callback.handle(textPane.getText().replace("\n", ""));
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                callback.handle(null);
            }
        });
    }


    public void configureRadio(String radio, RadioCallback callback) {
        textPane.setText(radio);
        RadioPreviewPanel.callback = callback;
        renderPreviewPanel();
        frame.setVisible(true);
    }

    private void initCustomStyle() {
        setSkin(saveButton);
        setSkin(cancelButton);
        setSkin(textPane);
        setSkin(textPanel);
        setSkin(colorPanel);
        setSkin(mainPanel);
        setSkin(buttonPane);
        setSkin(resultPane);
        setSkin(resultPanel);
    }

    private void putTextMarker(TextMarker marker) {
        StringBuilder builder = new StringBuilder(textPane.getText());
        int position = textPane.getCaretPosition();
        builder.insert(position, marker.getHumanCode());
        textPane.setText(builder.toString());
        renderPreviewPanel();
    }

    private void renderPreviewPanel() {
        StyledDocument document = resultPane.getStyledDocument();
        try {
            document.remove(0, document.getLength());
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try clear Document Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
        if (ATTRIBUTE_MAP.isEmpty()) {
            availableColors.forEach(marker -> ATTRIBUTE_MAP.put(marker, new SimpleAttributeSet() {{
                addAttribute(StyleConstants.ColorConstants.Foreground, marker.getColor());
                addAttribute(StyleConstants.ColorConstants.Bold, true);
            }}));
        }
        String text = textPane.getText().replace("\n", "");
        if (text.contains(";")) {
            String[] split = text.split(";");
            for (String s : split) {
                List<String> list = Arrays.stream(s.split("#")).filter(str -> !str.isEmpty()).toList();
                renderPreviewPanelBlock(list);

                try {
                    document.insertString(document.getLength(), "\r\n===============OR===============\r\n", ATTRIBUTE_MAP.get(TextMarker.White));
                } catch (Exception e) {
                    ConsoleManager.getConsole().printError("Try insert Code Block Throw Exception!");
                }
            }
        } else {
            List<String> list = Arrays.stream(text.split("#")).filter(str -> !str.isEmpty()).toList();
            renderPreviewPanelBlock(list);
        }

//        if (!dataText.contains("#")) {
//            previusTextmarker = TextMarker.??????;
//            try {
//                document.insertString(document.getLength(), dataText, ATTRIBUTE_MAP.get(TextMarker.??????));
//            } catch (Exception ignored) {
//            }
//            return;
//        }

    }

    private void renderPreviewPanelBlock(List<String> renderList) {
        StyledDocument document = resultPane.getStyledDocument();
        List<String> stringList = new ArrayList<>();

        for (String s : renderList) {
            boolean isColor = false;
            for (TextMarker marker : TextMarker.values()) {
                String humanCode = marker.getHumanCode().replace("#", "");
                if (s.equals(humanCode)) {
                    isColor = true;
                    stringList.add(marker.getHumanCode());
                    break;
                }
            }

            if (!isColor) {
                stringList.add(s);
            }
        }

        try {
            TextMarker previusTextmarker = TextMarker.LightBlue;
            String configName = ConfigLoader.getConfigObject().getPreviousName();
            String name = configName.equals("NULL") ? "YourName" : configName;
            document.insertString(document.getLength(), name + " @ ", ATTRIBUTE_MAP.get(previusTextmarker));
            document.insertString(document.getLength(), "????????????????????? ", ATTRIBUTE_MAP.get(TextMarker.LowGreen));
            document.insertString(document.getLength(), "(?????????) ??? ", ATTRIBUTE_MAP.get(previusTextmarker));
            for (String s : stringList) {
                TextMarker humanCode = TextMarker.getAsHumanCode(s);
                if (humanCode != null) {
                    if (humanCode == TextMarker.Wrap) {
                        document.insertString(document.getLength(), "\n", ATTRIBUTE_MAP.get(TextMarker.White));
                        continue;
                    }
                    if (humanCode == TextMarker.Random) {
                        previusTextmarker = TextMarker.getRandomColor();
//                        document.insertString(document.getLength(), "", ATTRIBUTE_MAP.get(TextMarker.??????));
                        continue;
                    }
                    if (humanCode == TextMarker.Playername) {
                        String names = ConfigLoader.getConfigObject().getPreviousName();
                        if (names != null) {
                            document.insertString(document.getLength(), names, ATTRIBUTE_MAP.get(previusTextmarker));
                        } else {
                            document.insertString(document.getLength(), "[??????ID]", ATTRIBUTE_MAP.get(previusTextmarker));
                        }
                        continue;
                    }
                    previusTextmarker = humanCode;
                    continue;
                }
                document.insertString(document.getLength(), s, ATTRIBUTE_MAP.get(previusTextmarker));
            }
        } catch (Exception e) {
            ConsoleManager.getConsole().printError("Try render First RadioPanel Throw Exception!");
            ConsoleManager.getConsole().printException(e);
        }
    }

    private void registerColorButton() {
        List<TextMarker> availableColors = TextMarker.availableColors();
        Dimension colorSize = new Dimension(30, 30);
        Dimension textSize = new Dimension(70, 30);
        for (TextMarker color : availableColors) {
            addTextMarkerButton(color, null, null, colorSize);
        }
        addTextMarkerButton(TextMarker.Random, "??????", BACKGROUND_COLOR, textSize);
        addTextMarkerButton(TextMarker.Wrap, "??????", BACKGROUND_COLOR, textSize);
        addTextMarkerButton(TextMarker.Playername, "??????", BACKGROUND_COLOR, textSize);
    }

    private void addTextMarkerButton(TextMarker color, String text, Color showColor, Dimension size) {
        JButton button = new JButton();
        if (size == null) {
            size = new Dimension(30, 30);
        }
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setText("");
        if (text != null && text.length() > 0) {
            button.setText(text);
        }

        setSkin(button);
        if (showColor != null) {
            button.setBackground(showColor);
        } else {
            button.setBackground(color.getColor());
        }

        button.addActionListener(e -> {
            System.out.println("Clicked Color button: " + color.getHumanCode());
            putTextMarker(color);
        });

        colorPanel.add(button);
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
        mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(900, 400));
        mainPanel.setPreferredSize(new Dimension(900, 400));
        colorPanel = new JPanel();
        colorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        mainPanel.add(colorPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonPane = new JPanel();
        buttonPane.setLayout(new GridLayoutManager(1, 3, new Insets(2, 2, 2, 2), -1, -1));
        mainPanel.add(buttonPane, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        buttonPane.add(saveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        buttonPane.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        buttonPane.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textPanel = new JScrollPane();
        mainPanel.add(textPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textPane = new JTextPane();
        Font textPaneFont = this.$$$getFont$$$("Sans Serif Collection", Font.PLAIN, 12, textPane.getFont());
        if (textPaneFont != null) textPane.setFont(textPaneFont);
        textPane.setText("CustomRadio Code");
        textPanel.setViewportView(textPane);
        resultPanel = new JScrollPane();
        mainPanel.add(resultPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        resultPane = new JTextPane();
        Font resultPaneFont = this.$$$getFont$$$("Sans Serif Collection", Font.PLAIN, 12, resultPane.getFont());
        if (resultPaneFont != null) resultPane.setFont(resultPaneFont);
        resultPane.setText("ResultPane");
        resultPanel.setViewportView(resultPane);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    public interface RadioCallback {
        void handle(String radio);
    }
}
