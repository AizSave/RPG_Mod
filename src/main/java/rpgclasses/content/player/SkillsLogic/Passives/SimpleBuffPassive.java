package rpgclasses.content.player.SkillsLogic.Passives;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.data.PlayerData;

abstract public class SimpleBuffPassive extends Passive {
    public boolean containsComplexTooltips;

    public SimpleBuffPassive(String stringID, String color, int levelMax, int requiredClassLevel, boolean containsComplexTooltips) {
        super(stringID, color, levelMax, requiredClassLevel);
        this.containsComplexTooltips = containsComplexTooltips;
    }

    public SimpleBuffPassive(String stringID, String color, int levelMax, int requiredClassLevel) {
        this(stringID, color, levelMax, requiredClassLevel, true);
    }

    @Override
    public void givePassiveBuff(PlayerMob player, PlayerData playerData, int passiveLevel) {
        ActiveBuff oldAb = player.buffManager.getBuff(getBuffStringID());
        int oldLevel = oldAb == null ? 0 : oldAb.getGndData().getInt("skillLevel");
        boolean differentOldLevel = oldLevel != passiveLevel;

        if (differentOldLevel) {
            ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getBuffStringID()), player, 1000, null);
            ab.getGndData().setInt("skillLevel", passiveLevel);

            player.buffManager.addBuff(ab, player.isServer(), oldAb != null);
        }
    }

    public void giveDatalessSecondaryPassiveBuff(Mob target, int duration) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getSecondaryBuffStringID()), target, duration, null);
        target.buffManager.addBuff(ab, target.isServer());
    }

    public void giveSecondaryPassiveBuff(PlayerMob player, Mob target, PlayerData playerData, int passiveLevel, int duration) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getSecondaryBuffStringID()), target, duration, null);
        ab.getGndData().setInt("skillLevel", passiveLevel);
        ab.getGndData().setInt("playerLevel", passiveLevel);
        ab.getGndData().setFloat("endurance", playerData.getEndurance(player));
        ab.getGndData().setFloat("speed", playerData.getSpeed(player));
        ab.getGndData().setFloat("strength", playerData.getStrength(player));
        ab.getGndData().setFloat("intelligence", playerData.getIntelligence(player));
        ab.getGndData().setFloat("grace", playerData.getGrace(player));
        target.buffManager.addBuff(ab, player.isServer());
    }

    @Override
    public void removePassiveBuffs(PlayerMob player) {
        player.buffManager.removeBuff(getBuffStringID(), true);
    }

    @Override
    public void registry() {
        super.registry();
        PrincipalPassiveBuff principalPassiveBuff = getBuff();
        if (principalPassiveBuff != null) BuffRegistry.registerBuff(getBuffStringID(), principalPassiveBuff);
        SecondaryPassiveBuff secondaryPassiveBuff = getSecondaryBuff();
        if (secondaryPassiveBuff != null) BuffRegistry.registerBuff(getSecondaryBuffStringID(), secondaryPassiveBuff);
    }

    abstract public PrincipalPassiveBuff getBuff();

    public SecondaryPassiveBuff getSecondaryBuff() {
        return null;
    }

    public String getBuffStringID() {
        return stringID + buffsStringID();
    }

    public String getSecondaryBuffStringID() {
        return stringID + "2" + buffsStringID();
    }

    @Override
    public boolean containsComplexTooltips() {
        return containsComplexTooltips;
    }

    public String buffsStringID() {
        return "passivebuff";
    }
}
