package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class IceEnchantment extends CastBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(10),
            new SkillParam("2 + <skilllevel>"),
            new SkillParam("0.04 + <skilllevel>")
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(20);
    }

    public IceEnchantment(int levelMax, int requiredClassLevel) {
        super("iceenchantment", "#00ffff", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 1000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        GameUtils.streamServerClients(player.getLevel()).forEach(targetPlayer -> {
            if (targetPlayer.isSameTeam(player.getTeam()))
                super.giveBuff(player, targetPlayer.playerMob, activeSkillLevel);
        });

        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(
                        target -> super.giveBuff(player, target, activeSkillLevel)
                );
    }

    @Override
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);

        SoundManager.playSound(GameResources.iceHit, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(200, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }


    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(getColor()).height(16.0F);
                }
            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (event.damage > 0 && !event.wasPrevented) {
                    int skillLevel = getLevel(activeBuff);

                    event.target.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.FREEZING, event.target, params[1].value(skillLevel), activeBuff.owner), activeBuff.owner.isServer());
                    event.target.buffManager.addBuff(new ActiveBuff(BuffRegistry.FROZEN_MOB, event.target, params[2].value(skillLevel), activeBuff.owner), activeBuff.owner.isServer());
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return (int) (params[0].value() * 1000);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 30000;
    }

}
