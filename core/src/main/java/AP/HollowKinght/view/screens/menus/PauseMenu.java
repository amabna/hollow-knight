package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import AP.HollowKinght.view.screens.GameplayScreen;
import AP.HollowKinght.controller.core.SaveManager;
import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.model.enemy.Enemy;
import AP.HollowKinght.model.player.Knight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class PauseMenu extends BaseMenuScreen {
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont statsFont;
    private TextButton.TextButtonStyle menuButtonStyle;
    private Animation<TextureRegion> pointerAnimation;
    private final Array<Table> rowTables = new Array<>();
    private final GameplayScreen activeGameplayScreen;
    private Table sideOverlayPanel;
    private boolean isCheatsVisible = false;

    public PauseMenu(GameplayScreen gameplayScreen) {
        super();
        this.activeGameplayScreen = gameplayScreen;

        // ساخت فونت‌ها برای عنوان، دکمه‌ها و بخش آمار چیت‌ها
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 90;
        parameter.color = new Color(0.9f, 0.9f, 0.9f, 1f);
        parameter.borderColor = new Color(0f, 0f, 0f, 0.8f);
        parameter.borderWidth = 4f;
        titleFont = generator.generateFont(parameter);

        parameter.size = 46;
        parameter.borderWidth = 0;
        parameter.color = new Color(1, 1, 1, 0.7f);
        buttonFont = generator.generateFont(parameter);

        parameter.size = 32;
        parameter.color = new Color(0.8f, 0.3f, 0.3f, 0.9f);
        statsFont = generator.generateFont(parameter);
        generator.dispose();

        menuButtonStyle = new TextButton.TextButtonStyle();
        menuButtonStyle.font = buttonFont;
        menuButtonStyle.fontColor = new Color(1, 1, 1, 0.5f);
        menuButtonStyle.overFontColor = Color.WHITE;

        // لود فریم‌های انیمیشن پوینتر دکمه‌ها
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

    @Override
    protected void initMenu() {
        rowTables.clear();
        mainTable.clear();

        Table containerTable = new Table();
        containerTable.setFillParent(true);
        containerTable.center();

        Table leftMenuTable = new Table();
        leftMenuTable.center();

        // اضافه کردن عنوان منوی پوز و خط تزیینی زیر آن
        Label pauseTitle = new Label(LanguageManager.bundle.get("menu_pause_title"), new Label.LabelStyle(titleFont, Color.WHITE));
        leftMenuTable.add(pauseTitle).padTop(60).padBottom(20).row();

        Texture fleurTex = GameAssetManager.getInstance().getTexture(MenuAssets.GAME_OVER_FLEUR);
        if (fleurTex != null) {
            Image fleur = new Image(fleurTex);
            leftMenuTable.add(fleur).width(500).height(18).padBottom(40).row();
        }

        // ایجاد ردیف‌های دکمه‌های منوی استپ
        createMenuRow(LanguageManager.bundle.get("btn_continue"), leftMenuTable, 1);
        createMenuRow(LanguageManager.bundle.get("menu_guide"), leftMenuTable, 2);
        createMenuRow(LanguageManager.bundle.get("menu_settings"), leftMenuTable, 3);
        createMenuRow(LanguageManager.bundle.get("btn_save_exit"), leftMenuTable, 4);

        // ایجاد پنل کناری برای نمایش آمار و کدهای تقلب
        sideOverlayPanel = new Table();
        Texture bgTex = GameAssetManager.getInstance().getTexture("shop_item_bg.png");
        if (bgTex != null) {
            sideOverlayPanel.setBackground(new Image(bgTex).getDrawable());
        }
        sideOverlayPanel.getColor().a = 0f;
        buildSidePanelContent();

        // چیدمان المان‌ها در جدول کانتینر برای حفظ تعادل تقارن دکمه‌ها در مرکز مانیتور
        containerTable.add().width(420).padLeft(50);
        containerTable.add(leftMenuTable).expandX().center();
        containerTable.add(sideOverlayPanel).width(420).height(450).padRight(50);

        mainTable.add(containerTable).fill().expand();

        // لیسنر ورودی کیبورد برای بستن منو با ESC یا کشتن آنی انمی‌ها با CTRL+L
        stage.addListener(new InputListener() {
            public boolean transformInput(InputEvent event, float x, float y) {
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    AudioManager.getInstance().playClick();
                    UIManager.getInstance().changeScreen(activeGameplayScreen);
                    return true;
                }

                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                    if (keycode == Input.Keys.L) {
                        GameController gc = GameController.getInstance();
                        if (gc != null && gc.getEnemies() != null) {
                            int killedCount = gc.getEnemies().size;
                            for (Enemy enemy : gc.getEnemies()) {
                                enemy.health = 0;
                                gc.markEnemyAsPermanentlyDead(enemy);
                            }
                            Knight knight = activeGameplayScreen.getKnight();
                            if (knight != null) {
                                knight.totalMobsKilled += killedCount;
                            }
                            gc.getEnemies().clear();
                            buildSidePanelContent();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void buildSidePanelContent() {
        sideOverlayPanel.clear();
        sideOverlayPanel.top().left().pad(25);

        Label.LabelStyle headStyle = new Label.LabelStyle(statsFont, Color.GOLD);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(statsFont, Color.WHITE);
        bodyStyle.font.getData().setScale(0.85f);

        // پر کردن اطلاعات بخش آمار بازی مانند زمان و تعداد دفعات مرگ
        sideOverlayPanel.add(new Label("--- STATS ---", headStyle)).padBottom(10).row();

        int totalSeconds = (int) activeGameplayScreen.totalGameTime;
        String timeStr = String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);

        Knight knight = activeGameplayScreen.getKnight();
        int totalKills = (knight != null) ? knight.totalMobsKilled : 0;
        int deaths = (knight != null) ? knight.knightDeathCount : 0;

        sideOverlayPanel.add(new Label("Time: " + timeStr, bodyStyle)).padBottom(5).row();
        sideOverlayPanel.add(new Label("Kills: " + totalKills, bodyStyle)).padBottom(5).row();
        sideOverlayPanel.add(new Label("Deaths: " + deaths, bodyStyle)).padBottom(20).row();

        // قرار دادن متن‌های راهنمای کدهای تقلب بازی در پنل راست
        sideOverlayPanel.add(new Label("--- CHEATS ---", headStyle)).padBottom(10).row();
        addCheatHelpRow("CTRL+F", "Teleport to Boss", bodyStyle);
        addCheatHelpRow("CTRL+G", "Toggle Noclip", bodyStyle);
        addCheatHelpRow("CTRL+H", "Heal (+1)", bodyStyle);
        addCheatHelpRow("CTRL+J", "Refill Soul", bodyStyle);
        addCheatHelpRow("CTRL+K", "God Mode", bodyStyle);
        addCheatHelpRow("CTRL+L", "Kill All Mobs", bodyStyle);
    }

    private void createMenuRow(String text, Table parentTable, final int actionId) {
        final Table rowTable = new Table();
        rowTable.getColor().a = 0.6f;
        rowTables.add(rowTable);

        final AnimatedImage leftPointer = new AnimatedImage(pointerAnimation);
        leftPointer.getColor().a = 0f;
        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        final AnimatedImage rightPointer = new AnimatedImage(flippedAnimation);
        rightPointer.getColor().a = 0f;

        final TextButton button = new TextButton(text, menuButtonStyle);

        rowTable.add(leftPointer).padRight(15).size(30, 30);
        rowTable.add(button).width(400).height(65);
        rowTable.add(rightPointer).padLeft(15).size(30, 30);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                handlePauseAction(actionId);
            }
        });

        addHoverEffect(button, rowTable, leftPointer, rightPointer);
        parentTable.add(rowTable).padBottom(20).row();
    }

    private void handlePauseAction(int actionId) {
        switch (actionId) {
            case 1:
                UIManager.getInstance().changeScreen(activeGameplayScreen);
                break;
            case 2:
                // سوئیچ وضعیت نمایش یا پنهان شدن انیمیشنی پنل چیت‌ها
                isCheatsVisible = !isCheatsVisible;
                sideOverlayPanel.clearActions();
                if (isCheatsVisible) {
                    buildSidePanelContent();
                    sideOverlayPanel.addAction(Actions.sequence(Actions.visible(true), Actions.fadeIn(0.25f)));
                } else {
                    sideOverlayPanel.addAction(Actions.sequence(Actions.fadeOut(0.25f)));
                }
                break;
            case 3:
                UIManager.getInstance().changeScreen(new SettingsMenu(this));
                break;
            case 4:
                // ذخیره‌سازی هارد سیو لول و وضعیت کاراکتر در اسلات فعال و بازگشت به منو
                int currentActiveSlot = SaveManager.getInstance().currentSlot;
                Knight currentKnight = activeGameplayScreen.getKnight();
                float totalGameTime = activeGameplayScreen.totalGameTime;

                SaveManager.getInstance().saveGame(currentKnight, totalGameTime, currentActiveSlot);
                AudioManager.getInstance().playMusicFade("back.wav");
                UIManager.getInstance().changeScreen(new MainMenu());
                break;
        }
    }

    private void addCheatHelpRow(String code, String desc, Label.LabelStyle style) {
        Table row = new Table();
        Label lblCode = new Label(code + " : ", style);
        lblCode.setColor(Color.RED);
        row.add(lblCode).left();

        Label lblDesc = new Label(desc, style);
        lblDesc.setWrap(true);
        row.add(lblDesc).width(300).left();

        sideOverlayPanel.add(row).left().padBottom(12).row();
    }

    private void addHoverEffect(final TextButton button, final Table rowTable, final AnimatedImage leftPointer, final AnimatedImage rightPointer) {
        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                AudioManager.getInstance().playHover();
                leftPointer.resetAnimation();
                rightPointer.resetAnimation();
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeIn(0.01f));
                rightPointer.addAction(Actions.fadeIn(0.01f));

                for (Table t : rowTables) {
                    if (t != rowTable) t.addAction(Actions.alpha(0.2f, 0.1f));
                    else t.addAction(Actions.alpha(1.0f, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeOut(0.15f));
                rightPointer.addAction(Actions.fadeOut(0.15f));

                for (Table t : rowTables) {
                    t.clearActions();
                    t.addAction(Actions.alpha(0.6f, 0.15f));
                }
            }
        });
    }

    @Override
    public void render(float delta) {
        // رندر همزمان گیم‌پلی در بک‌گراند منوی استپ
        if (activeGameplayScreen != null) {
            activeGameplayScreen.render(delta);
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (statsFont != null) statsFont.dispose();
    }
}
