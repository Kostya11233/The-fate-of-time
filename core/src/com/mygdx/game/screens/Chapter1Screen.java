package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.viewport.FitViewport;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

public class Chapter1Screen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;
    private static final float PPM = 32f;
    private static final float ZOOM = 3.0f;
    private static final float GRAVITY = 0f;
    private boolean fading = false;
    private float fadeAlpha = 0f;
    private Runnable fadeCallback = null;
    private static final float FADE_DURATION = 0.5f;


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
    private Stage imageStage;
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

    private Map<Body, String> interactiveBodies = new HashMap<>();
    private Texture currentImageTexture = null;
    private boolean showingImage = false;
    private Body pendingInteractiveBody = null;
    private String pendingInteractiveName = null;
    private Body pendingMaBody = null;

    private float saveTimer = 0f;
    private static final float SAVE_INTERVAL = 5f;

    // Адаптивные размеры
    private float uiScale;
    private int btnSize;
    private int pauseSize;

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

        // Адаптивные размеры
        this.uiScale = game.getUIScale();
        this.btnSize = game.getScaledSize(100);
        this.pauseSize = game.getScaledSize(70);

        float worldWidth = TheFateGame.VIRTUAL_WIDTH / ZOOM;
        float worldHeight = TheFateGame.VIRTUAL_HEIGHT / ZOOM;
        this.camera.setToOrtho(false, worldWidth, worldHeight);
        this.camera.zoom = ZOOM;

        this.uiStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.pauseStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.messageStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.imageStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));

        collectedItems = game.prefs.getInteger("chapter1_items", 0);
        allItemsCollected = collectedItems >= totalItems;

        loadTextures();
        createUI();
        loadMap(currentMap);
        createWorld();
        createPlayer();
        createCollisionAndTeleports();

        Gdx.input.setInputProcessor(uiStage);
        game.stopMenuMusic();
        game.startGameMusic();
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
        float screenW = TheFateGame.VIRTUAL_WIDTH;
        float screenH = TheFateGame.VIRTUAL_HEIGHT;

        float btnMargin = 20 * uiScale;
        float btnBottomY = 30 * uiScale;
        float btnAreaCenter = screenW / 5;

        // Up button
        upBtn = new ImageButton(new TextureRegionDrawable(upTex));
        upBtn.setSize(btnSize, btnSize);
        upBtn.setPosition(btnAreaCenter - btnSize/2, btnBottomY + btnSize + btnMargin);
        upBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !showingImage) movingUp = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingUp = false; }
        });

        // Down button
        downBtn = new ImageButton(new TextureRegionDrawable(downTex));
        downBtn.setSize(btnSize, btnSize);
        downBtn.setPosition(btnAreaCenter - btnSize/2, btnBottomY);
        downBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !showingImage) movingDown = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingDown = false; }
        });

        // Left button
        leftBtn = new ImageButton(new TextureRegionDrawable(leftTex));
        leftBtn.setSize(btnSize, btnSize);
        leftBtn.setPosition(btnAreaCenter - btnSize - btnMargin, btnBottomY + btnSize/2);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !showingImage) movingLeft = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingLeft = false; }
        });

        // Right button
        rightBtn = new ImageButton(new TextureRegionDrawable(rightTex));
        rightBtn.setSize(btnSize, btnSize);
        rightBtn.setPosition(btnAreaCenter + btnMargin, btnBottomY + btnSize/2);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !showingImage) movingRight = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingRight = false; }
        });

        // Pause button
        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - 20 * uiScale, screenH - pauseSize - 20 * uiScale);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!showingImage) {
                    isPaused = true;
                    createPauseDialog();
                    Gdx.input.setInputProcessor(pauseStage);
                    if (game.gameMusic != null) game.gameMusic.pause();
                }
            }
        });

        // Interaction button
        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(game.getScaledSize(80), game.getScaledSize(80));
        interactionBtn.setPosition(screenW / 2 - game.getScaledSize(40), screenH / 2 - game.getScaledSize(50));
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn && !showingImage) {
                    if (pendingDoorId != null) {
                        teleportToDoor(pendingDoorId);
                    } else if (pendingItemBody != null && pendingItemId != null) {
                        collectItem(pendingItemBody, pendingItemId);
                    } else if (pendingInteractiveBody != null && pendingInteractiveName != null) {
                        showImageForObject(pendingInteractiveName);
                        showInteractionBtn = false;
                        interactionBtn.setVisible(false);
                        pendingInteractiveBody = null;
                        pendingInteractiveName = null;
                    } else if (pendingMaBody != null && allItemsCollected) {
                        showToBeContinuedAndExit();
                        showInteractionBtn = false;
                        interactionBtn.setVisible(false);
                        pendingMaBody = null;
                    }
                }
            }
        });

        uiStage.addActor(upBtn);
        uiStage.addActor(downBtn);
        uiStage.addActor(leftBtn);
        uiStage.addActor(rightBtn);
        uiStage.addActor(pauseBtn);
        uiStage.addActor(interactionBtn);

        // Items label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.smallFont;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20 * uiScale, screenH - 50 * uiScale);
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
            if (collectedItems >= totalItems) {
                allItemsCollected = true;
                showAllItemsCollectedMessage();
                game.prefs.putBoolean("chapter2_unlocked", true);
                game.prefs.flush();
            }
            saveProgress();
        }
    }

    private void saveProgress() {
        game.saveGameProgress(currentMap, collectedItems);
        game.prefs.flush();
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
        labelStyle.font = game.smallFont; // используем маленький шрифт
        labelStyle.fontColor = com.badlogic.gdx.graphics.Color.GREEN;
        Label label = new Label(game.languageManager.getText("all_items_collected"), labelStyle);
        label.setFontScale(1.2f); // меньший масштаб

        table.add(label).center();

        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                messageStage.clear();
            }
        }, 2);
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

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = com.badlogic.gdx.graphics.Color.GOLD;

        Label.LabelStyle subtitleStyle = new Label.LabelStyle();
        subtitleStyle.font = game.font;
        subtitleStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

        Label titleLabel = new Label("ГЛАВА 1 ПРОЙДЕНА!", titleStyle);
        titleLabel.setFontScale(1.8f);

        Label subtitleLabel = new Label("Открыта Глава 2!", subtitleStyle);
        subtitleLabel.setFontScale(1.2f);

        Label loadingLabel = new Label("Загрузка...", subtitleStyle);
        loadingLabel.setFontScale(1f);

        table.add(titleLabel).center().padBottom(20);
        table.row();
        table.add(subtitleLabel).center().padBottom(10);
        table.row();
        table.add(loadingLabel).center();

        game.clearSave();
        game.stopGameMusic();

        // Сохраняем что 2 глава разблокирована
        game.prefs.putBoolean("chapter2_unlocked", true);
        game.prefs.flush();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                // Запускаем 2 главу
                game.setScreen(new com.mygdx.game.screens.Chapter2Screen(game));
            }
        }, 2);
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
        Label title = new Label(game.languageManager.getText("game_paused"), labelStyle);
        title.setFontScale(2f);
        TextButton continueBtn = new TextButton(game.languageManager.getText("continue"), buttonStyle);
        continueBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(uiStage);
                if (game.gameMusic != null && game.musicEnabled) game.gameMusic.play();
            }
        });
        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_to_menu"), buttonStyle);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                saveProgress();
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new StartMenuScreen(game));
            }
        });
        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(game.getScaledSize(250)).height(game.getScaledSize(60)).padBottom(20).row();
        dialog.add(exitBtn).width(game.getScaledSize(250)).height(game.getScaledSize(60)).row();
        table.add(dialog).center();
    }

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

            Image displayedImage = new Image(currentImageTexture);
            float screenW = TheFateGame.VIRTUAL_WIDTH;
            float screenH = TheFateGame.VIRTUAL_HEIGHT;
            float imgWidth = currentImageTexture.getWidth();
            float imgHeight = currentImageTexture.getHeight();

            float scale = Math.min(screenW * 0.8f / imgWidth, screenH * 0.8f / imgHeight);
            displayedImage.setSize(imgWidth * scale, imgHeight * scale);
            table.add(displayedImage).center();

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = game.font;
            Label continueLabel = new Label(game.languageManager.getText("tap_to_continue"), labelStyle);
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

    private void hideCurrentImage() {
        showingImage = false;
        imageStage.clear();
        if (currentImageTexture != null) {
            currentImageTexture.dispose();
            currentImageTexture = null;
        }
        Gdx.input.setInputProcessor(uiStage);
    }

    private void loadMap(String mapPath) {
        try {
            if (tiledMap != null) tiledMap.dispose();
            tiledMap = new TmxMapLoader().load(mapPath);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            currentMap = mapPath;
        } catch (Exception e) {
        }
    }

    private void createWorld() {
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if (a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) {
                    pendingItemBody = b; pendingItemId = itemBodies.get(b); pendingDoorId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected")) {
                    pendingItemBody = a; pendingItemId = itemBodies.get(a); pendingDoorId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && isDoor(b)) {
                    pendingDoorId = (String) b.getUserData(); pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && isDoor(a)) {
                    pendingDoorId = (String) a.getUserData(); pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && interactiveBodies.containsKey(b)) {
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = b;
                    pendingInteractiveName = interactiveBodies.get(b);
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && interactiveBodies.containsKey(a)) {
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = a;
                    pendingInteractiveName = interactiveBodies.get(a);
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && isMaExit(b)) {
                    if (allItemsCollected) {
                        pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = b;
                        showInteractionBtn = true; interactionBtn.setVisible(true);
                    }
                } else if (b == playerBody && isMaExit(a)) {
                    if (allItemsCollected) {
                        pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = a;
                        showInteractionBtn = true; interactionBtn.setVisible(true);
                    }
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b) || interactiveBodies.containsKey(b) || isMaExit(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a) || interactiveBodies.containsKey(a) || isMaExit(a)))) {
                    showInteractionBtn = false; interactionBtn.setVisible(false);
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingInteractiveName = null;
                    pendingMaBody = null;
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

    private boolean isMaExit(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).equals("ma_exit");
    }
    // Добавим методы для fade перехода
    private void startFadeTransition(Runnable callback) {
        fading = true;
        fadeAlpha = 0f;
        fadeCallback = callback;
    }
    private void updateFade(float delta) {
        if (fading) {
            fadeAlpha += delta / FADE_DURATION;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                if (fadeCallback != null) {
                    fadeCallback.run();
                    fadeCallback = null;
                }
            }
        }
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

        final String finalMap = targetMap, finalSpawnId = targetSpawnId;
        final String finalReturnDoorId = returnDoorId;

        // Плавное затухание в черный
        startFadeTransition(() -> {
            loadMap(finalMap);
            recreateWorld();
            createPlayer();
            createCollisionAndTeleports();
            returnDoorId = finalReturnDoorId;
            Vector2 spawnPos = findSpawnPosition(finalSpawnId);
            if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
            saveProgress();

            // Плавное появление из черного
            fading = true;
            fadeAlpha = 1f;
            fadeCallback = () -> {
                fading = false;
                isTransitioning = false;
            };
        });
        isTransitioning = true;
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
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if (a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) {
                    pendingItemBody = b; pendingItemId = itemBodies.get(b); pendingDoorId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected")) {
                    pendingItemBody = a; pendingItemId = itemBodies.get(a); pendingDoorId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && isDoor(b)) {
                    pendingDoorId = (String) b.getUserData(); pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && isDoor(a)) {
                    pendingDoorId = (String) a.getUserData(); pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingMaBody = null;
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && interactiveBodies.containsKey(b)) {
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = b;
                    pendingInteractiveName = interactiveBodies.get(b);
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                } else if (b == playerBody && interactiveBodies.containsKey(a)) {
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = a;
                    pendingInteractiveName = interactiveBodies.get(a);
                    showInteractionBtn = true; interactionBtn.setVisible(true);
                }
                else if (a == playerBody && isMaExit(b)) {
                    if (allItemsCollected) {
                        pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = b;
                        showInteractionBtn = true; interactionBtn.setVisible(true);
                    }
                } else if (b == playerBody && isMaExit(a)) {
                    if (allItemsCollected) {
                        pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = a;
                        showInteractionBtn = true; interactionBtn.setVisible(true);
                    }
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b) || interactiveBodies.containsKey(b) || isMaExit(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a) || interactiveBodies.containsKey(a) || isMaExit(a)))) {
                    showInteractionBtn = false; interactionBtn.setVisible(false);
                    pendingDoorId = null; pendingItemBody = null; pendingItemId = null;
                    pendingInteractiveBody = null; pendingInteractiveName = null;
                    pendingMaBody = null;
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

        for (int i = 1; i <= 10; i++) {
            String layerName = "z" + i;
            MapLayer zLayer = tiledMap.getLayers().get(layerName);
            if (zLayer != null) {
                for (MapObject obj : zLayer.getObjects()) {
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
                    body.setUserData("interactive_" + layerName);
                    interactiveBodies.put(body, layerName);
                }
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

        boolean spawnSet = false;

        if (currentMap.equals("room4.tmx")) {
            float spawnX = 273f / PPM;
            float spawnY = 268f / PPM;
            playerBody.setTransform(spawnX, spawnY, 0);
            spawnSet = true;
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

        if (!spawnSet) {
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
            } else {
                playerBody.setTransform(640 / PPM, 360 / PPM, 0);
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
        if (isPaused || isTransitioning || showingImage) return;

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

        saveTimer += delta;
        if (saveTimer >= SAVE_INTERVAL) {
            saveTimer = 0f;
            saveProgress();
        }
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
        if (isPaused) { pauseStage.act(delta); pauseStage.draw(); }
        if (allItemsCollected && !showingImage) { messageStage.act(delta); messageStage.draw(); }
        if (showingImage) { imageStage.act(delta); imageStage.draw(); }

        // Отрисовка fade перехода
        if (fading) {
            updateFade(delta);
            batch.begin();
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(game.fadeTexture, 0, 0, TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }
    }

    @Override public void resize(int width, int height) {
        game.viewport.update(width, height, true);

        float worldWidth = game.viewport.getWorldWidth() / ZOOM;
        float worldHeight = game.viewport.getWorldHeight() / ZOOM;
        camera.viewportWidth = worldWidth;
        camera.viewportHeight = worldHeight;
        camera.update();

        uiStage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);
        if (imageStage != null) imageStage.getViewport().update(width, height, true);

        // Обновляем адаптивные размеры
        uiScale = game.getUIScale();
        btnSize = game.getScaledSize(100);
        pauseSize = game.getScaledSize(70);

        // Пересоздаем UI
        uiStage.clear();
        createUI();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(isPaused ? pauseStage : uiStage);
        game.stopMenuMusic();
        game.startGameMusic();
    }

    @Override public void hide() {
        game.stopGameMusic();
    }

    @Override public void dispose() {
        uiStage.dispose(); pauseStage.dispose(); messageStage.dispose();
        if (imageStage != null) imageStage.dispose();
        if (currentImageTexture != null) currentImageTexture.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
}