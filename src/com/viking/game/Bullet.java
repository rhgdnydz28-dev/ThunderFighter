package com.viking.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Bullet extends GameObject {

    public static final int HERO_BULLET_WIDTH = 14;
    public static final int HERO_BULLET_HEIGHT = 30;
    public static final int ENEMY_BULLET_WIDTH = 16;
    public static final int ENEMY_BULLET_HEIGHT = 24;

    public int dx;
    public int dy;
    public boolean isHeroBullet;
    public int damage = 1;

    public Bullet(int x, int y, int dx, int dy, boolean isHeroBullet) {
        super(x, y, 8, 16, null, 8);
        this.dx = dx;
        this.dy = dy;
        this.isHeroBullet = isHeroBullet;
        loadImage();
    }

    private void loadImage() {
        Image bulletImg = GameObject.loadImage(isHeroBullet ? "images/bullet/heroBullet.png" : "images/bullet/enemyBullet.png");
        image = bulletImg;
        width = isHeroBullet ? HERO_BULLET_WIDTH : ENEMY_BULLET_WIDTH;
        height = isHeroBullet ? HERO_BULLET_HEIGHT : ENEMY_BULLET_HEIGHT;
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        if (y < -20 || y > 820 || x < -20 || x > 420) {
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
        } else {
            g.setColor(isHeroBullet ? Color.YELLOW : Color.RED);
            g.fillRect(x, y, width, height);
        }
    }
}
