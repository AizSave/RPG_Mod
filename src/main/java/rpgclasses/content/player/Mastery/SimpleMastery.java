package rpgclasses.content.player.Mastery;

import necesse.engine.modifiers.ModifierValue;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.buffs.Skill.SimpleMasteryBuff;

public class SimpleMastery extends Mastery {
    public final ModifierValue<?>[] modifiers;

    public SimpleMastery(String stringID, String color, ModifierValue<?>... modifiers) {
        super(stringID, color);
        this.modifiers = modifiers;
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new SimpleMasteryBuff(modifiers);
    }
}
