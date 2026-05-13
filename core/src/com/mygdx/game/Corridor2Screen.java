package com.mygdx.game;

import com.badlogic.gdx.Gdx;

public class Corridor2Screen extends BaseGameScreen {

    public Corridor2Screen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public Corridor2Screen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame, startX);
    }

    @Override
    protected String getDefaultMap() {
        return "cormap/corid2.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        System.out.println("Corridor2Screen.setupDoorTransitions: " + doorId);

        if (doorId.equals("door21") || doorId.equals("21") || doorId.equals("6")) {
            targetMap = "cormap/corid3.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door22") || doorId.equals("22") || doorId.equals("7")) {
            targetMap = "room/k3.tmx";
            targetX = 640;
            targetY = fixedY;
        }
        else if (doorId.equals("door23") || doorId.equals("23") || doorId.equals("8")) {
            targetMap = "room/k4.tmx";
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
        System.out.println("Corridor2Screen.setupExitTransitions: " + exitId);

        if (exitId.equals("exit3") || exitId.equals("3") || exitId.equals("1")) {
            targetMap = "cormap/corid2.tmx";
            targetX = 640;  // door22
            targetY = fixedY;
        }
        else if (exitId.equals("exit4") || exitId.equals("4") || exitId.equals("2")) {
            targetMap = "cormap/corid2.tmx";
            targetX = 1400;  // door23
            targetY = fixedY;
        }
        else {
            targetMap = "cormap/corid2.tmx";
            targetX = 640;
            targetY = fixedY;
        }
    }
}