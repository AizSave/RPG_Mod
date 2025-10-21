package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;

public class Bastion extends Mastery {

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
                activeBuff.setModifier(BuffModifiers.RESILIENCE_REGEN_FLAT, 0.25F * (owner.isInCombat() ? (1.0F + owner.getCombatRegen()) : (1.0F + owner.getRegen() + owner.getCombatRegen())));
            }
        };
    }
}
