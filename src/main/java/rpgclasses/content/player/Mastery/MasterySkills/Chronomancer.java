package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.modifiers.ModifierValue;
import rpgclasses.content.player.Mastery.SimpleMastery;
import rpgclasses.registry.RPGModifiers;

public class Chronomancer extends SimpleMastery {

    public Chronomancer(String stringID, String color) {
        super(stringID, color,
                new ModifierValue<>(RPGModifiers.CAST_TIME, -0.2F)
        );
    }
}
