package rpgclasses.mobs.summons;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.entity.mobs.ai.behaviourTree.trees.PlayerFlyingFollowerValidTargetCollisionChaserAI;
import necesse.entity.mobs.ai.behaviourTree.util.FlyingAIMover;
import necesse.entity.mobs.itemAttacker.ItemAttackerMob;
import necesse.entity.mobs.summon.summonFollowingMob.attackingFollowingMob.AttackingFollowingMob;
import necesse.entity.particle.Particle;
import necesse.entity.trails.Trail;
import necesse.entity.trails.TrailVector;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.CollisionFilter;
import necesse.level.maps.Level;
import rpgclasses.buffs.IgnitedBuff;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

public class DancingFlameMob extends AttackingFollowingMob {
    public boolean isPurple = false;

    public DancingFlameMob() {
        super(50);

        this.moveAccuracy = 15;
        this.setSpeed(160.0F);
        this.setFriction(2.0F);
        this.collision = new Rectangle(-10, -10, 20, 20);
        this.hitBox = new Rectangle(-10, -10, 20, 20);
        this.selectBox = new Rectangle();
    }

    public Trail trail;
    public float moveAngle;
    private float toMove;

    protected ParticleTypeSwitcher spinningTypeSwitcher;

    public void setIsPurple(boolean isPurple) {
        this.isPurple = isPurple;
    }

    public void setPurple() {
        setIsPurple(true);
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addBoolean("isPurple", isPurple);
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        isPurple = load.getBoolean("isPurple");
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextBoolean(isPurple);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        isPurple = reader.getNextBoolean();
    }

    @Override
    public GameDamage getCollisionDamage(Mob target, boolean fromPacket, ServerClient packetSubmitter) {
        return summonDamage;
    }

    @Override
    public int getCollisionKnockback(Mob target) {
        return 15;
    }

    @Override
    public void handleCollisionHit(Mob target, GameDamage damage, int knockback) {
        Mob owner = this.getAttackOwner();
        if (owner != null && target != null) {
            target.isServerHit(damage, target.x - owner.x, target.y - owner.y, (float) knockback, this);

            IgnitedBuff.apply(getFollowingMob(), target, damage.damage * 0.2F, 5F, isPurple);

            this.collisionHitCooldowns.startCooldown(target);
            this.remove(0.0F, 0.0F, null, true);
        }
    }

    @Override
    public void init() {
        super.init();
        this.ai = new BehaviourTreeAI<>(this, new PlayerFlyingFollowerValidTargetCollisionChaserAI<DancingFlameMob>(192, null, 15, 500, 640, 64) {
            public boolean isValidTarget(DancingFlameMob mob, ItemAttackerMob owner, Mob target) {
                if (owner == null) {
                    return false;
                } else {
                    Object result = GameUtils.castRayFirstHit(new Line2D.Float(owner.x, owner.y, target.x, target.y), 100.0, (line) -> {
                        CollisionFilter collisionFilter = mob.modifyChasingCollisionFilter((new CollisionFilter()).projectileCollision(), target);
                        return mob.getLevel().collides(line, collisionFilter) ? new Object() : null;
                    });
                    return result == null;
                }
            }
        }, new FlyingAIMover());
        this.spinningTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.COSMETIC);
        if (this.isClient()) {
            this.trail = new Trail(this, this.getLevel(), isPurple ? new Color(102, 51, 204) : new Color(255, 51, 0), 20.0F, 200, 18.0F);
            this.trail.drawOnTop = true;
            this.trail.removeOnFadeOut = false;
            this.getLevel().entityManager.addTrail(this.trail);
        }

    }

    @Override
    public void tickMovement(float delta) {
        this.toMove += delta;

        while (this.toMove > 4.0F) {
            float oldX = this.x;
            float oldY = this.y;
            super.tickMovement(4.0F);
            this.toMove -= 4.0F;
            Point2D.Float d = GameMath.normalize(oldX - this.x, oldY - this.y);
            this.moveAngle = (float) Math.toDegrees(Math.atan2(d.y, d.x)) - 90.0F;
            if (this.trail != null) {
                float trailOffset = 5.0F;
                this.trail.addPoint(new TrailVector(this.x + d.x * trailOffset, this.y + d.y * trailOffset, -d.x, -d.y, this.trail.thickness, 0.0F));
            }
        }

    }

    @Override
    public void clientTick() {
        super.clientTick();
        this.getLevel().entityManager.addParticle(this.x + (float) (GameRandom.globalRandom.nextGaussian() * 4.0), this.y + (float) (GameRandom.globalRandom.nextGaussian() * 4.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(this.dx / 10.0F, this.dy / 10.0F).color(isPurple ? new Color(102, 51, 204) : new Color(255, 51, 0));

        this.refreshParticleLight();
    }

    public void refreshParticleLight() {
        Color color = isPurple ? new Color(102, 51, 204) : new Color(255, 51, 0);
        this.getLevel().lightManager.refreshParticleLightFloat(this.x, this.y, color, 0.75F);
    }

    @Override
    public void playDeathSound() {
        SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this).volume(0.5F));
    }

    @Override
    protected void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.trail != null) {
            this.trail.removeOnFadeOut = true;
        }
    }

}
