package AP.HollowKinght.view.renderer;

import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.model.enemy.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class EnemyRenderer implements Disposable {
    // تعریف انیمیشن‌های مربوط به دشمن نوع Crawler (خزنده‌ها)
    private Animation<TextureRegion> crawlerWalkAnimation;
    private Animation<TextureRegion> crawlerDeathAnimation;

    // تعریف انیمیشن‌های مربوط به دشمن نوع Mosquito (پشه‌ها)
    private Animation<TextureRegion> mosIdleAnimation;
    private Animation<TextureRegion> mosAnticipateAnimation;
    private Animation<TextureRegion> mosDeathAnimation;

    // تعریف انیمیشن‌ها و فریم‌های مربوط به دشمن نوع Husk
    private Animation<TextureRegion> huskIdleAnimation;
    private Animation<TextureRegion> huskWalkAnimation;
    private Animation<TextureRegion> huskAttackAnimation;
    private TextureRegion huskCorpseFrame;

    // تعریف انیمیشن‌های مربوط به غول آخر یا همان Crystal Guardian
    private Animation<TextureRegion> crystalIdleAnimation;
    private Animation<TextureRegion> crystalShootAnimation;
    private Animation<TextureRegion> crystalRunAnimation;
    private Animation<TextureRegion> crystalDeathAnimation;
    private Animation<TextureRegion> crystalLaserCircleAnimation;

    // متغیرهای کمکی برای چک کردن لود شدن تکسچرها و مدیریت حافظه
    private boolean hasCrystalFrames = false;
    private Array<Texture> loadedTextures;

    public EnemyRenderer() {
        // ایجاد یک لیست برای نگهداری تکسچرها تا بعداً بتوانیم راحت dispose کنیم
        loadedTextures = new Array<>();

        // --- ۱. بارگذاری انیمیشن‌های خزنده‌ها (Crawler) ---
        Array<TextureRegion> walkFrames = new Array<>();
        for (int i = 0; i <= 3; i++) {
            String path = "HKassets/crw/Walk_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                walkFrames.add(new TextureRegion(tex));
            }
        }
        crawlerWalkAnimation = new Animation<>(0.1f, walkFrames, Animation.PlayMode.LOOP);

        // انیمیشن فریم‌های مرگ کراولر
        Array<TextureRegion> crawlerDeathFrames = new Array<>();
        for (int i = 0; i <= 1; i++) {
            String path = "HKassets/crw/Death Land_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                crawlerDeathFrames.add(new TextureRegion(tex));
            }
        }
        crawlerDeathAnimation = new Animation<>(0.15f, crawlerDeathFrames, Animation.PlayMode.NORMAL);

        // --- ۲. بارگذاری انیمیشن‌های پشه‌ها (Mosquito) ---
        Array<TextureRegion> mosIdleFrames = new Array<>();
        for (int i = 0; i <= 7; i++) {
            String path = "HKassets/mos/idle_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                mosIdleFrames.add(new TextureRegion(tex));
            }
        }
        mosIdleAnimation = new Animation<>(0.1f, mosIdleFrames, Animation.PlayMode.LOOP);

        // انیمیشن آماده شدن پشه برای حمله
        Array<TextureRegion> mosAttackFrames = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String path = "HKassets/mos/Attack Anticipate_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                mosAttackFrames.add(new TextureRegion(tex));
            }
        }
        mosAnticipateAnimation = new Animation<>(0.1f, mosAttackFrames, Animation.PlayMode.LOOP);

        // انیمیشن فریم‌های مرگ پشه
        Array<TextureRegion> mosDeathFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            String path = "HKassets/mos/Death Land_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                mosDeathFrames.add(new TextureRegion(tex));
            }
        }
        mosDeathAnimation = new Animation<>(0.12f, mosDeathFrames, Animation.PlayMode.NORMAL);

        // --- ۳. بارگذاری انیمیشن‌های هاسک (Husk) ---
        Array<TextureRegion> hIdle = new Array<>();
        for (int i = 0; i <= 4; i++) {
            String path = "HKassets/husk/idle_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                hIdle.add(new TextureRegion(tex));
            }
        }
        huskIdleAnimation = new Animation<>(0.15f, hIdle, Animation.PlayMode.LOOP);

        // انیمیشن راه رفتن هاسک
        Array<TextureRegion> hWalk = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String path = "HKassets/husk/Walk_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                hWalk.add(new TextureRegion(tex));
            }
        }
        huskWalkAnimation = new Animation<>(0.12f, hWalk, Animation.PlayMode.LOOP);

        // انیمیشن حمله هاسک به جلو
        Array<TextureRegion> hAttack = new Array<>();
        for (int i = 0; i <= 10; i++) {
            String path = "HKassets/husk/Attack Lunge_0" + (i < 10 ? "0" + i : i) + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                hAttack.add(new TextureRegion(tex));
            }
        }
        huskAttackAnimation = new Animation<>(0.08f, hAttack, Animation.PlayMode.LOOP);

        // لود کردن تک فریم جسد هاسک
        String corpsePath = "HKassets/husk/Death Land_007.png";
        if (Gdx.files.internal(corpsePath).exists()) {
            Texture tex = new Texture(Gdx.files.internal(corpsePath));
            loadedTextures.add(tex);
            huskCorpseFrame = new TextureRegion(tex);
        }

        // --- ۴. بارگذاری انیمیشن‌های کریستال گاردین (Crystalized) ---
        Array<TextureRegion> cryIdle = new Array<>();
        for (int i = 0; i <= 4; i++) {
            String path = "HKassets/crys/idle_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                cryIdle.add(new TextureRegion(tex));
            }
        }
        crystalIdleAnimation = new Animation<>(0.15f, cryIdle, Animation.PlayMode.LOOP);

        if (cryIdle.size > 0) {
            hasCrystalFrames = true; // اگر فریم‌ها با موفقیت لود شدند فلگ را فعال می‌کنیم
        }

        // انیمیشن شلیک لیزر کریستال گاردین
        Array<TextureRegion> cryShoot = new Array<>();
        for (int i = 0; i <= 6; i++) {
            String path = "HKassets/crys/Shoot_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                cryShoot.add(new TextureRegion(tex));
            }
        }
        crystalShootAnimation = new Animation<>(0.1f, cryShoot, Animation.PlayMode.LOOP);

        // انیمیشن دویدن عصبانی غول
        Array<TextureRegion> cryRun = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String path = "HKassets/crys/Run_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                cryRun.add(new TextureRegion(tex));
            }
        }
        crystalRunAnimation = new Animation<>(0.1f, cryRun, Animation.PlayMode.LOOP);

        // انیمیشن مرگ کریستال گاردین در هوا
        Array<TextureRegion> cryDeath = new Array<>();
        for (int i = 0; i <= 5; i++) {
            String path = "HKassets/crys/Death Air_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                cryDeath.add(new TextureRegion(tex));
            }
        }
        crystalDeathAnimation = new Animation<>(0.15f, cryDeath, Animation.PlayMode.NORMAL);

        // انیمیشن دایره‌های هشدار لیزر روی زمین
        Array<TextureRegion> cryLaserCircle = new Array<>();
        for (int i = 0; i <= 3; i++) {
            String path = "HKassets/crys/LaserCircle_00" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                loadedTextures.add(tex);
                cryLaserCircle.add(new TextureRegion(tex));
            }
        }
        crystalLaserCircleAnimation = new Animation<>(0.08f, cryLaserCircle, Animation.PlayMode.LOOP);
    }

    // متد رسم انمی‌هایی که هنوز زنده هستند
    public void renderAliveEnemies(SpriteBatch batch) {
        for (Enemy enemy : GameController.getInstance().getEnemies()) {
            if (enemy.isDead) continue; // اگر مرده بود رد شو (چون در متد دیگری رسم می‌شود)

            // رسم خزنده‌ها
            if (enemy instanceof CrystalCrawler) {
                CrystalCrawler crawler = (CrystalCrawler) enemy;
                TextureRegion currentFrame = crawlerWalkAnimation.getKeyFrame(crawler.stateTime);
                boolean flipX = crawler.isMovingRight(); // چرخاندن عکس بر اساس جهت حرکت

                batch.draw(currentFrame.getTexture(), crawler.x, crawler.y, crawler.width, crawler.height,
                    currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), flipX, false);
            }
            // رسم پشه‌ها
            else if (enemy instanceof Mosquito) {
                Mosquito mos = (Mosquito) enemy;
                TextureRegion currentFrame;

                // اگر در حال قفل کردن یا حمله بود انیمیشن مخصوص، وگرنه انیمیشن سکون
                if (mos.currentState == Mosquito.State.LOCKING || mos.currentState == Mosquito.State.CHARGING) {
                    currentFrame = mosAnticipateAnimation.getKeyFrame(mos.getAnimationTime());
                } else {
                    currentFrame = mosIdleAnimation.getKeyFrame(mos.getAnimationTime());
                }

                boolean flipX = mos.facingRight;
                batch.draw(currentFrame.getTexture(), mos.x, mos.y, mos.width+50, mos.height+50,
                    currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), flipX, false);
            }
            // رسم هاسک‌ها
            else if (enemy instanceof Husk) {
                Husk husk = (Husk) enemy;
                TextureRegion currentFrame;

                // تعیین فریم مناسب بر اساس وضعیت فعلی هاسک (راه رفتن، حمله یا ایستاده)
                if (husk.currentState == Husk.State.WALKING) {
                    currentFrame = huskWalkAnimation.getKeyFrame(husk.getAnimationTime());
                } else if (husk.currentState == Husk.State.CHARGING) {
                    currentFrame = huskAttackAnimation.getKeyFrame(husk.getAnimationTime());
                } else {
                    currentFrame = huskIdleAnimation.getKeyFrame(husk.getAnimationTime());
                }

                boolean flipX = husk.facingRight;
                batch.draw(currentFrame.getTexture(), husk.x - 20f, husk.y - 5f, husk.width + 40f, husk.height + 10f,
                    currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), flipX, false);
            }
            // رسم غول کریستالی (Crystalized)
            else if (enemy instanceof Crystalized) {
                Crystalized crystal = (Crystalized) enemy;
                TextureRegion currentFrame = null;

                if (hasCrystalFrames) {
                    // تغییر فریم غول با توجه به ایستادن، شلیک لیزر یا دویدن سرعتی
                    if (crystal.getCurrentState() == Crystalized.State.IDLE || crystal.getCurrentState() == Crystalized.State.FALLING) {
                        currentFrame = crystalIdleAnimation.getKeyFrame(crystal.getAnimationTime(), true);
                    } else if (crystal.getCurrentState() == Crystalized.State.FIRING_LASER) {
                        currentFrame = crystalShootAnimation.getKeyFrame(crystal.getAnimationTime(), true);
                    } else if (crystal.getCurrentState() == Crystalized.State.ENRAGED_CHARGE) {
                        currentFrame = crystalRunAnimation.getKeyFrame(crystal.getAnimationTime(), true);
                    }
                }

                if (currentFrame != null) {
                    boolean flipX = crystal.isFacingRight();
                    batch.draw(currentFrame.getTexture(),
                        crystal.x - 70f, crystal.y - 20f, crystal.width + 140f, crystal.height + 40f,
                        currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                        flipX, false
                    );
                }

                // رسم دایره‌های افکت افقی لیزر غول در صورت فعال بودن
                if (crystal.isLaserActive && crystalLaserCircleAnimation != null) {
                    TextureRegion laserFrame = crystalLaserCircleAnimation.getKeyFrame(crystal.laserAnimTimer, true);
                    if (laserFrame != null) {
                        batch.draw(laserFrame.getTexture(), crystal.laserBounds.x, crystal.laserBounds.y, crystal.laserBounds.width+150, crystal.laserBounds.height+90,
                            laserFrame.getRegionX(), laserFrame.getRegionY(), laserFrame.getRegionWidth(), laserFrame.getRegionHeight(), false, false);
                    }
                }
            }
        }
    }

    // متد رسم جسد انمی‌ها بعد از شکست خوردن با افکت نیمه شفاف
    public void renderCorpses(SpriteBatch batch) {
        for (EnemyCorpse corpse : GameController.getInstance().getCorpses()) {
            // رسم جسد خزنده‌ها
            if (corpse.type.equals("tiktik")) {
                TextureRegion deadFrame;
                // اگر انیمیشن مرگ تمام شده بود روی فریم آخر قفل بماند، در غیر این صورت فریم جاری را بگیرد
                if (crawlerDeathAnimation != null && crawlerDeathAnimation.isAnimationFinished(corpse.stateTime)) {
                    deadFrame = crawlerDeathAnimation.getKeyFrame(crawlerDeathAnimation.getAnimationDuration());
                } else if (crawlerDeathAnimation != null) {
                    deadFrame = crawlerDeathAnimation.getKeyFrame(corpse.stateTime);
                } else {
                    deadFrame = crawlerWalkAnimation.getKeyFrame(corpse.stateTime);
                }

                batch.setColor(1f, 1f, 1f, 0.3f); // شفافیت ۳۰ درصد برای حالت جسد و محو شدن
                if (deadFrame != null) {
                    batch.draw(deadFrame.getTexture(), corpse.x, corpse.y, corpse.width, corpse.height,
                        deadFrame.getRegionX(), deadFrame.getRegionY(), deadFrame.getRegionWidth(), deadFrame.getRegionHeight(), corpse.facingRight, false);
                }
            }
            // رسم جسد پشه‌ها
            else if (corpse.type.equals("mosq")) {
                TextureRegion deadFrame;
                if (mosDeathAnimation != null && mosDeathAnimation.isAnimationFinished(corpse.stateTime)) {
                    deadFrame = mosDeathAnimation.getKeyFrame(mosDeathAnimation.getAnimationDuration());
                } else if (mosDeathAnimation != null) {
                    deadFrame = mosDeathAnimation.getKeyFrame(corpse.stateTime);
                } else {
                    deadFrame = mosIdleAnimation.getKeyFrame(corpse.stateTime);
                }

                batch.setColor(1f, 1f, 1f, 0.3f);
                if (deadFrame != null) {
                    batch.draw(deadFrame.getTexture(), corpse.x, corpse.y, corpse.width+50, corpse.height+50,
                        deadFrame.getRegionX(), deadFrame.getRegionY(), deadFrame.getRegionWidth(), deadFrame.getRegionHeight(), corpse.facingRight, false);
                }
            }
            // رسم تک فریم جسد هاسک
            else if (corpse.type.equals("husk")) {
                if (huskCorpseFrame != null) {
                    batch.setColor(1f, 1f, 1f, 0.3f);
                    batch.draw(huskCorpseFrame.getTexture(), corpse.x - 20f, corpse.y - 5f, corpse.width + 40f, corpse.height + 10f,
                        huskCorpseFrame.getRegionX(), huskCorpseFrame.getRegionY(), huskCorpseFrame.getRegionWidth(), huskCorpseFrame.getRegionHeight(), corpse.facingRight, false);
                }
            }
            // رسم جسد غول کریستالی
            else if (corpse.type.equals("crystalized")) {
                if (hasCrystalFrames && crystalDeathAnimation != null) {
                    TextureRegion deadFrame;
                    if (crystalDeathAnimation.isAnimationFinished(corpse.stateTime)) {
                        deadFrame = crystalDeathAnimation.getKeyFrame(crystalDeathAnimation.getAnimationDuration());
                    } else {
                        deadFrame = crystalDeathAnimation.getKeyFrame(corpse.stateTime);
                    }

                    boolean flipX = corpse.facingRight;
                    batch.setColor(1f, 1f, 1f, 0.4f); // شفافیت ۴۰ درصد برای غول
                    if (deadFrame != null) {
                        batch.draw(deadFrame.getTexture(), corpse.x, corpse.y, corpse.width+140, corpse.height+40,
                            deadFrame.getRegionX(), deadFrame.getRegionY(), deadFrame.getRegionWidth(), deadFrame.getRegionHeight(), flipX, false);
                    }
                }
            }
        }
        // برگرداندن رنگ اصلی و شفافیت کامل اسپریت‌بچ به حالت عادی پس از اتمام رندر جسدها
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // متد آزادسازی تکسچرهای لود شده از رم برای جلوگیری از نشت حافظه (Memory Leak)
    @Override
    public void dispose() {
        for (Texture tex : loadedTextures) tex.dispose();
    }
}
