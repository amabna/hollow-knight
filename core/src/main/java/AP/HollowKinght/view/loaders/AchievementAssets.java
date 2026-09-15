package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class AchievementAssets {

    public static final String ACH_HUNTER_MARKS = "HKassets/achievements/achievement_Hunter_Marks.png";
    public static final String ACH_HORNET = "HKassets/achievements/achievement_icons__0003_hornet.png";
    public static final String ACH_PURE_COMPLETION = "HKassets/achievements/achievement_pure_completion.png";
    public static final String ACH_SECRET = "HKassets/achievements/achievement_secret.png";
    public static final String ACH_ULTRA_FAST_FINISH = "HKassets/achievements/achievement_ultra_fast_finish.png";
    public static final String ACH_FALSE_KNIGHT = "HKassets/achievements/achievement_false_knight.png";

    public static void load(AssetManager manager) {
        manager.load(ACH_HUNTER_MARKS, Texture.class);
        manager.load(ACH_HORNET, Texture.class);
        manager.load(ACH_PURE_COMPLETION, Texture.class);
        manager.load(ACH_SECRET, Texture.class);
        manager.load(ACH_ULTRA_FAST_FINISH, Texture.class);
        manager.load(ACH_FALSE_KNIGHT, Texture.class);
    }
}
