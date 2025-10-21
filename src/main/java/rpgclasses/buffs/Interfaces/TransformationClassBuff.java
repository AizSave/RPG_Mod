package rpgclasses.buffs.Interfaces;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.registry.RPGBuffs;

import java.util.ArrayList;
import java.util.List;

public interface TransformationClassBuff {
    void onTransform(ActiveBuff activeBuff, PlayerMob player, Mob target);

    static void apply(Mob mob) {
        PlayerMob player = null;
        if (mob instanceof PlayerMob) {
            player = (PlayerMob) mob;
        } else if (mob.getRider() instanceof PlayerMob) {
            player = (PlayerMob) mob.getRider();
        }
        if (player != null) {
            List<ActiveBuff> activeBuffs = new ArrayList<>(player.buffManager.getBuffs().values());
            for (ActiveBuff activeBuff : activeBuffs) {
                if (activeBuff.buff instanceof TransformationClassBuff) {
                    ((TransformationClassBuff) activeBuff.buff).onTransform(activeBuff, player, mob);
                }
            }

            mob.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.TRANSFORMED, mob, 100, null), mob.isServer());
        }
    }
}
