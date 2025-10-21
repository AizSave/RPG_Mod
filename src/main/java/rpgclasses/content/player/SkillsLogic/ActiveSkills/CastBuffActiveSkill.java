package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.data.PlayerData;

abstract public class CastBuffActiveSkill extends CastActiveSkill {

    public CastBuffActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }


    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        giveBuff(player, player, playerData, activeSkillLevel);
    }

    public void giveBuff(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = getActiveBuff(buffOwner, target, playerData, activeSkillLevel);
        target.buffManager.addBuff(ab, buffOwner.isServer());
    }

    public void giveBuff2(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = getActiveBuff2(buffOwner, target, playerData, activeSkillLevel);
        target.buffManager.addBuff(ab, buffOwner.isServer());
    }

    public ActiveBuff getActiveBuff(String buffID, int duration, PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(buffID), target, duration, null);
        ab.getGndData().setInt("skillLevel", activeSkillLevel);
        ab.getGndData().setInt("playerLevel", activeSkillLevel);
        ab.getGndData().setFloat("endurance", playerData.getEndurance(buffOwner));
        ab.getGndData().setFloat("speed", playerData.getSpeed(buffOwner));
        ab.getGndData().setFloat("strength", playerData.getStrength(buffOwner));
        ab.getGndData().setFloat("intelligence", playerData.getIntelligence(buffOwner));
        ab.getGndData().setFloat("grace", playerData.getGrace(buffOwner));
        return ab;
    }

    public ActiveBuff getActiveBuff(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        return this.getActiveBuff(getBuffStringID(), getDuration(activeSkillLevel), buffOwner, target, playerData, activeSkillLevel);
    }

    public ActiveBuff getActiveBuff2(PlayerMob buffOwner, Mob target, PlayerData playerData, int activeSkillLevel) {
        return this.getActiveBuff(getBuff2StringID(), getDuration2(activeSkillLevel), buffOwner, target, playerData, activeSkillLevel);
    }

    @Override
    public void registry() {
        super.registry();
        BuffRegistry.registerBuff(getBuffStringID(), getBuff());
        ActiveSkillBuff buff2 = getBuff2();
        if (buff2 != null) BuffRegistry.registerBuff(getBuff2StringID(), buff2);
    }

    abstract public ActiveSkillBuff getBuff();

    public ActiveSkillBuff getBuff2() {
        return null;
    }

    abstract public int getDuration(int activeSkillLevel);

    public int getDuration2(int activeSkillLevel) {
        return 0;
    }

    public String getBuffStringID() {
        return stringID + "activeskillbuff";
    }

    public String getBuff2StringID() {
        return stringID + "2activeskillbuff";
    }


}
