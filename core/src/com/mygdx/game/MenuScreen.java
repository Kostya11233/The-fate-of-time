package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen implements Screen {
    MovingBackground background;
    TheFateGame TheFateGame;
    private Button buttonNewGame;
    private Button buttonContinue;
    private Button buttonGameSettings;
    private Button buttonAchievements;
    private Button buttonTraining;
    private Button buttonExit;

    public MenuScreen(TheFateGame TheFateGame) {
        this.TheFateGame = TheFateGame;
        background = new MovingBackground();
        buttonNewGame = new Button(67, 580, 267, 90, "New game");
        buttonContinue = new Button(67, 440, 267, 90, "Continue");
        buttonGameSettings = new Button(67, 300, 267, 90, "GameSettings");
        buttonAchievements = new Button(67, 160, 267, 90, "Achievements");
        buttonExit = new Button(67, 20, 267, 90, "Exit");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 0, 0, 1);
        TheFateGame.camera.update();
        TheFateGame.batch.setProjectionMatrix(TheFateGame.camera.combined);
        TheFateGame.batch.begin();
        background.draw(TheFateGame.batch);
        buttonNewGame.draw(TheFateGame.batch);
        buttonContinue.draw(TheFateGame.batch);
        buttonGameSettings.draw(TheFateGame.batch);
        buttonAchievements.draw(TheFateGame.batch);
        buttonExit.draw(TheFateGame.batch);
        TheFateGame.batch.end();
    }

    @Override
    public void dispose() {
        background.dispose();
        buttonNewGame.dispose();
        buttonContinue.dispose();
        buttonGameSettings.dispose();
        buttonAchievements.dispose();
        buttonExit.dispose();
        background.dispose();
    }

    @Override public void show() {

    }
    @Override public void resize(int i, int i1) {

    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {

    }
}