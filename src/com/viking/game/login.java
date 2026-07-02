package com.viking.game;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * 登录和注册窗口。
 */
public class login extends JDialog {

    private final sign userStore;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private String loginUser;

    private login(sign userStore) {
        this.userStore = userStore;
        setTitle("用户登录");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildContent();
        pack();
        setLocationRelativeTo(null);
    }

    public static String showLogin(sign userStore) {
        login dialog = new login(userStore);
        dialog.setVisible(true);
        return dialog.loginUser;
    }

    private void buildContent() {
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("用户名"));
        form.add(usernameField);
        form.add(new JLabel("密码"));
        form.add(passwordField);

        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");
        JButton exitButton = new JButton("退出");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        exitButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(loginButton);
        buttons.add(registerButton);
        buttons.add(exitButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 12, 16));
        panel.add(new JLabel("请先登录账号后进入游戏"), BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        setContentPane(panel);

        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    private void login() {
        String username = getUsername();
        String password = getPassword();
        if (userStore.authenticate(username, password)) {
            loginUser = username;
            dispose();
            return;
        }
        JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.WARNING_MESSAGE);
    }

    private void register() {
        String username = getUsername();
        String password = getPassword();
        if (!userStore.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "用户名需为3-16位字母、数字或下划线", "注册失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!userStore.isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "密码长度需为4-20位", "注册失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userStore.exists(username)) {
            JOptionPane.showMessageDialog(this, "该用户名已存在", "注册失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userStore.register(username, password)) {
            JOptionPane.showMessageDialog(this, "注册成功，请点击登录进入游戏", "注册成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String getUsername() {
        return usernameField.getText().trim();
    }

    private String getPassword() {
        return new String(passwordField.getPassword());
    }
}
