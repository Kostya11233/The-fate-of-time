package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class K7RoomScreen extends BaseGameScreen {

    public K7RoomScreen(TheFateGame game, boolean loadSavedGame) {
        super(game, loadSavedGame);
    }

    public K7RoomScreen(TheFateGame game, boolean loadSavedGame, float startX) {
        super(game, loadSavedGame, startX);
        this.position = new Vector2(startX, fixedY);
        this.facingRight = true;
        if (camera != null) {
            camera.position.set(position.x, 360, 0);
        }
    }

    @Override
    protected String getDefaultMap() {
        return "room/k7.tmx";
    }

    @Override
    protected void setupDoorTransitions(String doorId) {}

    @Override
    protected void setupExitTransitions(String exitId) {
        System.out.println("K7RoomScreen: выход из комнаты " + exitId);
        targetMap = "cormap/corid4.tmx";
        targetX = 1920;
        targetY = fixedY;
    }

    @Override
    protected void checkInteractions() {

        if (!isTransitioning && !isPaused) {
            showInteractionBtn = true;
            pendingExitId = "exit7";
            if (interactionBtn != null && !interactionBtn.isVisible()) {
                interactionBtn.setVisible(true);
            }
        } else {
            if (interactionBtn != null) interactionBtn.setVisible(false);
        }
    }
}