package rpgclasses.registry;

import necesse.engine.registries.ProjectileRegistry;
import rpgclasses.projectiles.*;

public class RPGProjectiles {

    public static void registerCore() {
        ProjectileRegistry.registerProjectile("objectprojectile", ObjectProjectile.class, null, null);

        ProjectileRegistry.registerProjectile("lethalarrow", LethalArrowProjectile.class, "lethalarrow", "arrow_shadow");
        ProjectileRegistry.registerProjectile("venomarrow", VenomArrowProjectile.class, "lethalarrow", "arrow_shadow");
        ProjectileRegistry.registerProjectile("explosivearrow", ExplosiveArrowProjectile.class, "explosivearrow", "arrow_shadow");

        ProjectileRegistry.registerProjectile("plasmagrenade", PlasmaGrenadeProjectile.class, null, null);

        ProjectileRegistry.registerProjectile("iceball", IceBallProjectile.class, null, null);
        ProjectileRegistry.registerProjectile("fireball", FireballProjectile.class, null, null);
        ProjectileRegistry.registerProjectile("minifireball", MiniFireballProjectile.class, null, null);
    }

}
