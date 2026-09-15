package AP.HollowKinght.controller.player;

import AP.HollowKinght.controller.core.InputController;
import AP.HollowKinght.controller.core.GameController;
import AP.HollowKinght.controller.core.MapTrigger;
import AP.HollowKinght.controller.core.SpawnPoint;
import AP.HollowKinght.controller.core.DialogueManager;
import AP.HollowKinght.model.enemy.Crystalized;
import AP.HollowKinght.model.player.Knight;
import AP.HollowKinght.view.audio.AudioManager;
import com.badlogic.gdx.math.Rectangle;

public class KnightController {
    public Knight knight;
    private InputController input;

    private float deathDelayTimer = 0f;
    private boolean hasHitEnemyThisAttack = false;

    // لیست نگهداری انمی‌هایی که در طول دَش فعلی یک‌بار از Sharp Shadow دمیج خورده‌اند
    private com.badlogic.gdx.utils.Array<AP.HollowKinght.model.enemy.Enemy> dashedEnemies = new com.badlogic.gdx.utils.Array<>();

    public KnightController(Knight knight, InputController input) {
        this.knight = knight;
        this.input = input;
    }

    // متد به روزرسانی فریم به فریم وضعیت‌های شوالیه
    public void update(float deltaTime) {
        // ۱. اگر منوی اینونتوری باز باشد، شوالیه کاملاً متوقف می‌شود
        if (AP.HollowKinght.controller.core.InventoryManager.getInstance().isMenuOpen) {
            knight.velocityX = 0;
            knight.velocityY = 0;
            return;
        }

        // ۲. به روزرسانی تایمرهای خنک‌شدن (Cooldown) حملات و دَش
        if (knight.attackCooldownTimer > 0) {
            knight.attackCooldownTimer -= deltaTime;
        }
        if (knight.dashCooldownTimer > 0) {
            knight.dashCooldownTimer -= deltaTime;
        }

        // ۳. منطق مدیریت مرگ بازیکن و ریسپاون شدن پس از تأخیر مشخص
        if (knight.health <= 0) {
            if (deathDelayTimer == 0f) {
                deathDelayTimer = 0.6f;
            }
            deathDelayTimer -= deltaTime;
            if (deathDelayTimer <= 0) {
                knight.x = GameController.getInstance().getRespawnX();
                knight.y = GameController.getInstance().getRespawnY();
                knight.health = 5;
                deathDelayTimer = 0f;
                knight.changeState(Knight.State.IDLE);
                knight.updateHitbox();
            }
            return;
        }

        // ۴. منطق توقف فیزیک شوالیه در هنگام فعال بودن دیالوگ‌ها
        if (DialogueManager.getInstance().isDialogueActive()) {
            knight.velocityX = 0;
            knight.velocityY = 0;
            if (knight.isGrounded) {
                knight.changeState(Knight.State.IDLE);
            }
            knight.updateHitbox();
            return;
        }

        // ۵. منطق پرواز و عبور از دیوار شوالیه (Noclip Mode)
        if (knight.isNoclip) {
            float flySpeed = knight.MOVE_SPEED * 1.5f;
            knight.velocityX = 0;
            knight.velocityY = 0;

            if (input.isLeftPressed()) {
                knight.velocityX = -flySpeed;
                knight.facingRight = false;
            } else if (input.isRightPressed()) {
                knight.velocityX = flySpeed;
                knight.facingRight = true;
            }

            if (input.isUpPressed()) {
                knight.velocityY = flySpeed;
            } else if (input.isDownPressed()) {
                knight.velocityY = -flySpeed;
            }

            knight.x += knight.velocityX * deltaTime;
            knight.y += knight.velocityY * deltaTime;
            knight.updateHitbox();
            return;
        }

        // ۶. سیستم محاسبه دمیج خوردن شوالیه از باس‌فایت (False Knight)
        // جلوگیری از آسیب دیدن در صورت فعال بودن گادمود، رویین‌تنی موقت یا داشتن Sharp Shadow در حین دَش
        boolean isInvincibleToBoss = knight.invulnerableTimer > 0 || knight.isGodMode || (knight.currentState == Knight.State.DASHING && knight.hasSharpShadow);
        if (!isInvincibleToBoss) {
            for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                if (enemy instanceof AP.HollowKinght.model.enemy.FalseKnight) {
                    AP.HollowKinght.model.enemy.FalseKnight boss = (AP.HollowKinght.model.enemy.FalseKnight) enemy;

                    if (boss.isDead || boss.currentState == AP.HollowKinght.model.enemy.FalseKnight.State.INACTIVE) {
                        continue;
                    }

                    boolean isKnightHit = false;

                    // بررسی همپوشانی هیت‌باکس شوالیه با بدن اسکلتی باس
                    if (knight.hitbox.overlaps(boss.hitbox)) {
                        isKnightHit = true;
                    }

                    // بررسی برخورد پتک (Mace) در زمان حملات کوبشی باس
                    if (!isKnightHit && (boss.currentState == AP.HollowKinght.model.enemy.FalseKnight.State.MACE_SLAM ||
                        boss.currentState == AP.HollowKinght.model.enemy.FalseKnight.State.MEGA_SLAM)) {

                        float maceReachWidth = 160f;
                        float maceReachHeight = boss.hitbox.height;
                        float maceX = boss.facingRight ? (boss.hitbox.x + boss.hitbox.width) : (boss.hitbox.x - maceReachWidth);
                        float maceY = boss.hitbox.y;

                        com.badlogic.gdx.math.Rectangle maceHitbox = new com.badlogic.gdx.math.Rectangle(
                            maceX, maceY, maceReachWidth, maceReachHeight
                        );

                        if (knight.hitbox.overlaps(maceHitbox)) {
                            isKnightHit = true;
                        }
                    }

                    if (isKnightHit) {
                        boolean damageFromRight = (boss.hitbox.x + boss.hitbox.width / 2f) > (knight.x + knight.width / 2f);
                        knight.takeDamage(1, damageFromRight);
                        break;
                    }
                }
            }
        }

