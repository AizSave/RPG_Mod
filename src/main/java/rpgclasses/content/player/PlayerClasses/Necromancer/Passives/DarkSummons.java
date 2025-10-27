package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGBuffs;

public class DarkSummons extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("8 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public DarkSummons(int levelMax, int requiredClassLevel) {
        super("darksummons", "#666666", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff activeBuff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(activeBuff, event);
                if (event.damage.type.equals(DamageTypeRegistry.SUMMON) && event.target.buffManager.hasBuff(RPGBuffs.MAGIC_POISON)) {
                    event.damage = event.damage.modDamage(1 + params[0].value(getLevel(activeBuff)));
                }
            }
        };
    }
}
