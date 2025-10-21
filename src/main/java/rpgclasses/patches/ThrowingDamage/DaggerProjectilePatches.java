package rpgclasses.patches.ThrowingDamage;

import aphorea.projectiles.toolitem.DaggerProjectile;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.GameDamage;
import net.bytebuddy.asm.Advice;
import rpgclasses.registry.RPGDamageType;

public class DaggerProjectilePatches {

    @ModMethodPatch(target = DaggerProjectile.class, name = "init", arguments = {})
    public static class init {

        @Advice.OnMethodExit
        static void onExit(@Advice.This DaggerProjectile This) {
            GameDamage gameDamage = This.getDamage();
            This.setDamage(new GameDamage(RPGDamageType.THROWING, gameDamage.damage, gameDamage.armorPen, gameDamage.baseCritChance));
        }
    }

}
