package com.viking.game;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 游戏主窗口
 * 雷霆战机游戏的顶层容器窗口
 */
public class GameFrame extends JFrame {

    /** 游戏面板（包含所有游戏逻辑和渲染） */
    private GamePanel gamePanel;

    private JMenuItem startItem;
    private JMenuItem pauseItem;
    private JMenuItem resumeItem;
    private JMenuItem restartItem;
    private JMenuItem customItem;
    private JMenuItem skinItem;
    private String username;
    private sign userStore;
    private static final int[] SKIN_COSTS = {0, 300};
    private static final String[] SKIN_NAMES = {"原始皮肤", "新皮肤"};

    public GameFrame(String username, sign userStore) {
        this.username = username;
        this.userStore = userStore;
        gamePanel = new GamePanel(username, userStore);

        this.setTitle("雷霆战机 - " + username);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setJMenuBar(createMenuBar());
        this.add(gamePanel, BorderLayout.CENTER);

        Image icon = GameObject.loadImage("images/hero1/hero1_1.png");
        if (icon != null) {
            this.setIconImage(icon);
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        new Timer(200, e -> refreshMenuState()).start();
        System.out.println("[雷霆战机] 游戏窗口初始化完成");
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        startItem = new JMenuItem("开始游戏(U)");
        pauseItem = new JMenuItem("暂停(I)");
        resumeItem = new JMenuItem("继续(O)");
        restartItem = new JMenuItem("重新开始");
        JMenuItem exitItem = new JMenuItem("退出游戏");

        startItem.addActionListener(e -> gamePanel.startNewGame());
        pauseItem.addActionListener(e -> gamePanel.pauseGame());
        resumeItem.addActionListener(e -> gamePanel.resumeGame());
        restartItem.addActionListener(e -> confirmRestart());
        exitItem.addActionListener(e -> confirmExit());

        gameMenu.add(startItem);
        gameMenu.add(pauseItem);
        gameMenu.add(resumeItem);
        customItem = new JMenuItem("自定义");
        customItem.addActionListener(e -> showCustomDialog());
        gameMenu.add(restartItem);
        gameMenu.add(customItem);
        skinItem = new JMenuItem("皮肤商城");
        skinItem.addActionListener(e -> showSkinDialog());
        gameMenu.add(skinItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        JMenu helpMenu = new JMenu("帮助");
        JMenuItem helpItem = new JMenuItem("操作说明");
        JMenuItem aboutItem = new JMenuItem("关于");
        helpItem.addActionListener(e -> showHelp());
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        bar.add(gameMenu);
        bar.add(helpMenu);
        return bar;
    }

    private void refreshMenuState() {
        int state = gamePanel.getGameState();
        boolean ready = state == GamePanel.STATE_MENU || state == GamePanel.STATE_GAMEOVER || state == GamePanel.STATE_WIN;
        startItem.setEnabled(ready);
        pauseItem.setEnabled(state == GamePanel.STATE_PLAYING);
        resumeItem.setEnabled(state == GamePanel.STATE_PAUSED);
        restartItem.setEnabled(state != GamePanel.STATE_MENU);
        customItem.setEnabled(ready);
        skinItem.setEnabled(ready && userStore != null);
    }

    private void confirmRestart() {
        int oldState = gamePanel.getGameState();
        if (oldState == GamePanel.STATE_PLAYING) {
            gamePanel.pauseGame();
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要重新开始吗？",
            "重新开始",
            JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            gamePanel.resetToReady();
        } else if (oldState == GamePanel.STATE_PLAYING) {
            gamePanel.resumeGame();
        }
    }

    private void confirmExit() {
        int oldState = gamePanel.getGameState();
        if (oldState == GamePanel.STATE_PLAYING) {
            gamePanel.pauseGame();
        }
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出游戏吗？",
            "退出游戏",
            JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else if (oldState == GamePanel.STATE_PLAYING) {
            gamePanel.resumeGame();
        }
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(
            this,
            "W/A/S/D 或方向键：移动英雄机\n"
                + "U：开始游戏\n"
                + "I：暂停游戏\n"
                + "O：继续游戏\n"
                + "H：使用核弹\n"
                + "Q：退出游戏",
            "操作说明",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
            this,
            "雷霆战机\n当前用户：" + username,
            "关于",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showSkinDialog() {
        if (userStore == null) {
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel pointsLabel = new JLabel("当前积分：" + userStore.getPoints(username), JLabel.CENTER);
        JPanel skinList = new JPanel(new GridLayout(0, 1, 8, 8));

        for (int skinId = sign.ORIGINAL_SKIN; skinId <= sign.MAX_SKIN_ID; skinId++) {
            skinList.add(createSkinRow(skinId, pointsLabel));
        }

        panel.add(pointsLabel, BorderLayout.NORTH);
        panel.add(skinList, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
            this,
            panel,
            "皮肤商城",
            JOptionPane.PLAIN_MESSAGE
        );
        gamePanel.requestFocusInWindow();
    }

    private JPanel createSkinRow(int skinId, JLabel pointsLabel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        JLabel preview = new JLabel();
        Image image = skinId == sign.ORIGINAL_SKIN
            ? GameObject.loadImage(getHeroPreviewPath(skinId))
            : GameObject.loadImageWithTransparentWhite(getHeroPreviewPath(skinId));
        if (image != null) {
            preview.setIcon(new ImageIcon(image.getScaledInstance(64, 74, Image.SCALE_SMOOTH)));
        }

        boolean unlocked = userStore.isSkinUnlocked(username, skinId);
        boolean equipped = gamePanel.getSelectedHeroSkin() == skinId;
        String status;
        if (equipped) {
            status = "已装备";
        } else if (unlocked) {
            status = "已解锁";
        } else {
            status = SKIN_COSTS[skinId] + "积分";
        }

        JLabel info = new JLabel(SKIN_NAMES[skinId] + "  " + status);
        JButton action = new JButton(unlocked ? "装备" : "解锁");
        action.setEnabled(!equipped && (unlocked || userStore.getPoints(username) >= SKIN_COSTS[skinId]));
        action.addActionListener(e -> {
            if (!userStore.isSkinUnlocked(username, skinId)
                && !userStore.unlockSkin(username, skinId, SKIN_COSTS[skinId])) {
                JOptionPane.showMessageDialog(this, "积分不足，继续战斗获取积分。", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (userStore.setSelectedSkin(username, skinId)) {
                gamePanel.setSelectedHeroSkin(skinId);
                pointsLabel.setText("当前积分：" + userStore.getPoints(username));
                JOptionPane.showMessageDialog(this, "皮肤已装备。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.add(action);
        row.add(preview, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(actionPanel, BorderLayout.EAST);
        return row;
    }

    private String getHeroPreviewPath(int skinId) {
        int heroType = gamePanel.getSelectedHeroType();
        if (skinId == sign.ORIGINAL_SKIN) {
            return "images/hero" + heroType + "/hero" + heroType + "_1.png";
        }
        return "images/hero" + heroType + "/hero" + heroType + "_skin" + skinId + ".png";
    }

    private void showCustomDialog() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        JSlider speedSlider = new JSlider(0, 5, gamePanel.getEnemySpeedBonus());
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setSnapToTicks(true);
        JLabel speedValue = new JLabel(String.valueOf(speedSlider.getValue()));
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                speedValue.setText(String.valueOf(speedSlider.getValue()));
            }
        });

        JComboBox<String> levelBox = new JComboBox<>(new String[]{"第一关", "第二关", "第三关"});
        levelBox.setSelectedIndex(gamePanel.getCustomStartLevel() - 1);
        JLabel levelValue = new JLabel((String) levelBox.getSelectedItem());
        levelBox.addActionListener(e -> levelValue.setText((String) levelBox.getSelectedItem()));

        JRadioButton colorSkin = new JRadioButton("经典彩色", gamePanel.getSelectedHeroType() == 1);
        JRadioButton greySkin = new JRadioButton("经典灰色", gamePanel.getSelectedHeroType() == 2);
        ButtonGroup skinGroup = new ButtonGroup();
        skinGroup.add(colorSkin);
        skinGroup.add(greySkin);

        JPanel speedPanel = new JPanel(new BorderLayout(6, 0));
        speedPanel.add(speedSlider, BorderLayout.CENTER);
        speedPanel.add(speedValue, BorderLayout.EAST);

        JPanel skinPanel = new JPanel(new GridLayout(1, 2));
        skinPanel.add(colorSkin);
        skinPanel.add(greySkin);

        form.add(new JLabel("敌机速度"));
        form.add(speedPanel);
        form.add(new JLabel("初始关卡"));
        JPanel levelPanel = new JPanel(new BorderLayout(6, 0));
        levelPanel.add(levelBox, BorderLayout.CENTER);
        levelPanel.add(levelValue, BorderLayout.EAST);
        form.add(levelPanel);
        form.add(new JLabel("皮肤套装"));
        form.add(skinPanel);

        JPanel previews = new JPanel(new GridLayout(1, 4, 8, 0));
        addPreview(previews, "英雄机", "images/hero1/hero1_1.png");
        addPreview(previews, "初级敌机", "images/enemy1/enemyPlane1_1.png");
        addPreview(previews, "高级敌机", "images/enemy3/enemyPlane3_1.png");
        addPreview(previews, "Boss", "images/enemyboss/boss1.png");

        panel.add(form, BorderLayout.NORTH);
        panel.add(previews, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "自定义",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            int heroType = colorSkin.isSelected() ? 1 : 2;
            gamePanel.applyCustomSettings(speedSlider.getValue(), levelBox.getSelectedIndex() + 1, heroType);
        }
    }

    private void addPreview(JPanel previews, String title, String path) {
        Image image = GameObject.loadImage(path);
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
        if (image != null) {
            label.setIcon(new ImageIcon(image.getScaledInstance(42, 42, Image.SCALE_SMOOTH)));
        }
        previews.add(label);
    }
}
