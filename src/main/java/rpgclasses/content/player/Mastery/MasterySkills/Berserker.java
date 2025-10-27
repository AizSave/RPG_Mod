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
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Berserker extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(50).setDecimals(2, 0),
            SkillParam.staticParam(25).setDecimals(2, 0),
            SkillParam.staticParam(25).setDecimals(2, 0),
            SkillParam.staticParam(3)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

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
                if (healthPercent <= params[0].value()) {
                    activeBuff.setModifier(
                            BuffModifiers.MELEE_CRIT_CHANCE, params[1].value()
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
                    giveDatalessSecondaryPassiveBuff(activeBuff.owner, params[3].value());
                }
            }
        };
    }

    @Override
    public SecondaryMasteryBuff secondaryMasteryBuff() {
        float value = params[2].value();
        return new SimpleSecondaryMasteryBuff(
                new ModifierValue<>(BuffModifiers.ATTACK_SPEED, value),
                new ModifierValue<>(BuffModifiers.SPEED, value)
        );
    }
}
