package com.thefateoftime.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.thefateoftime.TheFateGame;

public class SplashScreen implements Screen {
    private final TheFateGame game;
    private Texture logo;
    private SpriteBatch batch;
    private float elapsedTime = 0;

    public SplashScreen(final TheFateGame game) {
        this.game = game;
        this.batch = new SpriteBatch();

        try {
            logo = new Texture("splash.png");
        } catch (Exception e) {
            // Если нет splash.png, создаем заглушку
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(400, 400, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.2f, 0.2f, 0.3f, 1);
            pixmap.fill();
            logo = new Texture(pixmap);
            pixmap.dispose();
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (game.languageManager.isLanguageChosen()) {
                    game.setScreen(new StartMenuScreen(game));
                } else {
                    game.setScreen(new LanguageChoiceScreen(game));
                }
            }
        }, 2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        elapsedTime += delta;
        float alpha = 1f;
        if (elapsedTime < 0.5f) {
            alpha = elapsedTime / 0.5f;
        } else if (elapsedTime > 1.5f) {
            alpha = 1f - ((elapsedTime - 1.5f) / 0.5f);
            if (alpha < 0) alpha = 0;
        }

        if (logo != null) {
            batch.begin();
            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            float logoWidth = 400;
            float logoHeight = 400 * logo.getHeight() / logo.getWidth();
            float x = (width - logoWidth) / 2;
            float y = (height - logoHeight) / 2;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(logo, x, y, logoWidth, logoHeight);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }
    }

    @Override public void resize(int width, int height) { game.resize(width, height); }
    @Override public void dispose() {
        batch.dispose();
        if (logo != null) logo.dispose();
    }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}