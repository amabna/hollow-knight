package AP.HollowKinght.model.core;

import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    public float x, y;
    public float width, height;
    public float velocityX, velocityY;
    public Rectangle hitbox;

    public Entity(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public void updateHitbox() {
        hitbox.setPosition(x, y);
    }
}
