package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Preferences;

public class TheFateGame extends Game {
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public BitmapFont font;
    public Music menuMusic;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public Preferences prefs;
    public float volume = 0.7f;
    public boolean musicEnabled = true;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Загружаем сохраненные настройки
        prefs = Gdx.app.getPreferences("TheFateGame");
        volume = prefs.getFloat("volume", 0.7f);
        musicEnabled = prefs.getBoolean("musicEnabled", true);

        // Загружаем музыку
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(volume);
            System.out.println("Музыка загружена, громкость: " + volume);
        } catch (Exception e) {
            System.out.println("Ошибка загрузки музыки: " + e.getMessage());
        }

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        setScreen(new SplashScreen(this));
    }

    public void saveSettings() {
        prefs.putFloat("volume", volume);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.flush();
        System.out.println("Настройки сохранены");
    }

    @Override
    public void resize(int width, int height) {
        SCREEN_WIDTH = width;
        SCREEN_HEIGHT = height;
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (menuMusic != null) {
            menuMusic.dispose();
        }
    }
}