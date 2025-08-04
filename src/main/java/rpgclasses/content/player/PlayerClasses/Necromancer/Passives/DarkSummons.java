package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGBuffs;

public class DarkSummons extends SimpleBuffPassive {
    public DarkSummons(int levelMax, int requiredClassLevel) {
        super("darksummons", "#666666", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff activeBuff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(activeBuff, event);
                if (event.target.buffManager.hasBuff(RPGBuffs.MagicPoison)) {
                    event.damage = event.damage.modDamage(1 + getLevel(activeBuff) * 0.1F);
                }
            }
        };
    }
}
