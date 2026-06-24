package com.thefateoftime.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.thefateoftime.TheFateGame;
import com.thefateoftime.game.GameScreen;

public class UIManager {
    public TheFateGame game;
    public GameScreen gameScreen;
    public Stage stage;
    public Label itemsLabel;
    public ImageButton leftBtn, rightBtn, jumpBtn, pauseBtn;

    public UIManager(TheFateGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
    }

    public void createGameUI() {
        int btnSize = 100;
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        leftBtn = createButton("button/button_left.png", screenW / 5 - btnSize - 20, 30 + (float) btnSize / 2, btnSize);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                gameScreen.getPlayer().setMovingLeft(true);
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                gameScreen.getPlayer().setMovingLeft(false);
            }
        });

        rightBtn = createButton("button/button_right.png", screenW / 5 + 20, 30 + (float) btnSize / 2, btnSize);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                gameScreen.getPlayer().setMovingRight(true);
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                gameScreen.getPlayer().setMovingRight(false);
            }
        });

        jumpBtn = createButton("button/button_jump.png", screenW - 120, 30, 100);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                gameScreen.handleJumpButton();
            }
        });

        pauseBtn = createButton("button/button_pause.png", screenW - 80, screenH - 80, 70);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                gameScreen.setPaused(true);
            }
        });

        stage.addActor(leftBtn);
        stage.addActor(rightBtn);
        stage.addActor(jumpBtn);
        stage.addActor(pauseBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        labelStyle.fontColor = Color.WHITE;
        itemsLabel.setPosition(20, screenH - 50);
        stage.addActor(itemsLabel);
    }

    private ImageButton createButton(String texturePath, float x, float y, int size) {
        Texture texture = new Texture(texturePath);
        ImageButton btn = new ImageButton(new TextureRegionDrawable(texture));
        btn.setSize(size, size);
        btn.setPosition(x, y);
        return btn;
    }
    public void actAndDraw(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() { return stage; }
    public void dispose() { stage.dispose(); }
}