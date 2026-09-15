package AP.HollowKinght.view.screens;

import AP.HollowKinght.controller.core.*;
import AP.HollowKinght.model.core.GameData;
import AP.HollowKinght.model.enemy.*;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.renderer.ZoteRenderer;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.renderer.KnightRenderer;
import AP.HollowKinght.view.renderer.HudRenderer;
import AP.HollowKinght.view.renderer.EnemyRenderer;
import AP.HollowKinght.view.renderer.FalseKnightRenderer;
import AP.HollowKinght.view.screens.menus.PauseMenu;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameplayScreen extends AbstractScreen {
    // ابزارهای اصلی رندرینگ و دوربین بازی
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    public OrthographicCamera camera;

    // موجودیت‌های بازی و رندرر‌های اختصاصی آن‌ها
    private Knight knight;
    private KnightRenderer knightRenderer;
    private HudRenderer hudRenderer;
    private EnemyRenderer enemyRenderer;
    private FalseKnightRenderer falseKnightRenderer;
    private GameController gameController;
    private ZoteRenderer zoteRenderer;

    // مدیریت نقشه بازی (TiledMap)
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    // متغیرهای کنترل وضعیت منطق نقشه و با‌س‌فایت
    private Rectangle bossDoorRect = null;
    private boolean bossDoorClosed = false;
    public float totalGameTime = 0;
    private boolean isVictoryMenuTriggered = false;
    private float victoryTimer = 0;

    // حافظه موقت (Cache) برای تریگرهای درب اتاق باس
    private Array<MapTrigger> bossDoorTriggers = new Array<>();
    private boolean bossTriggersCached = false;

    // شمارشگر فریم و محدوده حرکتی دوربین
    private int frameCounter = 0;
    private boolean setupLogDone = false;
    private float minX, maxX, minY, maxY;

    // ابعاد پیش‌فرض نقشه بازی
    private final float MAP_WIDTH = 8000f;
    private final float MAP_HEIGHT = 8000f;

    // مدیریت موسیقی محیطی و تغییرات پویا
    private String currentMusic = "main1.wav";
    private String nextMusicTarget = null;
    private float musicTimer = -1f;
    private MapTrigger boss_door = null;

    // متغیرهای وضعیت گفتگو و وضعیت‌های منطقی بازی
    private boolean showPromptE = false;
    private boolean f1 = true;
    private boolean f2 = true;
    private boolean f3 = true;
    private boolean f5 = true;
    private boolean f4 = true;
    public boolean isPaused = false;

    // ساختار داده داخلی برای مدیریت ذرات محیطی نقشه (Particles)
    private class EnvironmentalParticle {
        float x, y;
        float speedX, speedY;
        float size;
        float alpha;
        float lifeTime;
        float maxLifeTime;
        float waveOffset;
        float r, g, b;

        public EnvironmentalParticle() {
            reset(true);
        }

        // بازنشانی موقعیت و مشخصات ذرات بر اساس موسیقی فعال محیط
        public void reset(boolean randomY) {
            this.x = camera.position.x - 700f + MathUtils.random(1400f);
            this.y = randomY ? (camera.position.y - 400f + MathUtils.random(800f)) : (camera.position.y + 400f);
            this.size = MathUtils.random(5f, 11.5f);
            this.alpha = MathUtils.random(0.3f, 0.65f);
            this.maxLifeTime = MathUtils.random(8f, 12f);
            this.lifeTime = 0f;
            this.waveOffset = MathUtils.random(0f, 6.28f);

            // تعیین رنگ ذرات بر اساس اتمسفر صوتی مپ
            if (currentMusic.equalsIgnoreCase("main2.wav")) {
                this.speedY = MathUtils.random(-70f, -140f);
                this.speedX = MathUtils.random(-100f, -10f);
                float offset = MathUtils.random(-0.7f, 0.4f);
                this.r = MathUtils.clamp(0.964f + offset, 0f, 1f);
                this.g = MathUtils.clamp(0.620f + offset, 0f, 1f);
                this.b = MathUtils.clamp(0.968f + offset, 0f, 1f);
            } else {
                this.speedY = MathUtils.random(-50f, -90f);
                this.speedX = MathUtils.random(-50f, 50f);
                float offset = MathUtils.random(-0.24f, 0.15f);
                this.r = MathUtils.clamp(0.451f + offset, 0f, 1f);
                this.g = MathUtils.clamp(0.698f + offset, 0f, 1f);
                this.b = MathUtils.clamp(1.000f + offset, 0f, 1f);
            }
        }

        // به‌روزرسانی فیزیک حرکتی ذرات در هر فریم
        public void update(float delta) {
            lifeTime += delta;
            if (currentMusic.equalsIgnoreCase("main2.wav")) {
                alpha = (MathUtils.sin(lifeTime * 4f + waveOffset) + 1f) / 2f * 0.5f + 0.3f;
                x += speedX * delta;
                y += speedY * delta;
            } else {
                x += (speedX + MathUtils.sin(lifeTime * 2f + waveOffset) * 15f) * delta;
                y += speedY * delta;
            }

            // بازنشانی در صورت اتمام طول عمر یا خروج از محدوده دید دوربین
            if (lifeTime >= maxLifeTime || y < camera.position.y - 700f || x < camera.position.x - 750f || x > camera.position.x + 750f) {
                reset(false);
            }
        }
    }

    private Array<EnvironmentalParticle> particles;
    private final int PARTICLE_COUNT = 70;

    // سازنده فرعی برای شروع بازی جدید (New Game)
    public GameplayScreen() {
        this(false, AP.HollowKinght.controller.core.SaveManager.getInstance().currentSlot);
        Gdx.app.log("GameplayEngine", "Initializing fresh game cycle on slot: " + SaveManager.getInstance().currentSlot);
    }

    // سازنده اصلی با قابلیت تشخیص وضعیت لود بازی و شماره اسلات مربوطه
    public GameplayScreen(boolean isLoadGame, int slotToLoad) {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        knight = new Knight(250, 200);
        gameController = new GameController(knight);
        knightRenderer = new KnightRenderer();
        hudRenderer = new HudRenderer();
        enemyRenderer = new EnemyRenderer();
        falseKnightRenderer = new FalseKnightRenderer();
        zoteRenderer = new ZoteRenderer();

        // بارگذاری نقشه بازی از ماژول کمکی Tiled
        TiledMapHelper mapHelper = new TiledMapHelper();
        tiledMap = mapHelper.loadMap("newmap/myMap.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1f);

        // ارسال داده‌های مربوط به تریگرها و نقاط اسپاون به کنترلر بازی
        gameController.setMapData(mapHelper.getMapTriggers(), mapHelper.getSpawnPoints());
        gameController.initEnemySpawns();

        HiddenRoomManager.getInstance().init(
            GameController.getInstance().getMapTriggers(),
            GameController.getInstance().getSpawnPoints()
        );

        // اعمال دیتای لود شده در صورت معتبر بودن وضعیت لود
        if (isLoadGame) {
            GameData data = SaveManager.getInstance().loadGame(slotToLoad);
            if (data != null) {
                knight.x = data.playerX;
                knight.y = data.playerY;
                knight.health = data.health;
                knight.soul = data.soul;
                this.totalGameTime = data.totalGameTime;

                // بازیابی فیلدهای آماری مربوط به اچیومنت‌ها
                knight.totalMobsKilled = data.totalMobsKilled;
                knight.knightDeathCount = data.knightDeathCount;
                knight.killedTiktik = data.killedTiktik;
                knight.killedHusk = data.killedHusk;
                knight.killedMosq = data.killedMosq;
                knight.killedCrys = data.killedCrys;

                applySaveDataToWorld(data);
                gameController.loadPlayerPosition(data.playerX, data.playerY);
            }
        } else {
            // تعیین موقعیت اولیه بازیکن بر اساس نقاط اسپاون پیش‌فرض نقشه
            float spawnX = gameController.getRespawnX();
            float spawnY = gameController.getRespawnY();
            knight.x = spawnX;
            knight.y = spawnY;
            knight.updateHitbox();
        }

        camera.position.set(knight.x, knight.y, 0);
        camera.update();

        GameController.getInstance().setTiledMap(this.tiledMap);

        particles = new Array<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new EnvironmentalParticle());
        }

        // تولید فونت‌های سیستم پاپ‌آپ اچیومنت‌ها
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 40;
        p.color = Color.WHITE;
        BitmapFont headerBigFont = gen.generateFont(p);
        AchievementPopup.getInstance().setFont(headerBigFont);
    }

    @Override
    public void buildStage() {}

    @Override
    public void render(float delta) {
        // مدیریت کلید ESCAPE برای باز کردن منوی توقف (Pause)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            isPaused = true;
            UIManager.getInstance().changeScreen(new PauseMenu(this));
        }

        DialogueManager.getInstance().update(delta);

        // مدیریت فرآیند گفتگو و برهم‌کنش بازیکن با کاراکتر Zote
        Zote zote = gameController.getZoteInstance();
        if (zote != null) {
            float knightCenterX = knight.x + knight.width / 2f;
            float knightCenterY = knight.y + knight.height / 2f;
            float dist = (float) Math.sqrt(Math.pow(knightCenterX - (zote.spawnX + zote.width/2f), 2) + Math.pow(knightCenterY - (zote.spawnY + zote.height/2f), 2));

            if (dist <= 300f) {
                showPromptE = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !DialogueManager.getInstance().isDialogueActive()) {
                    DialogueManager.getInstance().loadLanguage("fr");
                    DialogueManager.getInstance().startZoteDialogue();
                }
            } else {
                showPromptE = false;
            }

            if (DialogueManager.getInstance().isDialogueActive() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                DialogueManager.getInstance().advanceDialogue();
            }
        }

        // سیستم کدهای تقلب (Cheat Codes) برای تست بخش‌های مختلف گیم‌پلی
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            // ۱. جابه‌جایی سریع به اتاق باس‌فایت (Ctrl + F)
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                for (AP.HollowKinght.controller.core.SpawnPoint sp : GameController.getInstance().getSpawnPoints()) {
                    if (sp.spawnType != null && sp.spawnType.equalsIgnoreCase("player_for_boss")) {
                        knight.x = sp.x;
                        knight.y = sp.y;
                        knight.velocityX = 0;
                        knight.velocityY = 0;
                        Gdx.app.log("CheatSystem", "Teleported knight to boss room successfully.");
                        break;
                    }
                }
            }

            // ۲. تغییر وضعیت حالت عبور از موانع - Noclip (Ctrl + G)
            if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                knight.isNoclip = !knight.isNoclip;
                Gdx.app.log("CheatSystem", "Noclip mode state modified to: " + knight.isNoclip);
            }

            // ۳. بازیابی سلامت بازیکن (Ctrl + H)
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                if (knight.health < 5) {
                    knight.health += 1;
                    Gdx.app.log("CheatSystem", "Heal command executed. Player health: " + knight.health);
                }
            }

            // ۴. پر کردن مخزن روح بازیکن (Ctrl + J)
            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                knight.soul = 99;
                Gdx.app.log("CheatSystem", "Soul container completely refilled.");
            }

            // ۵. فعال‌سازی حالت رویین‌تنی - God Mode (Ctrl + K)
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                knight.isGodMode = !knight.isGodMode;
                Gdx.app.log("CheatSystem", "God mode state modified to: " + knight.isGodMode);
            }

            // ۶. نابود کردن دشمنان در محدوده مشخص (Ctrl + L)
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                int killCount = 0;
                for (Enemy enemy : GameController.getInstance().getEnemies()) {
                    if (!enemy.isDead) {
                        float diffX = enemy.x - knight.x;
                        float diffY = enemy.y - knight.y;
                        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

                        if (distance <= 1500f) {
                            enemy.takeDamage(100000, knight.x < enemy.x);
                            killCount++;
                        }
                    }
                }
                Gdx.app.log("CheatSystem", "Purged " + killCount + " enemies within range.");
            }
        }

        // نرمال‌سازی تغییرات زمانی فریم‌ها برای جلوگیری از گلیچ فیزیکی
        if (delta > 0.05f) delta = 0.0166f;
        if (!isPaused) {
            this.totalGameTime += delta;
            victoryTimer += delta;
        }

        frameCounter++;
        AudioManager.getInstance().updateMusicFade(delta);
        gameController.update(delta);
        gameController.updateEnemiesAndCorpses(delta);

        for (Enemy enemy : GameController.getInstance().getEnemies()) {
            if (enemy instanceof FalseKnight) {
                ((FalseKnight) enemy).update(delta, knight.hitbox);
            } else if (enemy instanceof Zote) {
                ((Zote) enemy).update(delta, knight.hitbox);
            }
        }

        float camHalfWidth = camera.viewportWidth / 2f;
        float camHalfHeight = camera.viewportHeight / 2f;

        // ذخیره تریگرهای درب اتاق باس در حافظه موقت در فریم اول
        if (!bossTriggersCached) {
            for (int i=0; i<gameController.getMapTriggers().size; i++) {
                MapTrigger trigger = gameController.getMapTriggers().get(i);
                if (trigger.type != null && trigger.type.equalsIgnoreCase("boss_door")) {
                    boss_door = trigger;
                }
            }
            bossTriggersCached = true;
            if (!bossDoorTriggers.isEmpty()) {
                MapTrigger firstDoor = bossDoorTriggers.first();
                bossDoorRect = new Rectangle(firstDoor.x, firstDoor.y, firstDoor.width, firstDoor.height);
            }
        }

        Rectangle mapBoundsRect = null;
        Rectangle activeBossRoomRect = null;
        Rectangle bossRoomRect = null;

        // ارزیابی برخورد شوالیه با محدوده‌های نقشه و اتاق باس
        for (MapTrigger trigger : gameController.getMapTriggers()) {
            if (trigger.type != null) {
                if (trigger.type.equalsIgnoreCase("map")) {
                    mapBoundsRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                } else if (trigger.type.equalsIgnoreCase("boss_room")) {
                    bossRoomRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (knight.hitbox.overlaps(bossRoomRect)) {
                        activeBossRoomRect = bossRoomRect;
                    }
                }
            }
        }

        boolean isFalseKnightAlive = false;
        for (Enemy enemy : GameController.getInstance().getEnemies()) {
            if (enemy instanceof FalseKnight) {
                isFalseKnightAlive = enemy.health > 0;
                break;
            }
        }

        // بستن درهای اتاق باس در صورت زنده بودن باس اصلی بازی
        if (bossRoomRect != null) {
            bossDoorClosed = knight.hitbox.overlaps(bossRoomRect) && isFalseKnightAlive;
        }

        if (boss_door != null) {
            boss_door.type = bossDoorClosed ? "Plat" : "boss_door";
        }

        // پایش پویای موقعیت شوالیه برای تعویض خودکار موزیک پس‌زمینه
        String roomMusicDetected = null;
        for (MapTrigger trigger : gameController.getMapTriggers()) {
            if (trigger.bgmFile != null && "audio_zone".equalsIgnoreCase(trigger.type)) {
                Rectangle zoneInstance = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (knight.hitbox.overlaps(zoneInstance)) {
                    roomMusicDetected = trigger.bgmFile;
                    break;
                }
            }
        }

        if (roomMusicDetected != null && !roomMusicDetected.equals(currentMusic) && musicTimer < 0f) {
            musicTimer = 0f;
            nextMusicTarget = roomMusicDetected;
            Gdx.app.log("AudioEngine", "Audio zone transition initiated towards: " + nextMusicTarget);
        }

        if (musicTimer >= 0f) {
            musicTimer += delta;
            if (musicTimer >= 3.0f) {
                currentMusic = nextMusicTarget;
                AudioManager.getInstance().playMusicFade(currentMusic);
                musicTimer = -1f;
                Gdx.app.log("AudioEngine", "Audio track successfully modified to: " + currentMusic);
            }
        }

        // اعمال محدودیت‌های دوربین بر اساس ابعاد زون جاری
        Rectangle currentActiveBounds = (activeBossRoomRect != null) ? activeBossRoomRect : mapBoundsRect;
        if (currentActiveBounds != null) {
            minX = currentActiveBounds.x + camHalfWidth;
            maxX = currentActiveBounds.x + currentActiveBounds.width - camHalfWidth;
            minY = currentActiveBounds.y + camHalfHeight;
            maxY = currentActiveBounds.y + currentActiveBounds.height - camHalfHeight;

            if (maxX < minX) { float midX = currentActiveBounds.x + currentActiveBounds.width / 2f; minX = midX; maxX = midX; }
            if (maxY < minY) { float midY = currentActiveBounds.y + currentActiveBounds.height / 2f; minY = midY; maxY = midY; }
        } else {
            minX = camHalfWidth;
            maxX = MAP_WIDTH - camHalfWidth;
            minY = camHalfHeight;
            maxY = MAP_HEIGHT - camHalfHeight;
        }

        // تعقیب نرم موقعیت شوالیه توسط دوربین (Lerp)
        camera.position.lerp(new com.badlogic.gdx.math.Vector3(knight.x, knight.y, 0), 0.018f);
        camera.position.x = com.badlogic.gdx.math.MathUtils.clamp(camera.position.x, minX, maxX);
        camera.position.y = com.badlogic.gdx.math.MathUtils.clamp(camera.position.y, minY, maxY);

        // اعمال افکت لرزش دوربین در صورت آسیب دیدن شوالیه
        if (knight.shakeTime > 0) {
            float currentIntensity = knight.shakeIntensity * (knight.shakeTime / 0.25f);
            camera.position.x += MathUtils.random(-currentIntensity, currentIntensity);
            camera.position.y += MathUtils.random(-currentIntensity, currentIntensity);
        }
        camera.update();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        int[] backgroundLayers = new int[] {0, 1, 2, 3, 4, 5, 6, 8};
        tiledMapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        enemyRenderer.renderCorpses(batch);
        batch.setColor(1f, 1f, 1f, 1f);
        enemyRenderer.renderAliveEnemies(batch);

        for (Enemy enemy : GameController.getInstance().getEnemies()) {
            if (enemy instanceof FalseKnight) {
                falseKnightRenderer.render(batch, (FalseKnight) enemy, delta);
            }
        }

        if (zote != null) {
            zoteRenderer.render(batch, zote, delta);
        }
        batch.end();

        int layer1Index = tiledMap.getLayers().getIndex("Tile Layer 1");
        if (layer1Index != -1) {
            tiledMapRenderer.render(new int[] { layer1Index });
        }

        batch.begin();
        batch.setColor(1f, 1f, 1f, knight.isVisible ? 1f : 0.25f);
        knightRenderer.render(batch, knight, delta);
        batch.end();

        // رندر و به‌روزرسانی سیستم ذرات محیطی تحت آلفای مشخص
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (EnvironmentalParticle p : particles) {
            p.update(delta);
            shapeRenderer.setColor(p.r, p.g, p.b, currentMusic.equalsIgnoreCase("main2.wav") ? p.alpha : p.alpha * 0.6f);
            shapeRenderer.circle(p.x, p.y, p.size/2);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        HiddenRoomManager.getInstance().update(delta);
        HiddenRoomManager.getInstance().render(batch, shapeRenderer);

        // تنظیم ماتریکس ۲ بعدی ثابت برای نمایش المان‌های HUD و رابط کاربری
        com.badlogic.gdx.math.Matrix4 hudMatrix = new com.badlogic.gdx.math.Matrix4();
        hudMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(hudMatrix);
        batch.setProjectionMatrix(hudMatrix);

        batch.begin();
        hudRenderer.render(batch, knight, delta);
        if (zote != null) {
            zoteRenderer.renderUI(batch, zote, showPromptE, this.camera);
        }
        batch.end();

        InventoryManager.getInstance().handleInput(this.knight);
        InventoryManager.getInstance().render(batch, shapeRenderer, this.knight);

        // مدیریت فرآیند ارزیابی و بررسی شرایط اچیومنت‌ها
        AchievementPopup.getInstance().update(delta);
        if (AchievementPopup.getInstance().isActive) {
            if (batch.isDrawing()) batch.end();
            if (frameCounter % 60 == 0) {
                Gdx.app.log("UIRenderer", "Rendering active achievement box. Alpha level: " + AchievementPopup.getInstance().alpha);
            }
            AchievementPopup.getInstance().render(batch, shapeRenderer);
        }

        boolean allRegularMobsDead = knight.killedCrys && knight.killedMosq && knight.killedTiktik && knight.killedHusk;
        boolean isFalseKnightDefeated = knight.ach4;
        boolean isHiddenRoomFound = knight.ach5;
        java.util.HashMap<String, Boolean> checkAchs = SaveManager.getInstance().loadGlobalAchievements();

        // اچیومنت ۱: اتمام کامل بازی
        if (f1 && isHiddenRoomFound && allRegularMobsDead && isFalseKnightDefeated && !checkAchs.getOrDefault("ach1", false)) {
            Gdx.app.log("AchievementSystem", "Achievement 1 successfully unlocked.");
            knight.ach1 = true;
            GameController.getInstance().getKnightController().knight.ach1 = true;

            java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
            globalAchs.put("ach1", true);
            SaveManager.getInstance().saveGlobalAchievements(globalAchs);

            AchievementPopup.getInstance().show("Achievement Unlocked: You finished the game!");
            AudioManager.getInstance().playSFX("unlock.mp3");
            f1 = false;
        }

        // اچیومنت ۲: اسپیدران (اتمام بازی زیر ۱۵ دقیقه)
        if (knight.ach1 && totalGameTime <= 15 * 60 && f2 && !checkAchs.getOrDefault("ach2", false)) {
            Gdx.app.log("AchievementSystem", "Achievement 2 (Speedrun) successfully unlocked.");
            knight.ach2 = true;
            GameController.getInstance().getKnightController().knight.ach2 = true;

            java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
            globalAchs.put("ach2", true);
            SaveManager.getInstance().saveGlobalAchievements(globalAchs);

            AchievementPopup.getInstance().show("Achievement Unlocked: Speedrunner King!");
            AudioManager.getInstance().playSFX("unlock.mp3");
            f2 = false;
        }

        // اچیومنت ۳: شکار تمامی دشمنان معمولی
        if (allRegularMobsDead && f3 && !checkAchs.getOrDefault("ach3", false)) {
            knight.ach3 = true;
            java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
            globalAchs.put("ach3", true);
            SaveManager.getInstance().saveGlobalAchievements(globalAchs);
            AchievementPopup.getInstance().show("Achievement Unlocked: You killed all the hunters!");
            AudioManager.getInstance().playSFX("unlock.mp3");
            f3 = false;
        }

        // اچیومنت ۴: شکست دادن باس اصلی (False Knight)
        if (isFalseKnightDefeated && f4 && !checkAchs.getOrDefault("ach4", false)) {
            knight.ach4 = true;
            java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
            globalAchs.put("ach4", true);
            SaveManager.getInstance().saveGlobalAchievements(globalAchs);
            AchievementPopup.getInstance().show("Achievement Unlocked: False Knight Def!");
            AudioManager.getInstance().playSFX("unlock.mp3");
            AudioManager.getInstance().playSFX("wiiin.mp3");
            f4 = false;
            if (victoryTimer >= 10.0f) {
                isVictoryMenuTriggered = true;
                GameCompletionManager.getInstance().isMenuOpen = true;
            }
        }

        // اچیومنت ۵: کشف اتاق مخفی
        if (knight.ach5 && f5 && !checkAchs.getOrDefault("ach5", false)) {
            java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
            globalAchs.put("ach5", true);
            SaveManager.getInstance().saveGlobalAchievements(globalAchs);
            AchievementPopup.getInstance().show("Achievement Unlocked: Hidden room was found!");
            AudioManager.getInstance().playSFX("unlock.mp3");
            f5 = false;
        }

        if (batch.isDrawing()) batch.end();

        GameCompletionManager.getInstance().updateAndInput(this);
        GameCompletionManager.getInstance().render(batch, shapeRenderer, this);

        AchievementPopup.getInstance().update(delta);
        if (AchievementPopup.getInstance().isActive) {
            AchievementPopup.getInstance().render(batch, shapeRenderer);
        }

        if (batch.isDrawing()) batch.end();
    }

    @Override
    public void show(){
        this.isPaused = false;
        currentMusic = "main1.wav";
        AudioManager.getInstance().playMusicFade(currentMusic);
    }

    // تزریق پایدار اطلاعات فایل ذخیره به موجودیت‌های جهان بازی بعد از عملیات لود
    private void applySaveDataToWorld(GameData data) {
        knight.x = data.playerX;
        knight.y = data.playerY;
        knight.killedTiktik = data.killedTiktik;
        knight.killedHusk = data.killedHusk;
        knight.killedMosq = data.killedMosq;
        knight.killedCrys = data.killedCrys;
        knight.health = data.health;
        knight.soul = data.soul;
        knight.isGodMode = data.isGodMode;
        knight.isNoclip = data.isNoclip;
        this.totalGameTime = data.totalGameTime;
        knight.updateHitbox();

        java.util.HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
        if (globalAchs == null) globalAchs = new java.util.HashMap<>();

        knight.ach1 = globalAchs.getOrDefault("ach1", false);
        knight.ach2 = globalAchs.getOrDefault("ach2", false);
        knight.ach3 = globalAchs.getOrDefault("ach3", false);
        knight.ach4 = globalAchs.getOrDefault("ach4", false);
        knight.ach5 = data.isHiddenRoomDestroyed;

        this.f1 = !knight.ach1;
        this.f2 = !knight.ach2;
        this.f3 = !knight.ach3;
        this.f4 = !knight.ach4;
        this.f5 = !globalAchs.getOrDefault("ach5", false);

        InventoryManager inv = InventoryManager.getInstance();
        System.arraycopy(data.equippedCharms, 0, inv.equippedCharms, 0, 3);
        for (int charmId : inv.equippedCharms) {
            if (charmId != -1) {
                inv.applyCharmEffectsLogic(charmId, true, knight);
            }
        }

        HiddenRoomManager.getInstance().wallHealth = (int) data.hiddenDoorHP;
        HiddenRoomManager.getInstance().isDestroyed = data.isHiddenRoomDestroyed;

        if (data.hiddenDoorHP <= 0 || data.isHiddenRoomDestroyed) {
            HiddenRoomManager.getInstance().isDestroyed = true;
            HiddenRoomManager.getInstance().brokenImageAlpha = 1.0f;
            HiddenRoomManager.getInstance().darknessAlpha = 0.0f;
            HiddenRoomManager.getInstance().forceOpenRoomSilent();
        }

        gameController.getEnemies().clear();

        if (!data.falseKnightIsDead) {
            FalseKnight fk = new FalseKnight(data.falseKnightX, data.falseKnightY);
            fk.health = data.falseKnightHP;
            fk.isPhase2 = data.falseKnightIsPhase2;
            if (data.falseKnightPhase != null) {
                try {
                    fk.currentState = FalseKnight.State.valueOf(data.falseKnightPhase);
                } catch (IllegalArgumentException e) {
                    fk.currentState = FalseKnight.State.IDLE;
                }
            }
            gameController.getEnemies().add(fk);
        }

        if (!data.zoteIsDead) {
            Zote zoteInstance = new Zote(data.zoteX, data.zoteY);
            zoteInstance.health = data.zoteHP;
            zoteInstance.aiState = data.zoteAiState;
            DialogueManager.getInstance().globalPreceptCounter = data.zoteDialogueIndex;
            gameController.getEnemies().add(zoteInstance);
        }

        for (GameData.EnemySaveState state : data.enemiesState) {
            if (state.isDead) continue;
            Enemy e = null;
            if ("husk".equals(state.type)) e = new Husk(state.x, state.y);
            else if ("mosq".equals(state.type)) e = new Mosquito(state.x, state.y);
            else if ("tiktik".equals(state.type)) e = new CrystalCrawler(state.x, state.y);
            else if ("crystalized".equals(state.type)) e = new Crystalized(state.x, state.y, true);

            if (e != null) {
                e.health = state.health;
                gameController.getEnemies().add(e);
            }
        }

        for (EnemySpawnTracker tracker : gameController.getSpawnTrackers()) {
            boolean foundAliveEnemy = false;
            for (Enemy aliveEnemy : gameController.getEnemies()) {
                if (Math.abs(aliveEnemy.x - tracker.spawnX) < 5f && Math.abs(aliveEnemy.y - tracker.spawnY) < 5f) {
                    tracker.activeEnemy = aliveEnemy;
                    tracker.savedHealth = aliveEnemy.health;
                    tracker.hasReset = false;
                    tracker.isPermanentlyDead = false;
                    foundAliveEnemy = true;
                    break;
                }
            }
            if (!foundAliveEnemy && !tracker.type.equals("false_knight") && !tracker.type.equals("zote")) {
                tracker.activeEnemy = null;
                tracker.isPermanentlyDead = true;
                tracker.hasReset = true;
            }
        }
        Gdx.app.log("SaveEngine", "Save state injected and synchronized with world data.");
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudRenderer.dispose();
        if (enemyRenderer != null) enemyRenderer.dispose();
        if (falseKnightRenderer != null) falseKnightRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }

    public Knight getKnight(){
        return knight;
    }

    // ذخیره‌سازی سخت اطلاعات اسلات جاری در قالب دیتابیس
    public void saveCurrentSlotData() {
        try {
            int currentActiveSlot = SaveManager.getInstance().currentSlot;
            Knight currentKnight = this.getKnight();
            if (currentKnight != null) {
                SaveManager.getInstance().saveGame(currentKnight, this.totalGameTime, currentActiveSlot);
                Gdx.app.log("SaveEngine", "Hard save successfully performed on slot: " + currentActiveSlot);
            }
        } catch (Exception e) {
            Gdx.app.error("SaveEngine", "Failed to write active slot data to persistent storage", e);
        }
    }
}
