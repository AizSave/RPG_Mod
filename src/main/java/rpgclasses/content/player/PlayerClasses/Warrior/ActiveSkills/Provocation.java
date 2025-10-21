package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.Objects;

public class Provocation extends SimpleBuffActiveSkill {

    public Provocation(int levelMax, int requiredClassLevel) {
        super("provocation", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 1000 * activeSkillLevel;
    }

    @Override
    public int getBaseCooldown() {
        return 15000;
    }

    @Override
    public ActiveBuff getActiveBuff(PlayerMob player, Mob target, PlayerData playerData, int activeSkillLevel) {
        ActiveBuff ab = super.getActiveBuff(player, target, playerData, activeSkillLevel);
        ab.getGndData().setString("playerTarget", playerData.playerName);
        return ab;
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        RPGUtils.streamMobsAndPlayers(player, 500)
                .filter(RPGUtils.isValidAttackerFilter(player))
                .filter(mob -> !mob.isPlayer)
                .forEach(
                        target -> super.giveBuff(player, target, playerData, activeSkillLevel)
                );

    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.croneLaugh, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(500, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {

            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.TARGET_RANGE, 10F);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                tick(activeBuff);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                tick(activeBuff);
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(255, 0, 0)).height(16.0F);
                }
            }

            public void tick(ActiveBuff activeBuff) {
                if (activeBuff.owner.ai != null) {
                    PlayerMob playerTarget = getPlayerTarget(activeBuff);
                    if (playerTarget != null) {
                        activeBuff.owner.ai.blackboard.put("currentTarget", playerTarget);
                        activeBuff.owner.ai.blackboard.put("focusTarget", playerTarget);
                    }
                }
            }

            public PlayerMob getPlayerTarget(ActiveBuff activeBuff) {
                String playerName = activeBuff.getGndData().getString("playerTarget");
                if (playerName == null) return null;
                return RPGUtils.streamPlayers(activeBuff.owner, 600)
                        .filter(player -> Objects.equals(player.playerName, playerName))
                        .findFirst().orElse(null);
            }
        };
    }
}