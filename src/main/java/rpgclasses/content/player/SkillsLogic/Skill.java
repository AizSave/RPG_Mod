package rpgclasses.content.player.SkillsLogic;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import org.jetbrains.annotations.NotNull;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.utils.RPGColors;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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

    public static String[] changes = new String[]{"skilllevel", "playerlevel", "endurance", "speed", "strength", "intelligence", "grace"};

    public ListGameTooltips getToolTips() {
        ListGameTooltips tooltips = new ListGameTooltips();
        for (String string : getToolTipsText()) {
            if (string.contains("<") && string.contains(">")) {
                for (String change : changes) {
                    string = string.replaceAll("<" + change + ">", Localization.translate("skillsdesckeys", change));
                }
            }
            tooltips.add(string);
        }
        return tooltips;
    }

    public Skill setFamily(String family) {
        this.family = family;
        return this;
    }

    abstract public List<String> getToolTipsText();

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
            return getToolTips();
        }

        PlayerData playerData = PlayerDataList.getPlayerData(player);
        ListGameTooltips tooltips = new ListGameTooltips();

        List<String> raw = getToolTipsText();
        raw.set(0, raw.get(0) + " - " + Localization.translate("ui", "level", "level", skillLevel));

        for (String rawTip : raw) {
            String processed = processToolTip(rawTip, skillLevel, player, playerData, onlyChanges);
            if (processed != null) {
                tooltips.add(processed);
            }
        }
        return tooltips;
    }


    public String[] getExtraTooltips() {
        return new String[0];
    }

    public String[] getFinalExtraTooltips(PlayerMob player, boolean processComplex) {
        String[] tooltips = getExtraTooltips().clone();

        PlayerData playerData = PlayerDataList.getPlayerData(player);

        for (int i = 0; i < tooltips.length; i++) {
            String tooltip = Localization.translate("extraskilldesc", tooltips[i]);
            String finalTooltip = processComplex ? processToolTip(tooltip, 0, player, playerData) : tooltip;
            if (finalTooltip != null) {
                tooltips[i] = finalTooltip;
            }
        }
        return tooltips;
    }

    public static String processToolTip(String toolTip, int skillLevel, PlayerMob player, PlayerData playerData) {
        return processToolTip(toolTip, skillLevel, player, playerData, false);
    }

    public static String processToolTip(String toolTip, int skillLevel, PlayerMob player, PlayerData playerData, boolean onlyChanges) {
        if (toolTip.contains("<") && toolTip.contains(">")) {
            List<String> parts = splitToolTip(toolTip);

            int playerLevel = playerData.getLevel();
            float endurance = playerData.getEndurance(player);
            float speed = playerData.getSpeed(player);
            float strength = playerData.getStrength(player);
            float intelligence = playerData.getIntelligence(player);
            float grace = playerData.getGrace(player);

            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (part.startsWith("[[") && part.endsWith("]]")) {
                    int removeEnd = 2;
                    int round = -1;
                    if (part.endsWith("↓]]")) {
                        round = 0;
                        removeEnd++;
                    } else if (part.endsWith("→]]")) {
                        round = 1;
                        removeEnd++;
                    } else if (part.endsWith("↑]]")) {
                        round = 2;
                        removeEnd++;
                    }
                    String expr = part.substring(2, part.length() - removeEnd)
                            .replaceAll("<skilllevel>", String.valueOf(skillLevel))
                            .replaceAll("<playerlevel>", String.valueOf(playerLevel))
                            .replaceAll("<endurance>", String.valueOf(endurance))
                            .replaceAll("<speed>", String.valueOf(speed))
                            .replaceAll("<strength>", String.valueOf(strength))
                            .replaceAll("<intelligence>", String.valueOf(intelligence))
                            .replaceAll("<grace>", String.valueOf(grace));

                    float val = calculateValue(expr);
                    String text;
                    if (round == 0) {
                        text = String.valueOf((int) Math.floor(val));
                    } else if (round == 1) {
                        text = String.valueOf(Math.round(val));
                    } else if (round == 2) {
                        text = String.valueOf((int) Math.ceil(val));
                    } else {
                        text = (val == (int) val)
                                ? String.valueOf((int) val)
                                : String.format("%.1f", val);
                    }
                    parts.set(i, text);
                }
            }

            StringBuilder builder = new StringBuilder();
            for (String p : parts) builder.append(p);
            return builder.toString();

        } else {
            return onlyChanges ? null : toolTip;
        }
    }

    private static @NotNull List<String> splitToolTip(String toolTip) {
        List<String> parts = new ArrayList<>();
        int lastIndex = 0;

        while (lastIndex < toolTip.length()) {
            int startIndex = toolTip.indexOf("[[", lastIndex);
            if (startIndex == -1) {
                parts.add(toolTip.substring(lastIndex));
                break;
            }

            if (startIndex > lastIndex) {
                parts.add(toolTip.substring(lastIndex, startIndex));
            }

            int endIndex = toolTip.indexOf("]]", startIndex);
            if (endIndex == -1) {
                parts.add(toolTip.substring(startIndex));
                break;
            }

            parts.add(toolTip.substring(startIndex, endIndex + 2));
            lastIndex = endIndex + 2;
        }

        return parts;
    }

    public boolean containsComplexTooltips() {
        return false;
    }

    private static float calculateValue(String expression) {
        String[] tokens = expression.split(" ");
        List<Float> values = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.equals("x")) {
                float prev = values.remove(values.size() - 1);
                float next;

                try {
                    next = Float.parseFloat(tokens[++i]);
                } catch (RuntimeException e) {
                    next = 0;
                }

                values.add(prev * next);
            } else if (token.equals("+") || token.equals("-")) {
                operators.add(token);
            } else {
                try {
                    float n = Float.parseFloat(token);
                    values.add(n);
                } catch (RuntimeException e) {
                    values.add(0F);
                }
            }
        }

        float result = values.get(0);
        int opIndex = 0;

        for (int i = 1; i < values.size(); i++) {
            String op = operators.get(opIndex++);
            if (op.equals("+")) {
                result += values.get(i);
            } else if (op.equals("-")) {
                result -= values.get(i);
            }
        }

        return result;
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
}
