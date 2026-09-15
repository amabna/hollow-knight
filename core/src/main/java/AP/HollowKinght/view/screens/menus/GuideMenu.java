package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

// کلاس منوی راهنمای بازی شامل تب‌های کلیدهای بازی، قابلیت‌ها و کدهای تقلب
public class GuideMenu extends BaseMenuScreen {

    private BitmapFont headerBigFont;
    private BitmapFont headerSmallFont;
    private BitmapFont sectionFont, contentFont;
    private TextButton.TextButtonStyle tabButtonStyle, backButtonStyle;

    private Array<Table> rowTables = new Array<>();
    private Table contentContainer; // کانتینر پویا برای رندر محتوای داخل تب فعال
    private Preferences prefs;

    // آرایه فریم‌های انیمیشن نشانگر منو
    private final String[] pointerFrames = {
        MenuAssets.MAIN_MENU_POINTER_ANIM0000, MenuAssets.MAIN_MENU_POINTER_ANIM0001,
        MenuAssets.MAIN_MENU_POINTER_ANIM0002, MenuAssets.MAIN_MENU_POINTER_ANIM0003,
        MenuAssets.MAIN_MENU_POINTER_ANIM0004, MenuAssets.MAIN_MENU_POINTER_ANIM0005,
        MenuAssets.MAIN_MENU_POINTER_ANIM0006, MenuAssets.MAIN_MENU_POINTER_ANIM0007,
        MenuAssets.MAIN_MENU_POINTER_ANIM0008, MenuAssets.MAIN_MENU_POINTER_ANIM0009,
        MenuAssets.MAIN_MENU_POINTER_ANIM0010
    };

    public GuideMenu() {
        super();
        prefs = Gdx.app.getPreferences("HollowKnightSettings");
        initFonts();
        initStyles();
    }

    // ساخت تمام فونت‌های منوی راهنما با سایزهای هماهنگ
    private void initFonts() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // فونت برای تک حرف درشت اول کلمات هدر
        p.size = 110;
        p.color = Color.WHITE;
        p.borderColor = new Color(1f, 1f, 1f, 0.85f);
        p.borderWidth = 3.5f;
        p.shadowColor = new Color(1f, 1f, 1f, 0.5f);
        headerBigFont = gen.generateFont(p);

        // فونت برای ادامه بدنه کلمات هدر
        p.size = 54;
        p.borderWidth = 2.2f;
        headerSmallFont = gen.generateFont(p);

        // فونت دکمه‌ها و سرفصل‌ها
        p.size = 28;
        p.borderWidth = 0;
        p.shadowColor = new Color(0, 0, 0, 0);
        p.color = new Color(1, 1, 1, 0.8f);
        sectionFont = gen.generateFont(p);

        // فونت ریزتر برای متن توضیحات داخلی هر بخش
        p.size = 22;
        p.color = new Color(0.9f, 0.9f, 0.9f, 0.95f);
        contentFont = gen.generateFont(p);

