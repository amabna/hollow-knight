package AP.HollowKinght.controller.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.managers.LanguageManager;

public class InventoryManager {
    private static InventoryManager instance;

    public boolean isMenuOpen = false;

    private Texture[] charmTextures;
    private Texture notchTexture;
    private String[] charmFileNames;

    private Rectangle[] charmBounds;
    private Rectangle[] notchBounds;

    // ۳ جایگاه ناچ: مقدار ۱- یعنی خالی، یا مقادیر ۰ تا ۷ که ایندکس چارم مجهز شده است
    public int[] equippedCharms = {-1, -1, -1};

    private BitmapFont font;
    private BitmapFont font1;

    private int hoveredCharmIndex = -1;

    private InventoryManager() {
        // نام دقیق فایل‌ها با رعایت بزرگ و کوچکی حروف جهت ست کردن تصاویر چارم‌ها
        charmFileNames = new String[]{
            "Soul Catcher", "Dashmaster", "Unbreakable Strength", "Quick Slash",
            "Quick Focus", "Heavy Blow", "Sharp Shadow", "Void Heart"
        };

        charmTextures = new Texture[8];
        for (int i = 0; i < 8; i++) {
            try {
                charmTextures[i] = new Texture("HKassets/charm/" + charmFileNames[i] + ".png");
            } catch (Exception e) {
                Gdx.app.log("InventoryManager", "Texture missing: " + charmFileNames[i]);
            }
        }

        try {
            notchTexture = new Texture("HKassets/charm/Kingsoul - charm_white_full.png");
        } catch (Exception e) {
            Gdx.app.log("InventoryManager", "Kingsoul notch texture missing!");
        }

        // مقداردهی فونت Perpetua Bold سفارشی برای بخش متن‌های اینونتوری و هاد
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 23;
            p.color = Color.WHITE;
            FreeTypeFontGenerator.FreeTypeFontParameter p1 = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p1.size = 70;
            p.color = Color.GRAY;
            font = gen.generateFont(p);
            font1 = gen.generateFont(p1);
            gen.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
        }

