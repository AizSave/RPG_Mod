package rpgclasses.registry;

import necesse.engine.registries.MobRegistry;
import rpgclasses.mobs.mount.LichSkeletonMob;
import rpgclasses.mobs.summons.DancingFlameMob;
import rpgclasses.mobs.summons.damageable.BeeDamageableSummonMob;
import rpgclasses.mobs.summons.damageable.DryadSaplingDamageableSummonMob;
import rpgclasses.mobs.summons.damageable.NecromancerTombMob;
import rpgclasses.mobs.summons.damageable.necrotic.NecromancerBoneslingerMob;
import rpgclasses.mobs.summons.damageable.necrotic.NecromancerSkeletonMob;
import rpgclasses.mobs.summons.damageable.necrotic.NecromancerSkeletonWarriorMob;
import rpgclasses.mobs.summons.passive.RangerWolfMob;

public class RPGMobs {

    public static void registerCore() {

        // Mounts
        MobRegistry.registerMob("lichskeletonmob", LichSkeletonMob.class, false);

        // Summons
        MobRegistry.registerMob("dancingflame", DancingFlameMob.class, false);

        // Damageable Summons
        MobRegistry.registerMob("necromancerskeleton", NecromancerSkeletonMob.class, false);
        MobRegistry.registerMob("necromancerskeletonwarrior", NecromancerSkeletonWarriorMob.class, false);
        MobRegistry.registerMob("necromancerboneslinger", NecromancerBoneslingerMob.class, false);
        MobRegistry.registerMob("necromancertomb", NecromancerTombMob.class, false);

        MobRegistry.registerMob("beedamageablesummon", BeeDamageableSummonMob.class, false);
        MobRegistry.registerMob("dryadsaplingdamageablesummon", DryadSaplingDamageableSummonMob.class, false);

        // Passive Summons
        MobRegistry.registerMob("rangerwolf", RangerWolfMob.class, false);
    }

}
