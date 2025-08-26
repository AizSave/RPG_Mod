package rpgclasses.buffs;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;
import necesse.entity.particle.ParticleOption;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;

public class IgnitedBuff extends Buff {
    public IgnitedBuff() {
        this.shouldSave = false;
        this.isImportant = true;
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        if (activeBuff.owner.isVisible()) {
            Mob owner = activeBuff.owner;
            ParticleOption particle = owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).givesLight(0.0F, 0.5F).height(16.0F);
            if (isPurple(activeBuff)) {
                particle.color(ParticleOption.randomFlameColor(260F));
            } else {
                particle.flameColor();
            }
        }
    }


    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.setModifier(BuffModifiers.FIRE_DAMAGE_FLAT, activeBuff.getGndData().getFloat("igniteDamage"));
    }

    public boolean isPurple(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getBoolean("isPurple");
    }

    public static void apply(Mob attacker, Mob target, float damage, float duration, boolean isPurple) {
        apply(attacker, target, damage, (int) (duration * 1000), isPurple);
    }

    public static void apply(Mob attacker, Mob target, float damage, int duration, boolean isPurple) {
        ActiveBuff ab = new ActiveBuff(RPGBuffs.IGNITED, target, duration, attacker);
        IgnitedBuff.setIgniteDamage(ab, damage);
        if (isPurple) ab.getGndData().setBoolean("isPurple", true);

        if (shouldApply(target, damage, duration)) target.buffManager.addBuff(ab, attacker.isServer());
    }

    public static boolean shouldApply(Mob target, float damage, int duration) {
        if (!target.buffManager.hasBuff(RPGBuffs.IGNITED)) return true;

        float finalDamage = damage * duration / 1000F;

        ActiveBuff ab = target.buffManager.getBuff(RPGBuffs.IGNITED);
        float finalOldDamage = ab.getGndData().getFloat("igniteDamage") * ab.getDurationLeft() / 1000F;

        return finalDamage > finalOldDamage;
    }

    public static void setIgniteDamage(ActiveBuff activeBuff, float damage) {
        Mob attacker = activeBuff.getAttacker() == null ? null : activeBuff.getAttacker().getAttackOwner();
        if (attacker != null) damage *= attacker.buffManager.getModifier(RPGModifiers.IGNITE_DAMAGE);
        activeBuff.getGndData().setFloat("igniteDamage", damage);
    }
}
