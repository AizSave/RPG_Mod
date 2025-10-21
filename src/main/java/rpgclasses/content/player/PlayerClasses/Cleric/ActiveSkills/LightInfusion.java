package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.registry.AphModifiers;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;

public class LightInfusion extends SimpleBuffActiveSkill {

    public LightInfusion(int levelMax, int requiredClassLevel) {
        super("lightinfusion", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        RPGBuffs.applyStop(player, 300);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.glyphTrapCharge, SoundEffect.effect(player.x, player.y).volume(2F).pitch(0.5F));

        player.getLevel().entityManager.addParticle(new Smite.SmiteParticle(player.getLevel(), player.x, player.y, 300), Particle.GType.CRITICAL);
    }


    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                int level = getLevel(activeBuff);
                activeBuff.setModifier(AphModifiers.MAGIC_HEALING, level * 0.2F);
                activeBuff.setModifier(RPGModifiers.HOLY_DAMAGE, level * 0.2F);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(getColor()).height(16.0F);
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 8000 + 300;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 10 + activeSkillLevel * 2;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage", "manausage"};
    }

    @Override
    public int getBaseCooldown() {
        return 14000;
    }
}
