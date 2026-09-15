package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class ArchAssets {

    private static final String PREFIX = "HKassets/arch/";

    // پلتفرم‌ها، موانع و آبجکت‌های تعاملی نقشه بازی
    public static final String ENV_FLOOR      = PREFIX + "env_floor.png";
    public static final String ENV_WALL       = PREFIX + "env_wall.png";
    public static final String ENV_SPIKES     = PREFIX + "env_spikes.png";
    public static final String ENV_DOOR       = PREFIX + "env_door.png";
    public static final String ENV_BACKGROUND = PREFIX + "env_background.png";
    public static final String ENV_BENCH      = PREFIX + "env_bench.png";

    private static void safeLoad(AssetManager manager, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Texture.class);
                Gdx.app.log("ArchAssets", "Successfully queued: " + path);
            } else {
                Gdx.app.error("ArchAssets", "File NOT found at path: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("ArchAssets", "Skipping asset due to engine error: " + path);
        }
    }

    public static void load(AssetManager manager) {
        safeLoad(manager, ENV_FLOOR);
        safeLoad(manager, ENV_WALL);
        safeLoad(manager, ENV_SPIKES);
        safeLoad(manager, ENV_DOOR);
        safeLoad(manager, ENV_BACKGROUND);
        safeLoad(manager, ENV_BENCH);
    }
}
