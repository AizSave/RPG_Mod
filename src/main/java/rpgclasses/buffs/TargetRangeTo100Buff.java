package rpgclasses.buffs;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

public class TargetRangeTo100Buff extends Buff {
    public TargetRangeTo100Buff() {
        this.isVisible = false;
        this.canCancel = false;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        new ModifierValue<>(BuffModifiers.TARGET_RANGE).min(1F).max(1F).apply(activeBuff);
    }
}
