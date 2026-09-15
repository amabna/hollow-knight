package AP.HollowKinght.controller.core;

public class MapTrigger {
    // مختصات ابعادی تریگر در نقشه بازی
    public float x, y, width, height;

    // نوع تریگر (مانند hidden_room، erase یا audio_zone)
    public String type;

    // مسیر یا نام فایل موزیک پس‌زمینه مخصوص زون‌های صوتی پویا
    public String bgmFile;

    // فیلد کمکی برای ذخیره ویژگی‌های اختصاصی مپ (مانند تایل‌های در مخفی)
    public String t;

    // سازنده قدیمی: برای حفظ سازگاری و جلوگیری از خراب شدن منطق‌های قبلی نقشه
    public MapTrigger(float x, float y, float width, float height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    // سازنده جدید: مخصوص راه‌اندازی زون‌های صوتی پویا و داینامیک نقشه
    public MapTrigger(float x, float y, float width, float height, String type, String bgmFile) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.bgmFile = bgmFile;
    }
}
