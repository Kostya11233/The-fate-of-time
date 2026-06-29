package com.thefateoftime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage;
    private Map<String, Map<String, String>> translations;
    private final Preferences prefs;

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

        Map<String, String> ru = new HashMap<>();
        Map<String, String> en = new HashMap<>();

        ru.put("settings", "НАСТРОЙКИ");                         en.put("settings", "SETTINGS");
        ru.put("continue", "ПРОДОЛЖИТЬ");                        en.put("continue", "CONTINUE");
        ru.put("new_game", "НОВАЯ ИГРА");                        en.put("new_game", "NEW GAME");
        ru.put("social", "СОЦСЕТИ");                             en.put("social", "SOCIAL");
        ru.put("exit", "ВЫХОД");                                 en.put("exit", "EXIT");
        ru.put("back", "НАЗАД");                                 en.put("back", "BACK");

        ru.put("select_chapter", "ВЫБЕРИТЕ ГЛАВУ");              en.put("select_chapter", "SELECT CHAPTER");
        ru.put("chapter_1", "ПЕРВАЯ ГЛАВА");                     en.put("chapter_1", "CHAPTER 1");
        ru.put("chapter_2", "ВТОРАЯ ГЛАВА");                     en.put("chapter_2", "CHAPTER 2");
        ru.put("chapter_3", "ТРЕТЬЯ ГЛАВА");                     en.put("chapter_3", "CHAPTER 3");
        ru.put("chapter_locked", "ЗАКРЫТА");                     en.put("chapter_locked", "LOCKED");

        ru.put("music", "МУЗЫКА");                               en.put("music", "MUSIC");
        ru.put("on", "ВКЛ");                                     en.put("on", "ON");
        ru.put("off", "ВЫКЛ");                                   en.put("off", "OFF");
        ru.put("reset_settings", "СБРОСИТЬ НАСТРОЙКИ");          en.put("reset_settings", "RESET SETTINGS");
        ru.put("language", "ЯЗЫК");                              en.put("language", "LANGUAGE");
        ru.put("russian", "РУССКИЙ");                            en.put("russian", "RUSSIAN");
        ru.put("english", "АНГЛИЙСКИЙ");                         en.put("english", "ENGLISH");
        ru.put("volume", "ГРОМКОСТЬ");                           en.put("volume", "VOLUME");

        ru.put("game_paused", "ПАУЗА");                          en.put("game_paused", "GAME PAUSED");
        ru.put("resume", "ПРОДОЛЖИТЬ");                          en.put("resume", "RESUME");
        ru.put("restart_level", "ПЕРЕЗАПУСТИТЬ УРОВЕНЬ");        en.put("restart_level", "RESTART LEVEL");
        ru.put("exit_to_menu", "В МЕНЮ");                        en.put("exit_to_menu", "EXIT TO MENU");

        ru.put("you_died", "ВЫ УМЕРЛИ");                         en.put("you_died", "YOU DIED");
        ru.put("tap_to_continue", "Нажмите для продолжения");    en.put("tap_to_continue", "Tap to continue");
        ru.put("loading", "ЗАГРУЗКА...");                        en.put("loading", "LOADING...");
        ru.put("exit_unlocked", "Выход открыт!");                en.put("exit_unlocked", "Exit unlocked!");
        ru.put("page", "Страница");                              en.put("page", "Page");
        ru.put("of", "из");                                      en.put("of", "of");
        ru.put("failed_to_load_note", "Не удалось открыть записку"); en.put("failed_to_load_note", "Failed to load note");

        ru.put("items_collected", "Предметов: %d / 5");          en.put("items_collected", "Items: %d / 5");
        ru.put("all_items_collected", "ВСЕ ПРЕДМЕТЫ СОБРАНЫ!\nИдите к выходу из главы");  en.put("all_items_collected", "ALL ITEMS COLLECTED!\nGo to the chapter exit");
        ru.put("chapter1_complete_title", "ГЛАВА 1 ПРОЙДЕНА!");  en.put("chapter1_complete_title", "CHAPTER 1 COMPLETE!");
        ru.put("chapter2_unlocked", "ГЛАВА 2 ОТКРЫТА!");         en.put("chapter2_unlocked", "CHAPTER 2 UNLOCKED!");

        ru.put("note_collected", "Найдена записка!");            en.put("note_collected", "Note found!");
        ru.put("no_notes", "У вас пока нет записок!");           en.put("no_notes", "You have no notes yet!");
        ru.put("book_title", "СОБРАННЫЕ ЗАПИСКИ");               en.put("book_title", "COLLECTED NOTES");
        ru.put("note_prefix", "ЗАПИСКА №");                      en.put("note_prefix", "NOTE #");
        ru.put("previous", "ПРЕДЫДУЩАЯ");                        en.put("previous", "PREVIOUS");
        ru.put("next", "СЛЕДУЮЩАЯ");                             en.put("next", "NEXT");
        ru.put("close", "ЗАКРЫТЬ");                              en.put("close", "CLOSE");
        ru.put("open_note", "ОТКРЫТЬ ЗАПИСКУ");                  en.put("open_note", "OPEN NOTE");

        ru.put("note_z1", "ПЕРВАЯ ЗАПИСКА\n\nЯ, Александр, начал этот дневник, чтобы зафиксировать свои исследования временных аномалий.\n\nСегодня я впервые заметил странное мерцание в лаборатории. Возможно, это связано с моими экспериментами с гиперкубом.\n\nНужно быть осторожнее.");
        en.put("note_z1", "FIRST NOTE\n\nI, Alexander, started this diary to document my research on time anomalies.\n\nToday I noticed a strange flickering in the laboratory for the first time. Perhaps this is related to my experiments with the hypercube.\n\nI need to be more careful.");

        ru.put("note_z2", "ВТОРАЯ ЗАПИСКА\n\nВременной объект ведет себя нестабильно. Каждый раз, когда я приближаюсь к нему, часы начинают идти назад.\n\nЯ должен найти способ стабилизировать его, прежде чем произойдет непоправимое.");
        en.put("note_z2", "SECOND NOTE\n\nThe time object is behaving unstable. Every time I approach it, the clocks start running backwards.\n\nI must find a way to stabilize it before something irreversible happens.");

        ru.put("note_z3", "ТРЕТЬЯ ЗАПИСКА\n\nСегодня я понял, что гиперкуб - это не просто машина времени. Он связан с самой тканью реальности.\n\nЕсли я сделаю ошибку, последствия могут быть катастрофическими для всего мира.");
        en.put("note_z3", "THIRD NOTE\n\nToday I realized that the hypercube is not just a time machine. It is connected to the very fabric of reality.\n\nIf I make a mistake, the consequences could be catastrophic for the entire world.");

        ru.put("note_z4", "ЧЕТВЕРТАЯ ЗАПИСКА\n\nМоя дочь Элис... Она единственная, кто понимает важность моей работы.\n\nЯ надеюсь, что если со мной что-то случится, она сможет продолжить мое дело и исправить ошибки.");
        en.put("note_z4", "FOURTH NOTE\n\nMy daughter Alice... She is the only one who understands the importance of my work.\n\nI hope that if something happens to me, she will be able to continue my work and correct the mistakes.");

        ru.put("note_z5", "ПЯТАЯ ЗАПИСКА\n\nВременная аномалия растет. Я чувствую, как время искажается вокруг меня.\n\nНужно собрать все пять артефактов, чтобы активировать гиперкуб и перезагрузить реальность.\n\nЭлис, если ты читаешь это - найди их.");
        en.put("note_z5", "FIFTH NOTE\n\nThe time anomaly is growing. I can feel time distorting around me.\n\nI need to collect all five artifacts to activate the hypercube and reset reality.\n\nAlice, if you are reading this - find them.");

        ru.put("note_z6", "ШЕСТАЯ ЗАПИСКА\n\nСегодня я встретил загадочного незнакомца. Он сказал, что знает способ контролировать время.\n\nНо цена... цена слишком высока. Я не могу рисковать жизнями других людей.");
        en.put("note_z6", "SIXTH NOTE\n\nToday I met a mysterious stranger. He said he knows a way to control time.\n\nBut the price... the price is too high. I cannot risk the lives of others.");

        ru.put("note_z7", "СЕДЬМАЯ ЗАПИСКА\n\nВременные петли становятся все сильнее. Я вижу одни и те же события снова и снова.\n\nКажется, я теряю связь с реальностью. Элис, прости меня за все.");
        en.put("note_z7", "SEVENTH NOTE\n\nThe time loops are getting stronger. I see the same events over and over again.\n\nIt seems I am losing touch with reality. Alice, forgive me for everything.");

        ru.put("note_z8", "ВОСЬМАЯ ЗАПИСКА\n\nЯ нашел способ! Если собрать все осколки времени, можно будет остановить аномалию.\n\nНо для этого нужен проводник - кто-то, кто не боится путешествовать сквозь время.\n\nЭлис, ты единственная, кто может это сделать.");
        en.put("note_z8", "EIGHTH NOTE\n\nI found a way! If you collect all the time shards, you can stop the anomaly.\n\nBut for this you need a guide - someone who is not afraid to travel through time.\n\nAlice, you are the only one who can do this.");

        ru.put("note_z9", "ДЕВЯТАЯ ЗАПИСКА\n\nВременной объект показал мне будущее. Мир в опасности, если мы не вмешаемся.\n\nУ нас есть всего несколько дней, чтобы все исправить.\n\nЯ верю в тебя, дочка.");
        en.put("note_z9", "NINTH NOTE\n\nThe time object showed me the future. The world is in danger if we do not intervene.\n\nWe have only a few days to fix everything.\n\nI believe in you, daughter.");

        ru.put("note_z10", "ДЕСЯТАЯ ЗАПИСКА\n\nЭто последняя запись. Я отправляюсь в прошлое, чтобы предотвратить катастрофу.\n\nЕсли я не вернусь, найди гиперкуб и заверши начатое.\n\nПомни: время - это не враг. Время - это дар. Используй его с умом.\n\nС любовью, твой отец.");
        en.put("note_z10", "TENTH NOTE\n\nThis is the last entry. I am going to the past to prevent the catastrophe.\n\nIf I do not return, find the hypercube and complete what I started.\n\nRemember: time is not an enemy. Time is a gift. Use it wisely.\n\nWith love, your father.");

        ru.put("level_1_title", "УРОВЕНЬ 1\nЗаброшенная лаборатория");  en.put("level_1_title", "LEVEL 1\nAbandoned Laboratory");
        ru.put("level_2_title", "УРОВЕНЬ 2\nЗона экспериментов");       en.put("level_2_title", "LEVEL 2\nExperiment Zone");
        ru.put("level_3_title", "УРОВЕНЬ 3\nХранилище времени");        en.put("level_3_title", "LEVEL 3\nTime Vault");
        ru.put("quests", "ЗАДАНИЯ");                                     en.put("quests", "QUESTS");
        ru.put("chapter2_complete", "ГЛАВА 2 ПРОЙДЕНА!\nЗАГРУЗКА ГЛАВЫ 3...");  en.put("chapter2_complete", "CHAPTER 2 COMPLETE!\nLOADING CHAPTER 3...");

        ru.put("trash_task", "Собрать мусор (%d/%d)");            en.put("trash_task", "Collect trash (%d/%d)");
        ru.put("talk_to_survivor", "Поговорить с Выжившим");      en.put("talk_to_survivor", "Talk to Survivor");
        ru.put("go_to_exit", "Идти к выходу");                    en.put("go_to_exit", "Go to exit");
        ru.put("talk_to_chemist", "Поговорить с Химиком");        en.put("talk_to_chemist", "Talk to Chemist");
        ru.put("find_parts", "Найти запчасти (%d/3)");            en.put("find_parts", "Find parts (%d/3)");
        ru.put("return_to_chemist", "Вернуться к Химику");        en.put("return_to_chemist", "Return to Chemist");
        ru.put("talk_to_watchmaker", "Поговорить с Часовщиком");  en.put("talk_to_watchmaker", "Talk to Watchmaker");
        ru.put("collect_shards", "Собрать осколки (%d/%d)");      en.put("collect_shards", "Collect shards (%d/%d)");
        ru.put("find_parts_message", "Найди 3 запчасти на карте!"); en.put("find_parts_message", "Find 3 parts on the map!");

        ru.put("trash_minigame_title", "СОБЕРИ МУСОР");           en.put("trash_minigame_title", "COLLECT TRASH");
        ru.put("trash_minigame_counter", "Собрано: %d/%d");       en.put("trash_minigame_counter", "Collected: %d/%d");
        ru.put("shard_minigame_title", "СОБЕРИ ОСКОЛКИ");         en.put("shard_minigame_title", "COLLECT SHARDS");
        ru.put("shard_minigame_counter", "Собрано: %d/%d");       en.put("shard_minigame_counter", "Collected: %d/%d");
        ru.put("exit_minigame", "ВЫЙТИ");                         en.put("exit_minigame", "EXIT");
        ru.put("trash_collected_message", "Мусор собран! %d/%d"); en.put("trash_collected_message", "Trash collected! %d/%d");
        ru.put("shard_collected_message", "Осколок собран! %d/%d"); en.put("shard_collected_message", "Shard collected! %d/%d");
        ru.put("all_trash_collected", "Поздравляю! Весь мусор собран!"); en.put("all_trash_collected", "Congratulations! All trash collected!");
        ru.put("all_shards_collected", "Поздравляю! Все осколки собраны!"); en.put("all_shards_collected", "Congratulations! All shards collected!");
        ru.put("return_to_npc", "Вернись к %s!");                 en.put("return_to_npc", "Return to %s!");
        ru.put("part_collected", "Запчасть %d/3");                en.put("part_collected", "Part %d/3");
        ru.put("all_parts_collected", "Все запчасти собраны! Вернись к Химику."); en.put("all_parts_collected", "All parts collected! Return to Chemist.");

        ru.put("survivor_dialog_1", "Выживший: Помоги! Нужно собрать мусор в корзину.");  en.put("survivor_dialog_1", "Survivor: Help! We need to collect trash into the bin.");
        ru.put("survivor_dialog_2", "Перетащи мусор пальцем в корзину. Нужно %d штук."); en.put("survivor_dialog_2", "Drag the trash with your finger to the bin. Need %d pieces.");
        ru.put("survivor_dialog_3", "Выживший: Спасибо! Ты очистила зону от мусора!");   en.put("survivor_dialog_3", "Survivor: Thank you! You cleared the area of trash!");
        ru.put("survivor_dialog_4", "Выживший: Чтобы предотвратить аномалию, нужно найти временной объект."); en.put("survivor_dialog_4", "Survivor: To prevent the anomaly, you need to find the time object.");
        ru.put("survivor_dialog_5", "Выживший: Иди к выходу! Удачи!");                    en.put("survivor_dialog_5", "Survivor: Go to the exit! Good luck!");

        ru.put("chemist_dialog_1", "Химик: Эксперимент вышел из-под контроля...");       en.put("chemist_dialog_1", "Chemist: The experiment is out of control...");
        ru.put("chemist_dialog_2", "Химик: Чтобы остановить аномалию, найди 3 запчасти."); en.put("chemist_dialog_2", "Chemist: To stop the anomaly, find 3 parts.");
        ru.put("chemist_dialog_3", "Химик: Это ГИПЕРКУБ!");                              en.put("chemist_dialog_3", "Chemist: It's the HYPERCUBE!");
        ru.put("chemist_dialog_4", "Химик: Нужно найти 3 запчасти. Ищи их по лаборатории."); en.put("chemist_dialog_4", "Chemist: You need to find 3 parts. Look around the lab.");
        ru.put("chemist_dialog_5", "Химик: Отлично! Все запчасти на месте!");           en.put("chemist_dialog_5", "Chemist: Excellent! All parts are in place!");
        ru.put("chemist_dialog_6", "Химик: Временной объект, который ты ищешь - это ГИПЕРКУБ."); en.put("chemist_dialog_6", "Chemist: The time object you're looking for is the HYPERCUBE.");
        ru.put("chemist_dialog_7", "Химик: Иди к выходу!");                              en.put("chemist_dialog_7", "Chemist: Go to the exit!");

        ru.put("watchmaker_dialog_1", "Часовщик: Время искажено... Нужно собрать осколки."); en.put("watchmaker_dialog_1", "Watchmaker: Time is distorted... Need to collect the shards.");
        ru.put("watchmaker_dialog_2", "Нажми на все осколки. Их %d штук.");              en.put("watchmaker_dialog_2", "Tap on all the shards. There are %d of them.");
        ru.put("watchmaker_dialog_3", "Часовщик: Ты собрала все осколки времени!");     en.put("watchmaker_dialog_3", "Watchmaker: You collected all the time shards!");
        ru.put("watchmaker_dialog_4", "Часовщик: Гиперкуб ждет тебя. Иди к выходу.");    en.put("watchmaker_dialog_4", "Watchmaker: The Hypercube awaits you. Go to the exit.");

        ru.put("chapter3_hint", "← ПЕРЕМЕЩАЙ РАКЕТУ ПАЛЬЦЕМ →");  en.put("chapter3_hint", "← MOVE ROCKET WITH FINGER →");
        ru.put("chapter3_evades", "УКЛОНЕНИЙ");                    en.put("chapter3_evades", "EVADES");
        ru.put("time_object_reached", "ВРЕМЕННОЙ ОБЪЕКТ ДОСТИГНУТ!"); en.put("time_object_reached", "TIME OBJECT REACHED!");
        ru.put("hit_message", "-1 УКЛОНЕНИЕ");                     en.put("hit_message", "-1 EVADE");

        ru.put("alex_dialog_1", "Элис... Ты слышишь меня?");      en.put("alex_dialog_1", "Alice... Can you hear me?");
        ru.put("alex_dialog_2", "Это твой отец. Я записал это сообщение на случай, если время остановится."); en.put("alex_dialog_2", "This is your father. I recorded this message in case time stops.");
        ru.put("alex_dialog_3", "Ты всегда была сильной. Я знал, что ты доберешься до временного объекта."); en.put("alex_dialog_3", "You've always been strong. I knew you'd reach the time object.");
        ru.put("alex_dialog_4", "Гиперкуб - это не просто машина времени. Это ключ к перезагрузке реальности."); en.put("alex_dialog_4", "The Hypercube is not just a time machine. It's the key to reality reset.");
        ru.put("alex_dialog_5", "Но есть цена. Если ты используешь его... ты останешься в прошлом навсегда."); en.put("alex_dialog_5", "But there's a price. If you use it... you'll stay in the past forever.");
        ru.put("alex_dialog_6", "Выбор за тобой, дочка. Я горжусь тобой, что бы ты ни решила."); en.put("alex_dialog_6", "The choice is yours, daughter. I'm proud of you, whatever you decide.");

        ru.put("choice_title", "ВЫБОР СУДЬБЫ");                    en.put("choice_title", "CHOICE OF FATE");
        ru.put("choice_question", "Что сделает Элис с Гиперкубом?"); en.put("choice_question", "What will Alice do with the Hypercube?");
        ru.put("choice_sacrifice", "🌌 ОСТАТЬСЯ В ПРОШЛОМ");       en.put("choice_sacrifice", "🌌 STAY IN THE PAST");
        ru.put("choice_sacrifice_desc", "Спасти отца, но исчезнуть из настоящего"); en.put("choice_sacrifice_desc", "Save father, but disappear from the present");
        ru.put("choice_return", "✨ ВЕРНУТЬСЯ В НАСТОЯЩЕЕ");       en.put("choice_return", "✨ RETURN TO PRESENT");
        ru.put("choice_return_desc", "Отец останется в прошлом, мир спасён"); en.put("choice_return_desc", "Father stays in the past, world is saved");
        ru.put("choice_freeze", "⏰ ЗАМОРОЗИТЬ ВРЕМЯ");            en.put("choice_freeze", "⏰ FREEZE TIME");
        ru.put("choice_freeze_desc", "Остановить момент выбора навсегда"); en.put("choice_freeze_desc", "Stop the moment of choice forever");

        ru.put("ending_sacrifice_title", "КОНЦОВКА: ЖЕРТВА");      en.put("ending_sacrifice_title", "ENDING: SACRIFICE");
        ru.put("ending_sacrifice_text", "Элис активирует Гиперкуб и переносится в прошлое.\n\nОна успевает спасти отца, но сама исчезает из реальности.\nАлекс живёт и помнит свою дочь.\n\nВременная петля замкнулась.\n\n★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("ending_return_title", "КОНЦОВКА: ВОЗВРАЩЕНИЕ");    en.put("ending_return_title", "ENDING: RETURN");
        ru.put("ending_return_text", "Элис возвращается в настоящее, забрав частицу Гиперкуба.\n\nОтец остался в прошлом, но мир спасён от временной аномалии.\nЭлис продолжает своё путешествие сквозь время...\n\n★ СПАСИБО ЗА ИГРУ! ★");
        ru.put("ending_freeze_title", "КОНЦОВКА: ЗАМОРОЗКА");      en.put("ending_freeze_title", "ENDING: FREEZE");
        ru.put("ending_freeze_text", "Элис замораживает время в момент выбора.\n\nНикто не знает, что произошло дальше.\nМожет быть, когда-нибудь время снова пойдёт...\n\n★ СПАСИБО ЗА ИГРУ! ★");

        ru.put("main_menu", "В ГЛАВНОЕ МЕНЮ");                    en.put("main_menu", "MAIN MENU");
        ru.put("exit_game", "ВЫЙТИ ИЗ ИГРЫ");                     en.put("exit_game", "EXIT GAME");
        ru.put("thanks_for_playing", "★ СПАСИБО ЗА ИГРУ! ★");    en.put("thanks_for_playing", "★ THANK YOU FOR PLAYING! ★");
        ru.put("next_level", "ДАЛЕЕ →");                          en.put("next_level", "NEXT →");

        ru.put("alex", "✧ АЛЕКС (ОТЕЦ) ✧");                      en.put("alex", "✧ ALEX (FATHER) ✧");
        ru.put("survivor_name", "✧ ВЫЖИВШИЙ ✧");                 en.put("survivor_name", "✧ SURVIVOR ✧");
        ru.put("chemist_name", "✧ ХИМИК ✧");                     en.put("chemist_name", "✧ CHEMIST ✧");
        ru.put("watchmaker_name", "✧ ЧАСОВЩИК ✧");               en.put("watchmaker_name", "✧ WATCHMAKER ✧");
        ru.put("note_collected", "Собрано");                      en.put("note_collected", "Collected");

        translations.put(RUSSIAN, ru);
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

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isLanguageChosen() {
        return prefs.getBoolean("language_chosen", false);
    }
}