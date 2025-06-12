package com.mephi.b23902.kts;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameGUI game = new GameGUI();
            game.setVisible(true);
        });
    }
}
