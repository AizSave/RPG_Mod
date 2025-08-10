package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.MovedRectangle;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;

import java.awt.*;
import java.awt.geom.Point2D;

public class Teleport extends ActiveSkill {
    public Teleport(int levelMax, int requiredClassLevel) {
        super("teleport", "#6633cc", levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);

        int range = 200;
        Point2D.Float dir = getDir(player);

        int lastPosX = player.getX();
        int lastPosY = player.getY();

        int newPosX;
        int newPosY;
        while (true) {
            newPosX = player.getX() + (int) (dir.x * range);
            newPosY = player.getY() + (int) (dir.y * range);
            MovedRectangle moveRect = new MovedRectangle(player, player.getX(), player.getY(), newPosX, newPosY);
            if (!player.getLevel().collides(moveRect, player.getLevelCollisionFilter())) {
                break;
            }

            range -= 4;
            if (range <= 0.0F) {
                newPosX = player.getX();
                newPosY = player.getY();
                break;
            }
        }

        player.setPos((float) newPosX, (float) newPosY, true);
        if (player.getLevel().isClient()) {
            int i;
            if (lastPosX != newPosX || lastPosY != newPosY) {
                for (i = 0; i < 15; ++i) {
                    player.getLevel().entityManager.addParticle((float) lastPosX + (float) GameRandom.globalRandom.nextGaussian() * 10.0F + player.dx / 2.0F, (float) lastPosY + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + player.dy / 2.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(player.dx * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, player.dy * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(new Color(204, 153, 255)).height(18.0F).lifeTime(700);
                }
            }

            for (i = 0; i < 15; ++i) {
                player.getLevel().entityManager.addParticle(player.x + (float) GameRandom.globalRandom.nextGaussian() * 10.0F + player.dx / 2.0F, player.y + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + player.dy / 2.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(player.dx * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, player.dy * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(new Color(204, 153, 255)).height(18.0F).lifeTime(700);
            }
        }
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.teleport, SoundEffect.effect(player).volume(0.5F));
    }

    @Override
    public int getBaseCooldown() {
        return 8000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return super.getCooldownModPerLevel() - 1200;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 10;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

}
