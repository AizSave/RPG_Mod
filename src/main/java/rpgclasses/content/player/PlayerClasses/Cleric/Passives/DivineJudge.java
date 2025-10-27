package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGModifiers;

public class DivineJudge extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("5 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public DivineJudge(int levelMax, int requiredClassLevel) {
        super("divinejudge", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                updateBuff(activeBuff);
                isVisible = false;
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
                float transference = params[0].value(getLevel(activeBuff));

                activeBuff.setModifier(
                        RPGModifiers.HOLY_DAMAGE, activeBuff.owner.buffManager.getModifier(BuffModifiers.MAGIC_DAMAGE) * transference
                );
                activeBuff.setModifier(
                        RPGModifiers.HOLY_CRIT_CHANCE, activeBuff.owner.buffManager.getModifier(BuffModifiers.MAGIC_CRIT_CHANCE) * transference
                );
                activeBuff.setModifier(
                        RPGModifiers.HOLY_CRIT_DAMAGE, activeBuff.owner.buffManager.getModifier(BuffModifiers.MAGIC_CRIT_DAMAGE) * transference
                );

            }
        };
    }
}
