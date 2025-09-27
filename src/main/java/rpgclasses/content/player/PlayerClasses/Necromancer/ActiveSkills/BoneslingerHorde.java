package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;

import java.awt.geom.Point2D;

public class BoneslingerHorde extends CastActiveSkill {

    public BoneslingerHorde(int levelMax, int requiredClassLevel) {
        super("boneslingerhorde", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public int castingTime() {
        return 5000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        for (int i = 0; i < activeSkillLevel + 1; i++) {
            if (player.isServer()) {
                DamageableFollowingMob mob = (DamageableFollowingMob) MobRegistry.getMob("necromancerboneslinger", player.getLevel());
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
    public int getBaseCooldown() {
        return 30000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerboneslinger"};
    }
}
