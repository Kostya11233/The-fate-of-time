package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;

public class MovingBackground {
    Texture texture;

    public MovingBackground() {
        texture = new Texture("moving.png");
    }

    public void draw(Batch batch, OrthographicCamera camera) {
        // Растягиваем фон на весь размер камеры
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;
        batch.draw(texture, 0, 0, width, height);
    }

    public void dispose() {
        texture.dispose();
    }
}