package AP.HollowKinght.view.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import AP.HollowKinght.model.enemy.Zote;
import AP.HollowKinght.controller.core.DialogueManager;

public class ZoteRenderer {
    // تعریف انیمیشن‌های کاراکتر Zote و متغیر زمان
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> attackAnimation;
    private float stateTime = 0f;

    // تعریف فونت‌ها و باکس‌های متنی UI دیالوگ
    public BitmapFont font;
    private BitmapFont eFont;
    private Texture dialogueBoxTexture;
    private Texture buttonETexture;

    public ZoteRenderer() {
        // ایجاد فونت اختصاصی با اندازه مناسب ۴۰ برای متن‌های دیالوگ بازی
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = 40;
        p.color = Color.WHITE;
        font = gen.generateFont(p);

        // ایجاد یک فونت بزرگتر سایه‌دار اختصاصی برای دکمه تعامل E
        FreeTypeFontParameter eP = new FreeTypeFontParameter();
        eP.size = 100;
        eP.color = Color.WHITE;
        eP.shadowColor = new Color(0, 0, 0, 0.5f);
        eP.shadowOffsetX = 2;
        eP.shadowOffsetY = 2;
        eFont = gen.generateFont(eP);
        gen.dispose(); // حذف جنریتور پس از اتمام ساخت فونت‌ها برای بهینه‌سازی حافظه

        // بارگذاری ۱۲ فریم انیمیشن حالت سکون زوت (Idle)
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 11; i++) {
            String num = String.format("%03d", i);
            Texture tex = new Texture(Gdx.files.internal("HKassets/zote/idle_" + num + ".png"));
            idleFrames.add(new TextureRegion(tex));
        }
        idleAnimation = new Animation<>(0.15f, idleFrames, Animation.PlayMode.LOOP);

        // بارگذاری ۴ فریم انیمیشن حمله زوت (Attack)
        Array<TextureRegion> attackFrames = new Array<>();
        for (int i = 0; i <= 3; i++) {
            String num = String.format("%03d", i);
            Texture tex = new Texture(Gdx.files.internal("HKassets/zote/Attack_" + num + ".png"));
            attackFrames.add(new TextureRegion(tex));
        }
        attackAnimation = new Animation<>(0.2f, attackFrames, Animation.PlayMode.LOOP);

