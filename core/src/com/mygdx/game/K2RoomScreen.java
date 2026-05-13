package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class K2RoomScreen extends BaseGameScreen {

    public K2RoomScreen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public K2RoomScreen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "room/k2.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        // В комнате нет дверей
    }

    @Override
    protected void setupExitTransitions(String exitId) {
        targetMap = "cormap/corid1.tmx";
        targetX = 1400;
        targetY = fixedY;
    }
}