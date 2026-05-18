package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.HashMap;
import java.util.Map;

public class PlatformGameScreen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;
    private boolean onGround = false;
    private Box2DDebugRenderer debugRenderer;
    private static final float PPM = 32f; // пикселей в метре
    private Body playerBody;
    private TextureRegion[] walkRightFrames;
    private TextureRegion[] walkLeftFrames;
    private TextureRegion standFrame;
    private TextureRegion standLeftFrame;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private float stateTime;
    private boolean facingRight = true;
    private boolean movingLeft = false, movingRight = false;
    private float speed = 7f;      // горизонтальная скорость
    private boolean canJump = false;
    private int jumpsLeft = 1;      // обычный прыжок – 1, после бонуса может быть 3
    private int maxJumps = 1;
    private float jumpVelocity = 8f;

    private Stage uiStage;
    private Label itemsLabel;
    private int collectedItems = 0;
    private int totalItems = 2;
    private Map<Body, String> itemBodies = new HashMap<>(); // тело -> тип предмета
    private boolean levelComplete = false;
    private boolean gameOver = false;

    // UI кнопки
    private Texture leftTex, rightTex, jumpTex;
    private ImageButton leftBtn, rightBtn, jumpBtn;

    public PlatformGameScreen(TheFateGame game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        this.uiStage = new Stage(new ExtendViewport(1280, 720));
        loadTextures();
        createUI();
        loadMap();
        createWorld();
        createPlayer();
        createCollisionAndItems();
        Gdx.input.setInputProcessor(uiStage);
    }

    private void loadTextures() {
        Texture stand = new Texture("player/step1.png");
        standFrame = new TextureRegion(stand);
        standLeftFrame = new TextureRegion(stand);
        standLeftFrame.flip(true, false);

        walkRightFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture t = new Texture("player/step" + (i+1) + ".png");
            walkRightFrames[i] = new TextureRegion(t);
        }
        walkRightAnimation = new Animation<>(0.12f, walkRightFrames);

        walkLeftFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture t = new Texture("player/step" + (i+1) + ".png");
            walkLeftFrames[i] = new TextureRegion(t);
            walkLeftFrames[i].flip(true, false);
        }
        walkLeftAnimation = new Animation<>(0.12f, walkLeftFrames);

        leftTex = new Texture("button/button_left.png");
        rightTex = new Texture("button/button_right.png");
        jumpTex = new Texture("button/button_jump.png"); // нужна текстура прыжка, можно взять любую
    }

    private void createUI() {
        leftBtn = new ImageButton(new TextureRegionDrawable(leftTex));
        leftBtn.setSize(80, 80);
        leftBtn.setPosition(50, 50);
        leftBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                movingLeft = true;
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                movingLeft = false;
            }
        });

        rightBtn = new ImageButton(new TextureRegionDrawable(rightTex));
        rightBtn.setSize(80, 80);
        rightBtn.setPosition(150, 50);
        rightBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                movingRight = true;
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                movingRight = false;
            }
        });

        jumpBtn = new ImageButton(new TextureRegionDrawable(jumpTex));
        jumpBtn.setSize(80, 80);
        jumpBtn.setPosition(250, 50);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!levelComplete && !gameOver) {
                    if (onGround) {
                        playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpVelocity);
                        onGround = false;
                        jumpsLeft = maxJumps - 1;
                    } else if (jumpsLeft > 0) {
                        playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpVelocity);
                        jumpsLeft--;
                    }
                }
                return true;
            }
        });

        uiStage.addActor(leftBtn);
        uiStage.addActor(rightBtn);
        uiStage.addActor(jumpBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20, Gdx.graphics.getHeight() - 50);
        uiStage.addActor(itemsLabel);
        updateItemsLabel();
    }

    private void updateItemsLabel() {
        itemsLabel.setText(game.languageManager.format("required_items", collectedItems, totalItems));
    }

    private void loadMap() {
        try {
            tiledMap = new TmxMapLoader().load("gl2L1.tmx");
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            int mapWidth = tiledMap.getProperties().get("width", Integer.class) * 32;
            int mapHeight = tiledMap.getProperties().get("height", Integer.class) * 32;
            System.out.println("Карта загружена: gl2L1.tmx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createWorld() {
        world = new World(new Vector2(0, -20f), true);
        debugRenderer = new Box2DDebugRenderer();
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                handleContact(a, b);
                handleContact(b, a);

                // Проверка касания земли
                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) {
                    onGround = true;
                    jumpsLeft = maxJumps;
                }
            }

            @Override
            public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) {
                    onGround = false;
                }
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private boolean isGround(Body body) {
        return body.getUserData() instanceof String && "ground".equals(body.getUserData());
    }
    private void handleContact(Body bodyA, Body bodyB) {
        if (bodyA == playerBody && itemBodies.containsKey(bodyB)) {
            String type = itemBodies.get(bodyB);
            if (type.startsWith("item")) {
                collectedItems++;
                updateItemsLabel();
                world.destroyBody(bodyB);
                itemBodies.remove(bodyB);
            } else if (type.equals("jump")) {
                maxJumps = 3;
                jumpsLeft = maxJumps;
                world.destroyBody(bodyB);
                itemBodies.remove(bodyB);
            } else if (type.equals("kill")) {
                gameOver = true;
                showGameOver();
            } else if (type.equals("exit")) {
                if (collectedItems >= totalItems) {
                    levelComplete = true;
                    showLevelComplete();
                }
            }
        }
    }

    private void createPlayer() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        // временная позиция в метрах
        def.position.set(100 / PPM, 200 / PPM);
        playerBody = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f); // ~16px
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        playerBody.setFixedRotation(true);
    }

    private void createCollisionAndItems() {
        // Слой collision – статические прямоугольники
        MapLayer collisionLayer = tiledMap.getLayers().get("collision");
        if (collisionLayer != null) {
            for (MapObject obj : collisionLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    BodyDef def = new BodyDef();
                    def.type = BodyDef.BodyType.StaticBody;
                    def.position.set(rect.x + rect.width/2, rect.y + rect.height/2);
                    Body body = world.createBody(def);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(rect.width/2, rect.height/2);
                    Fixture fixture = body.createFixture(shape, 0);
                    fixture.setUserData("ground");
                    shape.dispose();
                }
            }
        }

        // Слой start – позиция игрока
        MapLayer startLayer = tiledMap.getLayers().get("start");
        if (startLayer != null) {
            for (MapObject obj : startLayer.getObjects()) {
                Float x = obj.getProperties().get("x", Float.class);
                Float y = obj.getProperties().get("y", Float.class);
                if (x != null && y != null) {
                    playerBody.setTransform(x, y, 0);
                }
            }
        }

        // Функция создания предметов по слою
        createItemsFromLayer("item1", "item1");
        createItemsFromLayer("item2", "item2");
        createItemsFromLayer("jump", "jump");
        createItemsFromLayer("kill", "kill");
        createExitFromLayer("exit");
    }

    private void createItemsFromLayer(String layerName, String type) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject obj : layer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set(rect.x + rect.width/2, rect.y + rect.height/2);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2, rect.height/2);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                itemBodies.put(body, type);
            }
        }
    }

    private void createExitFromLayer(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject obj : layer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set(rect.x + rect.width/2, rect.y + rect.height/2);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2, rect.height/2);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                itemBodies.put(body, "exit");
            }
        }
    }

    private Rectangle getObjectRectangle(MapObject obj) {
        if (obj instanceof RectangleMapObject) {
            return ((RectangleMapObject) obj).getRectangle();
        } else {
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            Float width = obj.getProperties().get("width", Float.class);
            Float height = obj.getProperties().get("height", Float.class);
            if (x != null && y != null) {
                float w = width != null ? width : 32f;
                float h = height != null ? height : 32f;
                return new Rectangle(x, y, w, h);
            }
        }
        return null;
    }

    private void showGameOver() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(new StartMenuScreen(game));
            }
        }, 2);
    }

    private void showLevelComplete() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(new StartMenuScreen(game));
            }
        }, 2);
    }

    private void update(float delta) {
        if (levelComplete || gameOver) return;

        Vector2 vel = playerBody.getLinearVelocity();
        float targetVelX = 0;
        if (movingRight) targetVelX = speed;
        if (movingLeft) targetVelX = -speed;
        vel.x = targetVelX;
        playerBody.setLinearVelocity(vel);

        stateTime += delta;

        // Камера следует за игроком (координаты в пикселях)
        Vector2 pos = playerBody.getPosition();
        float px = pos.x * PPM;
        camera.position.set(px, 360, 0);
        camera.update(); // ВАЖНО: обновляем камеру

        world.step(delta, 6, 2);
    }
    private void drawPlayer() {
        TextureRegion region;
        if (movingLeft) {
            region = walkLeftAnimation.getKeyFrame(stateTime, true);
            facingRight = false;
        } else if (movingRight) {
            region = walkRightAnimation.getKeyFrame(stateTime, true);
            facingRight = true;
        } else {
            region = facingRight ? standFrame : standLeftFrame;
        }
        Vector2 pos = playerBody.getPosition();
        float w = 60f, h = 60f;
        batch.draw(region, pos.x - w/2, pos.y - h/2, w, h);
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPlayer();
        // можно нарисовать предметы (но они удаляются при сборе)
        batch.end();

        // UI счётчик и кнопки
        uiStage.act(delta);
        uiStage.draw();

        // отладка физики (закомментировать)
        // debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        uiStage.dispose();
        world.dispose();
        debugRenderer.dispose();
        tiledMap.dispose();
        tiledMapRenderer.dispose();
        for (TextureRegion tr : walkRightFrames) tr.getTexture().dispose();
        for (TextureRegion tr : walkLeftFrames) tr.getTexture().dispose();
        standFrame.getTexture().dispose();
        leftTex.dispose();
        rightTex.dispose();
        jumpTex.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}