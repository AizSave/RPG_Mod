package rpgclasses.data;

import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.objectEntity.interfaces.OEInventory;
import necesse.inventory.item.toolItem.ToolType;
import necesse.level.gameObject.*;
import necesse.level.gameObject.container.CraftingStationObject;
import necesse.level.gameObject.furniture.RoomFurniture;
import necesse.level.maps.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rpgclasses.content.player.Attribute;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.packets.ShowModExpPacket;
import rpgclasses.packets.UpdateClientExpPacket;
import rpgclasses.packets.UpdateClientResetsPacket;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.settings.RPGSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerData {
    public static int EQUIPPED_SKILLS_MAX = 12;
    public static int MAX_EXP = 2000000000;

    public static String prefixDataName = "rpgmod_";
    public static String expDataName = prefixDataName + "exp";
    public static String resetsDataName = prefixDataName + "resets";
    public static String attributesDataName = prefixDataName + "attributes";
    public static String masteryDataName = prefixDataName + "mastery";
    public static String classesDataName = prefixDataName + "classes";
    public static String equippedActiveSkillsDataName = prefixDataName + "equippedactives";

    public static String lastDeathDataName = prefixDataName + "lastdeath";
    public static String grabbedObjectDataName = prefixDataName + "grabbedobject";

    public final String playerName;
    public final long worldUniqueID;
    private int exp = 0;
    private int resets = 0;
    private int[] attributePointsUsed = new int[Attribute.attributesList.size()];
    public List<Integer> masterySkills = new ArrayList<>();
    private int[] classLevels = new int[PlayerClass.classesList.size()];
    private PlayerClassData[] classesData = new PlayerClassData[PlayerClass.classesList.size()];
    public EquippedActiveSkill[] equippedActiveSkills = new EquippedActiveSkill[EQUIPPED_SKILLS_MAX];

    public long lastDeath;
    public GameObject grabbedObject;

    public PlayerData(String playerName, long worldUniqueID) {
        this.playerName = playerName;
        this.worldUniqueID = worldUniqueID;

        for (int i = 0; i < classesData.length; i++) {
            classesData[i] = new PlayerClassData(i, playerName);
        }
        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = new EquippedActiveSkill();
        }
    }

    public void loadData(PlayerMob player, @NotNull LoadData loadData) {
        loadData(
                loadData.getInt(expDataName, 0, false),
                loadData.getInt(resetsDataName, 0, false)
        );
        loadDataAttributes(loadData);
        loadDataMastery(loadData);
        loadDataClasses(loadData);
        loadDataClassesData(classLevels, loadData);
        loadDataEquippedActiveSkills(player, loadData);
        loadDataMisc(loadData);
    }

    public void loadData(int exp, int resets) {
        loadDataExp(exp);
        loadDataResets(resets);
    }

    public void loadDataExp(int exp) {
        this.exp = exp;
    }

    public void loadDataResets(int resets) {
        this.resets = resets;
    }

    public void loadDataAttributes(LoadData loadData) {
        for (Attribute attribute : Attribute.attributesList) {
            this.attributePointsUsed[attribute.id] = loadData.getInt(attributesDataName + "_" + attribute.stringID, 0, false);
        }
    }

    public void loadDataMastery(LoadData loadData) {
        masterySkills.clear();
        String[] strings = loadData.getStringArray(masteryDataName, new String[0], false);
        for (String string : strings) {
            Mastery mastery = Mastery.masterySkills.get(string);
            if (mastery != null) {
                masterySkills.add(mastery.id);
            }
        }
    }

    public void loadDataClasses(LoadData loadData) {
        for (PlayerClass playerClass : PlayerClass.classesList) {
            classLevels[playerClass.id] = loadData.getInt(classesDataName + "_" + playerClass.stringID, 0, false);
        }
    }

    public void loadDataClassesData(@NotNull int[] classLevels, LoadData loadData) {
        boolean update = classLevels.length == PlayerClass.classesList.size();
        this.classesData = new PlayerClassData[classLevels.length];
        for (int i = 0; i < this.classesData.length; i++) {
            PlayerClassData classData = new PlayerClassData(i, playerName);
            if (update) classData.loadData(loadData);
            classesData[i] = classData;
        }
    }

    public void loadDataEquippedActiveSkills(PlayerMob player, LoadData loadData) {
        equippedActiveSkills = new EquippedActiveSkill[EQUIPPED_SKILLS_MAX];
        for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.loadData(player, loadData, i);
        }
    }

    public void loadDataMisc(LoadData loadData) {
        lastDeath = loadData.getLong(lastDeathDataName, 0, false);

        int grabbedObjectID = loadData.getInt(grabbedObjectDataName, -1, false);
        if (grabbedObjectID == -1) {
            grabbedObject = null;
        } else {
            grabbedObject = ObjectRegistry.getObject(grabbedObjectID);
        }
    }

    public void saveData(@NotNull SaveData saveData) {
        saveData.addInt(expDataName, exp);
        saveData.addInt(resetsDataName, resets);

        for (Attribute attribute : Attribute.attributesList) {
            saveData.addInt(attributesDataName + "_" + attribute.stringID, attributePointsUsed[attribute.id]);
        }

        saveData.addStringArray(masteryDataName, masterySkills.stream()
                .map(index -> Mastery.masterySkillsList.get(index).stringID)
                .toArray(String[]::new));

        for (PlayerClass playerClass : PlayerClass.classesList) {
            saveData.addInt(classesDataName + "_" + playerClass.stringID, classLevels[playerClass.id]);
        }

        for (PlayerClassData classesDatum : this.classesData) {
            classesDatum.saveData(saveData);
        }

        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i].saveData(saveData, i);
        }

        saveData.addLong(lastDeathDataName, lastDeath);

        saveData.addInt(grabbedObjectDataName, grabbedObject == null ? -1 : grabbedObject.getID());
    }

    public void setAttributes(@NotNull int[] attributePoints) {
        this.attributePointsUsed = attributePoints;
    }

    public void setClassLevels(@NotNull int[] classLevels) {
        this.classLevels = classLevels.length != PlayerClass.classesList.size() ? new int[PlayerClass.classesList.size()] : classLevels;
    }

    public void setMasterySkills(List<Integer> masterySkills) {
        this.masterySkills.clear();
        this.masterySkills.addAll(masterySkills);
    }

    public int getBaseExp() {
        return this.exp;
    }

    public int getExp() {
        int maxExp = MAX_EXP - RPGSettings.startingExperience();
        if (this.exp > maxExp) {
            return MAX_EXP;
        } else {
            return this.exp + RPGSettings.startingExperience();
        }
    }

    public int[] getAttributePointsUsed() {
        return attributePointsUsed;
    }

    public float getAttribute(int id, @NotNull PlayerMob player) {
        return getAttribute(Attribute.attributesList.get(id), player);
    }

    public float getAttribute(Attribute attribute, @NotNull PlayerMob player) {
        return pointsConversion(getAttributePoints(attribute.id)) + player.buffManager.getModifier(attribute.ownModifier);
    }

    public float getEndurance(@NotNull PlayerMob player) {
        return getAttribute(0, player);
    }

    public float getSpeed(@NotNull PlayerMob player) {
        return getAttribute(1, player);
    }

    public float getStrength(@NotNull PlayerMob player) {
        return getAttribute(2, player);
    }

    public float getIntelligence(@NotNull PlayerMob player) {
        return getAttribute(3, player);
    }

    public float getGrace(@NotNull PlayerMob player) {
        return getAttribute(4, player);
    }

    public static float pointsConversion(int amount) {
        int level = amount + 5;
        if (level <= 10) return level;

        float total = 0;
        float valor = 1;

        for (int i = 1; i <= level; i++) {
            if (i > 10) {
                valor *= 0.99f;
            }
            total += valor;
        }

        return total;
    }


    public void setAttributePointsUsed(int[] attributePointsUsed) {
        this.attributePointsUsed = attributePointsUsed;
    }

    public int getAttributePoints(int id) {
        return (id < 0 || id >= attributePointsUsed.length) ? 0 : attributePointsUsed[id];
    }

    public boolean hasMasterySkill(Mastery mastery) {
        return masterySkills.contains(mastery.id);
    }

    public boolean hasMasterySkill(int id) {
        return masterySkills.contains(id);
    }

    public int[] getClassLevels() {
        int[] trueClassLevels = new int[classLevels.length];
        for (int i = 0; i < classLevels.length; i++) {
            trueClassLevels[i] = getClassLevel(i);
        }
        return trueClassLevels;
    }

    public int getClassLevel(int id) {
        return (id < 0 || id >= classLevels.length) ? 0 : (PlayerClass.classesList.get(id).isEnabled() ? classLevels[id] : 0);
    }

    public PlayerClassData[] getClassesData() {
        return classesData;
    }

    public int getResets() {
        return resets;
    }

    public void modResetsSendPacket(@NotNull ServerClient serverClient, int resets) {
        this.resets += resets;
        serverClient.getServer().network.sendToAllClients(new UpdateClientResetsPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));
    }

    public int getExpActual() {
        return this.getExp() - getExpRequiredForLevel(this.getLevel());
    }

    public int getExpNext() {
        return getExpRequiredForLevel(this.getLevel() + 1) - getExpRequiredForLevel(getLevel());
    }

    public int getExpRequiredForLevel(int level) {
        return level * RPGSettings.firstExperienceReq() + RPGSettings.experienceReqInc() * (level * (level - 1)) / 2 + RPGSettings.squareExperienceReqInc() * (level * (level - 1) * (2 * level - 1)) / 6 + RPGSettings.cubeExperienceReqInc() * (int) Math.pow((double) (level * (level - 1)) / 2, 2);
    }

    public int getLevel() {
        int level = 0;
        while (getExpRequiredForLevel(level + 1) <= this.getExp()) {
            level++;
        }
        return level;
    }

    public void updateAllBuffs(PlayerMob player) {
        updateModifiersBuff(player);

        boolean isServer = player.isServer();

        if (!player.buffManager.hasBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE))
            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE, player, 1000, null), isServer);

        for (int i = 0; i < Mastery.masterySkillsList.size(); i++) {
            Mastery mastery = Mastery.masterySkillsList.get(i);
            if (hasMasterySkill(i)) {
                mastery.giveMasteryBuff(player);
            } else {
                mastery.removePassiveBuffs(player);
            }
        }

        boolean someOverlevel = Arrays.stream(getClassLevels()).sum() > totalClassPoints() ||
                Arrays.stream(getAttributePointsUsed()).sum() > totalAttributePoints() ||
                masterySkills.size() > totalMasteryPoints();

        for (PlayerClassData classesDatum : classesData) {
            PlayerClass playerClass = classesDatum.playerClass;
            int classLevel = getClassLevel(playerClass.id);

            boolean validClass = classLevel > 0;
            if (validClass && !someOverlevel) {
                if (classesDatum.usedPassivePoints() > playerClass.totalPassivePoints(classLevel) || classesDatum.usedActiveSkillPoints() > playerClass.totalActiveSkillPoints(classLevel)) {
                    someOverlevel = true;
                }
            }

            for (int i = 0; i < classesDatum.getPassiveLevels().length; i++) {
                Passive passive = playerClass.passivesList.get(i);
                if (!(passive instanceof BasicPassive)) {
                    if (validClass) {
                        int level = classesDatum.getPassiveLevels()[i];
                        if (level > 0) {
                            passive.givePassiveBuff(player, this, level);
                        } else {
                            passive.removePassiveBuffs(player);
                        }
                    } else {
                        passive.removePassiveBuffs(player);
                    }
                }
            }
        }

        if(isServer) {
            boolean hasOverlevelBuff = player.buffManager.hasBuff(RPGBuffs.PASSIVES.OVER_LEVEL);
            if (someOverlevel && !hasOverlevelBuff) {
                player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.OVER_LEVEL, player, 1000, null), true);
            } else if (!someOverlevel && hasOverlevelBuff) {
                player.buffManager.removeBuff(RPGBuffs.PASSIVES.OVER_LEVEL, true);
            }
        }
    }

    public void updateModifiersBuff(@NotNull PlayerMob player) {
        player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.MODIFIERS, player, 1000, null), player.isServer(), true);
    }

    public void modExpSendPacket(ServerClient serverClient, int amount) {
        if (amount == 0) return;

        int oldLevel = this.getLevel();

        if (amount > 0) {
            if (this.exp > MAX_EXP - amount) {
                amount = MAX_EXP - this.exp;
                if (amount <= 0) return;
                this.exp = MAX_EXP;
            } else {
                this.exp += amount;
            }
        } else {
            if (this.exp + amount < 0) {
                amount = -this.exp;
                if (amount == 0) return;
                this.exp = 0;
            } else {
                this.exp += amount;
            }
        }

        int newLevel = this.getLevel();
        boolean levelUp = newLevel > oldLevel;

        if (levelUp) {
            int levelsGained = newLevel - oldLevel;
            this.modResetsSendPacket(serverClient, levelsGained * 5);

            for (int level = oldLevel + 1; level <= newLevel; level++) {
                serverClient.sendChatMessage(new LocalMessage("message", "newlevel", "level", level));
            }
        }

        serverClient.getServer().network.sendToClientsAtEntireLevel(new ShowModExpPacket(serverClient.playerMob.getX(), serverClient.playerMob.getY(), amount, levelUp), serverClient.getLevel());
        serverClient.getServer().network.sendToAllClients(new UpdateClientExpPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));
    }

    public int totalAttributePoints() {
        return getLevel() * 2;
    }

    public int totalMasteryPoints() {
        return getLevel() / 20;
    }

    public int totalClassPoints() {
        return getLevel();
    }

    public void setupPacket(@NotNull PacketWriter writer) {
        writer.putNextString(playerName);
        writer.putNextLong(worldUniqueID);

        // Exp
        writer.putNextInt(exp);

        // Resets
        writer.putNextInt(resets);

        // Attribute Levels
        writer.putNextInts(attributePointsUsed);

        // Mastery
        writer.putNextInt(masterySkills.size());
        writer.putNextInts(masterySkills.stream().mapToInt(Integer::intValue).toArray());

        // Class Levels
        writer.putNextInts(classLevels);

        // Classes Data
        for (PlayerClassData classesDatum : classesData) {
            classesDatum.setupPacket(writer);
        }

        // Equipped Active Skills
        for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
            equippedActiveSkill.setupPacket(writer);
        }

        writer.putNextLong(lastDeath);

        writer.putNextInt(grabbedObject == null ? -1 : grabbedObject.getID());
    }

    @NotNull
    public static PlayerData applyPacket(@NotNull PacketReader reader) {
        PlayerData playerData = new PlayerData(reader.getNextString(), reader.getNextLong());

        // Exp
        playerData.exp = reader.getNextInt();

        // Resets
        playerData.resets = reader.getNextInt();

        // Attribute Levels
        playerData.attributePointsUsed = reader.getNextInts(Attribute.attributes.size());

        // Mastery
        int masterySize = reader.getNextInt();
        int[] masteryArray = reader.getNextInts(masterySize);
        for (int i : masteryArray) {
            playerData.masterySkills.add(i);
        }

        // Class Levels
        playerData.classLevels = reader.getNextInts(PlayerClass.classesList.size());

        // Classes Data
        for (int i = 0; i < PlayerClass.classesList.size(); i++) {
            playerData.classesData[i] = PlayerClassData.applyPacket(reader);
        }

        // Equipped Active Skills
        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            playerData.equippedActiveSkills[i] = EquippedActiveSkill.applyPacket(reader);
        }

        playerData.lastDeath = reader.getNextLong();

        int grabbedObjectID = reader.getNextInt();
        playerData.grabbedObject = grabbedObjectID == -1 ? null : ObjectRegistry.getObject(grabbedObjectID);

        return playerData;
    }

    @Nullable
    public static GameObject isGrabbableObject(Level level, int tileX, int tileY) {
        GameObject object = level.getObject(tileX, tileY);

        if (object == null) return null;
        if (object.getID() <= 0) return null;
        if (object.toolType == ToolType.UNBREAKABLE) return null;
        if (object.isMultiTile()) return null;
        if (level.entityManager.getObjectEntity(tileX, tileY) instanceof OEInventory) return null;

        return (object instanceof WallObject || object instanceof RockObject || object instanceof ColumnObject || object instanceof RoomFurniture || object instanceof StreetlampObject || object instanceof CandlePedestalObject || object instanceof TrainingDummyObject || object instanceof TreeObject)
                ? object : null;
    }

    public int getInUseActiveSkillSlotIndex() {
        for (int i = 0; i < equippedActiveSkills.length; i++) {
            if (equippedActiveSkills[i].isInUse()) return i;
        }
        return -1;
    }

    public EquippedActiveSkill getInUseActiveSkillSlot() {
        int index = getInUseActiveSkillSlotIndex();
        return index != -1 ? equippedActiveSkills[index] : null;
    }

    public ActiveSkill getInUseActiveSkill() {
        EquippedActiveSkill equippedActiveSkill = getInUseActiveSkillSlot();
        return equippedActiveSkill == null ? null : equippedActiveSkill.getActiveSkill();
    }

    public void removeInUseActiveSkillSlot() {
        EquippedActiveSkill equippedActiveSkill = getInUseActiveSkillSlot();
        if (equippedActiveSkill != null) {
            equippedActiveSkill.restartCooldown();
        }
    }
}
