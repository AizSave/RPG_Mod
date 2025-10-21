package rpgclasses.buffs.Passive;

import necesse.entity.mobs.buffs.staticBuffs.Buff;

abstract public class PassiveBuff extends Buff {
    public PassiveBuff() {
        this.canCancel = false;
        this.isVisible = false;
        this.isPassive = true;
        this.shouldSave = false;
    }
}
