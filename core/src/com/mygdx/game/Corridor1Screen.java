package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class Corridor1Screen extends BaseGameScreen {

    public Corridor1Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
        if (!loadSavedGame) {
            // После загрузки карты устанавливаем позицию на спавн ID 26
            float spawnX = readSpawnPointById(26);
            this.position.set(spawnX, fixedY);
            this.camera.position.set(position.x, 360, 0);
        }
    }

    public Corridor1Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame, startX);
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid1.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor1Screen.setupDoorTransitions: " + doorId);
        lastUsedDoorId = doorId;
        if (doorId.equals("door1") || doorId.equals("1") || doorId.equals("11")) {
            targetMap = "room/k1.tmx";
            targetX = 640;
            targetY = fixedY;
            lastDoorX = 640;
        }
        else if (doorId.equals("door2") || doorId.equals("2") || doorId.equals("12")) {
            targetMap = "room/k2.tmx";
            targetX = 640;
            targetY = fixedY;
            lastDoorX = 1400;
        }
        else if (doorId.equals("door3") || doorId.equals("3") || doorId.equals("13")) {
            targetMap = "cormap/corid2.tmx";
            targetX = 272;
            targetY = fixedY;
            lastDoorX = 640;
        }
        else {
            targetMap = currentMap;
            targetX = position.x;
            targetY = fixedY;
        }
    }

    @Override
    protected void setupExitTransitions(String exitId) {
        System.out.println("Corridor1Screen.setupExitTransitions: " + exitId);
        if (exitId.equals("exit1") || exitId.equals("1") || exitId.equals("k1_exit")) {
            targetMap = "cormap/corid1.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (exitId.equals("exit2") || exitId.equals("2") || exitId.equals("k2_exit")) {
            targetMap = "cormap/corid1.tmx";
            targetX = 1400;
            targetY = fixedY;
        }
        else {
            targetMap = "cormap/corid1.tmx";
            targetX = 640;
            targetY = fixedY;
        }
    }
}