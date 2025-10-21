package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.levelevents.NecroticExplosionLevelEvent;
import rpgclasses.utils.RPGUtils;

public class NecroticBarrage extends ActiveSkill {

    public NecroticBarrage(int levelMax, int requiredClassLevel) {
        super("necroticbarrage", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        float damage = 5 * playerData.getLevel() + 5 * playerData.getIntelligence(player) * activeSkillLevel;
        float poisonDamage = damage * 0.2F;
        RPGUtils.getAllDamageableFollowers(player, 2048, RPGUtils.isNecroticFollowerFilter(player))
                .forEach(
                        mob -> {
                            if (mob.isServer()) {
                                mob.remove(0, 0, null, true);
                                mob.getLevel().entityManager.events.add(new NecroticExplosionLevelEvent(mob.x, mob.y, 250, new GameDamage(DamageTypeRegistry.MAGIC, damage), player, poisonDamage));
                            }
                        }
                );

    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 2048, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }
}