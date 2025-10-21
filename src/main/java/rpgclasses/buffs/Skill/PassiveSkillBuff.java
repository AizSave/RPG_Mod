package rpgclasses.buffs.Skill;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;

public class PassiveSkillBuff extends SkillBuff {
    public PassiveSkillBuff() {
        this.canCancel = false;
        this.isVisible = true;
        this.isPassive = true;
        this.shouldSave = false;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
    }
}