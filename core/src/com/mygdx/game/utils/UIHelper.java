package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.TheFateGame;

public class UIHelper {
    private TheFateGame game;

    public UIHelper(TheFateGame game) {
        this.game = game;
    }

    public float getScaledX(float x) {
        return x * game.getUIScale();
    }

    public float getScaledY(float y) {
        return y * game.getUIScale();
    }

    public int getScaledSize(int size) {
        return Math.round(size * game.getUIScale());
    }

    public TextButton createScaledButton(String text, int width, int height) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = game.font;

        TextButton button = new TextButton(text, style);
        button.setSize(getScaledSize(width), getScaledSize(height));
        return button;
    }

    public ImageButton createScaledImageButton(Texture texture, int size) {
        int scaledSize = getScaledSize(size);
        ImageButton button = new ImageButton(new TextureRegionDrawable(texture));
        button.setSize(scaledSize, scaledSize);
        return button;
    }

    public Label createScaledLabel(String text, BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        Label label = new Label(text, style);
        return label;
    }

    // Позиционирование относительно экрана (0-1)
    public float getRelativeX(float percent) {
        return TheFateGame.VIRTUAL_WIDTH * percent;
    }

    public float getRelativeY(float percent) {
        return TheFateGame.VIRTUAL_HEIGHT * percent;
    }
}