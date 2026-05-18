package com.mygdx.game;

import com.badlogic.gdx.Gdx;

public class Corridor4Screen extends BaseGameScreen {

    public Corridor4Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
        this.fixedY = 160;
        if (!loadSavedGame) {
            float spawnX = readSpawnPointById(7);
            this.position.set(spawnX, fixedY);
            this.camera.position.set(position.x, 360, 0);
        }
    }

    // ВАЖНО: правильный конструктор с startX
    public Corridor4Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame, startX);  // вызываем родительский с startX
        this.fixedY = 160;
        // Родитель установил позицию на startX, но Y по умолчанию 180, корректируем
        if (this.position != null) {
            this.position.y = fixedY;
            this.camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid4.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor4Screen.setupDoorTransitions: " + doorId);
        if (doorId.equals("door41") || doorId.equals("41") || doorId.equals("5")) {
            targetMap = "room/k8.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door42") || doorId.equals("42") || doorId.equals("4")) {
            targetMap = "room/k9.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door43") || doorId.equals("43") || doorId.equals("3")) {
            targetMap = "room/k7.tmx";
            targetX = 640;
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
        System.out.println("Corridor4Screen.setupExitTransitions: " + exitId);
        if (exitId.equals("exit8") || exitId.equals("8") || exitId.equals("1")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (exitId.equals("exit9") || exitId.equals("9") || exitId.equals("2")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 1280;
            targetY = fixedY;
        }
        else if (exitId.equals("exit7") || exitId.equals("7") || exitId.equals("1")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 1920;
            targetY = fixedY;
        }
        else if (exitId.equals("doore3") || exitId.equals("door3") || exitId.equals("3")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 3920;
            targetY = fixedY;
        }
        else if (exitId.equals("exit41") || exitId.equals("41_exit")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1920;
            targetY = fixedY;
        }
        else {
            targetMap = "cormap/corid4.tmx";
            targetX = 640;
            targetY = fixedY;
        }
    }
}