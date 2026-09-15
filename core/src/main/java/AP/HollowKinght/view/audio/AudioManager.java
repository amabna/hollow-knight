package AP.HollowKinght.view.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;

public class AudioManager implements Disposable {

    private static AudioManager instance;
    private Preferences prefs;

    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private boolean isMusicOn = true;
    private boolean isSfxOn = true;

    private final HashMap<String, Sound> sfxCache;
    private Music currentMusic;
    private Music targetMusic;
    private String currentMusicName;
    private String targetMusicName; // نام آهنگ جدید که در صف پخش تدریجی قرار دارد

    // تنظیم فید روان صوتی روی ۱.۵ ثانیه زمان ثابت
    private final float fadeSpeed = 1.0f / 1.5f;
    private float currentMusicVol = 1.0f;
    private float targetMusicVol = 0.0f;

    private enum FadeState { NONE, FADING_OUT, FADING_IN }
    private FadeState fadeState = FadeState.NONE;

    private AudioManager() {
        sfxCache = new HashMap<>();
        loadSettings();
        preloadEssentialSFX();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadSettings() {
        try {
            prefs = Gdx.app.getPreferences("HollowKnightSettings");
            float rawMusic = prefs.getFloat("musicVolume", 100f);
            float rawSfx = prefs.getFloat("sfxVolume", 100f);

            this.musicVolume = rawMusic / 100f;
            this.sfxVolume = rawSfx / 100f;

            this.isMusicOn = prefs.getBoolean("isMusicOn", true);
            this.isSfxOn = prefs.getBoolean("isSfxOn", true);

            if (this.musicVolume <= 0 && rawMusic == 100f) this.musicVolume = 1.0f;
            if (this.sfxVolume <= 0 && rawSfx == 100f) this.sfxVolume = 1.0f;
        } catch (Exception e) {
            this.musicVolume = 1.0f;
            this.sfxVolume = 1.0f;
        }
    }

    // پیش‌بارگذاری افکت‌های صوتی کلیدی در ساختار حافظه کش جهت ارتقای پرفورمنس
    private void preloadEssentialSFX() {
        String[] sfxFiles = {
            "hero_tentacle_sword.wav", "hero_damage.wav", "enemy_damage.wav","back.wav",
            "dark_spell_get.wav", "focus_health_charging.wav", "focus_health_heal.wav",
            "hero_jump.wav", "hero_dash.wav", "hero_land_soft.wav","wiiin.mp3","unlock.mp3",
            "geo_small_collect_1.wav", "enemy_death_sword.wav", "breakable_wall_death.wav",
            "button.wav", "charm_click_in.wav", "damage_to_hero.wav", "fulling_soul.wav",
            "Zote_01.wav","Zote_02.wav","Zote_03.wav","Zote_04.wav","Zote_05.wav",
            "breakable_wall_hit_2.wav","select.wav","oc.wav","hornet_dash.wav","scream.wav","fireball.wav"
        };

        for (String file : sfxFiles) {
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/" + file));
                sfxCache.put(file, sound);
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "خطا در بارگذاری فایل افکت: " + file, e);
            }
        }
    }

    // متد تغییر موزیک اتمسفر مپ به صورت Fade روان صوتی بدون پرش ناگهانی ولوم
    public void playMusicFade(String fileName) {
        if (!isMusicOn) return;

        if (fileName.equals(currentMusicName) && targetMusicName == null) return;
        if (fileName.equals(targetMusicName)) return;

        targetMusicName = fileName;

        if (currentMusic != null && currentMusic.isPlaying()) {
            fadeState = FadeState.FADING_OUT;
            currentMusicVol = 1.0f;
        } else {
            startTargetMusicFadeIn();
        }
    }

    private void startTargetMusicFadeIn() {
        try {
            if (targetMusic != null) {
                targetMusic.stop();
                targetMusic.dispose();
            }

            currentMusicName = targetMusicName;
            targetMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/" + targetMusicName));
            targetMusic.setLooping(true);
            targetMusic.setVolume(0f);
            targetMusic.play();

            targetMusicVol = 0.0f;
            fadeState = FadeState.FADING_IN;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "خطا در بارگذاری موزیک جدید: " + targetMusicName, e);
            fadeState = FadeState.NONE;
            targetMusicName = null;
        }
    }

    // آپدیت گام‌به‌گام فید قطعات موسیقی در بدنه چرخه اصلی بازی (Main Loop)
    public void updateMusicFade(float dt) {
        if (fadeState == FadeState.NONE) {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.setVolume(musicVolume);
            }
            return;
        }

        // فاز اول: کم کردن ولوم موزیک قبلی تا صفر مطلق و حذف آن از حافظه رم
        if (fadeState == FadeState.FADING_OUT) {
            if (currentMusic != null) {
                currentMusicVol -= fadeSpeed * dt;
                if (currentMusicVol <= 0f) {
                    currentMusicVol = 0f;
                    currentMusic.stop();
                    currentMusic.dispose();
                    currentMusic = null;

                    startTargetMusicFadeIn();
                } else {
                    currentMusic.setVolume(currentMusicVol * musicVolume);
                }
            } else {
                startTargetMusicFadeIn();
            }
        }

        // فاز دوم: افزایش گام‌به‌گام قطعه جدید تا مرز ولوم تنظیمات کاربر
        if (fadeState == FadeState.FADING_IN) {
            if (targetMusic != null) {
                targetMusicVol += fadeSpeed * dt;
                if (targetMusicVol >= 1.0f) {
                    targetMusicVol = 1.0f;
                    targetMusic.setVolume(musicVolume);

                    currentMusic = targetMusic;
                    currentMusicVol = 1.0f;

                    targetMusic = null;
                    targetMusicName = null;
                    fadeState = FadeState.NONE;
                } else {
                    targetMusic.setVolume(targetMusicVol * musicVolume);
                }
            } else {
                fadeState = FadeState.NONE;
                targetMusicName = null;
            }
        }
    }

    public void playMusic(String fileName) {
        if (!isMusicOn) return;
        if (currentMusicName != null && currentMusicName.equals(fileName) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }
        stopMusic();
        try {
            currentMusicName = fileName;
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/" + fileName));
            currentMusic.setLooping(true);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "خطا در پخش موزیک: " + fileName, e);
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            try {
                currentMusic.stop();
                currentMusic.dispose();
            } catch (Exception e) {}
            currentMusic = null;
            currentMusicName = null;
        }
        fadeState = FadeState.NONE;
        targetMusicName = null;
    }

    public void playSFX(String fileName) {
        if (!isSfxOn) return;
        Sound sound = sfxCache.get(fileName);
        if (sound != null) {
            sound.play(sfxVolume);
        }
    }

    public void playSFX(String fileName, float volumeMultiplier) {
        if (!isSfxOn) return;
        Sound sound = sfxCache.get(fileName);
        if (sound != null) {
            sound.play(sfxVolume * volumeMultiplier);
        }
    }

    public void stopSFX(String fileName) {
        Sound sound = sfxCache.get(fileName);
        if (sound != null) {
            sound.stop();
        }
    }

    public void playClick() { playSFX("button.wav"); }
    public void playHover() { playSFX("charm_click_in.wav", 0.5f); }

    public void updateMusicSettings(boolean isOn, float volumeFromSlider) {
        this.isMusicOn = isOn;
        this.musicVolume = volumeFromSlider / 100f;

        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
            if (!isMusicOn && currentMusic.isPlaying()) {
                currentMusic.pause();
            } else if (isMusicOn && !currentMusic.isPlaying()) {
                currentMusic.play();
            }
        } else if (isMusicOn && currentMusicName != null) {
            playMusic(currentMusicName);
        }
    }

    public void updateSfxSettings(boolean isOn, float volumeFromSlider) {
        this.isSfxOn = isOn;
        this.sfxVolume = volumeFromSlider / 100f;
    }

    @Override
    public void dispose() {
        stopMusic();
        if (targetMusic != null) targetMusic.dispose();
        for (Sound sound : sfxCache.values()) {
            if (sound != null) sound.dispose();
        }
        sfxCache.clear();
    }
}
