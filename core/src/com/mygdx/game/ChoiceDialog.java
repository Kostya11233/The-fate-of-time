package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class ChoiceDialog implements Screen {
    private final TheFateGame game;
    private final StartMenuScreen menuScreen;
    private Stage stage;

    public ChoiceDialog(TheFateGame game, StartMenuScreen menuScreen) {
        this.game = game;
        this.menuScreen = menuScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Стили
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        // Заголовок
        Label titleLabel = new Label(game.languageManager.getText("choose_action"), labelStyle);
        titleLabel.setFontScale(1.5f);

        // Новая игра
        TextButton newGameBtn = new TextButton(game.languageManager.getText("new_game"), buttonStyle);
        newGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                System.out.println("Новая игра");
                game.setScreen(menuScreen);
            }
        });

        // Продолжить
        TextButton continueBtn = new TextButton(game.languageManager.getText("continue"), buttonStyle);
        continueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                System.out.println("Продолжить игру");
                game.setScreen(menuScreen);
            }
        });

        // Отмена
        TextButton cancelBtn = new TextButton(game.languageManager.getText("cancel"), buttonStyle);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(menuScreen);
            }
        });

        table.add(titleLabel).padBottom(40).row();
        table.add(newGameBtn).width(250).height(60).padBottom(20).row();
        table.add(continueBtn).width(250).height(60).padBottom(20).row();
        table.add(cancelBtn).width(250).height(60).padTop(10).row();
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