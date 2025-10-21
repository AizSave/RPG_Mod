package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.EntityDrawable;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;
import rpgclasses.utils.RPGUtils;

import java.util.List;

public class Smite extends ActiveSkill {

    public Smite(int levelMax, int requiredClassLevel) {
        super("smite", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 8000;
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        Mob target = RPGUtils.findBestTarget(player, 300);

        if (target != null) {
            float damage = 4 * playerData.getLevel() + 2 * activeSkillLevel * (playerData.getIntelligence(player) + playerData.getGrace(player));
            if (MobData.isWeakToHoly(target, player)) damage *= 2;
            target.isServerHit(new GameDamage(RPGDamageType.HOLY, damage), player.x, player.y, 0, player);
            RPGBuffs.applyStun(target, 300);
        }
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.glyphTrapCharge, SoundEffect.effect(player.x, player.y).volume(1F).pitch(0.5F));

        Mob target = RPGUtils.findBestTarget(player, 300);

        if (target != null) {
            target.getLevel().entityManager.addParticle(new SmiteParticle(target.getLevel(), target.x, target.y, 300), Particle.GType.CRITICAL);
        }
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 10 + activeSkillLevel * 2;
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 300) ? null : "notarget";
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage", "constrained", "manausage"};
    }

    public static class SmiteParticle extends Particle {
        public SmiteParticle(Level level, float x, float y, long lifeTime) {
            super(level, x, y, lifeTime);
        }

        @Override
        public void clientTick() {
            super.clientTick();
            this.getLevel().lightManager.refreshParticleLightFloat(this.x, this.y, 0.13F, 1.0F);
        }

        @Override
        public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            if (!this.removed()) {
                GameTexture texture = RPGResources.PARTICLE_TEXTURES.smite;

                int drawX = camera.getDrawX(this.x) - texture.getWidth() / 2;
                int drawY = camera.getDrawY(this.y) - texture.getHeight();

                final TextureDrawOptions options = texture.initDraw().pos(drawX, drawY);
                list.add(new EntityDrawable(this) {
                    public void draw(TickManager tickManager) {
                        options.draw();
                    }
                });
            }
        }
    }

}