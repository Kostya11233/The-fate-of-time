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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Timer;

public abstract class BaseGameScreen implements Screen {
    protected final TheFateGame game;
    protected Stage stage;
    protected Stage pauseStage;
    protected SpriteBatch batch;

    protected OrthographicCamera camera;
    protected TiledMap tiledMap;
    protected OrthogonalTiledMapRenderer tiledMapRenderer;
    protected String currentMap;

    protected int mapWidth;
    protected int mapHeight;

    protected TextureRegion[] walkRightFrames;
    protected TextureRegion[] walkLeftFrames;
    protected TextureRegion standFrame;
    protected TextureRegion standLeftFrame;
    protected Animation<TextureRegion> walkRightAnimation;
    protected Animation<TextureRegion> walkLeftAnimation;
    protected float stateTime;
    protected Vector2 position;
    protected boolean movingRight = false;
    protected boolean movingLeft = false;
    protected float speed = 400f;

    protected ImageButton leftBtn;
    protected ImageButton rightBtn;
    protected ImageButton pauseBtn;
    protected ImageButton inventoryBtn;
    protected ImageButton interactionBtn;

    protected Texture leftTexture;
    protected Texture rightTexture;
    protected Texture pauseTexture;
    protected Texture inventoryTexture;
    protected Texture interactionTexture;

    protected boolean isPaused = false;
    protected boolean isGameOver = false;
    protected boolean showPauseDialog = false;
    protected boolean showSettingsFromPause = false;
    protected boolean showInteractionBtn = false;
    protected boolean isTransitioning = false;
    protected String targetMap;
    protected float targetX;
    protected float targetY;

    protected boolean leftPressed = false;
    protected boolean rightPressed = false;

    protected float playerSize = 250f;
    protected float fixedY = 180;
    protected boolean loadSavedGame;
    protected boolean facingRight = true;
    protected String enteredFromDoor = "door1";

    protected String pendingDoorId = null;
    protected String pendingExitId = null;

    protected String sourceDoorId = null; // запоминаем, через какую дверь вошли на текущую карту
    protected float spawnX = 640; // точка спавна по умолчанию
    private int spawnId;

