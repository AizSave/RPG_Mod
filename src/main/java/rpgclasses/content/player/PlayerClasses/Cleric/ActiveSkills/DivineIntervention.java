package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeDamageOverTimeTakenEvent;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGArea;
import rpgclasses.utils.RPGUtils;

import java.awt.*;

public class DivineIntervention extends SimpleBuffActiveSkill {

    public DivineIntervention(int levelMax, int requiredClassLevel) {
        super("divineintervention", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        Mob[] targetContainer = new Mob[1];
        float[] lowestHealthPercent = new float[1];
        int[] lowestHealth = new int[1];
        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(m -> {
                    float healthPercent = m.getHealthPercent();
                    int health = m.getMaxHealth();

                    boolean change;
                    if (targetContainer[0] == null) {
                        change = true;
                    } else {
                        float lHealthPercent = lowestHealthPercent[0];
                        if (targetContainer[0] == player) {
                            change = /*Ally*/ healthPercent < /*Player*/ lHealthPercent + 0.25F;
                        } else if (m == player) {
                            change = /*Player*/ healthPercent < /*Ally*/ lHealthPercent - 0.25F;
                        } else if (lHealthPercent != healthPercent) {
                            change = healthPercent < lHealthPercent;
                        } else {
                            change = health > lowestHealth[0];
                        }
                    }

                    if (change) {
                        lowestHealthPercent[0] = healthPercent;
                        lowestHealth[0] = health;
                        targetContainer[0] = m;
                    }
                });

        Mob target = targetContainer[0];
        int healing = target.getMaxHealth() - target.getHealth();
        if (healing > 0) {
            target.getLevel().entityManager.addLevelEvent(new MobHealthChangeEvent(target, healing));
        }
        giveBuff(player, target, playerData, activeSkillLevel);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(1F).pitch(2F));

        new AphAreaList(
                new RPGArea(200, new Color(255, 255, 0))
        ).setOnlyVision(false).executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 60 + activeSkillLevel * 12;
    }

    @Override
    public int getBaseCooldown() {
        return 40000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -4000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                buffEventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, MobBeforeDamageOverTimeTakenEvent::prevent);
            }

            @Override
            public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
                super.onBeforeHit(activeBuff, event);
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible()) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), GameRandom.globalRandom.nextInt(2) == 0 ? Particle.GType.COSMETIC : Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(GameRandom.globalRandom.getOneOf(new Color(255, 255, 0))).height(16.0F);
                }

                owner.setHealth(1);
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return activeSkillLevel * 1000;
    }
}