        // ساخت داینامیک باکس دیالوگ سرمه‌ای رنگ و دکمه E حاشیه‌دار با Pixmap در کد
        dialogueBoxTexture = createRoundedBox(800, 180, 20, new Color(0.271f, 0.514f, 0.549f, 1.0f));
        buttonETexture = createBorderedRoundedBox(90, 90, 15, new Color(0.988f, 0.996f, 1.0f, 1.0f), new Color(0f, 0f, 0f, 0.9f), 5);
    }

    // متد کمکی ترسیم یک مستطیل با گوشه‌های گرد با استفاده از Pixmap
    private Texture createRoundedBox(int width, int height, int radius, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    // متد ساخت باکس حاشیه‌دار دو رنگ (پرکننده داخلی + کادر بیرونی) برای دکمه E
    private Texture createBorderedRoundedBox(int width, int height, int radius, Color fillColor, Color borderColor, int borderThickness) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // ابتدا باکس بزرگتر بیرونی را با رنگ حاشیه رسم می‌کنیم
        pixmap.setColor(borderColor);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        // سپس باکس داخلی کوچکتری روی آن برای شبیه‌سازی کادر می‌کشیم
        pixmap.setColor(fillColor);
        int innerW = width - 2 * borderThickness;
        int innerH = height - 2 * borderThickness;
        int innerR = radius - borderThickness > 0 ? radius - borderThickness : 0;
        int off = borderThickness;

        pixmap.fillRectangle(off, off + innerR, innerW, innerH - 2 * innerR);
        pixmap.fillRectangle(off + innerR, off, innerW - 2 * innerR, innerH);
        if (innerR > 0) {
            pixmap.fillCircle(off + innerR, off + innerR, innerR);
            pixmap.fillCircle(off + innerW - innerR, off + innerR, innerR);
            pixmap.fillCircle(off + innerR, off + innerH - innerR, innerR);
            pixmap.fillCircle(off + innerW - innerR, off + innerH - innerR, innerR);
        }

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    // رندر خود کاراکتر زوت در مپ بازی
    public void render(SpriteBatch batch, Zote zote, float delta) {
        stateTime += delta;
        TextureRegion currentFrame;

        // انتخاب انیمیشن حمله یا عادی بر اساس وضعیت هوش مصنوعی (aiState)
        if (zote.aiState == 1) {
            currentFrame = attackAnimation.getKeyFrame(stateTime);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime);
        }

        // اصلاح جهت چرخش عکس بر اساس فیس زوت به چپ یا راست
        if (zote.facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (!zote.facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        batch.draw(currentFrame, zote.x, zote.y, zote.width, zote.height);
    }

    // رندر المان‌های لایه UI تعامل و متن دیالوگ زوت روی مانیتور
    public void renderUI(SpriteBatch batch, Zote zote, boolean showPromptE, com.badlogic.gdx.graphics.OrthographicCamera camera) {
        // مدیریت افکت ظاهر شدن نوسانی دکمه E بالای سر زوت در نزدیکی شوالیه
        if (showPromptE && !DialogueManager.getInstance().isDialogueActive()) {

            // تبدیل مختصات دو بعدی ثابت جهان مپ بازی به پیکسل‌های لایه HUD مانیتور دکمه
            com.badlogic.gdx.math.Vector3 worldPos = new com.badlogic.gdx.math.Vector3(zote.spawnX + (zote.width / 2f), zote.spawnY + zote.height + 25f, 0);
            camera.project(worldPos);

            float btnX = worldPos.x - 24f;
            float btnY = worldPos.y;

            // ساخت یک آلفای نوسانی برای افکت چشمک‌زن ملایم و جذاب
            float alpha = (com.badlogic.gdx.math.MathUtils.sin(stateTime * 3f) + 1f) / 2f * 0.5f + 0.5f;
            batch.setColor(1, 1, 1, alpha);

            // رسم باکس گرافیکی دکمه E
            if (buttonETexture != null) {
                batch.draw(buttonETexture, btnX, btnY);
            }

            // نوشتن کلمه متن "E" در مرکز دقیق باکس دکمه
            if (eFont != null) {
                eFont.setColor(1, 1, 1, alpha);
                eFont.draw(batch, "E", btnX + 22f, btnY + 74f);
            }

            batch.setColor(1, 1, 1, 1); // برگرداندن آلفای اصلی بچ به وضعیت کامل
        }

        // رسم باکس بزرگ پایینی نمایش زیرنویس‌ها در صورت فعال بودن دیالوگ
        if (DialogueManager.getInstance().isDialogueActive()) {
            float boxW = 800f;
            float boxH = 180f;
            float boxX = (Gdx.graphics.getWidth() - boxW) / 2f;
            float boxY = 40f;

            batch.draw(dialogueBoxTexture, boxX, boxY, boxW, boxH);

            // عنوان گوینده دیالوگ
            font.setColor(Color.BLUE);
            font.draw(batch, "Zote The Mighty", boxX + 30f, boxY + boxH - 25f);

            // متن اصلی صحبت بر اساس زبان انتخابی پروژه
            String txt = DialogueManager.getInstance().getCurrentDisplayLanguageText();
            font.setColor(Color.WHITE);
            font.draw(batch, txt, boxX + 30f, boxY + boxH - 70f, boxW - 60f, -1, true);

            font.setColor(Color.GRAY);
        }
    }

    // متد پاکسازی فونت‌ها و بافت‌های ایجاد شده از حافظه رم گرافیک
    public void dispose() {
        if (dialogueBoxTexture != null) dialogueBoxTexture.dispose();
        if (buttonETexture != null) buttonETexture.dispose();
        if (font != null) font.dispose();
        if (eFont != null) eFont.dispose();
    }
}
