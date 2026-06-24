package com.thefateoftime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thefateoftime.screens.SplashScreen;

public class TheFateGame extends Game {
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public Viewport viewport;
    public BitmapFont font;
    public BitmapFont titleFont;
    public BitmapFont smallFont;
    public Music menuMusic;
    public Music gameMusic;

    public static final int VIRTUAL_WIDTH = 1280;
    public static final int VIRTUAL_HEIGHT = 720;

    public Preferences prefs;
    public float volume = 0.7f;
    public boolean musicEnabled = true;
    public LanguageManager languageManager;

    public float scaleX = 1f;
    public float scaleY = 1f;
    public float uiScale = 1f;


    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        calculateScaling();
        loadFonts();

        languageManager = LanguageManager.getInstance();
        prefs = Gdx.app.getPreferences("TheFateGame");
        volume = prefs.getFloat("volume", 0.7f);
        musicEnabled = prefs.getBoolean("musicEnabled", true);

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(volume);

        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(volume);
        setScreen(new SplashScreen(this));
    }

    private void calculateScaling() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        scaleX = screenWidth / VIRTUAL_WIDTH;
        scaleY = screenHeight / VIRTUAL_HEIGHT;
        uiScale = Math.min(scaleX, scaleY);
    }

    public float getUIScale() {return uiScale;}
    public int getScaledSize(int originalSize) {return Math.round(originalSize * uiScale);}
    private void loadFonts() {
        try {
            String[] fontPaths = {"font.ttf", "fonts/font.ttf", "Font.ttf", "fonts/Font.ttf"};
            FreeTypeFontGenerator generator = null;

            for (String path : fontPaths) {
              if (Gdx.files.internal(path).exists()) {
                  generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
                  break;
              }
            }

            if (generator == null) {
                font = new BitmapFont();
                titleFont = new BitmapFont();
                smallFont = new BitmapFont();
                return;
            }

            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = Math.max(24, Math.round(30 * uiScale));
            param.characters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{};:'\",.<>/?\\|`~";
            font = generator.generateFont(param);

            FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            titleParam.size = Math.max(36, Math.round(52 * uiScale));
            titleParam.characters = param.characters;
            titleFont = generator.generateFont(titleParam);

            FreeTypeFontGenerator.FreeTypeFontParameter smallParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            smallParam.size = Math.max(16, Math.round(20 * uiScale));
            smallParam.characters = param.characters;
            smallFont = generator.generateFont(smallParam);

            generator.dispose();

        } catch (Exception e) {
            font = new BitmapFont();
            titleFont = new BitmapFont();
            smallFont = new BitmapFont();
        }
    }

    public void stopGameMusic() {
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.stop();
        }
    }

    public void startGameMusic() {
        if (gameMusic != null && musicEnabled && !gameMusic.isPlaying()) {
            gameMusic.play();
            gameMusic.setVolume(volume);
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    public void startMenuMusic() {
        if (menuMusic != null && musicEnabled && !menuMusic.isPlaying()) {
            menuMusic.play();
            menuMusic.setVolume(volume);
        }
    }
    public void saveGameProgress(String currentMap, int collectedItems) {
        prefs.putString("saved_map", currentMap);
        prefs.putInteger("saved_items", collectedItems);
        prefs.putBoolean("has_save", true);
        prefs.flush();
    }
    public boolean hasSaveGame() {
        return prefs.getBoolean("has_save", false);
    }
    public String getSavedMap() {
        return prefs.getString("saved_map", "room3.tmx");
    }
    public int getSavedItems() {
        return prefs.getInteger("saved_items", 0);
    }
    public void clearSave() {
        prefs.putBoolean("has_save", false);
        prefs.putString("saved_map", "");
        prefs.putInteger("saved_items", 0);
        prefs.flush();
    }

    public void clearAllProgress() {
        prefs.clear();
        prefs.flush();
        volume = 0.7f;
        musicEnabled = true;
        saveSettings();
        languageManager.setLanguage("ru");
    }

    public void saveSettings() {
        prefs.putFloat("volume", volume);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.flush();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        calculateScaling();
        loadFonts();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        titleFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (gameMusic != null) gameMusic.dispose();
    }
}