        // ۷. تنظیم سرعت انیمیشن بر اساس نگه‌داشتن کلید پرش
        if (knight.currentState == Knight.State.JUMP) {
            if (input.isJumpPressed()) {
                knight.stateTime += deltaTime * 0.75f;
            } else {
                knight.stateTime += deltaTime * 0.2f;
            }
        } else if (knight.currentState != Knight.State.FOCUSING && knight.currentState != Knight.State.WALL_SLIDE) {
            knight.stateTime += deltaTime;
        }

        // ۸. اعمال منطق عقب رانده شدن ناشی از ضربه خوردن شوالیه (Knockback)
        if (knight.knockbackTimer > 0) {
            knight.knockbackTimer -= deltaTime;
            knight.x += knight.knockbackVelX * deltaTime;
            knight.updateHitbox();
            handleSolidCollisionsX();
            return;
        }

        // ۹. به روزرسانی تایمرهای انیمیشن فرود آمدن و دویدن به سکون
        if (knight.landingTimer > 0) {
            knight.landingTimer -= deltaTime;
            if (knight.landingTimer <= 0) knight.changeState(Knight.State.IDLE);
        }
        if (knight.runToIdleTimer > 0) {
            knight.runToIdleTimer -= deltaTime;
            if (knight.runToIdleTimer <= 0) knight.changeState(Knight.State.IDLE);
        }

