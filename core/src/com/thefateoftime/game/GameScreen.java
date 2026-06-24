package com.thefateoftime.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.thefateoftime.TheFateGame;
import com.thefateoftime.config.GameConfig;
import com.thefateoftime.ui.UIManager;

public abstract class GameScreen extends ScreenAdapter {

    protected final TheFateGame game;
    protected final SpriteBatch batch;
    protected OrthographicCamera camera;
    protected TiledMap tiledMap;
    protected OrthogonalTiledMapRenderer tiledMapRenderer;
    protected World world;
    protected Player player;
    protected UIManager uiManager;
    protected ItemManager itemManager;
    protected boolean isPaused;
    protected String currentMap;
    protected boolean allItemsCollected;
    protected int requiredItemsCount;

    public GameScreen(TheFateGame game, String startMap, int requiredItemsCount) {
        this.game = game;
        this.batch = game.batch;

        this.currentMap = startMap;
        this.requiredItemsCount = requiredItemsCount;

        initializeCamera();
        initializeWorld();
        loadMap(startMap);
        initializeUI();

        Gdx.input.setInputProcessor(uiManager.getStage());
    }
    private void initializeCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameConfig.SCREEN_WIDTH / GameConfig.CAMERA_ZOOM, GameConfig.SCREEN_HEIGHT / GameConfig.CAMERA_ZOOM);
        camera.zoom = GameConfig.CAMERA_ZOOM;
    }

    protected void initializeWorld() {

        world = new World(new Vector2(0, GameConfig.GRAVITY_Y), true);
        world.setContactListener(createContactListener());
    }
    protected void initializeUI() {

        uiManager = new UIManager(game, this);
        uiManager.createGameUI();
    }
    protected void loadMap(String mapPath) {

        disposeCurrentMap();

        tiledMap = new TmxMapLoader().load(mapPath);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        currentMap = mapPath;
    }
    private void disposeCurrentMap() {
        tiledMap.dispose();
        tiledMapRenderer.dispose();
    }
    protected ContactListener createContactListener() {
        return new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                onBeginContact(contact);
            }
            @Override
            public void endContact(Contact contact) {
                onEndContact(contact);
            }
            @Override
            public void preSolve(Contact contact, Manifold manifold) {
            }
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        };
    }
    protected void update(float delta) {
        updatePlayer(delta);
        updatePhysics(delta);
        checkPlayerDeath();
        checkItemsCollection();
        updateCamera();
        saveProgress();
    }
    protected void updatePlayer(float delta) {player.update(delta);}

    protected void updatePhysics(float delta) {world.step(delta, GameConfig.VELOCITY_ITERATIONS, GameConfig.POSITION_ITERATIONS);}
    protected void checkPlayerDeath() {if (checkDeath()) {dieAndRestart();}}
    
    protected void checkItemsCollection() {
        if (itemManager.isAllCollected()) {
            allItemsCollected = true;
            onAllItemsCollected();
        }
    }
    protected void updateCamera() {

        camera.position.set(player.getPosition().x * GameConfig.PIXELS_PER_METER, player.getPosition().y * GameConfig.PIXELS_PER_METER, 0);
        camera.update();
    }
    protected void renderMapAndPlayer() {
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
    protected void jump() {if (player.canJump()) {player.jump();}}
    public void handleJumpButton() {jump();}
    public void setPaused(boolean paused) {isPaused = paused;}

    @Override
    public void resize(int width, int height) {uiManager.resize(width, height);}
    @Override
    public void show() {Gdx.input.setInputProcessor(uiManager.getStage());}

    @Override
    public void dispose() {
        uiManager.dispose();
        world.dispose();
        disposeCurrentMap();
    }
    protected abstract void onBeginContact(Contact contact);
    protected abstract void onEndContact(Contact contact);
    protected abstract boolean checkDeath();
    protected abstract void dieAndRestart();
    protected abstract void onAllItemsCollected();
    public abstract void saveProgress();
    public abstract Player getPlayer();
}