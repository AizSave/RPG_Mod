package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class NecroticHero extends SimpleBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0),
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0),
            SkillParam.staticParam(5)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(10);
    }

    public NecroticHero(int levelMax, int requiredClassLevel) {
        super("necrotichero", "#993333", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 20000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        Mob tauntSummon = RPGUtils.findClosestDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player));
        giveBuff(player, tauntSummon, activeSkillLevel);
        if (tauntSummon != null) {
            if (player.isServer()) {
                RPGUtils.streamMobsAndPlayers(tauntSummon, 200)
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
                        new AphArea(200, getColor())
                ).setOnlyVision(false);
                areaList.executeClient(player.getLevel(), tauntSummon.x, tauntSummon.y);

            }
        }
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, params[0].value(getLevel(activeBuff)));
                activeBuff.setModifier(BuffModifiers.INCOMING_DAMAGE_MOD, 1F - params[1].value(getLevel(activeBuff)));
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return (int) (params[2].value(activeSkillLevel) * 1000);
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, int activeSkillLevel, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }
}