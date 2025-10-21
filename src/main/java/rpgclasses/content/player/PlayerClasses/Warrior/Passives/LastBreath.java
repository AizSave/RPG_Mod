package rpgclasses.content.player.PlayerClasses.Warrior.Passives;

import necesse.engine.network.packet.PacketLifelineEvent;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.*;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.particle.Particle;
import necesse.level.maps.Level;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.utils.RPGColors;

public class LastBreath extends SimpleBuffPassive {
    public LastBreath(int levelMax, int requiredClassLevel) {
        super("lastbreath", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {

            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                this.isVisible = false;
                eventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, (event) -> {
                    if (this.runLogic(activeBuff, event.getExpectedHealth())) {
                        event.prevent();
                    }
                });
            }

            @Override
            public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
                super.onBeforeHitCalculated(activeBuff, event);
                if (this.runLogic(activeBuff, event.getExpectedHealth())) {
                    event.prevent();
                }

            }

            private boolean runLogic(ActiveBuff activeBuff, int expectedHealth) {
                Level level = activeBuff.owner.getLevel();
                if (!activeBuff.owner.buffManager.hasBuff(getSecondaryBuffStringID()) && level.isServer() && expectedHealth <= 0) {
                    activeBuff.owner.setHealth(1);
                    giveDatalessSecondaryPassiveBuff((PlayerMob) activeBuff.owner, getLevel(activeBuff) * 3000);
                    level.getServer().network.sendToClientsWithEntity(new PacketLifelineEvent(activeBuff.owner.getUniqueID()), activeBuff.owner);
                    activeBuff.owner.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.LIFELINE_COOLDOWN, activeBuff.owner, 300.0F, null), activeBuff.owner.isServer());
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return new SecondaryPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                buffEventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, MobBeforeDamageOverTimeTakenEvent::prevent);
            }

            @Override
            public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
                super.onBeforeHit(activeBuff, event);
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;
            }

            @Override
            public void onRemoved(ActiveBuff activeBuff) {
                super.onRemoved(activeBuff);
                activeBuff.owner.setHealth(0);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                if (activeBuff.owner.getHealth() > 1) activeBuff.owner.setHealth(1);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible()) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), GameRandom.globalRandom.nextInt(2) == 0 ? Particle.GType.COSMETIC : Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(GameRandom.globalRandom.getOneOf(RPGColors.red, RPGColors.black)).height(16.0F);
                }

                if (activeBuff.owner.getHealth() > 1) owner.setHealth(1);
            }
        };
    }
}
