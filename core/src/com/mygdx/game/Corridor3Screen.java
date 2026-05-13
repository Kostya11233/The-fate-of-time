package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Corridor3Screen extends BaseGameScreen {

    public Corridor3Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public Corridor3Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid3.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor3Screen.setupDoorTransitions: " + doorId);

        // door31 (ID 1) -> комната k5 (выход на X=640)
        if (doorId.equals("door31") || doorId.equals("31") || doorId.equals("1")) {
            targetMap = "room/k5.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в k5 на X=640");
        }
        // door32 (ID 2) -> комната k6 (выход на X=1280)
        else if (doorId.equals("door32") || doorId.equals("32") || doorId.equals("2")) {
            targetMap = "room/k6.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в k6 на X=640");
        }
        // door33 (ID 4) -> коридор 4 (спавн на X=640)
        else if (doorId.equals("door33") || doorId.equals("33") || doorId.equals("4")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в corid4 на X=640");
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

        // Выход из комнаты k5 (ID 1) -> обратно к door31
        if (exitId.equals("exit5") || exitId.equals("5") || exitId.equals("1")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 640;  // door31
            targetY = fixedY;
            System.out.println("  -> выход из k5 в corid3 на X=640");
        }
        // Выход из комнаты k6 (ID 2) -> обратно к door32
        else if (exitId.equals("exit6") || exitId.equals("6") || exitId.equals("2")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1280;  // door32
            targetY = fixedY;
            System.out.println("  -> выход из k6 в corid3 на X=1280");
        }
        // Выход из коридора 4 обратно к door33
        else if (exitId.equals("exit33") || exitId.equals("33_exit") || exitId.equals("4")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1920;  // door33 (правая дверь)
            targetY = fixedY;
            System.out.println("  -> выход из corid4 в corid3 на X=1920");
        }
    }
}