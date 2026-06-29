package com.thefateoftime.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thefateoftime.TheFateGame;
import com.thefateoftime.game.CollectibleObject;

import java.util.Comparator;

public class Chapter1Screen extends ScreenAdapter {

    // ============================================================
    // КОНСТАНТЫ
    // ============================================================

    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;

    // РАЗМЕРЫ КНОПОК (увеличены в 2 раза)
    private static final float BUTTON_SIZE = 160;
    private static final float INTERACTION_SIZE = 200;
    private static final float MARGIN = 30;
    private static final float SPACING = 30;

    // ДЖОЙСТИК (увеличен в 3 раза)
    private static final float JOYSTICK_BASE_RADIUS = 210;
    private static final float JOYSTICK_KNOB_RADIUS = 105;

    private static final float PPM = 32f;
    private static final float CAMERA_WIDTH = 600;
    private static final float CAMERA_HEIGHT = 337;
    private static final float GRAVITY = 0f;
    private static final float SAVE_INTERVAL = 5f;

    // ============================================================
    // ОСНОВНЫЕ КОМПОНЕНТЫ
    // ============================================================

    private final TheFateGame fateGame;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage imageStage;
    private Stage bookStage;
    private FitViewport uiViewport;

    // ============================================================
    // КАРТА И ФИЗИКА
    // ============================================================

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;

    private Body playerBody;
    private TextureRegion standRight, standLeft;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private float stateTime;
    private boolean facingRight = true;
    private String currentMap;
    private boolean isTransitioning = false;
    private String returnDoorId = null;
    private String startSpawnId = null;

    // ============================================================
    // UI КНОПКИ
    // ============================================================

    private ImageButton pauseBtn;
    private ImageButton interactionBtn;
    private ImageButton bookBtn;

    private Texture pauseTex;
    private Texture interactionTex;
    private Texture bookTex;
    private Texture defaultItemTexture;

    // ============================================================
    // ДЖОЙСТИК
    // ============================================================

    private Texture joystickBaseTexture;
    private Texture joystickKnobTexture;
    private Vector2 joystickBasePos;
    private Vector2 joystickKnobPos;
    private float joystickBaseRadius = JOYSTICK_BASE_RADIUS;
    private float joystickKnobRadius = JOYSTICK_KNOB_RADIUS;
    private boolean joystickActive = false;
    private int joystickPointer = -1;
    private Vector2 joystickDirection = new Vector2(0, 0);

    // ============================================================
    // СОСТОЯНИЕ ИГРЫ
    // ============================================================

    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingDoorId = null;
    private CollectibleObject pendingCollectible = null;
    private Body pendingMaBody = null;

    private Label itemsLabel;
    private int collectedItems = 0;
    private int totalItems = 5;
    private Array<CollectibleObject> collectibleObjects = new Array<>();
    private boolean allItemsCollected = false;

    // ============================================================
    // ЗАМЕТКИ
    // ============================================================

    private Texture currentImageTexture = null;
    private Texture currentNoteTexture = null;
    private boolean showingImage = false;
    private Array<String> collectedNotes = new Array<>();
    private int currentNoteIndex = 0;
    private boolean showingBook = false;

    private float saveTimer = 0f;


    private float targetMusicVolume = 1f;
    private float currentMusicVolume = 1f;
    private float volumeLerpSpeed = 2f;

    // ============================================================
    // КОНСТРУКТОРЫ
    // ============================================================

    public Chapter1Screen(TheFateGame fateGame) {
        this(fateGame, "room3.tmx", "spawn");
    }

    public Chapter1Screen(TheFateGame fateGame, String startMap, String startSpawnId) {
        this.fateGame = fateGame;
        this.currentMap = startMap;
        this.startSpawnId = startSpawnId;
        this.batch = fateGame.batch;
        this.camera = new OrthographicCamera();

        camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);

        uiViewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        uiStage = new Stage(uiViewport);
        pauseStage = new Stage(uiViewport);
        messageStage = new Stage(uiViewport);
        imageStage = new Stage(uiViewport);
        bookStage = new Stage(uiViewport);

        loadCollectedNotes();
        collectedItems = fateGame.prefs.getInteger("chapter1_items", 0);
        allItemsCollected = collectedItems >= totalItems;

        loadTextures();
        createUI();
        loadMap(currentMap);
        createWorld();
        createPlayer();
        createCollisionAndTeleports();

