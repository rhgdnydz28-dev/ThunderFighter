package com.viking.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Award extends GameObject {

    public static final int SIZE = 42;
    public static final int TYPE_BOMB = 0;
    public static final int TYPE_DOUBLE_FIRE = 1;
    public static final int TYPE_LIFE = 2;

    private static final int ZIGZAG_SPEED_X = 5;
    private static final Image BOMB_IMAGE = GameObject.loadImage("images/award/atomBomb.png");
    private static final Image DOUBLE_FIRE_IMAGE = GameObject.loadImage("images/award/doubleFire.png");
    private static final Image LIFE_IMAGE = GameObject.loadImage("images/award/bee.png");

    public int awardType;
    private int zigzagDirection = 1;

    public Award(int x, int y, int awardType) {
        super(x, y, SIZE, SIZE, null, 3);
        this.awardType = awardType;
        this.image = getImageByType();
        if (x > GamePanel.GAME_WIDTH / 2) {
            zigzagDirection = -1;
        }
    }

    private Image getImageByType() {
        switch (awardType) {
            case TYPE_DOUBLE_FIRE:
                return DOUBLE_FIRE_IMAGE;
            case TYPE_LIFE:
                return LIFE_IMAGE;
            default:
                return BOMB_IMAGE;
        }
    }

    @Override
    public void update() {
        x += ZIGZAG_SPEED_X * zigzagDirection;
        if (x <= 0 || x >= GamePanel.GAME_WIDTH - width) {
            x = Math.max(0, Math.min(x, GamePanel.GAME_WIDTH - width));
            zigzagDirection *= -1;
        }
        y += speed;
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
        g.setColor(awardType == TYPE_DOUBLE_FIRE ? Color.CYAN : awardType == TYPE_LIFE ? Color.GREEN : Color.ORANGE);
        g.fillOval(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawOval(x, y, width, height);
    }
}
