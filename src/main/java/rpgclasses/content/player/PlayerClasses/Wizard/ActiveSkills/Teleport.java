package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.MovedRectangle;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;

import java.awt.geom.Point2D;

public class Teleport extends CastActiveSkill {
    public static SkillParam[] params = new SkillParam[]{};

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(8, false);
    }

    public Teleport(int levelMax, int requiredClassLevel) {
        super("teleport", "#6633cc", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 500;
    }

    @Override
    public void castedRun(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRun(player, playerData, activeSkillLevel, seed);

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
                    player.getLevel().entityManager.addParticle((float) lastPosX + (float) GameRandom.globalRandom.nextGaussian() * 10.0F + player.dx / 2.0F, (float) lastPosY + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + player.dy / 2.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(player.dx * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, player.dy * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(getColor()).height(18.0F).lifeTime(700);
                }
            }

            for (i = 0; i < 15; ++i) {
                player.getLevel().entityManager.addParticle(player.x + (float) GameRandom.globalRandom.nextGaussian() * 10.0F + player.dx / 2.0F, player.y + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + player.dy / 2.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(player.dx * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, player.dy * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(getColor()).height(18.0F).lifeTime(700);
            }
        }
    }

    @Override
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);
        SoundManager.playSound(GameResources.teleport, SoundEffect.effect(player).volume(0.5F));
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 8000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return super.getCooldownModPerLevel() - 1200;
    }

}
