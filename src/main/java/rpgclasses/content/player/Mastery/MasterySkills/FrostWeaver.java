package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;

public class FrostWeaver extends Mastery {

    public FrostWeaver(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff buff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(buff, event);
                if (event.target.buffManager.hasBuff(BuffRegistry.Debuffs.FROSTSLOW) ||
                        event.target.buffManager.hasBuff(BuffRegistry.Debuffs.FREEZING) ||
                        event.target.buffManager.hasBuff(BuffRegistry.Debuffs.FROSTBURN)
                ) {
                    event.damage = event.damage.modDamage(1.5F);
                }
            }
        };
    }
}