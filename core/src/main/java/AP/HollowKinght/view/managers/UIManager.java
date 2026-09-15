package AP.HollowKinght.view.managers;

import AP.HollowKinght.view.screens.AbstractScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class UIManager {
    private static UIManager instance;
    private Game game;

    private UIManager() {}

    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager();
        }
        return instance;
    }

    public void initialize(Game game) {
        this.game = game;
    }

    //تغییر مستقیم و آنی اسکرین (حفظ سازگاری با کدهای هسته منوهای قبلی)
    public void changeScreen(AbstractScreen newScreen) {
        if (game == null) return;
        newScreen.buildStage();
        game.setScreen(newScreen);
        Gdx.input.setInputProcessor(newScreen.getStage());
    }

    //سوییچ اسکرین هوشمند مجهز به انیمیشن فیدآوت ۱.۵ ثانیه‌ای در تمام صفحات بازی
    public void changeScreenWithFade(AbstractScreen newScreen) {
        if (game == null) return;

        if (game.getScreen() instanceof AbstractScreen) {
            // شروع پروسه فیدآوت صوتی و گرافیکی اسکرین فعلی تا رسیدن به تاریکی مطلق
            ((AbstractScreen) game.getScreen()).startFadeOutTo(newScreen);
        } else {
            changeScreen(newScreen);
        }
    }


    //تغییر مستقیم اسکرین که توسط خود کلاس انتزاعی AbstractScreen در لحظه اوج FadeOut تاریکی صدا زده می‌شود
    public void changeScreenDirectly(AbstractScreen newScreen) {
        if (game == null) return;
        newScreen.buildStage();
        game.setScreen(newScreen);
        Gdx.input.setInputProcessor(newScreen.getStage());
    }
}
