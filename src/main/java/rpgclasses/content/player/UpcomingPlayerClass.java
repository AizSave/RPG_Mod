package rpgclasses.content.player;

import necesse.engine.localization.Localization;
import necesse.gfx.gameTooltips.ListGameTooltips;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.content.player.SkillsAndAttributes.SkillsList;

public class UpcomingPlayerClass extends PlayerClass {

    public UpcomingPlayerClass(String stringID, String color) {
        super(stringID, color);
    }

    public ListGameTooltips getToolTips() {
        ListGameTooltips tooltips = new ListGameTooltips();
        tooltips.add("§" + color + Localization.translate("classes", stringID));
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "upcomingclass"));
        return tooltips;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public SkillsList<ActiveSkill> getActiveSkillsList() {
        return new SkillsList<>();
    }

    @Override
    public SkillsList<Passive> getPassivesList() {
        return new SkillsList<>();
    }
}
