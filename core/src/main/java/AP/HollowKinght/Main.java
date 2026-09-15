package AP.HollowKinght;

import AP.HollowKinght.view.managers.LanguageManager;
import com.badlogic.gdx.Game;
import AP.HollowKinght.view.managers.GameAssetManager;
import AP.HollowKinght.view.managers.UIManager;
import AP.HollowKinght.view.screens.menus.MainMenu;
import AP.HollowKinght.view.loaders.MenuAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {
    private SpriteBatch cursorBatch;
    private Texture cursorTexture;
    private boolean cursorLoaded = false;

    @Override
    public void create() {
        cursorBatch = new SpriteBatch();

        // ۱. بارگذاری assets های اولیه منو
        GameAssetManager assetManager = GameAssetManager.getInstance();
        assetManager.loadAllCoreAssets();  // ← این متد رو باید اصلاح کنیم

        // ۲. لود کردن Texture کورسر بعد از بارگذاری
        cursorTexture = assetManager.getTexture(MenuAssets.INV_0014_SELECTION_CURSOR);
        cursorLoaded = (cursorTexture != null);

        // ۳. سیستم دوزبانگی
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        String currentLang = prefs.getString("language", "en");

        // اعتبارسنجی زبان
        if (!currentLang.equals("en") && !currentLang.equals("fr")) {
            currentLang = "en";
            prefs.putString("language", "en");
            prefs.flush();
        }

        // ۴. لود باندل زبان
        LanguageManager.loadLanguage(currentLang);

        // ۵. راه‌اندازی UIManager و رفتن به منوی اصلی
        UIManager.getInstance().initialize(this);
        UIManager.getInstance().changeScreen(new MainMenu());
    }

    @Override
    public void render() {
        super.render();  // این خط باعث میشه Screenهای فعلی رندر بشن

        // رسم کورسر سفارشی (فقط اگر لود شده باشه)
        if (cursorLoaded && cursorTexture != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            cursorBatch.begin();
            cursorBatch.draw(cursorTexture,
                mouseX - 16,  // مرکز کورسر روی موس
                mouseY - 16,
                32, 32
            );
            cursorBatch.end();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (cursorBatch != null) {
            cursorBatch.dispose();
        }
        // Textureها توسط AssetManager مدیریت می‌شن
        GameAssetManager.getInstance().dispose();
    }
}
