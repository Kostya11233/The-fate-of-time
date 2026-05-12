package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class MovingBackground {
    Texture texture;
    float xOffset = 0;
    float speed = 50f;

    public MovingBackground() {
        texture = new Texture("moving.png");
    }

    public void draw(Batch batch, OrthographicCamera camera, float delta) {
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;

        xOffset += speed * delta;
        if (xOffset >= width) {
            xOffset = 0;
        }

        batch.draw(texture, -xOffset, 0, width, height);
        batch.draw(texture, width - xOffset, 0, width, height);
    }

    public void dispose() {
        texture.dispose();
    }
}