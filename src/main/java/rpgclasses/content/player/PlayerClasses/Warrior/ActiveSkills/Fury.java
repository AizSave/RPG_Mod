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
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGColors;

public class Fury extends SimpleBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(6),
            new SkillParam("20 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

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
                float value = params[1].value(level);
                activeBuff.setModifier(BuffModifiers.MELEE_DAMAGE, value);
                activeBuff.setModifier(BuffModifiers.ATTACK_SPEED, value);
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
        return (int) (params[0].value() * 1000);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 20000;
    }
}
