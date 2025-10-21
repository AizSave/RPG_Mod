package rpgclasses.buffs;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;

public class MagicPoisonBuff extends Buff {
    public MagicPoisonBuff() {
        this.shouldSave = false;
        this.isImportant = true;
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
            owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(150, 0, 150)).height(16.0F);
        }

    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        updateModifier(activeBuff);
    }

    public static void apply(Mob attacker, Mob target, float damage, float duration) {
        apply(attacker, target, damage, (int) (duration * 1000));
    }

    public static void apply(Mob attacker, Mob target, float damage, int duration) {
        ActiveBuff ab = new ActiveBuff(RPGBuffs.MAGIC_POISON, target, duration, attacker);
        setPoisonDamage(ab, damage);

        if (attacker.isPlayer) {
            PlayerData playerData = PlayerDataList.getPlayerData((PlayerMob) attacker);
            if (playerData.hasMasterySkill(Mastery.PYROMANCER)) {
                duration *= 2;
            }
        }

        if (shouldApply(target, damage, duration)) {
            target.buffManager.addBuff(ab, attacker.isServer());
        }
    }

    public static boolean shouldApply(Mob target, float damage, int duration) {
        if (!target.buffManager.hasBuff(RPGBuffs.MAGIC_POISON)) return true;

        float finalDamage = damage * duration / 1000F;

        ActiveBuff ab = target.buffManager.getBuff(RPGBuffs.MAGIC_POISON);
        float finalOldDamage = ab.getGndData().getFloat("poisonDamage") * ab.getDurationLeft() / 1000F;

        return finalDamage > finalOldDamage;
    }

    public static float getPoisonDamage(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("poisonDamage");
    }

    public static void setPoisonDamage(ActiveBuff activeBuff, float damage) {
        activeBuff.getGndData().setFloat("poisonDamage", damage);
    }

    public static void updateModifier(ActiveBuff activeBuff) {
        activeBuff.setModifier(BuffModifiers.POISON_DAMAGE_FLAT, activeBuff.getGndData().getFloat("poisonDamage"));
    }
}
