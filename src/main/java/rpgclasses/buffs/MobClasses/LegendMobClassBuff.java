package rpgclasses.buffs.MobClasses;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;

import java.awt.*;

public class LegendMobClassBuff extends MobClassBuff {
    @Override
    public float healthBoost() {
        return 1F;
    }

    @Override
    public float healthRegenBoost() {
        return 0.2F;
    }

    @Override
    public float damageBoost() {
        return 1F;
    }

    @Override
    public float speedBoost() {
        return 0.5F;
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null && owner.isVisible()) {
            owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(255, 153, 0)).givesLight(36, 100).height(16.0F);
        }
    }

}
