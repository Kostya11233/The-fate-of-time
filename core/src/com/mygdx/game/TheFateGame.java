package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TheFateGame extends Game {
	public SpriteBatch batch;
	public OrthographicCamera camera;
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;

	@Override
	public void create() {
		batch = new SpriteBatch();


		SCREEN_WIDTH = Gdx.graphics.getWidth();
		SCREEN_HEIGHT = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
		setScreen(new StartMenuScreen(this));
	}

	@Override
	public void resize(int width, int height) {

		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		camera.setToOrtho(false, width, height);
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}