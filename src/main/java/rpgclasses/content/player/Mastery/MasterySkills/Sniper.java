package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;

public class Sniper extends Mastery {

    public Sniper(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff buff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(buff, event);
                if (event.damage.type.equals(DamageTypeRegistry.RANGED)) {
                    float distance = buff.owner.getDistance(event.target);
                    float damageMod = 1 + Math.min(1, distance / 1000);
                    event.damage = event.damage.modDamage(damageMod);
                }
            }
        };
    }
}
