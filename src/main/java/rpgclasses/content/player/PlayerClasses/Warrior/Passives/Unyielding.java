package rpgclasses.content.player.PlayerClasses.Warrior.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class Unyielding extends SimpleBuffPassive {
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
                if (healthPercent <= 0.2F) {
                    float increment = getLevel(activeBuff) * 0.05F;
                    activeBuff.setModifier(
                            BuffModifiers.ATTACK_SPEED, increment
                    );
                    activeBuff.setModifier(
                            BuffModifiers.INCOMING_DAMAGE_MOD, 1F - increment
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
