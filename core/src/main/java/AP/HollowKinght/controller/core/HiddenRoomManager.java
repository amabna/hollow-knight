package AP.HollowKinght.controller.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import AP.HollowKinght.view.audio.AudioManager;

public class HiddenRoomManager {
    private static HiddenRoomManager instance;

    public int wallHealth = 3;
    public boolean isDestroyed = false;

    // متغیرهای فید انیمیشن (شفافیت تاریکی اتاق و تصویر افکت شکستگی)
    public float darknessAlpha = 1.0f;     // شروع از ۱.۰ (کاملاً سیاه)
    public float brokenImageAlpha = 0.0f;  // شروع از ۰.۰ (کاملاً محو)

    private MapTrigger hiddenRoomTrigger;
    private MapTrigger hiddenDoorTrigger;
    private MapTrigger eraseTrigger;
    private SpawnPoint brokenSpawnPoint;

    private Texture brokenTexture;

    // لیست نگهداری ذرات غبار و کلوخه‌های ناشی از ضربه به دیوار
    private Array<DustParticle> particles = new Array<>();

    // کلاس کمکی داخلی برای شبیه‌سازی سیستم ذرات (Particle System) گرد و غبار
    private static class DustParticle {
        float x, y;
        float vx, vy;
        float radius;
        float alpha;
        float lifeTime;
        float maxLife;
        float r, g, b;

        public DustParticle(float startX, float startY) {
            this.x = startX;
            this.y = startY;

            // پرتاب ذرات به جهات مختلف به صورت رندم و طبیعی
            this.vx = MathUtils.random(-120f, 120f);
            this.vy = MathUtils.random(-40f, 180f);

            // اندازه‌های مختلف برای شبیه‌سازی کلوخه‌ها و غبار ریز
            this.radius = MathUtils.random(2f, 6f);
            this.alpha = MathUtils.random(0.7f, 1.0f);
            this.maxLife = MathUtils.random(0.4f, 0.9f);
            this.lifeTime = 0f;

            // طیف رنگی قهوه‌ای خاک‌آلود مایل به تیره و روشن دیوارها
            float colorFix = MathUtils.random(0.0f, 0.2f);
            this.r = 0.45f + colorFix; // قرمز مایل به قهوه‌ای
            this.g = 0.35f + colorFix; // سبز مخلوط
            this.b = 0.25f + colorFix; // آبی کمتر برای حفظ تم قهوه‌ای
        }

        public void update(float dt) {
            lifeTime += dt;
            // اعمال شتاب گرانش زمین روی ذرات در حال سقوط
            vy -= 420f * dt;

            x += vx * dt;
            y += vy * dt;

            // فید اوت و غیب شدن تدریجی همزمان با پایان عمر ذره
            alpha = Math.max(0f, 1.0f - (lifeTime / maxLife));
        }

        public boolean isDead() {
            return lifeTime >= maxLife;
        }
    }

    private HiddenRoomManager() {
        loadWallTexture();
    }

    // مکانیزم لود ایمن و فیکس تکسچر دیوار شکسته با مسیرهای جایگزین
    private void loadWallTexture() {
        try {
            if (Gdx.files.internal("HKassets/hidden_room/broken.png").exists()) {
                brokenTexture = new Texture(Gdx.files.internal("HKassets/hidden_room/broken.png"));
            } else if (Gdx.files.internal("broken.png").exists()) {
                brokenTexture = new Texture(Gdx.files.internal("broken.png"));
            } else {
                System.out.println("⚠️ Texture broken.png not found in standard paths. Attempting backup direct load...");
                brokenTexture = new Texture("HKassets/hidden_room/broken.png");
            }
        } catch (Exception e) {
            System.err.println("❌ Critical Error: Could not load broken.png tile asset: " + e.getMessage());
        }
    }

    public static HiddenRoomManager getInstance() {
        if (instance == null) {
            instance = new HiddenRoomManager();
        }
        return instance;
    }

