package rpgclasses.buffs.MobClasses;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import aphorea.utils.magichealing.AphMagicHealing;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HealerMobClassBuff extends MobClassBuff {
    @Override
    public float healthBoost() {
        return 0.2F;
    }

    @Override
    public float healthRegenBoost() {
        return 0.02F;
    }

    public Map<Mob, Long> cooldowns = new HashMap<>();

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null && owner.isVisible()) {
            long lastArea = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            float cooldown = getCooldown();
            if (GameRandom.globalRandom.getChance(Math.min(cooldown, (now - lastArea) / cooldown))) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 255, 0)).height(16.0F);
            }
        }
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastArea = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown();
            if ((now - lastArea) > cooldown) {
                int range = 300;

                if (owner.getHealthPercent() < 1 || RPGUtils.streamMobs(owner, range).anyMatch(
                        t -> AphMagicHealing.canHealMob(owner, t) && t.getHealthPercent() < 1
                )) {
                    int heal = mobData.levelScaling() * 5;

                    AphAreaList areaList = new AphAreaList(
                            new AphArea(range, new Color(0, 255, 0, 102)).setHealingArea(heal)
                    ).setOnlyVision(false);

                    areaList.execute(owner, true);
                }

                cooldowns.put(owner, now);
            }
        }
    }

    public long getCooldown() {
        return 6000;
    }

}
