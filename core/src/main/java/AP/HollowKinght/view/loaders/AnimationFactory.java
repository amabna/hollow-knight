package AP.HollowKinght.view.loaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import AP.HollowKinght.view.managers.GameAssetManager;

public class AnimationFactory {

    /**
     * ساخت انیمیشن از روی آرایه‌ای از مسیرهای تصاویر موجود در AssetManager
     */
    public static Animation<TextureRegion> createAnimation(String[] paths, float frameDuration, Animation.PlayMode playMode) {
        TextureRegion[] frames = new TextureRegion[paths.length];
        GameAssetManager manager = GameAssetManager.getInstance();

        for (int i = 0; i < paths.length; i++) {
            Texture texture = manager.getTexture(paths[i]);
            if (texture != null) {
                frames[i] = new TextureRegion(texture);
            } else {
                // سیستم ضد کرش: جایگذاری مستطیل قرمز رنگ در صورت نبود فایل روی هارد دیسک
                frames[i] = createFallbackTexture();
                System.err.println("⚠️ Warning: Texture not found: " + paths[i]);
            }
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(playMode);
        return animation;
    }

    /**
     * معکوس کردن افقی فریم‌های انیمیشن (قرینه‌سازی برای حرکت به سمت چپ)
     */
    public static Animation<TextureRegion> createFlippedAnimation(Animation<TextureRegion> original) {
        TextureRegion[] originalFrames = original.getKeyFrames();
        TextureRegion[] flippedFrames = new TextureRegion[originalFrames.length];

        for (int i = 0; i < originalFrames.length; i++) {
            flippedFrames[i] = new TextureRegion(originalFrames[i]);
            flippedFrames[i].flip(true, false);
        }

        Animation<TextureRegion> flipped = new Animation<>(original.getFrameDuration(), flippedFrames);
        flipped.setPlayMode(original.getPlayMode());
        return flipped;
    }

    private static TextureRegion createFallbackTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1); // قرمز داغ
        pixmap.fill();
        pixmap.setColor(1, 1, 1, 1);
        pixmap.drawRectangle(2, 2, 28, 28);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
}
