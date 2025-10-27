package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class NecroticRemains extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("0.1 x <skilllevel>"),
            SkillParam.staticParam(10)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public NecroticRemains(int levelMax, int requiredClassLevel) {
        super("necroticremains", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                this.isVisible = false;
            }
        };
    }
}
