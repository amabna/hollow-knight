package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import com.badlogic.gdx.math.Rectangle;

public class Crystalized extends Enemy {
    public enum State { FALLING, IDLE, FIRING_LASER, ENRAGED_CHARGE }
    private State currentState = State.FALLING;

    private boolean facingRight = true;
    private float chargeSpeed = 250f;

    private float stateTimer = 0f;
    private float animTimer = 0f;
    private float velocityY = 0f;
    private final float GRAVITY = -1100f;

    private final float VISION_WIDTH = 900f;
    private final float VISION_HEIGHT = 120f;

    private final float LASER_CHARGE_TIME = 0.6f;
    private final float LASER_TOTAL_TIME = 0.9f;
    public float laserBallX = 0f;
    public float laserBallY = 0f;
    private float laserBallVelX = 0f;
    private final float LASER_BALL_SPEED = 350f;

    private final float LASER_BALL_SIZE = 10f;
    private boolean laserFired = false;
    public float laserAnimTimer = 0f;

    private final float ENRAGED_MAX_TIME = 1.5f;
    private float enragedTimer = 0f;

    public boolean isLaserActive = false;
    public Rectangle laserBounds;

    private final float spawnX;
    private final float spawnY;

    public Crystalized(float x, float y, boolean ignoredFacing) {
        super(x, y, 70f, 110f, 520);
        this.spawnX = x;
        this.spawnY = y;
        this.facingRight = true;
        this.laserBounds = new Rectangle(0, 0, 0, 0);
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        if (isDead) {
            isLaserActive = false;
            return;
        }

        stateTimer += deltaTime;
        animTimer += deltaTime;

        // اگه انمی به هر دلیلی مثل باگ فیزیک از مپ بیرون پرت شد، برش گردون سر جاش
        if (Math.abs(this.y - spawnY) > 3000f || Math.abs(this.x - spawnX) > 3000f) {
            this.x = spawnX;
            this.y = spawnY;
            this.velocityY = 0f;
            this.currentState = State.FALLING;
            updateHitbox();
        }

        // مدیریت حرکت ناک‌بک انمی موقع ضربه خوردن
        if (this.knockbackTimer > 0) {
            this.knockbackTimer -= deltaTime;
            this.x += this.knockbackVelX * deltaTime;
            updateHitbox();
            if (checkWallCollision()) {
                this.knockbackVelX = 0;
                this.knockbackTimer = 0;
            }
        }

        // برخورد فیزیکی بدنه خود انمی با شوالیه
        if (this.hitbox.overlaps(knightHitbox)) {
            GameController.getInstance().getKnightController().takeDamage(this.x + this.width / 2f);
        }

        // مدیریت حرکت و برخورد گلوله لیزر در مپ
        if (isLaserActive) {
            laserAnimTimer += deltaTime;
            laserBallX += laserBallVelX * deltaTime;

            // به روز رسانی موقعیت مستطیل برخورد لیزر
            laserBounds.set(laserBallX, laserBallY, LASER_BALL_SIZE, LASER_BALL_SIZE);

            // اگه لیزر به شوالیه خورد، دمیج بزن و لیزر رو غیب کن
            if (laserBounds.overlaps(knightHitbox)) {
                GameController.getInstance().getKnightController().takeDamage(laserBallX + LASER_BALL_SIZE / 2f);
                isLaserActive = false;
            }
            // اگه لیزر به دیوار خورد، غیبش کن
            else if (checkLaserWallCollision()) {
                isLaserActive = false;
            }
        }

        // ماشین وضعیت هوش مصنوعی انمی کریستالی
        switch (currentState) {
            case FALLING:
                // در حال سقوط آزاد تا زمانی که به زمین برسد
                velocityY += GRAVITY * deltaTime;
                this.y += velocityY * deltaTime;
                updateHitbox();

                if (checkFloorCollision()) {
                    velocityY = 0;
                    currentState = State.IDLE;
                    stateTimer = 0f;
                }
                break;

            case IDLE:
                // در حال استراحت، اگه شوالیه رو دید بچرخ سمتش و آماده شلیک شو
                if (canSeeKnight(knightHitbox)) {
                    facingRight = (knightHitbox.x + knightHitbox.width / 2f) > (this.x + this.width / 2f);
                    currentState = State.FIRING_LASER;
                    stateTimer = 0f;
                    animTimer = 0f;
                    laserFired = false;
                }
                break;

            case FIRING_LASER:
                // فاز شارژ و شلیک کردن لیزر کریستالی
                if (stateTimer >= LASER_CHARGE_TIME && !laserFired) {
                    isLaserActive = true;
                    laserAnimTimer = 0f;

                    // مشخص کردن مختصات اولیه شلیک بر اساس جهت نگاه انمی
                    laserBallX = facingRight ? (this.x + width) : (this.x - LASER_BALL_SIZE);
                    laserBallY = this.y + 35f; // ۳۵ پیکسل بالاتر از زمین تا به کف نچسبه

                    laserBallVelX = facingRight ? LASER_BALL_SPEED : -LASER_BALL_SPEED;
                    laserBounds.set(laserBallX, laserBallY, LASER_BALL_SIZE, LASER_BALL_SIZE);
                    laserFired = true;
                }

                // بعد از تموم شدن زمان شلیک، برو تو حالت دویدن خشمگین
                if (stateTimer >= LASER_TOTAL_TIME) {
                    currentState = State.ENRAGED_CHARGE;
                    stateTimer = 0f;
                    animTimer = 0f;
                    enragedTimer = 0f;
                }
                break;

            case ENRAGED_CHARGE:
                // دویدن سریع به سمت شوالیه
                enragedTimer += deltaTime;
                float chargeVel = facingRight ? chargeSpeed : -chargeSpeed;
                this.x += chargeVel * deltaTime;
                updateHitbox();

                boolean hitWall = checkWallCollision();
                boolean hitLedge = checkLedge();
                boolean hitKnight = this.hitbox.overlaps(knightHitbox);
                boolean timeout = enragedTimer >= ENRAGED_MAX_TIME;

                // اگه به مانع خورد یا زمانش تموم شد، متوقف شو و دوباره چک کن شوالیه رو میبینه یا نه
                if (hitWall || hitLedge || hitKnight || timeout) {
                    currentState = State.IDLE;
                    stateTimer = 0f;
                    animTimer = 0f;
                    enragedTimer = 0f;

                    if (canSeeKnight(knightHitbox)) {
                        facingRight = (knightHitbox.x + knightHitbox.width / 2f) > (this.x + this.width / 2f);
                        currentState = State.FIRING_LASER;
                        laserFired = false;
                    }
                }
                break;
        }
    }

