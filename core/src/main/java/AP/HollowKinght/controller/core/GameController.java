package AP.HollowKinght.controller.core;

import AP.HollowKinght.controller.player.KnightController;
import AP.HollowKinght.model.player.Knight;
import com.badlogic.gdx.utils.Array;
import AP.HollowKinght.model.enemy.Zote;

public class GameController {
    private static GameController instance;

    private InputController inputController;
    private KnightController knightController;
    private Knight knight;

    private com.badlogic.gdx.maps.tiled.TiledMap tiledMap;

    private Array<MapTrigger> mapTriggers = new Array<>();
    private Array<SpawnPoint> spawnPoints = new Array<>();

    // آرایه‌های نگهداری جادوهای فعال در محیط (شامل گلوله آتشین و فریاد)
    public com.badlogic.gdx.utils.Array<FireballSpell> fireballs = new com.badlogic.gdx.utils.Array<>();
    public com.badlogic.gdx.utils.Array<ScreamSpell> screams = new com.badlogic.gdx.utils.Array<>();

    private float respawnX = 300f;
    private float respawnY = 400f;

    public GameController(Knight knight) {
        instance = this;
        this.knight = knight;
        this.inputController = new InputController();
        this.knightController = new KnightController(knight, inputController);
    }

    private com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.Enemy> enemies = new com.badlogic.gdx.utils.Array<>();
    private com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.EnemyCorpse> corpses = new com.badlogic.gdx.utils.Array<>();
    private com.badlogic.gdx.utils.Array<AP.HollowKinght.controller.core.EnemySpawnTracker> spawnTrackers = new com.badlogic.gdx.utils.Array<>();

    // خواندن نقاط اسپاون از نقشه و چیدمان اولیه دشمنان مپ و شخصیت زوت
    public void initEnemySpawns() {
        spawnTrackers.clear();
        enemies.clear();
        corpses.clear();
        for (SpawnPoint sp : getSpawnPoints()) {
            if (sp.spawnType == null) continue;

            if (sp.spawnType.equalsIgnoreCase("zote")) {
                enemies.add(new AP.HollowKinght.model.enemy.Zote(sp.x, sp.y));
                continue;
            }

            EnemySpawnTracker tracker = null;
            AP.HollowKinght.model.enemy.Enemy enemy = null;

            if (sp.spawnType.equalsIgnoreCase("tiktik")) {
                tracker = new EnemySpawnTracker(sp.x, sp.y, "tiktik");
                enemy = new AP.HollowKinght.model.enemy.CrystalCrawler(sp.x, sp.y);
            }
            else if (sp.spawnType.equalsIgnoreCase("mosq")) {
                tracker = new EnemySpawnTracker(sp.x, sp.y, "mosq");
                enemy = new AP.HollowKinght.model.enemy.Mosquito(sp.x, sp.y);
            }
            else if (sp.spawnType.equalsIgnoreCase("husk")) {
                tracker = new EnemySpawnTracker(sp.x, sp.y, "husk");
                enemy = new AP.HollowKinght.model.enemy.Husk(sp.x, sp.y);
            }
            else if (sp.spawnType.equalsIgnoreCase("crystalized")) {
                tracker = new EnemySpawnTracker(sp.x, sp.y, "crystalized");
                enemy = new AP.HollowKinght.model.enemy.Crystalized(sp.x, sp.y, true);
            }
            else if (sp.spawnType.equalsIgnoreCase("false_knight")) {
                enemy = new AP.HollowKinght.model.enemy.FalseKnight(sp.x, sp.y);
            }

            if (enemy != null) {
                enemies.add(enemy);
                if (tracker != null) {
                    tracker.activeEnemy = enemy;
                    tracker.hasReset = false;
                    spawnTrackers.add(tracker);
                }
            }
        }
    }

