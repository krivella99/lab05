package com.mephi.b23902.kts;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameGUI extends JFrame {
    private GameLogic gameLogic;
    private JLabel playerHealthLabel, playerStatsLabel;
    private JProgressBar playerHealthBar;
    private JPanel enemyPanel;
    private JTextArea resultArea;
    private JLabel locationLabel;
    private ArrayList<JLabel> enemyHealthLabels;
    private ArrayList<JProgressBar> enemyHealthBars;
    private ArrayList<JLabel> enemyStatsLabels;

    public GameGUI() {
        gameLogic = new GameLogic(this);
        enemyHealthLabels = new ArrayList<>();
        enemyHealthBars = new ArrayList<>();
        enemyStatsLabels = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Мини-игра");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель меню
        JPanel menuPanel = new JPanel();
        JButton startButton = new JButton("Новая игра");
        JButton leaderboardButton = new JButton("Таблица рекордов");
        menuPanel.add(startButton);
        menuPanel.add(leaderboardButton);
        add(menuPanel, BorderLayout.NORTH);

        // Панель игрока (слева)
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder("Игрок"));
        playerHealthLabel = new JLabel("Здоровье: 100/100");
        playerHealthBar = new JProgressBar(0, 100);
        playerHealthBar.setValue(100);
        playerHealthBar.setPreferredSize(new Dimension(150, 20)); // Уменьшаем высоту полосы
        playerStatsLabel = new JLabel("Уровень: 0, Урон: 10, Предметы: 0");
        playerPanel.add(playerHealthLabel, BorderLayout.NORTH);
        playerPanel.add(playerHealthBar, BorderLayout.CENTER);
        playerPanel.add(playerStatsLabel, BorderLayout.SOUTH);
        add(playerPanel, BorderLayout.WEST);

        // Панель врагов (справа)
        enemyPanel = new JPanel();
        enemyPanel.setLayout(new BoxLayout(enemyPanel, BoxLayout.Y_AXIS));
        enemyPanel.setBorder(BorderFactory.createTitledBorder("Противники"));
        add(enemyPanel, BorderLayout.EAST);

        // Центральная панель (действия и результаты)
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel actionPanel = new JPanel();
        JButton attackButton = new JButton("Атака");
        JButton defendButton = new JButton("Защита");
        JButton weakenButton = new JButton("Ослабление");
        JButton itemButton = new JButton("Использовать предмет");
        actionPanel.add(attackButton);
        actionPanel.add(defendButton);
        actionPanel.add(weakenButton);
        actionPanel.add(itemButton);
        centerPanel.add(actionPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        centerPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Панель локации
        JPanel locationPanel = new JPanel();
        locationLabel = new JLabel("Локация: 0/0");
        locationPanel.add(locationLabel);
        add(locationPanel, BorderLayout.SOUTH);

        // Обработчики событий
        startButton.addActionListener(e -> startNewGame());
        leaderboardButton.addActionListener(e -> showLeaderboard());
        attackButton.addActionListener(e -> gameLogic.playerAction("Attack"));
        defendButton.addActionListener(e -> gameLogic.playerAction("Defend"));
        weakenButton.addActionListener(e -> gameLogic.playerAction("Weaken"));
        itemButton.addActionListener(e -> gameLogic.playerAction("UseItem"));

        updateDisplay();
    }

    private void startNewGame() {
        String input = JOptionPane.showInputDialog(this, "Введите количество локаций (1-5):");
        try {
            int locations = Integer.parseInt(input);
            if (locations >= 1 && locations <= 5) {
                gameLogic.startNewGame(locations);
                updateDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Введите число от 1 до 5.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректное число.");
        }
    }

    private void showLeaderboard() {
        StringBuilder sb = new StringBuilder("Таблица рекордов:\n");
        for (ScoreRecord record : gameLogic.getLeaderboard()) {
            sb.append(record.name).append(": ").append(record.score).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    public void updateDisplay() {
        Fighter player = gameLogic.getPlayer();
        if (player != null) {
            playerHealthLabel.setText("Здоровье: " + player.health + "/" + player.maxHealth);
            playerHealthBar.setMaximum(player.maxHealth);
            playerHealthBar.setValue(player.health);
            playerStatsLabel.setText(String.format("Уровень: %d, Урон: %d, Предметы: %d, Статус: %s",
                    player.level, player.damage, player.items,
                    player.isWeakened ? "Ослаблен (" + player.weakenTurns + " ходов)" : "Нормальный"));
        }

        enemyPanel.removeAll();
        enemyHealthLabels.clear();
        enemyHealthBars.clear();
        enemyStatsLabels.clear();
        ArrayList<Fighter> enemies = gameLogic.getCurrentEnemies();
        Fighter currentEnemy = gameLogic.getCurrentEnemy();
        for (Fighter enemy : enemies) {
            JPanel enemySubPanel = new JPanel(new BorderLayout());
            if (enemy == currentEnemy) {
                enemySubPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            }
            JLabel healthLabel = new JLabel(enemy.name + ": " + enemy.health + "/" + enemy.maxHealth);
            JProgressBar healthBar = new JProgressBar(0, enemy.maxHealth);
            healthBar.setValue(enemy.health);
            healthBar.setPreferredSize(new Dimension(150, 20)); // Уменьшаем высоту полосы
            JLabel statsLabel = new JLabel(String.format("Уровень: %d, Урон: %d, Статус: %s",
                    enemy.level, enemy.damage,
                    enemy.isWeakened ? "Ослаблен (" + enemy.weakenTurns + " ходов)" : "Нормальный"));
            enemySubPanel.add(healthLabel, BorderLayout.NORTH);
            enemySubPanel.add(healthBar, BorderLayout.CENTER);
            enemySubPanel.add(statsLabel, BorderLayout.SOUTH);
            enemyPanel.add(enemySubPanel);
            enemyHealthLabels.add(healthLabel);
            enemyHealthBars.add(healthBar);
            enemyStatsLabels.add(statsLabel);
        }
        enemyPanel.revalidate();
        enemyPanel.repaint();

        locationLabel.setText("Локация: " + gameLogic.getCurrentLocation() + "/" + gameLogic.getTotalLocations());
    }

    public void showResult(String result) {
        resultArea.append(result + "\n");
    }

    public void showLevelUpDialog() {
        Object[] options = {"Здоровье", "Урон"};
        int choice = JOptionPane.showOptionDialog(this,
                "Вы повысили уровень! Выберите улучшение:",
                "Повышение уровня",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        gameLogic.upgradeStat(choice == 0 ? "Health" : "Damage");
        updateDisplay();
    }

    public void showNameInputDialog() {
        String name = JOptionPane.showInputDialog(this, "Введите ваше имя для таблицы рекордов:");
        if (name != null && !name.trim().isEmpty()) {
            gameLogic.addScore(name);
        } else {
            gameLogic.addScore("Unknown");
        }
    }

    public void showEndGameDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
        updateDisplay();
    }
}