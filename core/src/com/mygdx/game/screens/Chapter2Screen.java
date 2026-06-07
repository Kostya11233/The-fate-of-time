package com.mygdx.game.screens;

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
import com.badlogic.gdx.math.Vector3;
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
import com.mygdx.game.TheFateGame;
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
    private static final float ZOOM = 2.5f;
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
    private boolean exitUnlocked = false;

    private String[] levelMaps = {"lvl1.tmx", "lvl2.tmx", "lvl3.tmx"};
    private int[] levelItemCount = {3, 3, 5};
    private String[] levelStartIds = {"start11", "start22", "start33"};
    private String[] levelExitIds = {"exit11", "exit22", "exit33"};

    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage imageStage;
    private Stage dialogStage;      // НОВЫЙ: для диалогов
    private Stage questStage;       // НОВЫЙ: для заданий
    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingDoorId = null;
    private String pendingItemId = null;
    private Body pendingItemBody = null;
    private Body pendingExitBody = null;
    private Body pendingNPCBody = null;      // НОВЫЙ: для NPC
    private String pendingNPCName = null;    // НОВЫЙ: имя NPC

    private ImageButton upBtn, downBtn, leftBtn, rightBtn, jumpBtn, pauseBtn, interactionBtn, questBtn;
    private Texture upTex, downTex, leftTex, rightTex, jumpTex, pauseTex, interactionTex, questTex;
    private Texture dialogBgTexture;   // НОВЫЙ: фон диалога button1.png
    private Texture trashTexture;      // НОВЫЙ: мусор my.png
    private Texture binTexture;        // НОВЫЙ: корзина corzina.png
    private Texture shardTexture;      // НОВЫЙ: осколок os.png
    private Texture fadeTexture;       // НОВЫЙ: для затемнения

    private Label itemsLabel;
    private Map<Body, String> itemBodies = new HashMap<>();
    private Map<Body, String> interactiveBodies = new HashMap<>();
    private Array<Body> bodiesToDestroy = new Array<>();
    private Body exitBody = null;

    // НОВЫЕ поля для мини-игр и квестов
    private boolean trashMinigame = false;
    private int trashCollected = 0;
    private boolean draggingTrash = false;
    private Vector2 trashPos = null;
    private Vector2 binPos = null;
    private Vector2 dragOffset = new Vector2();

    private boolean shardMinigame = false;
    private Array<Vector2> shardPositions;
    private int shardsClicked = 0;

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

    public Chapter2Screen(TheFateGame game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();

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
        this.dialogStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.questStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));

        // Создаем текстуру для затемнения
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

        upTex = loadTexture("button/button_up.png", 0.2f, 0.8f, 0.2f);
        downTex = loadTexture("button/button_down.png", 0.2f, 0.8f, 0.2f);
        leftTex = loadTexture("button/button_left.png", 0.2f, 0.4f, 0.8f);
        rightTex = loadTexture("button/button_right.png", 0.2f, 0.4f, 0.8f);
        jumpTex = loadTexture("button/button_jump.png", 0.2f, 0.8f, 0.2f);
        pauseTex = loadTexture("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTexture("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
        questTex = loadTexture("button/button_.png", 0.5f, 0.3f, 0.8f);
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
        float screenW = TheFateGame.VIRTUAL_WIDTH;
        float screenH = TheFateGame.VIRTUAL_HEIGHT;

        float btnMargin = 20 * uiScale;
        float btnBottomY = 30 * uiScale;
        float btnAreaCenter = screenW / 5;

        upBtn = new ImageButton(new TextureRegionDrawable(upTex));
        upBtn.setSize(btnSize, btnSize);
        upBtn.setPosition(btnAreaCenter - btnSize/2, btnBottomY + btnSize + btnMargin);
        upBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame) movingUp = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingUp = false; }
        });

        downBtn = new ImageButton(new TextureRegionDrawable(downTex));
        downBtn.setSize(btnSize, btnSize);
        downBtn.setPosition(btnAreaCenter - btnSize/2, btnBottomY);
        downBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame) movingDown = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingDown = false; }
        });

        leftBtn = new ImageButton(new TextureRegionDrawable(leftTex));
        leftBtn.setSize(btnSize, btnSize);
        leftBtn.setPosition(btnAreaCenter - btnSize - btnMargin, btnBottomY + btnSize/2);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame) movingLeft = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingLeft = false; }
        });

        rightBtn = new ImageButton(new TextureRegionDrawable(rightTex));
        rightBtn.setSize(btnSize, btnSize);
        rightBtn.setPosition(btnAreaCenter + btnMargin, btnBottomY + btnSize/2);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame) movingRight = true;
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) { movingRight = false; }
        });

        jumpBtn = new ImageButton(new TextureRegionDrawable(jumpTex));
        jumpBtn.setSize(btnSize, btnSize);
        jumpBtn.setPosition(screenW - btnSize - 20 * uiScale, btnBottomY);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && !isDead && !showingDialog && !trashMinigame && !shardMinigame && isGrounded) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce);
                    isGrounded = false;
                }
            }
        });

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - 20 * uiScale, screenH - pauseSize - 20 * uiScale);
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

        // НОВАЯ КНОПКА ЗАДАНИЙ
        questBtn = new ImageButton(new TextureRegionDrawable(questTex));
        questBtn.setSize(pauseSize, pauseSize);
        questBtn.setPosition(20 * uiScale, screenH - pauseSize - 20 * uiScale);
        questBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!showingDialog && !trashMinigame && !shardMinigame) {
                    showQuestPanel();
                }
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(game.getScaledSize(80), game.getScaledSize(80));
        interactionBtn.setPosition(screenW / 2 - game.getScaledSize(40), screenH / 2 - game.getScaledSize(50));
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn && !showingDialog && !trashMinigame && !shardMinigame) {
                    if (pendingNPCBody != null) {
                        interactWithNPC();
                    } else if (pendingItemBody != null && pendingItemId != null) {
                        collectItem(pendingItemBody, pendingItemId);
                    } else if (pendingExitBody != null && exitUnlocked) {
                        goToNextLevel();
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
        uiStage.addActor(questBtn);
        uiStage.addActor(interactionBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.smallFont;
        labelStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20 * uiScale, screenH - 50 * uiScale);
        uiStage.addActor(itemsLabel);

        updateItemsLabel();
    }

    private void updateItemsLabel() {
        if (currentLevel == 2) {
            itemsLabel.setText("Запчасти: " + partsCollected + "/3");
        } else {
            itemsLabel.setText("");
        }
    }

    // НОВЫЙ МЕТОД: показать панель заданий
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
        panel.pad(30);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = com.badlogic.gdx.graphics.Color.GOLD;

        Label.LabelStyle textStyle = new Label.LabelStyle();
        textStyle.font = game.font;

        Label title = new Label("ЗАДАНИЯ", titleStyle);

        Table tasks = new Table();

        if (currentLevel == 1) {
            addTaskLine(tasks, "Поговорить с Выжившим", trashCollected > 0 || exitUnlocked);
            addTaskLine(tasks, "Собрать мусор (" + trashCollected + "/3)", trashCollected >= 3);
            addTaskLine(tasks, "Идти к выходу", exitUnlocked);
        } else if (currentLevel == 2) {
            addTaskLine(tasks, "Поговорить с Химиком", talkedToChemist);
            addTaskLine(tasks, "Найти запчасти (" + partsCollected + "/3)", partsCollected >= 3);
            addTaskLine(tasks, "Вернуться к Химику", partsCollected >= 3 && !exitUnlocked);
            addTaskLine(tasks, "Идти к выходу", exitUnlocked);
        } else if (currentLevel == 3) {
            addTaskLine(tasks, "Поговорить с Часовщиком", shardMinigame || shardsClicked > 0);
            addTaskLine(tasks, "Собрать осколки (" + shardsClicked + "/5)", shardsClicked >= 5);
            addTaskLine(tasks, "Идти к выходу", exitUnlocked);
        }

        TextButton closeBtn = new TextButton("ЗАКРЫТЬ", new TextButton.TextButtonStyle() {{
            font = game.font;
        }});
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
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

    private void addTaskLine(Table table, String text, boolean completed) {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.smallFont;
        style.fontColor = completed ? com.badlogic.gdx.graphics.Color.GREEN : com.badlogic.gdx.graphics.Color.WHITE;
        String prefix = completed ? "✓ " : "○ ";
        Label label = new Label(prefix + text, style);
        table.add(label).padBottom(8).left().row();
    }

    // НОВЫЙ МЕТОД: показать диалог
    private void showDialog(String[] lines, Runnable onEnd) {
        showingDialog = true;
        dialogLines = lines;
        dialogIndex = 0;
        dialogCallback = onEnd;
        showDialogLine();
    }

    private void showDialogLine() {
        dialogStage.clear();

        Table bg = new Table();
        bg.setFillParent(true);
        bg.setColor(0, 0, 0, 0.7f);
        dialogStage.addActor(bg);

        Table table = new Table();
        table.setFillParent(true);
        dialogStage.addActor(table);

        Table dialogBox = new Table();
        if (dialogBgTexture != null) {
            dialogBox.setBackground(new TextureRegionDrawable(dialogBgTexture));
        }
        dialogBox.pad(20);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.smallFont;
        style.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

        Label textLabel = new Label(dialogLines[dialogIndex], style);
        textLabel.setWrap(true);

        TextButton nextBtn = new TextButton("ДАЛЕЕ", new TextButton.TextButtonStyle() {{
            font = game.smallFont;
        }});
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialogIndex++;
                if (dialogIndex >= dialogLines.length) {
                    closeDialog();
                } else {
                    showDialogLine();
                }
            }
        });

        dialogBox.add(textLabel).width(500).padBottom(15).row();
        dialogBox.add(nextBtn).width(120).height(45);
        table.add(dialogBox).center().bottom().padBottom(80);
    }

    private void closeDialog() {
        showingDialog = false;
        dialogStage.clear();
        if (dialogCallback != null) {
            dialogCallback.run();
            dialogCallback = null;
        }
        Gdx.input.setInputProcessor(uiStage);
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
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.smallFont;
        Label label = new Label(msg, style);
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
        totalItems = levelItemCount[level - 1];
        collectedItems = 0;
        allItemsCollected = false;
        exitUnlocked = false;
        itemBodies.clear();
        interactiveBodies.clear();
        bodiesToDestroy.clear();
        exitBody = null;

        // Сброс квестовых переменных
        trashMinigame = false;
        trashCollected = 0;
        shardMinigame = false;
        shardsClicked = 0;
        partsCollected = 0;
        talkedToChemist = false;
        trashPos = null;
        binPos = null;
        shardPositions = null;

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
        style.fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        String title = "УРОВЕНЬ " + currentLevel;
        if (currentLevel == 1) title += "\nЗаброшенная лаборатория";
        else if (currentLevel == 2) title += "\nЗона экспериментов";
        else title += "\nХранилище времени";
        Label label = new Label(title, style);
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

                if ((a == playerBody && isGround(b)) || (b == playerBody && isGround(a))) {
                    isGrounded = true;
                }

                if ((a == playerBody && isJumpPad(b)) || (b == playerBody && isJumpPad(a))) {
                    playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, jumpForce * 1.5f);
                    isGrounded = false;
                }

                // Проверка NPC
                if ((a == playerBody && interactiveBodies.containsKey(b)) || (b == playerBody && interactiveBodies.containsKey(a))) {
                    pendingNPCBody = (a == playerBody) ? b : a;
                    pendingNPCName = interactiveBodies.get(pendingNPCBody);
                    pendingDoorId = null;
                    pendingItemBody = null;
                    pendingExitBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }

                // Проверка предметов
                if ((a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) ||
                        (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected"))) {
                    pendingItemBody = (a == playerBody) ? b : a;
                    pendingItemId = itemBodies.get(pendingItemBody);
                    pendingNPCBody = null;
                    pendingExitBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }

                // Проверка выхода
                if (exitBody != null && ((a == playerBody && b == exitBody) || (b == playerBody && a == exitBody))) {
                    if (exitUnlocked) {
                        pendingExitBody = exitBody;
                        pendingNPCBody = null;
                        pendingItemBody = null;
                        showInteractionBtn = true;
                        interactionBtn.setVisible(true);
                    }
                }

                if ((a == playerBody && isKill(b)) || (b == playerBody && isKill(a))) {
                    dieAndRestart();
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if ((a == playerBody && (isGround(b) || interactiveBodies.containsKey(b) || itemBodies.containsKey(b) || (exitBody != null && b == exitBody))) ||
                        (b == playerBody && (isGround(a) || interactiveBodies.containsKey(a) || itemBodies.containsKey(a) || (exitBody != null && a == exitBody)))) {
                    isGrounded = false;
                }

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

    private boolean isGround(Body body) {
        Object data = body.getUserData();
        return data != null && (data.equals("wall") || data.equals("ground"));
    }

    private boolean isJumpPad(Body body) {
        Object data = body.getUserData();
        return data != null && data.equals("jump");
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

        // Выход
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

        // СОЗДАНИЕ NPC И ПРЕДМЕТОВ ПО УРОВНЯМ
        if (currentLevel == 1) {
            // NPC Выживший
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
                    interactiveBodies.put(body, "Выживший");
                }
            }

            // Мусор и корзина
            MapLayer trashLayer = tiledMap.getLayers().get("trash");
            if (trashLayer != null) {
                for (MapObject obj : trashLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) {
                        trashPos = new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                    }
                }
            }
            MapLayer binLayer = tiledMap.getLayers().get("bin");
            if (binLayer != null) {
                for (MapObject obj : binLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) {
                        binPos = new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                    }
                }
            }
        }
        else if (currentLevel == 2) {
            // NPC Химик
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
                    interactiveBodies.put(body, "Химик");
                }
            }

            // Запчасти
            String[] partLayers = {"part1", "part2", "part3"};
            for (String partName : partLayers) {
                MapLayer partLayer = tiledMap.getLayers().get(partName);
                if (partLayer != null) {
                    for (MapObject obj : partLayer.getObjects()) {
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
        }
        else if (currentLevel == 3) {
            // NPC Часовщик
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
                    interactiveBodies.put(body, "Часовщик");
                }
            }

            // Осколки
            shardPositions = new Array<>();
            MapLayer shardsLayer = tiledMap.getLayers().get("shards");
            if (shardsLayer != null) {
                for (MapObject obj : shardsLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect != null) {
                        Vector2 pos = new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        shardPositions.add(pos);
                    }
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

    // НОВЫЙ МЕТОД: взаимодействие с NPC
    private void interactWithNPC() {
        if (currentLevel == 1 && pendingNPCName.equals("Выживший")) {
            if (trashCollected >= 3) {
                showDialog(new String[]{
                        "Выживший: Спасибо! Ты очистила зону от мусора!",
                        "Выживший: Чтобы предотвратить аномалию и закончить эксперимент твоего отца, нужно найти временной объект.",
                        "Выживший: Иди к выходу, он покажет путь. Удачи!"
                }, () -> {
                    exitUnlocked = true;
                    showMessage("Выход открыт!", 1.5f);
                });
            } else {
                showDialog(new String[]{
                        "Выживший: Помоги! Нужно собрать мусор в корзину.",
                        "Выживший: Перетащи мусор пальцем в корзину.",
                        "Нужно собрать 3 штуки."
                }, () -> {
                    startTrashMinigame();
                });
            }
        }
        else if (currentLevel == 2 && pendingNPCName.equals("Химик")) {
            if (partsCollected >= 3) {
                showDialog(new String[]{
                        "Химик: Отлично! Все запчасти на месте!",
                        "Химик: Временной объект, который ты ищешь - это ГИПЕРКУБ.",
                        "Химик: Совсем скоро ты узнаешь, что это. Иди к выходу!"
                }, () -> {
                    exitUnlocked = true;
                    showMessage("Выход открыт!", 1.5f);
                });
            } else if (talkedToChemist) {
                showDialog(new String[]{
                        "Химик: Нужно найти 3 запчасти. Ищи их по лаборатории."
                }, null);
            } else {
                showDialog(new String[]{
                        "Химик: Эксперимент вышел из-под контроля...",
                        "Химик: Чтобы остановить аномалию, найди 3 запчасти.",
                        "Химик: А я расскажу тебе про временной объект."
                }, () -> {
                    talkedToChemist = true;
                    updateItemsLabel();
                });
            }
        }
        else if (currentLevel == 3 && pendingNPCName.equals("Часовщик")) {
            if (shardsClicked >= 5) {
                showDialog(new String[]{
                        "Часовщик: Ты собрала все осколки времени!",
                        "Часовщик: Гиперкуб ждет тебя. Иди к выходу."
                }, () -> {
                    exitUnlocked = true;
                    showMessage("Выход открыт!", 1.5f);
                });
            } else {
                showDialog(new String[]{
                        "Часовщик: Время искажено... Нужно собрать осколки.",
                        "Часовщик: Нажми на все осколки времени. Их 5 штук."
                }, () -> {
                    startShardMinigame();
                });
            }
        }
    }

    private void startTrashMinigame() {
        trashMinigame = true;
        trashCollected = 0;
        showMessage("Собери мусор в корзину! (3 шт)", 2f);
    }

    private void startShardMinigame() {
        shardMinigame = true;
        shardsClicked = 0;
        showMessage("Нажми на все осколки! (5 шт)", 2f);
    }

    private void collectItem(Body itemBody, String itemId) {
        if (currentLevel == 2 && itemId.equals("part")) {
            partsCollected++;
            updateItemsLabel();
            bodiesToDestroy.add(itemBody);
            itemBodies.put(itemBody, "collected");
            showMessage("Найдена запчасть " + partsCollected + "/3", 1f);

            if (partsCollected >= 3) {
                showMessage("Все запчасти собраны! Вернись к Химику.", 2f);
            }
        }

        showInteractionBtn = false;
        interactionBtn.setVisible(false);
        pendingItemBody = null;
        pendingItemId = null;
    }

    private void goToNextLevel() {
        startFade(() -> {
            if (currentLevel < 3) {
                currentLevel++;
                loadLevel(currentLevel);
            } else {
                completeGame();
            }
            isTransitioning = false;
            fading = false;
        });
        isTransitioning = true;
    }

    private void completeGame() {
        showMessage("ГЛАВА 2 ПРОЙДЕНА!\nПРОДОЛЖЕНИЕ СЛЕДУЕТ...", 3f);
        game.prefs.putBoolean("chapter2_completed", true);
        game.prefs.flush();
        game.stopGameMusic();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(new StartMenuScreen(game));
            }
        }, 3);
    }

    private void dieAndRestart() {
        if (isDead) return;
        isDead = true;
        showMessage("ВЫ УМЕРЛИ", 1.5f);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                loadLevel(currentLevel);
                isDead = false;
            }
        }, 1.5f);
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
        if (isPaused || isTransitioning || isDead || showingDialog) return;

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

        // Мини-игры
        if (trashMinigame) updateTrashMinigame(delta);
        if (shardMinigame) updateShardMinigame(delta);

        Vector2 pos = playerBody.getPosition();
        if (pos.y < -2) {
            dieAndRestart();
        }

        camera.position.set(pos.x * PPM, pos.y * PPM, 0);
        camera.update();
    }

    private void updateTrashMinigame(float delta) {
        if (trashPos == null || binPos == null) return;

        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            Vector3 touch3 = camera.unproject(new Vector3(touchX, touchY, 0));
            Vector2 touch = new Vector2(touch3.x / PPM, touch3.y / PPM);

            if (!draggingTrash && touch.dst(trashPos) < 0.5f) {
                draggingTrash = true;
                dragOffset.set(trashPos.x - touch.x, trashPos.y - touch.y);
            }

            if (draggingTrash) {
                trashPos.set(touch.x + dragOffset.x, touch.y + dragOffset.y);
            }
        } else {
            if (draggingTrash && trashPos.dst(binPos) < 0.5f) {
                trashCollected++;
                showMessage("Мусор собран! " + trashCollected + "/3", 1f);

                if (trashCollected >= 3) {
                    trashMinigame = false;
                    showMessage("Мусор собран! Вернись к Выжившему.", 2f);
                } else {
                    // Респавн мусора
                    for (MapLayer layer : tiledMap.getLayers()) {
                        if (layer.getName() != null && layer.getName().equals("trash")) {
                            for (MapObject obj : layer.getObjects()) {
                                Rectangle rect = getObjectRectangle(obj);
                                if (rect != null) {
                                    trashPos = new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                                }
                            }
                        }
                    }
                }
            }
            draggingTrash = false;
        }
    }

    private void updateShardMinigame(float delta) {
        if (shardPositions == null) return;

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            Vector3 touch3 = camera.unproject(new Vector3(touchX, touchY, 0));
            Vector2 touch = new Vector2(touch3.x / PPM, touch3.y / PPM);

            for (int i = 0; i < shardPositions.size; i++) {
                Vector2 pos = shardPositions.get(i);
                if (pos != null && touch.dst(pos) < 0.5f) {
                    shardPositions.set(i, null);
                    shardsClicked++;
                    showMessage("Осколок собран! " + shardsClicked + "/5", 1f);

                    if (shardsClicked >= 5) {
                        shardMinigame = false;
                        showMessage("Все осколки собраны! Вернись к Часовщику.", 2f);
                    }
                    break;
                }
            }
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

    private void drawMinigames() {
        if (trashMinigame && trashTexture != null && trashPos != null) {
            batch.draw(trashTexture, trashPos.x * PPM - 32, trashPos.y * PPM - 32, 64, 64);
        }
        if (trashMinigame && binTexture != null && binPos != null) {
            batch.draw(binTexture, binPos.x * PPM - 32, binPos.y * PPM - 32, 64, 64);
        }
        if (shardMinigame && shardTexture != null && shardPositions != null) {
            for (Vector2 pos : shardPositions) {
                if (pos != null) {
                    batch.draw(shardTexture, pos.x * PPM - 32, pos.y * PPM - 32, 64, 64);
                }
            }
        }
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPlayer();
        drawMinigames();
        batch.end();

        uiStage.act(delta);
        uiStage.draw();
        if (isPaused) pauseStage.draw();
        messageStage.act(delta);
        messageStage.draw();
        if (showingDialog) dialogStage.draw();
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

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        float worldWidth = game.viewport.getWorldWidth() / ZOOM;
        float worldHeight = game.viewport.getWorldHeight() / ZOOM;
        camera.viewportWidth = worldWidth;
        camera.viewportHeight = worldHeight;
        camera.update();

        uiStage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);
        imageStage.getViewport().update(width, height, true);
        dialogStage.getViewport().update(width, height, true);
        questStage.getViewport().update(width, height, true);

        uiScale = game.getUIScale();
        btnSize = game.getScaledSize(100);
        pauseSize = game.getScaledSize(70);
        uiStage.clear();
        createUI();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
        game.startGameMusic();
    }

    @Override
    public void hide() {
        game.stopGameMusic();
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        pauseStage.dispose();
        messageStage.dispose();
        imageStage.dispose();
        dialogStage.dispose();
        questStage.dispose();
        if (fadeTexture != null) fadeTexture.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}