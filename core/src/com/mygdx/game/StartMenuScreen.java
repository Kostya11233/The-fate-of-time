package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private MovingBackground background;

    private Texture buttonTexture;
    private Texture doshirakTexture;
    private Image doshirakImage;

    private TextButton startBtn;
    private TextButton settingsBtn;
    private TextButton socBtn;
    private TextButton exitBtn;

    private TextButton.TextButtonStyle buttonStyle;
    private GlyphLayout glyphLayout;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        background = new MovingBackground();
        glyphLayout = new GlyphLayout();

        loadTextures();
        createStyles();
        createUI();
    }

    private void loadTextures() {
        try {
            buttonTexture = new Texture("button.png");
        } catch (Exception e) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.3f, 0.3f, 0.5f, 1);
            pixmap.fill();
            buttonTexture = new Texture(pixmap);
            pixmap.dispose();
        }

        try {
            doshirakTexture = new Texture("doshirak.png");
        } catch (Exception e) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(100, 100, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.8f, 0.5f, 0.2f, 1);
            pixmap.fill();
            doshirakTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        doshirakImage = new Image(doshirakTexture);
    }

    private void createStyles() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;
        buttonStyle.up = new TextureRegionDrawable(buttonTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonTexture);
        buttonStyle.over = new TextureRegionDrawable(buttonTexture);
    }

    private void createUI() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        float btnWidth = 300f;
        float btnHeight = 65f;
        float centerX = (w - btnWidth) / 2;

        float imageSize = 260f;
        float imageY = h / 2 + 70;
        float startY = imageY - imageSize/2 - 75;
        float stepY = 80f;

        doshirakImage.setSize(imageSize, imageSize);
        doshirakImage.setPosition((w - imageSize) / 2, imageY - imageSize/2);
        stage.addActor(doshirakImage);

        startBtn = new TextButton(game.languageManager.getText("start"), buttonStyle);
        startBtn.setPosition(centerX, startY);
        startBtn.setSize(btnWidth, btnHeight);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Показываем диалог выбора (Новая игра / Продолжить)
                game.setScreen(new ChoiceDialog(game, StartMenuScreen.this));
            }
        });

        settingsBtn = new TextButton(game.languageManager.getText("settings"), buttonStyle);
        settingsBtn.setPosition(centerX, startY - stepY);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });

        socBtn = new TextButton(game.languageManager.getText("social"), buttonStyle);
        socBtn.setPosition(centerX, startY - stepY * 2);
        socBtn.setSize(btnWidth, btnHeight);
        socBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Соцсети - пока не реализовано");
            }
        });

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

    public void refreshButtonTexts() {
        if (startBtn != null) startBtn.setText(game.languageManager.getText("start"));
        if (settingsBtn != null) settingsBtn.setText(game.languageManager.getText("settings"));
        if (socBtn != null) socBtn.setText(game.languageManager.getText("social"));
        if (exitBtn != null) exitBtn.setText(game.languageManager.getText("exit"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        background.draw(game.batch, game.camera, delta);
        drawTitle();
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawTitle() {
        String title = "THE FATE OF TIME";
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        game.titleFont.getData().setScale(0.65f);
        glyphLayout.setText(game.titleFont, title);
        float titleWidth = glyphLayout.width;
        float titleX = (w - titleWidth) / 2;
        float titleY = h - 65;

        game.titleFont.setColor(0, 0, 0, 0.5f);
        game.titleFont.draw(game.batch, title, titleX + 2, titleY - 2);
        game.titleFont.setColor(1, 0.85f, 0.3f, 1);
        game.titleFont.draw(game.batch, title, titleX, titleY);
        game.titleFont.setColor(1, 0.95f, 0.6f, 0.3f);
        game.titleFont.draw(game.batch, title, titleX - 1, titleY + 1);
        game.titleFont.setColor(1, 1, 1, 1);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        game.resize(width, height);
        stage.clear();
        createUI();
        refreshButtonTexts();
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        if (buttonTexture != null) buttonTexture.dispose();
        if (doshirakTexture != null) doshirakTexture.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        refreshButtonTexts();
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