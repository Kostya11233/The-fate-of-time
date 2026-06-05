package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.Chapter1Screen;
import com.mygdx.game.Chapter2Screen;

public class StartMenuScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private Texture buttonTexture;
    private Texture backgroundTexture;
    private TextButton continueBtn;
    private TextButton newGameBtn;
    private TextButton settingsBtn;
    private TextButton socialBtn;
    private TextButton exitBtn;
    private TextButton.TextButtonStyle buttonStyle;
    private GlyphLayout glyphLayout;

    // Адаптивные размеры
    private float uiScale;
    private float btnWidth;
    private float btnHeight;

    public StartMenuScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        this.glyphLayout = new GlyphLayout();

        this.uiScale = game.getUIScale();
        this.btnWidth = 320f * uiScale;
        this.btnHeight = 70f * uiScale;

        loadTextures();
        createStyles();
        createUI();

        game.stopGameMusic();
        game.startMenuMusic();
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
        float w = TheFateGame.VIRTUAL_WIDTH;
        float h = TheFateGame.VIRTUAL_HEIGHT;

        float centerX = (w - btnWidth / uiScale) / 2 * uiScale;
        float startY = h / 2 + 80 * uiScale;
        float stepY = 90f * uiScale;

        // Кнопка ПРОДОЛЖИТЬ
        if (game.hasSaveGame()) {
            continueBtn = new TextButton(game.languageManager.getText("continue"), buttonStyle);
            continueBtn.setSize(btnWidth, btnHeight);
            continueBtn.setPosition(centerX, startY);
            continueBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String savedMap = game.getSavedMap();
                    int savedItems = game.getSavedItems();

                    if (savedMap != null && savedMap.startsWith("lvl")) {
                        game.prefs.putInteger("chapter2_items", savedItems);
                        game.setScreen(new Chapter2Screen(game));
                    } else {
                        game.prefs.putInteger("chapter1_items", savedItems);
                        game.setScreen(new Chapter1Screen(game));
                    }
                }
            });
            stage.addActor(continueBtn);
        }

        // Кнопка НОВАЯ ИГРА
        float newGameY = game.hasSaveGame() ? startY - stepY : startY;
        newGameBtn = new TextButton(game.languageManager.getText("new_game"), buttonStyle);
        newGameBtn.setSize(btnWidth, btnHeight);
        newGameBtn.setPosition(centerX, newGameY);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ChapterSelectScreen(game));
            }
        });
        stage.addActor(newGameBtn);

        // Кнопка НАСТРОЙКИ
        settingsBtn = new TextButton(game.languageManager.getText("settings"), buttonStyle);
        settingsBtn.setSize(btnWidth, btnHeight);
        settingsBtn.setPosition(centerX, newGameY - stepY);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, StartMenuScreen.this));
            }
        });
        stage.addActor(settingsBtn);

        // Кнопка СОЦСЕТИ
        socialBtn = new TextButton(game.languageManager.getText("social"), buttonStyle);
        socialBtn.setSize(btnWidth, btnHeight);
        socialBtn.setPosition(centerX, newGameY - stepY * 2);
        socialBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://t.me/your_channel");
            }
        });
        stage.addActor(socialBtn);

        // Кнопка ВЫХОД
        exitBtn = new TextButton(game.languageManager.getText("exit"), buttonStyle);
        exitBtn.setSize(btnWidth, btnHeight);
        exitBtn.setPosition(centerX, newGameY - stepY * 3);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        stage.addActor(exitBtn);

        // Версия игры
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.smallFont;
        Label versionLabel = new Label("v1.0.0", labelStyle);
        versionLabel.setPosition(w - 80 * uiScale, 20 * uiScale);
        stage.addActor(versionLabel);
    }

    public void refreshButtonTexts() {
        if (continueBtn != null) continueBtn.setText(game.languageManager.getText("continue"));
        if (newGameBtn != null) newGameBtn.setText(game.languageManager.getText("new_game"));
        if (settingsBtn != null) settingsBtn.setText(game.languageManager.getText("settings"));
        if (socialBtn != null) socialBtn.setText(game.languageManager.getText("social"));
        if (exitBtn != null) exitBtn.setText(game.languageManager.getText("exit"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        if (backgroundTexture != null) {
            game.batch.draw(backgroundTexture, 0, 0, TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT);
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
        float w = TheFateGame.VIRTUAL_WIDTH;
        float h = TheFateGame.VIRTUAL_HEIGHT;

        game.titleFont.getData().setScale(0.7f * uiScale);
        glyphLayout.setText(game.titleFont, title);
        float titleWidth = glyphLayout.width;
        float titleX = (w - titleWidth) / 2;
        float titleY = h - 80 * uiScale;

        // Тень
        game.titleFont.setColor(0, 0, 0, 0.5f);
        game.titleFont.draw(game.batch, title, titleX + 3 * uiScale, titleY - 3 * uiScale);
        // Основной текст
        game.titleFont.setColor(1, 0.85f, 0.3f, 1);
        game.titleFont.draw(game.batch, title, titleX, titleY);
        // Сброс цвета
        game.titleFont.setColor(1, 1, 1, 1);
        game.titleFont.getData().setScale(1f);
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);

        // Обновляем адаптивные размеры
        uiScale = game.getUIScale();
        btnWidth = 320f * uiScale;
        btnHeight = 70f * uiScale;

        // Пересоздаем UI
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