package AP.HollowKinght.controller.core;

public class SpawnPoint {
    public float x, y, width, height;
    public String spawnType;

    public SpawnPoint(float x, float y, float width, float height, String spawnType) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.spawnType = spawnType;
    }
}
