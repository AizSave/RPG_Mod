package rpgclasses.content.player.SkillsLogic;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.utils.RPGColors;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

abstract public class Skill {
    public int id;
    public final String stringID;
    public final String color;
    public final int levelMax;
    public final int requiredClassLevel;
    public GameTexture texture;
    public PlayerClass playerClass;

    public String family = null;

    public Skill(String stringID, String color, int levelMax, int requiredClassLevel) {
        this.stringID = stringID;
        this.color = color;
        this.levelMax = levelMax;
        this.requiredClassLevel = requiredClassLevel;
    }

    public static String[] changes = new String[]{"skilllevel", "playerlevel"};

    public ListGameTooltips getBaseToolTips(PlayerMob player) {
        ListGameTooltips tooltips = new ListGameTooltips();
        for (String string : getToolTipsText(player)) {
            if (string.contains("[[") && string.contains("]]")) {
                string = baseToolTipsReplaces(player, string);
                SkillParam[] params = getParams();
                for (int i = 0; i < params.length; i++) {
                    SkillParam param = params[i];
                    string = string.replaceAll("\\[\\[" + i + "]]", param.baseParamValue());
                }
                for (String change : changes) {
                    string = string.replaceAll("<" + change + ">", Localization.translate("skillsdesckeys", change));
                }
            }
            tooltips.add(string);
        }
        return tooltips;
    }

    public String baseToolTipsReplaces(PlayerMob player, String string) {
        return string;
    }

    public Skill setFamily(String family) {
        this.family = family;
        return this;
    }

    abstract public List<String> getToolTipsText(PlayerMob player);

    abstract public void initResources();

    public int getLevel(PlayerMob player) {
        return getLevel(PlayerDataList.getPlayerData(player));
    }

    public int getLevel(PlayerData playerData) {
        return getLevel(playerData.getClassesData()[this.playerClass.id]);
    }

    public int getLevel(PlayerClassData classData) {
        return classData.getPassiveLevels()[id];
    }

    public void registry() {
    }

    public ListGameTooltips getFinalToolTips(PlayerMob player, int skillLevel, boolean onlyChanges) {
        if (!containsComplexTooltips() || skillLevel <= 0) {
            return getBaseToolTips(player);
        }

        PlayerData playerData = PlayerDataList.getPlayerData(player);

        if (playerData == null) {
            return getBaseToolTips(player);
        }

        ListGameTooltips tooltips = new ListGameTooltips();

        List<String> raw = getToolTipsText(player);
        raw.set(0, raw.get(0) + " - " + Localization.translate("ui", "level", "level", skillLevel));

        for (String string : raw) {
            if (string.contains("[[") && string.contains("]]")) {
                string = finalToolTipsReplaces(player, string, playerData, skillLevel);
                SkillParam[] params = getParams();
                for (int i = 0; i < params.length; i++) {
                    SkillParam param = params[i];
                    string = string.replaceAll("\\[\\[" + i + "]]", param.paramValue(playerData.getLevel(), skillLevel));
                }
                tooltips.add(string);
            } else if (!onlyChanges) {
                tooltips.add(string);
            }
        }

        return tooltips;
    }

    public String finalToolTipsReplaces(PlayerMob player, String string, PlayerData playerData, int skillLevel) {
        return string;
    }

    public String[] getAllExtraTooltips() {
        String[] baseTooltips = getExtraTooltips();

        if (addManaUsageExtraToolTip()) {
            String[] result = new String[baseTooltips.length + 1];
            System.arraycopy(baseTooltips, 0, result, 0, baseTooltips.length);
            result[baseTooltips.length] = "manausage";
            return result;
        } else {
            return baseTooltips;
        }
    }

    public String[] getExtraTooltips() {
        return new String[0];
    }

    public String[] getAllExtraTooltips(PlayerMob player, int skillLevel, boolean processFinal) {
        String[] tooltips = getAllExtraTooltips().clone();

        PlayerData playerData = processFinal ? PlayerDataList.getPlayerData(player) : null;

        for (int i = 0; i < tooltips.length; i++) {
            String extraToolTip = tooltips[i];
            String tooltip = Localization.translate("extraskilldesc", extraToolTip);

            SkillParam[] params = ComplexExtraToolTip.get(extraToolTip);
            if (params != null && params.length > 0) {
                if (processFinal) {
                    for (int j = 0; j < params.length; j++) {
                        SkillParam param = params[j];
                        tooltip = tooltip.replaceAll("\\[\\[" + j + "]]", param.paramValue(playerData.getLevel(), skillLevel));
                    }
                } else {
                    for (int j = 0; j < params.length; j++) {
                        SkillParam param = params[j];
                        tooltip = tooltip.replaceAll("\\[\\[" + j + "]]", param.baseParamValue());
                    }

                    for (String change : changes) {
                        tooltip = tooltip.replaceAll("<" + change + ">", Localization.translate("skillsdesckeys", change));
                    }
                }
            }
            tooltips[i] = tooltip;
        }
        return tooltips;
    }


    public boolean containsComplexTooltips() {
        return false;
    }


    public static Point2D.Float getDir(Mob mob) {
        float dirX, dirY;

        if (mob.dx == 0 && mob.dy == 0) {
            Point2D.Float dir = getDirFromFacing(mob.getDir());
            dirX = dir.x;
            dirY = dir.y;
        } else {
            dirX = mob.dx;
            dirY = mob.dy;

            float magnitude = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (magnitude != 0) {
                dirX /= magnitude;
                dirY /= magnitude;
            }

            Point2D.Float expected = getDirFromFacing(mob.getDir());

            if (expected.x != 0 && dirX > 0 != expected.x > 0) {
                dirX = expected.x;
            }
            if (expected.y != 0 && dirY > 0 != expected.y > 0) {
                dirY = expected.y;
            }

        }

        return new Point2D.Float(dirX, dirY);
    }

    private static Point2D.Float getDirFromFacing(int dir) {
        switch (dir) {
            case 0:
                return new Point2D.Float(0, -1);
            case 1:
                return new Point2D.Float(1, 0);
            case 2:
                return new Point2D.Float(0, 1);
            case 3:
                return new Point2D.Float(-1, 0);
            default:
                return new Point2D.Float(0, 0);
        }
    }

    public int getColorInt() {
        return RPGColors.getColorInt(color);
    }

    public Color getColor() {
        return RPGColors.getColor(color);
    }

    abstract public SkillParam[] getParams();

    public boolean addManaUsageExtraToolTip() {
        return false;
    }
}
