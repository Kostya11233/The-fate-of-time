package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Corridor4Screen extends BaseGameScreen {

    public Corridor4Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public Corridor4Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid4.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor4Screen.setupDoorTransitions: " + doorId);

        // door41 (ID 5) -> комната k8
        if (doorId.equals("door41") || doorId.equals("41") || doorId.equals("5")) {
            targetMap = "room/k8.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в k8 на X=640");
        }
        // door42 (ID 4) -> комната k9
        else if (doorId.equals("door42") || doorId.equals("42") || doorId.equals("4")) {
            targetMap = "room/k9.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в k9 на X=640");
        }
        // door43 (ID 3) -> комната k7
        else if (doorId.equals("door43") || doorId.equals("43") || doorId.equals("3")) {
            targetMap = "room/k7.tmx";
            targetX = 640;
            targetY = fixedY;
            System.out.println("  -> переход в k7 на X=640");
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

        // Выход из комнаты k8 (ID 1) -> обратно к door41
        if (exitId.equals("exit8") || exitId.equals("8") || exitId.equals("1")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 640;  // door41
            targetY = fixedY;
            System.out.println("  -> выход из k8 в corid4 на X=640");
        }
        // Выход из комнаты k9 (ID 2) -> обратно к door42
        else if (exitId.equals("exit9") || exitId.equals("9") || exitId.equals("2")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 1280;  // door42
            targetY = fixedY;
            System.out.println("  -> выход из k9 в corid4 на X=1280");
        }
        // Выход из комнаты k7 (ID 1) -> обратно к door43
        else if (exitId.equals("exit7") || exitId.equals("7") || exitId.equals("1")) {
            targetMap = "cormap/corid4.tmx";
            targetX = 1920;  // door43
            targetY = fixedY;
            System.out.println("  -> выход из k7 в corid4 на X=1920");
        }
        // Выход из коридора 4 обратно в corid3
        else if (exitId.equals("exit41") || exitId.equals("41_exit")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 1920;  // door33
            targetY = fixedY;
            System.out.println("  -> выход из corid4 в corid3 на X=1920");
        }
    }
}