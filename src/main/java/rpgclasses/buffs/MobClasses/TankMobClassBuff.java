package rpgclasses.buffs.MobClasses;

public class TankMobClassBuff extends MobClassBuff {
    @Override
    public float healthBoost() {
        return 0.3F;
    }

    @Override
    public float healthRegenBoost() {
        return 0.05F;
    }
}
