package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Sniper extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(100).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Sniper(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff buff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(buff, event);
                if (event.damage.type.equals(DamageTypeRegistry.RANGED)) {
                    float distance = buff.owner.getDistance(event.target);
                    float damageMod = 1 + Math.min(1, distance / 1000);
                    event.damage = event.damage.modDamage(damageMod * params[0].value());
                }
            }
        };
    }
}
