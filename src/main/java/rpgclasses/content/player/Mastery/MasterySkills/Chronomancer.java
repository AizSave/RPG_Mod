package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.modifiers.ModifierValue;
import rpgclasses.content.player.Mastery.SimpleMastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.registry.RPGModifiers;

public class Chronomancer extends SimpleMastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(20).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Chronomancer(String stringID, String color) {
        super(stringID, color,
                new ModifierValue<>(RPGModifiers.CAST_TIME, -params[0].value())
        );
    }
}
