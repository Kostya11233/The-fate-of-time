package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SettingsScreen implements Screen {
    private final TheFateGame game;
    private final StartMenuScreen menuScreen;
    private Stage stage;

    public SettingsScreen(TheFateGame game, StartMenuScreen menuScreen) {
        this.game = game;
        this.menuScreen = menuScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    private void createUI() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        TextButton backBtn = new TextButton("← Назад", new TextButton.TextButtonStyle());
        backBtn.setPosition(w / 2f - 100, h / 2f - 50);
        backBtn.setSize(200, 60);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(menuScreen);
            }
        });

        stage.addActor(backBtn);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}