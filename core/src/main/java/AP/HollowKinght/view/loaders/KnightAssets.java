package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class KnightAssets {

    private static final String PREFIX = "HKassets/knight/";

    // پیش‌تعریف فایل‌های تک فریمی یا پایگاه انیمیشن‌ها
    public static final String IDLE          = PREFIX + "Idle.png";
    public static final String RUN           = PREFIX + "Run.png";
    public static final String AIRBORNE      = PREFIX + "Airborne.png";
    public static final String LANDING        = PREFIX + "Landing.png";

    private static void safeLoad(AssetManager manager, String path) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, Texture.class);
        } else {
            Gdx.app.error("KnightAssets", "File not found: " + path);
        }
    }

    public static void load(AssetManager manager) {
        // بارگذاری هوشمند حلقوی اسپرایت‌ شیت‌های پوزیشن سکون
        safeLoad(manager, IDLE);
        for (int i = 0; i <= 8; i++) {
            safeLoad(manager, PREFIX + "Idle_" + String.format("%03d", i) + ".png");
        }

        // انیمیشن دویدن
        safeLoad(manager, RUN);
        for (int i = 0; i <= 12; i++) {
            safeLoad(manager, PREFIX + "Run_" + String.format("%03d", i) + ".png");
        }

        // انیمیشن توقف دویدن (ترانزیشن به سکون)
        for (int i = 0; i <= 5; i++) {
            safeLoad(manager, PREFIX + "Run To Idle_" + String.format("%03d", i) + ".png");
        }

        // انیمیشن سقوط آزاد
        safeLoad(manager, AIRBORNE);
        for (int i = 0; i <= 5; i++) {
            safeLoad(manager, PREFIX + "Fall_" + String.format("%03d", i) + ".png");
        }

        // انیمیشن فرود روی زمین
        safeLoad(manager, LANDING);
        for (int i = 0; i <= 3; i++) {
            safeLoad(manager, PREFIX + "Landing_" + String.format("%03d", i) + ".png");
        }
    }
}
