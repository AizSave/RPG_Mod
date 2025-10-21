package rpgclasses.buffs.Skill;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

abstract public class SkillBuff extends Buff {
    public int getLevel(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("skillLevel");
    }
}