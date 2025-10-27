package rpgclasses.content.player.Mastery;

import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class FlatMastery extends Mastery {
    public String[] extraTooltips;

    public FlatMastery(String stringID, String color, String... extraTooltips) {
        super(stringID, color);
        this.extraTooltips = extraTooltips;
    }

    @Override
    public MasteryBuff masteryBuff() {
        return null;
    }

    @Override
    public String[] getExtraTooltips() {
        return extraTooltips;
    }

    @Override
    public SkillParam[] getParams() {
        return new SkillParam[0];
    }
}
