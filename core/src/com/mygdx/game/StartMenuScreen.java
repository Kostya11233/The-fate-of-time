package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private MovingBackground background;
    private boolean isLoading = false;

    private ImageButton startBtn;
    private ImageButton settingsBtn;
    private ImageButton socBtn;
    private ImageButton exitBtn;

    private Texture startTex;
    private Texture settingsTex;
    private Texture socTex;
    private Texture exitTex;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);
        background = new MovingBackground();
        loadTextures();
        createButtons();
    }

    private void loadTextures() {
        startTex = new Texture("start_ru.jpg");
        settingsTex = new Texture("settings_ru.png");
        socTex = new Texture("soc_ru.jpg");
        exitTex = new Texture("exit_ru.png");
    }

    private void createButtons() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        float btnWidth = 400f;
        float btnHeight = 120f;
        float startX = 80f;
        float startY = h - 160f;
        float stepY = 140f;

        startBtn = new ImageButton(new TextureRegionDrawable(startTex));
        startBtn.setPosition(startX, startY);
        startBtn.setSize(btnWidth, btnHeight);
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (!isLoading) {
                    startGameWithDelay();
                }
            }
        });

        settingsBtn = new ImageButton(new TextureRegionDrawable(settingsTex));
        settingsBtn.setPosition(startX, startY - stepY);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });

        socBtn = new ImageButton(new TextureRegionDrawable(socTex));
        socBtn.setPosition(startX, startY - stepY * 2);
        socBtn.setSize(btnWidth, btnHeight);
        socBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                System.out.println("Соцсети - пока не реализовано");
            }
        });

        exitBtn = new ImageButton(new TextureRegionDrawable(exitTex));
        exitBtn.setPosition(startX, startY - stepY * 3);
        exitBtn.setSize(btnWidth, btnHeight);
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        stage.addActor(startBtn);
        stage.addActor(settingsBtn);
        stage.addActor(socBtn);
        stage.addActor(exitBtn);
    }

    private void startGameWithDelay() {
        isLoading = true;
        System.out.println("Загрузка...");

        startBtn.setDisabled(true);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                System.out.println("Загрузка завершена! (Здесь будет карта)");
                isLoading = false;
                startBtn.setDisabled(false);
            }
        }, 2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        background.draw(game.batch, game.camera, delta);
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        game.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        startTex.dispose();
        settingsTex.dispose();
        socTex.dispose();
        exitTex.dispose();
    }

    @Override
    public void show() {
        // Включаем музыку когда появляется меню (после заставки)
        if (game.menuMusic != null && game.musicEnabled && !game.menuMusic.isPlaying()) {
            game.menuMusic.play();
            game.menuMusic.setVolume(game.volume);
            System.out.println("Музыка запущена, громкость: " + game.volume);
        }
    }

    @Override
    public void hide() {
        // Паузим музыку когда уходим из меню
        if (game.menuMusic != null && game.menuMusic.isPlaying()) {
            game.menuMusic.pause();
            System.out.println("Музыка на паузе");
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
}