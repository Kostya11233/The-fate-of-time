package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

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

        }

        // Через 4 секунды переходим
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Если первый запуск - показываем выбор языка
                if (game.isFirstLaunch()) {
                    game.setScreen(new LanguageChoiceScreen(game));
                } else {
                    // Иначе сразу в меню
                    game.setScreen(new StartMenuScreen(game));
                }
            }
        }, 4);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        elapsedTime += delta;

        float alpha;
        if (elapsedTime < 1f) {
            alpha = elapsedTime / 1f;
        } else if (elapsedTime > 3f) {
            alpha = 1f - ((elapsedTime - 3f) / 1f);
            if (alpha < 0) alpha = 0;
        } else {
            alpha = 1f;
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

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (logo != null) {
            logo.dispose();
        }
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}