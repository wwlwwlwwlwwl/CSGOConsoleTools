package cn.wwl.radio.console.impl.gui;

import cn.wwl.radio.console.ConsoleManager;
import cn.wwl.radio.executor.FunctionExecutor;
import cn.wwl.radio.file.ConfigLoader;
import cn.wwl.radio.file.ConfigObject;
import cn.wwl.radio.file.RadioFileManager;
import cn.wwl.radio.file.SteamUtils;
import cn.wwl.radio.utils.TimerUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cn.wwl.radio.console.impl.gui.ManagerPanel.*;

public class SettingsPanel {
    private JPanel mainPanel;
    private JComboBox<String> typeBox;
    private JButton doneButton;
    private JPanel bottomPanel;
    private JButton saveButton;
    private JPanel fieldPanel;
    private JLabel notificationText;
    private JLabel tipLabel;

    private static final JFrame frame = new JFrame("SettingsPanel");
    private static final JList<String> moduleList = new JList<>();
    private static final JList<String> radioList = new JList<>();
    private static boolean installOnce;

    private static final List<Field> strClass = new ArrayList<>();
    private static final List<Field> intClass = new ArrayList<>();
    private static final List<Field> boolClass = new ArrayList<>();

    private static final JPanel CONFIG_PANEL = new JPanel();
    private static final JPanel MODULE_PANEL = new JPanel();
    private static final JPanel RADIO_PANEL = new JPanel();

    private static final JPanel MODULE_DETAIL_PANEL = new JPanel();
    private static final JPanel RADIO_DETAIL_PANEL = new JPanel();

    private static JComboBox<String> radioGroupSelect = new JComboBox<>();


    public static final List<String> DEFAULT_RADIO = List.of(
            "roger",
            "negative",
            "cheer",
            "holdpos",
            "followme",
            "thanks",
            "go",
            "fallback",
            "sticktog",
            "holdpos",
            "followme",
            "compliment",
            "thanks",
            "enemyspot",
            "needbackup",
            "takepoint",
            "sectorclear",
            "inposition",
            "needrop",
            "go_a",
            "go_b"
    );

