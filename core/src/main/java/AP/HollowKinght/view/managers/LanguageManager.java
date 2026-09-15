package AP.HollowKinght.view.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;

public class LanguageManager {
    public static I18NBundle bundle;

    public static void loadLanguage(String langCode) {
        // ۱. استانداردسازی کد زبان
        String code = langCode.toLowerCase().trim();
        if (code.contains("eng")) code = "en";
        if (code.contains("fra")) code = "fr";
        if (!code.equals("en") && !code.equals("fr")) code = "en";

        try {
            // ۲. آدرس‌دهی مستقیم و صریح به فایل پروپرتیز بدون دخالت لوکال سیستم‌عامل
            // (اگر فایل‌ها مستقیم در assets هستند i18n/ را حذف کنید)
            FileHandle fileHandle = Gdx.files.internal("i18n/strings_" + code);

            // استفاده از Locale.ROOT باعث می‌شود LibGDX دنبال هیچ فال‌بکی نگردد و دقیقاً همین فایل را باز کند
            bundle = I18NBundle.createBundle(fileHandle, Locale.ROOT);
        } catch (Exception e) {
            // فال‌بک امن به انگلیسی در صورت بروز هرگونه خطای پیش‌بینی نشده
            FileHandle fileHandle = Gdx.files.internal("i18n/strings_en");
            bundle = I18NBundle.createBundle(fileHandle, Locale.ROOT);
        }
    }
}
