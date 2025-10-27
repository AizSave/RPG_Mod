package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class BloodBorn extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(1).setDecimals(2, 0),
            SkillParam.staticParam(20),
            SkillParam.staticParam(2).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public BloodBorn(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                new ModifierValue<>(BuffModifiers.HEALTH_REGEN, -10F).max(-10F).apply(activeBuff);
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN, -10F).max(-10F).apply(activeBuff);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);

                int trueDamage;
                if (activeBuff.owner.getHealth() > 1) {
                    float damage = Math.min(activeBuff.owner.getMaxHealth() * params[2].value() * 0.05F, activeBuff.owner.getHealth() - 1) + activeBuff.getGndData().getFloat("damageDot");
                    trueDamage = (int) damage;

                    activeBuff.getGndData().setFloat("damageDot", damage - trueDamage);
                } else {
                    trueDamage = 0;
                }

                trueDamage -= activeBuff.getGndData().getInt("healthApply");
                activeBuff.getGndData().setInt("healthApply", 0);

                if (trueDamage != 0)
                    activeBuff.owner.getLevel().entityManager.events.add(new MobHealthChangeEvent(activeBuff.owner, -trueDamage));

            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (activeBuff.owner.isServer() && !event.wasPrevented && event.target.isHostile) {
                    float healing = event.damage * params[0].value() * (activeBuff.owner.getMaxHealth() / params[1].value()) + activeBuff.getGndData().getFloat("healthDot");
                    int trueHealing = (int) healing;

                    activeBuff.getGndData().setFloat("healthDot", healing - trueHealing);

                    if (trueHealing > 0)
                        activeBuff.getGndData().setInt("healthApply", trueHealing + activeBuff.getGndData().getInt("healthApply"));
                }
            }
        };
    }
}
