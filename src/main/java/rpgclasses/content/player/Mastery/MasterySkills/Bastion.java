package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Bastion extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(25).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Bastion(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                this.updateModifiers(activeBuff);
            }

            public void clientTick(ActiveBuff activeBuff) {
                this.updateModifiers(activeBuff);
            }

            public void serverTick(ActiveBuff activeBuff) {
                this.updateModifiers(activeBuff);
            }

            private void updateModifiers(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                activeBuff.setModifier(BuffModifiers.RESILIENCE_REGEN_FLAT, params[0].value() * (owner.isInCombat() ? (1.0F + owner.getCombatRegen()) : (1.0F + owner.getRegen() + owner.getCombatRegen())));
            }
        };
    }
}
