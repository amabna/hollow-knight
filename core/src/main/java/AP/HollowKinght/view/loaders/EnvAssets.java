package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class EnvAssets {

    private static final String PREFIX = "HKassets/env/";

    // ساختار گرافیکی HUD بالای صفحه (جان، روح، سکه)
    public static final String HEALTH_FRAME    = "HKassets/hud/select_game_HUD_0002_health_frame.png";
    public static final String HEALTH_MASK     = "HKassets/hud/select_game_HUD_0001_health.png";
    public static final String MAGIC_ORB_FRAME = "HKassets/hud/select_game_HUD_0000_magic_orb.png";
    public static final String MAGIC_ORB_FILL  = PREFIX + "hud_orb_fill.png";
    public static final String GEO_COIN        = PREFIX + "hud_geo_coin.png";
    public static final String SOUL_ORB_ATLAS  = "HKassets/hud/Soulorb.atlas";

    private static void safeLoad(AssetManager manager, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                // بارگذاری خودکار و هوشمند با کلاس تفکیک‌شده TextureAtlas در صورت داشتن فرمت اطلس لایب‌جی‌دی‌ایکس
                if (path.endsWith(".atlas")) {
                    manager.load(path, TextureAtlas.class);
                } else {
                    manager.load(path, Texture.class);
                }
                Gdx.app.log("EnvAssets", "Successfully queued: " + path);
            } else {
                Gdx.app.error("EnvAssets", "File NOT found at path: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("EnvAssets", "Skipping asset due to engine error: " + path);
        }
    }

    public static void load(AssetManager manager) {
        // سیستم تست ران‌تایم سلامت دپندنسی‌ها و فایل‌های ترکیبی
        String expectedDependency = "HKassets/hud/soul_fulling_frames.png";
        if (Gdx.files.internal(expectedDependency).exists()) {
            Gdx.app.log("EnvAssets", "DIAGNOSTIC: File EXACTLY exists on runtime assets directory!");
        } else {
            Gdx.app.error("EnvAssets", "DIAGNOSTIC: Missing dependency file! Check your build folder -> " + expectedDependency);
        }

        safeLoad(manager, HEALTH_FRAME);
        safeLoad(manager, HEALTH_MASK);
        safeLoad(manager, MAGIC_ORB_FRAME);
        safeLoad(manager, MAGIC_ORB_FILL);
        safeLoad(manager, GEO_COIN);
        safeLoad(manager, SOUL_ORB_ATLAS);
    }
}
