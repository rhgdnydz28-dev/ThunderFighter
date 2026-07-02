package com.viking.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Boss extends GameObject {

    private static final int MOVE_PERIOD = 120;
    private static final int MOVE_AMPLITUDE = 70;
    private static final int HP_BAR_WIDTH = 200;

    public int hp;
    public int maxHp;
    public int score;

    private final int bossType;
    private final int targetY = 80;
    private boolean inPosition = false;
    private int moveTimer = 0;
    private int shootTimer = 0;
    private int shootInterval = 30;
    private int spreadCount = 0;

    public Boss(int bossType) {
        super(140, -100, 100, 80, null, 2);
        this.bossType = bossType;
        initByType();
        image = GameObject.loadImage("images/enemyboss/boss1.png");
    }

    public Boss(int bossType, int hp) {
        this(bossType);
        this.hp = hp;
        this.maxHp = hp;
        this.score = hp * 10;
    }

    private void initByType() {
        if (bossType == 2) {
            setData(30, 300, 20, 120, 100);
        } else {
            setData(20, 200, 25, 110, 90);
        }
    }

    private void setData(int hp, int score, int shootInterval, int width, int height) {
        this.hp = hp;
        this.maxHp = hp;
        this.score = score;
        this.shootInterval = shootInterval;
        this.width = width;
        this.height = height;
    }

    public void hit(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public boolean shouldShoot() {
        if (!inPosition) {
            return false;
        }
        if (++shootTimer >= shootInterval) {
            shootTimer = 0;
            spreadCount++;
            return true;
        }
        return false;
    }

    public int[] getBulletOffsets() {
        switch (spreadCount % 3) {
            case 1:
                return new int[]{-20, 0, 20};
            case 2:
                return new int[]{-30, -15, 0, 15, 30};
            default:
                return new int[]{0};
        }
    }

    @Override
    public void update() {
        if (!inPosition) {
            y++;
            if (y >= targetY) {
                y = targetY;
                inPosition = true;
            }
            return;
        }
        moveTimer++;
        x = (int) (140 + Math.sin((double) moveTimer / MOVE_PERIOD * 2 * Math.PI) * MOVE_AMPLITUDE);
        x = Math.max(0, Math.min(x, 400 - width));
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            return;
        }
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            drawFallbackBoss(g);
        }
        drawHpBar(g);
    }

    private void drawFallbackBoss(Graphics g) {
        g.setColor(new Color(180, 20, 20));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(255, 100, 100));
        g.drawRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("BOSS", x + 15, y + height / 2 + 7);
    }

    private void drawHpBar(Graphics g) {
        int barHeight = 14;
        int barX = 400 / 2 - HP_BAR_WIDTH / 2;
        int barY = Math.max(5, y - 28);

        drawCenteredText(g, "BOSS [" + (bossType == 1 ? "STAGE I" : "STAGE II") + "]", barY - 4, 13);
        drawBarFrame(g, barX, barY, barHeight);

        String hpText = hp + " / " + maxHp;
        drawCenteredText(g, hpText, barY + barHeight - 3, 10);

        int hpWidth = (int) ((double) hp / maxHp * HP_BAR_WIDTH);
        if (hpWidth > 0) {
            drawHpFill(g, barX, barY, hpWidth, barHeight);
        }
        g.setColor(new Color(200, 200, 200, 150));
        g.drawRect(barX, barY, HP_BAR_WIDTH, barHeight);
    }

    private void drawCenteredText(Graphics g, String text, int y, int size) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.drawString(text, 400 / 2 - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    private void drawBarFrame(Graphics g, int barX, int barY, int barHeight) {
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX - 2, barY - 2, HP_BAR_WIDTH + 4, barHeight + 4);
        g.setColor(new Color(150, 150, 150));
        g.drawRect(barX - 2, barY - 2, HP_BAR_WIDTH + 4, barHeight + 4);
        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, HP_BAR_WIDTH, barHeight);
    }

    private void drawHpFill(Graphics g, int barX, int barY, int hpWidth, int barHeight) {
        if (hp > maxHp / 3) {
            g.setColor(new Color(0, 220, 60));
            g.fillRect(barX, barY, hpWidth, barHeight);
            g.setColor(new Color(120, 255, 120, 120));
        } else if (hp > maxHp / 6) {
            g.setColor(new Color(255, 200, 30));
            g.fillRect(barX, barY, hpWidth, barHeight);
            g.setColor(new Color(255, 240, 120, 120));
        } else {
            g.setColor(new Color(240, 30, 30));
            g.fillRect(barX, barY, hpWidth, barHeight);
            if (System.currentTimeMillis() / 300 % 2 != 0) {
                return;
            }
            g.setColor(new Color(255, 100, 100, 100));
        }
        g.fillRect(barX, barY, hpWidth, barHeight / 2);
    }
}
