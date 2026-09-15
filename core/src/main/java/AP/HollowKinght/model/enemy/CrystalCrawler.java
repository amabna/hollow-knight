package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import com.badlogic.gdx.math.Rectangle;

public class CrystalCrawler extends Enemy {
    private float speed = 120f;
    private boolean movingRight = true;
    public float stateTime = 0f;

    private float velocityY = 0f;
    private final float GRAVITY = -1000f;
    private boolean isGrounded = false;

    public CrystalCrawler(float x, float y) {
        super(x, y, 80f, 90f, 280);
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        if (isDead) return;

        // بخش مدیریت ناک‌بک: وقتی ضربه میخوره و پرت میشه عقب
        if (knockbackTimer > 0) {
            knockbackTimer -= deltaTime;
            x += knockbackVelX * deltaTime;
            updateHitbox();

            // اگه در حال عقب رفتن خورد به دیوار، متوقفش کن
            if (checkWallCollision()) {
                knockbackVelX = 0;
                knockbackTimer = 0;
            }

            // سنسور جلو پای انمی برای اینکه موقع عقب‌عقب رفتن از پرتگاه نیفته
            boolean goingRight = (knockbackVelX > 0);
            float sensorX = goingRight ? (x + width) : (x - 4f);
            Rectangle ledgeSensor = new Rectangle(sensorX, y - 6f, 4f, 6f);
            boolean groundAhead = false;

            // چک کردن اینکه ایا زیر سنسور زمین هست یا نه
            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String type = trigger.type.toLowerCase();
                if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                    if (ledgeSensor.overlaps(new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height))) {
                        groundAhead = true;
                        break;
                    }
                }
            }

            // اگه جلوش زمین نبود، همونجا روی لبه پرتگاه متوقفش کن
            if (!groundAhead) {
                knockbackVelX = 0;
                knockbackTimer = 0;
            }
            return;
        }

        this.stateTime += deltaTime;

        // اگه روی زمین نیست، جاذبه رو روش اعمال کن تا بیفته پایین
        if (!isGrounded) {
            velocityY += GRAVITY * deltaTime;
            y += velocityY * deltaTime;
            updateHitbox();

            // بررسی برخورد با زمین یا پلتفرم موقع سقوط
            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String type = trigger.type.toLowerCase();
                if (type.equals("floor") || type.equals("plat")) {
                    Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (this.hitbox.overlaps(obstacle) && velocityY <= 0) {
                        y = obstacle.y + obstacle.height;
                        velocityY = 0;
                        isGrounded = true;
                        break;
                    }
                }
            }
            return;
        }

        // حرکت عادی و گشت‌زنی روی پلتفرم
        float velocityX = movingRight ? speed : -speed;
        x += velocityX * deltaTime;
        updateHitbox();

        boolean hitWall = checkWallCollision();

        // سنسور جلو پا برای تشخیص لبه پرتگاه در حالت حرکت عادی
        float sensorX = movingRight ? (x + width) : (x - 4f);
        Rectangle ledgeSensor = new Rectangle(sensorX, y - 6f, 4f, 6f);
        boolean groundAhead = false;

        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                if (ledgeSensor.overlaps(new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height))) {
                    groundAhead = true;
                    break;
                }
            }
        }

        // اگه به دیوار خورد یا به ته پلتفرم رسید، جهتش رو عوض کن
        if (hitWall || !groundAhead) {
            movingRight = !movingRight;
        }

        checkStillGrounded();
        updateHitbox();

        // اگه بدنش به شوالیه خورد، به شوالیه دمیج بزن
        if (this.hitbox.overlaps(knightHitbox)) {
            GameController.getInstance().getKnightController().takeDamage(this.x + this.width / 2f);
        }
    }

    // متد چک کردن برخورد با دیوارهای نقشه
    private boolean checkWallCollision() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat") || type.equals("tiz")) {
                Rectangle obstacle = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(obstacle)) {
                    // تنظیم موقعیت دقیق انمی کنار دیوار بر اساس جهت حرکتش
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

    // چک میکنه که آیا انمی هنوز هم روی زمین قرار داره یا پلتفرم زیر پاش تموم شده
    private void checkStillGrounded() {
        Rectangle groundCheck = new Rectangle(x, y - 2f, width, 2f);
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String type = trigger.type.toLowerCase();
            if (type.equals("floor") || type.equals("plat")) {
                if (groundCheck.overlaps(new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height))) {
                    return;
                }
            }
        }
        isGrounded = false;
    }

    public boolean isMovingRight() { return movingRight; }
    @Override public float getAnimationTime() { return stateTime; }
    @Override public String getEnemyType() { return "tiktik"; }
}
