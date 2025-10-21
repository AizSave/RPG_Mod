package rpgclasses.levelevents;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.levelEvent.explosionEvent.ExplosionEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.utils.RPGUtils;

import java.awt.*;

public class RPGExplosionLevelEvent extends ExplosionEvent implements Attacker {
    private int particleBuffer;
    protected ParticleTypeSwitcher explosionTypeSwitcher;
    public Color initialColor;
    public Color finalColor;

    public RPGExplosionLevelEvent() {
        this(0.0F, 0.0F, 50, new GameDamage(0), null, false, new Color(255, 225, 155), new Color(200, 25, 29));
    }

    public RPGExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner, boolean hitsOwner, Color initialColor, Color finalColor) {
        super(x, y, range, damage, false, 0, owner);
        this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
        this.targetRangeMod = 0.0F;
        this.hitsOwner = hitsOwner;
        this.initialColor = initialColor;
        this.finalColor = finalColor;
        this.destroysObjects = false;
        this.destroysTiles = false;
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(initialColor.getRGB());
        writer.putNextInt(finalColor.getRGB());
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        initialColor = new Color(reader.getNextInt(), true);
        finalColor = new Color(reader.getNextInt(), true);
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt("initialColor", initialColor.getRGB());
        save.addInt("finalColor", finalColor.getRGB());
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        initialColor = new Color(load.getInt("initialColor"), true);
        finalColor = new Color(load.getInt("finalColor"), true);
    }

    @Override
    protected void playExplosionEffects() {
        SoundManager.playSound(GameResources.explosionHeavy, SoundEffect.effect(this.x, this.y).volume(0.8F).pitch(1F));
        this.level.getClient().startCameraShake(this.x, this.y, 200, 40, 1F, 0.8F, true);
    }

    @Override
    public void spawnExplosionParticle(float x, float y, float dirX, float dirY, int lifeTime, float range) {
        if (this.particleBuffer < 10) {
            ++this.particleBuffer;
        } else {
            this.particleBuffer = 0;
            if (range <= Math.max(this.range * 0.65F, 25)) {
                float dx = dirX * GameRandom.globalRandom.getFloatBetween(0.3F, 0.4F) * this.range;
                float dy = dirY * GameRandom.globalRandom.getFloatBetween(0.3F, 0.4F) * this.range * 0.9F;
                this.getLevel().entityManager.addParticle(x, y, this.explosionTypeSwitcher.next()).sprite(GameResources.puffParticles.sprite(GameRandom.globalRandom.getIntBetween(0, 4), 0, 12)).sizeFades(70, 100).givesLight(180F, 1F).movesFriction(dx * 0.05F, dy * 0.05F, 0.8F).color((options, lifeTime1, timeAlive, lifePercent) -> {
                    float clampedLifePercent = Math.max(0.0F, Math.min(1.0F, lifePercent));
                    options.color(
                            new Color(
                                    (int) (initialColor.getRed() + (float) (finalColor.getRed() - initialColor.getRed()) * clampedLifePercent),
                                    (int) (initialColor.getGreen() + (float) (finalColor.getGreen() - initialColor.getGreen()) * clampedLifePercent),
                                    (int) (initialColor.getBlue() + (float) (finalColor.getBlue() - initialColor.getBlue()) * clampedLifePercent),
                                    (int) (initialColor.getAlpha() + (float) (finalColor.getAlpha() - initialColor.getAlpha()) * clampedLifePercent)
                            )
                    );
                }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
            }
        }

    }

    @Override
    protected boolean canHitMob(Mob target) {
        return RPGUtils.isValidTarget(ownerMob, target);
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        if (damage.damage > 0) {
            super.onMobWasHit(mob, distance);
        }
    }
}
