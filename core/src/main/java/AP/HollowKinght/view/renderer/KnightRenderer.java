package AP.HollowKinght.view.renderer;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.animation.KnightAnimations;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class KnightRenderer {
    // کلاس کمکی انیمیشن‌های اصلی شوالیه (دویدن، سکون، پرش و...)
    private KnightAnimations animations;

    // انیمیشن‌های افکت ضربه ناخن (Nail Slash)
    private Animation<TextureRegion> nailSlashAnim;

    // انیمیشن‌های مربوط به جادوهای استاندارد (روح سفید)
    private Animation<TextureRegion> fireballCastAnim;
    private Animation<TextureRegion> soulBallAnim;
    private Animation<TextureRegion> screamAnim;
    private Animation<TextureRegion> soulScreamAnim;

    // انیمیشن‌های وضعیت تمرکز و پر کردن جان (Focus)
    private Animation<TextureRegion> focusKnightAnim;
    private Animation<TextureRegion> focusOverlayAnim;

    // انیمیشن‌های پیشرفته مربوط به چارم‌های لایف‌تایم و دارک (اسپل‌های سیاه)
    private Animation<TextureRegion> shadowDashAnim;
    private Animation<TextureRegion> shadowBallAnim;
    private Animation<TextureRegion> shadowScreamAnim;

    public KnightRenderer() {
        // ۱. مقداردهی و ساخت پکیج انیمیشن‌های حرکتی پایه شوالیه
        animations = new KnightAnimations();

        // تنظیم حالت پخش انیمیشن پرش به صورت غیر تکراری (نرمال)
        if (animations.jumpAnim != null) {
            animations.jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }

        // ۲. بارگذاری فریم‌های افکت ضربه ناخن معمولی (Nail Slash)
        Array<TextureRegion> slashFrames = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String fileName = String.format("HKassets/knight/SlashEffect_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                slashFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        nailSlashAnim = new Animation<>(0.035f, slashFrames, Animation.PlayMode.NORMAL);

        // ۳. بارگذاری ۷ فریم انیمیشن فوکوس خود شوالیه (مدت کل ۱.۵ ثانیه)
        Array<TextureRegion> focusFrames = new Array<>();
        for (int i = 0; i <= 6; i++) {
            String fileName = String.format("HKassets/hud/Focus_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                focusFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        focusKnightAnim = new Animation<>(1.5f / 7f, focusFrames, Animation.PlayMode.NORMAL);

        // ۴. بارگذاری ۴ فریم افکت خطوط سفید انرژی روی سر شوالیه (حالت تکرار شونده یا Loop)
        Array<TextureRegion> overlayFrames = new Array<>();
        for (int i = 265; i <= 268; i++) {
            String fileName = String.format("HKassets/hud/HUD Cln_%d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                overlayFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        focusOverlayAnim = new Animation<>(0.10f, overlayFrames, Animation.PlayMode.LOOP);

        // ۵. بارگذاری انیمیشن‌های پرتاب جادوی فایربال (مرحله شارژ بدنه)
        Array<TextureRegion> fireballCastFrames = new Array<>();
        for (int i = 0; i <= 8; i++) {
            String fileName = String.format("HKassets/knight/Fireball Cast_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                fireballCastFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        fireballCastAnim = new Animation<>(0.06f, fireballCastFrames, Animation.PlayMode.NORMAL);

        // ۶. بارگذاری خود تیر پرتابه جادوی روح سفید (Soul Ball)
        Array<TextureRegion> soulBallFrames = new Array<>();
        for (int i = 0; i <= 3; i++) {
            String fileName = String.format("HKassets/knight/SoulBall_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                soulBallFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        soulBallAnim = new Animation<>(0.12f, soulBallFrames, Animation.PlayMode.NORMAL);

        // ۷. بارگذاری بدنه جادوی جیغ صوتی رو به بالا (Scream)
        Array<TextureRegion> screamFrames = new Array<>();
        for (int i = 0; i <= 6; i++) {
            String fileName = String.format("HKassets/knight/Scream_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                screamFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        screamAnim = new Animation<>(0.12f, screamFrames, Animation.PlayMode.NORMAL);

        // ۸. بارگذاری افکت موج و انفجار سفید بالای سر شوالیه (Soul Scream)
        Array<TextureRegion> soulScreamFrames = new Array<>();
        for (int i = 0; i <= 10; i++) {
            String fileName = String.format("HKassets/knight/SoulScream_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                soulScreamFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        soulScreamAnim = new Animation<>(0.1f, soulScreamFrames, Animation.PlayMode.LOOP);

        // ۹. بارگذاری انیمیشن تاریک حرکت سریع (Shadow Dash) - مبنای تصاویر رو به چپ است
        Array<TextureRegion> shadowDashFrames = new Array<>();
        for (int i = 0; i <= 10; i++) {
            String fileName = String.format("HKassets/knight/Shadow Dash_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                shadowDashFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        shadowDashAnim = new Animation<>(0.09f, shadowDashFrames, Animation.PlayMode.NORMAL);

        // ۱۰. بارگذاری پرتابه ارتقایافته سیاه مپ (Shadow Ball)
        Array<TextureRegion> shadowBallFrames = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String fileName = String.format("HKassets/knight/ShadowBall_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                shadowBallFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        shadowBallAnim = new Animation<>(0.08f, shadowBallFrames, Animation.PlayMode.NORMAL);

        // ۱۱. بارگذاری انیمیشن انفجار شبح‌وار و ارتقایافته سیاه (Shadow Scream)
        Array<TextureRegion> shadowScreamFrames = new Array<>();
        for (int i = 0; i <= 12; i++) {
            String fileName = String.format("HKassets/knight/ShadowScream_%03d.png", i);
            if (Gdx.files.internal(fileName).exists()) {
                shadowScreamFrames.add(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
            }
        }
        shadowScreamAnim = new Animation<>(0.095f, shadowScreamFrames, Animation.PlayMode.LOOP);
    }

    // متد اصلی آپدیت زمان و رسم فریم‌های متحرک شوالیه
    public void render(SpriteBatch batch, Knight knight, float dt) {
        // بروزرسانی تمام تایمرهای داخلی مدل بازیکن
        knight.updateTimers(dt);

        Color originalColor = batch.getColor().cpy();

        // پیاده‌سازی افکت چشمک‌زن (Blink) زمان دریافت دمیج و فعال بودن زمان رویین‌تنی
        if (knight.invulnerableTimer > 0) {
            boolean flashToggle = ((int) (knight.invulnerableTimer / 0.08f)) % 2 == 0;
            if (flashToggle) {
                // شوالیه را در این فریم نیمه‌شفاف کن
                batch.setColor(originalColor.r, originalColor.g, originalColor.b, 0.25f);
            } else {
                batch.setColor(originalColor.r, originalColor.g, originalColor.b, 1.0f);
            }
        }

        TextureRegion currentFrame = null;
        boolean isCurrentFrameBaseFlippedLeft = false; // پرچم نشان‌دهنده چپ‌بیس بودن تصاویر خام ورودی

        // ماشین حالت (State Machine) برای انتخاب فریم اکشن جاری شوالیه
        switch (knight.currentState) {
            case IDLE:
                currentFrame = animations.idleAnim.getKeyFrame(knight.stateTime);
                break;
            case RUN:
            case RUN_TO_IDLE:
                currentFrame = animations.runAnim.getKeyFrame(knight.stateTime);
                break;

            case JUMP:
            case AIRBORNE:
            case FALL:
                // تفکیک فریم‌های صعود و سقوط بر اساس سرعت عمودی (velocityY)
                if (knight.velocityY > 300f) {
                    currentFrame = animations.jumpAnim.getKeyFrame(0.0f);
                } else if (knight.velocityY > 50f) {
                    currentFrame = animations.jumpAnim.getKeyFrame(animations.jumpAnim.getAnimationDuration());
                } else if (knight.velocityY >= -50f && knight.velocityY <= 50f) {
                    currentFrame = animations.airborneAnim.getKeyFrame(knight.stateTime);
                } else if (knight.velocityY > -300f) {
                    currentFrame = animations.fallAnim.getKeyFrame(0.0f);
                } else {
                    currentFrame = animations.fallAnim.getKeyFrame(animations.fallAnim.getAnimationDuration());
                }
                break;

            case LANDING:
                currentFrame = animations.landingAnim.getKeyFrame(knight.stateTime);
                break;
            case DOUBLE_JUMP:
                currentFrame = animations.doubleJumpAnim.getKeyFrame(knight.stateTime);
                break;
            case DASHING:
                // اگر چارم سایه فعال است، انیمیشن پیشرفته دارک‌دش را رسم کن
                if (knight.hasSharpShadow && shadowDashAnim != null) {
                    currentFrame = shadowDashAnim.getKeyFrame(knight.stateTime);
                    isCurrentFrameBaseFlippedLeft = false; // تصاویر شادو دش ذاتا چپ‌گرا هستند
                } else {
                    currentFrame = animations.dashAnim.getKeyFrame(knight.stateTime);
                }
                break;
            case WALL_SLIDE:
                currentFrame = animations.wallSlideAnim.getKeyFrame(knight.stateTime);
                break;
            case WALL_JUMPING:
                currentFrame = animations.wallJumpAnim.getKeyFrame(knight.stateTime);
                break;
            case ATTACKING:
                // تفکیک جهت ضربات ناخن (پایین، بالا یا مستقیم روبرو)
                if (knight.isPogoAttack) {
                    currentFrame = animations.downSlashAnim.getKeyFrame(knight.stateTime);
                } else if (knight.isUpAttack) {
                    currentFrame = animations.upSlashAnim != null ? animations.upSlashAnim.getKeyFrame(knight.stateTime) : (knight.isGrounded ? animations.idleAnim.getKeyFrame(knight.stateTime) : animations.airborneAnim.getKeyFrame(knight.stateTime));
                } else {
                    currentFrame = knight.isGrounded ? animations.runAnim.getKeyFrame(knight.stateTime) : animations.airborneAnim.getKeyFrame(knight.stateTime);
                }
                break;

            case FOCUSING:
                currentFrame = focusKnightAnim.getKeyFrame(knight.focusTimer, false);
                break;
            case CAST_FIREBALL:
                if (fireballCastAnim != null) {
                    currentFrame = fireballCastAnim.getKeyFrame(knight.stateTime, false);
                }
                break;
            case CAST_SCREAM:
                if (screamAnim != null) {
                    currentFrame = screamAnim.getKeyFrame(knight.stateTime, false);
                }
                break;
        }

        // بررسی فلیپ افقی تصاویر متناسب با جهت نگاه کردن کاراکتر
        if (currentFrame != null) {
            if (isCurrentFrameBaseFlippedLeft) {
                if (knight.facingRight && currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                } else if (!knight.facingRight && !currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                }
            } else {
                if (knight.facingRight && !currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                } else if (!knight.facingRight && currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                }
            }

            // تنظیم افست افقی موقع لیز خوردن روی دیوار
            float renderX = knight.x;
            if (knight.currentState == Knight.State.WALL_SLIDE) {
                if (knight.facingRight) renderX += 20.0f;
                else renderX -= 20.0f;
            }

            // رسم نهایی بدنه خود شوالیه
            batch.draw(currentFrame, renderX, knight.y, knight.RENDER_WIDTH, knight.RENDER_HEIGHT);
        }

        // رسم افکت اورلی خطوط نورانی سفید در زمان پر کردن خون
        if (knight.currentState == Knight.State.FOCUSING) {
            TextureRegion overlayFrame = focusOverlayAnim.getKeyFrame(knight.focusTimer, true);
            if (overlayFrame != null) {
                if (knight.facingRight && !overlayFrame.isFlipX()) overlayFrame.flip(true, false);
                else if (!knight.facingRight && overlayFrame.isFlipX()) overlayFrame.flip(true, false);

                batch.draw(overlayFrame, knight.x+35, knight.y, knight.RENDER_WIDTH-80, knight.RENDER_HEIGHT-80);
            }
        }

        // بازگرداندن رنگ بچ به مقدار اصلی پروژه
        batch.setColor(originalColor);

        // --- مدیریت و رندر افکت‌های ضربه شمشیر (Nail Slash Effects) ---
        if (knight.drawSlashEffect) {
            TextureRegion slashFrame = nailSlashAnim.getKeyFrame(knight.slashEffectTime);
            if (slashFrame != null) {
                float sW = 190f;
                float sH = 140f;
                float centerY = knight.y + (knight.RENDER_HEIGHT / 2f) - (sH / 2f);

                if (knight.slashDirection.equals("RIGHT")) {
                    float sX = knight.x + knight.hitboxOffsetX + knight.width - 42f;
                    if (!slashFrame.isFlipX()) slashFrame.flip(true, false);
                    batch.draw(slashFrame, sX-20, centerY - 15f, sW, sH);
                }
                else if (knight.slashDirection.equals("LEFT")) {
                    float sX = knight.x + knight.hitboxOffsetX - sW + 42f;
                    if (slashFrame.isFlipX()) slashFrame.flip(true, false);
                    batch.draw(slashFrame, sX+20, centerY - 15f, sW, sH);
                }
                else if (knight.slashDirection.equals("DOWN")) {
                    float sX = knight.x + knight.hitboxOffsetX + (knight.width / 2f) - (sW / 2f);
                    float sY = knight.y - 55f;

                    if (!slashFrame.isFlipX()) slashFrame.flip(true, false);
                    batch.draw(slashFrame, sX+17, sY-20, sW / 2f, sH / 2f, sW, sH, 1f, 1f, -90f);
                }
                else if (knight.slashDirection.equals("UP")) {
                    float sX = knight.x + knight.hitboxOffsetX + (knight.width / 2f) - (sW / 2f);
                    float sY = knight.y + knight.height + 10f;

                    if (!slashFrame.isFlipX()) slashFrame.flip(true, false);
                    batch.draw(slashFrame, sX-20, sY-70, sW / 2f, sH / 2f, sW, sH, 1f, 1f, 90f);
                }
            }
            // غیرفعال کردن افکت به محض اتمام کل تایم انیمیشن ناخن
            if (nailSlashAnim.isAnimationFinished(knight.slashEffectTime)) {
                knight.drawSlashEffect = false;
            }
        }

        // --- رندر کل پرتابه‌های جادوی فایربال موجود در بازی ---
        if (soulBallAnim != null) {
            for (GameController.FireballSpell f : GameController.getInstance().fireballs) {
                TextureRegion fFrame;

                // اگر بازیکن قلب وید (Void Heart) دارد، انیمیشن تیر سیاه پرتاب شود
                if (knight.hasVoidHeart && shadowBallAnim != null) {
                    if (shadowBallAnim.isAnimationFinished(f.stateTime)) {
                        fFrame = shadowBallAnim.getKeyFrame(0.4f, false); // قفل فریم روی حالت آخر
                    } else {
                        fFrame = shadowBallAnim.getKeyFrame(f.stateTime, false);
                    }
                } else {
                    fFrame = soulBallAnim.getKeyFrame(f.stateTime, false);
                }

                if (fFrame != null) {
                    boolean shouldFlip = !f.facingRight;
                    if (shouldFlip && !fFrame.isFlipX()) fFrame.flip(true, false);
                    else if (!shouldFlip && fFrame.isFlipX()) fFrame.flip(true, false);
                    batch.draw(fFrame, f.x - 20f, f.y - 10f, f.width * 1.5f, f.height * 1.5f);
                }
            }
        }

        // --- رندر کل اسپل‌های جیغ صوتی فعال در مپ ---
        if (soulScreamAnim != null) {
            for (GameController.ScreamSpell s : GameController.getInstance().screams) {
                TextureRegion sFrame;

                // سوئیچ به انیمیشن ارتقایافته سیاه جیغ روح (Shadow Scream)
                if (knight.hasVoidHeart && shadowScreamAnim != null) {
                    sFrame = shadowScreamAnim.getKeyFrame(s.stateTime, true);
                } else {
                    sFrame = soulScreamAnim.getKeyFrame(s.stateTime, true);
                }

                if (sFrame != null) {
                    float sW = 400f;
                    float sH = 350f;
                    float sX = s.spawnX + knight.width / 2f - sW / 2f;
                    float sY = s.spawnY + knight.height - 30f;
                    batch.draw(sFrame, sX, sY, sW, sH);
                }
            }
        }
    }
}
