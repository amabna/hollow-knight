package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.SaveManager;
import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import AP.HollowKinght.view.loaders.AchievementAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;

public class AchievementsMenu extends BaseMenuScreen {

    // تعریف فونت‌ها و استایل‌های دکمه برای متون مختلف منوی دستاوردها
    private BitmapFont headerBigFont;
    private BitmapFont headerSmallFont;
    private BitmapFont titleFont, descFont;
    private TextButton.TextButtonStyle buttonStyle;

    // انیمیشن پوینتر متحرک منو
    private Animation<TextureRegion> pointerAnimation;
    private Array<Table> rowTables = new Array<>();
    private Preferences prefs;

    public AchievementsMenu() {
        super();

        prefs = Gdx.app.getPreferences("HollowKnightSettings");

        // ۱. ساخت و استایل‌دهی داینامیک تمام فونت‌های منو با FreeType
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // تنظیم فونت بزرگ برای تک حرف اول هدر
        parameter.size = 110;
        parameter.color = Color.WHITE;
        parameter.borderColor = new Color(1f, 1f, 1f, 0.85f);
        parameter.borderWidth = 3.5f;
        parameter.shadowColor = new Color(1f, 1f, 1f, 0.5f);
        headerBigFont = generator.generateFont(parameter);

        // تنظیم فونت کوچک برای ادامه کلمه هدر
        parameter.size = 54;
        parameter.borderWidth = 2.2f;
        headerSmallFont = generator.generateFont(parameter);

        // تنظیم فونت برای عنوان دستاوردها
        parameter.size = 32;
        parameter.borderWidth = 0;
        parameter.shadowColor = new Color(0, 0, 0, 0);
        titleFont = generator.generateFont(parameter);

        // تنظیم فونت برای توضیحات دستاوردها
        parameter.size = 20;
        parameter.color = Color.WHITE;
        descFont = generator.generateFont(parameter);

        generator.dispose(); // حذف فوری ژنراتور جهت جلوگیری از نشت حافظه

        // ۲. تنظیم استایل دکمه‌های متنی لایه گرافیکی
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = titleFont;
        buttonStyle.fontColor = new Color(1, 1, 1, 0.5f);
        buttonStyle.overFontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;

        // ۳. ساخت لیست فریم‌های پوینتر نشانگر گزینه‌ها
        String[] pointerFrames = {
            MenuAssets.MAIN_MENU_POINTER_ANIM0000, MenuAssets.MAIN_MENU_POINTER_ANIM0001,
            MenuAssets.MAIN_MENU_POINTER_ANIM0002, MenuAssets.MAIN_MENU_POINTER_ANIM0003,
            MenuAssets.MAIN_MENU_POINTER_ANIM0004, MenuAssets.MAIN_MENU_POINTER_ANIM0005,
            MenuAssets.MAIN_MENU_POINTER_ANIM0006, MenuAssets.MAIN_MENU_POINTER_ANIM0007,
            MenuAssets.MAIN_MENU_POINTER_ANIM0008, MenuAssets.MAIN_MENU_POINTER_ANIM0009,
            MenuAssets.MAIN_MENU_POINTER_ANIM0010
        };
        pointerAnimation = AnimationFactory.createAnimation(pointerFrames, 0.015f, Animation.PlayMode.NORMAL);
    }

