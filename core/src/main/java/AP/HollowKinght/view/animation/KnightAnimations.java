package AP.HollowKinght.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class KnightAnimations {
    public Animation<TextureRegion> idleAnim;
    public Animation<TextureRegion> runAnim;
    public Animation<TextureRegion> runToIdleAnim;
    public Animation<TextureRegion> jumpAnim;
    public Animation<TextureRegion> airborneAnim;
    public Animation<TextureRegion> fallAnim;
    public Animation<TextureRegion> landingAnim;
    public Animation<TextureRegion> doubleJumpAnim;
    public Animation<TextureRegion> dashAnim;
    public Animation<TextureRegion> wallSlideAnim;
    public Animation<TextureRegion> wallJumpAnim;
    public Animation<TextureRegion> downSlashAnim; // انیمیشن حمله رو به پایین (پوگو)
    public Animation<TextureRegion> upSlashAnim;   // انیمیشن حمله رو به بالا

    public KnightAnimations() {
        // بارگذاری مجموعه‌های مختلف انیمیشنی به همراه تعداد فریم و سرعت اجرا
        idleAnim = loadAnimation("Idle", 8, 0.1f, Animation.PlayMode.LOOP);
        runAnim = loadAnimation("Run", 12, 0.2f, Animation.PlayMode.LOOP);
        dashAnim = loadAnimation("Dash", 11, 0.1f, Animation.PlayMode.NORMAL);
        wallSlideAnim = loadAnimation("Wall Slide", 3, 0.1f, Animation.PlayMode.LOOP);
        wallJumpAnim = loadAnimation("Walljump", 8, 0.06f, Animation.PlayMode.NORMAL);
        jumpAnim = loadAnimation("Airborne", 11, 0.3f, Animation.PlayMode.NORMAL);
        doubleJumpAnim = loadAnimation("Double Jump", 7, 0.3f, Animation.PlayMode.NORMAL);
        landingAnim = loadAnimation("Landing", 0, 0.12f, Animation.PlayMode.NORMAL);
        runToIdleAnim = loadAnimation("Run To Idle", 5, 0.03f, Animation.PlayMode.NORMAL);
        fallAnim = loadAnimation("Fall", 3, 0.08f, Animation.PlayMode.LOOP);
        airborneAnim = loadAnimation("Airborne", 11, 0.06f, Animation.PlayMode.LOOP);

        downSlashAnim = loadAnimation("DownSlash", 4, 0.2f, Animation.PlayMode.NORMAL);
        upSlashAnim = loadAnimation("UpSlash", 4, 0.2f, Animation.PlayMode.NORMAL);
    }

    private Animation<TextureRegion> loadAnimation(String prefix, int maxFrames, float frameDuration, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        int first = 0;

        // آفست شروع فریم‌های دویدن برای جلوه بصری هماهنگ‌تر
        if (prefix.equals("Run")) { first = 3; }

        for (int i = first; i <= maxFrames; i++) {
            String fileName = String.format("HKassets/knight/%s_%03d.png", prefix, i);
            try {
                if (Gdx.files.internal(fileName).exists()) {
                    Texture tex = new Texture(Gdx.files.internal(fileName));
                    frames.add(new TextureRegion(tex));
                }
            } catch (Exception e) {}
        }

        // بررسی فایل تک تصویر جایگزین در صورت پیدا نشدن توالی فریم‌ها
        if (frames.size == 0) {
            try {
                String fallback = String.format("HKassets/knight/%s.png", prefix);
                if (Gdx.files.internal(fallback).exists()) {
                    frames.add(new TextureRegion(new Texture(Gdx.files.internal(fallback))));
                }
            } catch (Exception e) {}
        }

        // سیستم محافظتی اضطراری ضد کرش (تولید بافت موقت بنفش رنگ در صورت نبود هیچ‌گونه دارایی گرافیکی)
        if (frames.size == 0) {
            System.err.println("[ANIMATION ERROR] فایل‌های انیمیشن یافت نشد -> " + prefix);
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.MAGENTA);
            pixmap.fill();
            frames.add(new TextureRegion(new Texture(pixmap)));
            pixmap.dispose();
        }

        return new Animation<>(frameDuration, frames, mode);
    }
}
