package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

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
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        stage.addActor(darkBg);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;
        Label titleLabel = new Label(game.languageManager.getText("choose_action"), labelStyle);
        titleLabel.setFontScale(1.5f);
        TextButton newGameBtn = new TextButton(game.languageManager.getText("new_game"), buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.prefs.putInteger("chapter1_items", 0);
                game.prefs.flush();
                game.setScreen(new Chapter1Screen(game, "room3.tmx", "spawn"));
            }
        });
        TextButton cancelBtn = new TextButton(game.languageManager.getText("cancel"), buttonStyle);
        cancelBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(menuScreen);
            }
        });
        Table dialog = new Table();
        dialog.pad(30);
        dialog.add(titleLabel).padBottom(40).row();
        dialog.add(newGameBtn).width(280).height(70).padBottom(20).row();
        dialog.add(cancelBtn).width(280).height(70).row();
        table.add(dialog).center();
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}
}