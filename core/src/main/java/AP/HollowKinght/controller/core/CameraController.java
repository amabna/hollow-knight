package AP.HollowKinght.controller.core;

import AP.HollowKinght.model.player.Knight;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class CameraController {

    private final float LERP_FACTOR = 0.001f;

    public void updateCamera(OrthographicCamera camera, Knight knight, float dt) {
        // محاسبه نقطه هدف وسط شوالیه با کمی فاصله
        float targetX = knight.x + knight.width / 2f + 50f;
        float targetY = knight.y + knight.height / 2f;

        // حرکت نرم دوربین به سمت شوالیه با فرمول لِرپ
        camera.position.x += (targetX - camera.position.x) * LERP_FACTOR;
        camera.position.y += (targetY - camera.position.y) * LERP_FACTOR;

        // اگه شوالیه ضربه خورده باشه صفحه رو می‌لرزونه
        if (knight.shakeTime > 0) {
            float currentIntensity = knight.shakeIntensity * (knight.shakeTime / 0.25f);
            camera.position.x += MathUtils.random(-currentIntensity, currentIntensity);
            camera.position.y += MathUtils.random(-currentIntensity, currentIntensity);
        }

        camera.update();
    }
}