    // متد مقداردهی و چیدمان اجزای منوی دستاوردها روی استیج
    @Override
    protected void initMenu() {
        rowTables.clear();

        // جدا کردن حرف اول به صورت هوشمند برای استایل زیبای دراپ‌کپ (Drop Cap) هدر دوزبانه
        String achText = LanguageManager.bundle.get("menu_achievements");
        Table headerTable = new Table();
        Label.LabelStyle bigStyle = new Label.LabelStyle(headerBigFont, Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(headerSmallFont, Color.WHITE);

        Table headerTextTable = new Table();

        Label labelA = new Label(String.valueOf(achText.charAt(0)), bigStyle);
        Label labelChievements = new Label(achText.substring(1), smallStyle);

        headerTextTable.add(labelA).bottom().padRight(4f);
        headerTextTable.add(labelChievements).bottom().padBottom(14f);

        headerTable.add(headerTextTable).colspan(4).center().row();

        // رسم خط تزیینی زیر عنوان هدر (Fleur)
        Texture fleurTex = GameAssetManager.getInstance().getTexture(MenuAssets.GAME_OVER_FLEUR);
        if (fleurTex != null) {
            Image fleurImage = new Image(fleurTex);
            headerTable.add(fleurImage).width(1100).height(30).padTop(25).padBottom(45).colspan(4).row();
        }

        mainTable.add(headerTable).padBottom(15).row();

        // بارگذاری کل وضعیت باز یا قفل بودن دستاوردها از کلاس سیو گلوبال بازی
        Table listTable = new Table();
        listTable.left();

        HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
        if (globalAchs == null) {
            globalAchs = new HashMap<>();
        }

        // اضافه کردن سطرهای مختلف دستاوردها به جدول نمایش منو
        addAchievementRow(listTable, LanguageManager.bundle.get("ach_title_completion"), LanguageManager.bundle.get("ach_desc_completion"), AchievementAssets.ACH_PURE_COMPLETION, "ach1", globalAchs.getOrDefault("ach1", false));
        addAchievementRow(listTable, LanguageManager.bundle.get("ach_title_speedrun"), LanguageManager.bundle.get("ach_desc_speedrun"), AchievementAssets.ACH_ULTRA_FAST_FINISH, "ach2", globalAchs.getOrDefault("ach2", false));
        addAchievementRow(listTable, LanguageManager.bundle.get("ach_title_hunter"), LanguageManager.bundle.get("ach_desc_hunter"), AchievementAssets.ACH_HUNTER_MARKS, "ach3", globalAchs.getOrDefault("ach3", false));
        addAchievementRow(listTable, LanguageManager.bundle.get("ach_title_false_knight"), LanguageManager.bundle.get("ach_desc_false_knight"), AchievementAssets.ACH_FALSE_KNIGHT, "ach4", globalAchs.getOrDefault("ach4", false));
        addAchievementRow(listTable, LanguageManager.bundle.get("ach_title_hornet"), LanguageManager.bundle.get("ach_desc_hornet"), AchievementAssets.ACH_HORNET, "ach5", globalAchs.getOrDefault("ach5", false));

        mainTable.add(listTable).padBottom(50).row();

        // ساخت نهایی دکمه بازگشت به منوی اصلی
        createBackButton(mainTable);
    }

    // متد کمکی برای ساخت یک سطر کامل دستاورد شامل آیکون، قفل، عنوان و متن توضیحات
    private void addAchievementRow(Table targetTable, String title, String desc, String assetPath, String prefKey, boolean isUnlocked) {
        Table rowContainer = new Table();
        rowContainer.left().pad(15);

        Stack iconStack = new Stack(); // استفاده از استک برای انداختن آیکون قفل روی عکس اصلی دستاورد

        Texture mainTexture = GameAssetManager.getInstance().getTexture(assetPath);
        Image achImage = (mainTexture != null) ? new Image(mainTexture) : new Image();

        Label lblTitle = new Label(title, new Label.LabelStyle(titleFont, Color.WHITE));
        Label lblDesc = new Label(desc, new Label.LabelStyle(descFont, Color.WHITE));

        // اگر دستاورد قفل است، رنگ آن را تیره و افکت قفل را فعال کن
        if (!isUnlocked) {
            achImage.setColor(0.2f, 0.2f, 0.2f, 0.6f);
            iconStack.add(achImage);

            Texture lockTexture = GameAssetManager.getInstance().getTexture(AchievementAssets.ACH_SECRET);
            if (lockTexture != null) {
                Image lockOverlay = new Image(lockTexture);
                Table lockWrapper = new Table();
                lockWrapper.top().right();
                lockWrapper.add(lockOverlay).size(22, 22);
                iconStack.add(lockWrapper);
            }

            lblTitle.setColor(new Color(1f, 1f, 1f, 0.5f));
            lblDesc.setColor(new Color(1f, 1f, 1f, 0.35f));
        } else {
            // اگر دستاورد باز شده است، با رنگ طلایی و جذاب کامل نمایش بده
            achImage.setColor(Color.WHITE);
            iconStack.add(achImage);

            lblTitle.setColor(new Color(0.95f, 0.85f, 0.6f, 1f));
            lblDesc.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        }

        rowContainer.add(iconStack).size(76, 76).padRight(35);

        Table textTable = new Table();
        textTable.left();
        textTable.add(lblTitle).left().row();
        textTable.add(lblDesc).left().padTop(6);

        rowContainer.add(textTable).width(650).left();

        targetTable.add(rowContainer).left().row();
    }

    // ساخت دکمه هوشمند بازگشت همراه با مدیریت لیسنرهای هاور و کلیک پوینتر
    private void createBackButton(Table mainTable) {
        final Table rowTable = new Table();
        rowTable.getColor().a = 0.6f;
        rowTables.add(rowTable);

        final AnimatedImage leftPointer = new AnimatedImage(pointerAnimation);
        leftPointer.getColor().a = 0f;

        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        final AnimatedImage rightPointer = new AnimatedImage(flippedAnimation);
        rightPointer.getColor().a = 0f;

        final TextButton button = new TextButton(LanguageManager.bundle.get("btn_back"), buttonStyle);

        rowTable.add(leftPointer).padRight(25).size(40, 40);
        rowTable.add(button);
        rowTable.add(rightPointer).padLeft(25).size(40, 40);

        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                AudioManager.getInstance().playHover();
                leftPointer.resetAnimation();
                rightPointer.resetAnimation();
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeIn(0.005f));
                rightPointer.addAction(Actions.fadeIn(0.005f));
                rowTable.addAction(Actions.alpha(1.0f, 0.15f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeOut(0.2f));
                rightPointer.addAction(Actions.fadeOut(0.2f));
                rowTable.addAction(Actions.alpha(0.6f, 0.15f));
            }
        });

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                UIManager.getInstance().changeScreen(new MainMenu());
            }
        });

        mainTable.add(rowTable).row();
    }

    // متد باز کردن یک دستاورد خاص و رفرش منوی جاری
    public void unlockAchievement(String achKey) {
        HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
        globalAchs.put(achKey, true);
        SaveManager.getInstance().saveGlobalAchievements(globalAchs);
        refreshMenu();
    }

    // متد باز کردن کل ۵ دستاورد بازی به صورت همزمان (مخصوص چیت یا تست سریع)
    public void unlockAllAchievements() {
        HashMap<String, Boolean> globalAchs = SaveManager.getInstance().loadGlobalAchievements();
        globalAchs.put("ach1", true);
        globalAchs.put("ach2", true);
        globalAchs.put("ach3", true);
        globalAchs.put("ach4", true);
        globalAchs.put("ach5", true);
        SaveManager.getInstance().saveGlobalAchievements(globalAchs);
        refreshMenu();
    }

    // رفرش لایه گرافیکی تیبل‌ها متناسب با تغییر دیتای دستاوردها
    private void refreshMenu() {
        mainTable.clear();
        initMenu();
    }

    // آزادسازی تمام ۴ آبجکت فونت ایجاد شده از حافظه
    @Override
    public void dispose() {
        super.dispose();
        if (headerBigFont != null) headerBigFont.dispose();
        if (headerSmallFont != null) headerSmallFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (descFont != null) descFont.dispose();
    }
}
