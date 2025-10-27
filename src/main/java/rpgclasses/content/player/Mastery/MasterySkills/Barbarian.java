package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.Mastery.SimpleMastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Barbarian extends SimpleMastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(100).setDecimals(2, 0),
            SkillParam.staticParam(100).setDecimals(2, 0),
            SkillParam.staticParam(100).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Barbarian(String stringID, String color) {
        super(stringID, color,
                new ModifierValue<>(BuffModifiers.MAX_HEALTH, params[0].value()),
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN, params[1].value()),
                new ModifierValue<>(BuffModifiers.MELEE_DAMAGE, params[2].value()),
                new ModifierValue<>(BuffModifiers.ARMOR, -1F).max(-1F)
        );
    }
}
