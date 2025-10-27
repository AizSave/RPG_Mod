package rpgclasses.content.player.PlayerClasses.Warrior.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class Unyielding extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(5).setDecimals(2, 0),
            SkillParam.staticParam(5).setDecimals(2, 0),
            SkillParam.staticParam(20).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Unyielding(int levelMax, int requiredClassLevel) {
        super("unyielding", "#ff0000", levelMax, requiredClassLevel, false);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                updateBuff(activeBuff);
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
                if (healthPercent <= params[2].value()) {
                    activeBuff.setModifier(
                            BuffModifiers.ATTACK_SPEED, params[0].value()
                    );
                    activeBuff.setModifier(
                            BuffModifiers.INCOMING_DAMAGE_MOD, 1F - params[1].value()
                    );
                    this.isVisible = true;
                } else {
                    activeBuff.setModifier(
                            BuffModifiers.ATTACK_SPEED, 0F
                    );
                    activeBuff.setModifier(
                            BuffModifiers.INCOMING_DAMAGE_MOD, 1F
                    );
                    this.isVisible = false;
                }
            }
        };
    }
}
