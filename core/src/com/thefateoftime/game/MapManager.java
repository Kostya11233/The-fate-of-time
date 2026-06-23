package com.thefateoftime.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import java.util.HashMap;
import java.util.Map;

public class MapManager {
    private World world;
    private TiledMap tiledMap;
    private Map<Body, String> teleportBodies;
    private Map<Body, Boolean> killZones;
    private Body exitBody;
    private boolean exitUnlocked = false;

    private static final float PPM = 32f;

    public MapManager(World world, TiledMap tiledMap) {
        this.world = world;
        this.tiledMap = tiledMap;
        this.teleportBodies = new HashMap<>();
        this.killZones = new HashMap<>();
    }

    public void createCollisionAndTeleports() {
        // Стены коллизии
        MapLayer collisionLayer = tiledMap.getLayers().get("collision");
        if (collisionLayer != null) {
            for (MapObject obj : collisionLayer.getObjects()) {
                createStaticBody(obj, "wall");
            }
        }

        // Телепорты/двери
        for (MapLayer layer : tiledMap.getLayers()) {
            String name = layer.getName();
            if (name != null && (name.startsWith("door") || name.startsWith("exit"))) {
                for (MapObject obj : layer.getObjects()) {
                    Body body = createSensorBody(obj);
                    teleportBodies.put(body, name);
                }
            }
        }

        // Выход с уровня
        MapLayer exitLayer = tiledMap.getLayers().get("exit");
        if (exitLayer != null) {
            for (MapObject obj : exitLayer.getObjects()) {
                exitBody = createSensorBody(obj);
                if (!exitUnlocked) {
                    exitBody.setActive(false);
                }
            }
        }
    }

    public void createJumpPads() {
        MapLayer jumpLayer = tiledMap.getLayers().get("jump");
        if (jumpLayer != null) {
            for (MapObject obj : jumpLayer.getObjects()) {
                Body body = createSensorBody(obj);
                body.setUserData("jump_pad");
            }
        }
    }

    public void createKillZones() {
        MapLayer killLayer = tiledMap.getLayers().get("kill");
        if (killLayer != null) {
            for (MapObject obj : killLayer.getObjects()) {
                Body body = createSensorBody(obj);
                killZones.put(body, true);
            }
        }
    }

    public void createInteractiveObjects() {
        // Объекты для взаимодействия (например, Ма)
        for (int i = 1; i <= 10; i++) {
            MapLayer layer = tiledMap.getLayers().get("z" + i);
            if (layer != null) {
                for (MapObject obj : layer.getObjects()) {
                    createSensorBody(obj);
                }
            }
        }
    }

    private Body createStaticBody(MapObject obj, String userData) {
        Rectangle rect = getObjectRectangle(obj);
        if (rect == null) return null;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
        Body body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
        body.createFixture(shape, 0);
        body.setUserData(userData);
        shape.dispose();

        return body;
    }

    private Body createSensorBody(MapObject obj) {
        Rectangle rect = getObjectRectangle(obj);
        if (rect == null) return null;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
        Body body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
        Fixture fix = body.createFixture(shape, 0);
        fix.setSensor(true);
        shape.dispose();

        return body;
    }

    public boolean isGround(Body body) {
        Object data = body.getUserData();
        return data != null && (data.equals("wall") || data.equals("ground"));
    }

    public boolean isTeleport(Body body) {
        return teleportBodies.containsKey(body);
    }

    public String getTeleportTarget(Body body) {
        return teleportBodies.get(body);
    }

    public boolean isOnKillZone(Body playerBody) {
        // Проверка через контакт, упрощенно
        return false;
    }

    public boolean isExit(Body body) {
        return body == exitBody;
    }

    public void unlockExit() {
        exitUnlocked = true;
        if (exitBody != null) {
            exitBody.setActive(true);
        }
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