package rpgclasses.content.player.SkillsLogic.ModifierBuffs;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;

public class FloatModifierBuff extends ModifierBuff<Float> {
    public FloatModifierBuff(Modifier<Float> modifier, float value) {
        super(modifier, value);
    }

    public String getTooltip() {
        return Localization.translate("buffmodifiers", getLocalizationString(), "mod", getNumber());
    }

    public String getNumber() {
        String sign = (value > 0 ? "+" : "");

        String valueString = (value == Math.floor(value))
                ? String.format("%.0f", value)
                : String.format("%.2f", value).replaceAll("\\.?0+$", "");

        return sign + valueString;
    }

    @Override
    public Float scaledValue(float level) {
        return level * value;
    }
}
