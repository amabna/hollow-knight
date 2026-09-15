package AP.HollowKinght.view.managers;

import AP.HollowKinght.view.loaders.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class GameAssetManager {
    private static GameAssetManager instance;
    private final AssetManager manager;

    private GameAssetManager() {
        manager = new AssetManager();
    }

    public static synchronized GameAssetManager getInstance() {
        if (instance == null) {
            instance = new GameAssetManager();
        }
        return instance;
    }

    /**
     * متد فراخوانی و بارگذاری همزمان کل کدهای مربوط به هسته بازی در فاز ابتدایی
     */
    public void loadAllCoreAssets() {
        MenuAssets.load(manager);
        AchievementAssets.load(manager);
        KnightAssets.load(manager);
        WorldAssets.load(manager);

        manager.finishLoading(); // متوقف کردن موقت ترد تا اتمام کامل لودینگ دیسک
    }

    public Texture getTexture(String fileName) {
        return manager.isLoaded(fileName) ? manager.get(fileName, Texture.class) : null;
    }

    public TextureAtlas getAtlas(String fileName) {
        return manager.isLoaded(fileName) ? manager.get(fileName, TextureAtlas.class) : null;
    }

    public void dispose() {
        manager.dispose();
    }
}
