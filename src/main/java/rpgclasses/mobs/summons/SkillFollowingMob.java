package rpgclasses.mobs.summons;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.summon.summonFollowingMob.attackingFollowingMob.AttackingFollowingMob;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class SkillFollowingMob extends AttackingFollowingMob {
    public static String prefixDataName = "rpgmod_summon_";
    public static String classDataName = prefixDataName + "class";
    public static String isPassiveDataName = prefixDataName + "isPassive";
    public static String skillDataName = prefixDataName + "skill";

    public PlayerClass playerClass;
    boolean isPassive;
    public Skill skill;

    public SkillFollowingMob(int health) {
        super(health);
    }

    public void setSkill(Skill skill) {
        this.playerClass = skill.playerClass;
        this.isPassive = skill instanceof Passive;
        this.skill = skill;
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        playerClass = PlayerClass.classesList.get(load.getInt(classDataName));
        isPassive = load.getBoolean(classDataName);
        skill = getSkillList(playerClass).get(load.getInt(skillDataName));
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(classDataName, playerClass.id);
        save.addBoolean(isPassiveDataName, isPassive);
        save.addInt(skillDataName, skill.id);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        playerClass = PlayerClass.classesList.get(reader.getNextInt());
        isPassive = reader.getNextBoolean();
        skill = playerClass.passivesList.get(reader.getNextInt());
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(playerClass.id);
        writer.putNextBoolean(isPassive);
        writer.putNextInt(skill.id);
    }

    public SkillsList<?> getSkillList(PlayerClass playerClass) {
        return isPassive ? playerClass.passivesList : playerClass.activeSkillsList;
    }

    public PlayerData getPlayerData() {
        NetworkClient client = this.getFollowingClient();
        if (client == null) return null;
        return PlayerDataList.getPlayerData(client.playerMob);
    }

    public PlayerClassData getClassData(PlayerData playerData) {
        if (playerData == null) return null;
        return playerData.getClassesData()[playerClass.id];
    }
}
