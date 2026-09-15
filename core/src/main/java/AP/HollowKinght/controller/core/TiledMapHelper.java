package AP.HollowKinght.controller.core;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TiledMapHelper {
    private TiledMap tiledMap;
    private float mapHeightInPixels = 0f;

    // بارگذاری فایل نقشه Tiled و محاسبه ارتفاع کلی آن بر اساس پیکسل
    public TiledMap loadMap(String path) {
        tiledMap = new TmxMapLoader().load(path);

        int mapHeightInTiles = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        mapHeightInPixels = mapHeightInTiles * tileHeight;

        return tiledMap;
    }

    // استخراج تمام تریگرهای محیطی و زون‌های صوتی از لایه‌های مختلف نقشه
    public Array<MapTrigger> getMapTriggers() {
        Array<MapTrigger> triggers = new Array<>();

        // ---- بخش اول: خواندن تریگرهای معمولی از Object Layer 1 ----
        MapLayer layer1 = tiledMap.getLayers().get("Object Layer 1");
        if (layer1 != null) {
            for (MapObject object : layer1.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    String typeProp = object.getProperties().get("type", String.class);
                    if (typeProp != null) {
                        MapTrigger trigger = new MapTrigger(rect.x, rect.y, rect.width, rect.height, typeProp);

                        // بررسی و لود ویژگی اختصاصی 't' در صورت وجود در پروپرتی‌های Tiled
                        if (object.getProperties().containsKey("t")) {
                            trigger.t = object.getProperties().get("t", String.class);
                        }
                        triggers.add(trigger);
                    }
                }
            }
        }

        // ---- بخش دوم: خواندن زون‌های صوتی پویا از Object Layer 2 ----
        MapLayer layer2 = tiledMap.getLayers().get("Object Layer 2");
        if (layer2 != null) {
            for (MapObject object : layer2.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    // خواندن پروپرتی سفارشی bgm که در نرم‌افزار Tiled ساخته شده است
                    if (object.getProperties().containsKey("bgm")) {
                        String bgmValue = object.getProperties().get("bgm", String.class);

                        // اضافه کردن زون به تریگرها با تایپ اختصاصی "audio_zone" برای پخش موزیک
                        triggers.add(new MapTrigger(rect.x, rect.y, rect.width, rect.height, "audio_zone", bgmValue));
                    }
                }
            }
        }

        return triggers;
    }

    // استخراج نقاط اسپاون (پیدایش بازیکن/آیتم‌ها/دیوار شکسته) از نقشه
    public Array<SpawnPoint> getSpawnPoints() {
        Array<SpawnPoint> spawns = new Array<>();
        MapLayer layer = tiledMap.getLayers().get("Object Layer 1");
        if (layer == null) return spawns;

        for (MapObject object : layer.getObjects()) {
            // شناسایی شیء در صورتی که پروپرتی کلیدی spawn را داشته باشد
            if (object.getProperties().containsKey("spawn")) {
                String spawnValue = object.getProperties().get("spawn", String.class);

                // لود ایمن مختصات و ابعاد اسپاون پوینت همراه با مقادیر فال‌بک پیش‌فرض
                float x = object.getProperties().get("x", Float.class) != null ? object.getProperties().get("x", Float.class) : 0f;
                float y = object.getProperties().get("y", Float.class) != null ? object.getProperties().get("y", Float.class) : 0f;
                float width = object.getProperties().get("width", Float.class) != null ? object.getProperties().get("width", Float.class) : 32f;
                float height = object.getProperties().get("height", Float.class) != null ? object.getProperties().get("height", Float.class) : 64f;

                spawns.add(new SpawnPoint(x, y, width, height, spawnValue));
            }
        }
        return spawns;
    }
}
