package rpgclasses.content.player.SkillsLogic.Passives;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

abstract public class Passive extends Skill {
    public Passive(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    public Passive setFamily(String family) {
        this.family = family;
        return this;
    }

    @Override
    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("passives", stringID));
        tooltips.add(" ");
        tooltips.add(Localization.translate("passivesdesc", stringID));
        if (requiredClassLevel > 1) {
            tooltips.add(" ");
            tooltips.add(Localization.translate("ui", "requiredclasslevel", "level", requiredClassLevel));
        }
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "maxlevel", "level", levelMax));
        return tooltips;
    }

    @Override
    public void initResources() {
        texture = GameTexture.fromFile("passives/" + stringID);
    }

    public boolean isBasic() {
        return false;
    }

    abstract public void givePassiveBuff(PlayerMob player, PlayerData playerData, int level);

    abstract public void removePassiveBuffs(PlayerMob player);

    @Override
    public boolean containsComplexTooltips() {
        return true;
    }
}
