package com.mygdx.game.game;

import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

public class QuestManager {
    public enum QuestType {
        TALK_TO_NPC,
        COLLECT_ITEMS,
        MINIGAME,
        GO_TO_EXIT
    }

    public static class Quest {
        public String id;
        public String title;
        public String description;
        public QuestType type;
        public String targetId;
        public int requiredAmount;
        public int currentAmount;
        public boolean completed;
        public boolean active;
        public String nextQuestId;

        public Quest(String id, String title, String description, QuestType type,
                     String targetId, int requiredAmount, String nextQuestId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.targetId = targetId;
            this.requiredAmount = requiredAmount;
            this.currentAmount = 0;
            this.completed = false;
            this.active = false;
            this.nextQuestId = nextQuestId;
        }
    }

    private Map<String, Quest> quests;
    private String activeQuestId;
    private QuestCompletedListener listener;

    public interface QuestCompletedListener {
        void onQuestCompleted(String questId);
    }

    public QuestManager() {
        quests = new HashMap<>();
    }

    public void addQuest(Quest quest) {
        quests.put(quest.id, quest);
    }

    public void startQuest(String questId) {
        Quest quest = quests.get(questId);
        if (quest != null) {
            quest.active = true;
            activeQuestId = questId;
        }
    }

    public void updateProgress(String targetId, int amount) {
        if (activeQuestId == null) return;
        Quest quest = quests.get(activeQuestId);
        if (quest != null && !quest.completed && quest.targetId.equals(targetId)) {
            quest.currentAmount += amount;
            if (quest.currentAmount >= quest.requiredAmount) {
                completeQuest();
            }
        }
    }

    public void completeQuest() {
        if (activeQuestId == null) return;
        Quest quest = quests.get(activeQuestId);
        if (quest != null) {
            quest.completed = true;
            quest.active = false;
            if (listener != null) {
                listener.onQuestCompleted(activeQuestId);
            }
            if (quest.nextQuestId != null) {
                startQuest(quest.nextQuestId);
            } else {
                activeQuestId = null;
            }
        }
    }
    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public void setListener(QuestCompletedListener listener) {
        this.listener = listener;
    }

    public Quest getActiveQuest() {
        return activeQuestId != null ? quests.get(activeQuestId) : null;
    }

    public boolean isQuestActive(String questId) {
        Quest quest = quests.get(questId);
        return quest != null && quest.active;
    }

    public boolean isQuestCompleted(String questId) {
        Quest quest = quests.get(questId);
        return quest != null && quest.completed;
    }
}