package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class EndlessLegion extends SimpleBuffPassive {
    public EndlessLegion(int levelMax, int requiredClassLevel) {
        super("endlesslegion", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                this.isVisible = false;
                activeBuff.setModifier(BuffModifiers.MAX_SUMMONS, getLevel(activeBuff) / 4);
            }
        };
    }
}
