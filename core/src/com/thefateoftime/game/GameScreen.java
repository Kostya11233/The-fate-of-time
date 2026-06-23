package com.thefateoftime.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.thefateoftime.TheFateGame;
import com.thefateoftime.ui.UIManager;

public abstract class GameScreen implements Screen {
    protected final TheFateGame game;
    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    protected TiledMap tiledMap;
    protected OrthogonalTiledMapRenderer tiledMapRenderer;
    protected World world;
    protected Player player;
    protected UIManager uiManager;
    protected MapManager mapManager;
    protected ItemManager itemManager;

    protected boolean isTransitioning = false;
    protected boolean isPaused = false;
    protected boolean isDead = false;

    protected static final float PPM = 32f;
    protected static final float ZOOM = 3.0f;

    protected String currentMap;
    protected boolean allItemsCollected = false;
    protected int requiredItemsCount;

    public GameScreen(TheFateGame game, String startMap, int requiredItems) {
        this.game = game;
        this.currentMap = startMap;
        this.requiredItemsCount = requiredItems;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280 / ZOOM, 720 / ZOOM);
        this.camera.zoom = ZOOM;

        createWorld();
        loadMap(startMap);
        createUI();

        Gdx.input.setInputProcessor(uiManager.getStage());
    }

    protected void createWorld() {
        world = new World(new Vector2(0, -20f), true); // Гравитация для прыжков
        world.setContactListener(createContactListener());
    }

    protected ContactListener createContactListener() {
        return new ContactListener() {
            @Override public void beginContact(Contact contact) { onBeginContact(contact); }
            @Override public void endContact(Contact contact) { onEndContact(contact); }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        };
    }

    protected abstract void onBeginContact(Contact contact);
    protected abstract void onEndContact(Contact contact);

    protected void loadMap(String mapPath) {
        if (tiledMap != null) tiledMap.dispose();
        tiledMap = new TmxMapLoader().load(mapPath);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        currentMap = mapPath;
    }

    protected void createUI() {
        uiManager = new UIManager(game, this);
        uiManager.createGameUI();
    }

    protected void update(float delta) {
        if (isPaused || isTransitioning || isDead) return;

        player.update(delta);
        world.step(delta, 6, 2);

        // Проверка на смерть от ловушек
        if (checkDeath()) {
            dieAndRestart();
        }

        // Проверка сбора всех предметов
        if (!allItemsCollected && itemManager != null && itemManager.isAllCollected()) {
            allItemsCollected = true;
            onAllItemsCollected();
        }

        camera.position.set(player.getPosition().x * PPM, player.getPosition().y * PPM, 0);
        camera.update();

        saveProgress();
    }

    protected abstract boolean checkDeath();
    protected abstract void dieAndRestart();
    protected abstract void onAllItemsCollected();
    public abstract void saveProgress();

    protected void renderMapAndPlayer() {
        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        batch.end();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderMapAndPlayer();

        uiManager.actAndDraw(delta);
    }

    public void restartCurrentMap() {
        isDead = false;
        isTransitioning = true;

        // Перезагружаем карту
        loadMap(currentMap);
        recreateWorld();
        player.reset(findSpawnPosition("start"));
        itemManager.reset();
        allItemsCollected = false;

        isTransitioning = false;
    }

    protected void recreateWorld() {
        world.dispose();
        createWorld();
    }

    protected Vector2 findSpawnPosition(String spawnId) {
        // Поиск спавна на карте
        return new Vector2(640 / PPM, 360 / PPM);
    }

    protected void jump() {
        if (player.canJump()) {
            player.jump();
        }
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void handleJumpButton() {
        jump();
    }

    @Override public void resize(int w, int h) { uiManager.resize(w, h); }
    @Override public void dispose() {
        uiManager.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
    @Override public void show() { Gdx.input.setInputProcessor(uiManager.getStage()); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    public abstract UIManager getUIManager();

    public abstract Player getPlayer();
}