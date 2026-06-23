package com.thefateoftime;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("The Fate of Time");
		config.setWindowedMode(1280, 720);
		config.setResizable(true);
		config.setMaximized(false);
		config.useVsync(true);
		config.setForegroundFPS(60);
		new Lwjgl3Application(new TheFateGame(), config);
	}
}