    public BaseGameScreen(TheFateGame game, boolean loadSavedGame, float startX) {
        this(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    public BaseGameScreen(TheFateGame game, boolean loadSavedGame) {
        this.game = game;
        this.loadSavedGame = loadSavedGame;
        this.batch = game.batch;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        this.pauseStage = new Stage(new ExtendViewport(1280, 720));

        loadTextures();
        createUI();

        if (loadSavedGame && game.prefs.contains("currentMap")) {
            currentMap = game.prefs.getString("currentMap", getDefaultMap());
            enteredFromDoor = game.prefs.getString("enteredFromDoor", "door1");
        } else {
            currentMap = getDefaultMap();
        }

        loadMap(currentMap);
        loadGame();
    }

    protected abstract String getDefaultMap();
    protected abstract void setupDoorTransitions(String doorId);
    protected void setupExitTransitions(String exitId) {}

    protected float readSpawnPoint() {
        if (tiledMap == null) return 640;

        MapLayer spawnLayer = tiledMap.getLayers().get("spawn");
        if (spawnLayer == null) spawnLayer = tiledMap.getLayers().get("spawns");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                String name = obj.getName();
                String type = obj.getProperties().get("type", String.class);
                if (name != null && name.equals("spawn") || (type != null && type.equals("spawn"))) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        return rect.x + rect.width / 2;
                    } else {
                        float x = obj.getProperties().get("x", Float.class);
                        return x;
                    }
                }
            }
        }
        return 640;
    }

    protected void loadTextures() {
        Texture standTexture = new Texture("player/step1.png");
        standFrame = new TextureRegion(standTexture);
        standLeftFrame = new TextureRegion(standTexture);
        standLeftFrame.flip(true, false);

        walkRightFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture walkTexture = new Texture("player/step" + (i + 1) + ".png");
            walkRightFrames[i] = new TextureRegion(walkTexture);
        }
        walkRightAnimation = new Animation<TextureRegion>(0.12f, walkRightFrames);

        walkLeftFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture walkTexture = new Texture("player/step" + (i + 1) + ".png");
            walkLeftFrames[i] = new TextureRegion(walkTexture);
            walkLeftFrames[i].flip(true, false);
        }
        walkLeftAnimation = new Animation<TextureRegion>(0.12f, walkLeftFrames);

        leftTexture = new Texture("button/button_left.png");
        rightTexture = new Texture("button/button_right.png");
        pauseTexture = new Texture("button/button_pause.png");
        inventoryTexture = new Texture("button/button_inventory.png");
        interactionTexture = new Texture("button/button_interaction.png");
    }

    protected void loadMap(String mapPath) {
        try {
            if (tiledMap != null) {
                tiledMap.dispose();
            }
            tiledMap = new TmxMapLoader().load(mapPath);
            mapWidth = tiledMap.getProperties().get("width", Integer.class) * 32;
            mapHeight = tiledMap.getProperties().get("height", Integer.class) * 32;

            camera = new OrthographicCamera();
            camera.setToOrtho(false, 1280, 720);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            System.out.println("Карта загружена: " + mapPath);

            pendingDoorId = null;
            pendingExitId = null;
            showInteractionBtn = false;
            if (interactionBtn != null) interactionBtn.setVisible(false);

            spawnX = readSpawnPoint();
        } catch (Exception e) {
            System.out.println("Ошибка загрузки карты: " + e.getMessage());
        }
    }

    protected void checkInteractions() {
        if (isTransitioning || tiledMap == null) {
            showInteractionBtn = false;
            if (interactionBtn != null) interactionBtn.setVisible(false);
            return;
        }

        boolean wasShowing = showInteractionBtn;
        showInteractionBtn = false;
        pendingDoorId = null;
        pendingExitId = null;

        Rectangle playerRect = new Rectangle(
                position.x - playerSize/2,
                position.y - playerSize/2,
                playerSize,
                playerSize
        );

        // Слой дверей
        MapLayer doorsLayer = tiledMap.getLayers().get("doors");
        if (doorsLayer == null) doorsLayer = tiledMap.getLayers().get("door");
        if (doorsLayer != null) {
            for (MapObject object : doorsLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(object);
                if (rect != null && playerRect.overlaps(rect)) {
                    showInteractionBtn = true;
                    pendingDoorId = object.getName();
                    if (pendingDoorId == null || pendingDoorId.isEmpty()) {
                        pendingDoorId = object.getProperties().get("id", String.class);
                    }
                    System.out.println("ДВЕРЬ НАЙДЕНА! ID: " + pendingDoorId);
                    break;
                }
            }
        }

        // Слой выходов
        if (!showInteractionBtn) {
            MapLayer exitLayer = tiledMap.getLayers().get("exit");
            if (exitLayer == null) exitLayer = tiledMap.getLayers().get("exits");
            if (exitLayer != null) {
                for (MapObject object : exitLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(object);
                    if (rect != null && playerRect.overlaps(rect)) {
                        showInteractionBtn = true;
                        pendingExitId = object.getName();
                        if (pendingExitId == null || pendingExitId.isEmpty()) {
                            pendingExitId = object.getProperties().get("id", String.class);
                        }
                        System.out.println("ВЫХОД НАЙДЕН! ID: " + pendingExitId);
                        break;
                    }
                }
            }
        }

        if (wasShowing != showInteractionBtn && interactionBtn != null) {
            interactionBtn.setVisible(showInteractionBtn);
        }
    }

    // Вспомогательный метод – получает прямоугольник из любого объекта карты
    private Rectangle getObjectRectangle(MapObject object) {
        if (object instanceof RectangleMapObject) {
            return ((RectangleMapObject) object).getRectangle();
        } else {
            // Для точек или полигонов – берём координаты и задаём размер по умолчанию
            Float x = object.getProperties().get("x", Float.class);
            Float y = object.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                Float width = object.getProperties().get("width", Float.class);
                Float height = object.getProperties().get("height", Float.class);
                float w = (width != null) ? width : 64f;
                float h = (height != null) ? height : 64f;
                return new Rectangle(x, y, w, h);
            }
        }
        return null;
    }



    protected void executeTransition() {
        if (targetMap == null || targetMap.equals(currentMap)) {
            System.out.println("Ошибка: targetMap не установлен");
            isTransitioning = false;
            pendingDoorId = null;
            pendingExitId = null;
            return;
        }

        isTransitioning = true;
        showInteractionBtn = false;
        if (interactionBtn != null) interactionBtn.setVisible(false);

        final String nextMap = targetMap;
        final float nextX = targetX;
        final float nextY = targetY;
        final String usedDoorId = (pendingDoorId != null) ? pendingDoorId : pendingExitId;

        System.out.println("Переход: " + currentMap + " -> " + nextMap + " на X=" + nextX + " через " + usedDoorId);

        pendingDoorId = null;
        pendingExitId = null;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                BaseGameScreen newScreen = null;

                // Используем конструкторы с startX для всех экранов
                if (nextMap.equals("cormap/corid1.tmx")) {
                    newScreen = new Corridor1Screen(game, false, nextX);
                } else if (nextMap.equals("cormap/corid2.tmx")) {
                    newScreen = new Corridor2Screen(game, false, nextX);
                } else if (nextMap.equals("cormap/corid3.tmx")) {
                    newScreen = new Corridor3Screen(game, false, nextX);
                } else if (nextMap.equals("cormap/corid4.tmx")) {
                    newScreen = new Corridor4Screen(game, false, nextX);
                } else if (nextMap.equals("room/k1.tmx")) {
                    newScreen = new K1RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k2.tmx")) {
                    newScreen = new K2RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k3.tmx")) {
                    newScreen = new K3RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k4.tmx")) {
                    newScreen = new K4RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k5.tmx")) {
                    newScreen = new K5RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k6.tmx")) {
                    newScreen = new K6RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k7.tmx")) {
                    newScreen = new K7RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k8.tmx")) {
                    newScreen = new K8RoomScreen(game, false, nextX);
                } else if (nextMap.equals("room/k9.tmx")) {
                    newScreen = new K9RoomScreen(game, false, nextX);
                } else {
                    System.out.println("Неизвестная карта: " + nextMap + ", используем Corridor1Screen");
                    newScreen = new Corridor1Screen(game, false, nextX);
                }

                if (newScreen != null) {
                    if (usedDoorId != null) newScreen.sourceDoorId = usedDoorId;
                    // Позиция уже установлена в конструкторе через startX, дополнительный setPosition не нужен
                    game.setScreen(newScreen);
                }
            }
        }, 0.2f);
    }
    public void setPosition(float x, float y) {
        this.position = new Vector2(x, y);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
            camera.update();
        }
    }

    protected String lastUsedDoorId = null;
    protected float lastDoorX = 640;

    protected void createUI() {
        leftBtn = new ImageButton(new TextureRegionDrawable(leftTexture));
        leftBtn.setPosition(50, 50);
        leftBtn.setSize(80, 80);
        leftBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!isPaused && !isTransitioning) {
                    leftPressed = true;
                    rightPressed = false;
                    facingRight = false;
                }
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                leftPressed = false;
            }
        });

        rightBtn = new ImageButton(new TextureRegionDrawable(rightTexture));
        rightBtn.setPosition(150, 50);
        rightBtn.setSize(80, 80);
        rightBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!isPaused && !isTransitioning) {
                    rightPressed = true;
                    leftPressed = false;
                    facingRight = true;
                }
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                rightPressed = false;
            }
        });

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTexture));
        pauseBtn.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 100);
        pauseBtn.setSize(60, 60);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isGameOver && !isTransitioning) {
                    showPauseDialog = true;
                    isPaused = true;
                    createPauseDialog();
                }
            }
        });

        inventoryBtn = new ImageButton(new TextureRegionDrawable(inventoryTexture));
        inventoryBtn.setPosition(Gdx.graphics.getWidth() - 100, 50);
        inventoryBtn.setSize(60, 60);
        inventoryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Инвентарь");
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTexture));
        interactionBtn.setPosition(Gdx.graphics.getWidth() / 2 - 40, Gdx.graphics.getHeight() / 2 - 100);
        interactionBtn.setSize(80, 80);
        interactionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn) {
                    System.out.println("Кнопка нажата!");
                    if (pendingDoorId != null) {
                        System.out.println("Переход через дверь: " + pendingDoorId);
                        setupDoorTransitions(pendingDoorId);
                        executeTransition();
                    } else if (pendingExitId != null) {
                        System.out.println("Выход: " + pendingExitId);
                        setupExitTransitions(pendingExitId);
                        executeTransition();
                    }
                }
            }
        });

        stage.addActor(leftBtn);
        stage.addActor(rightBtn);
        stage.addActor(pauseBtn);
        stage.addActor(inventoryBtn);
        stage.addActor(interactionBtn);
        interactionBtn.setVisible(false);
    }

    protected void createPauseDialog() {
        pauseStage.clear();
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        pauseStage.addActor(darkBg);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Table dialogTable = new Table();
        dialogTable.pad(30);
        Label titleLabel = new Label("ПАУЗА", labelStyle);
        titleLabel.setFontScale(2f);
        TextButton continueBtn = new TextButton("ПРОДОЛЖИТЬ", buttonStyle);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPauseDialog = false;
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(stage);
            }
        });
        TextButton saveAndExitBtn = new TextButton("СОХРАНИТЬ И ВЫЙТИ", buttonStyle);
        saveAndExitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveGame();
                game.setScreen(new StartMenuScreen(game));
            }
        });
        TextButton settingsBtn = new TextButton("НАСТРОЙКИ", buttonStyle);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSettingsFromPause = true;
                pauseStage.clear();
                createSettingsInPause();
            }
        });
        TextButton exitBtn = new TextButton("ВЫЙТИ В МЕНЮ", buttonStyle);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new StartMenuScreen(game));
            }
        });
        dialogTable.add(titleLabel).padBottom(40).row();
        dialogTable.add(continueBtn).width(250).height(60).padBottom(20).row();
        dialogTable.add(saveAndExitBtn).width(250).height(60).padBottom(20).row();
        dialogTable.add(settingsBtn).width(250).height(60).padBottom(20).row();
        dialogTable.add(exitBtn).width(250).height(60).row();
        table.add(dialogTable).center();
    }

    protected void createSettingsInPause() {
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        pauseStage.addActor(darkBg);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Table settingsTable = new Table();
        settingsTable.pad(30);
        Label titleLabel = new Label("НАСТРОЙКИ", labelStyle);
        titleLabel.setFontScale(2f);
        final Label volumeLabel = new Label("ГРОМКОСТЬ: " + (int)(game.volume * 100) + "%", labelStyle);
        Table volumeControls = new Table();
        TextButton minusBtn = new TextButton("-", buttonStyle);
        minusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.volume = Math.max(0, game.volume - 0.1f);
                volumeLabel.setText("ГРОМКОСТЬ: " + (int)(game.volume * 100) + "%");
                if (game.menuMusic != null) game.menuMusic.setVolume(game.volume);
                game.saveSettings();
            }
        });
        TextButton plusBtn = new TextButton("+", buttonStyle);
        plusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.volume = Math.min(1, game.volume + 0.1f);
                volumeLabel.setText("ГРОМКОСТЬ: " + (int)(game.volume * 100) + "%");
                if (game.menuMusic != null) game.menuMusic.setVolume(game.volume);
                game.saveSettings();
            }
        });
        volumeControls.add(minusBtn).width(60).height(50).padRight(20);
        volumeControls.add(plusBtn).width(60).height(50);
        TextButton backBtn = new TextButton("НАЗАД", buttonStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createPauseDialog();
            }
        });
        settingsTable.add(titleLabel).padBottom(40).row();
        settingsTable.add(volumeLabel).padBottom(20).row();
        settingsTable.add(volumeControls).padBottom(40).row();
        settingsTable.add(backBtn).width(200).height(60).row();
        table.add(settingsTable).center();
    }

    protected void saveGame() {
        game.prefs.putString("currentMap", currentMap);
        game.prefs.putFloat("playerX", position.x);
        game.prefs.putFloat("playerY", position.y);
        game.prefs.putBoolean("facingRight", facingRight);
        game.prefs.putString("enteredFromDoor", enteredFromDoor);
        game.prefs.flush();
        System.out.println("Игра сохранена!");
    }

    protected void loadGame() {
        if (loadSavedGame && game.prefs.contains("playerX")) {
            float savedX = game.prefs.getFloat("playerX", spawnX);
            position = new Vector2(savedX, fixedY);
            facingRight = game.prefs.getBoolean("facingRight", true);
            System.out.println("Загружено сохранение, позиция X=" + savedX);
        } else {
            // Новая игра — используем точку спавна из карты
            position = new Vector2(spawnX, fixedY);
            facingRight = true;
            System.out.println("Новая игра, спавн на X=" + spawnX);
        }
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
            camera.update();
        }
    }

    protected void update(float delta) {
        if (isPaused || isGameOver || isTransitioning) return;

        movingRight = rightPressed;
        movingLeft = leftPressed;

        if (movingRight) {
            position.x += speed * delta;
            stateTime += delta;
        } else if (movingLeft) {
            position.x -= speed * delta;
            stateTime += delta;
        } else {
            stateTime = 0;
        }

        float halfSize = playerSize / 2;
        position.x = Math.max(halfSize, Math.min(position.x, mapWidth - halfSize));
        position.y = fixedY;

        float cameraSpeed = 8f;
        float targetX = position.x;
        float currentX = camera.position.x;
        float newX = currentX + (targetX - currentX) * cameraSpeed * delta;
        camera.position.set(newX, 360, 0);
        camera.update();

        checkInteractions();
    }

    protected void drawPlayer() {
        TextureRegion currentFrame;
        if (movingRight) {
            currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
            facingRight = true;
        } else if (movingLeft) {
            currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
            facingRight = false;
        } else {
            if (facingRight) currentFrame = standFrame;
            else currentFrame = standLeftFrame;
        }
        batch.draw(currentFrame, position.x - playerSize/2, position.y - playerSize/2, playerSize, playerSize);
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
        batch.end();
        stage.act(delta);
        stage.draw();
        if (showPauseDialog) {
            pauseStage.act(delta);
            pauseStage.draw();
            Gdx.input.setInputProcessor(pauseStage);
        } else {
            Gdx.input.setInputProcessor(stage);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        if (pauseBtn != null) pauseBtn.setPosition(width - 100, height - 100);
        if (inventoryBtn != null) inventoryBtn.setPosition(width - 100, 50);
        if (interactionBtn != null) interactionBtn.setPosition(width / 2 - 40, height / 2 - 100);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
        pauseStage.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
        leftTexture.dispose();
        rightTexture.dispose();
        pauseTexture.dispose();
        inventoryTexture.dispose();
        interactionTexture.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
    protected float readSpawnPointById(int spawnId) {
        if (tiledMap == null) return 640;
        MapLayer spawnLayer = tiledMap.getLayers().get("spawn");
        if (spawnLayer == null) spawnLayer = tiledMap.getLayers().get("spawns");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                Integer id = obj.getProperties().get("id", Integer.class);
                if (id == null) {
                    String idStr = obj.getProperties().get("id", String.class);
                    if (idStr != null) {
                        try { id = Integer.parseInt(idStr); } catch(NumberFormatException e) {}
                    }
                }
                if (id != null && id == spawnId) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        return rect.x + rect.width / 2;
                    } else {
                        Float x = obj.getProperties().get("x", Float.class);
                        if (x != null) return x;
                    }
                }
            }
        }
        return 640;
    }
}