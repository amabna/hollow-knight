package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class MainMenu extends BaseMenuScreen {
    private BitmapFont customFont;
    private TextButton.TextButtonStyle buttonStyle;
    private Array<TextButton> menuButtons = new Array<>();
    private Array<Table> rowTables = new Array<>();
    private Animation<TextureRegion> pointerAnimation;

    public MainMenu() {
        super();
        // لود فونت و تنظیم اندازه آن
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 26;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0, 0, 0.8f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        customFont = generator.generateFont(parameter);
        generator.dispose();

        // تنظیم استایل دکمه‌ها در حالت معمولی، هاور و کلیک
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = customFont;
        buttonStyle.fontColor = new Color(1, 1, 1, 0.5f);
        buttonStyle.overFontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;

        // آرایه فریم‌های انیمیشن نشانگر ماوس
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
        menuButtons.clear();
        rowTables.clear();

        // لود و اضافه کردن لوگوی عنوان بازی به بالای منو
        Texture titleTexture = GameAssetManager.getInstance().getTexture(MenuAssets.VHEART_TITLE);
        if (titleTexture != null) {
            Image titleImage = new Image(titleTexture);
            mainTable.add(titleImage).top().padBottom(10).row();
        }

        // ساخت تک تک دکمه‌های منوی اصلی با متن‌های ترجمه شده
        createButton(LanguageManager.bundle.get("menu_start"), "START_GAME", mainTable);
        createButton(LanguageManager.bundle.get("menu_settings"), "SETTINGS", mainTable);
        createButton(LanguageManager.bundle.get("menu_guide"), "GUIDE", mainTable);
        createButton(LanguageManager.bundle.get("menu_achievements"), "ACHIEVEMENTS", mainTable);
        createButton(LanguageManager.bundle.get("menu_exit"), "EXIT", mainTable);
    }

    private void createButton(final String translatedText, final String actionKey, Table mainTable) {
        final Table rowTable = new Table();
        rowTable.getColor().a = 0.6f;
        rowTables.add(rowTable);

        // ساخت نشانگر انیمیشنی چپ و راست
        final AnimatedImage leftPointer = new AnimatedImage(pointerAnimation);
        leftPointer.getColor().a = 0f;
        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        final AnimatedImage rightPointer = new AnimatedImage(flippedAnimation);
        rightPointer.getColor().a = 0f;

        final TextButton button = new TextButton(translatedText, buttonStyle);
        menuButtons.add(button);

        rowTable.add(leftPointer).padRight(15).size(30, 30);
        rowTable.add(button);
        rowTable.add(rightPointer).padLeft(15).size(30, 30);

        // لیسنر هاور برای ظاهر شدن نشانگرها و کم‌رنگ شدن بقیه دکمه‌ها
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

                for (int i = 0; i < menuButtons.size; i++) {
                    rowTables.get(i).clearActions();
                    if (menuButtons.get(i) != button) {
                        rowTables.get(i).addAction(Actions.alpha(0.2f, 0.15f));
                    } else {
                        rowTables.get(i).addAction(Actions.alpha(1.0f, 0.15f));
                    }
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeOut(0.2f));
                rightPointer.addAction(Actions.fadeOut(0.2f));

                boolean anyoneHovered = false;
                for (TextButton b : menuButtons) {
                    if (b.isOver()) {
                        anyoneHovered = true;
                        break;
                    }
                }
                if (!anyoneHovered) {
                    for (Table t : rowTables) {
                        t.clearActions();
                        t.addAction(Actions.alpha(0.6f, 0.15f));
                    }
                }
            }
        });

        // لیسنرهای کلیک دکمه‌ها برای هدایت به صفحات مختلف بازی
        if (actionKey.equals("EXIT")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playClick();
                    Gdx.app.exit();
                }
            });
        }
        else if (actionKey.equals("START_GAME")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playClick();
                    UIManager.getInstance().changeScreen(new StartGameMenu());
                }
            });
        }
        else if (actionKey.equals("SETTINGS")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playClick();
                    UIManager.getInstance().changeScreen(new SettingsMenu(MainMenu.this));
                }
            });
        }
        else if (actionKey.equals("GUIDE")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playClick();
                    UIManager.getInstance().changeScreen(new GuideMenu());
                }
            });
        }
        else if (actionKey.equals("ACHIEVEMENTS")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playClick();
                    UIManager.getInstance().changeScreen(new AchievementsMenu());
                }
            });
        }

        mainTable.add(rowTable).padBottom(20).row();
    }

    @Override
    public void show() {
        super.show();
        // پخش آهنگ پس‌زمینه منو به صورت ملایم
        AudioManager.getInstance().playMusicFade("back.wav");
    }

    @Override
    public void render(float delta) {
        // آپدیت وضعیت بلند شدن صدای موزیک
        AudioManager.getInstance().updateMusicFade(delta);
        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (customFont != null) customFont.dispose();
    }
}
