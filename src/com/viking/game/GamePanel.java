package com.viking.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;
/**
 * 游戏主面板
 * 负责游戏主循环、对象增删改查、碰撞检测和状态切换。
 */
public class GamePanel extends JPanel implements KeyListener, MouseListener {
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_GAMEOVER = 3;
    public static final int STATE_WIN = 4;
    public static final int STATE_LEVEL_CLEAR = 5;

    public static final int GAME_WIDTH = 400;
    public static final int INFO_WIDTH = 170;
    public static final int SCREEN_WIDTH = GAME_WIDTH + INFO_WIDTH;
    public static final int SCREEN_HEIGHT = 800;
    private static final int AUTO_FIRE_INTERVAL = 15;
    private static final int MAX_ESCAPED_ENEMIES = 5;
    private static final int START_BUTTON_X = 60;
    private static final int START_BUTTON_Y = 210;
    private static final int START_BUTTON_W = 280;
    private static final int START_BUTTON_H = 100;
    private static final int PAUSE_BUTTON_X = 310;
    private static final int PAUSE_BUTTON_Y = 44;
    private static final int PAUSE_BUTTON_W = 80;
    private static final int PAUSE_BUTTON_H = 28;
    public int gameState = STATE_MENU;
    public int selectedHeroType = 1;
    private int selectedHeroSkin = sign.ORIGINAL_SKIN;
    public int score = 0;
    public int level = 1;
    private int customStartLevel = 1;
    private int enemySpeedBonus = 0;
    private int bgy = 0;
    private int bgSpeed = 2;
    private int autoFireTimer = 0;
    private int enemySpawnTimer = 0;
    private int enemySpawnInterval = 45;
    private int levelClearTimer = 0;
    private int nextLevel = 1;
    private boolean scoreStored = false;
    private final String username;
    private final sign userStore;
    private int remainingSmallEnemies = 20;
    private int remainingAdvancedEnemies = 0;
    private int escapedEnemies = 0;
    private boolean bossAppeared = false;
    private Image imageBg;
    private Image pauseImage;
    private Image gameOverImage;
    private Image smallEnemyImage;
    private Image advancedEnemyImage;
    private Image bossPreviewImage;
    private Image radarImage;
    public Hero hero;
    public List<Bullet> heroBullets = new ArrayList<>();
    public List<Bullet> enemyBullets = new ArrayList<>();
    public List<Enemy> enemies = new ArrayList<>();
    public List<Award> awards = new ArrayList<>();
    public List<Explosion> explosions = new ArrayList<>();
    public Boss boss;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private static final Random random = new Random();
    public GamePanel(String username, sign userStore) {
        this.username = username;
        this.userStore = userStore;
        if (username != null && userStore != null) {
            selectedHeroSkin = userStore.getSelectedSkin(username);
        }
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLayout(null);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        loadResources();
        resetHero();
        new Timer(16, e -> gameLoop()).start();
    }
    private void loadResources() {
        imageBg = GameObject.loadImage("images/background/background.jpg");
        pauseImage = GameObject.loadImage("images/controll/pause.png");
        gameOverImage = GameObject.loadImage("images/controll/gameover.png");
        smallEnemyImage = GameObject.loadImage("images/enemy1/enemyPlane1_1.png");
        advancedEnemyImage = GameObject.loadImage("images/enemy3/enemyPlane3_1.png");
        bossPreviewImage = GameObject.loadImage("images/enemyboss/boss1.png");
        radarImage = GameObject.loadImage("images/radar.gif");
    }
    private void gameLoop() {
        if (gameState == STATE_MENU || gameState == STATE_GAMEOVER || gameState == STATE_WIN) {
            updateBackground();
        } else if (gameState == STATE_PLAYING) {
            updatePlaying();
        } else if (gameState == STATE_LEVEL_CLEAR) {
            updateLevelClear();
        }
        repaint();
    }
    private void updateBackground() {
        bgy += Math.max(1, bgSpeed / 2);
        if (bgy >= SCREEN_HEIGHT) {
            bgy = 0;
        }
    }
    private void updatePlaying() {
        bgy += bgSpeed;
        if (bgy >= SCREEN_HEIGHT) {
            bgy = 0;
        }
        updateHero();
        updateHeroBullets();
        updateEnemies();
        updateEnemyBullets();
        updateBoss();
        updateAwards();
        updateExplosions();
        checkCollisions();
        spawnFlyingObject();
        checkBossSpawn();
        checkLevelState();
        cleanupDeadObjects();
    }
    private void updateLevelClear() {
        updateBackground();
        updateExplosions();
        levelClearTimer--;
        if (levelClearTimer <= 0) {
            level = nextLevel;
            setupLevel(level);
            gameState = STATE_PLAYING;
        }
    }
    private void updateHero() {
        if (!hero.alive) {
            return;
        }
        if (upPressed) {
            hero.moveUp();
        }
        if (downPressed) {
            hero.moveDown();
        }
        if (leftPressed) {
            hero.moveLeft();
        }
        if (rightPressed) {
            hero.moveRight();
        }

        autoFireTimer++;
        if (autoFireTimer >= AUTO_FIRE_INTERVAL) {
            autoFireTimer = 0;
            shootHeroBullet();
        }
        hero.update();
    }
    private void shootHeroBullet() {
        if (!hero.alive) {
            return;
        }
        int bulletY = hero.y - 5;
        if (hero.fireType == 1) {
            heroBullets.add(new Bullet(hero.x + hero.width / 2 - Bullet.HERO_BULLET_WIDTH / 2, bulletY, 0, -24, true));
        } else {
            heroBullets.add(new Bullet(hero.x + hero.width / 4 - Bullet.HERO_BULLET_WIDTH / 2, bulletY, 0, -24, true));
            heroBullets.add(new Bullet(hero.x + hero.width * 3 / 4 - Bullet.HERO_BULLET_WIDTH / 2, bulletY, 0, -24, true));
        }
    }
    private void updateHeroBullets() {
        for (Bullet bullet : heroBullets) {
            bullet.update();
        }
    }
    private void updateEnemyBullets() {
        for (Bullet bullet : enemyBullets) {
            bullet.update();
        }
    }
    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.update();
            if (enemy.alive && enemy.y > SCREEN_HEIGHT) {
                enemy.alive = false;
                escapedEnemies++;
            }
            if (enemy.alive && enemy.shouldShoot()) {
                int bulletX = enemy.x + enemy.width / 2 - Bullet.ENEMY_BULLET_WIDTH / 2;
                int bulletY = enemy.y + enemy.height;
                enemyBullets.add(new Bullet(bulletX, bulletY, 0, 5, false));
                if (enemy.enemyType != Enemy.TYPE_SMALL) {
                    enemyBullets.add(new Bullet(bulletX - 10, bulletY, -1, 5, false));
                    enemyBullets.add(new Bullet(bulletX + 10, bulletY, 1, 5, false));
                }
            }
        }
    }
    private void updateBoss() {
        if (boss == null || !boss.alive) {
            return;
        }
        boss.update();
        if (!boss.shouldShoot()) {
            return;
        }
        for (int offset : boss.getBulletOffsets()) {
            int bulletX = boss.x + boss.width / 2 + offset;
            int bulletY = boss.y + boss.height - 20;
            double angle = Math.atan2(hero.y + hero.height / 2.0 - bulletY, hero.x + hero.width / 2.0 - bulletX);
            enemyBullets.add(new Bullet(bulletX, bulletY, (int) (Math.cos(angle) * 4), (int) (Math.sin(angle) * 4), false));
        }
    }
    private void updateAwards() {
        for (Award award : awards) {
            award.update();
        }
    }
    private void updateExplosions() {
        for (Explosion explosion : explosions) {
            explosion.update();
        }
    }
    private void checkCollisions() {
        checkBulletVsBullet();
        checkHeroBullets();
        checkHeroDamage();
        checkAwards();
    }
    private void checkBulletVsBullet() {
        for (Bullet heroBullet : heroBullets) {
            if (!heroBullet.alive) {
                continue;
            }
            for (Bullet enemyBullet : enemyBullets) {
                if (enemyBullet.alive && heroBullet.intersects(enemyBullet)) {
                    heroBullet.alive = false;
                    enemyBullet.alive = false;
                    break;
                }
            }
        }
    }
    private void checkHeroBullets() {
        for (Bullet bullet : heroBullets) {
            if (!bullet.alive) {
                continue;
            }
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.intersects(enemy)) {
                    bullet.alive = false;
                    enemy.hit(bullet.damage);
                    if (!enemy.alive) {
                        onEnemyDestroyed(enemy);
                    }
                    break;
                }
            }
            if (bullet.alive && boss != null && boss.alive && bullet.intersects(boss)) {
                bullet.alive = false;
                boss.hit(1);
                if (!boss.alive) {
                    onBossDestroyed();
                }
            }
        }
    }
    private void checkHeroDamage() {
        if (!hero.alive) {
            return;
        }
        for (Bullet bullet : enemyBullets) {
            if (bullet.alive && bullet.intersects(hero)) {
                bullet.alive = false;
                hero.hit();
                onHeroDestroyed();
                return;
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.intersects(hero)) {
                enemy.alive = false;
                onEnemyDestroyed(enemy);
                hero.hit();
                onHeroDestroyed();
                return;
            }
        }
        if (boss != null && boss.alive && boss.intersects(hero)) {
            hero.hit();
            onHeroDestroyed();
        }
    }
    private void checkAwards() {
        if (!hero.alive) {
            return;
        }
        for (Award award : awards) {
            if (award.alive && award.intersects(hero)) {
                award.alive = false;
                if (award.awardType == Award.TYPE_BOMB) {
                    hero.addBomb();
                } else if (award.awardType == Award.TYPE_DOUBLE_FIRE) {
                    hero.upgradeFire();
                } else if (award.awardType == Award.TYPE_LIFE) {
                    hero.addLife();
                }
            }
        }
    }
    private void onEnemyDestroyed(Enemy enemy) {
        score += enemy.score;
        explosions.add(new Explosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, enemy.enemyType == Enemy.TYPE_SMALL ? 1 : 2));
        if (random.nextInt(100) < 25) {
            awards.add(new Award(enemy.x + enemy.width / 2 - Award.SIZE / 2, enemy.y + enemy.height / 2 - Award.SIZE / 2, random.nextInt(3)));
        }
    }
    private void onBossDestroyed() {
        score += boss.score;
        explosions.add(new Explosion(boss.x + 20, boss.y + 40, 2));
        explosions.add(new Explosion(boss.x + boss.width / 2, boss.y + 20, 2));
        explosions.add(new Explosion(boss.x + boss.width - 20, boss.y + 40, 2));
        boss = null;
    }
    private void onHeroDestroyed() {
        explosions.add(new Explosion(hero.x + hero.width / 2, hero.y + hero.height / 2, 2));
        if (hero.bombCount > 0) {
            hero.bombCount = 0;
            detonateBomb(true);
        }
        clearMovementKeys();
        enemyBullets.clear();
        if (hero.lives <= 0) {
            storeScorePoints();
            gameState = STATE_GAMEOVER;
            return;
        }
        new Timer(1200, e -> {
            respawnHero();
            ((Timer) e.getSource()).stop();
        }).start();
    }
    private void spawnFlyingObject() {
        if (bossAppeared) {
            return;
        }
        enemySpawnTimer++;
        if (enemySpawnTimer < enemySpawnInterval) {
            return;
        }
        enemySpawnTimer = 0;
        int flyingRoll = random.nextInt(100);
        if (flyingRoll < 70 && (remainingSmallEnemies + remainingAdvancedEnemies) > 0) {
            spawnEnemyByQuota();
        } else {
            spawnAwardFromTop();
        }
    }
    private void spawnEnemyByQuota() {
        int enemyType;
        if (remainingSmallEnemies > 0 && remainingAdvancedEnemies > 0) {
            enemyType = random.nextBoolean() ? Enemy.TYPE_SMALL : Enemy.TYPE_LARGE;
        } else if (remainingSmallEnemies > 0) {
            enemyType = Enemy.TYPE_SMALL;
        } else {
            enemyType = Enemy.TYPE_LARGE;
        }

        if (enemyType == Enemy.TYPE_SMALL) {
            remainingSmallEnemies--;
        } else {
            remainingAdvancedEnemies--;
        }
        int x = random.nextInt(GAME_WIDTH - 90);
        enemies.add(new Enemy(enemyType, x, -50));
    }
    private void spawnAwardFromTop() {
        int awardType;
        int roll = random.nextInt(30);
        if (roll < 10) {
            awardType = Award.TYPE_LIFE;
        } else if (roll < 20) {
            awardType = Award.TYPE_DOUBLE_FIRE;
        } else {
            awardType = Award.TYPE_BOMB;
        }
        awards.add(new Award(random.nextInt(GAME_WIDTH - Award.SIZE), -Award.SIZE, awardType));
    }
    private void checkBossSpawn() {
        if (!bossAppeared && remainingSmallEnemies == 0 && remainingAdvancedEnemies == 0) {
            bossAppeared = true;
            int hp = level == 1 ? 20 : (level == 2 ? 30 : 50);
            boss = new Boss(level == 1 ? 1 : 2, hp);
        }
    }
    private void checkLevelState() {
        if (escapedEnemies > MAX_ESCAPED_ENEMIES || hero.lives <= 0) {
            storeScorePoints();
            gameState = STATE_GAMEOVER;
            return;
        }
        boolean levelFinished = bossAppeared && boss == null && enemies.isEmpty() && hero.alive;
        if (!levelFinished) {
            return;
        }
        if (level >= 3) {
            storeScorePoints();
            gameState = STATE_WIN;
        } else {
            nextLevel = level + 1;
            levelClearTimer = 180;
            gameState = STATE_LEVEL_CLEAR;
        }
    }
    private void cleanupDeadObjects() {
        removeDead(heroBullets);
        removeDead(enemyBullets);
        removeDead(enemies);
        removeDead(awards);
        removeDead(explosions);
    }
    private void removeDead(List<? extends GameObject> objects) {
        Iterator<? extends GameObject> iterator = objects.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().alive) {
                iterator.remove();
            }
        }
    }
    private void useBomb() {
        if (!hero.alive || !hero.useBomb()) {
            return;
        }
        detonateBomb(true);
    }
    private void detonateBomb(boolean awardScore) {
        enemyBullets.clear();
        heroBullets.clear();
        for (Award award : awards) {
            award.alive = false;
        }
        for (Enemy enemy : enemies) {
            if (enemy.alive) {
                enemy.alive = false;
                if (awardScore) {
                    score += enemy.score;
                }
                explosions.add(new Explosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, 2));
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        if (gameState == STATE_MENU) {
            drawMenu(g);
        } else if (gameState == STATE_GAMEOVER) {
            drawGameOverOverlay(g);
        } else if (gameState == STATE_WIN) {
            drawWinOverlay(g);
        } else {
            drawPlaying(g);
            if (gameState == STATE_PAUSED) {
                drawPauseOverlay(g);
            } else if (gameState == STATE_LEVEL_CLEAR) {
                drawLevelClearOverlay(g);
            }
        }
        drawInfoPanel(g);
    }
    private void drawBackground(Graphics g) {
        if (gameState == STATE_MENU) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
            g.setColor(new Color(238, 238, 238));
            g.fillRect(GAME_WIDTH, 0, INFO_WIDTH, SCREEN_HEIGHT);
            return;
        }
        if (imageBg != null) {
            g.drawImage(imageBg, 0, bgy, GAME_WIDTH, SCREEN_HEIGHT, null);
            g.drawImage(imageBg, 0, bgy - SCREEN_HEIGHT, GAME_WIDTH, SCREEN_HEIGHT, null);
        } else {
            g.setColor(new Color(5, 5, 25));
            g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
        }
        g.setColor(new Color(10, 14, 24));
        g.fillRect(GAME_WIDTH, 0, INFO_WIDTH, SCREEN_HEIGHT);
    }
    private void drawMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("微软雅黑", Font.BOLD, 42));
        String title = "“飞机大战”";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int titleX = GAME_WIDTH / 2 - titleWidth / 2;
        int titleY = 330;

        g2.setColor(new Color(120, 120, 120));
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.setColor(new Color(175, 175, 175));
        g2.drawString(title, titleX, titleY);
        g2.setColor(new Color(80, 80, 80));
        g2.drawString(title, titleX, titleY);

        g2.setFont(new Font("微软雅黑", Font.BOLD, 15));
        drawMenuText(g2, "WASD / 方向键 - 移动战机", 445);
        drawMenuText(g2, "H - 核弹", 475);
        drawMenuText(g2, "U / Enter - 开始游戏", 505);
        drawMenuText(g2, "I - 暂停    O - 继续", 535);
        g2.dispose();
    }

    private void drawMenuText(Graphics g, String text, int y) {
        int width = g.getFontMetrics().stringWidth(text);
        int x = GAME_WIDTH / 2 - width / 2;
        g.setColor(Color.WHITE);
        g.drawString(text, x + 1, y + 1);
        g.setColor(new Color(45, 45, 45));
        g.drawString(text, x, y);
    }
    private void drawPlaying(Graphics g) {
        for (Award award : awards) {
            award.draw(g);
        }
        for (Bullet bullet : enemyBullets) {
            bullet.draw(g);
        }
        for (Bullet bullet : heroBullets) {
            bullet.draw(g);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        if (boss != null && boss.alive) {
            boss.draw(g);
        }
        hero.draw(g);
        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }
        drawTopHud(g);
        drawPauseButton(g);
    }
    private void drawTopHud(Graphics g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, GAME_WIDTH, 42);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 13));
        g.drawString("分数: " + score, 8, 18);
        g.drawString("第" + level + "关", 120, 18);
        g.drawString("生命: " + hero.lives, 185, 18);
        g.drawString("核弹: " + hero.bombCount, 260, 18);
        g.setColor(new Color(0, 220, 255));
        g.drawString("越线: " + escapedEnemies + "/" + MAX_ESCAPED_ENEMIES, 8, 36);
        if (hero.fireType == 2) {
            g.setColor(new Color(0, 255, 150));
            g.drawString("双倍火力", 120, 36);
        }
    }
    private void drawPauseButton(Graphics g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(PAUSE_BUTTON_X, PAUSE_BUTTON_Y, PAUSE_BUTTON_W, PAUSE_BUTTON_H, 6, 6);
        g.setColor(new Color(100, 180, 255, 160));
        g.drawRoundRect(PAUSE_BUTTON_X, PAUSE_BUTTON_Y, PAUSE_BUTTON_W, PAUSE_BUTTON_H, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 13));
        String label = gameState == STATE_PAUSED ? "继续" : "PAUSE";
        int lw = g.getFontMetrics().stringWidth(label);
        g.drawString(label, PAUSE_BUTTON_X + (PAUSE_BUTTON_W - lw) / 2, PAUSE_BUTTON_Y + 20);
    }
    private void drawInfoPanel(Graphics g) {
        int x = GAME_WIDTH + 14;
        if (gameState == STATE_MENU) {
            g.setColor(new Color(238, 238, 238));
            g.fillRect(GAME_WIDTH, 0, INFO_WIDTH, SCREEN_HEIGHT);
        }
        g.setColor(gameState == STATE_MENU ? new Color(210, 210, 210) : new Color(60, 130, 210));
        g.drawLine(GAME_WIDTH, 0, GAME_WIDTH, SCREEN_HEIGHT);

        g.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        g.setColor(Color.BLACK);
        int y = 38;
        y = drawInfoLine(g, "得分", String.valueOf(score), x, y);
        y = drawInfoLine(g, "积分", String.valueOf(getUserPoints()), x, y);
        y = drawInfoLine(g, "生命值", String.valueOf(hero.lives), x, y);
        y = drawInfoLine(g, "当前第", level + " 关", x, y);
        y = drawInfoLine(g, "拥有核弹", hero.bombCount + " 枚", x, y);
        y = drawInfoLine(g, "越过防线敌机", escapedEnemies + "架", x, y);
        y += 12;
        g.drawString("未出现敌机数:", x, y);
        y += 38;
        drawEnemyInfo(g, smallEnemyImage, "初级敌机:" + remainingSmallEnemies + "架", x, y, 54, 42);
        y += 70;
        drawEnemyInfo(g, advancedEnemyImage, "高级敌机:" + remainingAdvancedEnemies + "架", x, y, 54, 52);
        y += 76;
        drawEnemyInfo(g, bossPreviewImage, "敌机Boss:" + (bossAppeared ? 0 : 1) + "架", x, y, 70, 46);
        if (boss != null && boss.alive) {
            y += 40;
            g.setColor(new Color(255, 95, 95));
            g.drawString("Boss血量: " + boss.hp + "/" + boss.maxHp, x, y);
        }
        if (radarImage != null) {
            g.drawImage(radarImage, GAME_WIDTH + 20, SCREEN_HEIGHT - 160, 130, 130, this);
        }
    }
    private int drawInfoLine(Graphics g, String name, String value, int x, int y) {
        g.setColor(gameState == STATE_MENU ? Color.BLACK : new Color(190, 200, 215));
        g.drawString(name + ":", x, y);
        g.setColor(gameState == STATE_MENU ? Color.BLACK : Color.WHITE);
        g.drawString(value, x + 78, y);
        return y + 28;
    }
    private void drawEnemyInfo(Graphics g, Image image, String text, int x, int y, int imageWidth, int imageHeight) {
        if (image != null) {
            g.drawImage(image, x + 8, y - imageHeight / 2, imageWidth, imageHeight, null);
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        g.drawString(text, x + 78, y + 5);
    }
    private void drawPauseOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
        if (pauseImage != null) {
            g.drawImage(pauseImage, GAME_WIDTH / 2 - 60, 300, 120, 60, null);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 18));
        drawCentered(g, "按 O 键继续游戏", 420);
    }
    private void drawGameOverOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
        if (gameOverImage != null) {
            g.drawImage(gameOverImage, GAME_WIDTH / 2 - 150, 150, 300, 150, null);
        }
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 42));
        drawCentered(g, String.valueOf(score), 380);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 15));
        drawCentered(g, "1 - 重新开始    2 - 返回主菜单", 430);
        drawCentered(g, "Q - 退出游戏", 465);
    }
    private void drawWinOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
        g.setColor(new Color(255, 220, 80));
        g.setFont(new Font("微软雅黑", Font.BOLD, 34));
        drawCentered(g, "恭喜通关", 260);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 18));
        drawCentered(g, "最终分数：" + score, 315);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        drawCentered(g, "Enter 重新开始  |  Q 退出", 370);
    }
    private void drawLevelClearOverlay(Graphics g) {
        int seconds = Math.max(1, (levelClearTimer + 59) / 60);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, SCREEN_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        drawCentered(g, seconds + " 秒后进入第 " + nextLevel + " 关", 365);
    }
    private void drawCentered(Graphics g, String text, int y) {
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, GAME_WIDTH / 2 - width / 2, y);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_Q) {
            System.exit(0);
        }
        if (gameState == STATE_MENU) {
            if (keyCode == KeyEvent.VK_U || keyCode == KeyEvent.VK_ENTER) {
                startNewGame();
            }
            return;
        }
        if (gameState == STATE_PLAYING) {
            handlePlayingKeyPress(keyCode);
            return;
        }
        if (gameState == STATE_PAUSED) {
            if (keyCode == KeyEvent.VK_O || keyCode == KeyEvent.VK_ESCAPE) {
                resumeGame();
            }
            return;
        }
        if (gameState == STATE_GAMEOVER) {
            if (keyCode == KeyEvent.VK_1 || keyCode == KeyEvent.VK_ENTER) {
                startNewGame();
            } else if (keyCode == KeyEvent.VK_2) {
                returnToMenu();
            }
            return;
        }
        if (gameState == STATE_WIN && keyCode == KeyEvent.VK_ENTER) {
            resetToReady();
        }
    }
    private void handlePlayingKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                downPressed = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = true;
                break;
            case KeyEvent.VK_H:
            case KeyEvent.VK_B:
                useBomb();
                break;
            case KeyEvent.VK_I:
            case KeyEvent.VK_ESCAPE:
                pauseGame();
                break;
            default:
                break;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            upPressed = false;
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            downPressed = false;
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameState == STATE_MENU
            && e.getX() >= START_BUTTON_X && e.getX() <= START_BUTTON_X + START_BUTTON_W
            && e.getY() >= START_BUTTON_Y && e.getY() <= START_BUTTON_Y + START_BUTTON_H) {
            startNewGame();
        }
        if (gameState == STATE_PLAYING || gameState == STATE_PAUSED) {
            if (e.getX() >= PAUSE_BUTTON_X && e.getX() <= PAUSE_BUTTON_X + PAUSE_BUTTON_W
                && e.getY() >= PAUSE_BUTTON_Y && e.getY() <= PAUSE_BUTTON_Y + PAUSE_BUTTON_H) {
                if (gameState == STATE_PLAYING) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        }
        requestFocusInWindow();
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    public void startNewGame() {
        resetRunData(customStartLevel);
        gameState = STATE_PLAYING;
        requestFocusInWindow();
    }
    public void resetToReady() {
        resetRunData(customStartLevel);
        gameState = STATE_MENU;
        requestFocusInWindow();
    }
    private void returnToMenu() {
        resetRunData(customStartLevel);
        gameState = STATE_MENU;
        requestFocusInWindow();
    }
    public void pauseGame() {
        if (gameState == STATE_PLAYING) {
            gameState = STATE_PAUSED;
            clearMovementKeys();
        }
    }
    public void resumeGame() {
        if (gameState == STATE_PAUSED) {
            gameState = STATE_PLAYING;
            requestFocusInWindow();
        }
    }
    public int getGameState() {
        return gameState;
    }
    public int getEnemySpeedBonus() {
        return enemySpeedBonus;
    }
    public int getCustomStartLevel() {
        return customStartLevel;
    }
    public int getSelectedHeroType() {
        return selectedHeroType;
    }
    public int getSelectedHeroSkin() {
        return selectedHeroSkin;
    }
    public void setSelectedHeroSkin(int skinId) {
        selectedHeroSkin = skinId;
        resetHero();
        requestFocusInWindow();
    }
    public int getUserPoints() {
        if (username == null || userStore == null) {
            return 0;
        }
        return userStore.getPoints(username);
    }
    public void applyCustomSettings(int speedBonus, int startLevel, int heroType) {
        this.enemySpeedBonus = speedBonus;
        this.customStartLevel = startLevel;
        this.selectedHeroType = heroType;
        if (username != null && userStore != null) {
            selectedHeroSkin = userStore.getSelectedSkin(username);
        }
        Enemy.speedBonus = speedBonus;
        resetRunData(customStartLevel);
        gameState = STATE_MENU;
        requestFocusInWindow();
    }
    private void resetRunData(int startLevel) {
        score = 0;
        scoreStored = false;
        level = startLevel;
        bgy = 0;
        autoFireTimer = 0;
        enemySpawnTimer = 0;
        bgSpeed = 2 + Math.max(0, startLevel - 1);
        Enemy.speedBonus = enemySpeedBonus;
        heroBullets.clear();
        enemyBullets.clear();
        enemies.clear();
        awards.clear();
        explosions.clear();
        boss = null;
        resetHero();
        setupLevel(level);
        clearMovementKeys();
    }
    private void setupLevel(int targetLevel) {
        remainingSmallEnemies = targetLevel == 1 ? 20 : 10;
        remainingAdvancedEnemies = targetLevel == 1 ? 0 : (targetLevel == 2 ? 10 : 20);
        escapedEnemies = 0;
        bossAppeared = false;
        boss = null;
        enemySpawnTimer = 0;
        enemySpawnInterval = Math.max(25, 50 - targetLevel * 5);
        bgSpeed = Math.min(5, 2 + targetLevel - 1);
        heroBullets.clear();
        enemyBullets.clear();
        enemies.clear();
        awards.clear();
    }
    private void resetHero() {
        hero = new Hero(selectedHeroType, selectedHeroSkin);
        hero.x = GAME_WIDTH / 2 - hero.width / 2;
        hero.y = SCREEN_HEIGHT - 150;
    }
    private void storeScorePoints() {
        if (scoreStored || username == null || userStore == null) {
            return;
        }
        int earnedPoints = score / 5;
        if (earnedPoints > 0) {
            userStore.addPoints(username, earnedPoints);
        }
        scoreStored = true;
    }
    private void respawnHero() {
        hero.alive = true;
        hero.x = GAME_WIDTH / 2 - hero.width / 2;
        hero.y = SCREEN_HEIGHT - 150;
        hero.fireType = 1;
        hero.bombCount = 0;
        hero.isInvincible = true;
        hero.invincibleTimer = Hero.INVINCIBLE_DURATION;
    }
    private void clearMovementKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
    }
}
