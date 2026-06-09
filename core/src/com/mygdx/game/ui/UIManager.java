package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.game.GameScreen;

public class UIManager {
    private TheFateGame game;
    private GameScreen gameScreen;
    private Stage stage;
    private Label itemsLabel;
    private ImageButton upBtn, downBtn, leftBtn, rightBtn, jumpBtn, pauseBtn;

    public UIManager(TheFateGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
    }

    public void createGameUI() {
        int btnSize = 100;
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        leftBtn = createButton("button/button_left.png", screenW / 5 - btnSize - 20, 30 + btnSize / 2, btnSize);
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingLeft(true);
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingLeft(false);
            }
        });

        rightBtn = createButton("button/button_right.png", screenW / 5 + 20, 30 + btnSize / 2, btnSize);
        rightBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingRight(true);
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingRight(false);
            }
        });

        jumpBtn = createButton("button/button_jump.png", screenW - 120, 30, 100);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (gameScreen != null) gameScreen.handleJumpButton();
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
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20, screenH - 50);
        stage.addActor(itemsLabel);
    }

    private ImageButton createButton(String texturePath, float x, float y, int size) {
        Texture texture;
        try {
            texture = new Texture(texturePath);
        } catch (Exception e) {
            texture = createFallbackTexture();
        }
        ImageButton btn = new ImageButton(new TextureRegionDrawable(texture));
        btn.setSize(size, size);
        btn.setPosition(x, y);
        return btn;
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.5f, 0.5f, 0.5f, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public void updateItemsLabel(int collected, int total) {
        itemsLabel.setText(game.languageManager.format("items_collected", collected, total));
    }

    public void showAllItemsCollectedMessage() {
        showMessage(game.languageManager.getText("all_items_collected"));
    }

    private void showMessage(String message) {
        // Реализация показа сообщения
    }

    public void showDeathMessage(Runnable onRestart) {
        showMessage(game.languageManager.getText("you_died"));
        onRestart.run();
    }

    public void showLevelMessage(String message) {
        showMessage(message);
    }

    public void showToBeContinued(Runnable onFinish) {
        showMessage(game.languageManager.getText("loading"));
        onFinish.run();
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