package com.mygdx.game.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.TheFateGame;
import java.util.HashMap;
import java.util.Map;

public class ItemManager {
    private TheFateGame game;
    private World world;
    private Map<Body, String> itemBodies;
    private Array<Body> bodiesToDestroy;
    private int collectedItems;
    private int totalItems;
    private String chapterPrefix;

    public ItemManager(TheFateGame game, World world, String chapterPrefix) {
        this.game = game;
        this.world = world;
        this.chapterPrefix = chapterPrefix;
        this.itemBodies = new HashMap<>();
        this.bodiesToDestroy = new Array<>();
        this.collectedItems = 0;
        this.totalItems = 0;
    }

    public void loadItemsFromMap(TiledMap tiledMap, String currentMap) {
        // Ищем слои с предметами
        for (MapLayer layer : tiledMap.getLayers()) {
            String layerName = layer.getName();
            if (layerName != null && layerName.startsWith("item")) {
                for (MapObject obj : layer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;

                    String itemId = currentMap + "_" + layerName;
                    boolean alreadyCollected = game.prefs.getBoolean(chapterPrefix + "_" + itemId, false);

                    if (!alreadyCollected) {
                        BodyDef def = new BodyDef();
                        def.type = BodyDef.BodyType.StaticBody;
                        def.position.set((rect.x + rect.width/2) / 32f, (rect.y + rect.height/2) / 32f);
                        Body body = world.createBody(def);

                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(rect.width/2 / 32f, rect.height/2 / 32f);
                        Fixture fix = body.createFixture(shape, 0);
                        fix.setSensor(true);
                        shape.dispose();

                        itemBodies.put(body, itemId);
                        totalItems++;
                    }
                }
            }
        }
    }

    public boolean collectItem(Body itemBody, String itemId) {
        if (!itemBodies.containsKey(itemBody)) return false;

        collectedItems++;
        bodiesToDestroy.add(itemBody);
        itemBodies.put(itemBody, "collected");
        game.prefs.putBoolean(chapterPrefix + "_" + itemId, true);
        game.prefs.flush();

        return true;
    }

    public void destroyMarkedBodies() {
        for (Body body : bodiesToDestroy) {
            if (body != null && body.getWorld() == world) {
                world.destroyBody(body);
            }
        }
        bodiesToDestroy.clear();
    }

    public boolean isAllCollected() {
        return collectedItems >= totalItems && totalItems > 0;
    }

    public int getCollectedItems() { return collectedItems; }
    public int getTotalItems() { return totalItems; }

    public boolean isItemBody(Body body) {
        return itemBodies.containsKey(body) && !itemBodies.get(body).equals("collected");
    }

    public String getItemId(Body body) {
        return itemBodies.get(body);
    }

    public void reset() {
        collectedItems = 0;
        totalItems = 0;
        itemBodies.clear();
        bodiesToDestroy.clear();
    }

    private Rectangle getObjectRectangle(MapObject obj) {
        if (obj instanceof RectangleMapObject) return ((RectangleMapObject) obj).getRectangle();
        Float x = obj.getProperties().get("x", Float.class);
        Float y = obj.getProperties().get("y", Float.class);
        Float w = obj.getProperties().get("width", Float.class);
        Float h = obj.getProperties().get("height", Float.class);
        if (x != null && y != null) {
            return new Rectangle(x, y, w != null ? w : 100, h != null ? h : 100);
        }
        return null;
    }
}