        Gdx.input.setInputProcessor(uiStage);
        fateGame.stopMenuMusic();
        fateGame.startGameMusic();
    }

    // ============================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
        }

        // Рендер игровых объектов (ПРАВИЛЬНЫЙ ПОРЯДОК)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Сначала рисуем коллекционируемые объекты
        drawCollectibles();

        // 2. Потом рисуем игрока (ПОВЕРХ)
        drawPlayer();

        batch.end();

        // Рендер UI
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        drawJoystick();
        batch.end();

        uiStage.act(delta);
        uiStage.draw();

        if (isPaused) {
            pauseStage.act(delta);
            pauseStage.draw();
        }
        messageStage.act(delta);
        messageStage.draw();
        if (showingImage) {
            imageStage.act(delta);
            imageStage.draw();
        }
        if (showingBook) {
            bookStage.act(delta);
            bookStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);
        imageStage.getViewport().update(width, height, true);
        bookStage.getViewport().update(width, height, true);

        camera.viewportWidth = CAMERA_WIDTH;
        camera.viewportHeight = CAMERA_HEIGHT;
        camera.update();

        createUI();
    }

    @Override
    public void hide() {
        fateGame.stopGameMusic();
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        pauseStage.dispose();
        messageStage.dispose();
        if (imageStage != null) imageStage.dispose();
        if (bookStage != null) bookStage.dispose();
        if (currentImageTexture != null) currentImageTexture.dispose();
        if (currentNoteTexture != null) currentNoteTexture.dispose();
        if (joystickBaseTexture != null) joystickBaseTexture.dispose();
        if (joystickKnobTexture != null) joystickKnobTexture.dispose();
        if (defaultItemTexture != null) defaultItemTexture.dispose();

        for (CollectibleObject obj : collectibleObjects) {
            obj.dispose();
        }
        collectibleObjects.clear();

        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    // ============================================================
    // UI СОЗДАНИЕ
    // ============================================================

    private void createUI() {
        uiStage.clear();

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(BUTTON_SIZE, BUTTON_SIZE);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!showingImage && !showingBook) {
                    isPaused = true;
                    createPauseDialog();
                    Gdx.input.setInputProcessor(pauseStage);
                    if (fateGame.gameMusic != null) fateGame.gameMusic.pause();
                }
            }
        });

        bookBtn = new ImageButton(new TextureRegionDrawable(bookTex));
        bookBtn.setSize(BUTTON_SIZE, BUTTON_SIZE);
        bookBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!showingImage && !showingBook && !isPaused && !isTransitioning) {
                    showBook();
                }
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(INTERACTION_SIZE, INTERACTION_SIZE);
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn && !showingImage && !showingBook) {
                    if (pendingDoorId != null) {
                        teleportToDoor(pendingDoorId);
                    } else if (pendingCollectible != null) {
                        handleCollectibleInteraction();
                    } else if (pendingMaBody != null && allItemsCollected) {
                        showToBeContinuedAndExit();
                        showInteractionBtn = false;
                        interactionBtn.setVisible(false);
                        pendingMaBody = null;
                    }
                }
            }
        });

        // === РАССТАНОВКА КНОПОК ===
        float topY = VIRTUAL_HEIGHT - BUTTON_SIZE - MARGIN;
        float rightX = VIRTUAL_WIDTH - BUTTON_SIZE - MARGIN;

        // Пауза - правый верхний угол
        pauseBtn.setPosition(rightX, topY);

        // Книга - левее паузы
        bookBtn.setPosition(rightX - BUTTON_SIZE - SPACING, topY);

        // Кнопка взаимодействия - правый нижний угол
        interactionBtn.setPosition(
                VIRTUAL_WIDTH - INTERACTION_SIZE - 90,
                60
        );

        // === ЛЕЙБЛ СЧЕТЧИКА ===
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = fateGame.smallFont;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setFontScale(3.0f);
        itemsLabel.setPosition(MARGIN, VIRTUAL_HEIGHT - 80);
        updateItemsLabel();

        uiStage.addActor(pauseBtn);
        uiStage.addActor(bookBtn);
        uiStage.addActor(interactionBtn);
        uiStage.addActor(itemsLabel);

        joystickBasePos = new Vector2(
                MARGIN + JOYSTICK_BASE_RADIUS,
                MARGIN + JOYSTICK_BASE_RADIUS
        );
        joystickKnobPos = new Vector2(joystickBasePos.x, joystickBasePos.y);
    }

    // ============================================================
    // ОБНОВЛЕНИЕ
    // ============================================================

    private void update(float delta) {
        if (isPaused || isTransitioning || showingImage || showingBook) return;

        updateJoystick();
        updateMusicVolume(delta);

        float speed = 5f;
        float velX = joystickDirection.x * speed;
        float velY = joystickDirection.y * speed;
        playerBody.setLinearVelocity(velX, velY);

        if (Math.abs(velX) > 0.1f || Math.abs(velY) > 0.1f) {
            stateTime += delta;
            facingRight = velX > 0;
        } else {
            stateTime = 0;
        }

        world.step(delta, 6, 2);

        Vector2 pos = playerBody.getPosition();
        float targetX = pos.x * PPM;
        float targetY = pos.y * PPM;

        float smoothness = 0.15f;
        camera.position.x += (targetX - camera.position.x) * smoothness;
        camera.position.y += (targetY - camera.position.y) * smoothness;

        camera.update();

        saveTimer += delta;
        if (saveTimer >= SAVE_INTERVAL) {
            saveTimer = 0f;
            saveProgress();
        }
    }

    // ============================================================
    // ДЖОЙСТИК
    // ============================================================

    private void updateJoystick() {
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float touchX = Gdx.input.getX(i);
                float touchY = Gdx.input.getY(i);

                Vector3 stageCoords = uiStage.getViewport().unproject(new Vector3(touchX, touchY, 0));
                float x = stageCoords.x;
                float y = stageCoords.y;

                if (!joystickActive && !isPaused && !isTransitioning && !showingImage && !showingBook) {
                    float dx = x - joystickBasePos.x;
                    float dy = y - joystickBasePos.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist <= joystickBaseRadius + 20) {
                        joystickActive = true;
                        joystickPointer = i;
                    }
                }

                if (joystickActive && joystickPointer == i) {
                    float dx = x - joystickBasePos.x;
                    float dy = y - joystickBasePos.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist > joystickBaseRadius) {
                        dx = dx / dist * joystickBaseRadius;
                        dy = dy / dist * joystickBaseRadius;
                        dist = joystickBaseRadius;
                    }

                    joystickKnobPos.set(joystickBasePos.x + dx, joystickBasePos.y + dy);

                    if (dist > 5) {
                        joystickDirection.set(dx / joystickBaseRadius, dy / joystickBaseRadius);
                    } else {
                        joystickDirection.set(0, 0);
                    }
                    return;
                }
            }
        }

        if (joystickActive) {
            joystickActive = false;
            joystickPointer = -1;
            joystickDirection.set(0, 0);
            joystickKnobPos.set(joystickBasePos.x, joystickBasePos.y);
        }
    }

    private void drawJoystick() {
        if (joystickBaseTexture != null) {
            batch.draw(joystickBaseTexture,
                    joystickBasePos.x - joystickBaseRadius,
                    joystickBasePos.y - joystickBaseRadius,
                    joystickBaseRadius * 2, joystickBaseRadius * 2);
        }
        if (joystickKnobTexture != null) {
            batch.draw(joystickKnobTexture,
                    joystickKnobPos.x - joystickKnobRadius,
                    joystickKnobPos.y - joystickKnobRadius,
                    joystickKnobRadius * 2, joystickKnobRadius * 2);
        }
    }

    // ============================================================
    // ЗАГРУЗКА ТЕКСТУР
    // ============================================================

    private void loadTextures() {
        try {
            Texture standTex = new Texture("player/step1.png");
            standRight = new TextureRegion(standTex);
            standLeft = new TextureRegion(standTex);
            standLeft.flip(true, false);

            TextureRegion[] walkRightFrames = new TextureRegion[4];
            TextureRegion[] walkLeftFrames = new TextureRegion[4];
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

        pauseTex = loadTextureWithFallback("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTextureWithFallback("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
        bookTex = loadTextureWithFallback("item/book3.png", 0.6f, 0.4f, 0.2f);
        defaultItemTexture = loadTextureWithFallback("item/tool.png", 0.8f, 0.6f, 0.2f);

        joystickBaseTexture = createJoystickBaseTexture();
        joystickKnobTexture = createJoystickKnobTexture();
    }

    private Texture createJoystickBaseTexture() {
        int size = (int)(joystickBaseRadius * 2);
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.4f, 0.6f);
        pixmap.fillCircle((int)joystickBaseRadius, (int)joystickBaseRadius, (int)joystickBaseRadius);
        pixmap.setColor(0.5f, 0.5f, 0.6f, 0.8f);
        pixmap.drawCircle((int)joystickBaseRadius, (int)joystickBaseRadius, (int)(joystickBaseRadius - 2));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createJoystickKnobTexture() {
        int size = (int)(joystickKnobRadius * 2);
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.8f, 0.8f, 0.9f, 0.9f);
        pixmap.fillCircle((int)joystickKnobRadius, (int)joystickKnobRadius, (int)joystickKnobRadius);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.drawCircle((int)joystickKnobRadius, (int)joystickKnobRadius, (int)(joystickKnobRadius - 3));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture loadTextureWithFallback(String path, float r, float g, float b) {
        try {
            return new Texture(path);
        } catch (Exception e) {
            return createFallbackTexture(r, g, b);
        }
    }

    private Texture createFallbackTexture(float r, float g, float b) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    // ============================================================
    // ОТРИСОВКА ИГРОВЫХ ОБЪЕКТОВ
    // ============================================================

    private void drawPlayer() {
        TextureRegion region;
        if (Math.abs(joystickDirection.x) > 0.1f || Math.abs(joystickDirection.y) > 0.1f) {
            if (joystickDirection.x < 0) region = walkLeftAnimation.getKeyFrame(stateTime, true);
            else region = walkRightAnimation.getKeyFrame(stateTime, true);
        } else {
            region = facingRight ? standRight : standLeft;
        }
        Vector2 pos = playerBody.getPosition();
        float size = 64f;
        batch.draw(region, pos.x * PPM - size/2, pos.y * PPM - size/2, size, size);
    }

    private void drawCollectibles() {
        for (CollectibleObject obj : collectibleObjects) {
            obj.render(batch);
        }
    }

    // ============================================================
    // КАРТА И ФИЗИКА
    // ============================================================

    private void loadMap(String mapPath) {
        try {
            if (tiledMap != null) tiledMap.dispose();
            tiledMap = new TmxMapLoader().load(mapPath);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            currentMap = mapPath;
        } catch (Exception e) {
            Gdx.app.error("Chapter1Screen", "Failed to load map: " + mapPath, e);
        }
    }

    private void createWorld() {
        if (world != null) world.dispose();
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(createContactListener());
    }

    private void recreateWorld() {
        for (CollectibleObject obj : collectibleObjects) {
            obj.dispose();
        }
        collectibleObjects.clear();

        if (world != null) {
            world.dispose();
        }
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(createContactListener());
    }

    private ContactListener createContactListener() {
        return new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if ((a == playerBody && b.getUserData() instanceof CollectibleObject) ||
                        (b == playerBody && a.getUserData() instanceof CollectibleObject)) {
                    CollectibleObject obj = (a == playerBody) ?
                            (CollectibleObject) b.getUserData() : (CollectibleObject) a.getUserData();

                    if (!obj.isCollected()) {
                        pendingCollectible = obj;
                        pendingDoorId = null;
                        pendingMaBody = null;
                        showInteractionBtn = true;
                        interactionBtn.setVisible(true);
                    }
                }
                else if ((a == playerBody && isDoor(b)) || (b == playerBody && isDoor(a))) {
                    pendingDoorId = (String) ((a == playerBody) ? b.getUserData() : a.getUserData());
                    pendingCollectible = null;
                    pendingMaBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && isMaExit(b)) || (b == playerBody && isMaExit(a))) {
                    if (allItemsCollected) {
                        pendingDoorId = null;
                        pendingCollectible = null;
                        pendingMaBody = (a == playerBody) ? b : a;
                        showInteractionBtn = true;
                        interactionBtn.setVisible(true);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || b.getUserData() instanceof CollectibleObject || isMaExit(b))) ||
                        (b == playerBody && (isDoor(a) || a.getUserData() instanceof CollectibleObject || isMaExit(a)))) {
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                    pendingDoorId = null;
                    pendingCollectible = null;
                    pendingMaBody = null;
                }
            }

            @Override
            public void preSolve(Contact c, Manifold m) {}

            @Override
            public void postSolve(Contact c, ContactImpulse i) {}
        };
    }

    private boolean isDoor(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).startsWith("door_");
    }

    private boolean isMaExit(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).equals("ma_exit");
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

        MapLayer maLayer = tiledMap.getLayers().get("ma");
        if (maLayer != null) {
            for (MapObject obj : maLayer.getObjects()) {
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
                body.setUserData("ma_exit");
            }
        }

        createCollectiblesForMap();

        Vector2 spawnPos = findSpawnPosition(startSpawnId);
        if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
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

    private void teleportToDoor(String doorId) {
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
            return;
        }

        Gdx.input.setInputProcessor(null);
        loadMap(targetMap);
        recreateWorld();
        createPlayer();
        createCollisionAndTeleports();
        Vector2 spawnPos = findSpawnPosition(targetSpawnId);
        if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
        saveProgress();
        Gdx.input.setInputProcessor(uiStage);
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

    // ============================================================
    // COLLECTIBLES
    // ============================================================

    private void createCollectiblesForMap() {
        for (CollectibleObject obj : collectibleObjects) {
            obj.dispose();
        }
        collectibleObjects.clear();

        float noteW = 24f;
        float noteH = 24f;

        if (currentMap.equals("room1.tmx")) {
            addNoteIfNotCollected("z1", 750.9f, 700.26f, noteW, noteH);
            addItemIfNotCollected("room1_item1", 1257.9f, 367.62f, 16, 16);
        } else if (currentMap.equals("room2.tmx")) {
            addNoteIfNotCollected("z2", 750.02f, 300.69f, noteW, noteH);
        } else if (currentMap.equals("room3.tmx")) {
            addNoteIfNotCollected("z3", 800.9f, 400.53f, noteW, noteH);
            addItemIfNotCollected("room3_item2", 900.0f, 300.33f, 16, 16);
        } else if (currentMap.equals("room4.tmx")) {
            addNoteIfNotCollected("z4", 285.24f, 400.77f, noteW, noteH);
        } else if (currentMap.equals("room5.tmx")) {
            addNoteIfNotCollected("z5", 382.39f, 541.84f, noteW, noteH);
            addItemIfNotCollected("room5_item3", 1259.1f, 285.05f, 16, 16);
        } else if (currentMap.equals("room6.tmx")) {
            addNoteIfNotCollected("z6", 650.21f, 390.30f, noteW, noteH);
        } else if (currentMap.equals("room7.tmx")) {
            addNoteIfNotCollected("z7", 928.66f, 294.20f, noteW, noteH);
            addItemIfNotCollected("room7_item4", 800.0f, 250.54f, 16, 16);
        } else if (currentMap.equals("room8.tmx")) {
            addNoteIfNotCollected("z8", 751.38f, 475.40f, noteW, noteH);
        } else if (currentMap.equals("room9.tmx")) {
            addNoteIfNotCollected("z9", 698.23f, 674.72f, noteW, noteH);
            addItemIfNotCollected("room9_item5", 700.8f, 216.06f, 16, 16);
        } else if (currentMap.equals("room10.tmx")) {
            addNoteIfNotCollected("z10", 605.33f, 441.16f, noteW, noteH);
        }
    }

    private void addNoteIfNotCollected(String noteId, float x, float y, float w, float h) {
        int noteNumber = Integer.parseInt(noteId.substring(1));
        boolean alreadyRead = fateGame.prefs.getBoolean("note_z" + noteNumber, false);

        if (!alreadyRead) {
            Texture noteTexture = loadNoteTexture(noteId);
            CollectibleObject note = new CollectibleObject(
                    CollectibleObject.Type.NOTE,
                    noteId,
                    noteTexture,
                    world,
                    x, y, w, h
            );
            collectibleObjects.add(note);
        }
    }

    private void addItemIfNotCollected(String itemId, float x, float y, float w, float h) {
        boolean alreadyCollected = fateGame.prefs.getBoolean("item_" + itemId, false);

        if (!alreadyCollected && collectedItems < totalItems) {
            CollectibleObject item = new CollectibleObject(
                    CollectibleObject.Type.ITEM,
                    itemId,
                    defaultItemTexture,
                    world,
                    x, y, w, h
            );
            collectibleObjects.add(item);
        }
    }

    private void handleCollectibleInteraction() {
        if (pendingCollectible.getType() == CollectibleObject.Type.ITEM) {
            collectItem(pendingCollectible);
        } else if (pendingCollectible.getType() == CollectibleObject.Type.NOTE) {
            collectNote(pendingCollectible);
        }
        pendingCollectible = null;
        showInteractionBtn = false;
        interactionBtn.setVisible(false);
    }

    private void collectItem(CollectibleObject item) {
        String itemId = item.getId();
        fateGame.prefs.putBoolean("item_" + itemId, true);
        fateGame.prefs.flush();
        item.collect();
        collectedItems++;
        updateItemsLabel();
        showMessage(fateGame.languageManager.getText("item_collected"), 1.5f);
        if (collectedItems >= totalItems) {
            allItemsCollected = true;
            showAllItemsCollectedMessage();
            fateGame.prefs.putBoolean("chapter2_unlocked", true);
            fateGame.prefs.flush();
        }
        saveProgress();
    }

    // ИЗМЕНЕННЫЙ МЕТОД - добавлено сообщение "Собрано"
    private void collectNote(CollectibleObject note) {
        String noteId = note.getId();
        fateGame.prefs.putBoolean("note_z" + Integer.parseInt(noteId.substring(1)), true);
        fateGame.prefs.flush();
        note.collect();
        saveNote(noteId);
        targetMusicVolume = 0.4f;
        showNoteImage(noteId);

        // Показываем сообщение "Собрано" на текущем языке
        showMessage(fateGame.languageManager.getText("note_collected"), 1.5f);
    }

    // ============================================================
    // ЗАМЕТКИ
    // ============================================================

    private void loadCollectedNotes() {
        collectedNotes.clear();
        for (int i = 1; i <= 10; i++) {
            if (fateGame.prefs.getBoolean("note_z" + i, false)) {
                collectedNotes.add("z" + i);
            }
        }
        sortNotes();
    }

    private void saveNote(String noteId) {
        if (!collectedNotes.contains(noteId, false)) {
            collectedNotes.add(noteId);
            sortNotes();
            fateGame.prefs.putBoolean("note_z" + Integer.parseInt(noteId.substring(1)), true);
            fateGame.prefs.flush();
        }
    }

    private void sortNotes() {
        // Сортируем по числовому значению (z1, z2, z3, ..., z10)
        collectedNotes.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                int numA = Integer.parseInt(a.substring(1));
                int numB = Integer.parseInt(b.substring(1));
                return Integer.compare(numA, numB);
            }
        });
    }

    private String getNoteImagePath(String noteId) {
        String currentLang = fateGame.languageManager.getCurrentLanguage();
        String imagePath = "notes/" + currentLang + "/" + noteId + ".png";

        if (!Gdx.files.internal(imagePath).exists()) {
            imagePath = "notes/ru/" + noteId + ".png";
        }

        return imagePath;
    }

    private Texture loadNoteTexture(String noteId) {
        String imagePath = getNoteImagePath(noteId);
        try {
            if (Gdx.files.internal(imagePath).exists()) {
                return new Texture(imagePath);
            }
        } catch (Exception e) {
            Gdx.app.error("Chapter1Screen", "Failed to load note texture: " + imagePath, e);
        }
        return createFallbackTexture(0.3f, 0.3f, 1f);
    }

    private void showNoteImage(String noteId) {
        String imagePath = getNoteImagePath(noteId);

        if (!Gdx.files.internal(imagePath).exists()) {
            showNoteText(noteId);
            return;
        }

        try {
            if (currentImageTexture != null) {
                currentImageTexture.dispose();
            }
            currentImageTexture = new Texture(imagePath);
            showingImage = true;

            imageStage.clear();
            Table darkBg = new Table();
            darkBg.setFillParent(true);
            darkBg.setColor(0, 0, 0, 0.9f);
            imageStage.addActor(darkBg);

            Table table = new Table();
            table.setFillParent(true);
            imageStage.addActor(table);

            int originalWidth = currentImageTexture.getWidth();
            int originalHeight = currentImageTexture.getHeight();

            float maxWidth = VIRTUAL_WIDTH * 0.85f;
            float maxHeight = VIRTUAL_HEIGHT * 0.8f;

            float scaleX = maxWidth / originalWidth;
            float scaleY = maxHeight / originalHeight;
            float scale = Math.min(scaleX, scaleY);

            float displayWidth = originalWidth * scale;
            float displayHeight = originalHeight * scale;

            Image displayedImage = new Image(currentImageTexture);
            displayedImage.setSize(displayWidth, displayHeight);

            Table imageContainer = new Table();
            imageContainer.add(displayedImage).center();
            table.add(imageContainer).center().padBottom(20);

            Label continueLabel = new Label(fateGame.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
                font = fateGame.font;
                fontColor = com.badlogic.gdx.graphics.Color.WHITE;
            }});
            continueLabel.setFontScale(1.2f);
            table.add(continueLabel).padTop(20).center();

            imageStage.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    hideCurrentImage();
                    return true;
                }
            });

            Gdx.input.setInputProcessor(imageStage);

        } catch (Exception e) {
            hideCurrentImage();
            showNoteText(noteId);
        }
    }

    private void showNoteText(String noteId) {
        String noteText = fateGame.languageManager.getText("note_" + noteId);

        imageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        imageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        imageStage.addActor(table);

        Table textContainer = new Table();
        textContainer.pad(30);

        int noteNumber = Integer.parseInt(noteId.substring(1));
        Label titleLabel = new Label(fateGame.languageManager.getText("note_prefix") + noteNumber, new Label.LabelStyle() {{
            font = fateGame.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        textContainer.add(titleLabel).padBottom(20).row();

        Label textLabel = new Label(noteText, new Label.LabelStyle() {{
            font = fateGame.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        textLabel.setWrap(true);
        textContainer.add(textLabel).width(VIRTUAL_WIDTH * 0.7f).padBottom(30).row();

        Label continueLabel = new Label(fateGame.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
            font = fateGame.font;
            fontColor = com.badlogic.gdx.graphics.Color.CYAN;
        }});
        textContainer.add(continueLabel);

        table.add(textContainer).center();

        imageStage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                hideCurrentImage();
                return true;
            }
        });

        Gdx.input.setInputProcessor(imageStage);
    }

    private void hideCurrentImage() {
        showingImage = false;
        imageStage.clear();
        if (currentImageTexture != null) {
            currentImageTexture.dispose();
            currentImageTexture = null;
        }
        Gdx.input.setInputProcessor(uiStage);
        targetMusicVolume = 1f;
    }

    private void showBook() {
        if (collectedNotes.size == 0) {
            showMessage(fateGame.languageManager.getText("no_notes"), 1.5f);
            return;
        }
        showingBook = true;
        targetMusicVolume = 0.4f;
        currentNoteIndex = 0;
        showCurrentNote();
        Gdx.input.setInputProcessor(bookStage);
    }

    private ScrollPane createTextScrollPane(String text) {
        Table textTable = new Table();
        textTable.pad(20);

        Label textLabel = new Label(text, new Label.LabelStyle() {{
            font = fateGame.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        textLabel.setWrap(true);
        textTable.add(textLabel).width(VIRTUAL_WIDTH * 0.65f);

        ScrollPane scrollPane = new ScrollPane(textTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    private void showCurrentNote() {
        bookStage.clear();
        targetMusicVolume = 1f;

        String noteId = collectedNotes.get(currentNoteIndex);
        int noteNumber = Integer.parseInt(noteId.substring(1));

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.95f);
        bookStage.addActor(darkBg);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        bookStage.addActor(mainTable);

        Table contentTable = new Table();
        contentTable.center();

        Label titleLabel = new Label(fateGame.languageManager.getText("note_prefix") + noteNumber, new Label.LabelStyle() {{
            font = fateGame.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).padBottom(25).row();

        String imagePath = getNoteImagePath(noteId);
        boolean imageLoaded = false;

        if (Gdx.files.internal(imagePath).exists()) {
            try {
                if (currentNoteTexture != null) {
                    currentNoteTexture.dispose();
                }
                currentNoteTexture = new Texture(imagePath);
                int originalWidth = currentNoteTexture.getWidth();
                int originalHeight = currentNoteTexture.getHeight();

                float maxWidth = VIRTUAL_WIDTH * 0.75f;
                float maxHeight = VIRTUAL_HEIGHT * 0.55f;

                float scaleX = maxWidth / originalWidth;
                float scaleY = maxHeight / originalHeight;
                float scale = Math.min(scaleX, scaleY);

                float displayWidth = originalWidth * scale;
                float displayHeight = originalHeight * scale;

                Image noteImage = new Image(currentNoteTexture);
                noteImage.setSize(displayWidth, displayHeight);

                Table imageContainer = new Table();
                imageContainer.add(noteImage).center();
                contentTable.add(imageContainer).center().padBottom(30);
                imageLoaded = true;
            } catch (Exception e) {
                imageLoaded = false;
            }
        }

        if (!imageLoaded) {
            String noteText = fateGame.languageManager.getText("note_" + noteId);
            ScrollPane scrollPane = createTextScrollPane(noteText);
            contentTable.add(scrollPane).width(VIRTUAL_WIDTH * 0.7f).height(VIRTUAL_HEIGHT * 0.5f).padBottom(30);
        }

        Table navTable = new Table();
        navTable.center();

        if (currentNoteIndex > 0) {
            TextButton prevBtn = new TextButton("← " + fateGame.languageManager.getText("previous"), new TextButton.TextButtonStyle() {{
                font = fateGame.smallFont;
            }});
            prevBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent e, float x, float y) {
                    if (currentNoteTexture != null) {
                        currentNoteTexture.dispose();
                        currentNoteTexture = null;
                    }
                    currentNoteIndex--;
                    showCurrentNote();
                }
            });
            navTable.add(prevBtn).width(160).height(50).padRight(25);
        }

        Label pageLabel = new Label(fateGame.languageManager.getText("page") + " " + (currentNoteIndex + 1) + " " +
                fateGame.languageManager.getText("of") + " " + collectedNotes.size, new Label.LabelStyle() {{
            font = fateGame.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        navTable.add(pageLabel).padLeft(15).padRight(15);

        if (currentNoteIndex < collectedNotes.size - 1) {
            TextButton nextBtn = new TextButton(fateGame.languageManager.getText("next") + " →", new TextButton.TextButtonStyle() {{
                font = fateGame.smallFont;
            }});
            nextBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent e, float x, float y) {
                    if (currentNoteTexture != null) {
                        currentNoteTexture.dispose();
                        currentNoteTexture = null;
                    }
                    currentNoteIndex++;
                    showCurrentNote();
                }
            });
            navTable.add(nextBtn).width(160).height(50).padLeft(25);
        }

        contentTable.add(navTable).padBottom(30).row();

        TextButton closeBtn = new TextButton(fateGame.languageManager.getText("close"), new TextButton.TextButtonStyle() {{
            font = fateGame.smallFont;
        }});
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (currentNoteTexture != null) {
                    currentNoteTexture.dispose();
                    currentNoteTexture = null;
                }
                showingBook = false;
                bookStage.clear();
                Gdx.input.setInputProcessor(uiStage);
            }
        });
        contentTable.add(closeBtn).width(150).height(50);

        mainTable.add(contentTable).center();
    }

    // ============================================================
    // ДИАЛОГИ И СООБЩЕНИЯ
    // ============================================================

    private void createPauseDialog() {
        pauseStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        pauseStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);

        Table dialog = new Table();
        dialog.pad(30);
        Label title = new Label(fateGame.languageManager.getText("game_paused"), new Label.LabelStyle() {{
            font = fateGame.font;
        }});
        title.setFontScale(2f);
        TextButton continueBtn = new TextButton(fateGame.languageManager.getText("resume"), new TextButton.TextButtonStyle() {{
            font = fateGame.font;
        }});
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(uiStage);
                if (fateGame.gameMusic != null && fateGame.musicEnabled) fateGame.gameMusic.play();
            }
        });
        TextButton exitBtn = new TextButton(fateGame.languageManager.getText("exit_to_menu"), new TextButton.TextButtonStyle() {{
            font = fateGame.font;
        }});
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                saveProgress();
                fateGame.stopGameMusic();
                fateGame.startMenuMusic();
                fateGame.setScreen(new StartMenuScreen(fateGame));
            }
        });
        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(250).height(60).padBottom(20).row();
        dialog.add(exitBtn).width(250).height(60).row();
        table.add(dialog).center();
    }
    private void updateMusicVolume(float delta) {
        if (fateGame.gameMusic != null && fateGame.musicEnabled) {
            currentMusicVolume += (targetMusicVolume - currentMusicVolume) * volumeLerpSpeed * delta;
            fateGame.gameMusic.setVolume(currentMusicVolume);
        }
    }

    private void showMessage(String msg, float duration) {
        messageStage.clear();
        Table bg = new Table();
        bg.setFillParent(true);
        bg.setColor(0, 0, 0, 0.7f);
        messageStage.addActor(bg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label label = new Label(msg, new Label.LabelStyle() {{
            font = fateGame.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        label.setFontScale(1.2f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() { messageStage.clear(); }
        }, duration);
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
        Label label = new Label(fateGame.languageManager.getText("all_items_collected"), new Label.LabelStyle() {{
            font = fateGame.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.GREEN;
        }});
        label.setFontScale(1.2f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() { messageStage.clear(); }
        }, 3);
    }

    private void showToBeContinuedAndExit() {
        isTransitioning = true;
        showInteractionBtn = false;
        interactionBtn.setVisible(false);

        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label titleLabel = new Label(fateGame.languageManager.getText("chapter1_complete_title"), new Label.LabelStyle() {{
            font = fateGame.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        titleLabel.setFontScale(1.8f);
        Label subtitleLabel = new Label(fateGame.languageManager.getText("chapter2_unlocked"), new Label.LabelStyle() {{
            font = fateGame.font;
        }});
        subtitleLabel.setFontScale(1.2f);
        Label loadingLabel = new Label(fateGame.languageManager.getText("loading"), new Label.LabelStyle() {{
            font = fateGame.font;
        }});

        table.add(titleLabel).center().padBottom(20);
        table.row();
        table.add(subtitleLabel).center().padBottom(10);
        table.row();
        table.add(loadingLabel).center();

        fateGame.clearSave();
        fateGame.stopGameMusic();
        fateGame.prefs.putBoolean("chapter2_unlocked", true);
        fateGame.prefs.flush();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                fateGame.setScreen(new Chapter2Screen(fateGame));
            }
        }, 2);
    }

    // ============================================================
    // СОХРАНЕНИЕ
    // ============================================================

    private void saveProgress() {
        fateGame.saveGameProgress(currentMap, collectedItems);
        fateGame.prefs.flush();
    }

    private void updateItemsLabel() {
        itemsLabel.setText(fateGame.languageManager.format("items_collected", collectedItems, totalItems));
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private void showImageForObject(String objectName) {
        String imagePath = objectName + ".png";
        try {
            if (currentImageTexture != null) {
                currentImageTexture.dispose();
            }
            currentImageTexture = new Texture(imagePath);
            showingImage = true;

            imageStage.clear();
            Table darkBg = new Table();
            darkBg.setFillParent(true);
            darkBg.setColor(0, 0, 0, 0.85f);
            imageStage.addActor(darkBg);

            Table table = new Table();
            table.setFillParent(true);
            imageStage.addActor(table);

            int originalWidth = currentImageTexture.getWidth();
            int originalHeight = currentImageTexture.getHeight();
            float maxWidth = VIRTUAL_WIDTH * 0.8f;
            float maxHeight = VIRTUAL_HEIGHT * 0.8f;
            float scale = Math.min(maxWidth / originalWidth, maxHeight / originalHeight);

            Image displayedImage = new Image(currentImageTexture);
            displayedImage.setSize(originalWidth * scale, originalHeight * scale);

            Table imageContainer = new Table();
            imageContainer.add(displayedImage).center();
            table.add(imageContainer).center();

            Label continueLabel = new Label(fateGame.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
                font = fateGame.font;
            }});
            continueLabel.setFontScale(1.2f);
            table.add(continueLabel).padTop(30).center();

            imageStage.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    hideCurrentImage();
                    return true;
                }
            });

            Gdx.input.setInputProcessor(imageStage);

        } catch (Exception e) {
            hideCurrentImage();
        }
    }
}