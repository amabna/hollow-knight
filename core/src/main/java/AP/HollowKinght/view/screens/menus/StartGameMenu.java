package AP.HollowKinght.view.screens.menus;

import AP.HollowKinght.view.managers.LanguageManager;
import AP.HollowKinght.view.screens.GameplayScreen;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.loaders.AnimationFactory;
import AP.HollowKinght.view.loaders.MenuAssets;
import AP.HollowKinght.controller.core.SaveManager;
import AP.HollowKinght.model.core.GameData;

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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class StartGameMenu extends BaseMenuScreen {
    private BitmapFont headerBigFont;
    private BitmapFont headerSmallFont;
    private BitmapFont slotFont;
    private TextButton.TextButtonStyle slotButtonStyle;
    private TextButton.TextButtonStyle backButtonStyle;
    private Animation<TextureRegion> pointerAnimation;
    private final Array<Table> rowTables = new Array<>();

    public StartGameMenu() {
        super();

        // تنظیم و تولید فونت‌های سایز بزرگ، کوچک و اسلات‌ها
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 110;
        parameter.color = Color.WHITE;
        parameter.borderColor = new Color(1f, 1f, 1f, 0.85f);
        parameter.borderWidth = 3.5f;
        parameter.shadowColor = new Color(1f, 1f, 1f, 0.5f);
        parameter.shadowOffsetX = 0;
        parameter.shadowOffsetY = 0;
        headerBigFont = generator.generateFont(parameter);

        parameter.size = 54;
        parameter.borderWidth = 2.2f;
        headerSmallFont = generator.generateFont(parameter);

        parameter.size = 50;
        parameter.color = new Color(1, 1, 1, 0.8f);
        parameter.borderWidth = 0;
        parameter.shadowColor = new Color(0, 0, 0, 0);
        slotFont = generator.generateFont(parameter);
        generator.dispose();

        // استایل دکمه‌های اسلات ذخیره
        slotButtonStyle = new TextButton.TextButtonStyle();
        slotButtonStyle.font = slotFont;
        slotButtonStyle.fontColor = new Color(1, 1, 1, 0.5f);
        slotButtonStyle.overFontColor = Color.WHITE;

        // استایل دکمه بازگشت
        backButtonStyle = new TextButton.TextButtonStyle();
        backButtonStyle.font = slotFont;
        backButtonStyle.fontColor = new Color(1, 1, 1, 0.4f);
        backButtonStyle.overFontColor = Color.WHITE;

        // فریم‌های انیمیشن پوینتر
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

        // جدا کردن کلمات هدر برای بزرگ کردن حرف اول
        String headerText = LanguageManager.bundle.get("menu_select_profile");
        String[] words = headerText.split(" ");

        Table headerTable = new Table();
        Label.LabelStyle bigStyle = new Label.LabelStyle(headerBigFont, Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(headerSmallFont, Color.WHITE);

        Table headerTextTable = new Table();

        String word1 = words.length > 0 ? words[0] : "SELECT";
        Label labelFirstW1 = new Label(String.valueOf(word1.charAt(0)), bigStyle);
        Label labelRestW1 = new Label(word1.substring(1), smallStyle);

        headerTextTable.add(labelFirstW1).bottom().padRight(4f);
        headerTextTable.add(labelRestW1).bottom().padBottom(14f).padRight(24f);

        if (words.length > 1) {
            String word2 = words[1];
            Label labelFirstW2 = new Label(String.valueOf(word2.charAt(0)), bigStyle);
            Label labelRestW2 = new Label(word2.substring(1), smallStyle);

            headerTextTable.add(labelFirstW2).bottom().padRight(2f);
            headerTextTable.add(labelRestW2).bottom().padBottom(14f);
        }

        headerTable.add(headerTextTable).colspan(4).center().row();

        // اضافه کردن عکس خط تزیینی زیر هدر
        Texture fleurTex = GameAssetManager.getInstance().getTexture(MenuAssets.GAME_OVER_FLEUR);
        if (fleurTex != null) {
            Image fleurImage = new Image(fleurTex);
            headerTable.add(fleurImage).width(1100).height(30).padTop(25).padBottom(45).colspan(4).row();
        }

        mainTable.add(headerTable).padBottom(40).row();

        String localizedNewGame = LanguageManager.bundle.get("slot_new_game");

        // حلقه ساخت ۴ اسلات سیو بازی
        for (int i = 1; i <= 4; i++) {
            boolean hasSave = SaveManager.getInstance().hasSave(i);
            String slotStateText;

            if (hasSave) {
                // اگر سیو وجود داشت زمان بازی را لود و فرمت می‌کنیم
                GameData savedData = SaveManager.getInstance().loadGame(i);
                if (savedData != null) {
                    int totalSeconds = (int) savedData.totalGameTime;
                    int hours = totalSeconds / 3600;
                    int minutes = (totalSeconds % 3600) / 60;
                    int seconds = totalSeconds % 60;
                    slotStateText = String.format("   %02d:%02d:%02d   ", hours, minutes, seconds);
                } else {
                    slotStateText = "   " + localizedNewGame + "   ";
                }
            } else {
                slotStateText = "   " + localizedNewGame + "   ";
            }

            createProfileSlot(i, slotStateText, hasSave, mainTable);
        }

        // ساخت دکمه بازگشت
        createBackButton(mainTable);
    }

    private void createProfileSlot(final int slotNumber, String slotStateText, final boolean hasSave, Table mainTable) {
        final Table rowTable = new Table();
        rowTable.getColor().a = 0.5f;
        rowTables.add(rowTable);

        final AnimatedImage leftPointer = new AnimatedImage(pointerAnimation);
        leftPointer.getColor().a = 0f;
        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        final AnimatedImage rightPointer = new AnimatedImage(flippedAnimation);
        rightPointer.getColor().a = 0f;

        String buttonText = slotNumber + "." + slotStateText;
        final TextButton slotButton = new TextButton(buttonText, slotButtonStyle);
        slotButton.getLabel().setAlignment(Align.left);
        slotButton.getLabelCell().padLeft(25).padRight(25);

        rowTable.add(leftPointer).padRight(25).size(35, 35);

        // تزیینات فلش‌های چپ و راست دکمه
        Texture arrowTex = GameAssetManager.getInstance().getTexture("shop_up_down_arrow.png");
        if (arrowTex != null) {
            Image leftDeco = new Image(arrowTex);
            leftDeco.setOrigin(Align.center);
            leftDeco.setRotation(90f);

            Image rightDeco = new Image(arrowTex);
            rightDeco.setOrigin(Align.center);
            rightDeco.setRotation(-270f);

            rowTable.add(leftDeco).padRight(35).size(25, 45);
            rowTable.add(slotButton).width(650).height(70).left();
            rowTable.add(rightDeco).padLeft(35).size(25, 45);
        } else {
            rowTable.add(slotButton).width(650).height(70).left();
        }

        rowTable.add(rightPointer).padLeft(25).size(35, 35);

        // لیسنر کلیک برای انتخاب اسلات و لود یا شروع بازی جدید
        slotButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                SaveManager.getInstance().currentSlot = slotNumber;
                UIManager.getInstance().changeScreen(new GameplayScreen(hasSave, slotNumber));
            }
        });

        addHoverEffect(slotButton, rowTable, leftPointer, rightPointer);
        mainTable.add(rowTable).padBottom(25).row();
    }

    private void createBackButton(Table mainTable) {
        final Table rowTable = new Table();
        rowTable.getColor().a = 0.5f;
        rowTables.add(rowTable);

        final AnimatedImage leftPointer = new AnimatedImage(pointerAnimation);
        leftPointer.getColor().a = 0f;
        Animation<TextureRegion> flippedAnimation = AnimationFactory.createFlippedAnimation(pointerAnimation);
        final AnimatedImage rightPointer = new AnimatedImage(flippedAnimation);
        rightPointer.getColor().a = 0f;

        final TextButton backButton = new TextButton(LanguageManager.bundle.get("btn_back"), backButtonStyle);
        backButton.getLabelCell().padLeft(25).padRight(25);

        rowTable.add(leftPointer).padRight(25).size(35, 35);

        Texture arrowTex = GameAssetManager.getInstance().getTexture("shop_up_down_arrow.png");
        if (arrowTex != null) {
            Image leftDeco = new Image(arrowTex);
            leftDeco.setOrigin(Align.center);
            leftDeco.setRotation(90f);

            Image rightDeco = new Image(arrowTex);
            rightDeco.setOrigin(Align.center);
            rightDeco.setRotation(-270f);

            rowTable.add(leftDeco).padRight(35).size(25, 45);
            rowTable.add(backButton).width(650).height(70);
            rowTable.add(rightDeco).padLeft(35).size(25, 45);
        } else {
            rowTable.add(backButton).width(650).height(70);
        }

        rowTable.add(rightPointer).padLeft(25).size(35, 35);

        addHoverEffect(backButton, rowTable, leftPointer, rightPointer);

        // کلیک دکمه بک برای برگشت به منوی اصلی
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playClick();
                UIManager.getInstance().changeScreen(new MainMenu());
            }
        });

        mainTable.add(rowTable).padTop(60).row();
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
                leftPointer.addAction(Actions.fadeIn(0.005f));
                rightPointer.addAction(Actions.fadeIn(0.005f));

                for (Table t : rowTables) {
                    if (t != rowTable) t.addAction(Actions.alpha(0.2f, 0.15f));
                    else t.addAction(Actions.alpha(1.0f, 0.15f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                leftPointer.clearActions();
                rightPointer.clearActions();
                leftPointer.addAction(Actions.fadeOut(0.2f));
                rightPointer.addAction(Actions.fadeOut(0.2f));

                for (Table t : rowTables) {
                    t.clearActions();
                    t.addAction(Actions.alpha(0.5f, 0.15f));
                }
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        if (headerBigFont != null) headerBigFont.dispose();
        if (headerSmallFont != null) headerSmallFont.dispose();
        if (slotFont != null) slotFont.dispose();
    }
}
