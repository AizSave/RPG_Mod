package rpgclasses.content.player.Mastery;

import rpgclasses.buffs.Skill.MasteryBuff;

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
}
