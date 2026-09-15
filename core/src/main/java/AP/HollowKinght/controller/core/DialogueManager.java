package AP.HollowKinght.controller.core;

import AP.HollowKinght.view.managers.LanguageManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import AP.HollowKinght.view.audio.AudioManager;
import java.util.Properties;

public class DialogueManager {
    private static DialogueManager instance;

    private Properties strings = new Properties();
    private boolean isDialogueActive = false;
    private String[] currentPrecepts = new String[3];
    private int currentPageIndex = 0;

    public int globalPreceptCounter = 0;
    private String currentTypedText = "";
    private float typeTimer = 0f;
    private int charIndex = 0;

    public DialogueManager() {
        instance = this;
    }

    public static DialogueManager getInstance() {
        if (instance == null) instance = new DialogueManager();
        return instance;
    }

    // لود کردن فایل متنی زبان بازی
    public void loadLanguage(String langCode) {
        try {
            FileHandle file = Gdx.files.internal("strings_" + langCode + ".properties");
            if (!file.exists() && langCode.equals("en")) {
                file = Gdx.files.internal("strings_en.properties");
            }
            if (file.exists()) {
                strings.load(file.reader("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // شروع دیالوگ زوت و لود کردن ۳ تا از قوانینش پشت سر هم
    public void startZoteDialogue() {
        isDialogueActive = true;
        currentPageIndex = 0;

        for (int i = 0; i < 3; i++) {
            String preceptText = "\n" + LanguageManager.bundle.get("zote_precept_" + globalPreceptCounter);
            currentPrecepts[i] = preceptText;

            globalPreceptCounter++;
            if (globalPreceptCounter > 57) {
                globalPreceptCounter = 1;
            }
        }

        setupPageText();
        playZoteSound();
    }

    private void setupPageText() {
        currentTypedText = "";
        charIndex = 0;
        typeTimer = 0f;
    }

    // افکت تایپ شدن کلمات دیالوگ به صورت حرف به حرف
    public void update(float deltaTime) {
        if (!isDialogueActive) return;

        String fullText = currentPrecepts[currentPageIndex];
        if (charIndex < fullText.length()) {
            typeTimer += deltaTime;
            if (typeTimer >= 0.04f) {
                typeTimer = 0f;
                charIndex++;
                currentTypedText = fullText.substring(0, charIndex);
            }
        }
    }

    // رفتن به خط بعدی دیالوگ یا کامل کردن متن در حال تایپ
    public void advanceDialogue() {
        if (!isDialogueActive) return;

        String fullText = currentPrecepts[currentPageIndex];
        if (charIndex < fullText.length()) {
            charIndex = fullText.length();
            currentTypedText = fullText;
            return;
        }

        currentPageIndex++;
        if (currentPageIndex >= 3) {
            isDialogueActive = false;
        } else {
            setupPageText();
            playZoteSound();
        }
    }

    private void playZoteSound() {
        int rand = MathUtils.random(1, 5);
        String soundPath = "Zote_0" + rand + ".wav";
        AudioManager.getInstance().playSFX(soundPath);
    }

    public boolean isDialogueActive() { return isDialogueActive; }
    public String getCurrentDisplayLanguageText() { return currentTypedText; }
}
