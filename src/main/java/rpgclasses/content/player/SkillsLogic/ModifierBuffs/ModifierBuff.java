package rpgclasses.content.player.SkillsLogic.ModifierBuffs;

import necesse.engine.modifiers.Modifier;
import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.buffs.ActiveBuff;

import java.util.Objects;

abstract public class ModifierBuff<T> {
    public final Modifier<T> modifier;
    public final T value;

    public boolean setMin = false;
    public boolean setMax = false;
    public T maxMod;
    public T minMod;

    public ModifierBuff(Modifier<T> modifier, T value) {
        this.modifier = modifier;
        this.value = value;
    }

    public ModifierBuff<T> doSetMin() {
        this.setMin = true;
        return this;
    }

    public ModifierBuff<T> doSetMin(T minMod) {
        this.setMin = true;
        this.maxMod = minMod;
        return this;
    }

    public ModifierBuff<T> doSetMax() {
        this.setMax = true;
        return this;
    }

    public ModifierBuff<T> doSetMax(T maxMod) {
        this.setMax = true;
        this.maxMod = maxMod;
        return this;
    }

    public void applyBuff(ActiveBuff activeBuff, float level) {
        T value = scaledValue(level);
        activeBuff.addModifier(this.modifier, value);

        ModifierValue<T> modifierValue = new ModifierValue<>(this.modifier);
        if (value instanceof Integer) {
            int base = (Integer) value;
            if (setMin) modifierValue.min((T) (Object) (base + (Integer) minMod));
            if (setMax) modifierValue.max((T) (Object) (base + (Integer) maxMod));
        } else if (value instanceof Float) {
            float base = (Float) value;
            if (setMin) modifierValue.min((T) (Object) (base + (Float) minMod));
            if (setMax) modifierValue.max((T) (Object) (base + (Float) maxMod));
        }
        activeBuff.addModifierLimits(this.modifier, modifierValue.limits);
    }

    public String getLocalizationString() {
        String stringID = modifier.stringID;
        if (stringID.startsWith("rangedam") || (stringID.startsWith("range") && !stringID.startsWith("ranged"))) {
            return stringID.replace("range", "ranged");
        } else if (Objects.equals(stringID, "attackmovementmod")) {
            return "attackmovement";
        } else if (Objects.equals(stringID, "incomingdamagemod")) {
            return "incdamage";
        } else if (Objects.equals(stringID, "staminacapacity")) {
            return "staminacap";
        } else if (Objects.equals(stringID, "alldamage")) {
            return "damage";
        }
        return stringID;
    }

    abstract public String getTooltip();

    abstract public T scaledValue(float level);
}
