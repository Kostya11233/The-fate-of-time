package com.thefateoftime.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class CollectibleObject {
    public enum Type {
        ITEM,
        NOTE,
        MA_EXIT
    }

    private final Type type;
    private final String id;
    private Texture texture;
    private final Body body;
    private final Vector2 position;
    private final float width;
    private final float height;
    private boolean collected;
    private static Texture debugTexture;

    public static final float PPM = 32f;

    public CollectibleObject(Type type, String id, Texture texture, World world,
                             float x, float y, float width, float height) {
        this.type = type;
        this.id = id;
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.collected = false;

        // Если текстура null, создаём отладочную
        if (texture == null) {
            this.texture = getDebugTexture();
            Gdx.app.error("COLLECTIBLE", "NULL texture for " + type + " '" + id + "' - using debug texture");
        } else {
            this.texture = texture;
            Gdx.app.log("COLLECTIBLE", "Texture OK for " + type + " '" + id + "' size=" + texture.getWidth() + "x" + texture.getHeight());
        }

        Gdx.app.log("COLLECTIBLE", "CREATE " + type + " id='" + id + "' pos=(" + x + "," + y + ") size=(" + width + "x" + height + ")");

        // Создаём физическое тело
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((x + width / 2) / PPM, (y + height / 2) / PPM);

        this.body = world.createBody(bodyDef);
        this.body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / PPM, height / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        Gdx.app.log("COLLECTIBLE", "Body created at world pos: " + body.getPosition());
    }

    private static Texture getDebugTexture() {
        if (debugTexture == null) {
            int size = 64;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 0f, 1f, 1f); // Ярко-пурпурный
            pixmap.fill();
            pixmap.setColor(0f, 0f, 0f, 1f);
            pixmap.drawRectangle(0, 0, size, size);
            pixmap.drawLine(0, 0, size, size);
            pixmap.drawLine(size, 0, 0, size);
            debugTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return debugTexture;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Body getBody() {
        return body;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        Gdx.app.log("COLLECTIBLE", "COLLECTED: " + type + " '" + id + "'");
        collected = true;
        if (body.getWorld() != null) {
            body.getWorld().destroyBody(body);
        }
    }

    public void render(SpriteBatch batch) {
        if (!collected && texture != null) {
            batch.draw(texture, position.x, position.y, width, height);
            // Логируем только каждый 60-й кадр чтобы не спамить
            if (Gdx.graphics.getFrameId() % 60 == 0) {
                Gdx.app.log("COLLECTIBLE", "RENDER " + type + " '" + id + "' at pixel(" + position.x + "," + position.y + ") size(" + width + "x" + height + ")");
            }
        }
    }

    public void dispose() {
        if (texture != null && texture != debugTexture) {
            texture.dispose();
            texture = null;
        }
    }
}