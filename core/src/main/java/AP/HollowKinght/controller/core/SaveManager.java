package AP.HollowKinght.controller.core;

import AP.HollowKinght.model.core.GameData;
import AP.HollowKinght.model.enemy.*;
import AP.HollowKinght.model.player.Knight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class SaveManager {
    private static SaveManager instance;
    public int currentSlot = 1; // اسلات ذخیره‌سازی پیش‌فرض (1,2,3,4)

    private SaveManager() {
        initDatabase();
    }

    public static SaveManager getInstance() {
        if (instance == null) instance = new SaveManager();
        return instance;
    }

    // مقداردهی اولیه دیتابیس و ایجاد جداول مربوط به اطلاعات ذخیره، وضعیت دشمنان و اچیومنت‌ها
    private void initDatabase() {
        String url = "jdbc:sqlite:hollow_knight.db";

        // جدول اصلی ذخیره وضعیت شوالیه، زمان بازی، اسلات چارم‌ها، باس‌ها و اتاق مخفی
        String createGameSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
            "slot_id INTEGER PRIMARY KEY, x REAL, y REAL, health INTEGER, soul INTEGER, time REAL, " +
            "is_god_mode INTEGER, is_noclip INTEGER, " +
            "fk_hp INTEGER, fk_is_dead INTEGER, fk_is_phase2 INTEGER, fk_phase TEXT, fk_x REAL, fk_y REAL, " +
            "zote_hp INTEGER, zote_is_dead INTEGER, zote_state INTEGER, zote_dialogue INTEGER, zote_x REAL, zote_y REAL, " +
            "total_kills INTEGER, death_count INTEGER, killed_tiktik INTEGER, killed_husk INTEGER, killed_mosq INTEGER, killed_crys INTEGER, " +
            "charm_0 INTEGER, charm_1 INTEGER, charm_2 INTEGER, " +
            "hidden_door_hp INTEGER, hidden_room_destroyed INTEGER);";

        // جدول مجزا برای حفظ وضعیت سلامت و مرگ دشمنان معمولی نقشه بازی
        String createEnemiesTable = "CREATE TABLE IF NOT EXISTS game_enemies (" +
            "slot_id INTEGER, type TEXT, x REAL, y REAL, health INTEGER, is_dead INTEGER);";

        // جدول اشتراکی و عمومی برای اچیومنت‌های آزاد شده بازی
        String createGlobalAchTable = "CREATE TABLE IF NOT EXISTS global_achievements (ach_id TEXT PRIMARY KEY, unlocked INTEGER);";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt1 = conn.prepareStatement(createGameSavesTable);
                 PreparedStatement stmt2 = conn.prepareStatement(createEnemiesTable);
                 PreparedStatement stmt3 = conn.prepareStatement(createGlobalAchTable)) {
                stmt1.execute();
                stmt2.execute();
                stmt3.execute();
            }
        } catch (Exception e) {
            System.err.println("DB Init Error: " + e.getMessage());
        }
    }

    // بررسی اینکه آیا قبلاً ذخیره‌ای در اسلات مورد نظر ثبت شده است یا خیر
    public boolean hasSave(int slot) {
        String url = "jdbc:sqlite:hollow_knight.db";
        String query = "SELECT 1 FROM game_saves WHERE slot_id = ?;";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, slot);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // بخش اچیومنت‌ها (Achievements)
    // ==========================================

    // بارگذاری اچیومنت‌های آزاد شده عمومی از دیتابیس
    public HashMap<String, Boolean> loadGlobalAchievements() {
        HashMap<String, Boolean> achievements = new HashMap<>();
        achievements.put("ach1", false); achievements.put("ach2", false);
        achievements.put("ach3", false); achievements.put("ach4", false); achievements.put("ach5", false);

        String url = "jdbc:sqlite:hollow_knight.db";
        String selectSQL = "SELECT * FROM global_achievements;";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(selectSQL);
                 ResultSet rs = stmt.executeQuery()) {

                boolean dataFound = false;
                while (rs.next()) {
                    dataFound = true;
                    achievements.put(rs.getString("ach_id"), rs.getInt("unlocked") == 1);
                }

                // در صورتی که دیتابیس خالی باشد، حالت اولیه را ذخیره کن
                if (!dataFound) saveGlobalAchievements(achievements);
            }
        } catch (Exception e) {}
        return achievements;
    }

    // به روزرسانی یا ثبت اچیومنت‌های جدید در دیتابیس با قابلیت بچ (Batch)
    public void saveGlobalAchievements(HashMap<String, Boolean> achievements) {
        String url = "jdbc:sqlite:hollow_knight.db";
        String replaceSQL = "REPLACE INTO global_achievements (ach_id, unlocked) VALUES (?, ?);";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement replaceStmt = conn.prepareStatement(replaceSQL)) {
                for (String key : achievements.keySet()) {
                    replaceStmt.setString(1, key);
                    replaceStmt.setInt(2, achievements.get(key) ? 1 : 0);
                    replaceStmt.addBatch();
                }
                replaceStmt.executeBatch();
            }
        } catch (Exception e) {}
    }

    // ==========================================
    // مکانیزم ذخیره بازی (Save Game Logic)
    // ==========================================

    // استخراج فیلدهای زنده بازی، دسته‌بندی انمی‌ها و فرستادن اطلاعات برای ثبت نهایی در SQLite
    public void saveGame(Knight knight, float totalGameTime, int slot) {
        this.currentSlot = slot;
        GameData data = new GameData();

        // ۱. اطلاعات موقعیتی و حیاتی شوالیه
        data.playerX = knight.x;
        data.playerY = knight.y;
        data.health = knight.health;
        data.soul = knight.soul;
        data.isGodMode = knight.isGodMode;
        data.isNoclip = knight.isNoclip;
        data.totalGameTime = totalGameTime;

        // ۲. آمار مربوط به کشتار، مرگ و وضعیت دشمنان کشته شده برای بخش پایان بازی
        data.totalMobsKilled = knight.totalMobsKilled;
        data.knightDeathCount = knight.knightDeathCount;
        data.killedTiktik = knight.killedTiktik;
        data.killedHusk = knight.killedHusk;
        data.killedMosq = knight.killedMosq;
        data.killedCrys = knight.killedCrys;

        // ۳. کپی کردن آخرین وضعیت چارم‌های مجهز شده از اینونتوری
        InventoryManager inv = InventoryManager.getInstance();
        System.arraycopy(inv.equippedCharms, 0, data.equippedCharms, 0, 3);

        // ۴. همگام‌سازی وضعیت سلامت و تخریب دیوار اتاق مخفی
        HiddenRoomManager hrm = HiddenRoomManager.getInstance();
        data.hiddenDoorHP = hrm.wallHealth;
        data.isHiddenRoomDestroyed = hrm.isDestroyed;

        // ۵. چرخه تفکیک باس‌ها و انمی‌های معمولی مپ برای ثبت وضعیت زنده یا مرده بودن آن‌ها
        for (Enemy enemy : GameController.getInstance().getEnemies()) {
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;
                data.falseKnightX = fk.x;
                data.falseKnightY = fk.y;
                data.falseKnightHP = fk.health;
                data.falseKnightIsDead = fk.isDead;
                data.falseKnightIsPhase2 = fk.isPhase2;
                data.falseKnightPhase = (fk.currentState != null) ? fk.currentState.name() : "INACTIVE";
            } else if (enemy instanceof Zote) {
                Zote z = (Zote) enemy;
                data.zoteX = z.x;
                data.zoteY = z.y;
                data.zoteHP = z.health;
                data.zoteIsDead = z.isDead;
                data.zoteAiState = z.aiState;
                data.zoteDialogueIndex = DialogueManager.getInstance().globalPreceptCounter;
            } else {
                GameData.EnemySaveState es = new GameData.EnemySaveState();
                es.x = enemy.x;
                es.y = enemy.y;
                es.health = enemy.health;
                es.isDead = enemy.isDead;

                if (enemy instanceof Husk) es.type = "husk";
                else if (enemy instanceof Mosquito) es.type = "mosq";
                else if (enemy instanceof CrystalCrawler) es.type = "tiktik";
                else if (enemy instanceof Crystalized) es.type = "crystalized";

                data.enemiesState.add(es);
            }
        }

        saveToSQLite(data, slot);
    }

    // لود کردن اطلاعات یک اسلات ذخیره خاص از روی دیتابیس
    public GameData loadGame(int slot) {
        this.currentSlot = slot;
        return loadFromSQLite(slot);
    }

    // ==========================================
    // متدهای داخلی ارتباط با دیتابیس SQLite
    // ==========================================

    // ثبت همزمان و اتمیک اطلاعات شیء GameData در پایگاه داده
    private void saveToSQLite(GameData data, int slot) {
        String url = "jdbc:sqlite:hollow_knight.db";
        String insertSQL = "REPLACE INTO game_saves (slot_id, x, y, health, soul, time, " +
            "is_god_mode, is_noclip, " +
            "fk_hp, fk_is_dead, fk_is_phase2, fk_phase, fk_x, fk_y, " +
            "zote_hp, zote_is_dead, zote_state, zote_dialogue, zote_x, zote_y, " +
            "total_kills, death_count, killed_tiktik, killed_husk, killed_mosq, killed_crys, " +
            "charm_0, charm_1, charm_2, " +
            "hidden_door_hp, hidden_room_destroyed) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        String delEnemiesSQL = "DELETE FROM game_enemies WHERE slot_id = ?;";
        String insEnemiesSQL = "INSERT INTO game_enemies (slot_id, type, x, y, health, is_dead) VALUES (?, ?, ?, ?, ?, ?);";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                 PreparedStatement delEnemies = conn.prepareStatement(delEnemiesSQL);
                 PreparedStatement insEnemies = conn.prepareStatement(insEnemiesSQL)) {

                // تنظیم پارامترهای اصلی ذخیره
                insertStmt.setInt(1, slot);
                insertStmt.setFloat(2, data.playerX);
                insertStmt.setFloat(3, data.playerY);
                insertStmt.setInt(4, data.health);
                insertStmt.setInt(5, data.soul);
                insertStmt.setFloat(6, data.totalGameTime);
                insertStmt.setInt(7, data.isGodMode ? 1 : 0);
                insertStmt.setInt(8, data.isNoclip ? 1 : 0);

                // فیلدهای مربوط به False Knight
                insertStmt.setInt(9, data.falseKnightHP);
                insertStmt.setInt(10, data.falseKnightIsDead ? 1 : 0);
                insertStmt.setInt(11, data.falseKnightIsPhase2 ? 1 : 0);
                insertStmt.setString(12, data.falseKnightPhase);
                insertStmt.setFloat(13, data.falseKnightX);
                insertStmt.setFloat(14, data.falseKnightY);

                // فیلدهای مربوط به Zote
                insertStmt.setInt(15, data.zoteHP);
                insertStmt.setInt(16, data.zoteIsDead ? 1 : 0);
                insertStmt.setInt(17, data.zoteAiState);
                insertStmt.setInt(18, data.zoteDialogueIndex);
                insertStmt.setFloat(19, data.zoteX);
                insertStmt.setFloat(20, data.zoteY);

                // داده‌های مربوط به آمار کلی بازی
                insertStmt.setInt(21, data.totalMobsKilled);
                insertStmt.setInt(22, data.knightDeathCount);
                insertStmt.setInt(23, data.killedTiktik ? 1 : 0);
                insertStmt.setInt(24, data.killedHusk ? 1 : 0);
                insertStmt.setInt(25, data.killedMosq ? 1 : 0);
                insertStmt.setInt(26, data.killedCrys ? 1 : 0);

                // اطلاعات چارم‌های اینونتوری
                insertStmt.setInt(27, data.equippedCharms[0]);
                insertStmt.setInt(28, data.equippedCharms[1]);
                insertStmt.setInt(29, data.equippedCharms[2]);

                // اطلاعات دیوار مخفی اتاق
                insertStmt.setInt(30, (int) data.hiddenDoorHP);
                insertStmt.setInt(31, data.isHiddenRoomDestroyed ? 1 : 0);

                insertStmt.executeUpdate();

                // پاکسازی دشمنان قدیمی اسلات برای جایگزینی با داده‌های فریم جدید
                delEnemies.setInt(1, slot);
                delEnemies.executeUpdate();

                // ثبت کل وضعیت دشمنان معمولی به صورت بهینه شده با اد بچ (Add Batch)
                for (GameData.EnemySaveState es : data.enemiesState) {
                    insEnemies.setInt(1, slot);
                    insEnemies.setString(2, es.type);
                    insEnemies.setFloat(3, es.x);
                    insEnemies.setFloat(4, es.y);
                    insEnemies.setInt(5, es.health);
                    insEnemies.setInt(6, es.isDead ? 1 : 0);
                    insEnemies.addBatch();
                }
                insEnemies.executeBatch();
            }
        } catch (Exception e) {
            System.err.println("SQL Save Error: " + e.getMessage());
        }
    }

    // بازخوانی اطلاعات ذخیره و بازسازی مجدد شیء دیتای بازی (GameData) از روی دیتابیس
    private GameData loadFromSQLite(int slot) {
        String url = "jdbc:sqlite:hollow_knight.db";
        String selectSQL = "SELECT * FROM game_saves WHERE slot_id = ?;";
        String selectEnemiesSQL = "SELECT * FROM game_enemies WHERE slot_id = ?;";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(selectSQL);
                 PreparedStatement stmtEnemies = conn.prepareStatement(selectEnemiesSQL)) {

                stmt.setInt(1, slot);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        GameData data = new GameData();

                        // بازخوانی موقعیت بازیکن
                        data.playerX = rs.getFloat("x");
                        data.playerY = rs.getFloat("y");
                        data.health = rs.getInt("health");
                        data.soul = rs.getInt("soul");
                        data.totalGameTime = rs.getFloat("time");

                        // کدهای تقلب فعال شده
                        data.isGodMode = rs.getInt("is_god_mode") == 1;
                        data.isNoclip = rs.getInt("is_noclip") == 1;

                        // دیتای لود شده باس False Knight
                        data.falseKnightHP = rs.getInt("fk_hp");
                        data.falseKnightIsDead = rs.getInt("fk_is_dead") == 1;
                        data.falseKnightIsPhase2 = rs.getInt("fk_is_phase2") == 1;
                        data.falseKnightPhase = rs.getString("fk_phase");
                        data.falseKnightX = rs.getFloat("fk_x");
                        data.falseKnightY = rs.getFloat("fk_y");

                        // دیتای لود شده کاراکتر Zote
                        data.zoteHP = rs.getInt("zote_hp");
                        data.zoteIsDead = rs.getInt("zote_is_dead") == 1;
                        data.zoteAiState = rs.getInt("zote_state");
                        data.zoteDialogueIndex = rs.getInt("zote_dialogue");
                        data.zoteX = rs.getFloat("zote_x");
                        data.zoteY = rs.getFloat("zote_y");

                        // آمارهای ثبت شده
                        data.totalMobsKilled = rs.getInt("total_kills");
                        data.knightDeathCount = rs.getInt("death_count");
                        data.killedTiktik = rs.getInt("killed_tiktik") == 1;
                        data.killedHusk = rs.getInt("killed_husk") == 1;
                        data.killedMosq = rs.getInt("killed_mosq") == 1;
                        data.killedCrys = rs.getInt("killed_crys") == 1;

                        // چارم‌های ذخیره شده
                        data.equippedCharms[0] = rs.getInt("charm_0");
                        data.equippedCharms[1] = rs.getInt("charm_1");
                        data.equippedCharms[2] = rs.getInt("charm_2");

                        // ساختار دیوار اتاق مخفی
                        data.hiddenDoorHP = rs.getInt("hidden_door_hp");
                        data.isHiddenRoomDestroyed = rs.getInt("hidden_room_destroyed") == 1;

                        // چرخه بازسازی کامل کل لیست دشمنان معمولی نقشه بازی
                        stmtEnemies.setInt(1, slot);
                        try (ResultSet rsE = stmtEnemies.executeQuery()) {
                            while (rsE.next()) {
                                GameData.EnemySaveState es = new GameData.EnemySaveState();
                                es.type = rsE.getString("type");
                                es.x = rsE.getFloat("x");
                                es.y = rsE.getFloat("y");
                                es.health = rsE.getInt("health");
                                es.isDead = rsE.getInt("is_dead") == 1;
                                data.enemiesState.add(es);
                            }
                        }

                        return data;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SQL Load Error: " + e.getMessage());
        }
        return null;
    }
}
