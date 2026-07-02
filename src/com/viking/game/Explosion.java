package com.viking.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Explosion extends GameObject {

    private static final int ANIM_RATE = 4;

    private Image[] blastFrames;
    private int frameIndex = 0;
    private int frameCount = 0;
    private int blastType;

    public Explosion(int x, int y, int blastType) {
        super(x, y, 64, 64, null, 0);
        this.blastType = blastType;
        loadFrames();
        if (blastFrames.length > 0) {
            image = blastFrames[0];
        }
    }

    private void loadFrames() {
        int count = blastType == 1 ? 5 : 6;
        String folder = blastType == 1 ? "blast1" : "blast2";
        String prefix = blastType == 1 ? "blast_0_" : "blast_2_";
        blastFrames = new Image[count];
        for (int i = 0; i < count; i++) {
            blastFrames[i] = GameObject.loadImage("images/" + folder + "/" + prefix + (i + 1) + ".png");
        }
        width = blastType == 1 ? 48 : 80;
        height = blastType == 1 ? 48 : 80;
    }

    @Override
    public void update() {
        if (++frameCount < ANIM_RATE) {
            return;
        }
        frameCount = 0;
        if (++frameIndex >= blastFrames.length) {
            alive = false;
        } else {
            image = blastFrames[frameIndex];
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            return;
        }
        int drawX = x - width / 2;
        int drawY = y - height / 2;
        if (image != null) {
            g.drawImage(image, drawX, drawY, width, height, null);
        } else {
            int r = width / 2;
            g.setColor(new Color(255, 100, 0, 180));
            g.fillOval(x - r, y - r, width, height);
            g.setColor(new Color(255, 200, 50, 200));
            g.fillOval(x - r / 2, y - r / 2, r, r);
        }
    }
}
