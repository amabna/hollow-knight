package AP.HollowKinght.controller.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Queue;

public class AchievementPopup {
    private static AchievementPopup instance;

    public boolean isActive = false;
    public float alpha = 0f;

    private BitmapFont font;
    private final GlyphLayout layout;
    private final Matrix4 popupMatrix;
    private Queue<String> achievementQueue = new Queue<>();
    private String currentText = "";
    private float displayTimer = 0f;
    private final float TOTAL_DISPLAY_TIME = 6.5f;

    private AchievementPopup() {
        this.layout = new GlyphLayout();
        this.popupMatrix = new Matrix4();
        this.font = new BitmapFont();
    }

    public static AchievementPopup getInstance() {
        if (instance == null) {
            instance = new AchievementPopup();
        }
        return instance;
    }

    public void setFont(BitmapFont gameFont) {
        if (gameFont != null) {
            this.font = gameFont;
        }
    }

    // اضافه کردن اچیومنت جدید به صف نوبت
    public void show(String text) {
        achievementQueue.addLast(text);
    }

    public void update(float dt) {
        // اگه اچیومنتی نشون داده نمیشه و صف پره، بعدی رو بیار
        if (!isActive && !achievementQueue.isEmpty()) {
            currentText = achievementQueue.removeFirst();
            isActive = true;
            displayTimer = TOTAL_DISPLAY_TIME;
            alpha = 0f;

            AP.HollowKinght.view.audio.AudioManager.getInstance().playSFX("unlock.mp3");
        }

        if (isActive) {
            displayTimer -= dt;

            // محاسبه مقدار محو شدگی افکت (Fade in / Fade out)
            if (displayTimer > TOTAL_DISPLAY_TIME - 0.5f) {
                alpha = (TOTAL_DISPLAY_TIME - displayTimer) / 0.5f;
            } else if (displayTimer < 0.5f) {
                alpha = displayTimer / 0.5f;
            } else {
                alpha = 1f;
            }

            if (alpha < 0f) alpha = 0f;
            if (alpha > 1f) alpha = 1f;

            if (displayTimer <= 0) {
                isActive = false;
            }
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!isActive || currentText.isEmpty()) return;

        layout.setText(font, currentText);
        popupMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Matrix4 oldBatchMatrix = batch.getProjectionMatrix().cpy();
        Matrix4 oldShapeMatrix = shapeRenderer.getProjectionMatrix().cpy();

        shapeRenderer.setProjectionMatrix(popupMatrix);
        batch.setProjectionMatrix(popupMatrix);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        float boxWidth = Math.max(480f, layout.width + 80f);
        float boxHeight = 75f;
        float boxX = (Gdx.graphics.getWidth() - boxWidth) / 2f;
        float boxY = 70f;
        float radius = 15f;

        // رسم پس‌زمینه بنفش پاپ‌آپ
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.427f, 0.231f, 0.478f, alpha * 0.9f);
        shapeRenderer.rect(boxX + radius, boxY, boxWidth - (2 * radius), boxHeight);
        shapeRenderer.rect(boxX, boxY + radius, boxWidth, boxHeight - (2 * radius));
        shapeRenderer.circle(boxX + radius, boxY + radius, radius);
        shapeRenderer.circle(boxX + boxWidth - radius, boxY + radius, radius);
        shapeRenderer.circle(boxX + radius, boxY + boxHeight - radius, radius);
        shapeRenderer.circle(boxX + boxWidth - radius, boxY + boxHeight - radius, radius);
        shapeRenderer.end();

        // رسم کادر و خطوط طلایی دور پاپ‌آپ
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1.0f, 0.843f, 0.0f, alpha * 0.95f);
        shapeRenderer.line(boxX + radius, boxY, boxX + boxWidth - radius, boxY);
        shapeRenderer.line(boxX + radius, boxY + boxHeight, boxX + boxWidth - radius, boxY + boxHeight);
        shapeRenderer.line(boxX, boxY + radius, boxX, boxY + boxHeight - radius);
        shapeRenderer.line(boxX + boxWidth, boxY + radius, boxX + boxWidth, boxY + boxHeight - radius);
        shapeRenderer.arc(boxX + radius, boxY + radius, radius, 180, 90);
        shapeRenderer.arc(boxX + boxWidth - radius, boxY + radius, radius, 270, 90);
        shapeRenderer.arc(boxX + radius, boxY + boxHeight - radius, radius, 90, 90);
        shapeRenderer.arc(boxX + boxWidth - radius, boxY + boxHeight - radius, radius, 0, 90);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // وسط‌چین کردن و نوشتن متن اچیومنت
        batch.begin();
        font.setColor(1f, 1f, 1f, alpha);
        float textX = boxX + (boxWidth - layout.width) / 2f;
        float textY = boxY + (boxHeight + layout.height) / 2f;
        font.draw(batch, currentText, textX, textY);
        batch.end();

        batch.setProjectionMatrix(oldBatchMatrix);
        shapeRenderer.setProjectionMatrix(oldShapeMatrix);
        batch.begin();
    }
}
