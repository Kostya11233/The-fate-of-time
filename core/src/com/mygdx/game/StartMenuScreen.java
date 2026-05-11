package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private MovingBackground background;

    // Кнопки
    private ImageButton startBtn;
    private ImageButton settingsBtn;
    private ImageButton socBtn;
    private ImageButton exitBtn;

    // Текстуры кнопок
    private Texture startTex;
    private Texture settingsTex;
    private Texture socTex;
    private Texture exitTex;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;

        // Используем ExtendViewport — он растягивается без искажений, но без чёрных полос
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
        // Получаем размеры сцены (а не экрана)
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        float btnWidth = 267f;
        float btnHeight = 90f;
        float startX = 67f;
        float startY = h - 140f;  // отступ сверху
        float stepY = 110f;

        // СТАРТ
        startBtn = new ImageButton(new TextureRegionDrawable(startTex));
        startBtn.setPosition(startX, startY);
        startBtn.setSize(btnWidth, btnHeight);
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                System.out.println("Старт - пока не реализовано");
            }
        });

        // НАСТРОЙКИ
        settingsBtn = new ImageButton(new TextureRegionDrawable(settingsTex));
        settingsBtn.setPosition(startX, startY - stepY);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });

        // СОЦСЕТИ
        socBtn = new ImageButton(new TextureRegionDrawable(socTex));
        socBtn.setPosition(startX, startY - stepY * 2);
        socBtn.setSize(btnWidth, btnHeight);
        socBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                System.out.println("Соцсети - пока не реализовано");
            }
        });

        // ВЫХОД
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Обновляем камеру игры
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        // Рисуем фон (на весь экран)
        game.batch.begin();
        background.draw(game.batch, game.camera);
        game.batch.end();

        // Рисуем кнопки
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

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}