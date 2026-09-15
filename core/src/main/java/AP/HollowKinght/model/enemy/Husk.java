package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import com.badlogic.gdx.math.Rectangle;

public class Husk extends Enemy {
    // وضعیت‌های مختلف انمی هاسک در طول بازی
    public enum State { FALLING, WALKING, RESTING, CHARGING }
    public State currentState = State.FALLING;

    private float walkSpeed = 80f;     // سرعت راه رفتن عادی
    private float chargeSpeed = 200f;  // سرعت دویدن و حمله به سمت شوالیه
    private boolean movingRight = true;
    public boolean facingRight = true;

    private float velocityY = 0f;
    private final float GRAVITY = -1100f; // جاذبه زمین برای سقوط

    private float stateTimer = 0f;
    private float animTimer = 0f;

    private final float WALK_DURATION = 3.0f; // مدت زمان راه رفتن قبل از استراحت
    private final float REST_DURATION = 1.5f; // مدت زمان ایستادن و استراحت
    private final float VISION_WIDTH = 300f;  // طول دید افقی هاسک برای شناسایی پلیر
    private final float VISION_HEIGHT = 80f;  // ارتفاع دید هاسک

    public Husk(float x, float y) {
        super(x, y, 130f, 160f, 430);
        this.facingRight = this.movingRight;
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        if (isDead) return;

        animTimer += deltaTime;

        // سیستم ترمز هوشمند ناک‌بک بدون پس‌زدگی لرزشی و رد شدن از دیوار
        if (knockbackTimer > 0) {
            knockbackTimer -= deltaTime;
            x += knockbackVelX * deltaTime;
            updateHitbox();
            // اگه موقع عقب رانده شدن به دیوار یا لبه پرتگاه خورد، ناک‌بک متوقف بشه
            if (checkWallCollision() || checkLedge()) {
                knockbackVelX = 0;
                knockbackTimer = 0;
            }
            return;
        }

        stateTimer += deltaTime;
        facingRight = movingRight;
        updateHitbox();

        // برخورد مستقیم هاسک با شوالیه که باعث آسیب دیدن شوالیه میشه
        if (this.hitbox.overlaps(knightHitbox)) {
            GameController.getInstance().getKnightController().takeDamage(x + width / 2f);
            if (currentState == State.CHARGING) {
                currentState = State.RESTING;
                resetTimers();
            }
        }

        // منطق بینایی هاسک: اگه شوالیه رو توی دیدش ببینه، وضعیتش حمله (Charging) میشه
        if (currentState != State.CHARGING && currentState != State.FALLING) {
            Rectangle visionRect;
            if (movingRight) {
                visionRect = new Rectangle(x + width, y, VISION_WIDTH, VISION_HEIGHT);
            } else {
                visionRect = new Rectangle(x - VISION_WIDTH, y, VISION_WIDTH, VISION_HEIGHT);
            }

            if (visionRect.overlaps(knightHitbox)) {
                currentState = State.CHARGING;
                resetTimers();
            }
        }

        // بررسی ماشین وضعیت هاسک
        switch (currentState) {
            case FALLING: // اعمال فیزیک سقوط تا زمان رسیدن به سطح زمین یا پلتفرم
                velocityY += GRAVITY * deltaTime;
                y += velocityY * deltaTime;
                updateHitbox();

                for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                    if (trigger.type == null) continue;
                    String type = trigger.type.toLowerCase();
                    if (type.equals("floor") || type.equals("plat")) {
                        Rectangle floorRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                        if (this.hitbox.overlaps(floorRect)) {
                            if (velocityY <= 0) {
                                y = floorRect.y + floorRect.height;
                                velocityY = 0;
                                currentState = State.WALKING;
                                resetTimers();
                                break;
                            }
                        }
                    }
                }
                break;

            case WALKING: // راه رفتن عادی روی پلتفرم
                float velocityX = movingRight ? walkSpeed : -walkSpeed;
                x += velocityX * deltaTime;
                updateHitbox();

                // اگه به دیوار یا لبه پلتفرم رسید یا زمانش تموم شد، جهت رو عوض کنه و استراحت کنه
                if (checkWallCollision() || checkLedge()) {
                    changeDirectionToRest();
                }
                else if (stateTimer >= WALK_DURATION) {
                    currentState = State.RESTING;
                    resetTimers();
                }
                break;

            case RESTING: // ایستادن موقت و بدون حرکت
                if (stateTimer >= REST_DURATION) {
                    currentState = State.WALKING;
                    resetTimers();
                }
                break;

            case CHARGING: // دویدن سریع به سمت پلیر برای وارد کردن آسیب
                float chargeVelX = movingRight ? chargeSpeed : -chargeSpeed;
                x += chargeVelX * deltaTime;
                updateHitbox();

                // اگه به مانع خورد یا بیش از ۴ ثانیه دوید و به پلیر نرسید، بیخیال بشه
                if (checkWallCollision() || checkLedge() || stateTimer >= 4.0f) {
                    changeDirectionToRest();
                }
                break;
        }

        updateHitbox();
    }

    private void resetTimers() {
        stateTimer = 0f;
        animTimer = 0f;
    }

    private void changeDirectionToRest() {
        movingRight = !movingRight;
        facingRight = movingRight;
        currentState = State.RESTING;
        resetTimers();
    }

    // متد چک کردن برخورد افقی با دیوارها و موانع مپ
    private boolean checkWallCollision() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(obstacle)) {
                    // بررسی هوشمند جهت فیزیکی هاسک بر اساس ناک‌بک یا حرکت استاندارد
                    boolean goingRight = (knockbackTimer > 0) ? (knockbackVelX > 0) : movingRight;
                    if (goingRight) x = obstacle.x - width - 1;
                    else x = obstacle.x + obstacle.width + 1;
                    updateHitbox();
                    return true;
                }
            }
        }
        return false;
    }

    // متد چک کردن لبه پرتگاه‌ها برای جلوگیری از سقوط خودکار هاسک از روی پلتفرم‌ها
    private boolean checkLedge() {
        boolean goingRight = (knockbackTimer > 0) ? (knockbackVelX > 0) : movingRight;
        float sensorX = goingRight ? (x + width) : (x - 4f);
        Rectangle ledgeSensor = new Rectangle(sensorX, y - 6f, 4f, 6f);

        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                if (ledgeSensor.overlaps(new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height))) {
                    return false; // هنوز روی زمین پلتفرم قرار دارد
                }
            }
        }
        return true; // به لبه پرتگاه رسیده است
    }

    @Override public float getAnimationTime() { return animTimer; }
    @Override public String getEnemyType() { return "husk"; }
}
