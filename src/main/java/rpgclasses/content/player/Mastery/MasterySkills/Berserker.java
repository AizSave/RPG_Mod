package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.buffs.Skill.SecondaryMasteryBuff;
import rpgclasses.buffs.Skill.SimpleSecondaryMasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;

public class Berserker extends Mastery {

    public Berserker(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                updateBuff(activeBuff);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                updateBuff(activeBuff);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                updateBuff(activeBuff);
            }

            public void updateBuff(ActiveBuff activeBuff) {
                float healthPercent = activeBuff.owner.getHealthPercent();
                if (healthPercent <= 0.5F) {
                    activeBuff.setModifier(
                            BuffModifiers.MELEE_CRIT_CHANCE, 0.25F
                    );
                    this.isVisible = true;
                } else {
                    activeBuff.setModifier(
                            BuffModifiers.MELEE_CRIT_CHANCE, 0F
                    );
                    this.isVisible = false;
                }
            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (activeBuff.owner.isServer() && event.isCrit && event.damageType.equals(DamageTypeRegistry.MELEE) && !event.wasPrevented && event.target.isHostile) {
                    giveDatalessSecondaryPassiveBuff(activeBuff.owner, 2000);
                }
            }
        };
    }

    @Override
    public SecondaryMasteryBuff secondaryMasteryBuff() {
        return new SimpleSecondaryMasteryBuff(
                new ModifierValue<>(BuffModifiers.ATTACK_SPEED, 0.25F),
                new ModifierValue<>(BuffModifiers.SPEED, 0.25F)
        );
    }
}
