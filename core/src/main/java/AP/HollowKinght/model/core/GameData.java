package AP.HollowKinght.model.core;

import java.util.ArrayList;

public class GameData {
    // ۱. متغیرهای مربوط به موقعیت شوالیه و مشخصات اصلی دنیا
    public float playerX;
    public float playerY;
    public int health;
    public int soul;
    public float totalGameTime;
    public boolean isGodMode;
    public boolean isNoclip;

    // ۲. وضعیت اینونتوری، چارم‌های فعال و قلب وید
    public int[] equippedCharms = new int[3];
    public boolean isVoidHeartActive;
    public int filledNotchesCount;

    // ۳. متغیرهای مربوط به خراب شدن دیوار اتاق مخفی
    public boolean isHiddenRoomDestroyed;
    public float hiddenDoorHP;

    // ۴. وضعیت سلامتی و فازهای حرکت باس فالس نایت
    public float falseKnightX;
    public float falseKnightY;
    public int falseKnightHP;
    public boolean falseKnightIsDead;
    public String falseKnightPhase;       // وضعیت فعلی باس رو ذخیره میکنه مثل ایستاده یا گیج شده
    public boolean falseKnightIsPhase2;   // مشخص میکنه باس وارد فاز دو شده یا نه
    public boolean falseKnightHasBeenStunned;

    // ۵. وضعیت دیالوگ‌ها و هوش مصنوعی زوت
    public float zoteX;
    public float zoteY;
    public int zoteHP;
    public boolean zoteIsDead;
    public int zoteAiState;               // وضعیت هوش مصنوعی زوت رو نگه میداره
    public int zoteDialogueIndex;         // شماره دیالوگی که زوت الان باید بگه

    // متغیرهای آماری جدید برای تعداد کشته‌ها و مرگ‌ها
    public int totalMobsKilled;           // کل دشمن‌هایی که توی این اسلات بازی کشته شدن
    public int knightDeathCount;          // تعداد دفعاتی که خود نایت مرده
    public boolean killedTiktik;          // آیا تا حالا تیک‌تیک کشته یا نه
    public boolean killedHusk;            // آیا تا حالا هاسک کشته یا نه
    public boolean killedMosq;            // آیا تا حالا موسکیتو کشته یا نه
    public boolean killedCrys;            // آیا تا حالا انمی کریستالی کشته یا نه

    // ساختار داخلی برای ذخیره کردن وضعیت تک‌تک انمی‌های معمولی روی نقشه
    public static class EnemySaveState {
        public String type;
        public float x;
        public float y;
        public int health;
        public boolean isDead;
    }
    public ArrayList<EnemySaveState> enemiesState = new ArrayList<>();
}
