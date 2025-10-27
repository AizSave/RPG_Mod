package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class Sacrifice extends ActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("20 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(5);
    }

    public Sacrifice(int levelMax, int requiredClassLevel) {
        super("sacrifice", "#990000", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        Mob sacrifice = RPGUtils.findClosestDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player));
        if (sacrifice != null) {
            sacrifice.remove(0, 0, null, true);

            int healing = (int) (params[0].value(activeSkillLevel) * player.getMaxHealth());

            if (healing > 0) {
                player.getLevel().entityManager.events.add(new MobHealthChangeEvent(player, healing));
            }
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, int activeSkillLevel, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(player.x, player.y).volume(1F).pitch(0.5F));
        SoundManager.playSound(GameResources.croneLaugh, SoundEffect.effect(player.x, player.y).volume(0.5F).pitch(0.5F));
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 26000;
    }
}
