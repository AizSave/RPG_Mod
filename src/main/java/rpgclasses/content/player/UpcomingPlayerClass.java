package rpgclasses.content.player;

import necesse.engine.localization.Localization;
import necesse.gfx.gameTooltips.ListGameTooltips;

public class UpcomingPlayerClass extends PlayerClass {

    public UpcomingPlayerClass(String stringID, String color) {
        super(stringID, color);
    }

    public ListGameTooltips getToolTips() {
        ListGameTooltips tooltips = new ListGameTooltips();
        tooltips.add("§" + color + Localization.translate("classes", stringID) + " §0- " + mod.name);
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "upcomingclass"));
        return tooltips;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
