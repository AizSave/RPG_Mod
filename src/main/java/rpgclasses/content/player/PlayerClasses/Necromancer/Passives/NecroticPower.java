package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class NecroticPower extends SimpleBuffPassive {
    public NecroticPower(int levelMax, int requiredClassLevel) {
        super("necroticpower", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (event.damage > 0 && !event.wasPrevented && event.damageType == DamageTypeRegistry.MAGIC) {
                    MagicPoisonBuff.apply(activeBuff.owner, event.target, event.damage * 0.05F, 2F * getLevel(activeBuff));
                }
            }
        };
    }
}
