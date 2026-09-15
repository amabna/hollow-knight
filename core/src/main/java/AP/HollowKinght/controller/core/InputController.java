package AP.HollowKinght.controller.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class InputController {
    public boolean isLeftPressed() { return Gdx.input.isKeyPressed(Keys.LEFT); }
    public boolean isRightPressed() { return Gdx.input.isKeyPressed(Keys.RIGHT); }
    public boolean isDownPressed() { return Gdx.input.isKeyPressed(Keys.DOWN); }
    public boolean isUpPressed() { return Gdx.input.isKeyPressed(Keys.UP); }

    public boolean isFocusPressed() { return Gdx.input.isKeyPressed(Keys.A); }
    public boolean isJumpPressed() { return Gdx.input.isKeyPressed(Keys.Z); }
    public boolean isJumpJustPressed() { return Gdx.input.isKeyJustPressed(Keys.Z); }
    public boolean isDashJustPressed() { return Gdx.input.isKeyJustPressed(Keys.C); }
    public boolean isAttackJustPressed() { return Gdx.input.isKeyJustPressed(Keys.X); }
    public boolean isSpell1JustPressed() { return Gdx.input.isKeyJustPressed(Keys.S); }
    public boolean isSpell2JustPressed() { return Gdx.input.isKeyJustPressed(Keys.D); }
}
