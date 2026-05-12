package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SettingsScreen implements Screen {
    private final TheFateGame game;
    private final StartMenuScreen menuScreen;
    private Stage stage;

    private Label titleLabel;
    private Label volumeLabel;
    private Label languageLabel;
    private TextButton soundToggle;
    private TextButton languageRuBtn;
    private TextButton languageEnBtn;
    private TextButton resetBtn;
    private TextButton backBtn;

    // Кнопки для громкости
    private TextButton volumeMinusBtn;
    private TextButton volumePlusBtn;
    private Table volumeTable;

    public SettingsScreen(TheFateGame game, StartMenuScreen menuScreen) {
        this.game = game;
        this.menuScreen = menuScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Стили
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.font;

        // Заголовок
        titleLabel = new Label(game.languageManager.getText("settings"), labelStyle);
        titleLabel.setFontScale(2f);

        // Громкость - заголовок
        volumeLabel = new Label(game.languageManager.getText("volume") + ":", labelStyle);

        // Текущее значение громкости
        final Label volumeValueLabel = new Label((int)(game.volume * 100) + "%", labelStyle);
        volumeValueLabel.setFontScale(1.5f);

        // Кнопка МИНУС
        volumeMinusBtn = new TextButton("-", buttonStyle);
        volumeMinusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.volume = Math.max(0, game.volume - 0.1f);
                volumeValueLabel.setText((int)(game.volume * 100) + "%");
                if (game.menuMusic != null && game.musicEnabled) {
                    game.menuMusic.setVolume(game.volume);
                }
                game.saveSettings();
            }
        });

        // Кнопка ПЛЮС
        volumePlusBtn = new TextButton("+", buttonStyle);
        volumePlusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.volume = Math.min(1, game.volume + 0.1f);
                volumeValueLabel.setText((int)(game.volume * 100) + "%");
                if (game.menuMusic != null && game.musicEnabled) {
                    game.menuMusic.setVolume(game.volume);
                }
                game.saveSettings();
            }
        });

        // Таблица для громкости
        volumeTable = new Table();
        volumeTable.add(volumeMinusBtn).width(60).height(50).padRight(20);
        volumeTable.add(volumeValueLabel).padRight(20);
        volumeTable.add(volumePlusBtn).width(60).height(50);

        // Кнопка включения/выключения музыки
        String soundText = game.languageManager.getText("music") + ": " +
                (game.musicEnabled ? game.languageManager.getText("on") : game.languageManager.getText("off"));
        soundToggle = new TextButton(soundText, buttonStyle);
        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.musicEnabled = !game.musicEnabled;
                soundToggle.setText(game.languageManager.getText("music") + ": " +
                        (game.musicEnabled ? game.languageManager.getText("on") : game.languageManager.getText("off")));
                if (game.menuMusic != null) {
                    if (game.musicEnabled) {
                        game.menuMusic.play();
                        game.menuMusic.setVolume(game.volume);
                    } else {
                        game.menuMusic.pause();
                    }
                }
                game.saveSettings();
            }
        });

        // Выбор языка
        languageLabel = new Label(game.languageManager.getText("language") + ":", labelStyle);

        // Кнопка Русский
        languageRuBtn = new TextButton(game.languageManager.getText("russian"), buttonStyle);
        languageRuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.languageManager.setLanguage(LanguageManager.RUSSIAN);
                refreshUI();
                if (menuScreen != null) {
                    menuScreen.refreshButtonTexts();
                }
            }
        });

        // Кнопка Английский
        languageEnBtn = new TextButton(game.languageManager.getText("english"), buttonStyle);
        languageEnBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.languageManager.setLanguage(LanguageManager.ENGLISH);
                refreshUI();
                if (menuScreen != null) {
                    menuScreen.refreshButtonTexts();
                }
            }
        });

        // Таблица для кнопок языка
        Table languageTable = new Table();
        languageTable.add(languageRuBtn).width(160).height(50).padRight(60);
        languageTable.add(languageEnBtn).width(160).height(50);

        // Кнопка сброса
        resetBtn = new TextButton(game.languageManager.getText("reset_settings"), buttonStyle);
        resetBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.volume = 0.7f;
                game.musicEnabled = true;
                volumeValueLabel.setText((int)(game.volume * 100) + "%");
                soundToggle.setText(game.languageManager.getText("music") + ": " + game.languageManager.getText("on"));
                if (game.menuMusic != null) {
                    game.menuMusic.setVolume(game.volume);
                    if (game.musicEnabled) {
                        game.menuMusic.play();
                    }
                }
                game.saveSettings();
            }
        });

        // Кнопка назад
        backBtn = new TextButton(game.languageManager.getText("back"), buttonStyle);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(menuScreen);
            }
        });

        // Собираем всё в таблицу
        table.add(titleLabel).padBottom(50).row();
        table.add(volumeLabel).padBottom(15).row();
        table.add(volumeTable).padBottom(30).row();
        table.add(soundToggle).padBottom(30).width(280).height(55).row();
        table.add(languageLabel).padBottom(15).row();
        table.add(languageTable).padBottom(45).row();
        table.add(resetBtn).padBottom(30).width(280).height(55).row();
        table.add(backBtn).padTop(10).width(220).height(65).row();
        table.center();
    }

    private void refreshUI() {
        titleLabel.setText(game.languageManager.getText("settings"));
        volumeLabel.setText(game.languageManager.getText("volume") + ":");
        soundToggle.setText(game.languageManager.getText("music") + ": " +
                (game.musicEnabled ? game.languageManager.getText("on") : game.languageManager.getText("off")));
        languageLabel.setText(game.languageManager.getText("language") + ":");
        languageRuBtn.setText(game.languageManager.getText("russian"));
        languageEnBtn.setText(game.languageManager.getText("english"));
        resetBtn.setText(game.languageManager.getText("reset_settings"));
        backBtn.setText(game.languageManager.getText("back"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}