package AP.HollowKinght.view.renderer;

import AP.HollowKinght.model.enemy.FalseKnight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class FalseKnightRenderer {

    // تعریف انیمیشن‌های مختلف باس فایت False Knight
    private Animation<TextureRegion> deathAnim;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> maceSlamAnim;
    private Animation<TextureRegion> recoverAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> megaSlamAnim;
    private Animation<TextureRegion> stunnedAnim;

    // متغیرهای مربوط به تصویر موج ضربه و لیست تکسچرها برای مدیریت حافظه
    private Texture waveTexture;
    private Array<Texture> allocatedTextures;

    public FalseKnightRenderer() {
        // ایجاد لیست برای نگهداری تمام تکسچرها جهت حذف آسان در متد dispose
        allocatedTextures = new Array<>();

        // --- ۱. بارگذاری انیمیشن حالت سکون (Idle) ---
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/idle_00" + i + ".png"));
            allocatedTextures.add(tex);
            idleFrames.add(new TextureRegion(tex));
        }
        idleAnim = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        // --- ۲. بارگذاری انیمیشن ضربه گرز معمولی (Mace Slam) ---
        Array<TextureRegion> attackFrames = new Array<>();
        for (int i = 0; i <= 8; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/attack_00" + i + ".png"));
            allocatedTextures.add(tex);
            attackFrames.add(new TextureRegion(tex));
        }
        maceSlamAnim = new Animation<>(0.15f, attackFrames, Animation.PlayMode.NORMAL);

        // --- ۳. بارگذاری انیمیشن بازگشت گرز بعد از ضربه (Recover) ---
        Array<TextureRegion> recoverFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/Attack Recover_00" + i + ".png"));
            allocatedTextures.add(tex);
            recoverFrames.add(new TextureRegion(tex));
        }
        recoverAnim = new Animation<>(0.14f, recoverFrames, Animation.PlayMode.NORMAL);

        // --- ۴. بارگذاری انیمیشن دویدن یا هجوم افقی (Run) ---
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/Run_00" + i + ".png"));
            allocatedTextures.add(tex);
            runFrames.add(new TextureRegion(tex));
        }
        runAnim = new Animation<>(0.22f, runFrames, Animation.PlayMode.LOOP);

        // --- ۵. بارگذاری انیمیشن پرش (Jump) ---
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i <= 8; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/Jump_00" + i + ".png"));
            allocatedTextures.add(tex);
            jumpFrames.add(new TextureRegion(tex));
        }
        jumpAnim = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);

        // --- ۶. بارگذاری انیمیشن پرش همراه با ضربه فاز دوم (Mega Slam) ---
        Array<TextureRegion> megaFrames = new Array<>();
        for (int i = 0; i <= 7; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/Jump Attack_00" + i + ".png"));
            allocatedTextures.add(tex);
            megaFrames.add(new TextureRegion(tex));
        }
        megaSlamAnim = new Animation<>(0.3f, megaFrames, Animation.PlayMode.NORMAL);

        // --- ۷. بارگذاری انیمیشن وضعیت گیج شدن باس (Stunned) ---
        Array<TextureRegion> stunnedFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            Texture tex = new Texture(Gdx.files.internal("HKassets/boss/Body_00" + i + ".png"));
            allocatedTextures.add(tex);
            stunnedFrames.add(new TextureRegion(tex));
        }
        stunnedAnim = new Animation<>(0.2f, stunnedFrames, Animation.PlayMode.LOOP);

        // --- ۸. بارگذاری انیمیشن مرگ آهسته باس (Death) ---
        Array<TextureRegion> deathFrames = new Array<>();
        for (int i = 0; i <= 10; i++) {
            String fileName = String.format("HKassets/boss/DeathLand_%03d.png", i);
            Texture tex = new Texture(Gdx.files.internal(fileName));
            allocatedTextures.add(tex);
            deathFrames.add(new TextureRegion(tex));
        }
        deathAnim = new Animation<>(0.15f, deathFrames);

        // تلاش برای بارگذاری عکس افکت موج ضربه روی زمین
        try {
            waveTexture = new Texture(Gdx.files.internal("HKassets/boss/shockwave.png"));
            allocatedTextures.add(waveTexture);
        } catch (Exception e) {
            // اگر فایل پیدا نشد، از فریم آخر انیمیشن حمله به عنوان بک‌آپ استفاده کن
            if (attackFrames.size > 8) {
                waveTexture = attackFrames.get(8).getTexture();
            }
        }
    }

    // متد اصلی رندر کردن باس فایت
    public void render(SpriteBatch batch, FalseKnight boss, float deltaTime) {
        if (boss.isDead) return; // اگر کاملاً نابود شده بود چیزی رسم نکن

        TextureRegion currentFrame = null;
        float stateTime = boss.getAnimationTime();

        // انتخاب فریم مناسب بر اساس وضعیت (State) فعلی ماشین حالت باس
        switch (boss.currentState) {
            case INACTIVE:
            case IDLE:
                currentFrame = idleAnim.getKeyFrame(stateTime, true);
                break;
            case MACE_SLAM:
                currentFrame = maceSlamAnim.getKeyFrame(stateTime, false);
                break;
            case MACE_RECOVER:
                currentFrame = recoverAnim.getKeyFrame(stateTime, false);
                break;
            case CHARGE_RUN:
                currentFrame = runAnim.getKeyFrame(stateTime, true);
                break;
            case OFFENSIVE_LEAP:
            case DEFENSIVE_LEAP:
                currentFrame = jumpAnim.getKeyFrame(stateTime, false);
                break;
            case MEGA_SLAM:
                currentFrame = megaSlamAnim.getKeyFrame(stateTime, false);
                break;
            case STUNNED:
                currentFrame = stunnedAnim.getKeyFrame(stateTime, true);
                break;
            case DEATH:
                // پخش انیمیشن مرگ بدون تکرار (روی فریم آخر قفل می‌شود)
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
                break;
            default:
                currentFrame = idleAnim.getKeyFrame(stateTime, true);
                break;
        }

        // رسم فریم نهایی باس روی صفحه
        if (currentFrame != null) {
            boolean flipX = boss.facingRight;
            // بررسی و اعمال فلیپ افقی عکس بر اساس جهت نگاه کردن باس
            if ((flipX && !currentFrame.isFlipX()) || (!flipX && currentFrame.isFlipX())) {
                currentFrame.flip(true, false);
            }

            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(
                currentFrame,
                boss.x, boss.y-20,
                boss.width / 2f, boss.height / 2f,
                boss.width, boss.height,
                1f, 1f,
                0f
            );
        }

        // رندر کردن موج ضربه فاز دوم (Shockwave) روی زمین
        if (boss.isWaveActive && waveTexture != null) {
            // تعیین جهت فلیپ موج بر اساس متغیر جهت حرکت آن
            boolean flipWaveX = !boss.waveDirectionRight;

            batch.draw(
                waveTexture,
                boss.waveX, boss.waveY,
                0f, 0f,
                90, 130f,
                1f, 1f,
                0f,
                0, 0,
                waveTexture.getWidth(),
                waveTexture.getHeight(),
                flipWaveX,                           // فلیپ افقی موج
                false
            );
        }
    }

    // متد آزادسازی حافظه تکسچرها
    public void dispose() {
        for (Texture tex : allocatedTextures) {
            if (tex != null) tex.dispose();
        }
        allocatedTextures.clear();
    }
}
