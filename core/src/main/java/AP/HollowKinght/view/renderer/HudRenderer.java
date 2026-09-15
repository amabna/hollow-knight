package AP.HollowKinght.view.renderer;

import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.audio.AudioManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class HudRenderer implements Disposable {
    // تعریف تکسچرهای ثابت رابط کاربری (HUD)
    private Texture libraryGoi;
    private Texture orbEmpty, orbFull, orbEye;
    private Texture maskFilled, maskEmpty;

    // آرایه انیمیشن خرد شدن ماسک‌های سلامتی
    private Texture[] maskBreakFrames;

    // فریم‌های ترنزیشن و انیمیشن مایع درونی گوی روح (Soul Orb)
    private Texture trans0;
    private Texture trans237;
    private Texture[] state2Frames; // فریم‌های حالت ۲۳۸ تا ۲۴۲
    private Texture trans243;
    private Texture[] state3Frames; // فریم‌های حالت ۲۴۴ تا ۲۴۸
    private Texture trans249;
    private Texture[] state4Frames; // فریم‌های حالت ۲۵۰ تا ۲۵۴
    private Texture trans255;

    // تایمرهای مدیریت زمان انیمیشن‌ها
    private float animationTimer = 0f;
    private final float FRAME_DURATION = 0.17f;
    private final float TRANSITION_DURATION = 0.25f;

    // متغیرهای کمکی برای تشخیص تغییرات مقدار روح و مدیریت افکت جابجایی فازها
    private int lastSoul = 0;
    private float transitionTimer = 0f;
    private Texture currentTransTexture = null;

    public HudRenderer() {
        // ۱. لود کردن بافت‌های ثابت و اصلی HUD
        libraryGoi = new Texture(Gdx.files.internal("HKassets/hud/goi.png"));
        orbEmpty = new Texture(Gdx.files.internal("HKassets/hud/SoulOrb_Empty.png"));
        orbFull = new Texture(Gdx.files.internal("HKassets/hud/full_soul.png"));
        orbEye = new Texture(Gdx.files.internal("HKassets/hud/SoulOrb_Eye.png"));
        maskFilled = new Texture(Gdx.files.internal("HKassets/hud/FilledHealth.png"));
        maskEmpty = new Texture(Gdx.files.internal("HKassets/hud/EmptyHealth.png"));

        // ۲. لود انیمیشن‌های شکستن ماسک جان (شامل ۶ فریم)
        maskBreakFrames = new Texture[6];
        for (int i = 0; i <= 5; i++) {
            maskBreakFrames[i] = new Texture(Gdx.files.internal("HKassets/hud/BreakHealth_00" + i + ".png"));
        }

        // ۳. لود فریم‌های واسط (Transition) و لوپ‌های پینگ‌پونگی آب داخل گوی روح
        trans0 = new Texture((Gdx.files.internal(("HKassets/hud/SoulOrb_Empty.png"))));
        trans237 = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_237.png"));
        trans243 = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_243.png"));
        trans249 = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_249.png"));
        trans255 = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_250.png"));

        state2Frames = new Texture[5];
        for (int i = 0; i < 5; i++) {
            state2Frames[i] = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_" + (238 + i) + ".png"));
        }

        state3Frames = new Texture[5];
        for (int i = 0; i < 5; i++) {
            state3Frames[i] = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_" + (244 + i) + ".png"));
        }

        state4Frames = new Texture[5];
        for (int i = 0; i < 5; i++) {
            state4Frames[i] = new Texture(Gdx.files.internal("HKassets/hud/HUD Cln_" + (250 + i) + ".png"));
        }
    }

    // متد رسم کل لایه HUD روی صفحه
    public void render(SpriteBatch batch, Knight knight, float delta) {
        // جهت انیمیشن گوی در زمان فوکوس معکوس می‌شود
        if (knight.currentState == Knight.State.FOCUSING) {
            animationTimer -= delta;
        } else {
            animationTimer += delta;
        }

        // --- منطق بررسی تغییر استیت روح و فعال‌سازی ترنزیشن‌ها ---
        int currentState = getSoulStateIndex(knight.soul);
        int previousState = getSoulStateIndex(lastSoul);

        if (currentState != previousState) {
            // فعال‌سازی تایمر ترنزیشن به محض تغییر فاز مقدار روح شوالیه
            transitionTimer = TRANSITION_DURATION;

            // اختصاص فریم ترنزیشن مناسب بر اساس مرزی که بازیکن از آن رد شده است
            if ((previousState == 1 && currentState == 2) || (previousState == 2 && currentState == 1)) {
                currentTransTexture = trans237;
            } else if ((previousState == 2 && currentState == 3) || (previousState == 3 && currentState == 2)) {
                currentTransTexture = trans243;
            } else if ((previousState == 3 && currentState == 4) || (previousState == 4 && currentState == 3)) {
                currentTransTexture = trans249;
            } else if ((previousState == 4 && currentState == 5) || (previousState == 5 && currentState == 4)) {
                currentTransTexture = trans255;
            } else {
                currentTransTexture = null;
            }

            // پخش صدای پر شدن روح در صورت وجود ترنزیشن معتبر
            if (currentTransTexture != null) {
                AudioManager.getInstance().playSFX("fulling_soul.wav");
            }
        }

        // کم کردن زمان تایمر ترنزیشن جاری
        if (transitionTimer > 0) {
            transitionTimer -= delta;
        }

        // --- رسم اجزای گوی روح ---
        float orbX = 20f;
        float orbY = Gdx.graphics.getHeight() - 150f;

        // رسم کادر فلزی و دکوراتیو دور گوی روح
        batch.draw(libraryGoi, orbX - 5f, orbY - 5f, 200f, 120f);

        // رسم مایع سفید درونی گوی متناسب با میزان روح فعلی
        Texture currentOrbFluid = getSoulOrbFluidTexture(knight.soul);
        if (currentOrbFluid != null) {
            batch.draw(currentOrbFluid, orbX - 39, orbY - 49, 180f, 180f);
        }

        // رسم جفت چشم‌های مشکی وسط گوی روح
        batch.draw(orbEye, orbX+21, orbY+12, 63f, 28f);

        // --- رسم پویا و انیمیشنی ۵ ماسک سلامتی کاراکتر ---
        float startMaskX = 160f;
        float maskY = Gdx.graphics.getHeight() - 80f;
        float maskSpacing = 55f;

        for (int i = 0; i < 5; i++) {
            float currentMaskX = startMaskX + (i * maskSpacing);
            int wMask = 100;
            int hMask = 120;
            float yMask = maskY - 77;
            float xMask = currentMaskX - 57;

            if (i < knight.health) {
                // اگر شوالیه این خط سلامتی را دارد، ماسک پر رسم شود
                batch.draw(maskFilled, xMask, yMask, wMask, hMask);
                knight.maskAnimationStates[i] = -1f; // ریست کردن تایمر انیمیشن شکستن
            } else {
                // اگر سلامتی از دست رفته، بررسی پخش انیمیشن خرد شدن ماسک
                if (knight.maskAnimationStates[i] >= 0) {
                    knight.maskAnimationStates[i] += delta;
                    int frameIndex = (int) (knight.maskAnimationStates[i] / FRAME_DURATION);

                    if (frameIndex < 6) {
                        batch.draw(maskBreakFrames[frameIndex], xMask, yMask, wMask , hMask);
                    } else {
                        batch.draw(maskEmpty, xMask, yMask, wMask,hMask);
                    }
                } else {
                    // در غیر این صورت فقط ماسک خالی خاموش رسم شود
                    batch.draw(maskEmpty, xMask, yMask, wMask, hMask);
                }
            }
        }

        // ذخیره مقدار روح برای مقایسه فاز در فریم بعدی بازی
        lastSoul = knight.soul;
    }

    // متد کمکی برای دسته‌بندی مقدار عددی روح به ۵ فاز مشخص
    private int getSoulStateIndex(int soul) {
        if (soul <= 19) return 1;
        if (soul <= 39) return 2;
        if (soul <= 59) return 3;
        if (soul <= 79) return 4;
        return 5;
    }

    // متد دریافت تکسچر متناسب مایع روح با اولویت دادن به فریم‌های ترنزیشن واسط
    private Texture getSoulOrbFluidTexture(int soul) {
        if (transitionTimer > 0 && currentTransTexture != null) {
            return currentTransTexture;
        }

        if (soul <= 19) return null;
        if (soul <= 39) return getPingPongFrame(state2Frames);
        if (soul <= 59) return getPingPongFrame(state3Frames);
        if (soul <= 79) return getPingPongFrame(state4Frames);
        return orbFull;
    }

    // تولید اندیس رفت و برگشتی (پینگ‌پونگی) برای نرم‌تر شدن انیمیشن موج آب داخل گوی
    private Texture getPingPongFrame(Texture[] frames) {
        int totalFrames = frames.length;
        int index = (int) (animationTimer / FRAME_DURATION);
        int loopLength = (totalFrames * 2) - 2;

        index = Math.abs(index % loopLength);
        if (index >= totalFrames) {
            index = loopLength - index;
        }
        return frames[index];
    }

    // آزادسازی تمام تکسچرهای لود شده در HUD از حافظه رم گرافیک
    @Override
    public void dispose() {
        libraryGoi.dispose();
        orbEmpty.dispose();
        orbFull.dispose();
        orbEye.dispose();
        maskFilled.dispose();
        maskEmpty.dispose();
        for (Texture t : maskBreakFrames) t.dispose();
        trans0.dispose();
        trans237.dispose();
        trans243.dispose();
        trans249.dispose();
        trans255.dispose();
        for (Texture t : state2Frames) t.dispose();
        for (Texture t : state3Frames) t.dispose();
        for (Texture t : state4Frames) t.dispose();
    }
}
