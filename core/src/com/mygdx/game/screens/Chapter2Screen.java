package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.screens.StartMenuScreen;
import java.util.HashMap;
import java.util.Map;

public class Chapter2Screen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;
    private static final float PPM = 32f;
    private static final float ZOOM = 3.0f;
    private static final float GRAVITY = -20f;

    private Body playerBody;
    private TextureRegion[] walkRightFrames;
    private TextureRegion[] walkLeftFrames;
    private TextureRegion standRight, standLeft;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private float stateTime;
    private boolean movingUp = false, movingDown = false, movingLeft = false, movingRight = false;
    private boolean facingRight = true;
    private float speed = 5f;
    private float jumpForce = 12f;
    private boolean isGrounded = false;
    private String currentMap;
    private boolean isTransitioning = false;
    private boolean isDead = false;

    private int currentLevel = 1;
    private int collectedItems = 0;
    private int totalItems = 0;
    private boolean allItemsCollected = false;
    private boolean exitUnlocked = false; // Выход разблокирован только после сбора всех предметов

    private String[] levelMaps = {"lvl1.tmx", "lvl2.tmx", "lvl3.tmx"};
    private int[] levelItemCount = {2, 3, 2};
    private String[] levelStartIds = {"start11", "start22", "start33"};
    private String[] levelExitIds = {"exit11", "exit22", "exit33"};

    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage imageStage;
    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingDoorId = null;
    private String pendingItemId = null;
    private Body pendingItemBody = null;
    private Body pendingExitBody = null;
    private ImageButton upBtn, downBtn, leftBtn, rightBtn, jumpBtn, pauseBtn, interactionBtn;
    private Texture upTex, downTex, leftTex, rightTex, jumpTex, pauseTex, interactionTex;

    private Label itemsLabel;
    private Map<Body, String> itemBodies = new HashMap<>();
    private Array<Body> bodiesToDestroy = new Array<>();
    private Body exitBody = null;

    public Chapter2Screen(TheFateGame game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280 / ZOOM, 720 / ZOOM);
        this.camera.zoom = ZOOM;
        this.uiStage = new Stage(new ExtendViewport(1280, 720));
        this.pauseStage = new Stage(new ExtendViewport(1280, 720));
        this.messageStage = new Stage(new ExtendViewport(1280, 720));
        this.imageStage = new Stage(new ExtendViewport(1280, 720));

        loadTextures();
        createUI();
        loadLevel(1);

        Gdx.input.setInputProcessor(uiStage);
        game.stopMenuMusic();
        game.startGameMusic();
    }

    private void loadTextures() {
        try {
            Texture standTex = new Texture("player/step1.png");
            standRight = new TextureRegion(standTex);
            standLeft = new TextureRegion(standTex);
            standLeft.flip(true, false);

            walkRightFrames = new TextureRegion[4];
            walkLeftFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                Texture t = new Texture("player/step" + (i+1) + ".png");
                walkRightFrames[i] = new TextureRegion(t);
                walkLeftFrames[i] = new TextureRegion(t);
                walkLeftFrames[i].flip(true, false);
            }
            walkRightAnimation = new Animation<TextureRegion>(0.12f, walkRightFrames);
            walkLeftAnimation = new Animation<TextureRegion>(0.12f, walkLeftFrames);
        } catch (Exception e) {
            Texture fallback = createFallbackTexture(0.5f, 0.5f, 0.5f);
            standRight = standLeft = new TextureRegion(fallback);
        }

        upTex = loadTexture("button/button_up.png", 0.2f, 0.8f, 0.2f);
        downTex = loadTexture("button/button_down.png", 0.2f, 0.8f, 0.2f);
        leftTex = loadTexture("button/button_left.png", 0.2f, 0.4f, 0.8f);
        rightTex = loadTexture("button/button_right.png", 0.2f, 0.4f, 0.8f);
        jumpTex = loadTexture("button/button_jump.png", 0.2f, 0.8f, 0.2f);
        pauseTex = loadTexture("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTexture("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
    }

    private Texture loadTexture(String path, float r, float g, float b) {
        try { return new Texture(path); }
        catch (Exception e) { return createFallbackTexture(r, g, b); }
    }

    private Texture createFallbackTexture(float r, float g, float b) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void createUI() {
        int btnSize = 100;
        int pauseSize = 70;
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        upBtn = new ImageButton(new TextureRegionDrawable(upTex));
        upBtn.setSize(btnSize, btnSize);
        upBtn.setPosition(screenW / 5 - btnSize / 2, 30 + btnSize + 20);
        upBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead) movingUp = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingUp = false; }
        });

        downBtn = new ImageButton(new TextureRegionDrawable(downTex));
        downBtn.setSize(btnSize, btnSize);
        downBtn.setPosition(screenW / 5 - btnSize / 2, 30);
        downBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead) movingDown = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingDown = false; }
        });

        leftBtn = new ImageButton(new TextureRegionDrawable(leftTex));
        leftBtn.setSize(btnSize, btnSize);
        leftBtn.setPosition(screenW / 5 - btnSize - 20, 30 + btnSize / 2);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead) movingLeft = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingLeft = false; }
        });

        rightBtn = new ImageButton(new TextureRegionDrawable(rightTex));
        rightBtn.setSize(btnSize, btnSize);
        rightBtn.setPosition(screenW / 5 + 20, 30 + btnSize / 2);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead) movingRight = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingRight = false; }
        });

        jumpBtn = new ImageButton(new TextureRegionDrawable(jumpTex));
        jumpBtn.setSize(btnSize, btnSize);
        jumpBtn.setPosition(screenW - btnSize - 20, 30);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && !isDead && isGrounded) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce);
                    isGrounded = false;
                }
            }
        });

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - 20, screenH - pauseSize - 20);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = true;
                createPauseDialog();
                Gdx.input.setInputProcessor(pauseStage);
                if (game.gameMusic != null) game.gameMusic.pause();
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(80, 80);
        interactionBtn.setPosition(screenW / 2 - 40, screenH / 2 - 100);
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn) {
                    if (pendingDoorId != null && pendingDoorId.equals("exit")) {
                        // Переход на следующий уровень ТОЛЬКО при нажатии кнопки
                        goToNextLevel();
                    } else if (pendingItemBody != null && pendingItemId != null) {
                        collectItem(pendingItemBody, pendingItemId);
                    }
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                }
            }
        });

        uiStage.addActor(upBtn);
        uiStage.addActor(downBtn);
        uiStage.addActor(leftBtn);
        uiStage.addActor(rightBtn);
        uiStage.addActor(jumpBtn);
        uiStage.addActor(pauseBtn);
        uiStage.addActor(interactionBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20, screenH - 50);
        uiStage.addActor(itemsLabel);
        updateItemsLabel();
    }

    private void updateItemsLabel() {
        itemsLabel.setText("Предметы: " + collectedItems + "/" + totalItems);
    }

    private void loadLevel(int level) {
        currentLevel = level;
        currentMap = levelMaps[level - 1];
        totalItems = levelItemCount[level - 1];
        collectedItems = 0;
        allItemsCollected = false;
        exitUnlocked = false;
        itemBodies.clear();
        bodiesToDestroy.clear();
        exitBody = null;

        loadMap(currentMap);
        createWorld();
        createPlayer();
        createCollision();
        updateItemsLabel();

        showLevelMessage();
    }

    private void showLevelMessage() {
        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.8f);
        messageStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.titleFont;
        Label label = new Label("УРОВЕНЬ " + currentLevel + "\nНужно собрать: " + totalItems + " предметов", style);
        label.setFontScale(1.5f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() { messageStage.clear(); }
        }, 2);
    }

    private void loadMap(String mapPath) {
        if (tiledMap != null) tiledMap.dispose();
        tiledMap = new TmxMapLoader().load(mapPath);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    private void createWorld() {
        if (world != null) world.dispose();
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                // Земля
                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) {
                    isGrounded = true;
                }

                // Трамплин
                if ((a == playerBody && isJumpPad(b)) || (b == playerBody && isJumpPad(a))) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce * 1.5f);
                    isGrounded = false;
                }

                // Предметы - показываем кнопку
                if (a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) {
                    pendingItemBody = b;
                    pendingItemId = itemBodies.get(b);
                    pendingDoorId = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                } else if (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected")) {
                    pendingItemBody = a;
                    pendingItemId = itemBodies.get(a);
                    pendingDoorId = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }

                // Выход - показываем кнопку ТОЛЬКО если все предметы собраны
                if ((a == playerBody && isExit(b) && allItemsCollected) ||
                        (b == playerBody && isExit(a) && allItemsCollected)) {
                    pendingDoorId = "exit";
                    pendingItemBody = null;
                    pendingItemId = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }

                // Смерть
                if ((a == playerBody && isKill(b)) || (b == playerBody && isKill(a))) {
                    dieAndRestart();
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) {
                    isGrounded = false;
                }

                if ((a == playerBody && (itemBodies.containsKey(b) || isExit(b))) ||
                        (b == playerBody && (itemBodies.containsKey(a) || isExit(a)))) {
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                }
            }

            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private boolean isGround(Body body) {
        Object data = body.getUserData();
        return data != null && (data.equals("wall") || data.equals("ground"));
    }

    private boolean isJumpPad(Body body) {
        Object data = body.getUserData();
        return data != null && data.equals("jump");
    }

    private boolean isExit(Body body) {
        Object data = body.getUserData();
        return data != null && data.equals("exit");
    }

    private boolean isKill(Body body) {
        Object data = body.getUserData();
        return data != null && data.equals("kill");
    }

    private void createPlayer() {
        Vector2 spawnPos = findSpawnPosition(levelStartIds[currentLevel - 1]);
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(spawnPos);
        playerBody = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.25f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        playerBody.setFixedRotation(true);
    }

    private Vector2 findSpawnPosition(String spawnId) {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer.getName() != null && layer.getName().equals(spawnId)) {
                for (MapObject obj : layer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) {
                        return new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                    }
                }
            }
        }
        return new Vector2(640 / PPM, 360 / PPM);
    }

    private void createCollision() {
        // Стены
        MapLayer collision = tiledMap.getLayers().get("collision");
        if (collision != null) {
            for (MapObject obj : collision.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                body.createFixture(shape, 0);
                body.setUserData("wall");
                shape.dispose();
            }
        }

        // Трамплины
        MapLayer jumpLayer = tiledMap.getLayers().get("jump");
        if (jumpLayer != null) {
            for (MapObject obj : jumpLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                body.setUserData("jump");
            }
        }

        // Убийцы
        MapLayer kill = tiledMap.getLayers().get("kill");
        if (kill != null) {
            for (MapObject obj : kill.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                body.setUserData("kill");
            }
        }

        // Выход - создаем но он будет активен только после сбора всех предметов
        String exitName = levelExitIds[currentLevel - 1];
        MapLayer exitLayer = tiledMap.getLayers().get(exitName);
        if (exitLayer != null) {
            for (MapObject obj : exitLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                exitBody = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                Fixture fix = exitBody.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                exitBody.setUserData("exit");
            }
        }

        // Предметы
        String[][] itemsByLevel = {
                {"item11", "item121"},
                {"item222", "item22", "item2222"},
                {"item33", "item333"}
        };

        for (String itemName : itemsByLevel[currentLevel - 1]) {
            MapLayer itemLayer = tiledMap.getLayers().get(itemName);
            if (itemLayer != null) {
                for (MapObject obj : itemLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;
                    BodyDef def = new BodyDef();
                    def.type = BodyDef.BodyType.StaticBody;
                    def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                    Body body = world.createBody(def);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                    Fixture fix = body.createFixture(shape, 0);
                    fix.setSensor(true);
                    shape.dispose();
                    itemBodies.put(body, itemName);
                }
            }
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

    private void collectItem(Body itemBody, String itemId) {
        if (itemBodies.get(itemBody).equals("collected")) return;

        collectedItems++;
        updateItemsLabel();
        bodiesToDestroy.add(itemBody);
        itemBodies.put(itemBody, "collected");

        Gdx.app.log("Chapter2", "Item collected: " + collectedItems + "/" + totalItems);

        // Только когда собрали ВСЕ предметы - разблокируем выход
        if (collectedItems >= totalItems && !allItemsCollected) {
            allItemsCollected = true;
            showAllItemsCollectedMessage();
        }

        showInteractionBtn = false;
        interactionBtn.setVisible(false);
        pendingItemBody = null;
        pendingItemId = null;
    }

    private void showAllItemsCollectedMessage() {
        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.8f);
        messageStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.titleFont;
        Label label = new Label("ВСЕ ПРЕДМЕТЫ СОБРАНЫ!\nИдите к выходу", style);
        label.setFontScale(1.5f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                messageStage.clear();
            }
        }, 2);
    }

    private void goToNextLevel() {
        Gdx.app.log("Chapter2", "Going to next level from " + currentLevel);

        if (currentLevel < 3) {
            currentLevel++;
            loadLevel(currentLevel);
        } else {
            // Завершение игры после 3 уровня
            completeGame();
        }
    }

    private void completeGame() {
        isTransitioning = true;
        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        messageStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.titleFont;
        Label label = new Label("ПРОДОЛЖЕНИЕ СЛЕДУЕТ...", style);
        label.setFontScale(2f);
        table.add(label).center();
        game.stopGameMusic();
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                game.setScreen(new StartMenuScreen(game));
            }
        }, 3);
    }

    private void dieAndRestart() {
        if (isDead) return;
        isDead = true;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                loadLevel(currentLevel);
                isDead = false;
            }
        }, 0.5f);
    }

    private void destroyMarkedBodies() {
        for (Body body : bodiesToDestroy) {
            if (body != null && body.getWorld() == world) {
                world.destroyBody(body);
            }
        }
        bodiesToDestroy.clear();
    }

    private void createPauseDialog() {
        pauseStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        pauseStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Table dialog = new Table();
        dialog.pad(30);
        Label title = new Label("ПАУЗА", labelStyle);
        title.setFontScale(2f);
        TextButton continueBtn = new TextButton("ПРОДОЛЖИТЬ", buttonStyle);
        continueBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(uiStage);
                if (game.gameMusic != null && game.musicEnabled) game.gameMusic.play();
            }
        });
        TextButton exitBtn = new TextButton("В МЕНЮ", buttonStyle);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new StartMenuScreen(game));
            }
        });
        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(200).height(50).padBottom(20).row();
        dialog.add(exitBtn).width(200).height(50).row();
        table.add(dialog).center();
    }

    private void update(float delta) {
        if (isPaused || isTransitioning || isDead) return;

        Vector2 vel = playerBody.getLinearVelocity();
        float velX = 0;
        if (movingRight) velX = speed;
        if (movingLeft) velX = -speed;
        vel.x = velX;
        playerBody.setLinearVelocity(vel);

        if (movingLeft || movingRight) stateTime += delta;
        else stateTime = 0;
        if (movingRight) facingRight = true;
        if (movingLeft) facingRight = false;

        world.step(delta, 6, 2);
        destroyMarkedBodies();

        Vector2 pos = playerBody.getPosition();
        camera.position.set(pos.x * PPM, pos.y * PPM, 0);
        camera.update();
    }

    private void drawPlayer() {
        TextureRegion region;
        if (movingLeft) region = walkLeftAnimation.getKeyFrame(stateTime, true);
        else if (movingRight) region = walkRightAnimation.getKeyFrame(stateTime, true);
        else region = facingRight ? standRight : standLeft;
        Vector2 pos = playerBody.getPosition();
        float size = 64f;
        batch.draw(region, pos.x * PPM - size/2, pos.y * PPM - size/2, size, size);
    }

    @Override public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPlayer();
        batch.end();

        uiStage.act(delta);
        uiStage.draw();
        if (isPaused) pauseStage.draw();
        messageStage.act(delta);
        messageStage.draw();
    }

    @Override public void resize(int width, int height) {
        camera.viewportWidth = width / ZOOM;
        camera.viewportHeight = height / ZOOM;
        camera.update();
        uiStage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);

        int btnSize = 100, pauseSize = 70;
        float screenW = width, screenH = height;
        if (upBtn != null) upBtn.setPosition(screenW / 5 - btnSize / 2, 30 + btnSize + 20);
        if (downBtn != null) downBtn.setPosition(screenW / 5 - btnSize / 2, 30);
        if (leftBtn != null) leftBtn.setPosition(screenW / 5 - btnSize - 20, 30 + btnSize / 2);
        if (rightBtn != null) rightBtn.setPosition(screenW / 5 + 20, 30 + btnSize / 2);
        if (jumpBtn != null) jumpBtn.setPosition(width - btnSize - 20, 30);
        if (pauseBtn != null) pauseBtn.setPosition(width - pauseSize - 20, height - pauseSize - 20);
        if (interactionBtn != null) interactionBtn.setPosition(width / 2 - 40, height / 2 - 100);
        if (itemsLabel != null) itemsLabel.setPosition(20, height - 50);
    }

    @Override public void show() { Gdx.input.setInputProcessor(uiStage); game.startGameMusic(); }
    @Override public void hide() { game.stopGameMusic(); }
    @Override public void dispose() {
        uiStage.dispose(); pauseStage.dispose(); messageStage.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
}