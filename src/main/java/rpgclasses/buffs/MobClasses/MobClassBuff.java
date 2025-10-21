package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import rpgclasses.data.MobData;

public class MobClassBuff extends Buff {
    public MobClassBuff() {
        this.canCancel = false;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
        int level = getMobLevel(activeBuff);
        activeBuff.addModifier(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, healthRegenBoost() * activeBuff.owner.getMaxHealth());

        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, damageBoost() + MobData.levelScaling(level) * (1 + damageBoost()) * 0.02F);
        activeBuff.setModifier(BuffModifiers.SPEED, speedBoost() + MobData.levelScaling(level) * (1 + speedBoost()) * 0.002F);
    }

    public float healthBoost() {
        return 0;
    }

    public float healthRegenBoost() {
        return 0.01F;
    }

    public float damageBoost() {
        return 0;
    }

    public float speedBoost() {
        return 0;
    }

    public static void setMobLevel(ActiveBuff activeBuff, int level) {
        activeBuff.getGndData().setInt("moblevel", level);
    }

    public static int getMobLevel(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("moblevel", 0);
    }

    @Override
    public void onRemoved(ActiveBuff activeBuff) {
        activeBuff.owner.buffManager.addBuff(new ActiveBuff(this.getStringID(), activeBuff.owner, 3600F, null), false);
    }
}
