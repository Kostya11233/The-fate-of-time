package com.mygdx.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;

public class DialogSystem {
    private TheFateGame game;
    private Stage dialogStage;
    private Texture dialogBgTexture;
    private boolean showing = false;
    private DialogLine currentLine;
    private int currentIndex;
    private DialogCallback callback;

    public static class DialogLine {
        public String speaker;
        public String text;
        public Runnable action;

        public DialogLine(String speaker, String text) {
            this.speaker = speaker;
            this.text = text;
            this.action = null;
        }

        public DialogLine(String speaker, String text, Runnable action) {
            this.speaker = speaker;
            this.text = text;
            this.action = action;
        }
    }

    public interface DialogCallback {
        void onDialogEnd();
    }

    public DialogSystem(TheFateGame game) {
        this.game = game;
        this.dialogStage = new Stage(new ExtendViewport(TheFateGame.VIRTUAL_WIDTH, TheFateGame.VIRTUAL_HEIGHT));
        createDialogBackground();
    }

    private void createDialogBackground() {
        try {
            dialogBgTexture = new Texture("button1.png");
        } catch (Exception e) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(400, 150, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.2f, 0.2f, 0.3f, 0.95f);
            pixmap.fill();
            dialogBgTexture = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    public void showDialog(DialogLine[] lines, DialogCallback callback) {
        this.currentLine = null;
        this.currentIndex = 0;
        this.callback = callback;
        this.showing = true;

        dialogStage.clear();

        // Темный фон
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.6f);
        dialogStage.addActor(darkBg);

        showNextLine();

        Gdx.input.setInputProcessor(dialogStage);
    }

    private void showNextLine() {
        if (currentIndex >= getLines().length) {
            closeDialog();
            return;
        }

        currentLine = getLines()[currentIndex];
        currentIndex++;

        dialogStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.6f);
        dialogStage.addActor(darkBg);

        // Диалоговое окно
        Table dialogTable = new Table();
        dialogTable.setFillParent(true);
        dialogStage.addActor(dialogTable);

        Table dialogBox = new Table();
        dialogBox.setBackground(new TextureRegionDrawable(dialogBgTexture));
        dialogBox.pad(20);

        Label.LabelStyle nameStyle = new Label.LabelStyle();
        nameStyle.font = game.font;
        nameStyle.fontColor = Color.GOLD;

        Label.LabelStyle textStyle = new Label.LabelStyle();
        textStyle.font = game.smallFont;
        textStyle.fontColor = Color.WHITE;

        Label nameLabel = new Label(currentLine.speaker, nameStyle);
        Label textLabel = new Label(currentLine.text, textStyle);
        textLabel.setWrap(true);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = game.smallFont;
        TextButton nextBtn = new TextButton(game.languageManager.getText("next"), btnStyle);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showNextLine();
            }
        });

        dialogBox.add(nameLabel).padBottom(10).row();
        dialogBox.add(textLabel).width(600).padBottom(15).row();
        dialogBox.add(nextBtn).width(100).height(40);

        dialogTable.add(dialogBox).center().bottom().padBottom(50);
    }

    private DialogLine[] getLines() {
        // Этот метод должен быть переопределен или передан при создании
        return new DialogLine[0];
    }

    private void closeDialog() {
        showing = false;
        dialogStage.clear();
        if (currentLine != null && currentLine.action != null) {
            currentLine.action.run();
        }
        if (callback != null) {
            callback.onDialogEnd();
        }
        Gdx.input.setInputProcessor(null);
    }

    public void actAndDraw(float delta) {
        if (showing) {
            dialogStage.act(delta);
            dialogStage.draw();
        }
    }

    public boolean isShowing() {
        return showing;
    }

    public void dispose() {
        dialogStage.dispose();
        if (dialogBgTexture != null) dialogBgTexture.dispose();
    }
}