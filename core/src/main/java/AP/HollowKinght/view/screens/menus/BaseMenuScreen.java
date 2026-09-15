package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.screens.AbstractScreen;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.loaders.MenuAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

// این کلاس پایه‌ای برای تمام منوهای بازی است و ویژگی‌های مشترک مثل پس‌زمینه و ذرات معلق را مدیریت می‌کند
public abstract class BaseMenuScreen extends AbstractScreen {
    // جدولی که منوهای فرزند المان‌های خود (دکمه‌ها و هدرها) را داخل آن می‌چینند
    protected Table mainTable;

    // ابزارهای سیستم ذرات معلق (باران افکت در منوی اصلی)
    private ShapeRenderer shapeRenderer;
    private float[] particleX, particleY, particleSpeed, particleSize;
    private final int PARTICLE_COUNT = 70; // تعداد کل ذرات روی صفحه

    // --- کلاس داخلی و کمکی برای ساخت تصاویر انیمیشنی لایه UI منوها ---
    protected static class AnimatedImage extends Image {
        private final Animation<TextureRegion> animation;
        private float stateTime = 0; // زمان سپری شده برای انیمیشن جاری

        public AnimatedImage(Animation<TextureRegion> animation) {
            super(animation.getKeyFrame(0)); // تنظیم فریم اول به صورت پیش‌فرض
            this.animation = animation;
        }

        // ریست کردن زمان به فریم آغازین
        public void resetAnimation() { this.stateTime = 0; }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta; // جلو بردن زمان انیمیشن
            // جایگذاری فریم جاری متناسب با زمان سپری شده روی کامپوننت تصویر
            ((TextureRegionDrawable) getDrawable()).setRegion(animation.getKeyFrame(stateTime));
        }
    }

    public BaseMenuScreen() {
        super();

        // --- راه‌اندازی و تولید سیستم ذرات معلق (باران منو) ---
        shapeRenderer = new ShapeRenderer();
        particleX = new float[PARTICLE_COUNT];
        particleY = new float[PARTICLE_COUNT];
        particleSpeed = new float[PARTICLE_COUNT];
        particleSize = new float[PARTICLE_COUNT];

        // ساخت موقعیت و مشخصات تصادفی اولیه برای ذرات
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particleX[i] = MathUtils.random(0, Gdx.graphics.getWidth());
            particleY[i] = MathUtils.random(0, Gdx.graphics.getHeight());
            particleSpeed[i] = MathUtils.random(20f, 50f); // سرعت سقوط ذره
            particleSize[i] = MathUtils.random(1.5f, 5.5f); // شعاع ذره
        }
    }

    @Override
    public void buildStage() {
        // پاکسازی کامل استیج برای جلوگیری از روی هم افتادن منوها در حافظه
        mainStack.clearChildren();

        // ۱. مدیریت و تنظیم خودکار پس‌زمینه ثابت برای تمام منوها (Void Heart BG)
        Texture bgTexture = GameAssetManager.getInstance().getTexture(MenuAssets.VOIDHEART_BG);
        if (bgTexture != null) {
            Image bgImage = new Image(bgTexture);
            bgImage.setFillParent(true); // اجبار به پر کردن کل ابعاد رزولوشن صفحه
            mainStack.addActor(bgImage);
        }

        // ۲. ساخت جدول اصلی مرکز صفحه (منوهای فرزند المان‌های خود را داخل این می‌ریزند)
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainStack.addActor(mainTable);

        // ۳. صدا زدن متد انتزاعی فرزند جهت چیدمان دکمه‌ها و هدرهای اختصاصی
        initMenu();
    }

    // هر منویی که از این کلاس ارث ببرد، باید این متد را برای پر کردن دکمه‌ها پیاده‌سازی کند
    protected abstract void initMenu();

    @Override
    public void render(float delta) {
        // ---- اعمال روشنایی سرتاسری منوها قبل از رسم المان‌های استیج ----
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        float brightness = prefs.getFloat("brightnessAlpha", 1.0f); // مقدار پیش‌فرض ۱.۰ در صورت عدم وجود تنظیمات

        if (stage != null && stage.getBatch() != null) {
            // تنظیم میزان روشنایی رنگ رندر استیج
            stage.getBatch().setColor(brightness, brightness, brightness, 1f);
        }
        // ----------------------------------------------------

        super.render(delta); // رندر کردن لایه‌ی اصلی استیج و صحنه دکمه‌ها

        // فعال‌سازی قابلیت آلفا بلِندینگ (Alpha Blending) برای مدیریت شفافیت ذرات
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        // آپدیت موقعیت، رندرسازی و حرکت موجی ذرات معلق منو
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // اعمال ضریب روشنایی تنظیمات روی شفافیت کلی ذرات
        shapeRenderer.setColor(1, 1, 1, 0.35f * brightness);

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particleY[i] -= particleSpeed[i] * delta; // حرکت مستقیم به سمت پایین صفحه
            particleX[i] += MathUtils.sin(particleY[i] * 0.02f) * 10f * delta; // اضافه کردن نوسان موجی به چپ و راست

            // اگر ذره از مرز پایین صفحه خارج شد، مجدداً از سقف صفحه با موقعیت تصادفی جدید متولد شود
            if (particleY[i] < 0) {
                particleY[i] = Gdx.graphics.getHeight();
                particleX[i] = MathUtils.random(0, Gdx.graphics.getWidth());
            }
            // رسم نهایی دایره ذره جاری
            shapeRenderer.circle(particleX[i], particleY[i], particleSize[i]);
        }
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        // آزاد سازی حافظه رندر خطوط و اشکال هندسی برای جلوگیری از نشت حافظه گرافیکی
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
