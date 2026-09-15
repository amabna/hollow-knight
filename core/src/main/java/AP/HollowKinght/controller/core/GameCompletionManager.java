package AP.HollowKinght.controller.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.audio.AudioManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.screens.GameplayScreen;
import AP.HollowKinght.view.screens.menus.MainMenu;

public class GameCompletionManager {
    private static GameCompletionManager instance;

    public boolean isMenuOpen = false;

    private Rectangle containerBounds;
    private Rectangle continueBtnBounds;
    private Rectangle restartBtnBounds;
    private Rectangle mainMenuBtnBounds;

    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont buttonFont;
    private GlyphLayout glyphLayout;

    private GameCompletionManager() {
        glyphLayout = new GlyphLayout();
        containerBounds = new Rectangle();
        continueBtnBounds = new Rectangle();
        restartBtnBounds = new Rectangle();
        mainMenuBtnBounds = new Rectangle();

        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/perpetua-bold.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();

            p.size = 65;
            p.color = Color.GOLD;
            titleFont = gen.generateFont(p);

            p.size = 28;
            p.color = Color.WHITE;
            statsFont = gen.generateFont(p);

            p.size = 32;
            p.color = Color.LIGHT_GRAY;
            buttonFont = gen.generateFont(p);

            gen.dispose();
        } catch (Exception e) {
            titleFont = new BitmapFont();
            statsFont = new BitmapFont();
            buttonFont = new BitmapFont();
        }
    }

    public static GameCompletionManager getInstance() {
        if (instance == null) {
            instance = new GameCompletionManager();
        }
        return instance;
    }

    // محاسبه پویای ابعاد پنجره و دکمه‌ها بدون هاردکد کردن مقادیر ثابت
    private void updateLayout() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float rectW = 650f;
        float rectH = 550f;

        float rectX = (screenW - rectW) / 2f;
        float rectY = ((screenH - rectH) / 2f) + 40f;

        containerBounds.set(rectX, rectY, rectW, rectH);

        float btnW = 450f;
        float btnH = 52f;
        float btnX = rectX + (rectW - btnW) / 2f;

        continueBtnBounds.set(btnX, rectY + 160f, btnW, btnH);
        restartBtnBounds.set(btnX, rectY + 95f, btnW, btnH);
        mainMenuBtnBounds.set(btnX, rectY + 30f, btnW, btnH);
    }

    // بررسی کلیک کاربر روی گزینه‌های ادامه، شروع مجدد یا خروج
    public void updateAndInput(GameplayScreen gameplayScreen) {
        if (!isMenuOpen) return;

        updateLayout();

        Knight knight = gameplayScreen.getKnight();
        if (knight != null) {
            knight.velocityX = 0;
            knight.velocityY = 0;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // بستن منو و ادامه بازی فعلی
            if (continueBtnBounds.contains(mouseX, mouseY)) {
                AudioManager.getInstance().playSFX("select.wav");
                isMenuOpen = false;
                gameplayScreen.isPaused = false;
            }
            // حذف فایل سیو و بازنشانی کامل متغیرها برای شروع بازی جدید
            else if (restartBtnBounds.contains(mouseX, mouseY)) {
                AudioManager.getInstance().playSFX("select.wav");
                isMenuOpen = false;
                gameplayScreen.isPaused = false;

                int currentSlot = SaveManager.getInstance().currentSlot;
                String filename = "saves/save_slot_" + currentSlot + ".json";
                FileHandle fileHandle = Gdx.files.local(filename);
                if (fileHandle.exists()) {
                    fileHandle.delete();
                }

                InventoryManager inv = InventoryManager.getInstance();
                for (int i = 0; i < inv.equippedCharms.length; i++) {
                    inv.equippedCharms[i] = -1;
                }

                if (knight != null) {
                    for (int i = 0; i < 8; i++) {
                        inv.applyCharmEffectsLogic(i, false, knight);
                    }
                }

                try {
                    HiddenRoomManager hrm = HiddenRoomManager.getInstance();
                    hrm.wallHealth = 3;
                    hrm.isDestroyed = false;
                    hrm.brokenImageAlpha = 0.0f;
                    hrm.darknessAlpha = 1.0f;
                } catch (Exception e) {
                }

                GameplayScreen freshGameplay = new GameplayScreen(false, currentSlot);

                if (freshGameplay.getKnight() != null) {
                    freshGameplay.getKnight().ach5 = false;
                }

                UIManager.getInstance().changeScreen(freshGameplay);
            }
            // ذخیره‌سازی خودکار وضعیت فعلی و بازگشت به منوی اصلی بازی
            else if (mainMenuBtnBounds.contains(mouseX, mouseY)) {
                AudioManager.getInstance().playSFX("select.wav");
                isMenuOpen = false;
                gameplayScreen.isPaused = false;

                int currentSlot = SaveManager.getInstance().currentSlot;
                Knight currentKnight = gameplayScreen.getKnight();
                float totalGameTime = gameplayScreen.totalGameTime;

                if (currentKnight != null) {
                    SaveManager.getInstance().saveGame(currentKnight, totalGameTime, currentSlot);
                }

                AudioManager.getInstance().playMusicFade("back.wav");
                UIManager.getInstance().changeScreen(new MainMenu());
            }
        }
    }

    // رندر پس‌زمینه منو، افکت‌های هاور ماوس و تراز وسط متون آماری بازی
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, GameplayScreen gameplayScreen) {
        if (!isMenuOpen) return;

        updateLayout();

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.14f, 0.94f);

        float x = containerBounds.x;
        float y = containerBounds.y;
        float w = containerBounds.width;
        float h = containerBounds.height;
        float r = 18f;

        shapeRenderer.rect(x + r, y, w - 2f * r, h);
        shapeRenderer.rect(x, y + r, w, h - 2f * r);
        shapeRenderer.circle(x + r, y + r, r);
        shapeRenderer.circle(x + w - r, y + r, r);
        shapeRenderer.circle(x + r, y + h - r, r);
        shapeRenderer.circle(x + w - r, y + h - r, r);

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        shapeRenderer.setColor(continueBtnBounds.contains(mouseX, mouseY) ? new Color(0.35f, 0.35f, 0.4f, 0.75f) : new Color(0.18f, 0.18f, 0.2f, 0.6f));
        shapeRenderer.rect(continueBtnBounds.x, continueBtnBounds.y, continueBtnBounds.width, continueBtnBounds.height);

        shapeRenderer.setColor(restartBtnBounds.contains(mouseX, mouseY) ? new Color(0.35f, 0.35f, 0.4f, 0.75f) : new Color(0.18f, 0.18f, 0.2f, 0.6f));
        shapeRenderer.rect(restartBtnBounds.x, restartBtnBounds.y, restartBtnBounds.width, restartBtnBounds.height);

        shapeRenderer.setColor(mainMenuBtnBounds.contains(mouseX, mouseY) ? new Color(0.35f, 0.35f, 0.4f, 0.75f) : new Color(0.18f, 0.18f, 0.2f, 0.6f));
        shapeRenderer.rect(mainMenuBtnBounds.x, mainMenuBtnBounds.y, mainMenuBtnBounds.width, mainMenuBtnBounds.height);

        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();

        String titleText = "VICTORY";
        glyphLayout.setText(titleFont, titleText);
        titleFont.draw(batch, titleText, x + (w - glyphLayout.width) / 2f, y + h - 45f);

        Knight knight = gameplayScreen.getKnight();
        int deaths = (knight != null) ? knight.knightDeathCount : 0;
        int kills = (knight != null) ? knight.totalMobsKilled : 0;
        int totalSeconds = (int) gameplayScreen.totalGameTime;
        String timeStr = String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);

        String dStr = "☠  Death Count: " + deaths;
        String mStr = "⚔  Mobs Defeated: " + kills;
        String tStr = "⏱  Total Game Time: " + timeStr;

        glyphLayout.setText(statsFont, tStr);
        float statsX = x + (w - glyphLayout.width) / 2f;

        statsFont.draw(batch, dStr, statsX, y + h - 140f);
        statsFont.draw(batch, mStr, statsX, y + h - 190f);
        statsFont.draw(batch, tStr, statsX, y + h - 240f);

        String btnText1 = "CONTINUE";
        glyphLayout.setText(buttonFont, btnText1);
        buttonFont.draw(batch, btnText1, continueBtnBounds.x + (continueBtnBounds.width - glyphLayout.width) / 2f, continueBtnBounds.y + (continueBtnBounds.height + glyphLayout.height) / 2f);

        String btnText2 = "RESTART GAME";
        glyphLayout.setText(buttonFont, btnText2);
        buttonFont.draw(batch, btnText2, restartBtnBounds.x + (restartBtnBounds.width - glyphLayout.width) / 2f, restartBtnBounds.y + (restartBtnBounds.height + glyphLayout.height) / 2f);

        String btnText3 = "RETURN TO MAIN MENU";
        glyphLayout.setText(buttonFont, btnText3);
        buttonFont.draw(batch, btnText3, mainMenuBtnBounds.x + (mainMenuBtnBounds.width - glyphLayout.width) / 2f, mainMenuBtnBounds.y + (mainMenuBtnBounds.height + glyphLayout.height) / 2f);

        batch.end();
    }
}
