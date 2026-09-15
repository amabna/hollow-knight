package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import com.badlogic.gdx.math.Rectangle;

public class EnemyCorpse {
    public float x, y, width, height;
    public float velocityY = 180f;
    public float velocityX;
    public float stateTime;
    public boolean facingRight;
    public String type;
    public boolean isGrounded = false;

    private float timeSinceDeath = 0f;
    private boolean shouldRemove = false;

    public EnemyCorpse(float x, float y, float width, float height, float stateTime, boolean facingRight, String type, float knockbackX) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.stateTime = stateTime;
        this.facingRight = facingRight;
        this.type = type;
        this.velocityX = knockbackX;

        // پشه‌ها چون پرواز میکنن موقع مرگ سرعت سقوط اولیه ندارن و درجا میفتن
        if (this.type.equals("mosq")) {
            this.velocityY = 0f;
        }
    }

    public void update(float deltaTime) {
        // تایمر برای حذف شدن جسد بعد از ۳ ثانیه جهت شلوغ نشدن مموری بازی
        timeSinceDeath += deltaTime;
        if (timeSinceDeath >= 3.0f) {
            shouldRemove = true;
            return;
        }

        if (!isGrounded) {
            // اعمال شتاب گرانش به جسد در صورتی که پشه نباشه
            if (!this.type.equals("mosq")) {
                velocityY += -1100f * deltaTime;
            }

            // چک کردن سنسور زیر پای جسد تا اگه زیرش پرتاب و پرتگاه بود سرعت افقیش صفر بشه
            float sensorX = (velocityX > 0) ? (x + width) : (x - 2f);
            Rectangle dropSensor = new Rectangle(sensorX, y - 800f, 2f, 800f);
            boolean groundBelow = false;

            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String t = trigger.type.toLowerCase();
                if (t.equals("floor") || t.equals("plat") || t.equals("tiz")) {
                    Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (dropSensor.overlaps(rect)) {
                        groundBelow = true;
                        break;
                    }
                }
            }

            if (!groundBelow) {
                velocityX = 0;
            }

            // حرکت دادن افقی جسد بر اساس جهت ضربه نهایی
            x += velocityX * deltaTime;

            // بررسی برخورد جسد با دیوارهای نقشه برای جلوگیری از رد شدن جسد از دیوارها
            Rectangle corpseHitbox = new Rectangle(x, y, width, height);
            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String t = trigger.type.toLowerCase();
                if (t.equals("floor") || t.equals("plat") || t.equals("tiz")) {
                    Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (corpseHitbox.overlaps(rect)) {
                        if (velocityX > 0) x = rect.x - width - 1;
                        else if (velocityX < 0) x = rect.x + rect.width + 1;
                        velocityX = 0;
                        break;
                    }
                }
            }

            // اعمال حرکت عمودی و کم کردن تدریجی سرعت افقی با ضریب اصطکاک
            y += velocityY * deltaTime;
            velocityX *= 0.92f;

            // چک کردن نهایی برخورد با زمین برای نشستن و فیکس شدن جسد روی زمین
            Rectangle hitbox = new Rectangle(x, y, width, height);
            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type == null) continue;
                String t = trigger.type.toLowerCase();

                if (t.equals("floor") || t.equals("plat")) {
                    Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (hitbox.overlaps(rect)) {
                        if (velocityY <= 0) {
                            y = rect.y + rect.height;
                            velocityY = 0;
                            velocityX = 0;
                            isGrounded = true;
                        }
                    }
                }
            }
        }
    }

    public boolean shouldRemove() {
        return shouldRemove;
    }
}
