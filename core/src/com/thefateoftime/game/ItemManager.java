package com.thefateoftime.game;

public class ItemManager {
    private final int collectedItems;
    private final int totalItems;

    public ItemManager() {
        this.collectedItems = 0;
        this.totalItems = 0;
    }
    public boolean isAllCollected() {
        return collectedItems >= totalItems && totalItems > 0;
    }
}