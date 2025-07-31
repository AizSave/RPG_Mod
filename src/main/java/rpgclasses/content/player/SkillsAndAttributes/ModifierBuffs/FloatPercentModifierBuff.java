package rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;
import necesse.entity.mobs.buffs.ActiveBuff;

public class FloatPercentModifierBuff extends ModifierBuff<Float> {
    public final boolean add;

    public FloatPercentModifierBuff(Modifier<Float> modifier, float value, boolean add) {
        super(modifier, value);
        this.add = add;
    }

    public FloatPercentModifierBuff(Modifier<Float> modifier, float value) {
        this(modifier, value, true);
    }

    public String getTooltip() {
        return Localization.translate("buffmodifiers", getLocalizationString(), "mod", getNumber());
    }

    public String getNumber() {
        String sign = value < 0 ? "" : (add ? "+" : "-");

        float percentValue = value * 100;

        String valueString = (percentValue == Math.floor(percentValue))
                ? String.format("%.0f", percentValue)
                : String.format("%.2f", percentValue).replaceAll("\\.?0+$", "");

        return sign + valueString;
    }

    @Override
    public Float scaledValue(int attributeLevel) {
        return add ? attributeLevel * value : 1F - (attributeLevel * value);
    }
}
