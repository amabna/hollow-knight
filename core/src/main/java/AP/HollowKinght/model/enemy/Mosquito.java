package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import AP.HollowKinght.view.audio.AudioManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Mosquito extends Enemy {
    // وضعیت‌های انمی پرنده پشه
    public enum State { HOVER, LOCKING, CHARGING, STUNNED_COOLDOWN }
    public State currentState = State.HOVER;

    private float chargeSpeed = 300f; // سرعت حمله انتحاری پشه به سمت هدف

    public float stateTimer = 0f;
    public float animTimer = 0f;
    private final float LOCK_DELAY = 0.5f;       // زمان قفل کردن روی شوالیه قبل از پرواز هجومی
    private final float MAX_CHARGE_TIME = 1.5f;   // حداکثر زمان مجاز برای فاز حمله
    private final float COOLDOWN_TIME = 1.2f;     // مدت زمان گیج شدن و استراحت بعد از حمله
    private final float AGGRO_RADIUS = 500f;      // شعاع دید دایره‌ای پشه برای فعال شدن

    private float targetX, targetY; // مختصات هدف (موقعیت شوالیه)
    private float dirX, dirY;       // بردار جهت حرکت دو بعدی
    private float startHoverY;      // ارتفاع پایه‌ای که پشه در آن شناور است

    public boolean facingRight = false;

    public Mosquito(float x, float y) {
        super(x, y, 110f, 100f, 350);
        this.startHoverY = y;
    }

    @Override
    public void takeDamage(int amount, boolean hitFromRight) {
        if (isDead) return;

        AudioManager.getInstance().playSFX("enemy_damage.wav");
        this.health -= amount;

        // اعمال دمیج و فعال‌سازی سیستم عقب رانده شدن (Knockback) با در نظر گرفتن ابزار Heavy Blow
        this.knockbackTimer = 0.22f;
        float megaKnockback = 750f;
        float v = (float) (GameController.getInstance().getKnightController().knight.hasHeavyBlow ? 2.0 : 1.0);
        this.knockbackVelX = hitFromRight ? megaKnockback * v : -megaKnockback * v;

        if (this.health <= 0) {
            this.health = 0;
            this.isDead = true;
            float corpseKnockbackX = hitFromRight ? 400f * v : -400f * v;
            boolean corpseFacingRight = !hitFromRight;

            // تولید جسد فیزیکی پشه برای شبیه‌سازی افکت مرگ بازی اصلی
            GameController.getInstance().createCorpse(x, y, width, height, animTimer, corpseFacingRight, "mosq", corpseKnockbackX);
            GameController.getInstance().getKnightController().knight.totalMobsKilled++;
            GameController.getInstance().getKnightController().knight.killedMosq = true;
        } else {
            // پشه پس از ضربه خوردن عصبانی شده و بلافاصله به سمت پلیر حمله می‌کند
            currentState = State.CHARGING;
            stateTimer = 0f;
        }
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        if (isDead) return;

        animTimer += deltaTime;

        // پچ امنیتی تشخیص نوع برخورد دیوار/زمین در فاز ناک‌بک برای جلوگیری از تله‌پورت ناگهانی پشه به داخل موانع
        if (knockbackTimer > 0) {
            knockbackTimer -= deltaTime;
            x += knockbackVelX * deltaTime;
            updateHitbox();

            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String type = trigger.type.toLowerCase();
                if (type.equals("floor") || type.equals("plat") || type.equals("roof")) {
                    Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (this.hitbox.overlaps(obstacle)) {
                        float overlapX = Math.min(this.hitbox.x + this.hitbox.width, obstacle.x + obstacle.width) - Math.max(this.hitbox.x, obstacle.x);
                        float overlapY = Math.min(this.hitbox.y + this.hitbox.height, obstacle.y + obstacle.height) - Math.max(this.hitbox.y, obstacle.y);

                        // فقط در صورتی اسنپ فیزیکی رخ می‌دهد که برخورد از کناره‌ها (دیوار) باشد، نه سایش عمودی
                        if (overlapX < overlapY) {
                            if (knockbackVelX > 0) x = obstacle.x - width - 1;
                            else x = obstacle.x + obstacle.width + 1;
                            updateHitbox();
                            knockbackVelX = 0;
                            knockbackTimer = 0; // متوقف کردن فاز ناک‌بک
                            break;
                        }
                    }
                }
            }
            return;
        }

        stateTimer += deltaTime;

        float knightCenterX = knightHitbox.x + knightHitbox.width / 2f;
        float knightCenterY = knightHitbox.y + knightHitbox.height / 2f;
        float mosquitoCenterX = x + width / 2f;
        float mosquitoCenterY = y + height / 2f;

        if (currentState == State.HOVER || currentState == State.STUNNED_COOLDOWN || currentState == State.CHARGING) {
            facingRight = (knightCenterX > mosquitoCenterX);
        }

        switch (currentState) {
            case HOVER: // پرواز و شناور بودن در آسمان به کمک فرمول سینوسی متماتیکس
                y = startHoverY + MathUtils.sin(stateTimer * 3f) * 15f;

                float distance = McCenterDistance(mosquitoCenterX, mosquitoCenterY, knightCenterX, knightCenterY);
                if (distance <= AGGRO_RADIUS) {
                    targetX = knightCenterX;
                    targetY = knightCenterY;

                    float deltaX = targetX - mosquitoCenterX;
                    float deltaY = targetY - mosquitoCenterY;
                    float len = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (len != 0) {
                        dirX = deltaX / len; // نرمالایز کردن بردار X
                        dirY = deltaY / len; // نرمالایز کردن بردار Y
                    }

                    currentState = State.LOCKING;
                    stateTimer = 0f;
                    animTimer = 0f;
                }
                break;

            case LOCKING: // ایست کوتاه در هوا برای نشانه گیری دقیق روی بازیکن
                if (stateTimer >= LOCK_DELAY) {
                    currentState = State.CHARGING;
                    stateTimer = 0f;
                }
                break;

            case CHARGING: // حرکت خطی مستقیم و سریع دو بعدی به سمت هدف قفل شده
                x += dirX * chargeSpeed * deltaTime;
                y += dirY * chargeSpeed * deltaTime;
                updateHitbox();

                boolean hitWall = false;

                // اصلاحیه فیزیک شارژ: اعمال سیستم جداسازی (Push-out) جهت نفوذ نکردن در دیوارهای زاویه‌دار یا سقف و کف
                for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                    if (trigger.type == null) continue;
                    String type = trigger.type.toLowerCase();
                    if (type.equals("floor") || type.equals("roof") || type.equals("plat")) {
                        Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                        if (this.hitbox.overlaps(obstacle)) {
                            hitWall = true;

                            float overlapX = Math.min(this.hitbox.x + this.hitbox.width, obstacle.x + obstacle.width) - Math.max(this.hitbox.x, obstacle.x);
                            float overlapY = Math.min(this.hitbox.y + this.hitbox.height, obstacle.y + obstacle.height) - Math.max(this.hitbox.y, obstacle.y);

                            if (overlapY < overlapX) {
                                // برخورد عمودی با زمین یا سقف -> پوش اوت عمودی
                                if (this.hitbox.y + this.hitbox.height / 2f < obstacle.y + obstacle.height / 2f) {
                                    y -= (overlapY + 1f);
                                } else {
                                    y += (overlapY + 1f);
                                }
                            } else {
                                // برخورد افقی با دیواره‌ها -> پوش اوت افقی
                                if (this.hitbox.x + this.hitbox.width / 2f < obstacle.x + obstacle.width / 2f) {
                                    x -= (overlapX + 1f);
                                } else {
                                    x += (overlapX + 1f);
                                }
                            }
                            updateHitbox();
                            break;
                        }
                    }
                }

                // برخورد با شوالیه در حین حمله
                if (this.hitbox.overlaps(knightHitbox)) {
                    GameController.getInstance().getKnightController().takeDamage(mosquitoCenterX);
                    enterCooldownState();
                    break;
                }

                if (hitWall || stateTimer >= MAX_CHARGE_TIME) {
                    enterCooldownState();
                }
                break;

            case STUNNED_COOLDOWN: // استراحت و گیج زدن پشه پس از اصابت یا برخورد با دیوار
                if (stateTimer >= COOLDOWN_TIME) {
                    currentState = State.HOVER;
                    stateTimer = 0f;
                }
                break;
        }
        updateHitbox();
    }

    private void enterCooldownState() {
        currentState = State.STUNNED_COOLDOWN;
        stateTimer = 0f;
        animTimer = 0f;
        startHoverY = y;
    }

    @Override public float getAnimationTime() { return animTimer; }
    @Override public String getEnemyType() { return "mosq"; }

    // متد محاسبه فاصله دو نقطه دو بعدی فیثاغورسی
    private float McCenterDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
