package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class MovingBackground {
    Texture texture;
    public MovingBackground() {
        texture = new Texture("MovingBackground_game.jpg");
    }

    public void draw(Batch batch) {
        batch.draw(texture, 0, 0);
    }

    public void dispose() {
        texture.dispose();
    }
}