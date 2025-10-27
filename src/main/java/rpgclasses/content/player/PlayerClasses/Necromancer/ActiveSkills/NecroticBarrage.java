package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.levelevents.NecroticExplosionLevelEvent;
import rpgclasses.utils.RPGUtils;

public class NecroticBarrage extends ActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.damageParam(4),
            SkillParam.staticParam(10),
            SkillParam.staticParam(20).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(20);
    }

    public NecroticBarrage(int levelMax, int requiredClassLevel) {
        super("necroticbarrage", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 20000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        float damage = params[0].value(playerData.getLevel(), activeSkillLevel);
        float poisonDuration = params[1].value();
        float poisonDamage = damage * params[2].value();
        RPGUtils.getAllDamageableFollowers(player, 2048, RPGUtils.isNecroticFollowerFilter(player))
                .forEach(
                        mob -> {
                            if (mob.isServer()) {
                                mob.remove(0, 0, null, true);
                                mob.getLevel().entityManager.events.add(new NecroticExplosionLevelEvent(mob.x, mob.y, 150, new GameDamage(DamageTypeRegistry.MAGIC, damage), player, poisonDamage, poisonDuration));
                            }
                        }
                );

    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, int activeSkillLevel, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 2048, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }
}