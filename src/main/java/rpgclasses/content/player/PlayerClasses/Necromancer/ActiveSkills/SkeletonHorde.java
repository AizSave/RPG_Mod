package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;

import java.awt.geom.Point2D;

public class SkeletonHorde extends CastActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("1 + <skilllevel>")
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(10);
    }

    public SkeletonHorde(int levelMax, int requiredClassLevel) {
        super("skeletonhorde", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        for (int i = 0; i < params[0].valueInt(activeSkillLevel); i++) {
            if (player.isServer()) {
                DamageableFollowingMob mob = (DamageableFollowingMob) MobRegistry.getMob("necromancerskeleton", player.getLevel());
                player.serverFollowersManager.addFollower(stringID, mob, FollowPosition.WALK_CLOSE, null, 1, Integer.MAX_VALUE, null, true);

                mob.updateStats(player, playerData);

                Point2D.Float target = getRandomClosePlace(player);
                player.getLevel().entityManager.addMob(mob, target.x, target.y);
            }
        }
    }

    @Override
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(player.x, player.y).volume(1F).pitch(0.5F));
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 30000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerskeleton"};
    }
}
