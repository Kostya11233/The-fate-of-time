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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import java.util.HashMap;
import java.util.Map;

public class Chapter1Screen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;
    private static final float PPM = 32f;
    private static final float ZOOM = 3.0f;

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
    private String currentMap;
    private boolean isTransitioning = false;
    private String returnDoorId = null;
    private String startMap;
    private String startSpawnId;

    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingDoorId = null;
    private String pendingItemId = null;
    private Body pendingItemBody = null;
    private ImageButton upBtn, downBtn, leftBtn, rightBtn, pauseBtn, interactionBtn;
    private Texture upTex, downTex, leftTex, rightTex, pauseTex, interactionTex;

    private Label itemsLabel;
    private int collectedItems = 0;
    private int totalItems = 5;
    private Map<Body, String> itemBodies = new HashMap<>();
    private Array<Body> bodiesToDestroy = new Array<>();
    private boolean allItemsCollected = false;

    public Chapter1Screen(TheFateGame game) {
        this(game, "room3.tmx", "spawn");
    }

    public Chapter1Screen(TheFateGame game, String startMap, String startSpawnId) {
        this.game = game;
        this.startMap = startMap;
        this.startSpawnId = startSpawnId;
        this.currentMap = startMap;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280 / ZOOM, 720 / ZOOM);
        this.camera.zoom = ZOOM;
        this.uiStage = new Stage(new ExtendViewport(1280, 720));
        this.pauseStage = new Stage(new ExtendViewport(1280, 720));
        this.messageStage = new Stage(new ExtendViewport(1280, 720));

        collectedItems = game.prefs.getInteger("chapter1_items", 0);
        allItemsCollected = collectedItems >= totalItems;

        loadTextures();
        createUI();
        loadMap(currentMap);
        createWorld();
        createPlayer();
        createCollisionAndTeleports();

        Gdx.input.setInputProcessor(uiStage);
    }

    private void loadTextures() {
        try {
            Texture rightTexAnim = new Texture("player/step1.png");
            standRight = new TextureRegion(rightTexAnim);
            standLeft = new TextureRegion(rightTexAnim);
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
            walkRightFrames = walkLeftFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                walkRightFrames[i] = new TextureRegion(fallback);
                walkLeftFrames[i] = new TextureRegion(fallback);
            }
            walkRightAnimation = new Animation<TextureRegion>(0.12f, walkRightFrames);
            walkLeftAnimation = new Animation<TextureRegion>(0.12f, walkLeftFrames);
        }

        upTex = loadTextureWithFallback("button/button_up.png", 0.2f, 0.8f, 0.2f);
        downTex = loadTextureWithFallback("button/button_down.png", 0.2f, 0.8f, 0.2f);
        leftTex = loadTextureWithFallback("button/button_left.png", 0.2f, 0.4f, 0.8f);
        rightTex = loadTextureWithFallback("button/button_right.png", 0.2f, 0.4f, 0.8f);
        pauseTex = loadTextureWithFallback("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTextureWithFallback("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
    }

    private Texture loadTextureWithFallback(String path, float r, float g, float b) {
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
        upBtn.setPosition(screenW / 2 - btnSize / 2, 30 + btnSize + 20);
        upBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning) movingUp = true; return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingUp = false; }
        });

        downBtn = new ImageButton(new TextureRegionDrawable(downTex));
        downBtn.setSize(btnSize, btnSize);
        downBtn.setPosition(screenW / 2 - btnSize / 2, 30);
        downBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning) movingDown = true; return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingDown = false; }
        });

        leftBtn = new ImageButton(new TextureRegionDrawable(leftTex));
        leftBtn.setSize(btnSize, btnSize);
        leftBtn.setPosition(screenW / 2 - btnSize - 20, 30 + btnSize / 2);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning) movingLeft = true; return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingLeft = false; }
        });

        rightBtn = new ImageButton(new TextureRegionDrawable(rightTex));
        rightBtn.setSize(btnSize, btnSize);
        rightBtn.setPosition(screenW / 2 + 20, 30 + btnSize / 2);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning) movingRight = true; return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingRight = false; }
        });

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - 20, screenH - pauseSize - 20);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = true;
                createPauseDialog();
                Gdx.input.setInputProcessor(pauseStage);
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(80, 80);
        interactionBtn.setPosition(screenW / 2 - 40, screenH / 2 - 100);
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn) {
                    if (pendingDoorId != null) teleportToDoor(pendingDoorId);
                    else if (pendingItemBody != null && pendingItemId != null) collectItem(pendingItemBody, pendingItemId);
                }
            }
        });

        uiStage.addActor(upBtn);
        uiStage.addActor(downBtn);
        uiStage.addActor(leftBtn);
        uiStage.addActor(rightBtn);
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
        itemsLabel.setText(game.languageManager.format("items_collected", collectedItems));
    }

    private void collectItem(Body itemBody, String itemId) {
        if (!itemId.equals("collected")) {
            collectedItems++;
            updateItemsLabel();
            game.prefs.putInteger("chapter1_items", collectedItems);
            game.prefs.flush();
            bodiesToDestroy.add(itemBody);
            itemBodies.put(itemBody, "collected");
            pendingItemBody = null;
            pendingItemId = null;
            showInteractionBtn = false;
            interactionBtn.setVisible(false);
            System.out.println("Предмет собран! " + collectedItems + "/" + totalItems);
            if (collectedItems >= totalItems) {
                allItemsCollected = true;
                showAllItemsCollectedMessage();
            }
        }
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
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.titleFont;
        Label label = new Label("ВСЕ ПРЕДМЕТЫ СОБРАНЫ!", labelStyle);
        label.setFontScale(1.8f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() { messageStage.clear(); }
        }, 3);
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
            }
        });
        TextButton exitBtn = new TextButton("ВЫЙТИ В МЕНЮ", buttonStyle);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new StartMenuScreen(game));
            }
        });
        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(250).height(60).padBottom(20).row();
        dialog.add(exitBtn).width(250).height(60).row();
        table.add(dialog).center();
    }

    private void loadMap(String mapPath) {
        try {
            if (tiledMap != null) tiledMap.dispose();
            tiledMap = new TmxMapLoader().load(mapPath);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            currentMap = mapPath;
            System.out.println("Загружена карта: " + mapPath);
        } catch (Exception e) {
            System.out.println("Ошибка загрузки карты: " + mapPath);
        }
    }

    private void createWorld() {
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if (a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) {
                    pendingItemBody = b; pendingItemId = itemBodies.get(b); pendingDoorId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected")) {
                    pendingItemBody = a; pendingItemId = itemBodies.get(a); pendingDoorId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                if (a == playerBody && isDoor(b)) {
                    pendingDoorId = (String) b.getUserData(); pendingItemBody = null; pendingItemId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && isDoor(a)) {
                    pendingDoorId = (String) a.getUserData(); pendingItemBody = null; pendingItemId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
            }
            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a)))) {
                    showInteractionBtn = false; interactionBtn.setVisible(false);
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                }
            }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private boolean isDoor(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).startsWith("door_");
    }

    private void teleportToDoor(String doorId) {
        isTransitioning = true;
        showInteractionBtn = false;
        interactionBtn.setVisible(false);
        String targetMap = null, targetSpawnId = null;
        if (doorId.equals("door_exit")) {
            targetMap = "corid.tmx";
            targetSpawnId = returnDoorId == null ? "center" : returnDoorId;
        } else if (doorId.startsWith("door_door")) {
            if (currentMap.equals("corid.tmx")) {
                String roomNumber = doorId.substring(9);
                targetMap = "room" + roomNumber + ".tmx";
                targetSpawnId = "start_" + roomNumber;
                returnDoorId = "door" + roomNumber;
            }
        }
        if (targetMap == null || targetSpawnId == null) {
            isTransitioning = false;
            return;
        }
        final String finalMap = targetMap, finalSpawnId = targetSpawnId, finalReturnDoorId = returnDoorId;
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                loadMap(finalMap);
                recreateWorld();
                createPlayer();
                createCollisionAndTeleports();
                returnDoorId = finalReturnDoorId;
                Vector2 spawnPos = findSpawnPosition(finalSpawnId);
                if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
                isTransitioning = false;
            }
        }, 0.1f);
    }

    private Vector2 findSpawnPosition(String spawnId) {
        if (spawnId != null && spawnId.equals("center")) return new Vector2(640 / PPM, 360 / PPM);
        if (spawnId == null) return new Vector2(640 / PPM, 360 / PPM);
        for (MapLayer layer : tiledMap.getLayers()) {
            String layerName = layer.getName();
            if (layerName != null && (layerName.equals("start") || layerName.equals("spawn"))) {
                for (MapObject obj : layer.getObjects()) {
                    String name = obj.getName();
                    if (name != null && name.equals(spawnId)) {
                        if (obj instanceof RectangleMapObject) {
                            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                            return new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        } else {
                            Float x = obj.getProperties().get("x", Float.class);
                            Float y = obj.getProperties().get("y", Float.class);
                            if (x != null && y != null) return new Vector2(x / PPM, y / PPM);
                        }
                    }
                }
            }
        }
        return new Vector2(640 / PPM, 360 / PPM);
    }

    private void destroyMarkedBodies() {
        for (Body body : bodiesToDestroy) {
            if (body != null && body.getWorld() == world) world.destroyBody(body);
        }
        bodiesToDestroy.clear();
    }

    private void recreateWorld() {
        world.dispose();
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if (a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) {
                    pendingItemBody = b; pendingItemId = itemBodies.get(b); pendingDoorId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected")) {
                    pendingItemBody = a; pendingItemId = itemBodies.get(a); pendingDoorId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                if (a == playerBody && isDoor(b)) {
                    pendingDoorId = (String) b.getUserData(); pendingItemBody = null; pendingItemId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && isDoor(a)) {
                    pendingDoorId = (String) a.getUserData(); pendingItemBody = null; pendingItemId = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
            }
            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a)))) {
                    showInteractionBtn = false; interactionBtn.setVisible(false);
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                }
            }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private void createPlayer() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(640 / PPM, 360 / PPM);
        playerBody = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.setRadius(0.20f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        playerBody.setFixedRotation(true);
    }

    private void createCollisionAndTeleports() {
        MapLayer collisionLayer = tiledMap.getLayers().get("collision");
        if (collisionLayer != null) {
            for (MapObject obj : collisionLayer.getObjects()) {
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

        for (MapLayer layer : tiledMap.getLayers()) {
            String layerName = layer.getName();
            if (layerName != null && layerName.startsWith("door")) {
                for (MapObject obj : layer.getObjects()) {
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
                    body.setUserData("door_" + layerName);
                }
            }
        }

        MapLayer exitLayer = tiledMap.getLayers().get("exit");
        if (exitLayer != null) {
            for (MapObject obj : exitLayer.getObjects()) {
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
                body.setUserData("door_exit");
            }
        }

        String[] itemLayers = {"item1", "item2", "item3", "item4", "item5"};
        for (String itemLayerName : itemLayers) {
            MapLayer itemLayer = tiledMap.getLayers().get(itemLayerName);
            if (itemLayer != null) {
                for (MapObject obj : itemLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;
                    String itemId = currentMap + "_" + itemLayerName;
                    boolean alreadyCollected = game.prefs.getBoolean("item_" + itemId, false);
                    if (!alreadyCollected && collectedItems < totalItems) {
                        BodyDef def = new BodyDef();
                        def.type = BodyDef.BodyType.StaticBody;
                        def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        Body body = world.createBody(def);
                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                        Fixture fix = body.createFixture(shape, 0);
                        fix.setSensor(true);
                        shape.dispose();
                        itemBodies.put(body, itemId);
                    }
                }
            }
        }

        // ===== УСТАНОВКА ПОЗИЦИИ СПАВНА =====
        boolean spawnSet = false;

// ОСОБЫЕ КООРДИНАТЫ ДЛЯ КОМНАТЫ 4
        if (currentMap.equals("room4.tmx")) {

            float spawnX = 273f / PPM;
            float spawnY = 268f / PPM;
            playerBody.setTransform(spawnX, spawnY, 0);
            spawnSet = true;
            System.out.println("Спавн в комнате 4: " + spawnX + ", " + spawnY + " (пиксели: 273,268)");
        }


        if (!spawnSet && startSpawnId != null) {
            for (MapLayer layer : tiledMap.getLayers()) {
                String layerName = layer.getName();
                if (layerName != null && (layerName.equals("start") || layerName.equals("spawn"))) {
                    for (MapObject obj : layer.getObjects()) {
                        String name = obj.getName();
                        if (name != null && name.equals(startSpawnId)) {
                            Float x = obj.getProperties().get("x", Float.class);
                            Float y = obj.getProperties().get("y", Float.class);
                            if (x != null && y != null) {
                                playerBody.setTransform(x / PPM, y / PPM, 0);
                                spawnSet = true;
                                break;
                            }
                        }
                    }
                }
                if (spawnSet) break;
            }
        }

        // Если всё ещё не нашли - центр комнаты
        if (!spawnSet) {
            // Находим центр по collision слою
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            if (collisionLayer != null) {
                for (MapObject obj : collisionLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) {
                        minX = Math.min(minX, rect.x);
                        minY = Math.min(minY, rect.y);
                        maxX = Math.max(maxX, rect.x + rect.width);
                        maxY = Math.max(maxY, rect.y + rect.height);
                    }
                }
            }
            if (minX != Float.MAX_VALUE) {
                float centerX = (minX + maxX) / 2 / PPM;
                float centerY = (minY + maxY) / 2 / PPM;
                playerBody.setTransform(centerX, centerY, 0);
                System.out.println("Спавн в центр комнаты (по collision): " + centerX + ", " + centerY);
            } else {
                playerBody.setTransform(640 / PPM, 360 / PPM, 0);
                System.out.println("Спавн в центр (640,360)");
            }
        }
    }

    private Rectangle getObjectRectangle(MapObject obj) {
        if (obj instanceof RectangleMapObject) return ((RectangleMapObject) obj).getRectangle();
        Float x = obj.getProperties().get("x", Float.class);
        Float y = obj.getProperties().get("y", Float.class);
        Float width = obj.getProperties().get("width", Float.class);
        Float height = obj.getProperties().get("height", Float.class);
        if (x != null && y != null) {
            float w = (width != null) ? width : 100f;
            float h = (height != null) ? height : 100f;
            return new Rectangle(x, y, w, h);
        }
        return null;
    }

    private void update(float delta) {
        if (isPaused || isTransitioning) return;
        Vector2 vel = playerBody.getLinearVelocity();
        float velX = 0, velY = 0;
        if (movingRight) velX = speed;
        if (movingLeft) velX = -speed;
        if (movingUp) velY = speed;
        if (movingDown) velY = -speed;
        if (velX != 0 && velY != 0) { velX *= 0.707f; velY *= 0.707f; }
        vel.x = velX; vel.y = velY;
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
        float size = 64f; // Увеличенный размер персонажа
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
        if (isPaused) { pauseStage.act(delta); pauseStage.draw(); }
        if (allItemsCollected) { messageStage.act(delta); messageStage.draw(); }
    }

    @Override public void resize(int width, int height) {
        camera.viewportWidth = width / ZOOM;
        camera.viewportHeight = height / ZOOM;
        camera.update();
        uiStage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);
        int btnSize = 100, pauseSize = 70;
        if (upBtn != null) upBtn.setPosition(width / 2 - btnSize / 2, 30 + btnSize + 20);
        if (downBtn != null) downBtn.setPosition(width / 2 - btnSize / 2, 30);
        if (leftBtn != null) leftBtn.setPosition(width / 2 - btnSize - 20, 30 + btnSize / 2);
        if (rightBtn != null) rightBtn.setPosition(width / 2 + 20, 30 + btnSize / 2);
        if (pauseBtn != null) pauseBtn.setPosition(width - pauseSize - 20, height - pauseSize - 20);
        if (interactionBtn != null) interactionBtn.setPosition(width / 2 - 40, height / 2 - 100);
        if (itemsLabel != null) itemsLabel.setPosition(20, height - 50);
    }

    @Override public void show() { Gdx.input.setInputProcessor(isPaused ? pauseStage : uiStage); }
    @Override public void hide() {}
    @Override public void dispose() {
        uiStage.dispose(); pauseStage.dispose(); messageStage.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
}