        setupLayout();
    }

    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    // ایجاد باند فیزیکی و مختصات قرارگیری مستطیل‌های چارم و ناچ‌ها روی منوی گرافیکی
    private void setupLayout() {
        charmBounds = new Rectangle[8];
        notchBounds = new Rectangle[3];

        float startX = 710f;
        float startY = 680f;
        float spacingX = 140f;
        float spacingY = 120f;

        // چیدمان دقیق ۸ چارم در ۲ ردیف زیر هم (هر ردیف ۴ چارم)
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            charmBounds[i] = new Rectangle(startX + col * spacingX, startY - row * spacingY, 80f, 80f);
        }

        // چیدمان ۳ جایگاه ناچ (Notch) دقیقاً در قسمت زیرین بخش چارم‌ها
        float notchStartX = 752f;
        float notchY = 433f;
        float notchSpacing = 160f;
        for (int i = 0; i < 3; i++) {
            notchBounds[i] = new Rectangle(notchStartX + i * notchSpacing, notchY, 80f, 80f);
        }
    }

    // هندل کردن کلید باز شدن اینونتوری (I)، کلیک چپ روی آیتم‌ها و بررسی وضعیت هاور ماوس
    public void handleInput(Knight knight) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            isMenuOpen = !isMenuOpen;
            AudioManager.getInstance().playSFX("oc.wav");

            // متوقف کردن سرعت و فیزیک شوالیه در زمان باز بودن منوی بازی
            if (isMenuOpen) {
                knight.velocityX = 0;
                knight.velocityY = 0;
            }
        }

        if (!isMenuOpen) return;

        float mouseX = Gdx.input.getX();
        // تبدیل کردن سیستم مختصات Y موس به حالت پایین به بالا سازگار با فریم‌ورک LibGDX
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // ۱. بررسی کلیک روی تصویر چارم‌ها برای فعال‌سازی و قرار دادن در ناچ‌های خالی
            for (int i = 0; i < 8; i++) {
                if (charmBounds[i].contains(mouseX, mouseY)) {
                    // چارم Void Heart (ایندکس ۷) تا قبل از شکستن دیوار مخفی کاملاً قفل و غیرقابل انتخاب است
                    if (i == 7 && !HiddenRoomManager.getInstance().isDestroyed) {
                        return;
                    }
                    tryEquipCharm(i, knight);
                    return;
                }
            }

            // ۲. کلیک روی ناچ مجهز شده جهت تخلیه کردن چارم و بازگشت به حالت خالی (Kingsoul)
            for (int i = 0; i < 3; i++) {
                if (notchBounds[i].contains(mouseX, mouseY)) {
                    if (equippedCharms[i] != -1) {
                        int removedIndex = equippedCharms[i];
                        equippedCharms[i] = -1;
                        applyCharmEffectsLogic(removedIndex, false, knight);
                        AudioManager.getInstance().playSFX("select.wav");
                        return;
                    }
                }
            }
        }

        // تشخیص هاور ماوس روی آیکون‌ها برای خواندن و نمایش توضیحات داینامیک چندزبانه
        hoveredCharmIndex = -1;
        for (int i = 0; i < 8; i++) {
            if (charmBounds[i].contains(mouseX, mouseY)) {
                if (i != 7 || HiddenRoomManager.getInstance().isDestroyed) {
                    hoveredCharmIndex = i;
                }
                break;
            }
        }
    }

    // متد مجهز کردن چارم در اولین اسلات خالی موجود (با سقف ۳ ناچ فعال)
    private void tryEquipCharm(int charmIndex, Knight knight) {
        // جلوگیری از مجهز کردن یا اعمال اثر تکراری یک چارم
        for (int i = 0; i < 3; i++) {
            if (equippedCharms[i] == charmIndex) return;
        }

        for (int i = 0; i < 3; i++) {
            if (equippedCharms[i] == -1) {
                equippedCharms[i] = charmIndex;
                applyCharmEffectsLogic(charmIndex, true, knight);
                AudioManager.getInstance().playSFX("select.wav");
                break;
            }
        }
    }

    // اعمال یا لغو منطق و پرچم توانمندی‌های هر چارم روی مدل اصلی شوالیه
    public void applyCharmEffectsLogic(int index, boolean activate, Knight knight) {
        switch (index) {
            case 0: knight.hasSoulCatcher = activate; break;
            case 1: knight.hasDashmaster = activate; break;
            case 2: knight.hasUnbreakableStrength = activate; break;
            case 3: knight.hasQuickSlash = activate; break;
            case 4: knight.hasQuickFocus = activate; break;
            case 5: knight.hasHeavyBlow = activate; break;
            case 6: knight.hasSharpShadow = activate; break;
            case 7: knight.hasVoidHeart = activate; break;
        }
    }

    // رندر پس‌زمینه خاکستری دودی با لبه‌های گرد، رسم آیکون‌ها و کادربندی دور چارم‌های مجهز شده
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Knight knight) {
        if (!isMenuOpen) return;

        // ۱. رندر پس‌زمینه رنگ خاکستری با گوشه‌های گرد شده (Rounded Corners)
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // تنظیم رنگ خاکستری دودی شیک با آلفای 0.85 (RGB: 0.25, 0.25, 0.25)
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 0.85f);

        float rectX = 535f;
        float rectY = 287f;
        float rectW = 850f;
        float rectH = 657f;
        float radius = 15f;

        // رسم مستطیل‌های داخلی برای پر کردن بدنه اصلی پس‌زمینه منو
        shapeRenderer.rect(rectX + radius, rectY, rectW - 2f * radius, rectH);
        shapeRenderer.rect(rectX, rectY + radius, rectW, rectH - 2f * radius);

        // رسم ۴ دایره در گوشه‌ها برای ایجاد لبه‌های کاملاً گرد و نرم منو
        shapeRenderer.circle(rectX + radius, rectY + radius, radius);
        shapeRenderer.circle(rectX + rectW - radius, rectY + radius, radius);
        shapeRenderer.circle(rectX + radius, rectY + rectH - radius, radius);
        shapeRenderer.circle(rectX + rectW - radius, rectY + rectH - radius, radius);

        shapeRenderer.end();

        // ۲. رندر دایره سفید رنگ به دور چارم‌های فعال و ناچ شده (Equipped Effect)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < 8; i++) {
            boolean isEquipped = false;
            for (int eq : equippedCharms) {
                if (eq == i) isEquipped = true;
            }
            if (isEquipped) {
                Rectangle r = charmBounds[i];
                shapeRenderer.circle(r.x + r.width / 2f, r.y + r.height / 2f, r.width / 2f + 6f);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        // ۳. رندر تصاویر چارم‌ها، عنوان منو و اسلات‌های ناچ پایینی
        batch.begin();
        for (int i = 0; i < 8; i++) {
            Rectangle r = charmBounds[i];
            if (i == 7 && !HiddenRoomManager.getInstance().isDestroyed) {
                // رندر سیاه و سفید کمرنگ برای وید هرت (Void Heart) در صورت باز نشدن درب مخفی اتاق
                batch.setColor(0.2f, 0.2f, 0.2f, 0.4f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }
            if (charmTextures[i] != null) {
                batch.draw(charmTextures[i], r.x, r.y, r.width, r.height);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f); // ریست کردن مقدار شفافیت آلفا بچ
        font1.draw(batch, "INVENTORY", 782f, 862f);

        // رندر گرافیکی ناچ‌های پایینی صفحه (نمایش تکسچر پیش‌فرض یا آیکون چارم مجهز شده)
        for (int i = 0; i < 3; i++) {
            Rectangle r = notchBounds[i];
            int eqIndex = equippedCharms[i];
            if (eqIndex == -1) {
                if (notchTexture != null) batch.draw(notchTexture, r.x, r.y, r.width, r.height);
            } else {
                if (charmTextures[eqIndex] != null) batch.draw(charmTextures[eqIndex], r.x, r.y, r.width, r.height);
            }
        }

        // ۴. نمایش توضیحات مختصر چندزبانه از فایل‌های باندل زبان بر اساس ساختار پروژه (.properties)
        if (hoveredCharmIndex != -1) {
            String nameKey = "charm_" + hoveredCharmIndex + "_name";
            String descKey = "charm_" + hoveredCharmIndex + "_desc";
            String charmName = LanguageManager.bundle.get(nameKey);
            String charmDesc = LanguageManager.bundle.get(descKey);
            font.draw(batch, charmName + ": " + charmDesc, 583f, 378f);
        }
        batch.end();
    }
}
