package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
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

        // Кнопки движения
        leftBtn = createButton("button/button_left.png", screenW / 5 - btnSize - 20, 30 + btnSize / 2, btnSize);
        // Вместо:
        leftBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingLeft(true);
                return true;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                if (gameScreen != null) gameScreen.getPlayer().setMovingLeft(false);
            }
        });

// Используйте такой синтаксис (без лямбд) - он уже правильный!

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

        // Кнопка прыжка
        jumpBtn = createButton("button/button_jump.png", screenW - 120, 30, 100);
        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (gameScreen != null) gameScreen.handleJumpButton();
            }
        });

        // Кнопка паузы
        pauseBtn = createButton("button/button_pause.png", screenW - 80, screenH - 80, 70);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                gameScreen.setPaused(true);
                // Показать меню паузы
            }
        });

        stage.addActor(leftBtn);
        stage.addActor(rightBtn);
        stage.addActor(jumpBtn);
        stage.addActor(pauseBtn);

        // Счетчик предметов
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
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
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        // Показать сообщение о сборе всех предметов
    }

    public void showDeathMessage(Runnable onRestart) {
        // Показать сообщение о смерти и перезапустить уровень
        onRestart.run();
    }

    public void showLevelMessage(String message) {
        // Показать сообщение об уровне
    }

    public void showToBeContinued(Runnable onFinish) {
        // Показать "продолжение следует" и выйти
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