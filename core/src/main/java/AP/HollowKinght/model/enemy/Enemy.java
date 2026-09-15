package AP.HollowKinght.model.enemy;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.audio.AudioManager;
import com.badlogic.gdx.math.Rectangle;

public abstract class Enemy {
    public float x, y;
    public float width, height;
    public Rectangle hitbox;

    public int health;
    public int maxHealth;
    public boolean isDead = false;

    // متغیرهای لازم برای شبیه‌سازی افکت پرت شدن انمی به عقب
    public float knockbackTimer = 0f;
    public float knockbackVelX = 0f;

    public Enemy(float x, float y, float width, float height, int health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHealth = health;
        this.health = health;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public abstract void update(float deltaTime, Rectangle knightHitbox);
    public abstract float getAnimationTime();
    public abstract String getEnemyType();

    // متد اعمال آسیب به انمی وقتی شوالیه بهش ضربه میزنه
    public void takeDamage(int amount, boolean hitFromRight) {
        if (isDead) return;

        AudioManager.getInstance().playSFX("enemy_damage.wav");
        this.health -= amount;

        // محاسبه قدرت عقب راندن انمی بر اساس داشتن یا نداشتن چارم Heavy Blow
        this.knockbackTimer = 0.15f;
        float knockbackSpeed = 450f;
        float v = (float) (GameController.getInstance().getKnightController().knight.hasHeavyBlow ? 2.0 : 1.0);

        // تعیین جهت پرت شدن انمی (اگه ضربه از راست خورده باشه باید به چپ پرتاب بشه)
        this.knockbackVelX = hitFromRight ? knockbackSpeed * v : -knockbackSpeed * v;

        // منطق مربوط به صفر شدن خون انمی و مردنش
        if (this.health <= 0) {
            this.health = 0;
            this.isDead = true;
            GameController.getInstance().getKnightController().knight.totalMobsKilled++;
            Knight knight = GameController.getInstance().getKnightController().knight;

            // ست کردن فلگ‌های آماری بر اساس نوع انمی که کشته شده
            if (this instanceof AP.HollowKinght.model.enemy.CrystalCrawler) knight.killedTiktik = true;
            if (this instanceof AP.HollowKinght.model.enemy.Husk) knight.killedHusk = true;
            if (this instanceof AP.HollowKinght.model.enemy.Mosquito) knight.killedMosq = true;
            if (this instanceof AP.HollowKinght.model.enemy.Crystalized) knight.killedCrys = true;

            float corpseKnockbackX = hitFromRight ? 400f * v : -400f * v;
            boolean corpseFacingRight = !hitFromRight;

            // ایجاد کردن آبجکت جسد فیزیکی در گیم کنترلر جهت نمایش افکت مرگ روی صفحه
            AP.HollowKinght.controller.core.GameController.getInstance().createCorpse(
                this.x, this.y, this.width, this.height,
                getAnimationTime(), corpseFacingRight, getEnemyType(), corpseKnockbackX
            );
        }
    }

    public void updateHitbox() {
        this.hitbox.set(x, y, width, height);
    }
}