        // ۱۰. سیستم دَش زدن و آسیب‌رسانی ویژگی پیشرفته شارپ شدو (Sharp Shadow)
        if (knight.currentState == Knight.State.DASHING) {
            updateDash(deltaTime);

            // اعمال دمیج ۵۰ واحدی شارپ شدو (تنها یک‌بار به ازای هر انمی در طول دَش فعلی)
            if (knight.hasSharpShadow) {
                for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                    if (!enemy.isDead && knight.hitbox.overlaps(enemy.hitbox)) {

                        if (!dashedEnemies.contains(enemy, true)) {
                            dashedEnemies.add(enemy);

                            // ذخیره موقت وضعیت ناک‌بک فعلی انمی جهت جلوگیری از عقب پرت شدن غیرطبیعی
                            float savedKnockbackTimer = enemy.knockbackTimer;
                            float savedKnockbackVelX = enemy.knockbackVelX;

                            boolean hitFromRight = (knight.x + knight.width / 2f) < (enemy.x + enemy.width / 2f);
                            enemy.takeDamage(50, hitFromRight);

                            // ثبت آمار مرگ انمی‌ها در پایان بازی
                            if (enemy.health <= 0) {
                                if (enemy instanceof AP.HollowKinght.model.enemy.CrystalCrawler) knight.killedTiktik = true;
                                if (enemy instanceof AP.HollowKinght.model.enemy.Husk) knight.killedHusk = true;
                                if (enemy instanceof AP.HollowKinght.model.enemy.Mosquito) knight.killedMosq = true;
                                if (enemy instanceof Crystalized) knight.killedCrys = true;
                            }

                            // برگرداندن آنی ناک‌بک انمی به حالت قبل
                            enemy.knockbackTimer = savedKnockbackTimer;
                            enemy.knockbackVelX = savedKnockbackVelX;
                        }
                    }
                }
            }
            return;
        } else {
            // پاکسازی لیست انمی‌های دَش خورده پس از پایان حالت DASHING
            if (dashedEnemies.size > 0) {
                dashedEnemies.clear();
            }
        }

        // ۱۱. وضعیت اجرای اسپل فایربال (Vengeful Spirit)
        if (knight.currentState == Knight.State.CAST_FIREBALL) {
            knight.velocityX = 0;
            knight.velocityY = 0;
            if (knight.stateTime >= 0.54f) {
                knight.changeState(Knight.State.IDLE);
            }
            knight.updateHitbox();
            return;
        }

        // ۱۲. وضعیت اجرای اسپل فریاد (Howling Wraiths)
        if (knight.currentState == Knight.State.CAST_SCREAM) {
            knight.velocityX = 0;
            knight.velocityY = 0;
            if (knight.stateTime >= 0.42f) {
                knight.changeState(Knight.State.IDLE);
            }
            knight.updateHitbox();
            return;
        }

        // ۱۳. سیستم جامع شمشیر زدن (حملات معمولی، رو به بالا و ضربه پوگو)
        if (knight.currentState == Knight.State.ATTACKING) {
            knight.attackTimer -= deltaTime;
            updateAttackHitboxPosition();

            if (!hasHitEnemyThisAttack) {
                // الف) بررسی آسیب‌رسانی به دیوار مخفی اتاق (Hidden Door)
                AP.HollowKinght.controller.core.HiddenRoomManager roomMgr = AP.HollowKinght.controller.core.HiddenRoomManager.getInstance();
                if (!roomMgr.isDestroyed) {
                    MapTrigger door = null;
                    for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                        if (trigger.t != null && trigger.t.equalsIgnoreCase("hidden_door")) {
                            door = trigger;
                            break;
                        }
                    }

                    if (door != null) {
                        Rectangle doorBounds = new Rectangle(door.x, door.y, door.width, door.height);
                        if (knight.attackHitbox.overlaps(doorBounds)) {
                            roomMgr.hitWall();

                            // جهش به بالا (Pogo) در صورت ضربه زدن به بخش مخفی در هوا
                            if (knight.isPogoAttack && !knight.isGrounded) {
                                knight.isGrounded = false;
                                knight.velocityY = knight.JUMP_VELOCITY * 0.9f;
                                knight.canDoubleJump = true;
                                knight.canDash = true;
                            }
                            hasHitEnemyThisAttack = true;
                        }
                    }
                }

                // ب) بررسی ضربه پوگو روی تیغ‌های تیز محیطی (Tiz)
                boolean hitSharpPoint = false;
                if (knight.isPogoAttack && !hasHitEnemyThisAttack) {
                    for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                        if (trigger.type != null && trigger.type.equalsIgnoreCase("Tiz")) {
                            Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                            if (knight.attackHitbox.overlaps(rect)) {
                                hitSharpPoint = true;
                                break;
                            }
                        }
                    }
                }

                if (hitSharpPoint) {
                    hasHitEnemyThisAttack = true;
                    knight.isGrounded = false;
                    knight.velocityY = knight.JUMP_VELOCITY * 0.9f;
                    knight.canDoubleJump = true;
                    knight.canDash = true;
                }

                // ج) بررسی آسیب‌رسانی به بدنه انمی‌ها و پر کردن سول (Soul)
                if (!hasHitEnemyThisAttack) {
                    for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                        if (!enemy.isDead && knight.attackHitbox.overlaps(enemy.hitbox)) {
                            boolean hitFromRight = (knight.x + knight.width / 2f) < (enemy.x + enemy.width / 2f);

                            // اعمال قدرت چارم Unbreakable Strength (دمیج ۱۵۰ به جای ۱۰۰)
                            enemy.takeDamage(knight.hasUnbreakableStrength ? 150 : 100, hitFromRight);

                            if (enemy.health <= 0) {
                                if (enemy instanceof AP.HollowKinght.model.enemy.CrystalCrawler) knight.killedTiktik = true;
                                if (enemy instanceof AP.HollowKinght.model.enemy.Husk) knight.killedHusk = true;
                                if (enemy instanceof AP.HollowKinght.model.enemy.Mosquito) knight.killedMosq = true;
                                if (enemy instanceof Crystalized) knight.killedCrys = true;
                            }

                            // پر کردن سول (اعمال چارم Soul Catcher: اضافه شدن ۱۷ واحد به جای ۱۱ واحد)
                            if (!(enemy instanceof AP.HollowKinght.model.enemy.Zote)) {
                                knight.soul += knight.hasSoulCatcher ? 17 : 11;
                                if (knight.soul > 99) knight.soul = 99;
                            }

                            // ایجاد مکانیزم جهش عمودی پوگو (Pogo Attack Jump)
                            if (knight.isPogoAttack && !knight.isGrounded) {
                                knight.isGrounded = false;
                                knight.velocityY = knight.JUMP_VELOCITY * 0.9f;
                                knight.canDoubleJump = true;
                                knight.canDash = true;
                            }

                            hasHitEnemyThisAttack = true;
                            break;
                        }
                    }
                }
            }

            if (knight.attackTimer <= 0) {
                knight.isPogoAttack = false;
                knight.isUpAttack = false;
                knight.changeState(knight.isGrounded ? Knight.State.IDLE : Knight.State.FALL);
            }
        }

        // ۱۴. سیستم پر کردن خون شوالیه با تمرکز ارواح (Focusing Logic)
        if (knight.currentState == Knight.State.FOCUSING) {
            if (!input.isFocusPressed() || knight.soul < 20) {
                knight.changeState(Knight.State.IDLE);
                knight.focusTimer = 0f;
                AudioManager.getInstance().stopSFX("focus_health_charging.wav");
                return;
            }

            knight.focusTimer += deltaTime;

            // تسریع سرعت فوکوس با چارم Quick Focus (مدت زمان 0.5 ثانیه به جای 1.5 ثانیه)
            if (knight.focusTimer >= (knight.hasQuickFocus ? 0.5f : 1.5f)) {
                knight.health += 1;
                knight.soul -= 20;
                if (knight.health > 5) knight.health = 5;
                if (knight.soul < 0) knight.soul = 0;

                AudioManager.getInstance().stopSFX("focus_health_charging.wav");
                AudioManager.getInstance().playSFX("focus_health_heal.wav");
                knight.changeState(Knight.State.IDLE);
                knight.focusTimer = 0f;
            }
        }

        // فراخوانی متدهای پردازش فیزیک، اینپوت و استیکتارهای محیطی دیوار
        checkWallClingLogic();
        handleInput(deltaTime);
        applyPhysics(deltaTime);
        updateState();
    }

    // هندل کردن دقیق ورودی‌های کلیدها، اسپل‌ها، پرش‌ها و تعامل با پلتفرم‌ها
    private void handleInput(float dt) {
        if (knight.currentState == Knight.State.FOCUSING) {
            knight.velocityX = 0;
            if (!input.isFocusPressed() || knight.soul < 20) {
                knight.changeState(Knight.State.IDLE);
                knight.focusTimer = 0f;
                AudioManager.getInstance().stopSFX("focus_health_charging.wav");
            }
            return;
        }

        if (knight.currentState != Knight.State.ATTACKING) {
            if (knight.currentState != Knight.State.WALL_SLIDE && knight.currentState != Knight.State.WALL_JUMPING) {
                knight.velocityX = 0;
                if (input.isLeftPressed()) {
                    knight.velocityX = -knight.MOVE_SPEED;
                    knight.facingRight = false;
                } else if (input.isRightPressed()) {
                    knight.velocityX = knight.MOVE_SPEED;
                    knight.facingRight = true;
                }
            }
            else if (knight.currentState == Knight.State.WALL_JUMPING) {
                if (knight.stateTime > 1.3f) {
                    if (input.isLeftPressed()) {
                        knight.velocityX = -knight.MOVE_SPEED;
                        knight.facingRight = false;
                    } else if (input.isRightPressed()) {
                        knight.velocityX = knight.MOVE_SPEED;
                        knight.facingRight = true;
                    }
                }
            }
        }

        if (input.isFocusPressed() && knight.isGrounded && knight.currentState != Knight.State.ATTACKING && knight.currentState != Knight.State.DASHING) {
            if (knight.soul >= 20 && knight.health < 5) {
                knight.changeState(Knight.State.FOCUSING);
                knight.focusTimer = 0f;
                AudioManager.getInstance().playSFX("focus_health_charging.wav");
            }
        }

        // مدیریت کلیک حمله و تنظیم وقفه خنک شدن (اعمال چارم Quick Slash جهت کاهش وقفه ضربات)
        if (input.isAttackJustPressed() && knight.currentState != Knight.State.ATTACKING && knight.currentState != Knight.State.DASHING && knight.attackCooldownTimer <= 0) {
            knight.changeState(Knight.State.ATTACKING);
            knight.attackTimer = knight.ATTACK_DURATION;
            knight.attackCooldownTimer = knight.hasQuickSlash ? (float) (0.4 * knight.attackPause) : (float) knight.attackPause;
            hasHitEnemyThisAttack = false;

            if (!knight.isGrounded && input.isDownPressed()) {
                knight.isPogoAttack = true;
                knight.triggerAttackSlash("DOWN");
                updateAttackHitboxPosition();
            } else if (input.isUpPressed()) {
                knight.isPogoAttack = false;
                knight.isUpAttack = true;
                knight.triggerAttackSlash("UP");
                updateAttackHitboxPosition();
            } else {
                knight.isPogoAttack = false;
                knight.isUpAttack = false;
                knight.triggerAttackSlash(knight.facingRight ? "RIGHT" : "LEFT");
                updateAttackHitboxPosition();
            }
        }

        // آغاز ده‌ش و اعمال چارم Dashmaster جهت پرشدن فوق‌العاده سریع‌تر خط زمانی دَش
        if (input.isDashJustPressed() && knight.canDash && knight.dashCooldownTimer <= 0) {
            knight.changeState(Knight.State.DASHING);
            knight.dashTimer = knight.DASH_DURATION;
            knight.dashCooldownTimer = knight.hasDashmaster ? (float) (0.4 * knight.dashPause) : (float) knight.dashPause;
            knight.canDash = false;
            knight.velocityY = 0;
            return;
        }

        // هندل کردن پرش معمولی، پرش دیواری (Wall Jump) و پرش دوتایی (Double Jump)
        if (input.isJumpJustPressed()) {
            if (knight.currentState == Knight.State.WALL_SLIDE) {
                knight.velocityY = knight.JUMP_VELOCITY * 0.8f;
                if (knight.facingRight) {
                    knight.velocityX = -knight.MOVE_SPEED;
                    knight.facingRight = false;
                    knight.x -= 5f;
                } else {
                    knight.velocityX = knight.MOVE_SPEED;
                    knight.facingRight = true;
                    knight.x += 5f;
                }
                knight.updateHitbox();
                knight.isGrounded = false;
                knight.canDoubleJump = true;
                knight.canDash = true;
                knight.changeState(Knight.State.WALL_JUMPING);
                knight.stateTime = 0f;
            } else if (knight.isGrounded) {
                knight.velocityY = knight.JUMP_VELOCITY;
                knight.isGrounded = false;
                knight.changeState(Knight.State.JUMP);
            } else if (knight.canDoubleJump) {
                knight.velocityY = knight.JUMP_VELOCITY * 0.95f;
                knight.canDoubleJump = false;
                knight.changeState(Knight.State.DOUBLE_JUMP);
            }
        }

        if (!input.isJumpPressed() && knight.velocityY > 50f &&
            (knight.currentState == Knight.State.JUMP || knight.currentState == Knight.State.DOUBLE_JUMP || knight.currentState == Knight.State.AIRBORNE)) {
            knight.velocityY -= 5000f * dt;
        }

        // فایر کردن جادوی روح افقی (Vengeful Spirit) در صورت وجود ۳۳ واحد سول
        if (input.isSpell1JustPressed() && knight.soul >= 33 && knight.currentState != Knight.State.DASHING && knight.currentState != Knight.State.FOCUSING && knight.currentState != Knight.State.CAST_FIREBALL && knight.currentState != Knight.State.CAST_SCREAM && knight.currentState != Knight.State.ATTACKING) {
            knight.soul -= 33;
            if (knight.soul < 0) knight.soul = 0;
            knight.changeState(Knight.State.CAST_FIREBALL);
            GameController.getInstance().fireballs.add(new GameController.FireballSpell(knight.x, knight.y, knight.facingRight));
            AudioManager.getInstance().playSFX("fireball.wav");
            knight.shakeTime = 0.3f;
            knight.shakeIntensity = 9.0f;
            return;
        }

        // فایر کردن جادوی روح عمودی (Howling Wraiths)
        if (input.isSpell2JustPressed() && knight.soul >= 33 && knight.currentState != Knight.State.DASHING && knight.currentState != Knight.State.FOCUSING && knight.currentState != Knight.State.CAST_FIREBALL && knight.currentState != Knight.State.CAST_SCREAM && knight.currentState != Knight.State.ATTACKING) {
            knight.soul -= 33;
            if (knight.soul < 0) knight.soul = 0;
            knight.changeState(Knight.State.CAST_SCREAM);
            GameController.getInstance().screams.add(new GameController.ScreamSpell(knight));
            AudioManager.getInstance().playSFX("scream.wav");
            knight.shakeTime = 0.3f;
            knight.shakeIntensity = 9.0f;
            return;
        }
    }

    // منطق چسبیدن به لبه دیوارها و سرخوردن آرام روی سطوح عمودی (Wall Slide)
    private void checkWallClingLogic() {
        if (knight.isGrounded || knight.currentState == Knight.State.DASHING || knight.currentState == Knight.State.ATTACKING || knight.currentState == Knight.State.WALL_JUMPING) {
            if (knight.currentState == Knight.State.WALL_SLIDE) {
                knight.changeState(Knight.State.FALL);
            }
            return;
        }

        boolean adjacentToWall = false;
        Rectangle leftCheck = new Rectangle(knight.hitbox.x - 3, knight.hitbox.y + 10, 3, knight.hitbox.height - 20);
        Rectangle rightCheck = new Rectangle(knight.hitbox.x + knight.hitbox.width, knight.hitbox.y + 10, 3, knight.hitbox.height - 20);

        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String t = trigger.type.toLowerCase();

            if (t.equals("floor") || t.equals("roof") || t.equals("tiz")) {
                Rectangle tileRect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);

                if (leftCheck.overlaps(tileRect) && input.isLeftPressed()) {
                    adjacentToWall = true;
                    knight.facingRight = false;
                    break;
                } else if (rightCheck.overlaps(tileRect) && input.isRightPressed()) {
                    adjacentToWall = true;
                    knight.facingRight = true;
                    break;
                }
            }
        }

        if (adjacentToWall && knight.velocityY <= 0) {
            if (knight.currentState != Knight.State.WALL_SLIDE) {
                knight.changeState(Knight.State.WALL_SLIDE);
                knight.canDash = true;
                knight.canDoubleJump = true;
            }
        } else {
            if (knight.currentState == Knight.State.WALL_SLIDE) {
                knight.changeState(Knight.State.FALL);
            }
        }
    }

    // تغییر و به روزرسانی ابعاد جعبه ضربه ناخن (Nail Attack Hitbox) بر اساس جهات شمشیر زدن
    private void updateAttackHitboxPosition() {
        if (knight.isPogoAttack) {
            float pogoWidth = 60f;
            float pogoHeight = 70f;
            float pogoX = (knight.x + knight.hitboxOffsetX) + (knight.width / 2f) - (pogoWidth / 2f);
            float pogoY = knight.y - pogoHeight - 5f;
            knight.attackHitbox.set(pogoX, pogoY, pogoWidth, pogoHeight);

        } else if (knight.isUpAttack) {
            float upAttackWidth = 60f;
            float upAttackHeight = 60f;
            float upAttackX = (knight.x + knight.hitboxOffsetX) + (knight.width / 2f) - (upAttackWidth / 2f);
            float upAttackY = knight.y + knight.height + 5f;
            knight.attackHitbox.set(upAttackX, upAttackY, upAttackWidth, upAttackHeight);

        } else {
            float attackWidth = 113f;
            float attackHeight = 60f;
            float attackY = knight.y + (knight.height / 2f) - (attackHeight / 2f);
            float attackX;

            if (knight.facingRight) {
                attackX = (knight.x + knight.hitboxOffsetX + knight.width);
            } else {
                attackX = (knight.x + knight.hitboxOffsetX) - attackWidth;
            }
            knight.attackHitbox.set(attackX, attackY, attackWidth, attackHeight);
        }
    }

    // به روزرسانی و مدیریت محاسبات سرعت دَش (افزایش سرعت دَش به میزان ۱.۲ برابر با فعال بودن Sharp Shadow)
    private void updateDash(float dt) {
        if (knight.dashTimer == knight.DASH_DURATION) AudioManager.getInstance().playSFX("hornet_dash.wav");
        knight.dashTimer -= dt;
        float d = knight.hasSharpShadow ? (knight.DASH_SPEED * 1.2f) : knight.DASH_SPEED;
        knight.velocityX = knight.facingRight ? d : -d;
        knight.velocityY = 0;

        knight.x += knight.velocityX * dt;
        knight.updateHitbox();
        handleSolidCollisionsX();

        if (knight.dashTimer <= 0) knight.changeState(knight.isGrounded ? Knight.State.IDLE : Knight.State.FALL);
    }

    // اعمال شتاب گرانش زمین، بررسی همپوشانی دمیج با انمی‌های مپ، تیغ‌ها و چک کردن نقاط تغییر ریسپاون پوینت
    private void applyPhysics(float dt) {
        if (knight.currentState == Knight.State.WALL_SLIDE) {
            knight.velocityY = -80f;
            knight.velocityX = knight.facingRight ? 40f : -40f;
        } else if (knight.currentState != Knight.State.DASHING) {
            knight.velocityY += knight.GRAVITY * dt;
        }

        knight.x += knight.velocityX * dt;
        knight.updateHitbox();
        handleSolidCollisionsX();

        knight.y += knight.velocityY * dt;
        knight.updateHitbox();
        handleSolidCollisionsY();

        // بررسی صدمه دیدن از دشمنان معمولی نقشه (ایمن بودن بازیکن هنگام استفاده از دَش سایه)
        boolean isInvincibleToNormal = knight.invulnerableTimer <= 0 && !knight.isGodMode && !(knight.currentState == Knight.State.DASHING && knight.hasSharpShadow);

        if (isInvincibleToNormal) {
            for (AP.HollowKinght.model.enemy.Enemy enemy : GameController.getInstance().getEnemies()) {
                if (enemy instanceof AP.HollowKinght.model.enemy.Zote) continue;
                if (!enemy.isDead && knight.hitbox.overlaps(enemy.hitbox)) {
                    takeDamage(enemy.x + enemy.width / 2f);
                    break;
                }
            }
        }

        // بررسی همپوشانی فیزیکی شوالیه با موانع تیغ مانند مرگبار (Tiz)
        if ((knight.currentState != Knight.State.ATTACKING || !knight.isPogoAttack) && !knight.isGodMode) {
            for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
                if (trigger.type != null && trigger.type.equalsIgnoreCase("Tiz")) {
                    Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                    if (knight.hitbox.overlaps(rect)) {
                        takeDamage(rect.x + rect.width / 2f);
                        break;
                    }
                }
            }
        }

        // به روزرسانی اتوماتیک آخرین پوینت احیا (Respawn Station) بر اساس تریگر لایه‌های مپ
        for (SpawnPoint sp : GameController.getInstance().getSpawnPoints()) {
            if (sp.spawnType != null && sp.spawnType.toLowerCase().startsWith("player")) {
                Rectangle checkRect = new Rectangle(sp.x, sp.y, Math.max(sp.width, 32f), Math.max(sp.height, 64f));
                if (knight.hitbox.overlaps(checkRect)) {
                    GameController.getInstance().setRespawnPosition(sp.x, sp.y);
                }
            }
        }
    }

    // هندل و برطرف کردن تداخل فیزیکی در محور افقی X با دیوارها و تایل‌های جامد مپ
    private void handleSolidCollisionsX() {
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String t = trigger.type.toLowerCase();
            if (t.equals("floor") || t.equals("roof") || t.equals("plat")) {
                Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (knight.hitbox.overlaps(rect)) {
                    if (knight.velocityX > 0) {
                        knight.x = rect.x - knight.hitboxOffsetX - knight.width;
                    } else if (knight.velocityX < 0) {
                        knight.x = rect.x + rect.width - knight.hitboxOffsetX;
                    }
                    knight.updateHitbox();
                }
            }
        }
    }

    // هندل و برطرف کردن تداخل فیزیکی در محور عمودی Y و تشخیص دقیق قرارگیری روی زمین (Grounded)
    private void handleSolidCollisionsY() {
        boolean grounded = false;
        for (MapTrigger trigger : GameController.getInstance().getMapTriggers()) {
            if (trigger.type == null) continue;
            String t = trigger.type.toLowerCase();
            if (t.equals("floor") || t.equals("roof") || t.equals("plat")) {
                Rectangle rect = new Rectangle(trigger.x, trigger.y, trigger.width, trigger.height);
                if (knight.hitbox.overlaps(rect)) {
                    if (knight.velocityY > 0) {
                        knight.y = rect.y - knight.hitboxOffsetY - knight.height;
                        knight.velocityY = 0;
                    } else if (knight.velocityY <= 0) {
                        knight.y = rect.y + rect.height - knight.hitboxOffsetY;
                        knight.velocityY = 0;

                        if (!knight.isGrounded && (knight.currentState == Knight.State.FALL || knight.currentState == Knight.State.AIRBORNE || knight.currentState == Knight.State.JUMP || knight.currentState == Knight.State.DOUBLE_JUMP)) {
                            knight.landingTimer = 0.12f;
                            knight.changeState(Knight.State.LANDING);
                        }
                        grounded = true;
                        knight.canDoubleJump = true;
                        knight.canDash = true;
                    }
                    knight.updateHitbox();
                }
            }
        }
        knight.isGrounded = grounded;
    }

    // متد اعمال دمیج به شوالیه، قطع فوکوس در حال شارژ و اعمال فیزیک پرتاب به عقب ناشی از ضربه
    public void takeDamage(float sourceX) {
        // جلوگیری قطعی از توقف شوالیه توسط تابع آسیب، هنگام دَش زدن با Sharp Shadow یا گادمود
        if (knight.isGodMode) return;
        if (knight.currentState == Knight.State.DASHING && knight.hasSharpShadow) return;

        if (knight.invulnerableTimer <= 0) {
            if (knight.currentState == Knight.State.FOCUSING) {
                knight.focusTimer = 0f;
                AudioManager.getInstance().stopSFX("focus_health_charging.wav");
                AudioManager.getInstance().stopSFX("focus_health_heal.wav");
            }

            float knightCenterX = knight.x + knight.hitboxOffsetX + (knight.width / 2f);
            boolean damageFromRight = knightCenterX < sourceX;

            knight.takeDamage(1, damageFromRight);

            knight.velocityY = knight.JUMP_VELOCITY * 0.45f;
            knight.isGrounded = false;

            knight.knockbackTimer = 0.25f;
            knight.velocityX = knight.knockbackVelX;

            knight.changeState(Knight.State.AIRBORNE);
            knight.updateHitbox();
        }
    }

    // متد ماشین وضعیت خودکار (State Machine) جهت انتقال ایمن شوالیه بین انیمیشن‌های پرش، سقوط، دویدن و سکون
    private void updateState() {
        if (knight.currentState == Knight.State.DASHING ||
            knight.currentState == Knight.State.ATTACKING ||
            knight.currentState == Knight.State.FOCUSING ||
            knight.currentState == Knight.State.WALL_SLIDE ||
            knight.currentState == Knight.State.WALL_JUMPING ||
            knight.currentState == Knight.State.CAST_FIREBALL ||
            knight.currentState == Knight.State.CAST_SCREAM ||
            knight.landingTimer > 0 ||
            knight.runToIdleTimer > 0) {

            if (knight.currentState == Knight.State.WALL_JUMPING) {
                if (knight.velocityY <= 0 || knight.isGrounded) {
                    knight.changeState(knight.isGrounded ? Knight.State.IDLE : Knight.State.FALL);
                }
            }
            return;
        }

        if (!knight.isGrounded) {
            if (knight.currentState == Knight.State.DOUBLE_JUMP) return;
            if (knight.currentState == Knight.State.JUMP) return;

            if (knight.velocityY > 180f) {
                knight.changeState(Knight.State.JUMP);
            } else if (knight.velocityY <= 180f && knight.velocityY >= -180f) {
                knight.changeState(Knight.State.AIRBORNE);
            } else {
                knight.changeState(Knight.State.FALL);
            }
        } else {
            if (knight.velocityX != 0) {
                knight.changeState(Knight.State.RUN);
            } else {
                if (knight.currentState == Knight.State.RUN) {
                    knight.runToIdleTimer = 0.15f;
                    knight.changeState(Knight.State.RUN_TO_IDLE);
                } else {
                    knight.changeState(Knight.State.IDLE);
                }
            }
        }
    }
}
