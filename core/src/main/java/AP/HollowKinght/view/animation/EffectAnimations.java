package AP.HollowKinght.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class EffectAnimations {
    public Animation<TextureRegion> slashAnim;

    public EffectAnimations() {
        Array<TextureRegion> frames = new Array<>();
        // بارگذاری فریم‌های متوالی افکت خط شمشیر
        for (int i = 0; i <= 5; i++) {
            String fileName = String.format("HKassets/knight/SlashEffect_%03d.png", i);
            try {
                if (Gdx.files.internal(fileName).exists()) {
                    frames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
                }
            } catch (Exception e) {}
        }

        // سیستم ضد کرش: در صورت مفقود بودن فریم‌ها، از تک فریم اصلی بک‌آپ استفاده می‌شود
        if (frames.size == 0) {
            try {
                if (Gdx.files.internal("HKassets/knight/SlashEffect.png").exists()) {
                    frames.add(new TextureRegion(new Texture(Gdx.files.internal("HKassets/knight/SlashEffect.png"))));
                }
            } catch(Exception e){}
        }
        slashAnim = new Animation<>(0.035f, frames, Animation.PlayMode.NORMAL);
    }
}
