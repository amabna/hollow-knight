package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class WorldAssets {

    private static final String PREFIX = "HKassets/world/";

    // تایل‌های کف و زمین فیزیکی مرحله
    public static final String FLOOR_STONE_01 = PREFIX + "floor_stone_01.png";
    public static final String FLOOR_STONE_02 = PREFIX + "floor_stone_02.png";
    public static final String SMALL_FLOOR    = PREFIX + "small_floor.png";

    // پلتفرم‌های معلق مپ
    public static final String PLAT_FLOAT_01  = PREFIX + "plat_float_01.png";
    public static final String PLAT_FLOAT_02  = PREFIX + "plat_float_02.png";
    public static final String PLAT_FLOAT_03  = PREFIX + "plat_float_03.png";

    // تصاویر پس‌زمینه لایه‌ای (Parallax Background)
    public static final String BG_FAR_02      = PREFIX + "BG_far_0002_a.png";
    public static final String FAR_BG_ROCKS   = PREFIX + "far_BG_rocks.png";

    private static void safeLoad(AssetManager manager, String path) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, Texture.class);
        } else {
            Gdx.app.error("WorldAssets", "Missing world asset: " + path);
        }
    }

    public static void load(AssetManager manager) {
        safeLoad(manager, FLOOR_STONE_01);
        safeLoad(manager, FLOOR_STONE_02);
        safeLoad(manager, SMALL_FLOOR);

        safeLoad(manager, PLAT_FLOAT_01);
        safeLoad(manager, PLAT_FLOAT_02);
        safeLoad(manager, PLAT_FLOAT_03);

        safeLoad(manager, BG_FAR_02);
        safeLoad(manager, FAR_BG_ROCKS);
    }
}
