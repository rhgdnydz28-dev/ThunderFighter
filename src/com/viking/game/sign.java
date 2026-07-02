package com.viking.game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class sign {

    private static final Path USER_FILE = Paths.get("users.dat");
    private static final String USER = "用户名：";
    private static final String PASSWORD = "  密码：";
    private static final String POINTS = "  积分：";
    private static final String SELECTED_SKIN = "  当前装备皮肤：";
    private static final String UNLOCKED_SKINS = "  已解锁皮肤：";

    public static final int ORIGINAL_SKIN = 0;
    public static final int CUSTOM_SKIN = 1;
    public static final int MAX_SKIN_ID = CUSTOM_SKIN;

    private final Map<String, UserRecord> users = new LinkedHashMap<>();

    public sign() {
        load();
    }

    public boolean register(String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password) || users.containsKey(username)) {
            return false;
        }
        users.put(username, new UserRecord(username, password));
        save();
        return true;
    }

    public boolean authenticate(String username, String password) {
        UserRecord record = users.get(username);
        return record != null && record.password.equals(password);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public int getPoints(String username) {
        UserRecord record = users.get(username);
        return record == null ? 0 : record.points;
    }

    public void addPoints(String username, int points) {
        UserRecord record = users.get(username);
        if (record != null && points > 0) {
            record.points += points;
            save();
        }
    }

    public int getSelectedSkin(String username) {
        UserRecord record = users.get(username);
        return record == null ? ORIGINAL_SKIN : record.selectedSkin;
    }

    public boolean isSkinUnlocked(String username, int skinId) {
        UserRecord record = users.get(username);
        return record != null && record.unlockedSkins.contains(skinId);
    }

    public boolean unlockSkin(String username, int skinId, int cost) {
        UserRecord record = users.get(username);
        if (record == null || skinId < ORIGINAL_SKIN || skinId > MAX_SKIN_ID || cost < 0) {
            return false;
        }
        if (record.unlockedSkins.contains(skinId)) {
            return true;
        }
        if (record.points < cost) {
            return false;
        }
        record.points -= cost;
        record.unlockedSkins.add(skinId);
        save();
        return true;
    }

    public boolean setSelectedSkin(String username, int skinId) {
        UserRecord record = users.get(username);
        if (record == null || !record.unlockedSkins.contains(skinId)) {
            return false;
        }
        record.selectedSkin = skinId;
        save();
        return true;
    }

    public boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,16}");
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 4 && password.length() <= 20;
    }

    private void load() {
        if (!Files.exists(USER_FILE)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(USER_FILE, StandardCharsets.UTF_8)) {
                UserRecord record = UserRecord.fromLine(line.trim());
                if (record != null && isValidUsername(record.username) && isValidPassword(record.password)) {
                    users.put(record.username, record);
                }
            }
        } catch (IOException e) {
            System.err.println("[user] read users.dat failed: " + e.getMessage());
        }
    }

    private void save() {
        StringBuilder builder = new StringBuilder();
        for (UserRecord record : users.values()) {
            builder.append(record.toLine()).append(System.lineSeparator());
        }
        try {
            Files.write(USER_FILE, builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[user] save users.dat failed: " + e.getMessage());
        }
    }

    private static class UserRecord {
        private final String username;
        private final String password;
        private int points;
        private int selectedSkin = ORIGINAL_SKIN;
        private final Set<Integer> unlockedSkins = new LinkedHashSet<>();

        private UserRecord(String username, String password) {
            this.username = username;
            this.password = password;
            this.unlockedSkins.add(ORIGINAL_SKIN);
        }

        private static UserRecord fromLine(String line) {
            String username = readField(line, USER, PASSWORD);
            String password = readField(line, PASSWORD, POINTS);
            String points = readField(line, POINTS, SELECTED_SKIN);
            String selectedSkin = readField(line, SELECTED_SKIN, UNLOCKED_SKINS);
            String unlockedSkins = readLastField(line, UNLOCKED_SKINS);
            if (username == null || password == null || points == null || selectedSkin == null || unlockedSkins == null) {
                return null;
            }
            UserRecord record = new UserRecord(username, password);
            record.points = parseInt(points, 0);
            record.selectedSkin = parseInt(selectedSkin, ORIGINAL_SKIN);
            record.loadSkins(unlockedSkins);
            return record;
        }

        private void loadSkins(String value) {
            unlockedSkins.clear();
            unlockedSkins.add(ORIGINAL_SKIN);
            for (String part : value.replace('，', ',').split(",")) {
                int skinId = parseInt(part, -1);
                if (skinId >= ORIGINAL_SKIN && skinId <= MAX_SKIN_ID) {
                    unlockedSkins.add(skinId);
                }
            }
            if (!unlockedSkins.contains(selectedSkin)) {
                selectedSkin = ORIGINAL_SKIN;
            }
        }

        private String toLine() {
            return USER + username + PASSWORD + password + POINTS + points
                + SELECTED_SKIN + selectedSkin + UNLOCKED_SKINS + formatSkins();
        }

        private String formatSkins() {
            StringBuilder builder = new StringBuilder();
            for (Integer skinId : unlockedSkins) {
                if (builder.length() > 0) {
                    builder.append('，');
                }
                builder.append(skinId);
            }
            return builder.toString();
        }

        private static String readField(String line, String start, String end) {
            int startIndex = line.indexOf(start);
            int endIndex = line.indexOf(end);
            return startIndex >= 0 && endIndex > startIndex
                ? line.substring(startIndex + start.length(), endIndex).trim()
                : null;
        }

        private static String readLastField(String line, String start) {
            int startIndex = line.indexOf(start);
            return startIndex >= 0 ? line.substring(startIndex + start.length()).trim() : null;
        }

        private static int parseInt(String value, int defaultValue) {
            try {
                return Math.max(0, Integer.parseInt(value.trim()));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }
}
