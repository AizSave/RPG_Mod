package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.AINode;
import necesse.entity.mobs.ai.behaviourTree.CompositeAINode;
import necesse.entity.mobs.ai.behaviourTree.CompositeTypedAINode;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.ai.RunningAwayAI;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Intimidation extends SimpleBuffActiveSkill {

    public Intimidation(int levelMax, int requiredClassLevel) {
        super("intimidation", "#9900cc", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 500 * activeSkillLevel;
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        RPGUtils.streamMobsAndPlayers(player, 300)
                .filter(m -> !m.isBoss())
                .filter(RPGUtils.isValidTargetFilter(player))
                .forEach(
                        target -> super.giveBuff(player, target, playerData, activeSkillLevel)
                );

    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.roar, SoundEffect.effect(player.x, player.y).volume(2F).pitch(0.5F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(300, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {

            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                if (activeBuff.owner.ai.tree instanceof CompositeAINode) {
                    CompositeAINode<Mob> aiNode = (CompositeAINode<Mob>) activeBuff.owner.ai.tree;
                    RunningAwayAI<Mob> runFromMobsAI = new RunningAwayAI<>(1000, (m) -> m.isPlayer);
                    aiNode.addChildFirst(runFromMobsAI);
                    try {
                        Field childrenField = CompositeTypedAINode.class.getDeclaredField("runningNode");
                        childrenField.setAccessible(true);
                        childrenField.set(aiNode, runFromMobsAI);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    activeBuff.setModifier(BuffModifiers.INTIMIDATED, true);
                    activeBuff.setModifier(BuffModifiers.SPEED, 1F);
                }
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(153, 0, 204)).height(16.0F);
                }
            }

            @Override
            public void onRemoved(ActiveBuff activeBuff) {
                super.onRemoved(activeBuff);
                if (activeBuff.owner.ai.tree instanceof CompositeAINode) {
                    CompositeAINode<Mob> aiNode = (CompositeAINode<Mob>) activeBuff.owner.ai.tree;
                    try {
                        Field childrenField = CompositeTypedAINode.class.getDeclaredField("children");
                        childrenField.setAccessible(true);
                        ArrayList<? extends AINode<Mob>> children = (ArrayList<? extends AINode<Mob>>) childrenField.get(aiNode);
                        children.remove(0);
                        childrenField.set(aiNode, children);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };
    }

}