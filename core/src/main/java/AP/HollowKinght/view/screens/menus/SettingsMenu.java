package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.screens.AbstractScreen;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class SettingsMenu extends BaseMenuScreen {
    private BitmapFont headerBigFont;
    private BitmapFont headerSmallFont;
    private BitmapFont itemFont;
    private TextButton.TextButtonStyle buttonStyle;
    private Slider.SliderStyle sliderStyle;
    private Animation<TextureRegion> pointerAnimation;
    private Array<Table> rowTables = new Array<>();
    private Preferences prefs;

    private float musicVolume;
    private boolean isMusicOn;
    private boolean isSfxOn;
    private float sfxVolume;
    private float brightnessVolume;
    private String currentLanguage;

    // نگهداری ارجاع به صفحه قبلی باز شده
    private final com.badlogic.gdx.Screen previousScreen;

    public SettingsMenu(com.badlogic.gdx.Screen previousScreen) {
        super();
        this.previousScreen = previousScreen;
        loadSettings();
        initFonts();
        initStyles();

        // فریم‌های پوینتر دکمه‌ها
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

    private void loadSettings() {
        // خواندن تنظیمات صدا، روشنایی و زبان از حافظه بازی
        prefs = Gdx.app.getPreferences("HollowKnightSettings");

        musicVolume = prefs.getFloat("musicVolume", 100f);
        sfxVolume = prefs.getFloat("sfxVolume", 100f);
        isMusicOn = prefs.getBoolean("isMusicOn", true);
        isSfxOn = prefs.getBoolean("isSfxOn", true);
        brightnessVolume = prefs.getFloat("brightnessVolume", 100f);

        currentLanguage = prefs.getString("language", "en");
        if (!currentLanguage.equals("en") && !currentLanguage.equals("fr")) {
            currentLanguage = "en";
        }

        AbstractScreen.brightness = brightnessVolume / 100f;
    }

    private void initFonts() {
        // لود فونت‌های بخش هدر و گزینه‌ها
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 110;
        p.color = Color.WHITE;
        p.borderColor = new Color(1f, 1f, 1f, 0.85f);
        p.borderWidth = 3.5f;
        p.shadowColor = new Color(1f, 1f, 1f, 0.5f);
        p.shadowOffsetX = 0;
        p.shadowOffsetY = 0;
        headerBigFont = gen.generateFont(p);

        p.size = 54;
        p.borderWidth = 2.2f;
        headerSmallFont = gen.generateFont(p);

        p.size = 32;
        p.borderWidth = 0;
        p.shadowColor = new Color(0, 0, 0, 0);
        itemFont = gen.generateFont(p);
        gen.dispose();
    }

    private void initStyles() {
        // ساخت گرافیک دکمه‌ها و اسلایدرها با پیکس‌مپ
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = itemFont;
        buttonStyle.fontColor = new Color(1, 1, 1, 0.6f);
        buttonStyle.overFontColor = Color.WHITE;

        Pixmap bgPixmap = new Pixmap(200, 4, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(Color.DARK_GRAY);
        bgPixmap.fill();
        TextureRegionDrawable sliderBg = new TextureRegionDrawable(new Texture(bgPixmap));
        bgPixmap.dispose();

        Pixmap knobPixmap = new Pixmap(10, 20, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(Color.WHITE);
        knobPixmap.fill();
        TextureRegionDrawable sliderKnob = new TextureRegionDrawable(new Texture(knobPixmap));
        knobPixmap.dispose();

        sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = sliderBg;
        sliderStyle.knob = sliderKnob;
    }

    @Override
    protected void initMenu() {
        rowTables.clear();

        String settingsText = LanguageManager.bundle.get("menu_settings");

        Table headerTable = new Table();
        Label.LabelStyle bigStyle = new Label.LabelStyle(headerBigFont, Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(headerSmallFont, Color.WHITE);

        Table headerTextTable = new Table();
        Label labelFirst = new Label(String.valueOf(settingsText.charAt(0)), bigStyle);
        Label labelRest = new Label(settingsText.substring(1), smallStyle);

        headerTextTable.add(labelFirst).bottom().padRight(4f);
        headerTextTable.add(labelRest).bottom().padBottom(14f);
        headerTable.add(headerTextTable).colspan(4).center().row();

        Texture fleurTex = GameAssetManager.getInstance().getTexture(MenuAssets.GAME_OVER_FLEUR);
        if (fleurTex != null) {
            Image fleurImage = new Image(fleurTex);
            headerTable.add(fleurImage).width(1100).height(30).padTop(25).padBottom(45).colspan(4).row();
        }

        mainTable.add(headerTable).padBottom(15).row();

        // اسلایدر میزان صدای موزیک
        addSliderRow(LanguageManager.bundle.get("volume_music"), 0, 100, 1, (int) musicVolume, value -> {
            musicVolume = value;
            prefs.putFloat("musicVolume", musicVolume);
            prefs.flush();
            AudioManager.getInstance().updateMusicSettings(isMusicOn, musicVolume);
        });

        // سوییچ روشن و خاموش کردن موزیک
        addToggleRow(LanguageManager.bundle.get("toggle_music"), isMusicOn ? "ON" : "OFF", button -> {
            AudioManager.getInstance().playClick();
            isMusicOn = !isMusicOn;
            button.setText(LanguageManager.bundle.get("toggle_music") + ": " + (isMusicOn ? "ON" : "OFF"));
            prefs.putBoolean("isMusicOn", isMusicOn);
            prefs.flush();
            AudioManager.getInstance().updateMusicSettings(isMusicOn, musicVolume);
        });

        // سوییچ روشن و خاموش کردن افکت‌های صوتی
        addToggleRow(LanguageManager.bundle.get("toggle_sfx"), isSfxOn ? "ON" : "OFF", button -> {
            isSfxOn = !isSfxOn;
            button.setText(LanguageManager.bundle.get("toggle_sfx") + ": " + (isSfxOn ? "ON" : "OFF"));
            prefs.putBoolean("isSfxOn", isSfxOn);
            prefs.flush();
            AudioManager.getInstance().updateSfxSettings(isSfxOn, sfxVolume);
            AudioManager.getInstance().playClick();
        });

        // اسلایدر نور و روشنایی صفحه بازی
        addSliderRow(LanguageManager.bundle.get("label_brightness"), 10, 100, 1, (int) brightnessVolume, value -> {
            updateBrightnessSettings(value);
        });

        // دکمه تغییر زبان بازی بین انگلیسی و فرانسوی
        String displayLangName = currentLanguage.equals("en") ? "ENGLISH" : "FRANÇAIS";
        addToggleRow(LanguageManager.bundle.get("label_language"), displayLangName, button -> {
            AudioManager.getInstance().playClick();
            if (currentLanguage.equals("en")) {
                currentLanguage = "fr";
            } else {
                currentLanguage = "en";
            }
            prefs.putString("language", currentLanguage);
            prefs.flush();
            LanguageManager.loadLanguage(currentLanguage);
            refreshMenu();
        });

        // برگشتن به صفحه قبلی (منوی اصلی یا منوی پوز وسط بازی)
        addButtonRow(LanguageManager.bundle.get("btn_back"), () -> {
            AudioManager.getInstance().playClick();
            UIManager.getInstance().changeScreen((AbstractScreen) previousScreen);
        });
    }

    private void refreshMenu() {
        // بازسازی صفحه تنظیمات برای تغییر آنی متون ترجمه شده
        UIManager.getInstance().changeScreen(new SettingsMenu(previousScreen));
    }

    public void updateBrightnessSettings(float volumeFromSlider) {
        brightnessVolume = volumeFromSlider;
        AbstractScreen.brightness = volumeFromSlider / 100f;
        float brightnessAlpha = 0.2f + (volumeFromSlider / 100f) * 0.8f;

        prefs.putFloat("brightnessVolume", brightnessVolume);
        prefs.putFloat("brightnessAlpha", brightnessAlpha);
        prefs.flush();

        if (this.stage != null && this.stage.getBatch() != null) {
            this.stage.getBatch().setColor(brightnessAlpha, brightnessAlpha, brightnessAlpha, 1f);
        }
    }

    private void addSliderRow(String labelText, float min, float max, float step, int defaultValue, ValueChangeListener listener) {
        Table row = createRowContainer();
        Label lbl = new Label(labelText + ": ", new Label.LabelStyle(itemFont, Color.WHITE));
        Slider slider = new Slider(min, max, step, false, sliderStyle);
        slider.setValue(defaultValue);

        final Label valLbl = new Label(String.valueOf((int) slider.getValue()), new Label.LabelStyle(itemFont, Color.WHITE));

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) slider.getValue();
                valLbl.setText(String.valueOf(val));
                listener.onValueChanged(val);
            }
        });

        setupHoverEffect(slider, row);

        Table contentTable = (Table) row.getChildren().get(1);
        contentTable.add(lbl).width(410).left().padRight(20);
        contentTable.add(slider).width(200);
        contentTable.add(valLbl).padLeft(20).width(60).right();

        mainTable.add(row).padBottom(15).row();
    }

    private void addToggleRow(String title, String status, ToggleChangeListener listener) {
        Table row = createRowContainer();
        final TextButton btn = new TextButton(title + ": " + status, buttonStyle);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                listener.onToggle(btn);
            }
        });

        setupHoverEffect(btn, row);
        Table contentTable = (Table) row.getChildren().get(1);
        contentTable.add(btn);

        mainTable.add(row).padBottom(15).row();
    }

    private void addButtonRow(String text, Runnable action) {
        Table row = createRowContainer();
        TextButton btn = new TextButton(text, buttonStyle);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                action.run();
            }
        });

        setupHoverEffect(btn, row);
        Table contentTable = (Table) row.getChildren().get(1);
        contentTable.add(btn);

        mainTable.add(row).padBottom(15).row();
    }

    private Table createRowContainer() {
        Table row = new Table();
        row.getColor().a = 0.6f;
        rowTables.add(row);

        AnimatedImage left = new AnimatedImage(pointerAnimation);
        left.getColor().a = 0f;

        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        AnimatedImage right = new AnimatedImage(flippedAnimation);
        right.getColor().a = 0f;

        Table contentSlot = new Table();
        row.add(left).size(30, 30).padRight(15);
        row.add(contentSlot);
        row.add(right).size(30, 30).padLeft(15);

        return row;
    }

    private void setupHoverEffect(Actor triggerActor, final Table targetRow) {
        triggerActor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                AudioManager.getInstance().playHover();

                AnimatedImage left = (AnimatedImage) targetRow.getChildren().get(0);
                AnimatedImage right = (AnimatedImage) targetRow.getChildren().get(2);

                left.resetAnimation();
                right.resetAnimation();
                left.clearActions();
                right.clearActions();
                left.addAction(Actions.fadeIn(0.005f));
                right.addAction(Actions.fadeIn(0.005f));

                for (Table t : rowTables) {
                    t.clearActions();
                    if (t != targetRow) t.addAction(Actions.alpha(0.2f, 0.15f));
                    else t.addAction(Actions.alpha(1.0f, 0.15f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                AnimatedImage left = (AnimatedImage) targetRow.getChildren().get(0);
                AnimatedImage right = (AnimatedImage) targetRow.getChildren().get(2);

                left.clearActions();
                right.clearActions();
                left.addAction(Actions.fadeOut(0.2f));
                right.addAction(Actions.fadeOut(0.2f));

                for (Table t : rowTables) {
                    t.clearActions();
                    t.addAction(Actions.alpha(0.6f, 0.15f));
                }
            }
        });
    }

    interface ValueChangeListener { void onValueChanged(int value); }
    interface ToggleChangeListener { void onToggle(TextButton button); }

    @Override
    public void dispose() {
        super.dispose();
        if (headerBigFont != null) headerBigFont.dispose();
        if (headerSmallFont != null) headerSmallFont.dispose();
        if (itemFont != null) itemFont.dispose();
    }
}
