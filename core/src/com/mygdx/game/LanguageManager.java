package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage;
    private Map<String, Map<String, String>> translations;
    private Preferences prefs;

    public static final String RUSSIAN = "ru";
    public static final String ENGLISH = "en";

    private LanguageManager() {
        prefs = Gdx.app.getPreferences("TheFateGame");
        currentLanguage = prefs.getString("language", RUSSIAN);
        loadTranslations();
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private void loadTranslations() {
        translations = new HashMap<>();

        // Русский язык
        Map<String, String> ru = new HashMap<>();
        ru.put("settings", "НАСТРОЙКИ");
        ru.put("start", "СТАРТ");
        ru.put("social", "СОЦСЕТИ");
        ru.put("exit", "ВЫХОД");
        ru.put("music_volume", "ГРОМКОСТЬ МУЗЫКИ");
        ru.put("music", "МУЗЫКА");
        ru.put("on", "ВКЛ");
        ru.put("off", "ВЫКЛ");
        ru.put("reset_settings", "СБРОСИТЬ НАСТРОЙКИ");
        ru.put("back", "НАЗАД");
        ru.put("language", "ЯЗЫК");
        ru.put("russian", "РУССКИЙ");
        ru.put("english", "АНГЛИЙСКИЙ");
        ru.put("choose_action", "ВЫБЕРИТЕ ДЕЙСТВИЕ");
        ru.put("new_game", "НОВАЯ ИГРА");
        ru.put("continue", "ПРОДОЛЖИТЬ");
        ru.put("cancel", "ОТМЕНА");
        ru.put("volume", "ГРОМКОСТЬ");
        ru.put("select_language", "ВЫБЕРИТЕ ЯЗЫК");
        ru.put("welcome", "ДОБРО ПОЖАЛОВАТЬ!");
        ru.put("required_items", "Деталей: %d/%d");
        ru.put("game_over", "ВЫ УМЕРЛИ");
        ru.put("level_complete", "УРОВЕНЬ ПРОЙДЕН");
        ru.put("items_collected", "Собрано предметов: %d/5");
        ru.put("chapter2_unlocked", "ГЛАВА 2 РАЗБЛОКИРОВАНА!");
        ru.put("go_to_exit", "Идите к выходу из главы");
        translations.put(RUSSIAN, ru);

        // Английский язык
        Map<String, String> en = new HashMap<>();
        en.put("settings", "SETTINGS");
        en.put("start", "START");
        en.put("items_collected", "Items collected: %d/5");
        en.put("chapter2_unlocked", "CHAPTER 2 UNLOCKED!");
        en.put("go_to_exit", "Go to the chapter exit");
        en.put("social", "SOCIAL");
        en.put("exit", "EXIT");
        en.put("music_volume", "MUSIC VOLUME");
        en.put("music", "MUSIC");
        en.put("on", "ON");
        en.put("off", "OFF");
        en.put("reset_settings", "RESET SETTINGS");
        en.put("back", "BACK");
        en.put("language", "LANGUAGE");
        en.put("russian", "RUSSIAN");
        en.put("english", "ENGLISH");
        en.put("choose_action", "CHOOSE ACTION");
        en.put("new_game", "NEW GAME");
        en.put("continue", "CONTINUE");
        en.put("cancel", "CANCEL");
        en.put("volume", "VOLUME");
        en.put("select_language", "SELECT LANGUAGE");
        en.put("welcome", "WELCOME!");
        en.put("required_items", "Parts: %d/%d");
        en.put("game_over", "GAME OVER");
        en.put("level_complete", "LEVEL COMPLETE");
        translations.put(ENGLISH, en);
    }

    public String getText(String key) {
        Map<String, String> langMap = translations.get(currentLanguage);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        return key;
    }

    public String format(String key, Object... args) {
        return String.format(getText(key), args);
    }

    public void setLanguage(String language) {
        if (language.equals(RUSSIAN) || language.equals(ENGLISH)) {
            this.currentLanguage = language;
            prefs.putString("language", language);
            prefs.putBoolean("firstLaunch", false);
            prefs.flush();
            System.out.println("Язык изменен на: " + language);
        }
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean("firstLaunch", true);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isRussian() {
        return currentLanguage.equals(RUSSIAN);
    }

    public boolean isEnglish() {
        return currentLanguage.equals(ENGLISH);
    }
}