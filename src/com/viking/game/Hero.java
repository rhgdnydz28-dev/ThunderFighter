package com.viking.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Hero extends GameObject {

    private static final int ANIM_RATE = 5;
    private static final int HIT_MARGIN = 8;
    public static final int INVINCIBLE_DURATION = 150;

    public Image[] heroFrames;
    public int heroIndex = 0;
    public int lives = 3;
    public int fireType = 1;
    public int bombCount = 1;
    public boolean isInvincible = false;
    public int invincibleTimer = 0;

    private int animCount = 0;
    private final int heroType;
    private final int skinId;

    public Hero(int heroType) {
        this(heroType, sign.ORIGINAL_SKIN);
    }

    public Hero(int heroType, int skinId) {
        super(168, 640, 64, 74, null, 5);
        this.heroType = heroType;
        this.skinId = skinId;
        loadFrames();
        image = heroFrames[0];
    }

    private void loadFrames() {
        heroFrames = new Image[2];
        if (skinId > sign.ORIGINAL_SKIN) {
            for (int i = 0; i < heroFrames.length; i++) {
                heroFrames[i] = GameObject.loadImageWithTransparentWhite(
                    "images/hero" + heroType + "/hero" + heroType + "_skin" + (i + 1) + ".png"
                );
            }
            if (heroFrames[0] != null && heroFrames[1] != null) {
                return;
            }
        }
        for (int i = 0; i < heroFrames.length; i++) {
            heroFrames[i] = GameObject.loadImage("images/hero" + heroType + "/hero" + heroType + "_" + (i + 1) + ".png");
        }
    }

    public void moveLeft() {
        x = Math.max(0, x - speed);
    }

    public void moveRight() {
        x = Math.min(400 - width, x + speed);
    }

    public void moveUp() {
        y = Math.max(0, y - speed);
    }

    public void moveDown() {
        y = Math.min(800 - height, y + speed);
    }

    public boolean useBomb() {
        if (bombCount <= 0) {
            return false;
        }
        bombCount--;
        return true;
    }

    public void hit() {
        if (!isInvincible) {
            lives--;
            alive = false;
        }
    }

    public void addBomb() {
        if (bombCount < 2) {
            bombCount++;
        }
    }

    public void upgradeFire() {
        fireType = 2;
    }

    public void addLife() {
        if (lives < 5) {
            lives++;
        }
    }

    @Override
    public void update() {
        if (++animCount >= ANIM_RATE) {
            animCount = 0;
            heroIndex = (heroIndex + 1) % heroFrames.length;
            image = heroFrames[heroIndex];
        }
        if (isInvincible && --invincibleTimer <= 0) {
            isInvincible = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!alive || (isInvincible && (invincibleTimer / 5) % 2 == 0)) {
            return;
        }
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
            return;
        }
        int[] xPoints = {x + width / 2, x, x + width};
        int[] yPoints = {y, y + height, y + height};
        g.setColor(Color.BLUE);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(Color.CYAN);
        g.drawPolygon(xPoints, yPoints, 3);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + HIT_MARGIN, y + HIT_MARGIN, width - HIT_MARGIN * 2, height - HIT_MARGIN * 2);
    }
}
