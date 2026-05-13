package com.mygdx.game;

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

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        Label titleLabel = new Label("ВЫБЕРИТЕ ДЕЙСТВИЕ", labelStyle);
        titleLabel.setFontScale(1.5f);

        TextButton newGameBtn = new TextButton("НОВАЯ ИГРА", buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Новая игра");
                game.prefs.remove("currentMap");
                game.prefs.remove("playerX");
                game.prefs.remove("playerY");
                game.prefs.remove("enteredFromDoor");
                game.prefs.flush();
                game.setScreen(new Corridor1Screen(game, false));
            }
        });

        TextButton continueBtn = new TextButton("ПРОДОЛЖИТЬ", buttonStyle);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Продолжить игру");
                game.setScreen(new Corridor1Screen(game, true));
            }
        });
// Новая игра
        newGameBtn = new TextButton("НОВАЯ ИГРА", buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.prefs.remove("currentMap");
                game.prefs.remove("playerX");
                game.prefs.remove("playerY");
                game.prefs.remove("enteredFromDoor");
                game.prefs.flush();
                game.setScreen(new Corridor1Screen(game, false)); // Стартуем с коридора 1
            }
        });
        TextButton cancelBtn = new TextButton("ОТМЕНА", buttonStyle);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}