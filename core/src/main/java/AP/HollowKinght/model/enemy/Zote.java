package AP.HollowKinght.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import com.badlogic.gdx.utils.Array;

public class Zote extends Enemy {
    public float spawnX, spawnY; // مختصات نقطه اولیه اسپاون زوت برای بازگشت به آن
    public float velocityY = 0f;
    public boolean isGrounded = false;

    // ماشین وضعیت هوش مصنوعی زوت: 0 = ایستا(Idle)، 1 = تعقیب پلیر(Attacking)، 2 = بازگشت به خانه(Returning)
    public int aiState = 0;
    public float aiTimer = 0f;
    public boolean facingRight = false;

    public Zote(float x, float y) {
        // اختصاص مقدار ۱۰۰،۰۰۰ جان به زوت به نشانه نامیرا و شکست‌ناپذیر بودن او در بازی!
        super(x, y, 250f, 200f, 100000);
        this.spawnX = x;
        this.spawnY = y;
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        // ۱. مکانیک شبیه‌سازی جاذبه و سقوط روی زمین برای زوت
        if (!isGrounded) {
            velocityY -= 1000f * deltaTime;
            this.y += velocityY * deltaTime;
        }

        // ۲. تشخیص داینامیک برخورد با سطوح زمین (floor یا plat)
        isGrounded = false;
        Array<MapTrigger> triggers = GameController.getInstance().getMapTriggers();
        if (triggers != null) {
            for (MapTrigger trigger : triggers) {
                if (trigger.type != null && (trigger.type.equalsIgnoreCase("floor") || trigger.type.equalsIgnoreCase("plat"))) {
                    Rectangle platRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);

                    this.hitbox.setPosition(this.x, this.y);

                    // تراز کردن زوت روی لبه زمین در صورت سقوط عمودی
                    if (this.hitbox.overlaps(platRect) && velocityY <= 0) {
                        this.y = trigger.y + trigger.height;
                        velocityY = 0f;
                        isGrounded = true;
                        break;
                    }
                }
            }
        }

        // مدیریت زمان‌بندی و فیزیک فیلد ناک‌بک کلاس والد انمی
        if (this.knockbackTimer > 0) {
            this.knockbackTimer -= deltaTime;
            this.x += this.knockbackVelX * deltaTime;
        }

        float knightX = knightHitbox.x;

        // ۳. مدیریت رفتارهای ماشین وضعیت هوش مصنوعی زوت کبیر
        if (aiState == 1) { // حالت تعقیب و گریز خنده‌دار زوت بعد از کتک خوردن از شوالیه
            aiTimer -= deltaTime;
            float speed = 70f;

            // زوت با اصرار تمام فقط به سمت چپ یا راست مختصات فعلی نایت قدم برمی‌دارد
            if (this.x < knightX) {
                this.x += speed * deltaTime;
            } else {
                this.x -= speed * deltaTime;
            }

            if (aiTimer <= 0f) {
                aiState = 2; // زمان عصبانیت ۵ ثانیه‌ای تمام شد؛ حالا زوت به مکان اولیه خود برمی‌گردد
            }
        } else if (aiState == 2) { // فاز بازگشت خودکار به محل اسپاون اصلی
            float speed = 50f;
            if (Math.abs(this.x - spawnX) > 10f) {
                if (this.x < spawnX) {
                    this.x += speed * deltaTime;
                } else {
                    this.x -= speed * deltaTime;
                }
            } else {
                this.x = spawnX;
                aiState = 0; // زوت با موفقیت به جایگاهش برگشت و دوباره آرام (Idle) شد
            }
        }

        // بخش تعیین دقیق جهت نگاه کردن زوت (بر اساس وضعیت فعلی حرکتش)
        if (aiState == 2) {
            // اگر در حال بازگشت به نقطه اسپاون است، رو به سمت مقصد اصلی (spawnX) نگاه کند
            this.facingRight = (this.x < spawnX);
        } else {
            // در حالت Idle یا تعقیب نایت، همیشه با اقتدار رو به سمت نایت نگاه می‌کند
            this.facingRight = (this.x < knightX);
        }

        // آپدیت نهایی و فیکس کردن موقعیت هیت‌باکس مستطیلی زوت روی مپ
        this.hitbox.setPosition(this.x, this.y);
    }

    @Override
    public void takeDamage(int amount, boolean hitFromRight) {
        // قانون زوت: زوت با دمیج کلاً نمی‌میرد! جان او همیشه روی ۱۰۰،۰۰۰ ریست می‌شود
        this.health = 100000;

        // فعال‌سازی حالت تعقیب و خشم ۵ ثانیه‌ای زوت علیه شوالیه
        this.aiState = 1;
        this.aiTimer = 5f;

        // فعال‌سازی افکت عقب‌نشینی کوتاه ناک‌بک فیزیکی کلاس والد
        this.knockbackTimer = 0.15f;
        this.knockbackVelX = hitFromRight ? 100f : -100f;
    }

    @Override public float getAnimationTime() { return 0f; }
    @Override public String getEnemyType() { return "zote"; }
}
