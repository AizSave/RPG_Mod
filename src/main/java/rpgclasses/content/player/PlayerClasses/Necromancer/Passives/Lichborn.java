package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.packet.PacketLifelineEvent;
import necesse.engine.network.packet.PacketMobMount;
import necesse.engine.registries.MobRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeDamageOverTimeTakenEvent;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.level.maps.Level;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.mobs.mount.LichSkeletonMob;

public class Lichborn extends SimpleBuffPassive {
    public Lichborn(int levelMax, int requiredClassLevel) {
        super("lichborn", "#6600ff", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerlich"};
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
                if (level.isServer() && expectedHealth <= 0) {
                    Mob mount = activeBuff.owner.getMount();
                    if (mount instanceof LichSkeletonMob || activeBuff.owner.buffManager.hasBuff(getSecondaryBuffStringID()))
                        return false;

                    int skillLevel = getLevel(activeBuff);
                    activeBuff.owner.setHealth((int) (activeBuff.owner.getMaxHealth() * 0.1F * skillLevel));
                    giveDatalessSecondaryPassiveBuff((PlayerMob) activeBuff.owner, 120000 - skillLevel * 6000);
                    level.getServer().network.sendToClientsWithEntity(new PacketLifelineEvent(activeBuff.owner.getUniqueID()), activeBuff.owner);
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
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                Mob owner = activeBuff.owner;
                Level level = owner.getLevel();
                if (owner.isServer()) {
                    if (owner.isMounted()) {
                        return;
                    }

                    Mob mount = owner.getMount();
                    if (mount instanceof LichSkeletonMob) {
                        this.refreshDurationOnExistingSkeleton((LichSkeletonMob) mount, activeBuff.getDurationLeft());
                    } else {
                        this.spawnAndSetNewSkeleton(level, owner, activeBuff.getDurationLeft());
                    }
                }

                new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 2F).min(2F).apply(activeBuff);

            }

            private void refreshDurationOnExistingSkeleton(LichSkeletonMob mount, int duration) {
                mount.removeAtTime = mount.getTime() + (long) duration;
            }

            private void spawnAndSetNewSkeleton(Level level, Mob target, int duration) {
                LichSkeletonMob lichSkeletonMob = (LichSkeletonMob) MobRegistry.getMob("lichskeletonmob", level);
                lichSkeletonMob.removeAtTime = level.getTime() + (long) duration;
                lichSkeletonMob.setPos(target.x, target.y, true);
                lichSkeletonMob.dx = target.dx;
                lichSkeletonMob.dy = target.dy;
                target.mount(lichSkeletonMob, true, target.x, target.y, true);
                level.entityManager.mobs.add(lichSkeletonMob);
                level.getServer().network.sendToClientsWithEntity(new PacketMobMount(target.getUniqueID(), lichSkeletonMob.getUniqueID(), true, target.x, target.y), target);
            }
        };
    }
}
