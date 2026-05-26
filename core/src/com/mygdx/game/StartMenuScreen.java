package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private Texture buttonTexture;
    private Texture backgroundTexture;
    private TextButton continueBtn;
    private TextButton startBtn;
    private TextButton settingsBtn;
    private TextButton socBtn;
    private TextButton exitBtn;
    private TextButton.TextButtonStyle buttonStyle;
    private GlyphLayout glyphLayout;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
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
            backgroundTexture = new Texture("back.png");
        } catch (Exception e) {
            backgroundTexture = null;
        }
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

        float startY = h / 2 + 30;
        float stepY = 80f;

        if (game.hasSaveGame()) {
            continueBtn = new TextButton(game.languageManager.getText("continue"), buttonStyle);
            continueBtn.setPosition(centerX, startY);
            continueBtn.setSize(btnWidth, btnHeight);
            continueBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String savedMap = game.getSavedMap();
                    int savedItems = game.getSavedItems();
                    game.prefs.putInteger("chapter1_items", savedItems);
                    game.prefs.flush();
                    game.setScreen(new Chapter1Screen(game, savedMap, "spawn"));
                }
            });
            stage.addActor(continueBtn);
        }

        startBtn = new TextButton(game.languageManager.getText("new_game"), buttonStyle);
        float newGameY = game.hasSaveGame() ? startY - stepY : startY;
        startBtn.setPosition(centerX, newGameY);
        startBtn.setSize(btnWidth, btnHeight);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.clearSave();
                game.prefs.putInteger("chapter1_items", 0);
                game.prefs.flush();
                game.setScreen(new Chapter1Screen(game, "room3.tmx", "spawn"));
            }
        });

        settingsBtn = new TextButton(game.languageManager.getText("settings"), buttonStyle);
        settingsBtn.setPosition(centerX, newGameY - stepY);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });

        socBtn = new TextButton(game.languageManager.getText("social"), buttonStyle);
        socBtn.setPosition(centerX, newGameY - stepY * 2);
        socBtn.setSize(btnWidth, btnHeight);
        socBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        exitBtn = new TextButton(game.languageManager.getText("exit"), buttonStyle);
        exitBtn.setPosition(centerX, newGameY - stepY * 3);
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
        if (continueBtn != null) continueBtn.setText(game.languageManager.getText("continue"));
        if (startBtn != null) startBtn.setText(game.languageManager.getText("new_game"));
        if (settingsBtn != null) settingsBtn.setText(game.languageManager.getText("settings"));
        if (socBtn != null) socBtn.setText(game.languageManager.getText("social"));
        if (exitBtn != null) exitBtn.setText(game.languageManager.getText("exit"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        if (backgroundTexture != null) {
            game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } else {
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        game.batch.end();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
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
        if (buttonTexture != null) buttonTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        refreshButtonTexts();
        game.stopGameMusic();
        game.startMenuMusic();
    }

    @Override
    public void hide() {
        game.stopMenuMusic();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}