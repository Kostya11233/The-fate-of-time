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

        Map<String, String> ru = new HashMap<>();
        ru.put("settings", "НАСТРОЙКИ");
        ru.put("start", "СТАРТ");
        ru.put("continue", "ПРОДОЛЖИТЬ");
        ru.put("new_game", "НОВАЯ ИГРА");
        ru.put("social", "СОЦСЕТИ");
        ru.put("exit", "ВЫХОД");
        ru.put("music", "МУЗЫКА");
        ru.put("on", "ВКЛ");
        ru.put("off", "ВЫКЛ");
        ru.put("reset_settings", "СБРОСИТЬ НАСТРОЙКИ");
        ru.put("back", "НАЗАД");
        ru.put("language", "ЯЗЫК");
        ru.put("russian", "РУССКИЙ");
        ru.put("english", "АНГЛИЙСКИЙ");
        ru.put("volume", "ГРОМКОСТЬ");
        ru.put("game_paused", "ПАУЗА");
        ru.put("restart_level", "ПЕРЕЗАПУСТИТЬ УРОВЕНЬ");
        ru.put("exit_to_menu", "ВЫЙТИ В МЕНЮ");
        ru.put("you_died", "ВЫ УМЕРЛИ");
        ru.put("tap_to_continue", "Нажмите для продолжения");
        ru.put("select_language", "ВЫБЕРИТЕ ЯЗЫК");
        ru.put("items_collected", "Предметов собрано: %d из 5");
        ru.put("all_items_collected", "ВСЕ ПРЕДМЕТЫ СОБРАНЫ! Теперь можно идти к выходу.");
        ru.put("chapter1_complete_title", "ГЛАВА 1 ПРОЙДЕНА!");
        ru.put("chapter2_unlocked", "ГЛАВА 2 ОТКРЫТА!");
        ru.put("loading", "ЗАГРУЗКА...");
        ru.put("chapter2_loading", "Переход во 2 главу...");
        ru.put("note_collected", "Найдена записка!");
        ru.put("no_notes", "У вас пока нет записок!");
        ru.put("book_title", "СОБРАННЫЕ ЗАПИСКИ");
        ru.put("previous", "НАЗАД");
        ru.put("next", "ДАЛЕЕ");
        ru.put("close", "ЗАКРЫТЬ");
        ru.put("open_note", "ОТКРЫТЬ ЗАПИСКУ");
        ru.put("note_prefix", "Записка");
        translations.put(RUSSIAN, ru);

        Map<String, String> en = new HashMap<>();
        en.put("settings", "SETTINGS");
        en.put("start", "START");
        en.put("continue", "CONTINUE");
        en.put("new_game", "NEW GAME");
        en.put("social", "SOCIAL");
        en.put("exit", "EXIT");
        en.put("music", "MUSIC");
        en.put("on", "ON");
        en.put("off", "OFF");
        en.put("reset_settings", "RESET SETTINGS");
        en.put("back", "BACK");
        en.put("language", "LANGUAGE");
        en.put("russian", "RUSSIAN");
        en.put("english", "ENGLISH");
        en.put("volume", "VOLUME");
        en.put("game_paused", "GAME PAUSED");
        en.put("restart_level", "RESTART LEVEL");
        en.put("exit_to_menu", "EXIT TO MENU");
        en.put("you_died", "YOU DIED");
        en.put("tap_to_continue", "Tap to continue");
        en.put("select_language", "SELECT LANGUAGE");
        en.put("items_collected", "Items collected: %d out of 5");
        en.put("all_items_collected", "ALL ITEMS COLLECTED! You can now go to the exit.");
        en.put("chapter1_complete_title", "CHAPTER 1 COMPLETE!");
        en.put("chapter2_unlocked", "CHAPTER 2 UNLOCKED!");
        en.put("loading", "LOADING...");
        en.put("chapter2_loading", "Loading Chapter 2...");
        en.put("note_collected", "Note found!");
        en.put("no_notes", "You have no notes yet!");
        en.put("book_title", "COLLECTED NOTES");
        en.put("previous", "BACK");
        en.put("next", "NEXT");
        en.put("close", "CLOSE");
        en.put("open_note", "OPEN NOTE");
        en.put("note_prefix", "Note");
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
            prefs.putBoolean("language_chosen", true);
            prefs.flush();
        }
    }

    public boolean isLanguageChosen() {
        return prefs.getBoolean("language_chosen", false);
    }
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}