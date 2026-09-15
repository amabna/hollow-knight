package AP.HollowKinght.model.player;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.model.core.Entity;
import com.badlogic.gdx.math.Rectangle;

public class Knight extends Entity {
    // تمامی وضعیت‌های حرکتی و اکشن‌های شوالیه در طول بازی
    public enum State {
        IDLE, RUN, RUN_TO_IDLE, JUMP, AIRBORNE, FALL, LANDING,
        DOUBLE_JUMP, DASHING, WALL_SLIDE, WALL_JUMPING, ATTACKING,
        FOCUSING, CAST_FIREBALL, CAST_SCREAM
    }

    // چارم‌های فعال اینونتوری شوالیه
    public boolean hasSoulCatcher = false;
    public boolean hasDashmaster = false;
    public boolean hasUnbreakableStrength = false;
    public boolean hasQuickSlash = false;
    public boolean hasQuickFocus = false;
    public boolean hasHeavyBlow = false;
    public boolean hasSharpShadow = false;
    public boolean hasVoidHeart = false;

    // وضعیت اچیومنت‌های باز شده
    public boolean ach1 = false;
    public boolean ach2 = false;
    public boolean ach3 = false;
    public boolean ach4 = false;
    public boolean ach5 = false;

    // ماب‌های کشته شده برای ثبت آمار بازی
    public boolean killedTiktik = false;
    public boolean killedHusk = false;
    public boolean killedMosq = false;
    public boolean killedCrys = false;

    public int totalMobsKilled = 0;   // تعداد کل دشمنان شکست خورده
    public int knightDeathCount = 0;  // تعداد دفعات مرگ بازیکن

    public State currentState = State.IDLE;
    public boolean facingRight = true;
    public boolean isGrounded = false;

    public float attackCooldownTimer = 0f;
    public float attackPause = 0.5f;
    public float dashCooldownTimer = 0f;
    public float dashPause = 0.5f;

    public boolean canDoubleJump = true;
    public boolean canDash = true;

    // تایمرهای داخلی مدیریت وضعیت انیمیشن و کنترلرها
    public float stateTime = 0f;
    public float landingTimer = 0f;
    public float runToIdleTimer = 0f;
    public float dashTimer = 0f;
    public float attackTimer = 0f;
    public float wallJumpTimer = 0f;

    public final float DASH_DURATION = 0.2f;
    public final float DASH_SPEED = 800f;
    public final float ATTACK_DURATION = 0.22f;

    public boolean isPogoAttack = false; // ضربه به پایین روی انمی
    public boolean isUpAttack = false;   // ضربه رو به بالا
    public Rectangle attackHitbox;
    public float attackDamage = 100.0f;

    // فیزیک ثوابت حرکتی شوالیه
    public final float MOVE_SPEED = 400f;
    public final float JUMP_VELOCITY = 850f;
    public final float GRAVITY = -1700f;
    public final float WALL_SLIDE_SPEED = -220f;

    // ابعاد رندرینگ گرافیکی و آفست‌های هیت‌باکس واقعی
    public final float RENDER_WIDTH = 250f;
    public final float RENDER_HEIGHT = 150f;
    public float hitboxOffsetX = 95f;
    public float hitboxOffsetY = 0f;

    public int health = 5; // تعداد ماسک‌های جان پلیر
    public float invulnerableTimer = 0f;
    public float flashTimer = 0f;
    public boolean isVisible = true;
    public float knockbackTimer = 0f;
    public float knockbackVelX = 0f;

    // لرزش صفحه هنگام آسیب دیدن
    public float shakeTime = 0f;
    public float shakeIntensity = 0f;

    // افکت خط شمشیر (Slash Effect)
    public boolean drawSlashEffect = false;
    public float slashEffectTime = 0f;
    public String slashDirection = "RIGHT";

    public int soul = 0; // روح جمع‌آوری شده برای اسپل یا هیل
    public final int MAX_SOUL = 99;
    public float focusTimer = 0f;
    public float[] maskAnimationStates = new float[]{-1f, -1f, -1f, -1f, -1f};

    // وضعیت کدهای تقلب (چیت کدها)
    public boolean isNoclip = false;
    public boolean isGodMode = false;

    public Knight(float x, float y) {
        super(x, y, 60, 100);
        this.attackHitbox = new Rectangle();
    }

    public void changeState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.stateTime = 0f;

            if (newState == State.ATTACKING) {
                String dir = isPogoAttack ? "DOWN" : (facingRight ? "RIGHT" : "LEFT");
                if (isUpAttack) dir = "UP";
                triggerAttackSlash(dir);
            }
        }
    }

    public void takeDamage(int amount, boolean damageFromRight) {
        // عدم آسیب در صورت داشتن حالت ضد ضربه، حالت خدا، یا مود عبور از دیوار
        if (invulnerableTimer > 0 || isGodMode || isNoclip) return;

        // همپوشانی با قابلیت داش سایه (Sharp Shadow)
        if (GameController.getInstance().getKnightController().knight.currentState == State.DASHING
            && GameController.getInstance().getKnightController().knight.hasSharpShadow) return;

        if (currentState == State.FOCUSING) {
            focusTimer = 0f;
            changeState(State.IDLE);
        }

        // شکستن انیمیشنی ماسک‌ها متناسب با مقدار دمیج ورودی
        for (int i = health - 1; i >= Math.max(0, health - amount); i--) {
            if (i >= 0 && i < 5) {
                maskAnimationStates[i] = 0f;
            }
        }

        health -= amount;
        if (health < 0) {
            health = 0;
            knightDeathCount++;
        }

        // فعال‌سازی لرزش شدید دوربین و زمان رویین‌تنی موقت
        shakeTime = 0.25f;
        shakeIntensity = 8f;

        invulnerableTimer = 1.2f;
        flashTimer = 0f;
        isVisible = true;

        knockbackTimer = 0.2f;
        knockbackVelX = damageFromRight ? -350f : 350f;

        AP.HollowKinght.view.audio.AudioManager.getInstance().playSFX("damage_to_hero.wav");
    }

    public void triggerAttackSlash(String direction) {
        this.drawSlashEffect = true;
        this.slashEffectTime = 0f;
        this.slashDirection = direction;
        AP.HollowKinght.view.audio.AudioManager.getInstance().playSFX("hero_dash.wav");
    }

    public void updateTimers(float dt) {
        stateTime += dt;

        // رفع مشکل فریز حرکت در حالت Noclip (لغو دائمی تکانه‌های فیزیکی ناخواسته)
        if (isNoclip) {
            knockbackTimer = 0f;
            knockbackVelX = 0f;
            velocityY = 0f;
        }

        // سیستم چشمک‌زدن کاراکتر در فاز پس از آسیب دیدن
        if (invulnerableTimer > 0) {
            invulnerableTimer -= dt;
            flashTimer += dt;
            if (flashTimer >= 0.08f) {
                isVisible = true;
                flashTimer = 0f;
            }
            if (invulnerableTimer <= 0) {
                isVisible = true;
            }
        }

        if (drawSlashEffect) {
            slashEffectTime += dt;
        }

        if (shakeTime > 0) {
            shakeTime -= dt;
        }
    }

    @Override
    public void updateHitbox() {
        if (isNoclip) {
            hitbox.set(0, 0, 0, 0); // هیت‌باکس صفر برای عبور آزادانه از تمام سطوح تایل‌مپ
        } else {
            hitbox.set(x + hitboxOffsetX, y + hitboxOffsetY, width, height);
        }
    }
}
