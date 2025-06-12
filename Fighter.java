package com.mephi.b23902.kts;

public class Fighter {
    String name;
    int level;
    int maxHealth;
    int health;
    int damage;
    int experience;
    int score;
    int items;
    boolean isWeakened;
    int weakenTurns;
    boolean isPlayer;

    public Fighter(String name, int level, int health, int damage, boolean isPlayer) {
        this.name = name;
        this.level = level;
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.experience = 0;
        this.score = 0;
        this.items = 0;
        this.isWeakened = false;
        this.weakenTurns = 0;
        this.isPlayer = isPlayer;
    }
}