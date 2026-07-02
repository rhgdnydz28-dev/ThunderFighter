package com.viking.game;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.imageio.ImageIO;

public abstract class GameObject {

    public int x;
    public int y;
    public int width;
    public int height;
    public Image image;
    public int speed;
    public boolean alive;

    public GameObject(int x, int y, int width, int height, Image image, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.speed = speed;
        this.alive = true;
    }

    public static Image loadImage(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("[image] missing file: " + path);
                return null;
            }
            Image img = ImageIO.read(file);
            if (img != null && !path.toLowerCase().endsWith(".gif")) {
                return img;
            }
        } catch (IOException e) {
            System.err.println("[image] ImageIO failed: " + path + " - " + e.getMessage());
        }

        try {
            Image img = Toolkit.getDefaultToolkit().getImage(path);
            MediaTracker tracker = new MediaTracker(new Canvas());
            tracker.addImage(img, 0);
            tracker.waitForID(0, 2000);
            if (!tracker.isErrorID(0) && img.getWidth(null) > 0) {
                return img;
            }
        } catch (Exception e) {
            System.err.println("[image] Toolkit failed: " + path + " - " + e.getMessage());
        }
        return null;
    }

    public static Image loadImageWithTransparentWhite(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("[image] missing file: " + path);
                return null;
            }
            BufferedImage source = ImageIO.read(file);
            if (source == null) {
                return null;
            }
            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = result.getGraphics();
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();
            removeEdgeWhite(result);
            return result;
        } catch (IOException e) {
            System.err.println("[image] transparent white failed: " + path + " - " + e.getMessage());
            return null;
        }
    }

    private static void removeEdgeWhite(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[][] visited = new boolean[width][height];
        Queue<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            addWhitePixel(image, visited, queue, x, 0);
            addWhitePixel(image, visited, queue, x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            addWhitePixel(image, visited, queue, 0, y);
            addWhitePixel(image, visited, queue, width - 1, y);
        }

        int[] dx = {1, -1, 0, 0, 1, 1, -1, -1};
        int[] dy = {0, 0, 1, -1, 1, -1, 1, -1};
        while (!queue.isEmpty()) {
            int[] point = queue.poll();
            image.setRGB(point[0], point[1], image.getRGB(point[0], point[1]) & 0x00FFFFFF);
            for (int i = 0; i < dx.length; i++) {
                addWhitePixel(image, visited, queue, point[0] + dx[i], point[1] + dy[i]);
            }
        }
    }

    private static void addWhitePixel(BufferedImage image, boolean[][] visited, Queue<int[]> queue, int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight() || visited[x][y]) {
            return;
        }
        visited[x][y] = true;
        if (isNearWhite(image.getRGB(x, y))) {
            queue.add(new int[]{x, y});
        }
    }

    private static boolean isNearWhite(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        return alpha > 0 && red >= 245 && green >= 245 && blue >= 245;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean intersects(GameObject other) {
        return alive && other.alive && getBounds().intersects(other.getBounds());
    }

    public abstract void update();

    public abstract void draw(Graphics g);
}
