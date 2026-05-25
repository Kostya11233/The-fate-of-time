package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Preferences;

public class TheFateGame extends Game {
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public BitmapFont font;
    public BitmapFont titleFont;
    public Music menuMusic;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public Preferences prefs;
    public float volume = 0.7f;
    public boolean musicEnabled = true;
    public LanguageManager languageManager;

    private boolean isFirstLaunch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        loadFonts();
        languageManager = LanguageManager.getInstance();
        prefs = Gdx.app.getPreferences("TheFateGame");
        volume = prefs.getFloat("volume", 0.7f);
        musicEnabled = prefs.getBoolean("musicEnabled", true);
        isFirstLaunch = prefs.getBoolean("firstLaunch", true);

        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(volume);
            if (musicEnabled) menuMusic.play();
        } catch (Exception e) {}

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        setScreen(new SplashScreen(this));
    }

    private void loadFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 30;
            parameter.characters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!@#$%^&*()_+=-.,/\\|`~:;?\"'";
            font = generator.generateFont(parameter);
            FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            titleParam.size = 52;
            titleParam.characters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!@#$%^&*()_+=-.,/\\|`~:;?\"'";
            titleFont = generator.generateFont(titleParam);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
            titleFont = new BitmapFont();
        }
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public void setFirstLaunchComplete() {
        isFirstLaunch = false;
        prefs.putBoolean("firstLaunch", false);
        prefs.flush();
    }

    public void saveSettings() {
        prefs.putFloat("volume", volume);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.flush();
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
        titleFont.dispose();
        if (menuMusic != null) menuMusic.dispose();
    }
}