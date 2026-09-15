package AP.HollowKinght.controller.core;

import AP.HollowKinght.model.enemy.Enemy;

public class EnemySpawnTracker {
    public float spawnX, spawnY;
    public String type;
    public Enemy activeEnemy;
    public boolean hasReset = false;
    public int savedHealth = -1;
    public boolean isPermanentlyDead = false;

    // تنظیم مختصات اولیه و نوع انمی موقع ساخت روی نقشه
    public EnemySpawnTracker(float x, float y, String type) {
        this.spawnX = x;
        this.spawnY = y;
        this.type = type;
    }
}
