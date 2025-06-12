package com.mephi.b23902.kts;

import java.util.ArrayList;
import java.util.Random;

public class GameLogic {
    private ArrayList<Fighter> enemies;
    private ArrayList<Fighter> currentEnemies;
    private ArrayList<ScoreRecord> leaderboard;
    private Fighter player;
    private Fighter currentEnemy;
    private Random random;
    private int locations;
    private int currentLocation;
    private boolean isBossFight;
    private GameGUI gui;
    private ExcelHandler excelHandler;
    private int initialPlayerHealth;

    public GameLogic(GameGUI gui) {
        this.gui = gui;
        random = new Random();
        enemies = new ArrayList<>();
        currentEnemies = new ArrayList<>();
        leaderboard = new ArrayList<>();
        excelHandler = new ExcelHandler();
        initializeEnemies();
        loadLeaderboard();
    }

    private void initializeEnemies() {
        enemies.add(new Fighter("Goblin", 1, 30,5, false));
        enemies.add(new Fighter("Mage", 1, 20,7, false));
    }

    public void startNewGame(int locations) {
        this.locations = locations;
        this.currentLocation = 1;
        this.player = new Fighter("Player", 0, 100, 10, true);
        this.currentEnemies.clear();
        generateEnemies();
        this.isBossFight = false;
        this.currentEnemy = currentEnemies.get(0);
        initialPlayerHealth = player.health;
        gui.updateDisplay();
    }

    private void generateEnemies() {
        currentEnemies.clear();
        int enemyCount = random.nextInt(5) + 1; // 1-5 врагов
        for (int i = 0; i < enemyCount; i++) {
            int index = random.nextInt(2); // Goblin или Mage
            currentEnemies.add(new Fighter(enemies.get(index).name, player.level + 1,
                    enemies.get(index).maxHealth + player.level * 10,
                    enemies.get(index).damage + player.level * 2, false));
        }
        // Добавляем босса в конец локации
        currentEnemies.add(new Fighter("Boss", player.level + 2, 150 + player.level * 20, 15 + player.level * 3, false));
    }

    public void playerAction(String action) {
        if (currentEnemy == null || currentEnemy.health <= 0) return;
        String enemyAction = getEnemyAction();
        String result = "";
        int initialHealth = player.health;

        // Обработка ослабления
        if (currentEnemy.isWeakened) {
            currentEnemy.weakenTurns--;
            if (currentEnemy.weakenTurns <= 0) {
                currentEnemy.isWeakened = false;
            }
        }

        // Босс регенерирует здоровье
        if (isBossFight && random.nextInt(100) < 20) { // 20% шанс
            if (action.equals("Defend")) {
                int damageTaken = currentEnemy.maxHealth - currentEnemy.health;
                currentEnemy.health += damageTaken / 2;
                result = "Босс восстановил 50% урона!";
            } else if (action.equals("Attack")) {
                int damage = player.damage * 2;
                currentEnemy.health -= damage;
                result = "Регенерация прервана! Босс получил двойной урон: " + damage;
            }
        } else {
            if (action.equals("Attack")) {
                if (enemyAction.equals("Defend")) {
                    result = "Противник защищается, атака не удалась!";
                } else if (enemyAction.equals("Attack")) {
                    int playerDamage = (int) (player.damage * (currentEnemy.isWeakened ? 1.25 : 1));
                    int enemyDamage = (int) (currentEnemy.damage * (player.isWeakened ? 1.25 : 1));
                    currentEnemy.health -= playerDamage;
                    player.health -= enemyDamage;
                    result = "Обоюдный удар! Игрок нанес: " + playerDamage + ", получил: " + enemyDamage;
                } else if (enemyAction.equals("Weaken")) {
                    currentEnemy.damage = (int) (currentEnemy.damage * 1.15);
                    result = "Противник попытался ослабить, но получил +15% урона!";
                }
            } else if (action.equals("Defend")) {
                if (enemyAction.equals("Attack")) {
                    result = "Игрок защищается, атака врага не удалась!";
                } else if (enemyAction.equals("Weaken")) {
                    if (random.nextInt(100) < 75) {
                        player.isWeakened = true;
                        player.weakenTurns = player.level + 1;
                        player.damage = (int) (player.damage * 0.5);
                        result = "Игрок ослаблен на " + player.weakenTurns + " ходов!";
                    } else {
                        result = "Ослабление не удалось!";
                    }
                }
            } else if (action.equals("Weaken")) {
                if (enemyAction.equals("Defend")) {
                    if (random.nextInt(100) < 75) {
                        currentEnemy.isWeakened = true;
                        currentEnemy.weakenTurns = player.level + 1;
                        currentEnemy.damage = (int) (currentEnemy.damage * 0.5);
                        result = "Противник ослаблен на " + currentEnemy.weakenTurns + " ходов!";
                    } else {
                        result = "Ослабление не удалось!";
                    }
                } else if (enemyAction.equals("Attack")) {
                    player.damage = (int) (player.damage * 1.15);
                    result = "Игрок получил +15% урона!";
                }
            } else if (action.equals("UseItem")) {
                if (player.items > 0) {
                    player.health += 30;
                    if (player.health > player.maxHealth) player.health = player.maxHealth;
                    player.items--;
                    player.score -= 10; // Штраф за использование предмета
                    result = "Игрок восстановил 30 здоровья! (-10 очков)";
                } else {
                    result = "Нет предметов для использования!";
                }
            }
        }

        // Проверка результатов раунда
        if (currentEnemy.health <= 0) {
            int expGain = isBossFight ? 50 : 20;
            int scoreGain = isBossFight ? 100 + currentEnemy.level * 20 : 20 + currentEnemy.level * 10;
            if (player.health == initialHealth) scoreGain += 50; // Бонус за бой без урона
            player.experience += expGain;
            player.score += scoreGain;
            if (random.nextInt(100) < 30) player.items++;
            result += "\nПротивник побежден! + " + expGain + " опыта, + " + scoreGain + " очков.";
            currentEnemies.remove(currentEnemy);
            if (currentEnemies.isEmpty()) {
                player.score += 200; // Бонус за завершение локации
                result += "\nЛокация завершена! +200 очков.";
                if (currentLocation == locations) {
                    endGame();
                } else {
                    currentLocation++;
                    generateEnemies();
                    isBossFight = false;
                    currentEnemy = currentEnemies.get(0);
                }
            } else {
                currentEnemy = currentEnemies.get(0);
                isBossFight = currentEnemy.name.equals("Boss");
            }
            checkLevelUp();
        } else if (player.health <= 0) {
            endGame();
        }

        gui.updateDisplay();
        gui.showResult(result);
    }