    public SettingsPanel(ManagerPanel parent) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    parent.closeSettingsPanel();
                }
            }
        });

        frame.setIconImages(MinimizeTrayConsole.IMAGE_LIST);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(frame.getOwner());

        initCustomStyle();

        typeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFieldBox(e.getItem().toString());
            }
        });

        doneButton.addActionListener(e -> {
            saveConfig(false);
            parent.closeSettingsPanel();
        });

        saveButton.addActionListener(e -> saveConfig(true));
    }

    public void init() {
        if (installOnce) {
            return;
        }
        installOnce = true;
        initAllPanel();
    }

    private void initAllPanel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Config");
        model.addElement("Modules");
        model.addElement("Radio");
        typeBox.setModel(model);

        CONFIG_PANEL.setEnabled(true);
        CONFIG_PANEL.setFocusCycleRoot(false);
        CONFIG_PANEL.setInheritsPopupMenu(false);
        CONFIG_PANEL.setVisible(false);
        CONFIG_PANEL.setLayout(VERTICAL_LAYOUT);
        mainPanel.add(CONFIG_PANEL, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        initConfigValues();

        MODULE_PANEL.setEnabled(true);
        MODULE_PANEL.setFocusCycleRoot(false);
        MODULE_PANEL.setInheritsPopupMenu(false);
        MODULE_PANEL.setVisible(false);
        MODULE_PANEL.setLayout(DEFAULT_LAYOUT);
        mainPanel.add(MODULE_PANEL, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        initModulesConfig();

        MODULE_DETAIL_PANEL.setEnabled(true);
        MODULE_DETAIL_PANEL.setFocusCycleRoot(false);
        MODULE_DETAIL_PANEL.setInheritsPopupMenu(false);
        MODULE_DETAIL_PANEL.setVisible(true);
        MODULE_DETAIL_PANEL.setLayout(VERTICAL_LAYOUT);
        MODULE_PANEL.add(MODULE_DETAIL_PANEL, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        RADIO_PANEL.setEnabled(true);
        RADIO_PANEL.setFocusCycleRoot(false);
        RADIO_PANEL.setInheritsPopupMenu(false);
        RADIO_PANEL.setVisible(false);
        RADIO_PANEL.setLayout(DEFAULT_LAYOUT);
        mainPanel.add(RADIO_PANEL, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        initRadioSelectionPanel();

        RADIO_DETAIL_PANEL.setEnabled(true);
        RADIO_DETAIL_PANEL.setFocusCycleRoot(false);
        RADIO_DETAIL_PANEL.setInheritsPopupMenu(false);
        RADIO_DETAIL_PANEL.setVisible(true);
        RADIO_DETAIL_PANEL.setLayout(VERTICAL_LAYOUT);
        RADIO_PANEL.add(RADIO_DETAIL_PANEL, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    public void displayNotification(String s) {
        String text = notificationText.getText();
        notificationText.setText(s);
        TimerUtils.callMeLater(1500, () -> notificationText.setText(text));
    }

    public void notificationSave() {
        saveButton.setBackground(NOTIFICATION_COLOR);
        TimerUtils.callMeLater(500, () -> saveButton.setBackground(BACKGROUND_COLOR));
        TimerUtils.callMeLater(1000, () -> saveButton.setBackground(NOTIFICATION_COLOR));
        TimerUtils.callMeLater(1500, () -> saveButton.setBackground(BACKGROUND_COLOR));
        TimerUtils.callMeLater(2000, () -> saveButton.setBackground(NOTIFICATION_COLOR));
        TimerUtils.callMeLater(2500, () -> saveButton.setBackground(BACKGROUND_COLOR));
    }

    private void saveConfig(boolean disableSaveButton) {
        ConsoleManager.getConsole().printToConsole("Save user Config in Settings Menu.");
        String selectedItem = (String) typeBox.getSelectedItem();
        if (selectedItem == null || selectedItem.length() == 0) {
            return;
        }

        switch (selectedItem) {
            case "Config", "Modules" -> ConfigLoader.writeConfigObject();
            case "Radio" -> RadioFileManager.getInstance().saveRadioConfig();
        }
        notificationText.setText("Config Saved.");
        if (disableSaveButton) {
            saveButton.setText("Saved!!!");
            saveButton.setBackground(ManagerPanel.NOTIFICATION_COLOR);
            saveButton.setEnabled(false);
        }
        TimerUtils.callMeLater(1500, new TimerTask() {
            @Override
            public void run() {
                if (disableSaveButton) {
                    saveButton.setText("Save");
                    saveButton.setBackground(ManagerPanel.BACKGROUND_COLOR);
                    saveButton.setEnabled(true);
                }
                notificationText.setText("Ready.");
            }
        });
    }

    public JFrame getFrame() {
        return frame;
    }

    private void updateFieldBox(String val) {
        fieldPanel.setVisible(false);
        CONFIG_PANEL.setVisible(false);
        MODULE_PANEL.setVisible(false);
        RADIO_PANEL.setVisible(false);

        switch (val) {
            case "Modules" -> MODULE_PANEL.setVisible(true);
            case "Config" -> CONFIG_PANEL.setVisible(true);
            case "Radio" -> {
                if (RADIO_PANEL.getComponentCount() == 1) {
                    JOptionPane.showMessageDialog(frame, "Radio Manager Require Locate the Game to Continue.\nPlease Start Game and Reopen Application Again.");
                }
                RADIO_PANEL.setVisible(true);
            }
        }
    }

    private void initRadioSelectionPanel() {
        if (SteamUtils.getCsgoPath() == null) {
            return;
        }

        JPanel btnPanel = new JPanel();

        btnPanel.setEnabled(true);
        btnPanel.setFocusCycleRoot(false);
        btnPanel.setInheritsPopupMenu(false);
        btnPanel.setVisible(true);
        btnPanel.setLayout(ManagerPanel.VERTICAL_LAYOUT);
        setSkin(btnPanel);

        Dimension size = new Dimension(150, 350);
        updateRadioListData(RadioFileManager.RadioGroup.COMMON);

        radioList.setForeground(ManagerPanel.NOTIFICATION_COLOR);
        radioList.setBackground(ManagerPanel.FOREGROUND_COLOR);
        radioList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = radioList.getSelectedValue();
                String selectedItem = (String) radioGroupSelect.getSelectedItem();
                if (selectedValue == null) {
                    return;
                }
                renderRadioData(RadioFileManager.getInstance().getRadioObjectByName(RadioFileManager.RadioGroup.getByName(selectedItem), selectedValue));
            }
        });
        JScrollPane scrollPane = new JScrollPane(radioList);
        scrollPane.setForeground(ManagerPanel.CONSOLE_MESSAGE_COLOR);
        scrollPane.setBackground(ManagerPanel.CONSOLE_MESSAGE_COLOR);
        scrollPane.setPreferredSize(size);
        scrollPane.setVisible(true);
        btnPanel.add(scrollPane);
        JButton addButton = new JButton();
        JButton delButton = new JButton();
        JButton setTitleButton = new JButton();
        setSkin(addButton);
        setSkin(delButton);
        setSkin(setTitleButton);
        setSkin(radioGroupSelect);
        radioGroupSelect.setModel(new DefaultComboBoxModel<>() {{
            for (RadioFileManager.RadioGroup value : RadioFileManager.RadioGroup.values()) {
                addElement(value.getGroupName());
            }
        }});

        radioGroupSelect.addItemListener(e -> {
            Object item = e.getItem();
            if (item instanceof String selectedValue) {
                updateRadioListData(RadioFileManager.RadioGroup.getByName(selectedValue));
            }
        });
        addButton.setText("Add Radio");
        delButton.setText("Remove Radio");
        setTitleButton.setText("Set List Title");
        addButton.addActionListener(e -> {
            RadioFileManager.RadioGroup group = RadioFileManager.RadioGroup.getByName((String) radioGroupSelect.getSelectedItem());
            Map<String, RadioFileManager.RadioObject> groupList = RadioFileManager.getInstance().getObjectsByGroup(group);
            if (groupList.size() >= 9) {
                displayNotification("Radio Count Must Less than 9!");
                return;
            }

            String randomName = "NEW RADIO-" + new Random().nextInt(100000);
            RadioFileManager.RadioObject object = new RadioFileManager.RadioObject();
            object.setCmd("roger").setHotkey(String.valueOf(groupList.size() + 1)).setLabel(randomName);
            RadioFileManager.getInstance().putRadioCommand(group, "Radio0" + (groupList.size() + 1), object);
            updateRadioListData(group);
        });

        delButton.addActionListener(e -> {
            String selectedItem = radioList.getSelectedValue();
            if (selectedItem == null) {
                return;
            }
            RadioFileManager.RadioGroup group = RadioFileManager.RadioGroup.getByName((String) radioGroupSelect.getSelectedItem());
//            System.out.println("Group: " + group + ", Name: " + selectedItem);
            RadioFileManager.RadioObject radioObject = RadioFileManager.getInstance().getRadioObjectByName(group, selectedItem);
//            System.out.println("Delete Object: " + radioObject);
            RadioFileManager.getInstance().removeObject(radioObject);
            updateRadioListData(group);
        });

        setTitleButton.addActionListener(e -> {
            String selectedItem = (String) radioGroupSelect.getSelectedItem();
            if (selectedItem == null || selectedItem.length() == 0) {
                return;
            }
            RadioFileManager.RadioGroup group = RadioFileManager.RadioGroup.getByName(selectedItem);
            String inputDialog = JOptionPane.showInputDialog(frame, "Set the Radio List title: ", RadioFileManager.getInstance().getGroupTitle(group));
            if (inputDialog == null || inputDialog.length() == 0) {
                return;
            }
            RadioFileManager.getInstance().updateGroupTitle(group, inputDialog);
            RadioFileManager.getInstance().saveRadioConfig();
        });

        btnPanel.add(radioGroupSelect);
        btnPanel.add(setTitleButton);
        btnPanel.add(addButton);
        btnPanel.add(delButton);
        RADIO_PANEL.add(btnPanel);
//        RADIO_PANEL.add(btnPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    private void updateRadioListData(RadioFileManager.RadioGroup group) {
        DefaultListModel<String> model = new DefaultListModel<>();
        Map<String, RadioFileManager.RadioObject> map = RadioFileManager.getInstance().getObjectsByGroup(group);
        map.forEach((s, o) -> model.addElement(s));

        radioList.setModel(model);
        radioList.repaint();
    }

    private void renderRadioData(RadioFileManager.RadioObject radioObject) {
        RADIO_DETAIL_PANEL.removeAll();
        RADIO_DETAIL_PANEL.repaint();

        JLabel keyLabel = new JLabel();
        JLabel titleLabel = new JLabel();
        JLabel cmdLabel = new JLabel();
        JTextField labelField = new JTextField();
        JComboBox<String> cmdField = new JComboBox<>();
        Dimension dimension = new Dimension(150, 30);

        setSkin(keyLabel);
        setSkin(titleLabel);
        setSkin(cmdLabel);
        setSkin(labelField);
        setSkin(cmdField);

        keyLabel.setPreferredSize(dimension);
        keyLabel.setMinimumSize(dimension);
        titleLabel.setPreferredSize(dimension);
        titleLabel.setMinimumSize(dimension);
        cmdLabel.setPreferredSize(dimension);
        cmdLabel.setMinimumSize(dimension);
        labelField.setPreferredSize(dimension);
        labelField.setMinimumSize(dimension);
        cmdField.setPreferredSize(dimension);
        cmdField.setMinimumSize(dimension);

        keyLabel.setText("HotKey: " + radioObject.getHotkey());
        titleLabel.setText("Label: ");
        cmdLabel.setText("cmd: ");
        labelField.setText(radioObject.getLabel());
        cmdField.setModel(getCMDComboBoxModel());
        String cmd = radioObject.getCmd();
        String moduleName = RadioFileManager.getInstance().getModuleNameByAlias(cmd);
        if (moduleName != null) {
            cmd = moduleName;
        }
        cmdField.setSelectedItem(cmd);

        cmdField.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String item = (String) e.getItem();
//                System.out.println("Item: " + item);
                String aliasName = RadioFileManager.getInstance().getAliasByModuleName(item);
//                System.out.println("Alias: " + aliasName);
                if (aliasName != null) {
                    radioObject.setCmd(aliasName);
                } else {
                    radioObject.setCmd(item);
                }
                RadioFileManager.getInstance().updateObject(radioObject);
            }
        });

        labelField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String text = labelField.getText();
                radioObject.setLabel(text);

                RadioFileManager.RadioGroup group = RadioFileManager.RadioGroup.getByName((String) radioGroupSelect.getSelectedItem());
                RadioFileManager.getInstance().updateObject(radioObject);
                updateRadioListData(group);
            }
        });
        RADIO_DETAIL_PANEL.add(keyLabel);
        RADIO_DETAIL_PANEL.add(titleLabel);
        RADIO_DETAIL_PANEL.add(labelField);
        RADIO_DETAIL_PANEL.add(cmdLabel);
        RADIO_DETAIL_PANEL.add(cmdField);

        RADIO_DETAIL_PANEL.revalidate();
    }

    private ComboBoxModel<String> getCMDComboBoxModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (ConfigObject.ModuleObject moduleObject : ConfigLoader.getConfigObject().getModuleList()) {
            if (moduleObject.getFunction().equals("CustomRadio")) {
                model.addElement(moduleObject.getName());
            }
        }

        DEFAULT_RADIO.forEach(model::addElement);
        return model;
    }

    private void initModulesConfig() {
        JPanel btnPanel = new JPanel();
        btnPanel.setEnabled(true);
        btnPanel.setFocusCycleRoot(false);
        btnPanel.setInheritsPopupMenu(false);
        btnPanel.setVisible(true);
        btnPanel.setLayout(ManagerPanel.VERTICAL_LAYOUT);
        setSkin(btnPanel);
        MODULE_PANEL.add(btnPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        moduleList.setForeground(ManagerPanel.NOTIFICATION_COLOR);
        moduleList.setBackground(ManagerPanel.FOREGROUND_COLOR);
        Dimension size = new Dimension(150, 400);
//        list.setPreferredSize(size);
        updateModuleListData();

        moduleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = moduleList.getSelectedValue();
                renderModuleData(selectedValue);
            }
        });
        JScrollPane scrollPane = new JScrollPane(moduleList);
        scrollPane.setForeground(ManagerPanel.CONSOLE_MESSAGE_COLOR);
        scrollPane.setBackground(ManagerPanel.CONSOLE_MESSAGE_COLOR);
        scrollPane.setPreferredSize(size);
        btnPanel.add(scrollPane);
        JButton addButton = new JButton();
        JButton delButton = new JButton();
        setSkin(addButton);
        setSkin(delButton);
        addButton.setText("Create Module");
        delButton.setText("Remove Module");
        addButton.addActionListener(e -> {
            ConfigObject.ModuleObject moduleObject = ConfigObject.ModuleObject.create();
            ConfigLoader.getConfigObject().getModuleList().add(moduleObject);
            displayNotification("Created New module.");
            updateModuleListData();
        });

        delButton.addActionListener(e -> {
            String selectedValue = moduleList.getSelectedValue();
            ConfigObject.ModuleObject module = findModule(selectedValue);
            ConfigLoader.getConfigObject().getModuleList().remove(module);
            displayNotification("Module " + module.getName() + " Deleted.");
            saveConfig(false);
            MODULE_DETAIL_PANEL.removeAll();
            MODULE_DETAIL_PANEL.repaint();
        });

        btnPanel.add(addButton);
        btnPanel.add(delButton);
    }

    private void updateModuleListData() {
        ConfigObject configObject = ConfigLoader.getConfigObject();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 0; i < configObject.getModuleList().size(); i++) {
            listModel.add(i, configObject.getModuleList().get(i).getName());
        }

        moduleList.setModel(listModel);
        moduleList.repaint();
    }

    private ConfigObject.ModuleObject findModule(String name) {
        ConfigObject.ModuleObject targetModule = null;
        List<ConfigObject.ModuleObject> moduleList = ConfigLoader.getConfigObject().getModuleList();
        for (ConfigObject.ModuleObject moduleObject : moduleList) {
            if (moduleObject.getName().equals(name)) {
                targetModule = moduleObject;
                break;
            }
        }
        return targetModule;
    }

    private void renderModuleData(String moduleName) {
        ConfigObject.ModuleObject targetModule = findModule(moduleName);
        if (targetModule == null) {
            return;
        }

        MODULE_DETAIL_PANEL.removeAll();
        MODULE_DETAIL_PANEL.repaint();
        renderModuleDetails(MODULE_DETAIL_PANEL, targetModule);
        MODULE_DETAIL_PANEL.revalidate();
    }

    private void renderModuleDetails(JPanel panel, ConfigObject.ModuleObject moduleObject) {
        JCheckBox enable = new JCheckBox();
        enable.setText("Enable");
        enable.setSelected(moduleObject.isEnabled());
        setSkin(enable);
        enable.addActionListener(e -> moduleObject.setEnabled(enable.isSelected()));
        panel.add(enable);

        List<Field> strFields = Arrays.stream(moduleObject.getClass().getDeclaredFields())
                .filter(f -> String.class.equals(f.getAnnotatedType().getType()))
                .toList();
        for (Field strField : strFields) {
            if (strField.getName().equals("function")) {
                try {
                    strField.setAccessible(true);
                    JLabel label = new JLabel();
                    JComboBox<String> comboBox = new JComboBox<>();
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    label.setVerticalAlignment(SwingConstants.TOP);
                    label.setText(splitCaseStr(strField.getName()) + ": ");
                    setSkin(label);
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    FunctionExecutor.getFunctions().forEach((s, func) -> model.addElement(s));
                    comboBox.setModel(model);
                    comboBox.setSelectedItem(strField.get(moduleObject));
                    setSkin(comboBox);
                    Dimension dimension = new Dimension(150, 30);
                    comboBox.setMinimumSize(dimension);
                    comboBox.setPreferredSize(dimension);
                    comboBox.addItemListener(e -> {
                        if (!(e.getItem() instanceof String text)) {
                            return;
                        }
                        try {
                            strField.set(moduleObject, text);
                        } catch (Exception ex) {
                            ConsoleManager.getConsole().printError("Try set " + strField.getName() + " Value to [" + text + "] failed!");
                            ex.printStackTrace();
                        }
                    });
                    panel.add(label);
                    panel.add(comboBox);
                } catch (Exception e) {
                    ConsoleManager.getConsole().printError("Try put Module " + moduleObject.getName() + " Value Parameter SelectBox Throw Exception!");
                    ConsoleManager.getConsole().printException(e);
                }
                continue;
            }

            try {
                strField.setAccessible(true);
                JLabel label = new JLabel();
                JTextField textField = new JTextField();
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setText(splitCaseStr(strField.getName()) + ": ");
                setSkin(label);
                textField.setHorizontalAlignment(SwingConstants.LEFT);
                textField.setText((String) strField.get(moduleObject));
                setSkin(textField);
                Dimension dimension = new Dimension(150, 30);
                textField.setMinimumSize(dimension);
                textField.setPreferredSize(dimension);

                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        String text = textField.getText();
                        try {
                            strField.set(moduleObject, text);
                        } catch (Exception ex) {
                            ConsoleManager.getConsole().printError("Try set " + strField.getName() + " Value to [" + text + "] failed!");
                            ex.printStackTrace();
                        }
                    }
                });
                panel.add(label);
                panel.add(textField);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try render Module " + moduleObject.getName() + " Value " + strField.getName() + " Throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        }

        AtomicReference<String> p = new AtomicReference<>(generationStringParameter(moduleObject));
        JLabel plabel = new JLabel();
        plabel.setHorizontalAlignment(SwingConstants.LEFT);
        plabel.setVerticalAlignment(SwingConstants.TOP);
        plabel.setText("Parameter: " + (
                p.get().length() == 0 ?
                        "None" :
                        (p.get().length() > 50 ? p.get().substring(0, 50) + "..." : p)
        ));
        setSkin(plabel);
        panel.add(plabel);

        JButton parameterSettingButton = new JButton();
        parameterSettingButton.setEnabled(true);
        setSkin(parameterSettingButton);
        Dimension size = new Dimension(150, 30);
        parameterSettingButton.setPreferredSize(size);
        parameterSettingButton.setMinimumSize(size);
        parameterSettingButton.setVisible(true);
        parameterSettingButton.setText("Edit Parameter");
        parameterSettingButton.addActionListener(e -> {
            if (moduleObject.getFunction().equals("CustomRadio")) {
                RadioPreviewPanel previewPanel = RadioPreviewPanel.getInstance();
                frame.setEnabled(false);
                List<String> parameter = moduleObject.getParameter();
                StringBuilder result = new StringBuilder();
                if (parameter.size() == 1) {
                    result.append(parameter.get(0));
                } else if (parameter.size() > 1) {
                    for (int i = 0; i < parameter.size(); i++) {
                        String str = parameter.get(i);
                        if (i != 0) {
                            result.append(str).append(";");
                        }
                    }
                    String substring = result.substring(0, result.length() - 1);
                    result = new StringBuilder(substring);
                }

                System.out.println("Debug: " + result);
                previewPanel.configureRadio(result.toString(), (str) -> {
                    frame.setEnabled(true);
                    if (str == null) {
                        return;
                    }
                    List<String> list = new ArrayList<>();
                    if (!str.contains(";")) {
                        switch (parameter.size()) {
                            case 0, 1 -> list.add(str);
                            default -> {
                                list.add(parameter.get(0));
                                list.add(str);
                            }
                        }
                    } else {
                        String[] split = str.split(";");
                        switch (parameter.size()) {
                            case 0, 1 -> list.addAll(Arrays.asList(split));
                            default -> {
                                list.add(parameter.get(0));
                                list.addAll(Arrays.asList(split));
                            }
                        }
                    }

                    moduleObject.setParameter(list);
                    p.set(generationStringParameter(moduleObject));
                    plabel.setText("Parameter: " + (
                            p.get().length() == 0 ?
                                    "None" :
                                    (p.get().length() > 50 ? p.get().substring(0, 50) + "..." : p)
                    ));
                    displayNotification("Module Parameter Updated.");
                    notificationSave();
                });
                return;
            }

            String str = (String) JOptionPane.showInputDialog(null, "Enter the Parameter Here, Use [;] To split lines.", "Parameter Setting", JOptionPane.PLAIN_MESSAGE, null, null, p);
            if (str == null) {
                displayNotification("Canceled.");
            } else {
                if (str.contains(";")) {
                    moduleObject.setParameter(List.of(str.split(";")));
                } else if (str.length() == 0) {
                    moduleObject.setParameter(List.of());
                } else {
                    moduleObject.setParameter(List.of(str));
                }
                p.set(generationStringParameter(moduleObject));
                plabel.setText("Parameter: " + (
                        p.get().length() == 0 ?
                                "None" :
                                (p.get().length() > 50 ? p.get().substring(0, 50) + "..." : p)
                ));
                notificationSave();
//                plabel.repaint();
            }
        });
        panel.add(parameterSettingButton);
    }

    private String generationStringParameter(ConfigObject.ModuleObject object) {
        return object.getParameter().toString().replace("[", "").replace("]", "").replace(", ", ";");
    }

    private void initConfigValues() {
        ConfigObject configObject = ConfigLoader.getConfigObject();
        if (strClass.isEmpty() || intClass.isEmpty() || boolClass.isEmpty()) {
            List<Field> fields = Arrays.stream(ConfigObject.class.getDeclaredFields())
                    .filter(field -> !field.getAnnotatedType().getType().getTypeName().contains("List"))
                    .filter(field -> !field.getAnnotatedType().getType().getTypeName().contains("Map"))
                    .toList();
            for (Field field : fields) {
                Class<?> type = (Class<?>) field.getAnnotatedType().getType();
                field.setAccessible(true);
                if (String.class.equals(type)) {
                    strClass.add(field);
                } else if (boolean.class.equals(type)) {
                    boolClass.add(field);
                } else if (int.class.equals(type)) {
                    intClass.add(field);
                } else {
                    ConsoleManager.getConsole().printError("Unknown Config field Type: " + field.getName() + ", CLASS type: " + type);
                }
            }
        }

        boolClass.forEach(f -> {
            try {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setHorizontalAlignment(SwingConstants.LEFT);
                checkBox.setVerticalAlignment(SwingConstants.TOP);
                setSkin(checkBox);
                checkBox.setText(splitCaseStr(f.getName()));
                checkBox.setSelected(f.getBoolean(configObject));
                checkBox.addActionListener(e -> {
                    boolean selected = checkBox.isSelected();
                    try {
                        f.set(configObject, selected);
                    } catch (Exception ex) {
                        ConsoleManager.getConsole().printError("Try set " + f.getName() + " Value to [" + selected + "] failed!");
                        ex.printStackTrace();
                    }
                });
                CONFIG_PANEL.add(checkBox);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try draw Checkbox for: " + f.getName() + " Throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        });

        strClass.forEach(f -> {
            try {
                JLabel label = new JLabel();
                JTextField textField = new JTextField();
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setText(splitCaseStr(f.getName()) + ": ");
                setSkin(label);
                textField.setHorizontalAlignment(SwingConstants.LEFT);
                textField.setText((String) f.get(configObject));
                textField.setEnabled(true);
                setSkin(textField);
                Dimension dimension = new Dimension(100, 25);
                textField.setMinimumSize(dimension);
                textField.setPreferredSize(dimension);

                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        String text = textField.getText();
                        try {
                            f.set(configObject, text);
                            updateModuleListData();
                        } catch (Exception ex) {
                            ConsoleManager.getConsole().printError("Try set " + f.getName() + " Value to [" + text + "] failed!");
                            ex.printStackTrace();
                        }
                    }
                });
                CONFIG_PANEL.add(label);
                CONFIG_PANEL.add(textField);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try draw StringBox for: " + f.getName() + " Throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        });

        intClass.forEach(f -> {
            try {
                JLabel label = new JLabel();
                JSpinner spinner = new JSpinner();
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setText(splitCaseStr(f.getName()) + ": ");
                setSkin(label);
                spinner.setValue(f.get(configObject));
                spinner.setEnabled(true);
                setSkin(spinner);
                Dimension dimension = new Dimension(100, 25);
                spinner.setMinimumSize(dimension);
                spinner.setPreferredSize(dimension);

                spinner.addChangeListener(e -> {
                    int value = (Integer) spinner.getValue();
                    try {
                        f.set(configObject, value);
                    } catch (Exception ex) {
                        ConsoleManager.getConsole().printError("Try set " + f.getName() + " Value to [" + value + "] failed!");
                        ex.printStackTrace();
                    }
                });

                CONFIG_PANEL.add(label);
                CONFIG_PANEL.add(spinner);
            } catch (Exception e) {
                ConsoleManager.getConsole().printError("Try draw IntegerBox for: " + f.getName() + " Throw Exception!");
                ConsoleManager.getConsole().printException(e);
            }
        });
        frame.validate();
    }

    private String splitCaseStr(String s) {
        if (s.contains("API")) {
            return "API Token";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            Character ch = s.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(ch));
                continue;
            }

            if (Character.isUpperCase(ch)) {
                result.append(" ").append(ch);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private void initCustomStyle() {
        setSkin(mainPanel);
        setSkin(typeBox);
        setSkin(doneButton);
        setSkin(bottomPanel);
        setSkin(saveButton);
        setSkin(fieldPanel);
        setSkin(CONFIG_PANEL);
        setSkin(MODULE_PANEL);
        setSkin(MODULE_DETAIL_PANEL);
        setSkin(notificationText);
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
        mainPanel.setMinimumSize(new Dimension(1000, 600));
        mainPanel.setPreferredSize(new Dimension(1000, 600));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 4, new Insets(5, 2, 5, 5), -1, -1, true, false));
        mainPanel.add(bottomPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, true));
        typeBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        typeBox.setModel(defaultComboBoxModel1);
        bottomPanel.add(typeBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        doneButton = new JButton();
        doneButton.setText("Done");
        bottomPanel.add(doneButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        bottomPanel.add(saveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        notificationText = new JLabel();
        Font notificationTextFont = UIManager.getFont("TextArea.font");
        if (notificationTextFont != null) notificationText.setFont(notificationTextFont);
        notificationText.setText("Ready.");
        bottomPanel.add(notificationText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        fieldPanel.setEnabled(true);
        fieldPanel.setFocusCycleRoot(false);
        fieldPanel.setInheritsPopupMenu(false);
        fieldPanel.setVisible(true);
        mainPanel.add(fieldPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tipLabel = new JLabel();
        Font tipLabelFont = UIManager.getFont("TextField.font");
        if (tipLabelFont != null) tipLabel.setFont(tipLabelFont);
        tipLabel.setForeground(new Color(-4241085));
        tipLabel.setText("↙Switch the ComboBox to Show Config↙");
        fieldPanel.add(tipLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
