package com.thefateoftime.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.thefateoftime.LanguageManager;
import com.thefateoftime.TheFateGame;

public class LanguageChoiceScreen implements Screen {
    private final TheFateGame game;
    private Stage stage;

    public LanguageChoiceScreen(TheFateGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = game.titleFont;
        titleStyle.fontColor = Color.GOLD;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Table titleTable = new Table();
        Label titleLabelRu = new Label("ВЫБЕРИТЕ ЯЗЫК", titleStyle);
        titleLabelRu.setFontScale(1.5f);
        Label titleLabelEn = new Label("SELECT LANGUAGE", titleStyle);
        titleLabelEn.setFontScale(1.5f);
        titleTable.add(titleLabelRu).padBottom(5).row();
        titleTable.add(titleLabelEn);

        TextButton ruBtn = new TextButton("РУССКИЙ", buttonStyle);
        ruBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.languageManager.setLanguage(LanguageManager.RUSSIAN);
                game.setScreen(new StartMenuScreen(game));
            }
        });

        TextButton enBtn = new TextButton("ENGLISH", buttonStyle);
        enBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.languageManager.setLanguage(LanguageManager.ENGLISH);
                game.setScreen(new StartMenuScreen(game));
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(ruBtn).width(280).height(80).padRight(40);
        buttonTable.add(enBtn).width(280).height(80);

        table.add(titleTable).padBottom(60).row();
        table.add(buttonTable).row();
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