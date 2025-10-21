package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills.Dash;

import necesse.engine.registries.BuffRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;

import java.awt.geom.Point2D;

public class LongDash extends ActiveSkill {
    public LongDash(int levelMax, int requiredClassLevel) {
        super("longdash", "#99ff99", levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);

        player.buffManager.addBuff(new ActiveBuff(BuffRegistry.FOW_ACTIVE, player, 0.15F, null), false);
        player.buffManager.forceUpdateBuffs();

        Point2D.Float dir = getDir(player);
        int strength = 150;

        float forceX = dir.x * strength;
        float forceY = dir.y * strength;
        if (Math.abs(player.dx) < Math.abs(forceX)) {
            player.dx = forceX;
        }

        if (Math.abs(player.dy) < Math.abs(forceY)) {
            player.dy = forceY;
        }

        if (player.isClient() && player.getLevel() != null) {
            for (int i = 0; i < 30; ++i) {
                player.getLevel().entityManager.addParticle(player.x + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + forceX / 10.0F, player.y + (float) GameRandom.globalRandom.nextGaussian() * 20.0F + forceY / 10.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(forceX * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, forceY * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(getColor()).height(18.0F).lifeTime(700);
            }
        }
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(player).volume(0.35F).pitch(1.7F));
    }

    @Override
    public int getBaseCooldown() {
        return 6000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return super.getCooldownModPerLevel() - 1000;
    }

    @Override
    public float consumedStamina(PlayerMob player) {
        return 0.25F;
    }
}
