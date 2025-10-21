package rpgclasses.mobs.mount;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Skill;

abstract public class SkillTransformationMountMob extends TransformationMountMob {
    public PlayerClass playerClass;
    public Skill skill;

    public SkillTransformationMountMob() {
        super();
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt("classID", this.playerClass.id);
        save.addBoolean("isPassive", skill instanceof Passive);
        save.addInt("masteryID", this.skill.id);
    }

    public void applyData(PlayerClass playerClass, Skill skill) {
        this.playerClass = playerClass;
        this.skill = skill;
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.playerClass = PlayerClass.classesList.get(save.getInt("classID"));
        this.skill = (save.getBoolean("isPassive") ? playerClass.passivesList : playerClass.activeSkillsList).get(save.getInt("masteryID"));
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(playerClass.id);
        writer.putNextBoolean(skill instanceof Passive);
        writer.putNextInt(skill.id);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        this.playerClass = PlayerClass.classesList.get(reader.getNextInt());
        this.skill = (reader.getNextBoolean() ? playerClass.passivesList : playerClass.activeSkillsList).get(reader.getNextInt());
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!removed() && getActualSkillLevel() == 0) this.remove();
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (!removed() && getActualSkillLevel() == 0) this.remove();
    }

    public int getActualSkillLevel() {
        Mob rider = this.getRider();
        if (rider == null || !rider.isPlayer) return 0;

        return skill.getLevel((PlayerMob) rider);
    }
}
