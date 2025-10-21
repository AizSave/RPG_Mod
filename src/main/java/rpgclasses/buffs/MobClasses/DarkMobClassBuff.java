package rpgclasses.buffs.MobClasses;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;

public class DarkMobClassBuff extends MobClassBuff {

    @Override
    public float damageBoost() {
        return 0.3F;
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null && owner.isVisible()) {
            owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 0, 0)).height(16.0F);
        }
    }

    @Override
    public void onWasHit(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            int range = 200;
            int duration = 3000;

            AphAreaList areaList = new AphAreaList(
                    new AphArea(range, new Color(0, 0, 0, 102)).setDebuffArea(duration, RPGBuffs.DARK_CURSE.getStringID())
            ).setOnlyVision(false);

            areaList.execute(owner, activeBuff.owner.isServer());
        }
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            int duration = 10000;
            ActiveBuff ab = new ActiveBuff(RPGBuffs.DARK_CURSE, event.target, duration, null);
            MagicPoisonBuff.apply(activeBuff.owner, event.target, mobData.levelScaling() / 5F, 10F);
            event.target.buffManager.addBuff(ab, activeBuff.owner.isServer());
        }
    }
}
