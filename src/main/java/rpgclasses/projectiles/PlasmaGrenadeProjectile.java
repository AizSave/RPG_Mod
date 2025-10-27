package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.followingProjectile.FollowingProjectile;
import necesse.entity.trails.Trail;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.LevelObjectHit;
import rpgclasses.levelevents.RPGExplosionLevelEvent;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.List;

public class PlasmaGrenadeProjectile extends FollowingProjectile {
    public PlasmaGrenadeProjectile() {
    }

    public PlasmaGrenadeProjectile(Level level, Mob owner, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
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
        this.turnSpeed = 0.5F;
        this.givesLight = true;
        this.height = 18.0F;
        this.trailOffset = 0F;
        this.setWidth(1.0F, true);
        this.piercing = 0;
        this.bouncing = 0;
        this.doesImpactDamage = false;
    }

    public Color getParticleColor() {
        return new Color(0, 255, 255);
    }

    public Trail getTrail() {
        return new Trail(this, this.getLevel(), new Color(0, 255, 255), 2.0F, 500, this.getHeight());
    }

    @Override
    protected Color getWallHitColor() {
        return new Color(0, 255, 255);
    }

    @Override
    public void updateTarget() {
        if (this.traveledDistance > 50F) {
            target = RPGUtils.findBestTarget(getOwner(), 1000);
        }
    }

    @Override
    public float getTurnSpeed(int targetX, int targetY, float delta) {
        return super.getTurnSpeed(targetX, targetY, delta) * 0.002F * (traveledDistance - 50F);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
    }

    @Override
    public void doHitLogic(Mob mob, LevelObjectHit object, float x, float y) {
        super.doHitLogic(mob, object, x, y);
        getLevel().entityManager.events.add(new RPGExplosionLevelEvent(x, y, 50, getDamage(), getOwner(), false, new Color(0, 255, 255), new Color(0, 153, 153)));
    }

}
