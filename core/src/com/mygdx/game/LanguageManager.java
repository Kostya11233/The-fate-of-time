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
        if (instance == null) instance = new LanguageManager();
        return instance;
    }

    private void loadTranslations() {
        translations = new HashMap<>();

        // РУССКИЙ
        Map<String, String> ru = new HashMap<>();
        ru.put("settings", "НАСТРОЙКИ");
        ru.put("continue", "ПРОДОЛЖИТЬ");
        ru.put("new_game", "НОВАЯ ИГРА");
        ru.put("social", "СОЦСЕТИ");
        ru.put("exit", "ВЫХОД");
        ru.put("select_chapter", "ВЫБЕРИТЕ ГЛАВУ");
        ru.put("chapter_1", "ПЕРВАЯ ГЛАВА");
        ru.put("chapter_2", "ВТОРАЯ ГЛАВА");
        ru.put("chapter_3", "ТРЕТЬЯ ГЛАВА");
        ru.put("chapter_locked", "ЗАКРЫТА");
        ru.put("back", "НАЗАД");
        ru.put("music", "МУЗЫКА");
        ru.put("on", "ВКЛ");
        ru.put("off", "ВЫКЛ");
        ru.put("reset_settings", "СБРОСИТЬ НАСТРОЙКИ");
        ru.put("language", "ЯЗЫК");
        ru.put("russian", "РУССКИЙ");
        ru.put("english", "АНГЛИЙСКИЙ");
        ru.put("volume", "ГРОМКОСТЬ");
        ru.put("game_paused", "ПАУЗА");
        ru.put("resume", "ПРОДОЛЖИТЬ");
        ru.put("restart_level", "ПЕРЕЗАПУСТИТЬ УРОВЕНЬ");
        ru.put("exit_to_menu", "В МЕНЮ");
        ru.put("you_died", "ВЫ УМЕРЛИ");
        ru.put("tap_to_continue", "Нажмите для продолжения");
        ru.put("loading", "ЗАГРУЗКА...");
        ru.put("items_collected", "Предметов: %d / 5");
        ru.put("all_items_collected", "ВСЕ ПРЕДМЕТЫ СОБРАНЫ!\nИдите к выходу из главы");
        ru.put("chapter1_complete_title", "ГЛАВА 1 ПРОЙДЕНА!");
        ru.put("chapter2_unlocked", "ГЛАВА 2 ОТКРЫТА!");
        ru.put("note_collected", "Найдена записка!");
        ru.put("no_notes", "У вас пока нет записок!");
        ru.put("book_title", "СОБРАННЫЕ ЗАПИСКИ");
        ru.put("note_prefix", "ЗАПИСКА №");
        ru.put("previous", "ПРЕДЫДУЩАЯ");
        ru.put("next", "СЛЕДУЮЩАЯ");
        ru.put("close", "ЗАКРЫТЬ");
        ru.put("open_note", "ОТКРЫТЬ ЗАПИСКУ");
        ru.put("level_1_title", "УРОВЕНЬ 1\nЗаброшенная лаборатория");
        ru.put("level_2_title", "УРОВЕНЬ 2\nЗона экспериментов");
        ru.put("level_3_title", "УРОВЕНЬ 3\nХранилище времени");
        ru.put("quests", "ЗАДАНИЯ");
        ru.put("trash_task", "Собрать мусор (%d/%d)");
        ru.put("talk_to_survivor", "Поговорить с Выжившим");
        ru.put("go_to_exit", "Идти к выходу");
        ru.put("talk_to_chemist", "Поговорить с Химиком");
        ru.put("find_parts", "Найти запчасти (%d/3)");
        ru.put("return_to_chemist", "Вернуться к Химику");
        ru.put("talk_to_watchmaker", "Поговорить с Часовщиком");
        ru.put("collect_shards", "Собрать осколки (%d/%d)");
        ru.put("trash_minigame_title", "СОБЕРИ МУСОР");
        ru.put("trash_minigame_counter", "Собрано: %d/%d");
        ru.put("shard_minigame_title", "СОБЕРИ ОСКОЛКИ");
        ru.put("shard_minigame_counter", "Собрано: %d/%d");
        ru.put("exit_minigame", "ВЫЙТИ");
        ru.put("trash_collected_message", "Мусор собран! %d/%d");
        ru.put("shard_collected_message", "Осколок собран! %d/%d");
        ru.put("all_trash_collected", "Поздравляю! Весь мусор собран!");
        ru.put("all_shards_collected", "Поздравляю! Все осколки собраны!");
        ru.put("return_to_npc", "Вернись к %s!");
        ru.put("part_collected", "Запчасть %d/3");
        ru.put("all_parts_collected", "Все запчасти собраны! Вернись к Химику.");
        ru.put("exit_unlocked", "Выход открыт!");
        ru.put("chapter2_complete", "ГЛАВА 2 ПРОЙДЕНА!\nЗАГРУЗКА ГЛАВЫ 3...");
        ru.put("survivor_dialog_1", "Выживший: Помоги! Нужно собрать мусор в корзину.");
        ru.put("survivor_dialog_2", "Перетащи мусор пальцем в корзину. Нужно %d штук.");
        ru.put("survivor_dialog_3", "Выживший: Спасибо! Ты очистила зону от мусора!");
        ru.put("survivor_dialog_4", "Выживший: Чтобы предотвратить аномалию, нужно найти временной объект.");
        ru.put("survivor_dialog_5", "Выживший: Иди к выходу! Удачи!");
        ru.put("chemist_dialog_1", "Химик: Эксперимент вышел из-под контроля...");
        ru.put("chemist_dialog_2", "Химик: Чтобы остановить аномалию, найди 3 запчасти.");
        ru.put("chemist_dialog_3", "Химик: Это ГИПЕРКУБ!");
        ru.put("chemist_dialog_4", "Химик: Нужно найти 3 запчасти. Ищи их по лаборатории.");
        ru.put("chemist_dialog_5", "Химик: Отлично! Все запчасти на месте!");
        ru.put("chemist_dialog_6", "Химик: Временной объект, который ты ищешь - это ГИПЕРКУБ.");
        ru.put("chemist_dialog_7", "Химик: Иди к выходу!");
        ru.put("watchmaker_dialog_1", "Часовщик: Время искажено... Нужно собрать осколки.");
        ru.put("watchmaker_dialog_2", "Нажми на все осколки. Их %d штук.");
        ru.put("watchmaker_dialog_3", "Часовщик: Ты собрала все осколки времени!");
        ru.put("watchmaker_dialog_4", "Часовщик: Гиперкуб ждет тебя. Иди к выходу.");
        ru.put("chapter3_hint", "← ПЕРЕМЕЩАЙ РАКЕТУ ПАЛЬЦЕМ →");
        ru.put("chapter3_evades", "УКЛОНЕНИЙ");
        ru.put("time_object_reached", "ВРЕМЕННОЙ ОБЪЕКТ ДОСТИГНУТ!");
        ru.put("alex_dialog_1", "Элис... Ты слышишь меня?");
        ru.put("alex_dialog_2", "Это твой отец. Я записал это сообщение на случай, если время остановится.");
        ru.put("alex_dialog_3", "Ты всегда была сильной. Я знал, что ты доберешься до временного объекта.");
        ru.put("alex_dialog_4", "Гиперкуб - это не просто машина времени. Это ключ к перезагрузке реальности.");
        ru.put("alex_dialog_5", "Но есть цена. Если ты используешь его... ты останешься в прошлом навсегда.");
        ru.put("alex_dialog_6", "Выбор за тобой, дочка. Я горжусь тобой, что бы ты ни решила.");
        ru.put("choice_title", "ВЫБОР СУДЬБЫ");
        ru.put("choice_question", "Что сделает Элис с Гиперкубом?");
        ru.put("choice_sacrifice", "🌌 ОСТАТЬСЯ В ПРОШЛОМ");
        ru.put("choice_sacrifice_desc", "Спасти отца, но исчезнуть из настоящего");
        ru.put("choice_return", "✨ ВЕРНУТЬСЯ В НАСТОЯЩЕЕ");
        ru.put("choice_return_desc", "Отец останется в прошлом, мир спасён");
        ru.put("choice_freeze", "⏰ ЗАМОРОЗИТЬ ВРЕМЯ");
        ru.put("choice_freeze_desc", "Остановить момент выбора навсегда");
        ru.put("ending_sacrifice_title", "КОНЦОВКА: ЖЕРТВА");
        ru.put("ending_sacrifice_text", "Элис активирует Гиперкуб и переносится в прошлое.\n\nОна успевает спасти отца, но сама исчезает из реальности.\nАлекс живёт и помнит свою дочь.\n\nВременная петля замкнулась.\n\n★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("ending_return_title", "КОНЦОВКА: ВОЗВРАЩЕНИЕ");
        ru.put("ending_return_text", "Элис возвращается в настоящее, забрав частицу Гиперкуба.\n\nОтец остался в прошлом, но мир спасён от временной аномалии.\nЭлис продолжает своё путешествие сквозь время...\n\n★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("ending_freeze_title", "КОНЦОВКА: ЗАМОРОЗКА");
        ru.put("ending_freeze_text", "Элис замораживает время в момент выбора.\n\nНикто не знает, что произошло дальше.\nМожет быть, когда-нибудь время снова пойдёт...\n\n★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("main_menu", "В ГЛАВНОЕ МЕНЮ");
        ru.put("exit_game", "ВЫЙТИ ИЗ ИГРЫ");
        ru.put("thanks_for_playing", "★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("next_level", "ДАЛЕЕ →");
        ru.put("alex", "✧ АЛЕКС (ОТЕЦ) ✧");
        ru.put("survivor_name", "✧ ВЫЖИВШИЙ ✧");
        ru.put("chemist_name", "✧ ХИМИК ✧");
        ru.put("watchmaker_name", "✧ ЧАСОВЩИК ✧");
        ru.put("hit_message", "-1 УКЛОНЕНИЕ");
        translations.put(RUSSIAN, ru);

        // АНГЛИЙСКИЙ
        Map<String, String> en = new HashMap<>();
        en.put("settings", "SETTINGS");
        en.put("continue", "CONTINUE");
        en.put("new_game", "NEW GAME");
        en.put("social", "SOCIAL");
        en.put("exit", "EXIT");
        en.put("select_chapter", "SELECT CHAPTER");
        en.put("chapter_1", "CHAPTER 1");
        en.put("chapter_2", "CHAPTER 2");
        en.put("chapter_3", "CHAPTER 3");
        en.put("chapter_locked", "LOCKED");
        en.put("back", "BACK");
        en.put("music", "MUSIC");
        en.put("on", "ON");
        en.put("off", "OFF");
        en.put("reset_settings", "RESET SETTINGS");
        en.put("language", "LANGUAGE");
        en.put("russian", "RUSSIAN");
        en.put("english", "ENGLISH");
        en.put("volume", "VOLUME");
        en.put("game_paused", "GAME PAUSED");
        en.put("resume", "RESUME");
        en.put("restart_level", "RESTART LEVEL");
        en.put("exit_to_menu", "EXIT TO MENU");
        en.put("you_died", "YOU DIED");
        en.put("tap_to_continue", "Tap to continue");
        en.put("loading", "LOADING...");
        en.put("items_collected", "Items: %d / 5");
        en.put("all_items_collected", "ALL ITEMS COLLECTED!\nGo to the chapter exit");
        en.put("chapter1_complete_title", "CHAPTER 1 COMPLETE!");
        en.put("chapter2_unlocked", "CHAPTER 2 UNLOCKED!");
        en.put("note_collected", "Note found!");
        en.put("no_notes", "You have no notes yet!");
        en.put("book_title", "COLLECTED NOTES");
        en.put("note_prefix", "NOTE #");
        en.put("previous", "PREVIOUS");
        en.put("next", "NEXT");
        en.put("close", "CLOSE");
        en.put("open_note", "OPEN NOTE");
        en.put("level_1_title", "LEVEL 1\nAbandoned Laboratory");
        en.put("level_2_title", "LEVEL 2\nExperiment Zone");
        en.put("level_3_title", "LEVEL 3\nTime Vault");
        en.put("quests", "QUESTS");
        en.put("trash_task", "Collect trash (%d/%d)");
        en.put("talk_to_survivor", "Talk to Survivor");
        en.put("go_to_exit", "Go to exit");
        en.put("talk_to_chemist", "Talk to Chemist");
        en.put("find_parts", "Find parts (%d/3)");
        en.put("return_to_chemist", "Return to Chemist");
        en.put("talk_to_watchmaker", "Talk to Watchmaker");
        en.put("collect_shards", "Collect shards (%d/%d)");
        en.put("trash_minigame_title", "COLLECT TRASH");
        en.put("trash_minigame_counter", "Collected: %d/%d");
        en.put("shard_minigame_title", "COLLECT SHARDS");
        en.put("shard_minigame_counter", "Collected: %d/%d");
        en.put("exit_minigame", "EXIT");
        en.put("trash_collected_message", "Trash collected! %d/%d");
        en.put("shard_collected_message", "Shard collected! %d/%d");
        en.put("all_trash_collected", "Congratulations! All trash collected!");
        en.put("all_shards_collected", "Congratulations! All shards collected!");
        en.put("return_to_npc", "Return to %s!");
        en.put("part_collected", "Part %d/3");
        en.put("all_parts_collected", "All parts collected! Return to Chemist.");
        en.put("exit_unlocked", "Exit unlocked!");
        en.put("chapter2_complete", "CHAPTER 2 COMPLETE!\nLOADING CHAPTER 3...");
        en.put("survivor_dialog_1", "Survivor: Help! We need to collect trash into the bin.");
        en.put("survivor_dialog_2", "Drag the trash with your finger to the bin. Need %d pieces.");
        en.put("survivor_dialog_3", "Survivor: Thank you! You cleared the area of trash!");
        en.put("survivor_dialog_4", "Survivor: To prevent the anomaly, you need to find the time object.");
        en.put("survivor_dialog_5", "Survivor: Go to the exit! Good luck!");
        en.put("chemist_dialog_1", "Chemist: The experiment is out of control...");
        en.put("chemist_dialog_2", "Chemist: To stop the anomaly, find 3 parts.");
        en.put("chemist_dialog_3", "Chemist: It's the HYPERCUBE!");
        en.put("chemist_dialog_4", "Chemist: You need to find 3 parts. Look around the lab.");
        en.put("chemist_dialog_5", "Chemist: Excellent! All parts are in place!");
        en.put("chemist_dialog_6", "Chemist: The time object you're looking for is the HYPERCUBE.");
        en.put("chemist_dialog_7", "Chemist: Go to the exit!");
        en.put("watchmaker_dialog_1", "Watchmaker: Time is distorted... Need to collect the shards.");
        en.put("watchmaker_dialog_2", "Tap on all the shards. There are %d of them.");
        en.put("watchmaker_dialog_3", "Watchmaker: You collected all the time shards!");
        en.put("watchmaker_dialog_4", "Watchmaker: The Hypercube awaits you. Go to the exit.");
        en.put("chapter3_hint", "← MOVE ROCKET WITH FINGER →");
        en.put("chapter3_evades", "EVADES");
        en.put("time_object_reached", "TIME OBJECT REACHED!");
        en.put("alex_dialog_1", "Alice... Can you hear me?");
        en.put("alex_dialog_2", "This is your father. I recorded this message in case time stops.");
        en.put("alex_dialog_3", "You've always been strong. I knew you'd reach the time object.");
        en.put("alex_dialog_4", "The Hypercube is not just a time machine. It's the key to reality reset.");
        en.put("alex_dialog_5", "But there's a price. If you use it... you'll stay in the past forever.");
        en.put("alex_dialog_6", "The choice is yours, daughter. I'm proud of you, whatever you decide.");
        en.put("choice_title", "CHOICE OF FATE");
        en.put("choice_question", "What will Alice do with the Hypercube?");
        en.put("choice_sacrifice", "🌌 STAY IN THE PAST");
        en.put("choice_sacrifice_desc", "Save father, but disappear from the present");
        en.put("choice_return", "✨ RETURN TO PRESENT");
        en.put("choice_return_desc", "Father stays in the past, world is saved");
        en.put("choice_freeze", "⏰ FREEZE TIME");
        en.put("choice_freeze_desc", "Stop the moment of choice forever");
        en.put("ending_sacrifice_title", "ENDING: SACRIFICE");
        en.put("ending_sacrifice_text", "Alice activates the Hypercube and travels to the past.\n\nShe manages to save her father, but disappears from reality.\nAlex lives and remembers his daughter.\n\nThe time loop is closed.\n\n★ THANK YOU FOR PLAYING! ★");
        en.put("ending_return_title", "ENDING: RETURN");
        en.put("ending_return_text", "Alice returns to the present, taking a particle of the Hypercube.\n\nHer father remains in the past, but the world is saved from the time anomaly.\nAlice continues her journey through time...\n\n★ THANK YOU FOR PLAYING! ★");
        en.put("ending_freeze_title", "ENDING: FREEZE");
        en.put("ending_freeze_text", "Alice freezes time at the moment of choice.\n\nNo one knows what happened next.\nMaybe someday time will flow again...\n\n★ THANK YOU FOR PLAYING! ★");
        en.put("main_menu", "MAIN MENU");
        en.put("exit_game", "EXIT GAME");
        en.put("thanks_for_playing", "★ THANK YOU FOR PLAYING! ★");
        en.put("next_level", "NEXT →");
        en.put("alex", "✧ ALEX (FATHER) ✧");
        en.put("survivor_name", "✧ SURVIVOR ✧");
        en.put("chemist_name", "✧ CHEMIST ✧");
        en.put("watchmaker_name", "✧ WATCHMAKER ✧");
        en.put("hit_message", "-1 EVADE");
        translations.put(ENGLISH, en);
    }

    public String getText(String key) {
        Map<String, String> langMap = translations.get(currentLanguage);
        if (langMap != null && langMap.containsKey(key)) return langMap.get(key);
        return key;
    }

    public String format(String key, Object... args) {
        try {
            return String.format(getText(key), args);
        } catch (Exception e) {
            return getText(key);
        }
    }

    public void setLanguage(String language) {
        if (language.equals(RUSSIAN) || language.equals(ENGLISH)) {
            currentLanguage = language;
            prefs.putString("language", language);
            prefs.putBoolean("language_chosen", true);
            prefs.flush();
        }
    }

    public String getCurrentLanguage() { return currentLanguage; }
    public boolean isLanguageChosen() { return prefs.getBoolean("language_chosen", false); }
}