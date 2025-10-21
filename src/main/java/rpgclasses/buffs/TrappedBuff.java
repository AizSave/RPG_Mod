package rpgclasses.buffs;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

public class TrappedBuff extends Buff {
    public TrappedBuff() {
        this.isImportant = true;
        this.canCancel = false;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.addModifier(BuffModifiers.SLOW, 10.0F);
        activeBuff.addModifier(BuffModifiers.SPEED, -10.0F);
        activeBuff.addModifier(BuffModifiers.PARALYZED, true);
        activeBuff.addModifier(BuffModifiers.INTIMIDATED, true);
        activeBuff.addModifier(BuffModifiers.KNOCKBACK_INCOMING_MOD, 0F);
    }
}
