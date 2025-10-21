package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.Projectile;
import necesse.entity.trails.Trail;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.LevelObjectHit;
import rpgclasses.levelevents.IceExplosionLevelEvent;

import java.awt.*;
import java.util.List;

public class IceBallProjectile extends Projectile {
    public IceBallProjectile() {
    }

    public IceBallProjectile(Level level, Mob owner, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
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
        this.doesImpactDamage = false;
    }

    public Color getParticleColor() {
        return new Color(0, 204, 255);
    }

    public Trail getTrail() {
        return new Trail(this, this.getLevel(), new Color(0, 204, 255), 20.0F, 200, this.getHeight());
    }

    @Override
    protected Color getWallHitColor() {
        return new Color(0, 204, 255);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
    }

    @Override
    public void doHitLogic(Mob mob, LevelObjectHit object, float x, float y) {
        super.doHitLogic(mob, object, x, y);
        getLevel().entityManager.events.add(new IceExplosionLevelEvent(x, y, 150, getDamage(), getOwner(), true));
    }

}