    private String getEnemyAction() {
        if (currentEnemy.name.equals("Mage")) {
            int choice = random.nextInt(3);
            return choice == 0 ? "Attack" : choice == 1 ? "Defend" : "Weaken";
        } else if (isBossFight) {
            return random.nextInt(2) == 0 ? "Attack" : "Defend";
        } else {
            return random.nextInt(2) == 0 ? "Attack" : "Defend";
        }
    }

    private void checkLevelUp() {
        int expNeeded = (player.level + 1) * 30;
        if (player.experience >= expNeeded) {
            player.level++;
            gui.showLevelUpDialog();
        }
    }

    private void endGame() {
        boolean inTop10 = leaderboard.size() < 10 || leaderboard.stream().anyMatch(record -> player.score > record.score);
        if (inTop10) {
            gui.showNameInputDialog();
        } else {
            gui.showEndGameDialog("Игра окончена! Ваш счет: " + player.score);
        }
    }

    public void upgradeStat(String stat) {
        if (stat.equals("Health")) {
            player.maxHealth += 20;
            player.health += 20;
        } else if (stat.equals("Damage")) {
            player.damage += 5;
        }
        for (Fighter enemy : enemies) {
            enemy.maxHealth += 5;
            enemy.damage += 1;
        }
    }

    private void loadLeaderboard() {
        leaderboard = excelHandler.loadLeaderboard();
    }

    public void addScore(String name) {
        leaderboard.add(new ScoreRecord(name, player.score));
        leaderboard.sort((a, b) -> b.score - a.score);
        if (leaderboard.size() > 10) leaderboard.remove(10);
        excelHandler.saveLeaderboard(leaderboard);
        gui.showEndGameDialog("Игра окончена! Ваш счет: " + player.score);
    }

    public Fighter getPlayer() { return player; }
    public ArrayList<Fighter> getCurrentEnemies() { return currentEnemies; }
    public Fighter getCurrentEnemy() { return currentEnemy; }
    public ArrayList<ScoreRecord> getLeaderboard() { return leaderboard; }
    public int getCurrentLocation() { return currentLocation; }
    public int getTotalLocations() { return locations; }
}