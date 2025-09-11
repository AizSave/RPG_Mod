package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

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
import rpgclasses.content.player.Logic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGColors;

public class Fury extends SimpleBuffActiveSkill {

    public Fury(int levelMax, int requiredClassLevel) {
        super("fury", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.roar, SoundEffect.effect(player.x, player.y).volume(1F).pitch(1.2F));
    }


    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                int level = getLevel(activeBuff);
                activeBuff.setModifier(BuffModifiers.MELEE_DAMAGE, level * 0.2F);
                activeBuff.setModifier(BuffModifiers.ATTACK_SPEED, level * 0.2F);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(RPGColors.red).height(16.0F);
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 6000;
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }
}
