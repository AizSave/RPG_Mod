package rpgclasses.buffs.MobClasses;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.levelevents.FireExplosionLevelEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ExplosiveMobClassBuff extends MobClassBuff {
    @Override
    public float damageBoost() {
        return 0.1F;
    }

    public Map<Mob, Long> cooldowns = new HashMap<>();

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null && owner.isVisible()) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown();
            if ((now - lastExplosion) > cooldown) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(255, 0, 0)).height(16.0F);
            }
        }
    }

    @Override
    public void onWasHit(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown();
            if ((now - lastExplosion) > cooldown) {
                int range = 200;
                GameDamage damage = new GameDamage(mobData.levelScaling());
                owner.getLevel().entityManager.events.add(new FireExplosionLevelEvent(owner.x, owner.y, range, damage, owner, false, true));
                cooldowns.put(owner, now);
            }
        }
    }

    public long getCooldown() {
        return 6000;
    }
}
