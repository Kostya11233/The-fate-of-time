package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.Chapter1Screen;
import com.mygdx.game.Chapter2Screen;

public class ChapterSelectScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;
    private boolean chapter2Unlocked;

    public ChapterSelectScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        this.chapter2Unlocked = game.prefs.getBoolean("chapter2_unlocked", false);
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.titleFont;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Label titleLabel = new Label(game.languageManager.getText("select_chapter"), labelStyle);
        titleLabel.setFontScale(1.8f);

        TextButton chapter1Btn = new TextButton(game.languageManager.getText("chapter_1"), buttonStyle);
        chapter1Btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.clearSave();
                game.prefs.putInteger("chapter1_items", 0);
                game.prefs.putBoolean("chapter2_unlocked", false);
                game.prefs.flush();
                game.setScreen(new Chapter1Screen(game));
            }
        });

        String chapter2Text = game.languageManager.getText("chapter_2");
        if (!chapter2Unlocked) {
            chapter2Text = game.languageManager.getText("chapter_2_locked");
        }

        TextButton chapter2Btn = new TextButton(chapter2Text, buttonStyle);
        if (!chapter2Unlocked) {
            chapter2Btn.setDisabled(true);
        }
        chapter2Btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (chapter2Unlocked) {
                    game.clearSave();
                    game.prefs.putInteger("chapter2_items", 0);
                    game.setScreen(new Chapter2Screen(game));
                }
            }
        });

        TextButton backBtn = new TextButton(game.languageManager.getText("back"), buttonStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new StartMenuScreen(game));
            }
        });

        if (!chapter2Unlocked) {
            Label.LabelStyle infoStyle = new Label.LabelStyle();
            infoStyle.font = game.font;
            Label infoLabel = new Label(game.languageManager.getText("complete_chapter1_to_unlock"), infoStyle);
            infoLabel.setColor(0.7f, 0.7f, 0.2f, 1);

            table.add(titleLabel).padBottom(50).row();
            table.add(chapter1Btn).width(350).height(80).padBottom(30).row();
            table.add(chapter2Btn).width(350).height(80).padBottom(20).row();
            table.add(infoLabel).padBottom(30).row();
            table.add(backBtn).width(250).height(60).row();
        } else {
            table.add(titleLabel).padBottom(50).row();
            table.add(chapter1Btn).width(350).height(80).padBottom(30).row();
            table.add(chapter2Btn).width(350).height(80).padBottom(50).row();
            table.add(backBtn).width(250).height(60).row();
        }

        table.center();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        chapter2Unlocked = game.prefs.getBoolean("chapter2_unlocked", false);
        stage.clear();
        createUI();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}