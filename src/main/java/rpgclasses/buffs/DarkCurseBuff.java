package rpgclasses.buffs;

import necesse.engine.localization.Localization;
import necesse.engine.util.GameBlackboard;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.gameTooltips.ListGameTooltips;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class DarkCurseBuff extends Buff {
    public DarkCurseBuff() {
        this.isVisible = true;
        this.isImportant = true;
        this.canCancel = false;
        this.shouldSave = true;
    }

    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.addModifier(BuffModifiers.INCOMING_DAMAGE_MOD, 1.25F);
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        tick(activeBuff);
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        tick(activeBuff);
        Mob owner = activeBuff.owner;
        GameRandom random = GameRandom.globalRandom;
        AtomicReference<Float> currentAngle = new AtomicReference<>(random.nextFloat() * 360.0F);
        float distance = 75.0F;

        for (int i = 0; i < 4; ++i) {
            owner.getLevel().entityManager.addParticle(owner.x + GameMath.sin(currentAngle.get()) * distance + (float) random.getIntBetween(-5, 5), owner.y + GameMath.cos(currentAngle.get()) * distance + (float) random.getIntBetween(-5, 5) * 0.85F, Particle.GType.CRITICAL).sprite(GameResources.puffParticles.sprite(random.getIntBetween(0, 4), 0, 12)).height(0.0F).moves((pos, delta, lifeTime, timeAlive, lifePercent) -> {
                float angle = currentAngle.accumulateAndGet(delta * 30.0F / 250.0F, Float::sum);
                float distY = (distance - 20.0F) * 0.85F;
                pos.x = owner.x + GameMath.sin(angle) * (distance - distance / 2.0F * lifePercent);
                pos.y = owner.y + GameMath.cos(angle) * distY - 20.0F * lifePercent;
            }).color((options, lifeTime, timeAlive, lifePercent) -> {
                options.color(new Color(0, 0, 0));
                if (lifePercent > 0.5F) {
                    options.alpha(2.0F * (1.0F - lifePercent));
                }

            }).size((options, lifeTime, timeAlive, lifePercent) -> {
                options.size(22, 22);
            }).lifeTime(1000);
        }
    }

    public void tick(ActiveBuff activeBuff) {
        if (activeBuff.owner.getMaxHealth() > activeBuff.owner.getHealth()) {
            activeBuff.addModifier(BuffModifiers.MAX_HEALTH_FLAT, (int) Math.ceil((activeBuff.owner.getHealth() - activeBuff.owner.getMaxHealth()) / activeBuff.owner.buffManager.getModifier(BuffModifiers.MAX_HEALTH)));
        }
    }

    @Override
    public ListGameTooltips getTooltip(ActiveBuff ab, GameBlackboard blackboard) {
        ListGameTooltips tooltips = super.getTooltip(ab, blackboard);
        tooltips.add(Localization.translate("buffdesc", "darkcursebuff1"));
        tooltips.add(Localization.translate("buffdesc", "darkcursebuff2"));
        return tooltips;
    }
}
