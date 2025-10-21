package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.MarkedBuff;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HuntersMark extends SimpleBuffActiveSkill {

    public HuntersMark(int levelMax, int requiredClassLevel) {
        super("huntersmark", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        super.giveBuff(player, player, playerData, activeSkillLevel);

        List<Mob> validMobs = RPGUtils.getAllTargets(player, 400)
                .collect(Collectors.toList());

        if (!validMobs.isEmpty()) {
            validMobs.sort(Comparator.comparingInt(Mob::getMaxHealth).reversed());

            Mob target = validMobs.get(0);
            MarkedBuff.markMob(player, target, getDuration(activeSkillLevel));
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 400) ? null : "notarget";
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        AphAreaList areaList = new AphAreaList(
                new AphArea(400, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                PlayerMob player = (PlayerMob) activeBuff.owner;
                if (!event.wasPrevented && MarkedBuff.isMarked(player, event.target) && 0 >= event.target.getHealth()) {
                    giveBuff2(player, player, PlayerDataList.getPlayerData(player), getLevel(activeBuff));
                    player.buffManager.removeBuff(getBuffStringID(), activeBuff.owner.isServer());
                }
            }
        };
    }

    @Override
    public ActiveSkillBuff getBuff2() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.RANGED_ATTACK_SPEED, 0.2F * getLevel(activeBuff));
                activeBuff.setModifier(BuffModifiers.SPEED, 0.2F * getLevel(activeBuff));
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 102, 0)).height(16.0F);
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 10000;
    }

    @Override
    public int getDuration2(int activeSkillLevel) {
        return 5000;
    }

    @Override
    public int getBaseCooldown() {
        return 30000;
    }
}
