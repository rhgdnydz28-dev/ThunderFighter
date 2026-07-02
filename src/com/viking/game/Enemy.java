package com.viking.game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Enemy extends GameObject {

    public static final int TYPE_SMALL = 1;
    public static final int TYPE_MEDIUM = 2;
    public static final int TYPE_LARGE = 3;
    public static int speedBonus = 0;

    public int enemyType;
    public int hp;
    public int score;
    private int shootTimer = 0;
    private int shootInterval;
    private static final Random random = new Random();

    public Enemy(int enemyType, int x, int y) {
        super(x, y, 40, 36, null, 2);
        this.enemyType = enemyType;
        initByType();
        image = GameObject.loadImage("images/enemy" + enemyType + "/enemyPlane" + enemyType + "_1.png");
    }

    private void initByType() {
        switch (enemyType) {
            case TYPE_SMALL:
                setData(1, 4, 10, 95, 48, 38);
                break;
            case TYPE_MEDIUM:
                setData(2, 5, 20, 80, 58, 50);
                break;
            case TYPE_LARGE:
                setData(3, 5, 30, 50, 78, 64);
                break;
        }
    }

    private void setData(int hp, int speed, int score, int shootInterval, int width, int height) {
        this.hp = hp;
        this.speed = speed;
        this.score = score;
        this.shootInterval = shootInterval;
        this.shootTimer = random.nextInt(shootInterval);
        this.width = width;
        this.height = height;
    }

    public void hit(int damage) {
        hp -= damage;
        if (hp <= 0) {
            alive = false;
        }
    }

    public boolean shouldShoot() {
        if (++shootTimer >= shootInterval) {
            shootTimer = 0;
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        y += Math.max(1, speed + speedBonus);
        x = Math.max(0, Math.min(x, 400 - width));
        if (y > 820) {
            alive = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            return;
        }
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
            return;
        }
        g.setColor(enemyType == TYPE_MEDIUM ? Color.ORANGE : enemyType == TYPE_LARGE ? Color.MAGENTA : Color.RED);
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
    }
}
