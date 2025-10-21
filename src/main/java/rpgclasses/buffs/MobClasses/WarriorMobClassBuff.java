package rpgclasses.buffs.MobClasses;

public class WarriorMobClassBuff extends MobClassBuff {
    @Override
    public float healthBoost() {
        return 0.1F;
    }

    @Override
    public float damageBoost() {
        return 0.2F;
    }

    @Override
    public float speedBoost() {
        return 0.1F;
    }
}