    // مقداردهی اولیه اتاق، لود تریگرها و بررسی اینکه آیا دیوار قبلاً فروریخته بود یا خیر
    public void init(Array<MapTrigger> triggers, Array<SpawnPoint> spawns) {
        if (!isDestroyed) {
            wallHealth = 3;
            darknessAlpha = 1.0f;
            brokenImageAlpha = 0.0f;
        } else {
            darknessAlpha = 0.0f;
            brokenImageAlpha = 1.0f;
        }

        hiddenRoomTrigger = null;
        hiddenDoorTrigger = null;
        eraseTrigger = null;
        brokenSpawnPoint = null;
        particles.clear();

        for (MapTrigger trigger : triggers) {
            if (trigger.t != null && trigger.t.equalsIgnoreCase("hidden_door")) {
                hiddenDoorTrigger = trigger;
            }
            if (trigger.type != null) {
                if (trigger.type.equalsIgnoreCase("hidden_room")) {
                    hiddenRoomTrigger = trigger;
                }
                if (trigger.type.equalsIgnoreCase("erase")) {
                    eraseTrigger = trigger;
                }
            }
        }

        for (SpawnPoint sp : spawns) {
            if (sp.spawnType != null && sp.spawnType.equalsIgnoreCase("broken")) {
                brokenSpawnPoint = sp;
            }
        }

        // اگر اتاق مخفی قبلاً باز شده بود، بلافاصله تایل‌ها را بدون افکت صوتی از روی نقشه حذف کن
        if (isDestroyed) {
            forceOpenRoomSilent();
        }
    }

    // بررسی برخورد نیل (شمشیر) شوالیه به دیوار، اعمال لرزش صفحه و کم کردن جان دیوار
    public void hitWall() {
        if (isDestroyed) return;

        AP.HollowKinght.model.player.Knight knight = GameController.getInstance().getKnightController() != null ?
            GameController.getInstance().getKnightController().knight : null;

        // رفلکشن کمکی در صورتی که کنترلر به هر دلیلی نال شده باشد
        if (knight == null) {
            try {
                java.lang.reflect.Field kField = GameController.class.getDeclaredField("knight");
                kField.setAccessible(true);
                knight = (AP.HollowKinght.model.player.Knight) kField.get(GameController.getInstance());
            } catch(Exception e) {}
        }

        if (knight != null) {
            knight.shakeTime = 0.25f;
            knight.shakeIntensity = 6.0f;
        }

        // تولید ذرات گرد و غبار بر اثر ضربه معمولی
        if (hiddenDoorTrigger != null) {
            for (int i = 0; i < 30; i++) {
                float px = hiddenDoorTrigger.x + MathUtils.random(0f, hiddenDoorTrigger.width);
                float py = hiddenDoorTrigger.y + MathUtils.random(0f, hiddenDoorTrigger.height);
                particles.add(new DustParticle(px, py));
            }
        }

        wallHealth--;

        // تخریب نهایی در صورت اتمام جان دیوار و ثبت اچیومنت شماره ۵
        if (wallHealth <= 0) {
            if (hiddenDoorTrigger != null) {
                for (int i = 0; i < 100; i++) {
                    float px = hiddenDoorTrigger.x + MathUtils.random(0f, hiddenDoorTrigger.width);
                    float py = hiddenDoorTrigger.y + MathUtils.random(0f, hiddenDoorTrigger.height);
                    particles.add(new DustParticle(px, py));
                }
            }
            if (knight != null && !knight.ach5){
                knight.ach5 = true;
            }

            triggerRoomDestruction();
        } else {
            AudioManager.getInstance().playSFX("breakable_wall_hit_2.wav");
        }
    }

