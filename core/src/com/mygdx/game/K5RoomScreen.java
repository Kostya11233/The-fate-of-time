package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class K5RoomScreen extends BaseGameScreen {

    public K5RoomScreen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public K5RoomScreen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "room/k5.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {}

    @Override
    protected void setupExitTransitions(String exitId) {
        System.out.println("K5RoomScreen: выход из комнаты " + exitId);
        targetMap = "cormap/corid3.tmx";
        targetX = 640;  // door31
        targetY = fixedY;
    }
}