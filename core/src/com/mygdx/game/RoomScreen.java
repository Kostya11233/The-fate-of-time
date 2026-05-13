package com.mygdx.game;

public class RoomScreen extends BaseGameScreen {
    private String returnMap;
    private float returnX;
    private String returnDoorId;

    public RoomScreen(TheFateGame game, boolean loadSavedGame, String mapPath, String returnMap, float returnX, String returnDoorId) {
        super(game, loadSavedGame);
        this.returnMap = returnMap;
        this.returnX = returnX;
        this.returnDoorId = returnDoorId;
    }

    @Override
    protected String getDefaultMap() {
        return currentMap; // будет установлен при создании
    }

    @Override
    protected void setupDoorTransitions(String doorId) {
        // В комнатах обычно нет дверей, только выход
        System.out.println("Комната: дверь не распознана - " + doorId);
    }

    @Override
    protected void setupExitTransitions(String exitId) {
        // Выход из комнаты - возвращаемся в коридор к нужной двери
        targetMap = returnMap;
        targetX = returnX;
        targetY = fixedY;
        enteredFromDoor = returnDoorId;
        System.out.println("Выход из комнаты в " + returnMap + " на X=" + returnX + " (дверь " + returnDoorId + ")");
    }
}