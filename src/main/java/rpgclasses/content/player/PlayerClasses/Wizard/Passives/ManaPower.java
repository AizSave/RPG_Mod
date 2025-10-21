package rpgclasses.content.player.PlayerClasses.Wizard.Passives;

import necesse.engine.util.GameMath;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class ManaPower extends SimpleBuffPassive {
    public ManaPower(int levelMax, int requiredClassLevel) {
        super("manapower", "#3366ff", levelMax, requiredClassLevel, false);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                updateBuff(activeBuff);
                this.isVisible = false;
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                updateBuff(activeBuff);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                updateBuff(activeBuff);
            }

            public void updateBuff(ActiveBuff activeBuff) {
                float manaPercent = GameMath.clamp(activeBuff.owner.getMana() / activeBuff.owner.getMaxMana(), 0, 1);
                float increment = getLevel(activeBuff) * 0.01F * manaPercent * 10;
                activeBuff.setModifier(
                        BuffModifiers.MAGIC_DAMAGE, increment
                );
            }
        };
    }
}
