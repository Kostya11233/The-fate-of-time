package com.thefateoftime.game;

import com.badlogic.gdx.Preferences;
import com.thefateoftime.TheFateGame;

public class GameState {
    private static GameState instance;
    private TheFateGame game;

    // Состояние игры
    private int currentChapter;
    private int currentLevel;
    private int collectedItems;
    private int totalItemsInLevel;
    private String currentMap;
    private boolean chapter1Completed;
    private boolean chapter2Completed;
    private boolean isPaused;
    private boolean isTransitioning;
    private float playTime;

    // Preferences ключи
    private static final String PREF_CHAPTER1_COMPLETED = "chapter1_completed";
    private static final String PREF_CHAPTER2_COMPLETED = "chapter2_completed";
    private static final String PREF_CHAPTER2_UNLOCKED = "chapter2_unlocked";
    private static final String PREF_CURRENT_CHAPTER = "current_chapter";
    private static final String PREF_CURRENT_LEVEL = "current_level";
    private static final String PREF_COLLECTED_ITEMS = "collected_items";
    private static final String PREF_CURRENT_MAP = "current_map";
    private static final String PREF_PLAY_TIME = "play_time";

    private GameState(TheFateGame game) {
        this.game = game;
        loadState();
    }

    public static GameState getInstance(TheFateGame game) {
        if (instance == null) {
            instance = new GameState(game);
        }
        return instance;
    }

    public void loadState() {
        Preferences prefs = game.prefs;
        currentChapter = prefs.getInteger(PREF_CURRENT_CHAPTER, 1);
        currentLevel = prefs.getInteger(PREF_CURRENT_LEVEL, 1);
        collectedItems = prefs.getInteger(PREF_COLLECTED_ITEMS, 0);
        currentMap = prefs.getString(PREF_CURRENT_MAP, "room3.tmx");
        playTime = prefs.getFloat(PREF_PLAY_TIME, 0f);
        chapter1Completed = prefs.getBoolean(PREF_CHAPTER1_COMPLETED, false);
        chapter2Completed = prefs.getBoolean(PREF_CHAPTER2_COMPLETED, false);
    }

    public void saveState() {
        Preferences prefs = game.prefs;
        prefs.putInteger(PREF_CURRENT_CHAPTER, currentChapter);
        prefs.putInteger(PREF_CURRENT_LEVEL, currentLevel);
        prefs.putInteger(PREF_COLLECTED_ITEMS, collectedItems);
        prefs.putString(PREF_CURRENT_MAP, currentMap);
        prefs.putFloat(PREF_PLAY_TIME, playTime);
        prefs.putBoolean(PREF_CHAPTER1_COMPLETED, chapter1Completed);
        prefs.putBoolean(PREF_CHAPTER2_COMPLETED, chapter2Completed);
        prefs.flush();
    }

    public void clearState() {
        currentChapter = 1;
        currentLevel = 1;
        collectedItems = 0;
        currentMap = "room3.tmx";
        playTime = 0f;
        saveState();
    }

    public void completeChapter1() {
        chapter1Completed = true;
        game.prefs.putBoolean("chapter2_unlocked", true);
        game.prefs.flush();
        saveState();
    }

    public void completeChapter2() {
        chapter2Completed = true;
        saveState();
    }

    public void addItem() {
        collectedItems++;
        saveState();
    }

    public void resetItems() {
        collectedItems = 0;
        saveState();
    }

    public boolean isChapter2Unlocked() {
        return game.prefs.getBoolean("chapter2_unlocked", false);
    }

    public boolean hasSaveGame() {
        return game.prefs.getBoolean("has_save", false) || collectedItems > 0 || chapter1Completed;
    }

    // Getters and Setters
    public int getCurrentChapter() { return currentChapter; }
    public void setCurrentChapter(int chapter) { this.currentChapter = chapter; saveState(); }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int level) { this.currentLevel = level; saveState(); }
    public int getCollectedItems() { return collectedItems; }
    public void setCollectedItems(int items) { this.collectedItems = items; saveState(); }
    public String getCurrentMap() { return currentMap; }
    public void setCurrentMap(String map) { this.currentMap = map; saveState(); }
    public float getPlayTime() { return playTime; }
    public void setPlayTime(float time) { this.playTime = time; }
    public boolean isChapter1Completed() { return chapter1Completed; }
    public boolean isChapter2Completed() { return chapter2Completed; }
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
    public boolean isTransitioning() { return isTransitioning; }
    public void setTransitioning(boolean transitioning) { isTransitioning = transitioning; }
}