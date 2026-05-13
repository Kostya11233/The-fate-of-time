package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class K1RoomScreen extends BaseGameScreen {

    public K1RoomScreen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public K1RoomScreen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "room/k1.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        // В комнате нет дверей
    }

    @Override
    protected void setupExitTransitions(String exitId) {
        targetMap = "cormap/corid1.tmx";
        targetX = 640;
        targetY = fixedY;
    }
}