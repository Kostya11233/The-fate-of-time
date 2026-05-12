package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private MovingBackground background;

    // Фоновая текстура для кнопок
    private Texture buttonTexture;

    // Кнопки
    private TextButton startBtn;
    private TextButton settingsBtn;
    private TextButton socBtn;
    private TextButton exitBtn;

    // Стили
    private TextButton.TextButtonStyle buttonStyle;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        background = new MovingBackground();

        // Загружаем текстуру кнопки
        loadTextures();
        createStyles();
        createButtons();
    }

    private void loadTextures() {
        try {
            buttonTexture = new Texture("button.png");
        } catch (Exception e) {
            System.out.println("Файл button.png не найден, создаю заглушку");
            // Создаем простую белую текстуру как заглушку
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.3f, 0.3f, 0.5f, 1);
            pixmap.fill();
            buttonTexture = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    private void createStyles() {
        // Стиль для кнопок с фоновой текстурой
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;
        buttonStyle.up = new TextureRegionDrawable(buttonTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonTexture);
        buttonStyle.over = new TextureRegionDrawable(buttonTexture);
    }

    private void createButtons() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        float btnWidth = 350f;
        float btnHeight = 80f;
        float centerX = (w - btnWidth) / 2;
        float startY = h - 180f;
        float stepY = 110f;

        // Кнопка СТАРТ
        startBtn = new TextButton(game.languageManager.getText("start"), buttonStyle);
        startBtn.setPosition(centerX, startY);
        startBtn.setSize(btnWidth, btnHeight);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ChoiceDialog(game, StartMenuScreen.this));
            }
        });

        // Кнопка НАСТРОЙКИ
        settingsBtn = new TextButton(game.languageManager.getText("settings"), buttonStyle);
        settingsBtn.setPosition(centerX, startY - stepY);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });

        // Кнопка СОЦСЕТИ
        socBtn = new TextButton(game.languageManager.getText("social"), buttonStyle);
        socBtn.setPosition(centerX, startY - stepY * 2);
        socBtn.setSize(btnWidth, btnHeight);
        socBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Соцсети - пока не реализовано");
            }
        });

        // Кнопка ВЫХОД
        exitBtn = new TextButton(game.languageManager.getText("exit"), buttonStyle);
        exitBtn.setPosition(centerX, startY - stepY * 3);
        exitBtn.setSize(btnWidth, btnHeight);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        stage.addActor(startBtn);
        stage.addActor(settingsBtn);
        stage.addActor(socBtn);
        stage.addActor(exitBtn);
    }

    void refreshButtonTexts() {
        // Обновляем текст на кнопках при смене языка
        startBtn.setText(game.languageManager.getText("start"));
        settingsBtn.setText(game.languageManager.getText("settings"));
        socBtn.setText(game.languageManager.getText("social"));
        exitBtn.setText(game.languageManager.getText("exit"));
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

        // Пересоздаем кнопки при изменении размера экрана
        stage.clear();
        createButtons();
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        refreshButtonTexts(); // Обновляем текст при показе
        if (game.menuMusic != null && game.musicEnabled && !game.menuMusic.isPlaying()) {
            game.menuMusic.play();
            game.menuMusic.setVolume(game.volume);
        }
    }

    @Override
    public void hide() {
        if (game.menuMusic != null && game.menuMusic.isPlaying()) {
            game.menuMusic.pause();
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
}