    // باز کردن بی‌صدا و آنی اتاق (مفید برای بارگذاری بازی یا لود مجدد مپ‌ها)
    public void forceOpenRoomSilent() {
        this.isDestroyed = true;
        this.brokenImageAlpha = 1.0f;
        this.darknessAlpha = 0.0f;

        if (GameController.getInstance().getTiledMap() != null && eraseTrigger != null) {
            com.badlogic.gdx.maps.tiled.TiledMapTileLayer layer8 =
                (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) GameController.getInstance().getTiledMap().getLayers().get("Tile Layer 8");

            if (layer8 != null) {
                float tileW = layer8.getTileWidth();
                float tileH = layer8.getTileHeight();
                int startX = (int) (eraseTrigger.x / tileW);
                int startY = (int) (eraseTrigger.y / tileH);
                int endX = (int) ((eraseTrigger.x + eraseTrigger.width) / tileW);
                int endY = (int) ((eraseTrigger.y + eraseTrigger.height) / tileH);

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        layer8.setCell(x, y, null);
                    }
                }
                System.out.println("🔧 [Hidden Room Engine]: Synchronized map tiles cleared silently from Layer 8.");
            }
        }

        com.badlogic.gdx.utils.Array<MapTrigger> activeTriggers = GameController.getInstance().getMapTriggers();
        if (activeTriggers != null) {
            if (hiddenDoorTrigger != null) activeTriggers.removeValue(hiddenDoorTrigger, true);
            if (hiddenRoomTrigger != null) activeTriggers.removeValue(hiddenRoomTrigger, true);
        }
    }

    // فعال‌سازی فرآیند تخریب، پخش افکت صوتی انفجار دیوار و پاک کردن فیزیکی تایل‌ها از نقشه Tiled
    private void triggerRoomDestruction() {
        isDestroyed = true;
        AudioManager.getInstance().playSFX("breakable_wall_death.wav");

        if (GameController.getInstance().getTiledMap() != null && eraseTrigger != null) {
            TiledMapTileLayer layer8 = (TiledMapTileLayer) GameController.getInstance().getTiledMap().getLayers().get("Tile Layer 8");
            if (layer8 != null) {
                float tileW = layer8.getTileWidth();
                float tileH = layer8.getTileHeight();
                int startX = (int) (eraseTrigger.x / tileW);
                int startY = (int) (eraseTrigger.y / tileH);
                int endX = (int) ((eraseTrigger.x + eraseTrigger.width) / tileW);
                int endY = (int) ((eraseTrigger.y + eraseTrigger.height) / tileH);

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        layer8.setCell(x, y, null);
                    }
                }
            }
        }

        Array<MapTrigger> activeTriggers = GameController.getInstance().getMapTriggers();
        if (activeTriggers != null) {
            if (hiddenDoorTrigger != null) activeTriggers.removeValue(hiddenDoorTrigger, true);
            if (hiddenRoomTrigger != null) activeTriggers.removeValue(hiddenRoomTrigger, true);
        }
    }

    // بازگردانی وضعیت اتاق به حالت اولیه پیش‌فرض (مثلاً هنگام شروع بازی جدید)
    public void resetToDefaultState() {
        this.wallHealth = 3;
        this.isDestroyed = false;
        this.brokenImageAlpha = 0.0f;
        this.darknessAlpha = 1.0f;
    }

    // به‌روزرسانی فریم به فریم موقعیت ذرات غبار و چک تداوم حذف تایل‌ها پس از تعویض اسکرین
    public void update(float dt) {
        for (int i = particles.size - 1; i >= 0; i--) {
            DustParticle p = particles.get(i);
            p.update(dt);
            if (p.isDead()) {
                particles.removeIndex(i);
            }
        }

        if (isDestroyed) {
            if (GameController.getInstance().getTiledMap() != null && eraseTrigger != null) {
                TiledMapTileLayer layer8 = (TiledMapTileLayer) GameController.getInstance().getTiledMap().getLayers().get("Tile Layer 8");
                if (layer8 != null) {
                    float tileW = layer8.getTileWidth();
                    float tileH = layer8.getTileHeight();
                    int checkX = (int) (eraseTrigger.x / tileW);
                    int checkY = (int) (eraseTrigger.y / tileH);

                    if (layer8.getCell(checkX, checkY) != null) {
                        forceOpenRoomSilent();
                    }
                }
            }

            if (brokenImageAlpha < 1.0f) {
                brokenImageAlpha = 1.0f;
            }
            if (darknessAlpha > 0f) {
                darknessAlpha = 0f;
            }
        }
    }

    // رندر بافت‌های گرافیکی شکستگی دیوار، مستطیل سیاه تاریکی اتاق و سیستم ذرات با اعمال آلفا بلندینگ
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (brokenTexture == null) {
            loadWallTexture();
        }

        if (brokenSpawnPoint != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.end();
        }

        // رسم تصویر تکسچر شکستگی دیوار
        if (brokenSpawnPoint != null && brokenTexture != null && brokenImageAlpha > 0f) {
            batch.begin();
            float w = 180f;
            float h = 80f;
            batch.setColor(1, 1, 1, brokenImageAlpha);
            batch.draw(brokenTexture, brokenSpawnPoint.x - 75, brokenSpawnPoint.y - 68, w, h);
            batch.setColor(1, 1, 1, 1f);
            batch.end();
        }

        // رسم پرده تاریک‌کننده فضای داخل اتاق مخفی تا زمانی که پلیر تریگر را نزده است
        if (hiddenRoomTrigger != null && darknessAlpha > 0f) {
            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, darknessAlpha);
            shapeRenderer.rect(hiddenRoomTrigger.x, hiddenRoomTrigger.y, hiddenRoomTrigger.width, hiddenRoomTrigger.height);
            shapeRenderer.end();
            com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        }

        // چرخه رسم دایره‌های سیستم ذرات (خاک و گرد و غبار) با شکل هندسی پر شده
        if (particles.size > 0) {
            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (DustParticle p : particles) {
                shapeRenderer.setColor(p.r, p.g, p.b, p.alpha);
                shapeRenderer.circle(p.x, p.y, p.radius);
            }
            shapeRenderer.end();
            com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        }
    }

    public MapTrigger getHiddenRoomTrigger() { return hiddenRoomTrigger; }
}
