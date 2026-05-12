package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SettingsScreen implements Screen {
    private final TheFateGame game;
    private final StartMenuScreen menuScreen;
    private Stage stage;

    private Label volumeLabel;
    private Slider volumeSlider;
    private TextButton musicToggleBtn;

    public SettingsScreen(TheFateGame game, StartMenuScreen menuScreen) {
        this.game = game;
        this.menuScreen = menuScreen;
        this.stage = new Stage(new ExtendViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Заголовок
        Label titleLabel = new Label("SETTINGS", new Label.LabelStyle(game.font, null));
        titleLabel.setFontScale(2f);

        // Ползунок громкости
        volumeLabel = new Label("Volume: " + (int)(game.volume * 100) + "%", new Label.LabelStyle(game.font, null));

        volumeSlider = new Slider(0f, 1f, 0.01f, false, new Slider.SliderStyle());
        volumeSlider.setValue(game.volume);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.volume = volumeSlider.getValue();
                volumeLabel.setText("Volume: " + (int)(game.volume * 100) + "%");
                if (game.menuMusic != null && game.musicEnabled) {
                    game.menuMusic.setVolume(game.volume);
                }
                game.saveSettings();
            }
        });

        // Кнопка включения/выключения музыки
        String musicText = game.musicEnabled ? "Music: ON" : "Music: OFF";
        musicToggleBtn = new TextButton(musicText, new TextButton.TextButtonStyle());
        musicToggleBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.musicEnabled = !game.musicEnabled;
                musicToggleBtn.setText(game.musicEnabled ? "Music: ON" : "Music: OFF");
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

        // Кнопка сброса
        TextButton resetBtn = new TextButton("Reset", new TextButton.TextButtonStyle());
        resetBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.volume = 0.7f;
                game.musicEnabled = true;
                volumeSlider.setValue(game.volume);
                volumeLabel.setText("Volume: 70%");
                musicToggleBtn.setText("Music: ON");
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
        TextButton backBtn = new TextButton("BACK", new TextButton.TextButtonStyle());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(menuScreen);
            }
        });

        table.add(titleLabel).padBottom(40).row();
        table.add(volumeLabel).padBottom(10).row();
        table.add(volumeSlider).width(400).padBottom(20).row();
        table.add(musicToggleBtn).padBottom(20).width(200).row();
        table.add(resetBtn).padBottom(20).width(200).row();
        table.add(backBtn).padTop(30).width(200).row();
        table.center();
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
    public void dispose() {
        stage.dispose();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}