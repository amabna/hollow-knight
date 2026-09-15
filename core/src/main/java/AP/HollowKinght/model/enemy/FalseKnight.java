package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import AP.HollowKinght.controller.core.SaveManager;
import AP.HollowKinght.view.audio.AudioManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class FalseKnight extends Enemy {

    private float logTimer = 0f;

    // وضعیت‌های مختلف باس (ماشین وضعیت هوش مصنوعی)
    public enum State {
        INACTIVE, IDLE, MACE_SLAM, MACE_RECOVER, CHARGE_RUN, OFFENSIVE_LEAP, DEFENSIVE_LEAP, MEGA_SLAM, STUNNED, DEATH
    }

    public State currentState = State.INACTIVE;

    public float stateTimer = 0f;
    public float animTimer = 0f;
    private float decisionTimer = 0f; // تایمر برای اینکه باس هر چند ثانیه یک‌بار تصمیم بگیرد چه حرکتی کند

    private final float AGGRO_RANGE = 750f;   // فاصله‌ای که اگر شوالیه نزدیک‌تر بشه باس بیدار میشه
    private final float DEAGGRO_RANGE = 2000f; // فاصله‌ای که اگر شوالیه خیلی دور بشه باس غیرفعال میشه

    public boolean facingRight = false;
    public boolean isPhase2 = false;           // مشخص میکنه باس وارد فاز دو (خشمگین) شده یا نه
    private boolean hasBeenStunned = false;    // برای اینکه باس فقط یک‌بار در طول بازی گیج (Stun) بشه
    private float stunDuration = 5.0f;         // مدت زمان گیج ماندن باس روی زمین

    private State lastMove = State.IDLE;       // ذخیره حرکت قبلی برای جلوگیری از تکرار بیش از حد یک حرکت

    // متغیرهای مربوط به پرش‌های افقی و عمودی باس
    private float startLeapX, startLeapY;
    private float targetLeapX;
    private float leapDuration = 2.25f;

    // سیستم مدیریت پنجره زمانی دمیج برای پرش به عقب در صورت خوردن دمیج سریع
    private float damageWindowTimer = 0f;
    private int damageReceivedInWindow = 0;

    // ساختار ساده برای ذخیره دمیج‌های خورده شده به همراه زمان دقیق آن‌ها
    private static class DamageRecord {
        float timestamp;
        int amount;
        public DamageRecord(float timestamp, int amount) {
            this.timestamp = timestamp;
            this.amount = amount;
        }
    }
    private final Array<DamageRecord> damageHistory = new Array<>();
    private float totalElapsedTime = 0f;

    // متغیرهای مربوط به موج شوکی که بعد از ضربه پتک روی زمین راه می‌افتد
    public boolean isWaveActive = false;
    public float waveX, waveY;
    public float waveSpeed = 150f;
    public float waveMaxDistance = 650f;
    private float waveTravelled = 0f;
    public boolean waveDirectionRight = false;
    private boolean hasTriggeredWaveInCurrentSlam = false; // برای اینکه در هر ضربه فقط یک موج تولید بشه

    private float velocityY = 0f;
    private final float GRAVITY = -450f;
    private float groundY = 1840f; // مختصات ارتفاع کف اتاق باس

    public FalseKnight(float x, float y) {
        super(x, y, 480f, 440f, 5000);

        // تنظیم اندازه مستطیل برخورد (Hitbox) واقعی باس
        this.hitbox.width = 170f;
        this.hitbox.height = 190f;
        updateHitboxPositions();
    }

    @Override
    public void update(float deltaTime, Rectangle knightHitbox) {
        if (isDead) return;

        float oldX = this.x;
        float oldY = this.y;

        // آپدیت تایمر کل بازی برای بررسی هیستوری دمیج‌ها
        totalElapsedTime += deltaTime;

        // سرعت انیمیشن‌ها توی فاز دو سریع‌تر میشه
        if (currentState != State.DEATH) {
            float speedMultiplier = isPhase2 ? 1.45f : 0.9f;
            stateTimer += deltaTime * speedMultiplier;
            animTimer += deltaTime * speedMultiplier;
        }

        // اعمال جاذبه و فیزیک سقوط روی زمین (اگر روی هوا باشه و در حال پرش خاصی نباشه)
        if (currentState != State.OFFENSIVE_LEAP && currentState != State.DEFENSIVE_LEAP && currentState != State.MEGA_SLAM) {
            if (this.y > groundY) {
                velocityY += GRAVITY * deltaTime;
                this.y += velocityY * deltaTime;
            }
            if (this.y <= groundY) {
                this.y = groundY;
                velocityY = 0f;
            }
        }

        // مدیریت زمان پنجره دمیج
        if (damageWindowTimer > 0) {
            damageWindowTimer -= deltaTime;
            if (damageWindowTimer <= 0) damageReceivedInWindow = 0;
        }

        // حذف کردن اطلاعات دمیج‌هایی که بیشتر از ۴ ثانیه ازشون گذشته تا لیست شلوغ نشه
        for (int i = damageHistory.size - 1; i >= 0; i--) {
            if (totalElapsedTime - damageHistory.get(i).timestamp > 4.0f) {
                damageHistory.removeIndex(i);
            }
        }

        // حرکت و آپدیت منطق موج شوک روی زمین
        if (isWaveActive) {
            waveSpeed += 450f * deltaTime; // شتاب گرفتن موج به مرور زمان
            float waveDtSpeed = waveSpeed * deltaTime;
            if (waveDirectionRight) waveX += waveDtSpeed; else waveX -= waveDtSpeed;

            Rectangle waveHitbox = new Rectangle(waveX, waveY, 70f, 100f);

            // اگه موج به شوالیه خورد، بهش ۲ واحد دمیج بزن و موج رو حذف کن
            if (waveHitbox.overlaps(knightHitbox)) {
                boolean hitFromRight = waveX > (knightHitbox.x + knightHitbox.width / 2f);
                GameController.getInstance().getKnight().takeDamage(2, hitFromRight);
                isWaveActive = false;
            }

            // اگه موج به دیوار یا موانع مپ خورد، غیب بشه
            if (isWaveActive) {
                for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                    if (trigger.type == null) continue;
                    String t = trigger.type.toLowerCase();

                    if (t.equals("floor") || t.equals("plat")) {
                        Rectangle obstacleRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);

                        if (waveHitbox.overlaps(obstacleRect)) {
                            isWaveActive = false;
                            break;
                        }
                    }
                }
            }
        }

        // محاسبه فاصله مرکز به مرکز باس و شوالیه
        float bossCenterX = this.x + this.width / 2f;
        float bossCenterY = this.y + this.height / 2f;
        float knightCenterX = knightHitbox.x + knightHitbox.width / 2f;
        float knightCenterY = knightHitbox.y + knightHitbox.height / 2f;

        float diffX = knightCenterX - bossCenterX;
        float diffY = knightCenterY - bossCenterY;

        // محاسبه فاصله اقلیدسی (مستقیم) واقعی با در نظر گرفتن هر دو محور X و Y
        float actualDistance = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        float distanceX = Math.abs(diffX);

        // درخت تصمیم‌گیری ماشین وضعیت باس
        switch (currentState) {
            case INACTIVE:
                if (this.y != groundY) this.y = groundY;
                // اگه بازیکن وارد محدوده دید شد، باس بیدار میشه
                if (actualDistance <= AGGRO_RANGE) {
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case IDLE:
                // اگه بازیکن خیلی دور شد، باس دوباره غیرفعال میشه
                if (actualDistance > DEAGGRO_RANGE) {
                    currentState = State.INACTIVE;
                    resetTimers();
                    break;
                }
                // چرخیدن باس به سمتی که شوالیه قرار داره
                this.facingRight = diffX > 0;

                // هر ۲.۴ ثانیه یک‌بار هوش مصنوعی تصمیم میگیره حرکت بعدی چی باشه
                decisionTimer += deltaTime;
                if (decisionTimer >= 2.4f) {
                    decisionTimer = 0f;
                    makeAIDecision(distanceX, diffX);
                }
                break;

            case MACE_SLAM:
                // بعد از تموم شدن زمان انیمیشن ضربه، صفحه رو بلرزون و برو تو فاز ریکاوری ضربه
                if (stateTimer >= 2.1f) {
                    triggerCameraShake(0.35f, 10f);
                    currentState = State.MACE_RECOVER;
                    resetTimers();
                }
                break;

            case MACE_RECOVER:
                // استراحت کوتاه بعد از کوبیدن پتک
                if (stateTimer >= 0.7f) {
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case CHARGE_RUN:
                // دویدن مستقیم به سمت شوالیه با سرعت بالا (توی فاز دو سریع‌تر میدوه)
                float currentRunSpeed = isPhase2 ? 400 : 250;
                if (facingRight) this.x += currentRunSpeed * deltaTime;
                else this.x -= currentRunSpeed * deltaTime;

                updateHitboxPositions();
                handleSolidCollisionsX(oldX);

                // اگه موقع دویدن به شوالیه برخورد کرد، بهش دمیج بزنه
                if (this.hitbox.overlaps(knightHitbox)) {
                    GameController.getInstance().getKnight().takeDamage(1, !facingRight);
                }

                if (stateTimer >= 1.8f) {
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case OFFENSIVE_LEAP:
                // پرش هجومی به سمت جلو با فرمول ریاضی لِرپ خطی و سهمی
                float t1 = stateTimer / leapDuration;
                if (t1 > 1.0f) t1 = 1.0f;

                this.x = MathUtils.lerp(startLeapX, targetLeapX, t1);
                float h1 = 400; // اوج ارتفاع پرش هجومی
                this.y = startLeapY + (4 * h1 * t1 * (1 - t1));

                updateHitboxPositions();
                handleSolidCollisionsX(oldX);
                handleSolidCollisionsY(oldY);

                // وقتی فرود اومد زمین رو بلرزون و برگرد به حالت ایستا
                if (t1 >= 1.0f) {
                    this.y = groundY;
                    triggerCameraShake(0.4f, 12f);
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case DEFENSIVE_LEAP:
                // پرش دفاعی به سمت عقب برای فاصله گرفتن از پلیر
                float t2 = stateTimer / leapDuration;
                if (t2 > 1.0f) t2 = 1.0f;

                this.x = MathUtils.lerp(startLeapX, targetLeapX, t2);
                float h2 = 250f; // ارتفاع پرش دفاعی کمتره
                this.y = startLeapY + (4 * h2 * t2 * (1 - t2));

                updateHitboxPositions();
                handleSolidCollisionsX(oldX);
                handleSolidCollisionsY(oldY);

                if (t2 >= 1.0f) {
                    this.y = groundY;
                    triggerCameraShake(0.3f, 8f);
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case MEGA_SLAM:
                // ابرضربه فاز دوم که با پرش بلند همراهه و موج شوک درست میکنه
                float megaDuration = 2.4f;
                float t3 = stateTimer / megaDuration;
                if (t3 > 1.0f) t3 = 1.0f;

                this.x = MathUtils.lerp(startLeapX, targetLeapX, t3);
                float h3 = 290f;
                this.y = startLeapY + (4 * h3 * t3 * (1 - t3));

                updateHitboxPositions();
                handleSolidCollisionsX(oldX);
                handleSolidCollisionsY(oldY);

                // در انتهای پرش و موقع فرود آمدن، شلیک موج شوک فعال میشه
                if (t3 >= 0.9f && !hasTriggeredWaveInCurrentSlam) {
                    hasTriggeredWaveInCurrentSlam = true;
                    isWaveActive = true;
                    waveSpeed = 160f;
                    waveX = this.hitbox.x + (facingRight ? this.hitbox.width : -70f);
                    waveY = groundY;
                    waveTravelled = 0f;
                    waveDirectionRight = facingRight;

                    // بررسی برخورد مستقیم فیزیکی خود گرز مگا اسلم با شوالیه
                    Rectangle megaBox = new Rectangle(facingRight ? (hitbox.x + hitbox.width) : (hitbox.x - 50), y, 50, height);
                    if (megaBox.overlaps(knightHitbox)) {
                        GameController.getInstance().getKnight().takeDamage(1, !facingRight);
                    }
                    triggerCameraShake(0.55f, 18f); // لرزش شدید صفحه
                }

                if (t3 >= 1.0f) {
                    this.y = groundY;
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case STUNNED:
                // حالت گیج شدن که بعد از تموم شدنش، باس مستقیماً وارد فاز دو (عصبانی) میشه
                if (stateTimer >= stunDuration) {
                    isPhase2 = true;
                    currentState = State.IDLE;
                    resetTimers();
                }
                break;

            case DEATH:
                // کاهش سرعت زمان انیمیشن مرگ برای افکت سینمایی
                stateTimer += deltaTime * 0.5f;
                animTimer += deltaTime * 0.5f;
                break;
        }

        updateHitboxPositions();
        handleSolidCollisionsX(oldX);
        handleSolidCollisionsY(oldY);
    }

    // متد تصمیم‌گیری هوش مصنوعی براساس شانس و فاصله افقی تا شوالیه
    private void makeAIDecision(float distanceX, float diffX) {
        // محاسبه مجموع تمام دمیج‌های واقعی دریافتی در ۴ ثانیه اخیر
        int totalDamageInLast4Seconds = 0;
        for (DamageRecord record : damageHistory) {
            totalDamageInLast4Seconds += record.amount;
        }

        // وزن‌های پیش‌فرض برای شانس انتخاب هر حرکت
        int maceChance = 30;
        int chargeChance = 30;
        int leapChance = 25;
        int defensiveLeapChance = 15;
        int megaChance = 0;

        // تغییر وزن‌ها بر اساس نزدیک یا دور بودن شوالیه
        if (distanceX <= 300f) {
            maceChance = 65; chargeChance = 5; leapChance = 15; defensiveLeapChance = 15;
        } else if (distanceX > 450f) {
            maceChance = 10; chargeChance = 45; leapChance = 35; defensiveLeapChance = 10;
        }

        // اگه فاز دو باشه، شانس زدن حرکت مگا اسلم باز میشه
        if (isPhase2) {
            megaChance = 35; maceChance = 20; chargeChance = 20; leapChance = 10; defensiveLeapChance = 15;
        }

        // منطق هوشمند: اگه بازیکن تو ۴ ثانیه اخیر دمیج سنگینی (بالای ۲۹۰) زده، باس ۸۰٪ احتمال داره بپره عقب تا فرار کنه
        if (totalDamageInLast4Seconds > 290) {
            defensiveLeapChance = 80;
            if (isPhase2) {
                megaChance = 10; maceChance = 4; chargeChance = 3; leapChance = 3;
            } else {
                megaChance = 0; maceChance = 8; chargeChance = 6; leapChance = 6;
            }
        }

        int total = maceChance + chargeChance + leapChance + defensiveLeapChance + megaChance;
        int rand = MathUtils.random(1, total);

        State nextMove = State.IDLE;

        // انتخاب حرکت جدید بر اساس عدد تصادفی تولید شده
        if (rand <= maceChance) {
            nextMove = State.MACE_SLAM;
        } else if (rand <= maceChance + chargeChance) {
            nextMove = State.CHARGE_RUN;
        } else if (rand <= maceChance + chargeChance + leapChance) {
            nextMove = State.OFFENSIVE_LEAP;
        } else if (rand <= maceChance + chargeChance + leapChance + defensiveLeapChance) {
            nextMove = State.DEFENSIVE_LEAP;
        } else {
            nextMove = State.MEGA_SLAM;
        }

        // جلوگیری از اینکه باس دو بار پشت سر هم یک حرکت تکراری رو انجام بده
        if (nextMove == lastMove) {
            nextMove = (nextMove == State.MACE_SLAM) ? State.OFFENSIVE_LEAP : State.MACE_SLAM;
        }

        lastMove = nextMove;
        currentState = nextMove;
        resetTimers();

        // راه‌اندازی مختصات اولیه و هدف برای پرش‌ها پس از انتخاب حرکت
        if (currentState == State.OFFENSIVE_LEAP || currentState == State.MEGA_SLAM) {
            startLeapX = this.x;
            startLeapY = groundY;
            hasTriggeredWaveInCurrentSlam = false;

            float maxLeapDist = 240f;
            float v = isPhase2 ? 450 : 350;
            float clampedDiffX = MathUtils.clamp(diffX >= 0 ? v : -v, -maxLeapDist, maxLeapDist);
            targetLeapX = this.x + clampedDiffX;
        }
        else if (currentState == State.DEFENSIVE_LEAP) {
            startLeapX = this.x;
            startLeapY = groundY;

            boolean knightOnRight = diffX > 0;
            targetLeapX = knightOnRight ? (this.x - 200f) : (this.x + 200f);
        }
    }

    @Override
    public void takeDamage(int amount, boolean hitFromRight) {
        if (isDead || currentState == State.INACTIVE) return;

        AudioManager.getInstance().playSFX("enemy_damage.wav");

        // وقتی باس گیج (Stun) شده، ۱.۵ برابر بیشتر دمیج میخوره
        this.health -= currentState == State.STUNNED ? amount * 1.5 : amount;

        // ثبت دمیج در تاریخچه زمان‌دار
        damageHistory.add(new DamageRecord(totalElapsedTime, amount));

        // سیستم استان شدن: وقتی نصف خونش رفت برای اولین بار موقتاً بیهوش میشه روی زمین
        if (this.health <= this.maxHealth / 2 && !hasBeenStunned) {
            hasBeenStunned = true;
            currentState = State.STUNNED;
            resetTimers();
            return;
        }

        if (damageReceivedInWindow == 0) damageWindowTimer = 4f;
        damageReceivedInWindow += amount;

        // اگه توی پنجره زمانی مقدار دمیج متوالی به ۲۵۰ برسه، باس سریعاً یه پرش دفاعی به عقب میزنه تا امان نده
        if (damageReceivedInWindow >= 250 && this.y <= groundY && currentState != State.STUNNED && currentState != State.DEATH) {
            damageReceivedInWindow = 0;
            damageWindowTimer = 0f;

            currentState = State.DEFENSIVE_LEAP;
            resetTimers();
            startLeapX = this.x;
            startLeapY = this.y;
            targetLeapX = hitFromRight ? (this.x - 180f) : (this.x + 180f);
        }

        // منطق صفر شدن خون و فرستادن باس به وضعیت مرگ
        if (health <= 0) {
            if (currentState != State.DEATH) {
                GameController.getInstance().getKnightController().knight.ach4 = true; // فعال شدن اچیومنت شکست فالس نایت
                currentState = State.DEATH;
                resetTimers();
                this.y = groundY;
            }
            return;
        }
    }

    // متد کنترل برخورد فیزیکی باس با دیوارهای چپ و راست مپ
    private void handleSolidCollisionsX(float oldX) {
        float totalOffset = (this.width - this.hitbox.width) / 2f + 65;
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String t = trigger.type.toLowerCase();
            if (t.equals("floor") || t.equals("roof") || t.equals("plat")) {
                Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(rect)) {
                    if (this.hitbox.x + this.hitbox.width / 2f > rect.x + rect.width / 2f) {
                        this.hitbox.x = rect.x + rect.width;
                    } else {
                        this.hitbox.x = rect.x - this.hitbox.width;
                    }
                    this.x = this.hitbox.x - totalOffset;
                }
            }
        }
    }

    // متد کنترل برخورد فیزیکی عمودی باس با سقف و کف زمین مپ
    private void handleSolidCollisionsY(float oldY) {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String t = trigger.type.toLowerCase();
            if (t.equals("floor") || t.equals("plat")) {
                Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (this.hitbox.overlaps(rect)) {
                    if (this.y < oldY) {
                        this.y = rect.y + rect.height;
                        velocityY = 0f;
                    }
                    updateHitboxPositions();
                }
            }
        }
    }

    // متد کمکی برای لرزاندن دوربین بازی
    private void triggerCameraShake(float duration, float intensity) {
        AP.HollowKinght.model.player.Knight knight = GameController.getInstance().getKnight();
        if (knight != null) {
            knight.shakeTime = duration;
            knight.shakeIntensity = intensity;
        }
    }

    private void resetTimers() {
        stateTimer = 0f;
        animTimer = 0f;
    }

    // به‌روزرسانی ابعاد و موقعیت مستطیل برخورد بر اساس انیمیشن و وضعیت فعلی باس
    private void updateHitboxPositions() {
        if (currentState == State.DEATH) {
            this.hitbox.set(0, 0, 0, 0); // موقع مرگ هیت‌باکس صفر میشه تا دیگه نشه بهش ضربه زد یا ازش ضربه خورد
            return;
        }

        float baseHitboxX = this.x + (this.width - this.hitbox.width) / 2f;
        this.hitbox.x = baseHitboxX;
        this.hitbox.y = this.y;

        // مدیریت داینامیک هیت‌باکس موقع کوبیدن پتک برای تنظیم فریم دقیق آسیب‌رسانی گرز
        if (currentState == State.MACE_SLAM) {
            if (stateTimer <= 2.1 && stateTimer >= 1.7) {
                float attackWidth = 15f;
                this.hitbox.x = facingRight ? (baseHitboxX + 15f) : (baseHitboxX - attackWidth);
                this.hitbox.width = attackWidth;
                this.hitbox.height = 190f;
            } else {
                this.hitbox.set(0, 0, 0, 0); // خارج از فریم‌های اصلی ضربه، هیت‌باکس مخفی میشه
            }
        }
    }

    @Override public float getAnimationTime() { return animTimer; }
    @Override public String getEnemyType() { return "false_knight"; }
}
