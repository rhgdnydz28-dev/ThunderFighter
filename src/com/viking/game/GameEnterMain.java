package com.viking.game;

import javax.swing.SwingUtilities;

public class GameEnterMain {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("雷霆战机");
        System.out.println("=================================");

        SwingUtilities.invokeLater(() -> {
            sign userStore = new sign();
            String username = login.showLogin(userStore);
            if (username == null) {
                System.exit(0);
            }
            new GameFrame(username, userStore);
        });
    }
}
