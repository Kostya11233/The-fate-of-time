package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Corridor3Screen extends BaseGameScreen {

    public Corridor3Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
        this.fixedY = 220; // выше на 20 пикселей
        if (!loadSavedGame) {
            float spawnX = readSpawnPointById(6);
            this.position.set(spawnX, fixedY);
            this.camera.position.set(position.x, 360, 0);
        }
    }

    public Corridor3Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame, startX);
        this.fixedY = 200;
        this.position.y = fixedY;
        if (camera != null) camera.position.set(position.x, 360, 0);
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid3.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor3Screen.setupDoorTransitions: " + doorId);

        if (doorId.equals("door31") || doorId.equals("31") || doorId.equals("1")) {
            targetMap = "room/k5.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door32") || doorId.equals("32") || doorId.equals("2")) {
            targetMap = "room/k6.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door33") || doorId.equals("33") || doorId.equals("4")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 3896;
            targetY = fixedY;
        }
        else if (doorId.equals("doore2") || doorId.equals("door2") || doorId.equals("2")) {
            targetMap = "cormap/corid2.tmx";
            targetX = 236;
            targetY = fixedY;
        }
        else {
            System.out.println("  -> НЕИЗВЕСТНАЯ ДВЕРЬ: " + doorId);
            targetMap = null;
            return;
        }

        if (targetMap != null && !Gdx.files.internal(targetMap).exists()) {
            System.out.println("  -> Файл не найден: " + targetMap);
            targetMap = null;
        }
    }

    @Override
    protected void setupExitTransitions(String exitId) {
        System.out.println("Corridor3Screen.setupExitTransitions: " + exitId);

        if (exitId.equals("exit5") || exitId.equals("5") || exitId.equals("1")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (exitId.equals("exit6") || exitId.equals("6") || exitId.equals("2")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1280;
            targetY = fixedY;
        }
        else if (exitId.equals("exit33") || exitId.equals("33_exit") || exitId.equals("4")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1920;
            targetY = fixedY;
        }
        else {
            targetMap = "cormap/corid3.tmx";
            targetX = 640;
            targetY = fixedY;
        }
    }
}