package rpgclasses.content.player.Mastery.MasterySkills;

import rpgclasses.content.player.Mastery.FlatMastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Hunter extends FlatMastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(3)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Hunter(String stringID, String color, String... extraTooltips) {
        super(stringID, color, extraTooltips);
    }
}
