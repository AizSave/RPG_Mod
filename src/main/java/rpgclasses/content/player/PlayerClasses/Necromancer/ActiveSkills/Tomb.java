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
import rpgclasses.mobs.summons.damageable.NecromancerTombMob;

public class Tomb extends CastActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("10 - <skilllevel>"),
            SkillParam.complexParam(5).setDecimals(0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(10);
    }

    public Tomb(int levelMax, int requiredClassLevel) {
        super("tomb", "#666666", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 5000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        NecromancerTombMob mob = (NecromancerTombMob) MobRegistry.getMob("necromancertomb", player.getLevel());
        player.serverFollowersManager.addFollower(stringID, mob, FollowPosition.WALK_CLOSE, null, 1, Integer.MAX_VALUE, null, true);

        mob.setSkillLevel(activeSkillLevel);
        mob.updateStats(player, playerData);

        player.getLevel().entityManager.addMob(mob, player.x, player.y);
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
