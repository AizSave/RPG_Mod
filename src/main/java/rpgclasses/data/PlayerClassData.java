package rpgclasses.data;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.content.player.SkillsLogic.SkillsList;

import java.util.Arrays;

public class PlayerClassData {
    public final String prefixDataName;
    public final String passivesDataName;
    public final String activeSkillsDataName;

    public final PlayerClass playerClass;
    public final String playerName;

    private int[] passiveLevels;
    private int[] activeSkillLevels;

    public PlayerClassData(int classID, String playerName) {
        this.playerClass = PlayerClass.classesList.get(classID);
        this.playerName = playerName;

        this.passiveLevels = new int[playerClass.passivesList.size()];
        this.activeSkillLevels = new int[playerClass.activeSkillsList.size()];

        prefixDataName = "rpgmod_" + playerClass.stringID + "_";
        passivesDataName = prefixDataName + "passive" + "-";
        activeSkillsDataName = prefixDataName + "active" + "-";
    }

    public PlayerData getPlayerData(boolean isServer) {
        return PlayerDataList.getPlayerData(playerName, isServer);
    }

    public int getLevel(boolean isServer) {
        return playerClass.getLevel(getPlayerData(isServer));
    }

    public int totalPassivePoints(boolean isServer) {
        return playerClass.totalPassivePoints(getLevel(isServer));
    }

    public int totalActiveSkillPoints(boolean isServer) {
        return playerClass.totalActiveSkillPoints(getLevel(isServer));
    }

    public int usedPassivePoints() {
        return Arrays.stream(passiveLevels).sum();
    }

    public int usedActiveSkillPoints() {
        return Arrays.stream(activeSkillLevels).sum();
    }

    public void loadData(LoadData loadData) {
        int[] oldSavePassives = loadData.getIntArray(prefixDataName + "passives", null, false);
        int[] oldSaveActiveSkills = loadData.getIntArray(prefixDataName + "actives", null, false);
        if (oldSavePassives != null) {
            this.passiveLevels = oldSavePassives.length != playerClass.passivesList.size() ? new int[playerClass.passivesList.size()] : oldSavePassives;
        } else {
            this.passiveLevels = new int[playerClass.passivesList.size()];
            for (int i = 0; i < playerClass.passivesList.getList().size(); i++) {
                passiveLevels[i] = loadData.getInt(passivesDataName + playerClass.passivesList.get(i).stringID, 0, false);
            }
        }
        if (oldSaveActiveSkills != null) {
            this.activeSkillLevels = oldSaveActiveSkills.length != playerClass.activeSkillsList.size() ? new int[playerClass.activeSkillsList.size()] : oldSaveActiveSkills;
        } else {
            this.activeSkillLevels = new int[playerClass.activeSkillsList.size()];
            for (int i = 0; i < playerClass.activeSkillsList.getList().size(); i++) {
                activeSkillLevels[i] = loadData.getInt(activeSkillsDataName + playerClass.activeSkillsList.get(i).stringID, 0, false);
            }
        }
    }

    public int[] getPassiveLevels() {
        return passiveLevels;
    }

    public void setPassiveLevels(int[] passiveLevels) {
        this.passiveLevels = passiveLevels;
    }

    public void setPassiveLevel(int skillID, int level) {
        passiveLevels[skillID] = level;
    }

    public int[] getActiveSkillLevels() {
        return activeSkillLevels;
    }

    public void setActiveSkillLevels(int[] activeSkillLevels) {
        this.activeSkillLevels = activeSkillLevels;
    }

    public void setActiveSkillLevel(int skillID, int level) {
        activeSkillLevels[skillID] = level;
    }

    public void saveData(SaveData saveData) {
        for (int i = 0; i < playerClass.passivesList.getList().size(); i++) {
            if (passiveLevels[i] > 0)
                saveData.addInt(passivesDataName + playerClass.passivesList.get(i).stringID, passiveLevels[i]);
        }

        for (int i = 0; i < playerClass.activeSkillsList.getList().size(); i++) {
            if (activeSkillLevels[i] > 0)
                saveData.addInt(activeSkillsDataName + playerClass.activeSkillsList.get(i).stringID, activeSkillLevels[i]);
        }
    }

    public static PlayerClassData applyPacket(PacketReader reader) {
        int id = reader.getNextInt();
        String playerName = reader.getNextString();

        PlayerClassData classData = new PlayerClassData(id, playerName);

        classData.passiveLevels = reader.getNextInts(classData.playerClass.passivesList.size());
        classData.activeSkillLevels = reader.getNextInts(classData.playerClass.activeSkillsList.size());

        return classData;
    }

    public void setupPacket(PacketWriter writer) {
        writer.putNextInt(playerClass.id);
        writer.putNextString(playerName);

        writer.putNextInts(passiveLevels);
        writer.putNextInts(activeSkillLevels);
    }

    public int getEffectiveSkillMaxLevel(Skill skill, int classLevel, int[] skillLevels) {
        boolean isPassive = skill instanceof Passive;
        SkillsList<? extends Skill> skillsList = isPassive ? playerClass.passivesList : playerClass.activeSkillsList;

        int availablePoints = getAvailableSkillPointsAtLevel(skill.requiredClassLevel, classLevel, isPassive, skillLevels, skillsList, skill.id);

        return Math.min(skill.levelMax, availablePoints);
    }


    public static int getMaxSkillPointsAtLevel(int requiredLevel, int classLevel, boolean isPassive) {
        int maxPoints = classLevel - requiredLevel + 1;
        if (maxPoints <= 0) return 0;

        return isPassive ? maxPoints * 2 : maxPoints;
    }

    public static int getAvailableSkillPointsAtLevel(int requiredLevel, int classLevel, boolean isPassive, int[] skillLevels, SkillsList<? extends Skill> skillsList, int skillIdToExclude) {
        int maxPoints = getMaxSkillPointsAtLevel(requiredLevel, classLevel, isPassive);

        if (maxPoints == 0) return 0;

        int usedPoints = getUsedSkillPointsAtLevel(requiredLevel, skillLevels, skillsList, skillIdToExclude);

        int available = maxPoints - usedPoints;
        return Math.max(0, available);
    }

    public static int getUsedSkillPointsAtLevel(int requiredLevel, int[] skillLevels, SkillsList<? extends Skill> skillsList, int skillIdToExclude) {
        int usedPoints = 0;
        for (int i = 0; i < skillsList.size(); i++) {
            Skill skill = skillsList.get(i);
            if (skill.id != skillIdToExclude) {
                int skillLevel = skillLevels[i];
                if (skillLevel > 0) {
                    if (skill.requiredClassLevel >= requiredLevel) {
                        usedPoints += skillLevels[i];
                    }
                }
            }
        }
        return usedPoints;
    }

}