        gen.dispose();
    }

    // مقداردهی سبک گرافیکی و رنگ وضعیت‌های مختلف دکمه‌ها
    private void initStyles() {
        tabButtonStyle = new TextButton.TextButtonStyle();
        tabButtonStyle.font = sectionFont;
        tabButtonStyle.fontColor = new Color(1, 1, 1, 0.5f);
        tabButtonStyle.overFontColor = Color.WHITE;
        tabButtonStyle.checkedFontColor = new Color(0.3f, 0.7f, 0.9f, 1f); // رنگ تب انتخاب شده (آبی ملایم)

        backButtonStyle = new TextButton.TextButtonStyle();
        backButtonStyle.font = sectionFont;
        backButtonStyle.fontColor = new Color(1, 1, 1, 0.6f);
        backButtonStyle.overFontColor = Color.WHITE;
    }

    @Override
    protected void initMenu() {
        rowTables.clear();

        // --- ۱. ساخت هدر هوشمند و دوزبانه (جداسازی حروف اول کلمات) ---
        String guideText = LanguageManager.bundle.get("menu_game_guide");
        String[] words = guideText.split(" ");

        Table headerTable = new Table();
        Label.LabelStyle bigStyle = new Label.LabelStyle(headerBigFont, Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(headerSmallFont, Color.WHITE);

        Table headerTextTable = new Table();

        // کلمه اول هدر
        String word1 = words.length > 0 ? words[0] : "GAME";
        Label labelG1 = new Label(String.valueOf(word1.charAt(0)), bigStyle);
        Label labelAme = new Label(word1.substring(1), smallStyle);
        headerTextTable.add(labelG1).bottom().padRight(4f);
        headerTextTable.add(labelAme).bottom().padBottom(14f).padRight(24f);

        // کلمه دوم هدر (در صورت وجود در باندل ترجمه)
        if (words.length > 1) {
            String word2 = words[1];
            Label labelG2 = new Label(String.valueOf(word2.charAt(0)), bigStyle);
            Label labelUide = new Label(word2.substring(1), smallStyle);
            headerTextTable.add(labelG2).bottom().padRight(2f);
            headerTextTable.add(labelUide).bottom().padBottom(14f);
        }

        headerTable.add(headerTextTable).colspan(4).center().row();

        // بارگذاری و ترسیم خط تزئینی زیر هدر منو
        Texture fleurTex = GameAssetManager.getInstance().getTexture(MenuAssets.GAME_OVER_FLEUR);
        if (fleurTex != null) {
            Image fleurImage = new Image(fleurTex);
            headerTable.add(fleurImage).width(1100).height(30).padTop(25).padBottom(45).colspan(4).row();
        }

        mainTable.add(headerTable).padBottom(10).row();

        // --- ۲. ساخت دکمه‌های ناوبری تب‌ها (کنترل‌ها، قابلیت‌ها، چیت‌ها) ---
        Table tabTable = new Table();
        final TextButton btnControls = new TextButton(LanguageManager.bundle.get("tab_controls"), tabButtonStyle);
        final TextButton btnAbilities = new TextButton(LanguageManager.bundle.get("tab_abilities"), tabButtonStyle);
        final TextButton btnCheats = new TextButton(LanguageManager.bundle.get("tab_cheats"), tabButtonStyle);

        // غیرفعال کردن رویدادهای خودکار ناخواسته لیب‌جی‌دی‌ایکس هنگام کلیک روی تب‌ها
        btnControls.setProgrammaticChangeEvents(false);
        btnAbilities.setProgrammaticChangeEvents(false);
        btnCheats.setProgrammaticChangeEvents(false);

        tabTable.add(btnControls).padRight(40);
        tabTable.add(btnAbilities).padRight(40);
        tabTable.add(btnCheats);
        mainTable.add(tabTable).padBottom(40).row();

        // کانتینر اصلی نمایش اطلاعات تب فعال
        contentContainer = new Table();
        mainTable.add(contentContainer).width(950).height(420).top().row();

        // لیسنرهای کلیک تب‌ها جهت جابه‌جایی داینامیک زیرصفحه‌ها
        btnControls.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                setTabActive(btnControls, btnAbilities, btnCheats);
                showControls();
            }
        });

        btnAbilities.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                setTabActive(btnAbilities, btnControls, btnCheats);
                showAbilities();
            }
        });

        btnCheats.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                setTabActive(btnCheats, btnControls, btnAbilities);
                showCheats();
            }
        });

        // --- ۳. ایجاد ردیف و دکمه برگشت همراه با پوینتر متحرک دوجانبه ---
        final Table backRowTable = new Table();
        backRowTable.getColor().a = 0.6f;
        rowTables.add(backRowTable);

        Animation<TextureRegion> animLeft = AnimationFactory.createAnimation(pointerFrames, 0.015f, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> animRight = AnimationFactory.createAnimation(pointerFrames, 0.015f, Animation.PlayMode.NORMAL);

        // شبیه‌سازی فلیپ افقی پوینتر سمت راست بدون تغییر مراجع تکسچر اصلی چپ
        TextureRegion[] leftFrames = animLeft.getKeyFrames();
        TextureRegion[] rightFrames = new TextureRegion[leftFrames.length];
        for (int i = 0; i < leftFrames.length; i++) {
            rightFrames[i] = new TextureRegion(leftFrames[i]);
            if (!rightFrames[i].isFlipX()) {
                rightFrames[i].flip(true, false);
            }
        }
        animRight = new Animation<TextureRegion>(0.015f, rightFrames);

        final AnimatedImage leftPointer = new AnimatedImage(animLeft);
        final AnimatedImage rightPointer = new AnimatedImage(animRight);
        leftPointer.getColor().a = 0f;
        rightPointer.getColor().a = 0f;

        TextButton btnBack = new TextButton(LanguageManager.bundle.get("btn_back"), backButtonStyle);
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                AudioManager.getInstance().playClick();
                UIManager.getInstance().changeScreen(new MainMenu());
            }
        });

        // اعمال افکت‌های زیبای هاور و محو شدن تدریجی متون و آیکون‌ها
        btnBack.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                AudioManager.getInstance().playHover();
                leftPointer.resetAnimation();
                rightPointer.resetAnimation();
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeIn(0.005f));
                rightPointer.addAction(Actions.fadeIn(0.005f));

                for (Table t : rowTables) {
                    if (t != backRowTable) t.addAction(Actions.alpha(0.2f, 0.15f));
                    else t.addAction(Actions.alpha(1.0f, 0.15f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeOut(0.15f));
                rightPointer.addAction(Actions.fadeOut(0.15f));

                for (Table t : rowTables) {
                    t.addAction(Actions.alpha(0.6f, 0.15f));
                }
            }
        });

        backRowTable.add(leftPointer).size(35).padRight(15);
        backRowTable.add(btnBack);
        backRowTable.add(rightPointer).size(35).padLeft(15);

        mainTable.add(backRowTable).padTop(30).row();

        // فعال کردن اولیه تب کنترل‌ها به صورت پیش‌فرض در نخستین ورود
        btnControls.setChecked(true);
        showControls();
    }

    // فعال کردن گرافیکی تب جاری و پاکسازی لایه متنی تب قبلی با انیمیشن فید این
    private void setTabActive(TextButton active, TextButton... others) {
        active.setChecked(true);
        for (TextButton btn : others) btn.setChecked(false);
        contentContainer.clearChildren();
        contentContainer.getColor().a = 0f;
        contentContainer.addAction(Actions.fadeIn(0.2f));
    }

    // --- لود متون داینامیک زیرصفحه کلیدهای کنترلی بازی ---
    private void showControls() {
        Label.LabelStyle headStyle = new Label.LabelStyle(sectionFont, new Color(0.3f, 0.7f, 0.9f, 1f));
        Label.LabelStyle bodyStyle = new Label.LabelStyle(contentFont, Color.WHITE);

        contentContainer.add(new Label(LanguageManager.bundle.get("guide_action"), headStyle)).width(400).left();
        contentContainer.add(new Label(LanguageManager.bundle.get("guide_current_key"), headStyle)).width(300).right().row();
        contentContainer.add(new Label("------------------------------------------------------------------", bodyStyle)).colspan(2).padBottom(15).row();

        // استخراج داینامیک کلیدهای ثبت شده بازی از بخش تنظیمات ترجیحی سیستم
        addControlRow(LanguageManager.bundle.get("control_move_left"), Input.Keys.toString(prefs.getInteger("keyLeft", Input.Keys.LEFT)), bodyStyle);
        addControlRow(LanguageManager.bundle.get("control_move_right"), Input.Keys.toString(prefs.getInteger("keyRight", Input.Keys.RIGHT)), bodyStyle);
        addControlRow(LanguageManager.bundle.get("control_jump"), Input.Keys.toString(prefs.getInteger("keyJump", Input.Keys.Z)), bodyStyle);
        addControlRow(LanguageManager.bundle.get("control_dash"), Input.Keys.toString(prefs.getInteger("keyDash", Input.Keys.C)), bodyStyle);
        addControlRow(LanguageManager.bundle.get("control_attack"), Input.Keys.toString(prefs.getInteger("keyAttack", Input.Keys.X)), bodyStyle);
    }

    private void addControlRow(String action, String keyName, Label.LabelStyle style) {
        contentContainer.add(new Label(action, style)).left().padBottom(12);
        contentContainer.add(new Label(keyName, style)).right().padBottom(12).row();
    }

    // --- لود متون داینامیک زیرصفحه معرفی سیستم قابلیت‌ها (خون، سول و چارم‌ها) ---
    private void showAbilities() {
        Label.LabelStyle headStyle = new Label.LabelStyle(sectionFont, new Color(1f, 0.85f, 0.4f, 1f));
        Label.LabelStyle bodyStyle = new Label.LabelStyle(contentFont, Color.WHITE);

        Table scrollTable = new Table();

        scrollTable.add(new Label(LanguageManager.bundle.get("ability_health_title"), headStyle)).left().row();
        Label lblHealth = new Label(LanguageManager.bundle.get("ability_health_desc"), bodyStyle);
        lblHealth.setWrap(true); // فعال‌سازی قابلیت شکستن خودکار خطوط متن طولانی
        scrollTable.add(lblHealth).width(900).left().padBottom(20).row();

        scrollTable.add(new Label(LanguageManager.bundle.get("ability_soul_title"), headStyle)).left().row();
        Label lblSoul = new Label(LanguageManager.bundle.get("ability_soul_desc"), bodyStyle);
        lblSoul.setWrap(true);
        scrollTable.add(lblSoul).width(900).left().padBottom(20).row();

        scrollTable.add(new Label(LanguageManager.bundle.get("ability_char_title"), headStyle)).left().row();
        Label lblAbilities = new Label(LanguageManager.bundle.get("ability_char_desc"), bodyStyle);
        lblAbilities.setWrap(true);
        scrollTable.add(lblAbilities).width(900).left().row();

        contentContainer.add(scrollTable).top().left();
    }

    // --- لود متون داینامیک زیرصفحه معرفی کدهای تقلب (Cheats) ---
    private void showCheats() {
        Label.LabelStyle headStyle = new Label.LabelStyle(sectionFont, new Color(0.9f, 0.3f, 0.3f, 1f));
        Label.LabelStyle bodyStyle = new Label.LabelStyle(contentFont, Color.WHITE);

        contentContainer.add(new Label(LanguageManager.bundle.get("cheat_code_title"), headStyle)).width(300).left();
        contentContainer.add(new Label(LanguageManager.bundle.get("cheat_desc_title"), headStyle)).width(600).left().row();
        contentContainer.add(new Label("------------------------------------------------------------------", bodyStyle)).colspan(2).padBottom(15).row();

        // اضافه کردن تک‌تک سطرهای کدهای تقلب معتبر موتور بازی به جدول راهنما
        addCheatRow("BOSS", LanguageManager.bundle.get("cheat_boss_desc"), bodyStyle);
        addCheatRow("GHOST", LanguageManager.bundle.get("cheat_ghost_desc"), bodyStyle);
        addCheatRow("HEAL", LanguageManager.bundle.get("cheat_heal_desc"), bodyStyle);
        addCheatRow("SOUL", LanguageManager.bundle.get("cheat_soul_desc"), bodyStyle);
        addCheatRow("GOD", LanguageManager.bundle.get("cheat_god_desc"), bodyStyle);
        addCheatRow("KILL", LanguageManager.bundle.get("cheat_kill_desc"), bodyStyle);
    }

    private void addCheatRow(String code, String desc, Label.LabelStyle style) {
        Label lblCode = new Label(code, style);
        lblCode.setColor(Color.RED); // کدهای چیت به رنگ قرمز متمایز رسم شوند
        contentContainer.add(lblCode).left().padBottom(15);

        Label lblDesc = new Label(desc, style);
        lblDesc.setWrap(true);
        contentContainer.add(lblDesc).width(600).left().padBottom(15).row();
    }

    @Override
    public void dispose() {
        super.dispose();
        // تخلیه حافظه آبجکت‌های گرافیکی فونت‌ها
        if (headerBigFont != null) headerBigFont.dispose();
        if (headerSmallFont != null) headerSmallFont.dispose();
        if (sectionFont != null) sectionFont.dispose();
        if (contentFont != null) contentFont.dispose();
    }
}
