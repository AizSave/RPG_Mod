package rpgclasses.content.player.SkillsLogic;

import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

import java.util.HashMap;
import java.util.Map;

public class ComplexExtraToolTip {
    public static Map<String, SkillParam[]> extraToolTips = new HashMap<>();

    public static void registerCore() {
        registerExtraToolTip("necromancerskeleton",
                new SkillParam("10 x <playerlevel>"),
                new SkillParam("2 x <playerlevel>")
        );
        registerExtraToolTip("necromancerskeletonwarrior",
                new SkillParam("20 x <playerlevel>"),
                new SkillParam("3 x <playerlevel>")
        );
        registerExtraToolTip("necromancerboneslinger",
                new SkillParam("5 x <playerlevel>"),
                new SkillParam("2 x <playerlevel>")
        );
        registerExtraToolTip("bee",
                new SkillParam("<playerlevel>"),
                new SkillParam("4 x <playerlevel>")
        );
        registerExtraToolTip("dryadsapling",
                new SkillParam("3 x <playerlevel>"),
                new SkillParam("3 x <playerlevel>")
        );
    }

    public static void registerExtraToolTip(String stringID, SkillParam... params) {
        extraToolTips.put(stringID, params);
    }

    public static SkillParam[] get(String extraToolTip) {
        return extraToolTips.get(extraToolTip);
    }

}
