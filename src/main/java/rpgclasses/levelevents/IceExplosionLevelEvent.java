package rpgclasses.levelevents;

import necesse.engine.registries.BuffRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.GameResources;

import java.awt.*;

public class IceExplosionLevelEvent extends RPGExplosionLevelEvent {
    public IceExplosionLevelEvent() {
        super();
    }

    public IceExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner, boolean hitsOwner) {
        super(x, y, range, damage, owner, hitsOwner, new Color(50, 100, 255), new Color(20, 20, 155));
    }

    @Override
    protected void playExplosionEffects() {
        SoundManager.playSound(GameResources.explosionHeavy, SoundEffect.effect(this.x, this.y).volume(0.5F).pitch(0.5F));
        this.level.getClient().startCameraShake(this.x, this.y, 200, 40, 1F, 0.8F, true);
    }

    @Override
    public float getParticleCount(float currentRange, float lastRange) {
        return super.getParticleCount(currentRange, lastRange) * 1.5F;
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        float changedDistance = mob == ownerMob ? distance * 2 : distance;
        if (changedDistance < range) {
            super.onMobWasHit(mob, changedDistance);
            mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.FREEZING, mob, 5F, null), true);
            if (!mob.isBoss()) mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.FROZEN_MOB, mob, 1F, null), true);

            if (ownerMob.isHostile) {
                float duration = distance <= 32 ? 10F : Math.max(10F / (distance / 32), 2F);
                mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.BROKEN_ARMOR, mob, duration, null), true);
            }
        }
    }
}
