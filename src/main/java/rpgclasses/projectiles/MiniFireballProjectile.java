package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.Projectile;
import necesse.entity.trails.Trail;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.LevelObjectHit;
import rpgclasses.buffs.IgnitedBuff;

import java.awt.*;
import java.util.List;

public class MiniFireballProjectile extends Projectile {
    public MiniFireballProjectile() {
    }

    public MiniFireballProjectile(Level level, Mob owner, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
        this.setLevel(level);
        this.setOwner(owner);
        this.x = x;
        this.y = y;
        this.setTarget(targetX, targetY);
        this.speed = speed;
        this.distance = distance;
        this.setDamage(damage);
        this.knockback = knockback;
    }

    public void init() {
        super.init();
        this.givesLight = true;
        this.height = 18.0F;
        this.trailOffset = 0F;
        this.setWidth(10.0F, true);
        this.piercing = 0;
        this.bouncing = 0;
    }

    public Color getParticleColor() {
        return new Color(255, 51, 0);
    }

    public Trail getTrail() {
        return new Trail(this, this.getLevel(), new Color(255, 51, 0), 20.0F, 200, this.getHeight());
    }

    @Override
    protected Color getWallHitColor() {
        return new Color(255, 51, 0);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
    }

    @Override
    protected void playHitSound(float x, float y) {
        SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this.x, this.y).volume(0.5F).pitch(1.5F));
    }

    @Override
    public void doHitLogic(Mob mob, LevelObjectHit object, float x, float y) {
        super.doHitLogic(mob, object, x, y);
        if (mob != null) {
            IgnitedBuff.apply(getAttackOwner(), mob, getDamage().damage * 0.2F, 5F, false);
            if (mob.isClient())
                SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this.x, this.y).volume(0.5F).pitch(1.5F));
        }
    }

}