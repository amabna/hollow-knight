package AP.HollowKinght.view.screens;

import AP.HollowKinght.view.managers.UIManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// این کلاس انتزاعی والد تمام اسکرین‌ها و لول‌های اصلی و فرعی کل پروژه است
public abstract class AbstractScreen implements Screen {
    // استیج اصلی سیستم صحنه برای مدیریت اکتورها و دریافت رویدادهای ورودی
    protected Stage stage;

    // لایه‌ها و پشته‌های جدولی مختلف چیدمان UI برای دسته‌بندی مرتب المان‌ها
    protected Table mainStack;
    protected Table coverStack;
    protected Table noteStack;

    // ---- متغیرها و وضعیت‌های مدیریت فید انیمیشن تصویری فواصل صفحات ----
    public enum FadeState { IN, OUT, NONE }
    private FadeState fadeState = FadeState.IN; // هر صفحه در ابتدا حالت ظهور تدریجی (Fade In) دارد
    private float fadeAlpha = 1.0f; // شروع با آلفای کاملاً مشکی و تاریک
    private final float fadeSpeed = 1.0f / 1.5f;  // تنظیم بازه زمانی دقیقاً ۱.۵ ثانیه برای ترنزیشن‌ها
    private AbstractScreen nextScreen = null; // اسکرین هدف بعدی پس از اتمام فید آوت تاریک

    public AbstractScreen() {
        stage = new Stage(new ScreenViewport());

        mainStack = new Table();
        coverStack = new Table();
        noteStack = new Table();

        // مجبور کردن پشته‌ها به پر کردن کامل ابعاد صفحه والد
        mainStack.setFillParent(true);
        coverStack.setFillParent(true);
        noteStack.setFillParent(true);

        // افزودن لایه‌ها به استیج بر اساس ترتیب اولویت رندر (پایین به بالا)
        stage.addActor(mainStack);
        stage.addActor(coverStack);
        stage.addActor(noteStack);
    }

    // متدی که کلاس‌های فرزند باید برای ساخت و اضافه کردن المان‌های گرافیکی خود پیاده‌سازی کنند
    public abstract void buildStage();

    // آغاز فرآیند تاریک شدن تدریجی (Fade Out) صفحه فعلی و آماده‌سازی جهت سوییچ نهایی به لول بعدی
    public void startFadeOutTo(AbstractScreen targetScreen) {
        if (this.fadeState == FadeState.OUT) return;
        this.nextScreen = targetScreen;
        this.fadeState = FadeState.OUT; // تغییر فاز به حالت خروج تدریجی تاریک
    }

    @Override
    public void render(float delta) {
        // بروزرسانی رفتار زمانی و سپس رسم تمام المان‌های فعال روی استیج
        stage.act(delta);
        stage.draw();
    }

    // مدیریت پویای رندر افکت فید لایه‌ی مشکی انتقالی (توسط لول‌های فرزند در پایان متد رندر فراخوانی می‌شود)
    protected void handleScreenFade(ShapeRenderer shapeRenderer, float delta) {
        if (fadeState == FadeState.IN) {
            // کم کردن غلظت سیاهی جهت نمایان شدن تدریجی صفحه بازی
            fadeAlpha -= fadeSpeed * delta;
            if (fadeAlpha <= 0) {
                fadeAlpha = 0f;
                fadeState = FadeState.NONE; // اتمام انیمیشن ظهور اولیه
            }
        } else if (fadeState == FadeState.OUT) {
            // زیاد کردن غلظت سیاهی صفحه تا مرز تاریکی کامل
            fadeAlpha += fadeSpeed * delta;
            if (fadeAlpha >= 1.0f) {
                fadeAlpha = 1.0f;
                // صفحه کاملاً تاریک شد؛ حالا انتقال نهایی بدون فید توسط UIManager صورت می‌گیرد
                if (nextScreen != null) {
                    UIManager.getInstance().changeScreenDirectly(nextScreen);
                    nextScreen = null; // ریست کردن مرجع برای جلوگیری از نشت حافظه و باگ تکرار سوییچ
                }
            }
        }

        // رسم مستطیل مشکی فید روی کل ابعاد رزولوشن پنجره بازی
        if (fadeAlpha > 0f) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, fadeAlpha);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // تنظیم ابعاد ویوپورت استیج متناسب با تغییر اندازه دستی پنجره بازی توسط کاربر
        stage.getViewport().update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // تخلیه کامل منابع استیج از حافظه رم و گرافیک
        stage.dispose();
    }

    // بارگذاری، ساخت و تغییر شکل گرافیکی نشانگر ماوس به آیکون کاستوم و شیک هالو نایت (Selection Cursor)
    private void setCustomCursor() {
        try {
            com.badlogic.gdx.files.FileHandle cursorFile = Gdx.files.internal(AP.HollowKinght.view.loaders.MenuAssets.INV_0014_SELECTION_CURSOR);
            if (cursorFile.exists()) {
                Pixmap originalPixmap = new Pixmap(cursorFile);
                int cursorSize = 32; // مقیاس ابعاد استاندارد برای پیکس‌مپ ماوس
                Pixmap resizedPixmap = new Pixmap(cursorSize, cursorSize, originalPixmap.getFormat());
                resizedPixmap.setFilter(Pixmap.Filter.BiLinear); // اعمال فیلتر دوخطی برای نرمی لبه‌های کرسر جدید
                resizedPixmap.drawPixmap(originalPixmap, 0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(), 0, 0, resizedPixmap.getWidth(), resizedPixmap.getHeight());

                Cursor customCursor = Gdx.graphics.newCursor(resizedPixmap, 0, 0);
                Gdx.input.setCursorCatched(false);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(new Pixmap(1, 1, Pixmap.Format.RGBA8888), 0, 0));

                originalPixmap.dispose();
                resizedPixmap.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("CursorError", "خطا در ساخت موس سفارشی: " + e.getMessage());
        }
    }

    // متغیر گلوبال نگهداری آلفای درخشندگی تنظیمات نور محیطی بازی
    public static float brightness = 1.0f;

    // متد اعمال فیلتر درخشندگی سرتاسری بازی (رسم پرده تیره متناسب با تاریکی تعیین شده در آپشنز)
    protected void applyBrightness(ShapeRenderer shapeRenderer) {
        if (brightness < 1.0f) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // رنگ آلفا بر اساس فرمول معکوس ضریب براتنس تنظیم می‌شود
            shapeRenderer.setColor(0, 0, 0, 1.0f - brightness);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
        }
    }

    // متد گتر برای دسترسی کلاس‌های کنترلر به شی استیج لایه گرافیکی
    public Stage getStage() { return stage; }
}
