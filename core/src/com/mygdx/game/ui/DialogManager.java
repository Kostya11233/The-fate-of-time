package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.game.GameScreen;

public class DialogManager {
    private TheFateGame game;
    private Stage dialogStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage imageStage;

    private Texture currentImageTexture;
    private boolean showingImage = false;
    private Runnable onDialogClosed;

    public DialogManager(TheFateGame game) {
        this.game = game;
        this.dialogStage = new Stage(new ExtendViewport(1280, 720));
        this.pauseStage = new Stage(new ExtendViewport(1280, 720));
        this.messageStage = new Stage(new ExtendViewport(1280, 720));
        this.imageStage = new Stage(new ExtendViewport(1280, 720));
    }

    public void showPauseDialog(final GameScreen gameScreen) {
        pauseStage.clear();

        // Темный фон
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
        dialog.setBackground(getDialogBackground());

        Label title = new Label(game.languageManager.getText("game_paused"), labelStyle);
        title.setFontScale(2f);

        TextButton continueBtn = new TextButton(game.languageManager.getText("continue"), buttonStyle);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) {
                gameScreen.setPaused(false);
                pauseStage.clear();
                Gdx.input.setInputProcessor(gameScreen.getUIManager().getStage());
                if (game.gameMusic != null && game.musicEnabled) game.gameMusic.play();
            }
        });

        TextButton restartBtn = new TextButton(game.languageManager.getText("restart_level"), buttonStyle);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) {
                gameScreen.restartCurrentMap();
                gameScreen.setPaused(false);
                pauseStage.clear();
                Gdx.input.setInputProcessor(gameScreen.getUIManager().getStage());
            }
        });

        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_to_menu"), buttonStyle);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent e, float x, float y) {
                gameScreen.saveProgress();
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new com.mygdx.game.screens.StartMenuScreen(game));
            }
        });

        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(250).height(60).padBottom(20).row();
        dialog.add(restartBtn).width(250).height(60).padBottom(20).row();
        dialog.add(exitBtn).width(250).height(60).row();

        table.add(dialog).center();

        Gdx.input.setInputProcessor(pauseStage);
    }

    public void showDeathDialog(final Runnable onRestart) {
        messageStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.85f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = Color.RED;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        Label titleLabel = new Label(game.languageManager.getText("you_died"), titleStyle);
        titleLabel.setFontScale(2f);

        Label messageLabel = new Label(game.languageManager.getText("restarting_level"), labelStyle);

        table.add(titleLabel).padBottom(30).row();
        table.add(messageLabel).row();

        Gdx.input.setInputProcessor(messageStage);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                onRestart.run();
                Gdx.input.setInputProcessor(null);
            }
        }, 2);
    }

    public void showLevelCompleteDialog(int levelNum, final Runnable onNextLevel) {
        messageStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.85f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = Color.GOLD;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        Label titleLabel = new Label(game.languageManager.format("level_complete", levelNum), titleStyle);
        titleLabel.setFontScale(1.8f);

        Label messageLabel = new Label(game.languageManager.getText("loading_next_level"), labelStyle);

        table.add(titleLabel).padBottom(30).row();
        table.add(messageLabel).row();

        Gdx.input.setInputProcessor(messageStage);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                onNextLevel.run();
            }
        }, 2);
    }

    public void showChapterCompleteDialog(int chapterNum, boolean isLastChapter, final Runnable onFinish) {
        messageStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = Color.GOLD;

        Label.LabelStyle subtitleStyle = new Label.LabelStyle();
        subtitleStyle.font = game.font;

        Label titleLabel;
        if (isLastChapter) {
            titleLabel = new Label(game.languageManager.getText("game_complete"), titleStyle);
        } else {
            titleLabel = new Label(game.languageManager.format("chapter_complete", chapterNum), titleStyle);
        }
        titleLabel.setFontScale(1.8f);

        Label subtitleLabel = new Label(game.languageManager.getText("returning_to_menu"), subtitleStyle);

        table.add(titleLabel).padBottom(30).row();
        table.add(subtitleLabel).row();

        Gdx.input.setInputProcessor(messageStage);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                onFinish.run();
            }
        }, 3);
    }

    public void showAllItemsCollectedMessage() {
        messageStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.8f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.titleFont;
        labelStyle.fontColor = Color.GREEN;

        Label label = new Label(game.languageManager.getText("all_items_collected"), labelStyle);
        label.setFontScale(1.5f);

        table.add(label).center();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
            }
        }, 2);
    }

    public void showImage(String imagePath, Runnable onClose) {
        try {
            if (currentImageTexture != null) {
                currentImageTexture.dispose();
            }
            currentImageTexture = new Texture(imagePath);
            showingImage = true;
            this.onDialogClosed = onClose;

            imageStage.clear();

            Table darkBg = new Table();
            darkBg.setFillParent(true);
            darkBg.setColor(0, 0, 0, 0.85f);
            imageStage.addActor(darkBg);

            Table table = new Table();
            table.setFillParent(true);
            imageStage.addActor(table);

            Image displayedImage = new Image(currentImageTexture);
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();
            float imgWidth = currentImageTexture.getWidth();
            float imgHeight = currentImageTexture.getHeight();

            float scale = Math.min(screenW * 0.8f / imgWidth, screenH * 0.8f / imgHeight);
            displayedImage.setSize(imgWidth * scale, imgHeight * scale);
            table.add(displayedImage).center();

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = game.font;
            Label continueLabel = new Label(game.languageManager.getText("tap_to_continue"), labelStyle);
            continueLabel.setFontScale(1.2f);
            table.add(continueLabel).padTop(30).center();

            imageStage.addListener(new ClickListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    hideImage();
                    return true;
                }
            });

            Gdx.input.setInputProcessor(imageStage);

        } catch (Exception e) {
            onClose.run();
        }
    }

    public void hideImage() {
        showingImage = false;
        imageStage.clear();
        if (currentImageTexture != null) {
            currentImageTexture.dispose();
            currentImageTexture = null;
        }
        if (onDialogClosed != null) {
            onDialogClosed.run();
            onDialogClosed = null;
        }
    }

    public void showMessage(String message, float duration) {
        messageStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        Label label = new Label(message, labelStyle);
        label.setFontScale(1.5f);

        table.add(label).center();

        Gdx.input.setInputProcessor(messageStage);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                Gdx.input.setInputProcessor(null);
            }
        }, duration);
    }

    private TextureRegionDrawable getDialogBackground() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.3f, 0.95f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    public void actAndDraw(float delta) {
        if (showingImage) {
            imageStage.act(delta);
            imageStage.draw();
        }
        pauseStage.act(delta);
        pauseStage.draw();
        messageStage.act(delta);
        messageStage.draw();
    }

    public void dispose() {
        dialogStage.dispose();
        pauseStage.dispose();
        messageStage.dispose();
        imageStage.dispose();
        if (currentImageTexture != null) currentImageTexture.dispose();
    }

    public Stage getPauseStage() { return pauseStage; }
    public boolean isShowingImage() { return showingImage; }
}