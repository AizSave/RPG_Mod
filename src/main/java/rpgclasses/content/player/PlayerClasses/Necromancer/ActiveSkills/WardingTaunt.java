package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class WardingTaunt extends ActiveSkill {

    public WardingTaunt(int levelMax, int requiredClassLevel) {
        super("wardingtaunt", "#993333", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        Mob tauntSummon = RPGUtils.findClosestDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player));
        int distance = 50 * activeSkillLevel;
        if (tauntSummon != null) {
            if (player.isServer()) {
                RPGUtils.streamMobsAndPlayers(tauntSummon, distance)
                        .filter(RPGUtils.isValidAttackerFilter(tauntSummon))
                        .filter(mob -> !mob.isPlayer)
                        .forEach(
                                mob -> {
                                    if (mob.ai != null) {
                                        mob.ai.blackboard.put("currentTarget", tauntSummon);
                                        mob.ai.blackboard.put("focusTarget", tauntSummon);
                                    }
                                }
                        );

            } else if (player.isClient()) {
                SoundManager.playSound(GameResources.jingle, SoundEffect.effect(tauntSummon).volume(2F).pitch(0.5F));
                AphAreaList areaList = new AphAreaList(
                        new AphArea(distance, getColor())
                ).setOnlyVision(false);
                areaList.executeClient(player.getLevel(), tauntSummon.x, tauntSummon.y);

            }
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }
}