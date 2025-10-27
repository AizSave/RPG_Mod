package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class DarkMagic extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("5 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public DarkMagic(int levelMax, int requiredClassLevel) {
        super("darkmagic", "#666666", levelMax, requiredClassLevel);
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
                        BuffModifiers.MAGIC_DAMAGE, activeBuff.owner.buffManager.getModifier(BuffModifiers.SUMMON_DAMAGE) * transference
                );
                activeBuff.setModifier(
                        BuffModifiers.MAGIC_ATTACK_SPEED, activeBuff.owner.buffManager.getModifier(BuffModifiers.SUMMON_ATTACK_SPEED) * transference
                );
                activeBuff.setModifier(
                        BuffModifiers.MAGIC_CRIT_CHANCE, activeBuff.owner.buffManager.getModifier(BuffModifiers.SUMMON_CRIT_CHANCE) * transference
                );
                activeBuff.setModifier(
                        BuffModifiers.MAGIC_CRIT_DAMAGE, activeBuff.owner.buffManager.getModifier(BuffModifiers.SUMMON_CRIT_DAMAGE) * transference
                );

            }
        };
    }
}
