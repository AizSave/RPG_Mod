package rpgclasses.content.player.PlayerClasses.Warrior.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class UnleashingRage extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(1).setDecimals(2, 0),
            SkillParam.staticParam(10)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public UnleashingRage(int levelMax, int requiredClassLevel) {
        super("unleashingrage", "#ff0000", levelMax, requiredClassLevel, false);
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
                float healthPercent = activeBuff.owner.getHealthPercent();
                float increment = getLevel(activeBuff) * params[0].value() * (1 - healthPercent) * params[1].value();
                activeBuff.setModifier(
                        BuffModifiers.ALL_DAMAGE, increment
                );
            }
        };
    }
}
