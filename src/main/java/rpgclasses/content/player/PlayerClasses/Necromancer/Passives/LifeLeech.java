package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class LifeLeech extends SimpleBuffPassive {
    public LifeLeech(int levelMax, int requiredClassLevel) {
        super("lifeleech", "#993333", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (activeBuff.owner.isServer() && event.damage > 0 && event.target.isHostile && !event.wasPrevented && event.damageType.equals(DamageTypeRegistry.SUMMON)) {
                    float healing = event.damage * 0.0005F * getLevel(activeBuff) + activeBuff.getGndData().getFloat("healthDot");
                    int trueHealing = (int) healing;

                    activeBuff.getGndData().setFloat("healthDot", healing - trueHealing);

                    if (trueHealing > 0)
                        activeBuff.owner.getLevel().entityManager.events.add(new MobHealthChangeEvent(activeBuff.owner, trueHealing));
                }
            }
        };
    }
}
