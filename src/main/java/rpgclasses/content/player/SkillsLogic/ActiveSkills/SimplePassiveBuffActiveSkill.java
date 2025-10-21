package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PassiveActiveSkillBuff;
import rpgclasses.data.PlayerData;

abstract public class SimplePassiveBuffActiveSkill extends ActiveSkill {
    public SimplePassiveBuffActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        if (isInUse && player.buffManager.hasBuff(getBuffStringID())) {
            removeBuffOnRun(player);
        } else {
            giveBuffOnRun(player, playerData, activeSkillLevel);
        }
    }

    @Override
    public boolean isInUseSkill() {
        return true;
    }

    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        giveBuff(player, player, playerData, activeSkillLevel);
    }

    public void giveBuff(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = getActiveBuff(buffOwner, target, playerData, activeSkillLevel);
        target.buffManager.addBuff(ab, buffOwner.isServer());
    }

    public void removeBuffOnRun(PlayerMob player) {
        removeBuff(player);
    }

    public void removeBuff(Mob target) {
        target.buffManager.removeBuff(getBuffStringID(), true);
    }

    public ActiveBuff getActiveBuff(String buffID, PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(buffID), target, 1000, null);
        ab.getGndData().setInt("skillLevel", activeSkillLevel);
        ab.getGndData().setFloat("endurance", playerData.getEndurance(buffOwner));
        ab.getGndData().setFloat("speed", playerData.getSpeed(buffOwner));
        ab.getGndData().setFloat("strength", playerData.getStrength(buffOwner));
        ab.getGndData().setFloat("intelligence", playerData.getIntelligence(buffOwner));
        ab.getGndData().setFloat("grace", playerData.getGrace(buffOwner));
        return ab;
    }

    public ActiveBuff getActiveBuff(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        return this.getActiveBuff(getBuffStringID(), buffOwner, target, playerData, activeSkillLevel);
    }

    @Override
    public void registry() {
        super.registry();
        BuffRegistry.registerBuff(getBuffStringID(), getBuff());
    }

    abstract public PassiveActiveSkillBuff getBuff();

    public String getBuffStringID() {
        return stringID + "passiveactiveskillbuff";
    }
}
