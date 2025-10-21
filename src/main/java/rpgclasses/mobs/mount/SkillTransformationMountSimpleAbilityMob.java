package rpgclasses.mobs.mount;

import necesse.engine.input.Control;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ability.EmptyMobAbility;

abstract public class SkillTransformationMountSimpleAbilityMob extends SkillTransformationMountMob {
    protected EmptyMobAbility ability;
    public long abilityCooldown;

    public SkillTransformationMountSimpleAbilityMob() {
        super();

        this.registerAbility(this.ability = new EmptyMobAbility() {
            protected void run() {
                Mob rider = SkillTransformationMountSimpleAbilityMob.this.getRider();
                if (SkillTransformationMountSimpleAbilityMob.this.isMounted() && rider instanceof PlayerMob && SkillTransformationMountSimpleAbilityMob.this.getTime() >= SkillTransformationMountSimpleAbilityMob.this.abilityCooldown) {
                    SkillTransformationMountSimpleAbilityMob.this.abilityCooldown = SkillTransformationMountSimpleAbilityMob.this.getTime() + mountAbilityCooldown();
                    runMountAbility((PlayerMob) rider);
                }
            }
        });
    }

    abstract public void runMountAbility(PlayerMob player);

    abstract public int mountAbilityCooldown();

    @Override
    public boolean tickActiveMountAbility(PlayerMob playerMob, boolean isRunningClient) {
        if (this.getTime() >= this.abilityCooldown) {
            this.ability.executePacket(null);
        }
        return !isRunningClient || Control.TRINKET_ABILITY.isDown();
    }
}
