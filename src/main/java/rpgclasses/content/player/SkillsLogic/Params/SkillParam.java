package rpgclasses.content.player.SkillsLogic.Params;

import necesse.engine.localization.Localization;

import java.util.Objects;

public class SkillParam {
    public String color;
    public String schema;
    public int addedDecimals;
    public int decimals;
    public int round;

    public SkillParam(String schema, SkillParamColors color) {
        this.schema = schema;
        this.color = color == null ? null : color.color;

        this.decimals = 1;
        this.round = 0;
    }

    public SkillParam(String schema) {
        this(schema, SkillParamColors.NORMAL);
    }

    public static SkillParam complexParam(float value, SkillParamColors color) {
        if (value == 1)
            return new SkillParam("<playerlevel> x <skilllevel>", color);

        String valueString = String.valueOf(value);
        if (valueString.endsWith(".0")) valueString = valueString.replace(".0", "");
        return new SkillParam(valueString + " x <playerlevel> x <skilllevel>", color);
    }

    public static SkillParam complexParam(float value) {
        return complexParam(value, SkillParamColors.NORMAL);
    }

    public static SkillParam damageParam(float value) {
        return complexParam(value, SkillParamColors.DAMAGE);
    }

    public static SkillParam healingParam(float value) {
        return complexParam(value, SkillParamColors.HEALING).setDecimals(0);
    }

    public static SkillParam manaParam(float value) {
        return manaParam(value, true);
    }


    public static SkillParam manaParam(float value, boolean scales) {
        String valueString = String.valueOf(value);
        if (valueString.endsWith(".0")) valueString = valueString.replace(".0", "");

        if (!scales) {
            return new SkillParam(valueString, SkillParamColors.MANA);

        } else {
            float valueScale = value / 5;
            String valueScaleString = String.valueOf(valueScale);
            if (valueScaleString.endsWith(".0")) valueScaleString = valueScaleString.replace(".0", "");

            return new SkillParam(valueString + " + " + valueScaleString + " x <skilllevel>", SkillParamColors.MANA);
        }
    }

    public static SkillParam staticParam(float value) {
        String valueString = String.valueOf(value);
        if (valueString.endsWith(".0")) valueString = valueString.replace(".0", "");
        return new SkillParam(valueString, null);
    }


    public SkillParam setDecimals(int addedDecimals, int decimals) {
        this.addedDecimals = addedDecimals;
        this.decimals = decimals + addedDecimals;
        return this;
    }

    public SkillParam setDecimals(int decimals) {
        this.decimals = decimals;
        return this;
    }

    public SkillParam roundCeil() {
        this.round = 1;
        return this;
    }

    public SkillParam roundFloor() {
        this.round = -1;
        return this;
    }

    public float value(int playerLevel, int skillLevel) {
        return this.value(playerLevel, skillLevel, false);
    }

    public float value(int playerLevel, int skillLevel, boolean isVisual) {
        String[] tokens = schema.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = parseSection(tokens[i], playerLevel, skillLevel);
        }

        float result = 0;
        float currentTerm = 0;
        String operator = "+";
        String pendingOp = "+";

        for (String token : tokens) {
            switch (token) {
                case "+":
                case "-":
                    if (pendingOp.equals("+")) {
                        result += currentTerm;
                    } else {
                        result -= currentTerm;
                    }
                    pendingOp = token;
                    operator = "+";
                    currentTerm = 0;
                    break;
                case "*":
                case "x":
                    operator = "*";
                    break;
                default:
                    try {
                        float value;
                        switch (token) {
                            case "<skilllevel>":
                                value = skillLevel;
                                break;
                            case "<playerlevel>":
                                value = playerLevel;
                                break;
                            default:
                                value = Float.parseFloat(token);
                                break;
                        }
                        if (operator.equals("*")) {
                            currentTerm *= value;
                        } else {
                            currentTerm = value;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Not valid schema");
                    }
                    break;
            }
        }

        if (pendingOp.equals("+")) {
            result += currentTerm;
        } else {
            result -= currentTerm;
        }
        return round(result, isVisual);
    }

    public float round(float value, boolean isVisual) {
        int factor = (int) Math.pow(10, decimals);
        float fValue = isVisual || addedDecimals == 0 ? value : value / (int) Math.pow(10, addedDecimals);
        if (round == 0) {
            return (float) Math.round(fValue * factor) / factor;
        } else if (round == 1) {
            return (float) Math.ceil(fValue * factor) / factor;
        } else {
            return (float) Math.floor(fValue * factor) / factor;
        }
    }

    public float valuePlayer(int playerLevel) {
        return value(playerLevel, 0);
    }

    public float value(int skillLevel) {
        return value(0, skillLevel);
    }

    public float value() {
        return value(0, 0);
    }

    public String parseSection(String section, int playerLevel, int skillLevel) {
        if (Objects.equals(section, "playerlevel")) {
            return String.valueOf(playerLevel);
        } else if (Objects.equals(section, "skilllevel")) {
            return String.valueOf(skillLevel);
        } else {
            return section;
        }
    }

    public String baseParamValue() {
        String[] sections = schema.split(" ");
        for (int i = 0; i < sections.length; i++) {
            sections[i] = parseSection(sections[i]);
        }

        return getColorStart() + String.join(" ", sections) + getColorEnd();
    }

    public String parseSection(String section) {
        if (section.startsWith("<") && section.endsWith(">")) {
            return Localization.translate("skillsdesckeys", section.replaceAll("<", "").replaceAll(">", ""));
        } else {
            return section;
        }
    }

    public String paramValue(int playerLevel, int skillLevel) {
        String value = String.valueOf(value(playerLevel, skillLevel, true));
        if (value.endsWith(".0")) value = value.replace(".0", "");
        return getColorStart() + value + getColorEnd();
    }

    public String getColorStart() {
        return color == null ? "" : "§" + color;
    }

    public String getColorEnd() {
        return color == null ? "" : "§0";
    }


    public int valueInt(int playerLevel, int skillLevel) {
        decimals = 0;
        addedDecimals = 0;
        return Math.round(value(playerLevel, skillLevel));
    }

    public int valuePlayerInt(int playerLevel) {
        return valueInt(playerLevel, 0);
    }

    public int valueInt(int skillLevel) {
        return valueInt(0, skillLevel);
    }

    public int valueInt() {
        return valueInt(0, 0);
    }
}

