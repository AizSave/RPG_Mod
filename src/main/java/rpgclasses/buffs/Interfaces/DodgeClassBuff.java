package rpgclasses.buffs.Interfaces;

import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;

public interface DodgeClassBuff {
    void onDodge(ActiveBuff activeBuff, MobBeforeHitEvent event);
}
