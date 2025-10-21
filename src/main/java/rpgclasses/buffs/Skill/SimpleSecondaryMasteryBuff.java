package rpgclasses.buffs.Skill;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;

public class SimpleSecondaryMasteryBuff extends SecondaryMasteryBuff {
    public final ModifierValue<?>[] modifiers;

    public SimpleSecondaryMasteryBuff(ModifierValue<?>... modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void init(ActiveBuff buff, BuffEventSubscriber eventSubscriber) {
        for (ModifierValue<?> modifier : this.modifiers) {
            modifier.apply(buff);
        }
    }
}