    // بررسی خط دید انمی که ایا نایت در محدوده افقی و عمودی دیدش هست یا نه
    private boolean canSeeKnight(Rectangle knightHitbox) {
        float crysCenterX = this.x + this.width / 2f;
        float crysCenterY = this.y + this.height / 2f;
        float knightCenterX = knightHitbox.x + knightHitbox.width / 2f;
        float knightCenterY = knightHitbox.y + knightHitbox.height / 2f;

        if (Math.abs(crysCenterY - knightCenterY) <= VISION_HEIGHT) {
            if (facingRight && knightCenterX > crysCenterX && (knightCenterX - crysCenterX) <= VISION_WIDTH) {
                return true;
            } else if (!facingRight && knightCenterX < crysCenterX && (crysCenterX - knightCenterX) <= VISION_WIDTH) {
                return true;
            }
        }
        return false;
    }

    // چک کردن برخورد با زمین موقع سقوط
    private boolean checkFloorCollision() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat")) {
                Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(obstacle)) {
                    this.y = obstacle.y + obstacle.height;
                    updateHitbox();
                    return true;
                }
            }
        }
        return false;
    }

    // چک کردن برخورد فیزیکی بدنه انمی با دیوارهای نقشه
    private boolean checkWallCollision() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(obstacle)) {
                    boolean goingRight = (this.knockbackTimer > 0) ? (this.knockbackVelX > 0) : facingRight;
                    if (goingRight) this.x = obstacle.x - this.width - 1;
                    else this.x = obstacle.x + obstacle.width + 1;
                    updateHitbox();
                    return true;
                }
            }
        }
        return false;
    }

    // سنسور تشخیص لبه پرتگاه برای جلوگیری از سقوط موقع دویدن خشمگین
    private boolean checkLedge() {
        if (currentState == State.FALLING) return false;

        float sensorX = facingRight ? (this.x + this.width) : (this.x - 4f);
        Rectangle ledgeSensor = new Rectangle(sensorX, this.y - 6f, 4f, 6f);

        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                if (ledgeSensor.overlaps(new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height))) {
                    return false;
                }
            }
        }
        return true;
    }

    // چک کردن برخورد تیر لیزر با موانع ثابت مپ
    private boolean checkLaserWallCollision() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.laserBounds.overlaps(obstacle)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateHitbox() {
        this.hitbox.set(this.x, this.y, this.width, this.height);
    }

    public boolean isFacingRight() { return this.facingRight; }
    public State getCurrentState() { return this.currentState; }
    @Override public float getAnimationTime() { return this.animTimer; }
    @Override public String getEnemyType() { return "crystalized"; }
}
