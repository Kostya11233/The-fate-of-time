package com.thefateoftime.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.thefateoftime.TheFateGame;

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
    private static final float ZOOM = 2.6f;
    private static final float GRAVITY = -20f;

    private Body playerBody;
    private TextureRegion[] walkRightFrames;
    private TextureRegion[] walkLeftFrames;
    private TextureRegion standRight, standLeft;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private float stateTime;
    private boolean movingLeft = false, movingRight = false;
    private boolean facingRight = true;
    private float speed = 5.5f;
    private float jumpForce = 13f;
    private boolean isGrounded = false;
    private String currentMap;
    private boolean isTransitioning = false;
    private boolean isDead = false;

    private int currentLevel = 1;
    private boolean exitUnlocked = false;

    private String[] levelMaps = {"lvl1.tmx", "lvl2.tmx", "lvl3.tmx"};
    private String[] levelStartIds = {"start11", "start22", "start33"};
    private String[] levelExitIds = {"exit11", "exit22", "exit33"};

    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage dialogStage;
    private Stage questStage;
    private Stage minigameStage;
    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingItemId = null;
    private Body pendingItemBody = null;
    private Body pendingExitBody = null;
    private Body pendingNPCBody = null;
    private String pendingNPCName = null;

    private ImageButton leftBtn, rightBtn, jumpBtn, pauseBtn, interactionBtn, questBtn;
    private Texture leftTex, rightTex, jumpTex, pauseTex, interactionTex, questTex;
    private Texture dialogBgTexture;
    private Texture trashTexture;
    private Texture binTexture;
    private Texture shardTexture;
    private Texture fadeTexture;

    private Label itemsLabel;
    private Map<Body, String> itemBodies = new HashMap<>();
    private Map<Body, String> interactiveBodies = new HashMap<>();
    private Array<Body> bodiesToDestroy = new Array<>();
    private Body exitBody = null;

    private boolean trashMinigame = false;
    private int trashCollected = 0;
    private int totalTrash = 10;
    private Array<Vector2> trashPositions;
    private Vector2 binPos;
    private boolean draggingTrash = false;
    private int draggedIndex = -1;
    private Vector2 dragOffset = new Vector2();
    private float trashSize = 90f;
    private float binSize = 110f;

    private boolean shardMinigame = false;
    private int shardsClicked = 0;
    private int totalShards = 15;
    private Array<Vector2> shardPositions;
    private float shardSize = 80f;

    private int partsCollected = 0;
    private boolean talkedToChemist = false;

    private boolean showingDialog = false;
    private String[] dialogLines;
    private int dialogIndex = 0;
    private Runnable dialogCallback;

    private boolean fading = false;
    private float fadeAlpha = 0f;
    private Runnable fadeCallback = null;
    private static final float FADE_DURATION = 0.5f;

    private float uiScale;
    private int btnSize;
    private int pauseSize;

    private float playerSize = 90f;
    private float playerRadius = 0.45f;

    // КОНСТАНТЫ ДЛЯ UI
    private static final float UI_MARGIN = 20f;
    private static final float BTN_SPACING = 15f;

    public Chapter2Screen(TheFateGame game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();

        this.uiScale = game.getUIScale();
        this.btnSize = game.getScaledSize(90);
        this.pauseSize = game.getScaledSize(65);

        float worldWidth = TheFateGame.VIRTUAL_WIDTH / ZOOM;
        float worldHeight = TheFateGame.VIRTUAL_HEIGHT / ZOOM;
        this.camera.setToOrtho(false, worldWidth, worldHeight);
        this.camera.zoom = ZOOM;

        this.uiStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.pauseStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.messageStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.dialogStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.questStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.minigameStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        fadeTexture = new Texture(pixmap);
        pixmap.dispose();

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

        leftTex = loadTexture("button/button_left.png", 0.2f, 0.4f, 0.8f);
        rightTex = loadTexture("button/button_right.png", 0.2f, 0.4f, 0.8f);
        jumpTex = loadTexture("button/button_jump.png", 0.2f, 0.8f, 0.2f);
        pauseTex = loadTexture("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTexture("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
        questTex = loadTexture("button/button_tasks.png", 0.5f, 0.3f, 0.8f);
        dialogBgTexture = loadTexture("button1.png", 0.2f, 0.2f, 0.3f);
        trashTexture = loadTexture("my.png", 0.6f, 0.4f, 0.2f);
        binTexture = loadTexture("corzina.png", 0.3f, 0.6f, 0.3f);
        shardTexture = loadTexture("os.png", 0.8f, 0.5f, 0.1f);
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
        uiStage.clear();

        float screenW = TheFateGame.VIRTUAL_WIDTH;
        float screenH = TheFateGame.VIRTUAL_HEIGHT;

        // === НИЖНИЙ РЯД: КНОПКИ ДВИЖЕНИЯ ===
        float bottomY = 40f * uiScale;
        float leftBtnX = 40f * uiScale;
        float rightBtnX = leftBtnX + btnSize + BTN_SPACING;

        leftBtn = createMoveButton(leftTex, leftBtnX, bottomY, () -> movingLeft = true, () -> movingLeft = false);
        rightBtn = createMoveButton(rightTex, rightBtnX, bottomY, () -> movingRight = true, () -> movingRight = false);

        // === ПРЫЖОК (ПРАВЫЙ НИЖНИЙ УГОЛ) ===
        float jumpBtnX = screenW - btnSize - 40f * uiScale;
        jumpBtn = new ImageButton(new TextureRegionDrawable(jumpTex));
        jumpBtn.setSize(btnSize, btnSize);
        jumpBtn.setPosition(jumpBtnX, bottomY);
        jumpBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame && isGrounded) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce);
                    isGrounded = false;
                }
            }
        });

        // === КНОПКА ПАУЗЫ (ВЕРХНИЙ ПРАВЫЙ УГОЛ) ===
        float pauseBtnX = screenW - pauseSize - 25f * uiScale;
        float pauseBtnY = screenH - pauseSize - 25f * uiScale;
        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(pauseBtnX, pauseBtnY);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!showingDialog && !trashMinigame && !shardMinigame) {
                    isPaused = true;
                    createPauseDialog();
                    Gdx.input.setInputProcessor(pauseStage);
                    if (game.gameMusic != null) game.gameMusic.pause();
                }
            }
        });

        // === КНОПКА ЗАДАНИЙ (ВЕРХНИЙ ЛЕВЫЙ УГОЛ) ===
        float questBtnX = 25f * uiScale;
        float questBtnY = screenH - pauseSize - 25f * uiScale;
        questBtn = new ImageButton(new TextureRegionDrawable(questTex));
        questBtn.setSize(pauseSize, pauseSize);
        questBtn.setPosition(questBtnX, questBtnY);
        questBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!showingDialog && !trashMinigame && !shardMinigame) showQuestPanel();
            }
        });

        // === КНОПКА ВЗАИМОДЕЙСТВИЯ (ЦЕНТР ЭКРАНА) ===
        int interactionSize = game.getScaledSize(80);
        float interX = screenW / 2 - interactionSize / 2f;
        float interY = screenH / 2 - interactionSize / 2f;
        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(interactionSize, interactionSize);
        interactionBtn.setPosition(interX, interY);
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn && !showingDialog && !trashMinigame && !shardMinigame) {
                    if (pendingNPCBody != null) interactWithNPC();
                    else if (pendingItemBody != null && pendingItemId != null) collectItem(pendingItemBody, pendingItemId);
                    else if (pendingExitBody != null && exitUnlocked) goToNextLevel();
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                }
            }
        });

        // === ЛЕЙБЛ СЧЕТЧИКА (ВЕРХНИЙ ЦЕНТР) ===
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.smallFont;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setFontScale(1.2f);
        float labelX = screenW / 2 - 100f;
        float labelY = screenH - 45f * uiScale;
        itemsLabel.setPosition(labelX, labelY);
        updateItemsLabel();

        // === ДОБАВЛЯЕМ ВСЕ АКТОРЫ ===
        uiStage.addActor(leftBtn);
        uiStage.addActor(rightBtn);
        uiStage.addActor(jumpBtn);
        uiStage.addActor(pauseBtn);
        uiStage.addActor(questBtn);
        uiStage.addActor(interactionBtn);
        uiStage.addActor(itemsLabel);
    }

    private ImageButton createMoveButton(Texture tex, float x, float y, Runnable onDown, Runnable onUp) {
        ImageButton btn = new ImageButton(new TextureRegionDrawable(tex));
        btn.setSize(btnSize, btnSize);
        btn.setPosition(x, y);
        btn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame) onDown.run();
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { onUp.run(); }
        });
        return btn;
    }

    private void updateItemsLabel() {
        String text = "";
        if (currentLevel == 2 && partsCollected > 0) {
            text = game.languageManager.format("parts_collected", partsCollected);
        } else if (currentLevel == 3 && shardsClicked > 0) {
            text = game.languageManager.format("shards_collected", shardsClicked);
        }
        itemsLabel.setText(text);
    }

    private void showQuestPanel() {
        questStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.85f);
        questStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        questStage.addActor(table);

        Table panel = new Table();
        if (dialogBgTexture != null) panel.setBackground(new TextureRegionDrawable(dialogBgTexture));
        panel.pad(30);

        Label title = new Label(game.languageManager.getText("quests"), new Label.LabelStyle() {{ font = game.titleFont; fontColor = Color.GOLD; }});
        Table tasks = new Table();

        if (currentLevel == 1) {
            addTask(tasks, game.languageManager.getText("talk_to_survivor"), trashCollected > 0 || exitUnlocked);
            addTask(tasks, game.languageManager.format("trash_task", trashCollected, totalTrash), trashCollected >= totalTrash);
            addTask(tasks, game.languageManager.getText("go_to_exit"), exitUnlocked);
        } else if (currentLevel == 2) {
            addTask(tasks, game.languageManager.getText("talk_to_chemist"), talkedToChemist);
            addTask(tasks, game.languageManager.format("find_parts", partsCollected), partsCollected >= 3);
            addTask(tasks, game.languageManager.getText("return_to_chemist"), partsCollected >= 3 && !exitUnlocked);
            addTask(tasks, game.languageManager.getText("go_to_exit"), exitUnlocked);
        } else if (currentLevel == 3) {
            addTask(tasks, game.languageManager.getText("talk_to_watchmaker"), shardMinigame || shardsClicked > 0);
            addTask(tasks, game.languageManager.format("collect_shards", shardsClicked, totalShards), shardsClicked >= totalShards);
            addTask(tasks, game.languageManager.getText("go_to_exit"), exitUnlocked);
        }

        TextButton closeBtn = new TextButton(game.languageManager.getText("close"), new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                questStage.clear();
                Gdx.input.setInputProcessor(uiStage);
            }
        });

        panel.add(title).padBottom(30).row();
        panel.add(tasks).padBottom(30).row();
        panel.add(closeBtn).width(150).height(50);
        table.add(panel).center();
        Gdx.input.setInputProcessor(questStage);
    }

    private void addTask(Table table, String text, boolean completed) {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.smallFont;
        style.fontColor = completed ? Color.GREEN : Color.WHITE;
        table.add(new Label((completed ? "✓ " : "○ ") + text, style)).padBottom(8).left().row();
    }

    private void showDialog(String[] lines, Runnable onEnd) {
        showingDialog = true;
        dialogLines = lines;
        dialogIndex = 0;
        dialogCallback = onEnd;
        showDialogLine();
    }

    private void showDialogLine() {
        dialogStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        dialogStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        dialogStage.addActor(table);

        Table dialogBox = new Table();
        if (dialogBgTexture != null) dialogBox.setBackground(new TextureRegionDrawable(dialogBgTexture));
        dialogBox.pad(20);

        Label textLabel = new Label(dialogLines[dialogIndex], new Label.LabelStyle() {{ font = game.smallFont; }});
        textLabel.setWrap(true);

        TextButton nextBtn = new TextButton(game.languageManager.getText("next_level"), new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        nextBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                dialogIndex++;
                if (dialogIndex >= dialogLines.length) closeDialog();
                else showDialogLine();
            }
        });

        dialogBox.add(textLabel).width(500).padBottom(15).row();
        dialogBox.add(nextBtn).width(120).height(45);
        table.add(dialogBox).center().bottom().padBottom(80);

        Gdx.input.setInputProcessor(dialogStage);
    }

    private void closeDialog() {
        showingDialog = false;
        dialogStage.clear();
        Gdx.input.setInputProcessor(uiStage);
        if (dialogCallback != null) {
            dialogCallback.run();
            dialogCallback = null;
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
        Label label = new Label(msg, new Label.LabelStyle() {{ font = game.smallFont; }});
        label.setFontScale(1.2f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() { messageStage.clear(); }
        }, duration);
    }

    private void startFade(Runnable callback) {
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

    private void loadLevel(int level) {
        currentLevel = level;
        currentMap = levelMaps[level - 1];
        exitUnlocked = false;
        itemBodies.clear();
        interactiveBodies.clear();
        bodiesToDestroy.clear();
        exitBody = null;

        trashCollected = 0;
        shardMinigame = false;
        shardsClicked = 0;
        partsCollected = 0;
        talkedToChemist = false;

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

        String[] titles = {
                game.languageManager.getText("level_1_title"),
                game.languageManager.getText("level_2_title"),
                game.languageManager.getText("level_3_title")
        };

        Label label = new Label(titles[currentLevel - 1], new Label.LabelStyle() {{ font = game.titleFont; fontColor = Color.GOLD; }});
        label.setFontScale(1.3f);
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
                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) isGrounded = true;
                if ((a == playerBody && isJumpPad(b)) || (b == playerBody && isJumpPad(a))) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce * 1.5f);
                    isGrounded = false;
                }
                if ((a == playerBody && interactiveBodies.containsKey(b)) || (b == playerBody && interactiveBodies.containsKey(a))) {
                    pendingNPCBody = (a == playerBody) ? b : a;
                    pendingNPCName = interactiveBodies.get(pendingNPCBody);
                    pendingItemBody = null;
                    pendingExitBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                if ((a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) ||
                        (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected"))) {
                    pendingItemBody = (a == playerBody) ? b : a;
                    pendingItemId = itemBodies.get(pendingItemBody);
                    pendingNPCBody = null;
                    pendingExitBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                if (exitBody != null && ((a == playerBody && b == exitBody) || (b == playerBody && a == exitBody)) && exitUnlocked) {
                    pendingExitBody = exitBody;
                    pendingNPCBody = null;
                    pendingItemBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                if ((a == playerBody && isKill(b)) || (b == playerBody && isKill(a))) dieAndRestart();
            }
            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (interactiveBodies.containsKey(b) || itemBodies.containsKey(b) || (exitBody != null && b == exitBody))) ||
                        (b == playerBody && (interactiveBodies.containsKey(a) || itemBodies.containsKey(a) || (exitBody != null && a == exitBody)))) {
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                    pendingNPCBody = null;
                    pendingItemBody = null;
                    pendingExitBody = null;
                }
            }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private boolean isGround(Body body) { Object data = body.getUserData(); return data != null && (data.equals("wall") || data.equals("ground")); }
    private boolean isJumpPad(Body body) { Object data = body.getUserData(); return data != null && data.equals("jump"); }
    private boolean isKill(Body body) { Object data = body.getUserData(); return data != null && data.equals("kill"); }

    private void createPlayer() {
        Vector2 spawnPos = findSpawnPosition(levelStartIds[currentLevel - 1]);
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(spawnPos);
        playerBody = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(playerRadius);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        playerBody.setFixedRotation(true);
    }

    private Vector2 findSpawnPosition(String spawnId) {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer.getName() != null && layer.getName().equals(spawnId)) {
                for (MapObject obj : layer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) return new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                }
            }
        }
        return new Vector2(640 / PPM, 360 / PPM);
    }

    private void createCollision() {
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

        if (currentLevel == 1) {
            MapLayer npcLayer = tiledMap.getLayers().get("npc1");
            if (npcLayer != null) {
                for (MapObject obj : npcLayer.getObjects()) {
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
                    interactiveBodies.put(body, game.languageManager.getText("survivor_name"));
                }
            }
        } else if (currentLevel == 2) {
            MapLayer npcLayer = tiledMap.getLayers().get("npc2");
            if (npcLayer != null) {
                for (MapObject obj : npcLayer.getObjects()) {
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
                    interactiveBodies.put(body, game.languageManager.getText("chemist_name"));
                }
            }
            String[] parts = {"item22", "item222", "item2222"};
            for (String p : parts) {
                MapLayer layer = tiledMap.getLayers().get(p);
                if (layer != null) {
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
                        itemBodies.put(body, "part");
                    }
                }
            }
        } else if (currentLevel == 3) {
            MapLayer npcLayer = tiledMap.getLayers().get("npc3");
            if (npcLayer != null) {
                for (MapObject obj : npcLayer.getObjects()) {
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
                    interactiveBodies.put(body, game.languageManager.getText("watchmaker_name"));
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
        if (x != null && y != null) return new Rectangle(x, y, w != null ? w : 100, h != null ? h : 100);
        return null;
    }

    private void interactWithNPC() {
        if (currentLevel == 1 && pendingNPCName != null && pendingNPCName.equals(game.languageManager.getText("survivor_name"))) {
            if (trashCollected >= totalTrash) {
                showDialog(new String[]{
                        game.languageManager.getText("survivor_dialog_3"),
                        game.languageManager.getText("survivor_dialog_4"),
                        game.languageManager.getText("survivor_dialog_5")
                }, () -> {
                    exitUnlocked = true;
                    updateItemsLabel();
                    showMessage(game.languageManager.getText("exit_unlocked"), 1.5f);
                });
            } else {
                showDialog(new String[]{
                        game.languageManager.getText("survivor_dialog_1"),
                        game.languageManager.format("survivor_dialog_2", totalTrash)
                }, () -> startTrashMinigame());
            }
        } else if (currentLevel == 2 && pendingNPCName != null && pendingNPCName.equals(game.languageManager.getText("chemist_name"))) {
            if (partsCollected >= 3) {
                showDialog(new String[]{
                        game.languageManager.getText("chemist_dialog_5"),
                        game.languageManager.getText("chemist_dialog_6"),
                        game.languageManager.getText("chemist_dialog_7")
                }, () -> {
                    exitUnlocked = true;
                    updateItemsLabel();
                    showMessage(game.languageManager.getText("exit_unlocked"), 1.5f);
                });
            } else if (talkedToChemist) {
                showDialog(new String[]{
                        game.languageManager.getText("chemist_dialog_4")
                }, null);
            } else {
                showDialog(new String[]{
                        game.languageManager.getText("chemist_dialog_1"),
                        game.languageManager.getText("chemist_dialog_2"),
                        game.languageManager.getText("chemist_dialog_3")
                }, () -> {
                    talkedToChemist = true;
                    updateItemsLabel();
                    showMessage(game.languageManager.getText("find_parts_message"), 2f);
                });
            }
        } else if (currentLevel == 3 && pendingNPCName != null && pendingNPCName.equals(game.languageManager.getText("watchmaker_name"))) {
            if (shardsClicked >= totalShards) {
                showDialog(new String[]{
                        game.languageManager.getText("watchmaker_dialog_3"),
                        game.languageManager.getText("watchmaker_dialog_4")
                }, () -> {
                    exitUnlocked = true;
                    updateItemsLabel();
                    showMessage(game.languageManager.getText("exit_unlocked"), 1.5f);
                });
            } else {
                showDialog(new String[]{
                        game.languageManager.getText("watchmaker_dialog_1"),
                        game.languageManager.format("watchmaker_dialog_2", totalShards)
                }, () -> startShardMinigame());
            }
        }
    }

    private void startTrashMinigame() {
        trashMinigame = true;
        trashCollected = 0;
        trashPositions = new Array<>();
        float w = TheFateGame.VIRTUAL_WIDTH;
        float h = TheFateGame.VIRTUAL_HEIGHT;

        for (int i = 0; i < totalTrash; i++) {
            float x = 50 + (float) Math.random() * (w - 100);
            float y = 100 + (float) Math.random() * (h - 200);
            trashPositions.add(new Vector2(x, y));
        }
        binPos = new Vector2(w * 0.5f, h * 0.12f);

        minigameStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        minigameStage.addActor(darkBg);

        Label title = new Label(game.languageManager.getText("trash_minigame_title"), new Label.LabelStyle() {{ font = game.titleFont; fontColor = Color.GOLD; }});
        title.setPosition(w / 2 - 150, h - 80);
        minigameStage.addActor(title);

        final Label counter = new Label(game.languageManager.format("trash_minigame_counter", 0, totalTrash), new Label.LabelStyle() {{ font = game.font; }});
        counter.setPosition(20, h - 60);
        minigameStage.addActor(counter);

        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_minigame"), new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        exitBtn.setSize(100, 50);
        exitBtn.setPosition(w - 120, h - 70);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { exitTrashMinigame(); }
        });
        minigameStage.addActor(exitBtn);

        minigameStage.getRoot().setUserObject(counter);
        Gdx.input.setInputProcessor(minigameStage);
    }

    private void updateTrashMinigame(float delta) {
        if (!trashMinigame) return;

        Object obj = minigameStage.getRoot().getUserObject();
        if (obj instanceof Label) {
            ((Label) obj).setText(game.languageManager.format("trash_minigame_counter", trashCollected, totalTrash));
        }

        if (Gdx.input.isTouched()) {
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            Vector3 stageCoords = minigameStage.getViewport().unproject(new Vector3(screenX, screenY, 0));
            float touchX = stageCoords.x;
            float touchY = stageCoords.y;

            if (!draggingTrash) {
                for (int i = 0; i < trashPositions.size; i++) {
                    Vector2 p = trashPositions.get(i);
                    if (p != null && touchX >= p.x - trashSize/2 && touchX <= p.x + trashSize/2 &&
                            touchY >= p.y - trashSize/2 && touchY <= p.y + trashSize/2) {
                        draggingTrash = true;
                        draggedIndex = i;
                        dragOffset.set(p.x - touchX, p.y - touchY);
                        break;
                    }
                }
            } else if (draggedIndex >= 0 && draggedIndex < trashPositions.size) {
                Vector2 p = trashPositions.get(draggedIndex);
                if (p != null) {
                    p.set(touchX + dragOffset.x, touchY + dragOffset.y);
                }
            }
        } else {
            if (draggingTrash && draggedIndex >= 0 && draggedIndex < trashPositions.size) {
                Vector2 p = trashPositions.get(draggedIndex);
                if (p != null && p.x >= binPos.x - binSize/2 && p.x <= binPos.x + binSize/2 &&
                        p.y >= binPos.y - binSize/2 && p.y <= binPos.y + binSize/2) {
                    trashPositions.set(draggedIndex, null);
                    trashCollected++;

                    if (trashCollected >= totalTrash) {
                        showMinigameMessage(game.languageManager.getText("all_trash_collected"));
                        Timer.schedule(new Timer.Task() {
                            @Override public void run() { exitTrashMinigame(); }
                        }, 2);
                    } else {
                        showMinigameMessage(game.languageManager.format("trash_collected_message", trashCollected, totalTrash));
                    }
                }
            }
            draggingTrash = false;
            draggedIndex = -1;
        }
    }

    private void drawTrashMinigame() {
        if (binTexture != null && binPos != null) {
            batch.draw(binTexture, binPos.x - binSize/2, binPos.y - binSize/2, binSize, binSize);
        }
        if (trashTexture != null && trashPositions != null) {
            for (Vector2 p : trashPositions) {
                if (p != null) {
                    batch.draw(trashTexture, p.x - trashSize/2, p.y - trashSize/2, trashSize, trashSize);
                }
            }
        }
    }

    private void exitTrashMinigame() {
        trashMinigame = false;
        minigameStage.clear();
        Gdx.input.setInputProcessor(uiStage);
        if (trashCollected >= totalTrash) showMessage(game.languageManager.format("return_to_npc", game.languageManager.getText("survivor_name")), 2f);
    }

    private void startShardMinigame() {
        shardMinigame = true;
        shardsClicked = 0;
        shardPositions = new Array<>();
        float w = TheFateGame.VIRTUAL_WIDTH;
        float h = TheFateGame.VIRTUAL_HEIGHT;

        for (int i = 0; i < totalShards; i++) {
            float x = 50 + (float) Math.random() * (w - 100);
            float y = 100 + (float) Math.random() * (h - 200);
            shardPositions.add(new Vector2(x, y));
        }

        minigameStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        minigameStage.addActor(darkBg);

        Label title = new Label(game.languageManager.getText("shard_minigame_title"), new Label.LabelStyle() {{ font = game.titleFont; fontColor = Color.GOLD; }});
        title.setPosition(w / 2 - 150, h - 80);
        minigameStage.addActor(title);

        final Label counter = new Label(game.languageManager.format("shard_minigame_counter", 0, totalShards), new Label.LabelStyle() {{ font = game.font; }});
        counter.setPosition(20, h - 60);
        minigameStage.addActor(counter);

        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_minigame"), new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        exitBtn.setSize(100, 50);
        exitBtn.setPosition(w - 120, h - 70);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { exitShardMinigame(); }
        });
        minigameStage.addActor(exitBtn);

        minigameStage.getRoot().setUserObject(counter);
        Gdx.input.setInputProcessor(minigameStage);
    }

    private void updateShardMinigame(float delta) {
        if (!shardMinigame) return;

        Object obj = minigameStage.getRoot().getUserObject();
        if (obj instanceof Label) {
            ((Label) obj).setText(game.languageManager.format("shard_minigame_counter", shardsClicked, totalShards));
        }

        if (Gdx.input.justTouched()) {
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            Vector3 stageCoords = minigameStage.getViewport().unproject(new Vector3(screenX, screenY, 0));
            float touchX = stageCoords.x;
            float touchY = stageCoords.y;

            for (int i = 0; i < shardPositions.size; i++) {
                Vector2 p = shardPositions.get(i);
                if (p != null && touchX >= p.x - shardSize/2 && touchX <= p.x + shardSize/2 &&
                        touchY >= p.y - shardSize/2 && touchY <= p.y + shardSize/2) {
                    shardPositions.set(i, null);
                    shardsClicked++;

                    if (shardsClicked >= totalShards) {
                        showMinigameMessage(game.languageManager.getText("all_shards_collected"));
                        Timer.schedule(new Timer.Task() {
                            @Override public void run() { exitShardMinigame(); }
                        }, 2);
                    } else {
                        showMinigameMessage(game.languageManager.format("shard_collected_message", shardsClicked, totalShards));
                    }
                    break;
                }
            }
        }
    }

    private void drawShardMinigame() {
        if (shardTexture != null && shardPositions != null) {
            for (Vector2 p : shardPositions) {
                if (p != null) {
                    batch.draw(shardTexture, p.x - shardSize/2, p.y - shardSize/2, shardSize, shardSize);
                }
            }
        }
    }

    private void exitShardMinigame() {
        shardMinigame = false;
        minigameStage.clear();
        Gdx.input.setInputProcessor(uiStage);
        if (shardsClicked >= totalShards) showMessage(game.languageManager.format("return_to_npc", game.languageManager.getText("watchmaker_name")), 2f);
    }

    private void showMinigameMessage(String msg) {
        Label label = new Label(msg, new Label.LabelStyle() {{ font = game.smallFont; fontColor = Color.GREEN; }});
        label.setPosition(TheFateGame.VIRTUAL_WIDTH / 2 - 150, TheFateGame.VIRTUAL_HEIGHT / 2);
        minigameStage.addActor(label);
        Timer.schedule(new Timer.Task() { @Override public void run() { label.remove(); } }, 1.5f);
    }

    private void collectItem(Body body, String id) {
        if (currentLevel == 2 && id.equals("part")) {
            partsCollected++;
            updateItemsLabel();
            bodiesToDestroy.add(body);
            itemBodies.put(body, "collected");
            showMessage(game.languageManager.format("part_collected", partsCollected), 1f);
            if (partsCollected >= 3) showMessage(game.languageManager.getText("all_parts_collected"), 2f);
        }
        showInteractionBtn = false;
        interactionBtn.setVisible(false);
        pendingItemBody = null;
        pendingItemId = null;
    }

    private void goToNextLevel() {
        startFade(() -> {
            if (currentLevel < 3) { currentLevel++; loadLevel(currentLevel); }
            else completeGame();
            isTransitioning = false;
            fading = false;
        });
        isTransitioning = true;
    }

    private void completeGame() {
        showMessage(game.languageManager.getText("chapter2_complete"), 3f);
        game.prefs.putBoolean("chapter2_completed", true);
        game.prefs.putBoolean("chapter3_unlocked", true);
        game.prefs.flush();
        game.stopGameMusic();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(new Chapter3Screen(game));
            }
        }, 3);
    }

    private void dieAndRestart() {
        if (isDead) return;
        isDead = true;
        showMessage(game.languageManager.getText("you_died"), 1.5f);
        Timer.schedule(new Timer.Task() { @Override public void run() { loadLevel(currentLevel); isDead = false; } }, 1.5f);
    }

    private void destroyMarkedBodies() {
        for (Body b : bodiesToDestroy) if (b != null && b.getWorld() == world) world.destroyBody(b);
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
        Table dialog = new Table();
        dialog.pad(30);
        Label title = new Label(game.languageManager.getText("game_paused"), new Label.LabelStyle() {{ font = game.font; }});
        title.setFontScale(2f);
        TextButton continueBtn = new TextButton(game.languageManager.getText("resume"), new TextButton.TextButtonStyle() {{ font = game.font; }});
        continueBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(uiStage);
                if (game.gameMusic != null && game.musicEnabled) game.gameMusic.play();
            }
        });
        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_to_menu"), new TextButton.TextButtonStyle() {{ font = game.font; }});
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
        if (isPaused || isTransitioning || isDead || showingDialog) return;
        if (trashMinigame) { updateTrashMinigame(delta); return; }
        if (shardMinigame) { updateShardMinigame(delta); return; }

        float velX = (movingRight ? speed : 0) + (movingLeft ? -speed : 0);
        playerBody.setLinearVelocity(velX, playerBody.getLinearVelocity().y);
        if (movingLeft || movingRight) stateTime += delta;
        else stateTime = 0;
        if (movingRight) facingRight = true;
        if (movingLeft) facingRight = false;

        world.step(delta, 6, 2);
        destroyMarkedBodies();

        if (playerBody.getPosition().y < -2) dieAndRestart();
        camera.position.set(playerBody.getPosition().x * PPM, playerBody.getPosition().y * PPM, 0);
        camera.update();
    }

    private void drawPlayer() {
        TextureRegion region;
        if (movingLeft) region = walkLeftAnimation.getKeyFrame(stateTime, true);
        else if (movingRight) region = walkRightAnimation.getKeyFrame(stateTime, true);
        else region = facingRight ? standRight : standLeft;
        Vector2 pos = playerBody.getPosition();
        batch.draw(region, pos.x * PPM - playerSize/2, pos.y * PPM - playerSize/2, playerSize, playerSize);
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (trashMinigame) {
            batch.setProjectionMatrix(minigameStage.getCamera().combined);
            batch.begin();
            drawTrashMinigame();
            batch.end();
            minigameStage.act(delta);
            minigameStage.draw();
        } else if (shardMinigame) {
            batch.setProjectionMatrix(minigameStage.getCamera().combined);
            batch.begin();
            drawShardMinigame();
            batch.end();
            minigameStage.act(delta);
            minigameStage.draw();
        } else {
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
        }

        if (isPaused) pauseStage.draw();
        messageStage.act(delta);
        messageStage.draw();
        if (showingDialog) { dialogStage.act(delta); dialogStage.draw(); }
        questStage.act(delta);
        questStage.draw();

        if (fading) {
            updateFade(delta);
            batch.begin();
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(fadeTexture, 0, 0, TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }
    }

    @Override public void resize(int w, int h) {
        game.viewport.update(w, h, true);
        float worldW = game.viewport.getWorldWidth() / ZOOM;
        float worldH = game.viewport.getWorldHeight() / ZOOM;
        camera.viewportWidth = worldW;
        camera.viewportHeight = worldH;
        camera.update();
        uiStage.getViewport().update(w, h, true);
        pauseStage.getViewport().update(w, h, true);
        messageStage.getViewport().update(w, h, true);
        dialogStage.getViewport().update(w, h, true);
        questStage.getViewport().update(w, h, true);
        minigameStage.getViewport().update(w, h, true);
        uiScale = game.getUIScale();
        btnSize = game.getScaledSize(90);
        pauseSize = game.getScaledSize(65);
        createUI();
    }

    @Override public void show() { Gdx.input.setInputProcessor(uiStage); game.startGameMusic(); }
    @Override public void hide() { game.stopGameMusic(); }
    @Override public void dispose() {
        uiStage.dispose(); pauseStage.dispose(); messageStage.dispose();
        dialogStage.dispose(); questStage.dispose(); minigameStage.dispose();
        if (fadeTexture != null) fadeTexture.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
}