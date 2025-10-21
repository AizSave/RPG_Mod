package rpgclasses.buffs.MobClasses;

import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.levelevents.IceExplosionLevelEvent;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GlacialMobClassBuff extends MobClassBuff {
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
            float cooldown = getCooldown();
            if (GameRandom.globalRandom.getChance(Math.min(cooldown, (now - lastExplosion) / cooldown))) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 255, 255)).height(16.0F);
            }
        }
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown();
            if ((now - lastExplosion) > cooldown) {
                int range = 200;
                if (RPGUtils.anyTarget(owner, range)) {
                    owner.getLevel().entityManager.events.add(new IceExplosionLevelEvent(owner.x, owner.y, range, new GameDamage(0), owner, false));
                }
                cooldowns.put(owner, now);
            }
        }
    }

    public long getCooldown() {
        return 4000;
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.Debuffs.FREEZING, event.target, 5F, null);
        event.target.buffManager.addBuff(ab, activeBuff.owner.isServer());
    }
}
