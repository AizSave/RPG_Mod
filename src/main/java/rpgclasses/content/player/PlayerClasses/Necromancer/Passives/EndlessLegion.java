package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class EndlessLegion extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(4).setDecimals(0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public EndlessLegion(int levelMax, int requiredClassLevel) {
        super("endlesslegion", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                this.isVisible = false;
                activeBuff.setModifier(BuffModifiers.MAX_SUMMONS, getLevel(activeBuff) / params[0].valueInt());
            }
        };
    }
}