    // به‌روزرسانی وضعیت هوش مصنوعی انمی‌ها، اجساد و منطق ری‌پاون ماب‌های معمولی در صورت دور شدن پلیر
    public void updateEnemiesAndCorpses(float deltaTime) {
        float knightCenterX = knight.x + knight.width / 2f;
        float knightCenterY = knight.y + knight.height / 2f;

        for (EnemySpawnTracker tracker : spawnTrackers) {
            if (tracker.activeEnemy != null && (tracker.activeEnemy.isDead || tracker.activeEnemy.health <= 0)) {
                tracker.isPermanentlyDead = true;
                tracker.activeEnemy = null;
            }
        }

        for (EnemySpawnTracker tracker : spawnTrackers) {
            if (tracker.isPermanentlyDead) {
                tracker.activeEnemy = null;
                continue;
            }

            if (tracker.activeEnemy != null) {
                if (tracker.activeEnemy.isDead || tracker.activeEnemy.health <= 0) {
                    tracker.isPermanentlyDead = true;
                    tracker.activeEnemy = null;
                    continue;
                } else {
                    tracker.savedHealth = tracker.activeEnemy.health;
                }
            }

            float distance = (float) Math.sqrt(Math.pow(knightCenterX - tracker.spawnX, 2) + Math.pow(knightCenterY - tracker.spawnY, 2));

            if (distance > 4000f) {
                if (!tracker.hasReset || tracker.activeEnemy == null) {
                    if (tracker.activeEnemy != null) {
                        enemies.removeValue(tracker.activeEnemy, true);
                    }

                    if (!tracker.isPermanentlyDead) {
                        if (tracker.type.equals("tiktik")) {
                            AP.HollowKinght.model.enemy.CrystalCrawler crawler = new AP.HollowKinght.model.enemy.CrystalCrawler(tracker.spawnX, tracker.spawnY);
                            if (tracker.savedHealth != -1) crawler.health = tracker.savedHealth;
                            enemies.add(crawler);
                            tracker.activeEnemy = crawler;
                        }
                        else if (tracker.type.equals("mosq")) {
                            AP.HollowKinght.model.enemy.Mosquito mos = new AP.HollowKinght.model.enemy.Mosquito(tracker.spawnX, tracker.spawnY);
                            if (tracker.savedHealth != -1) mos.health = tracker.savedHealth;
                            enemies.add(mos);
                            tracker.activeEnemy = mos;
                        }
                        else if (tracker.type.equals("husk")) {
                            AP.HollowKinght.model.enemy.Husk husk = new AP.HollowKinght.model.enemy.Husk(tracker.spawnX, tracker.spawnY);
                            if (tracker.savedHealth != -1) husk.health = tracker.savedHealth;
                            enemies.add(husk);
                            tracker.activeEnemy = husk;
                        }
                        else if (tracker.type.contains("crys")) {
                            AP.HollowKinght.model.enemy.Crystalized crystal = new AP.HollowKinght.model.enemy.Crystalized(tracker.spawnX, tracker.spawnY, true);
                            if (tracker.savedHealth != -1) crystal.health = tracker.savedHealth;
                            enemies.add(crystal);
                            tracker.activeEnemy = crystal;
                        }
                    }

                    tracker.hasReset = true;
                }
            }
            else {
                tracker.hasReset = false;
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            AP.HollowKinght.model.enemy.Enemy e = enemies.get(i);
            if (e.isDead) {
                enemies.removeIndex(i);
            } else {
                e.update(deltaTime, knight.hitbox);
            }
        }

        for (int i = corpses.size - 1; i >= 0; i--) {
            corpses.get(i).update(deltaTime);
        }
    }

    // ایجاد افکت جسد انمی پس از شکست خوردن برای اعمال ضربه نهایی (Knockback)
    public void createCorpse(float x, float y, float w, float h, float stateTime, boolean facingRight, String type, float knockbackX) {
        corpses.add(new AP.HollowKinght.model.enemy.EnemyCorpse(x, y, w, h, stateTime, facingRight, type, knockbackX));
    }

    public com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.Enemy> getEnemies() { return enemies; }
    public com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.EnemyCorpse> getCorpses() { return corpses; }

    public static GameController getInstance() {
        return instance;
    }

    // لود تریگرها و ست کردن نقاط ورود و خروج نقشه به همراه جلوگیری از تکرار ساخت باس‌ها
    public void setMapData(Array<MapTrigger> triggers, Array<SpawnPoint> spawns) {
        this.mapTriggers = triggers;
        this.spawnPoints = spawns;

        for (SpawnPoint sp : spawns) {
            if (sp.spawnType != null && sp.spawnType.equalsIgnoreCase("player0")) {
                this.respawnX = sp.x;
                this.respawnY = sp.y;
            }
            else if (sp.spawnType != null && sp.spawnType.equalsIgnoreCase("false_knight")) {
                boolean alreadyExists = false;
                for (AP.HollowKinght.model.enemy.Enemy e : enemies) {
                    if (e instanceof AP.HollowKinght.model.enemy.FalseKnight) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    enemies.add(new AP.HollowKinght.model.enemy.FalseKnight(sp.x, sp.y));
                }
            }
            else if (sp.spawnType != null && sp.spawnType.equalsIgnoreCase("zote")) {
                boolean alreadyExists = false;
                for (AP.HollowKinght.model.enemy.Enemy e : enemies) {
                    if (e instanceof AP.HollowKinght.model.enemy.Zote) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    enemies.add(new AP.HollowKinght.model.enemy.Zote(sp.x, sp.y));
                }
            }
        }
        fireballs.clear();
        screams.clear();
    }

    public void setRespawnPosition(float x, float y) {
        this.respawnX = x;
        this.respawnY = y;
    }

    public float getRespawnX() { return respawnX; }
    public float getRespawnY() { return respawnY; }

    // به‌روزرسانی فریم به فریم فیزیک شوالیه، دیالوگ‌های متنی و بررسی شرط باز شدن اچیومنت شکار ماب‌ها
    public void update(float deltaTime) {
        AP.HollowKinght.controller.core.DialogueManager.getInstance().update(deltaTime);

        if (knightController != null) {
            knightController.update(deltaTime);
        }
        for (int i = fireballs.size - 1; i >= 0; i--) {
            FireballSpell f = fireballs.get(i);
            f.update(deltaTime);
            if (f.toRemove) fireballs.removeIndex(i);
        }
        for (int i = screams.size - 1; i >= 0; i--) {
            ScreamSpell s = screams.get(i);
            s.update(deltaTime, knight);
            if (s.toRemove) screams.removeIndex(i);
        }

        if (knight.killedCrys && knight.killedTiktik && knight.killedMosq && knight.killedHusk){
            knight.ach3 = true;
        }
    }

    public AP.HollowKinght.model.player.Knight getKnight() {
        return this.knight;
    }

    // پیدا کردن و دریافت نمونه فعال شیء زوت در مپ جاری بازی
    public Zote getZoteInstance() {
        for (AP.HollowKinght.model.enemy.Enemy e : enemies) {
            if (e instanceof Zote) {
                return (Zote) e;
            }
        }
        return null;
    }

    // کلاس مدیریت اسپل شلیک آتشین (Vengeful Spirit) شوالیه و فیزیک برخورد آن با دیوار و انمی
    public static class FireballSpell {
        public float x, y;
        public float width = 180f;
        public float height = 130f;
        public float velocityX;
        public boolean facingRight;
        public float stateTime = 0f;
        public com.badlogic.gdx.math.Rectangle hitbox;
        public com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.Enemy> hitEnemies = new com.badlogic.gdx.utils.Array<>();
        public boolean toRemove = false;

        public FireballSpell(float x, float y, boolean facingRight) {
            this.facingRight = facingRight;
            this.x = facingRight ? x + 100f : x - 100f;
            this.y = y + 20f;
            this.velocityX = facingRight ? 550f : -550f;
            this.hitbox = new com.badlogic.gdx.math.Rectangle(this.x, this.y, width, height);
        }

        public void update(float dt) {
            stateTime += dt;
            x += velocityX * dt;
            hitbox.setPosition(x, y);

            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String t = trigger.type.toLowerCase();
                if (t.equals("floor") || t.equals("plat")) {
                    com.badlogic.gdx.math.Rectangle rect = new com.badlogic.gdx.math.Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (hitbox.overlaps(rect)) {
                        toRemove = true;
                        return;
                    }
                }
            }

            for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                if (!enemy.isDead && !hitEnemies.contains(enemy, true) && hitbox.overlaps(enemy.hitbox)) {
                    boolean hitFromRight = velocityX >= 0;
                    enemy.takeDamage(GameController.getInstance().getKnightController().knight.hasVoidHeart ? 225 : 150, hitFromRight);
                    if (enemy.health <= 0){
                        Knight knight = getInstance().getKnightController().knight;
                        GameController.getInstance().markEnemyAsPermanentlyDead(enemy);
                        if (enemy instanceof AP.HollowKinght.model.enemy.CrystalCrawler) knight.killedTiktik = true;
                        if (enemy instanceof AP.HollowKinght.model.enemy.Husk) knight.killedHusk = true;
                        if (enemy instanceof AP.HollowKinght.model.enemy.Mosquito) knight.killedMosq = true;
                        if (enemy instanceof AP.HollowKinght.model.enemy.Crystalized) knight.killedCrys = true;
                    }
                    hitEnemies.add(enemy);
                }
            }
        }
    }

    // کلاس مدیریت اسپل فریاد صوتی (Howling Wraiths) بالای سر شوالیه با سیستم دمیج تیک‌تیک و چندمرحله‌ای
    public static class ScreamSpell {
        public float spawnX, spawnY;
        public float stateTime = 0f;
        public float duration = 1.2f;
        public boolean toRemove = false;
        private int ticksDone = 0;

        public ScreamSpell(Knight knight) {
            this.spawnX = knight.x + knight.hitboxOffsetX;
            this.spawnY = knight.y - 30;
        }

        public void update(float dt, Knight knight) {
            stateTime += dt;
            if (stateTime >= duration) {
                toRemove = true;
            }

            if ((ticksDone == 0 && stateTime >= 0f) ||
                (ticksDone == 1 && stateTime >= 0.5f) ||
                (ticksDone == 2 && stateTime >= 1.0f)) {

                ticksDone++;
                float hitboxW = 300f;
                float hitboxH = 300f;

                float hitboxX = this.spawnX + knight.width / 2f - hitboxW / 2f;
                float hitboxY = this.spawnY + knight.height;
                com.badlogic.gdx.math.Rectangle screamHitbox = new com.badlogic.gdx.math.Rectangle(hitboxX, hitboxY, hitboxW, hitboxH);

                for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                    if (!enemy.isDead && screamHitbox.overlaps(enemy.hitbox)) {
                        boolean hitFromRight = (this.spawnX + knight.width / 2f) < (enemy.x + enemy.width / 2f);
                        enemy.takeDamage(knight.hasVoidHeart ? 150 : 100, hitFromRight);
                        if (enemy.health <= 0){
                            GameController.getInstance().markEnemyAsPermanentlyDead(enemy);
                            if (enemy instanceof AP.HollowKinght.model.enemy.CrystalCrawler) knight.killedTiktik = true;
                            if (enemy instanceof AP.HollowKinght.model.enemy.Husk) knight.killedHusk = true;
                            if (enemy instanceof AP.HollowKinght.model.enemy.Mosquito) knight.killedMosq = true;
                            if (enemy instanceof AP.HollowKinght.model.enemy.Crystalized) knight.killedCrys = true;
                        }
                    }
                }
            }
        }
    }

    // لود پوزیشن و همگام‌سازی هیت‌باکس شوالیه پس از خواندن اطلاعات ذخیره شده از منو یا فایل سیو اسلات
    public void loadPlayerPosition(float savedX, float savedY) {
        if (this.knight != null) {
            this.knight.x = savedX;
            this.knight.y = savedY;

            if (this.knight.hitbox != null) {
                this.knight.hitbox.setPosition(savedX + knight.hitboxOffsetX, savedY + knight.hitboxOffsetY);
            }

            this.respawnX = savedX;
            this.respawnY = savedY;
        }
    }

    // ثبت مرگ انمی در ساختار سیستم ردیاب برای جلوگیری از زنده شدن مجدد ماب تا خروج بعدی از بازی
    public void markEnemyAsPermanentlyDead(AP.HollowKinght.model.enemy.Enemy enemy) {
        for (EnemySpawnTracker tracker : spawnTrackers) {
            if (tracker.activeEnemy == enemy) {
                tracker.isPermanentlyDead = true;
                tracker.activeEnemy = null;
                break;
            }
        }
    }

    public com.badlogic.gdx.utils.Array<AP.HollowKinght.controller.core.EnemySpawnTracker> getSpawnTrackers() { return spawnTrackers; }
    public InputController getInputController() { return inputController; }
    public KnightController getKnightController() { return knightController; }
    public Array<MapTrigger> getMapTriggers() { return mapTriggers; }
    public Array<SpawnPoint> getSpawnPoints() { return spawnPoints; }
    public void setTiledMap(com.badlogic.gdx.maps.tiled.TiledMap map) { this.tiledMap = map; }
    public com.badlogic.gdx.maps.tiled.TiledMap getTiledMap() { return this.tiledMap; }
}
