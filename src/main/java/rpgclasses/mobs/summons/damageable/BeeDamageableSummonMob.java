package rpgclasses.mobs.summons.damageable;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills.QueenBeeTransformation;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.ai.CustomSummonCollisionChaserAI;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;
import java.util.List;

public class BeeDamageableSummonMob extends DamageableFollowingMob {
    public BeeDamageableSummonMob() {
        super(1);
        this.setSpeed(100.0F);
        this.setFriction(2.0F);
        this.setSwimSpeed(1.0F);
        this.collision = new Rectangle(-7, -5, 14, 10);
        this.hitBox = new Rectangle(-12, -14, 24, 24);
        this.selectBox = new Rectangle(-16, -28, 32, 34);
    }

    @Override
    public void init() {
        super.init();

        this.ai = new BehaviourTreeAI<>(this, new CustomSummonCollisionChaserAI<BeeDamageableSummonMob>(1024, this.summonDamage, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, 64,
                mob -> {
                    if (!(getFollowingMob() instanceof PlayerMob)) return false;
                    PlayerMob player = (PlayerMob) getFollowingMob();
                    return player.buffManager.hasBuff(RPGBuffs.AGGRESSIVE_BEES) || !player.isRiding() || (!(player.getMount() instanceof QueenBeeTransformation.QueenBeeMob));
                }
        ) {
            @Override
            public boolean attackTarget(BeeDamageableSummonMob mob, Mob target) {
                boolean hits = super.attackTarget(mob, target);
                if (hits) {
                    if (getFollowingMob() instanceof PlayerMob) {
                        PlayerMob player = (PlayerMob) getFollowingMob();
                        MagicPoisonBuff.apply(player, target, damage.damage * 0.2F, 10F);
                    }
                    mob.remove(0, 0, null, true);
                }
                return hits;
            }
        });
    }

    @Override
    public void playHitSound() {
        float pitch = GameRandom.globalRandom.getOneOf(0.95F, 1.0F, 1.05F);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(this).volume(1.6F).pitch(pitch));
    }

    @Override
    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 16;
        int drawY = camera.getDrawY(y) - 22;
        int dir = this.getDir();
        int animationTime = 1000;
        long time = level.getTime();
        time += (new GameRandom(this.getUniqueID())).nextInt(animationTime);
        Point sprite = this.getAnimSprite(x, y, dir);
        TextureDrawOptions shadow = MobRegistry.Textures.honeyBee.shadow.initDraw().sprite(0, dir, 32).light(light).pos(drawX, drawY);
        tileList.add((tm) -> shadow.draw());
        float bobbingFloat = GameUtils.getBobbing(time, animationTime);

        drawY -= 6;
        drawY = (int) ((float) drawY + bobbingFloat * 5.0F);
        final DrawOptions options = MobRegistry.Textures.honeyBee.body.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                options.draw();
            }
        });
    }

    @Override
    public Point getAnimSprite(int x, int y, int dir) {
        long time = this.getTime();
        time += (new GameRandom(this.getUniqueID())).nextInt(200);
        return new Point(GameUtils.getAnim(time, 2, 200), dir);
    }

    @Override
    public int getHealthStat(PlayerMob player, PlayerData playerData) {
        return 1;
    }

    @Override
    public float getDamageStat(PlayerMob player, PlayerData playerData) {
        return (playerData.getLevel() + playerData.getIntelligence(player));
    }

    @Override
    public float getHealthDecreasePerSecond() {
        return 0;
    }

    @Override
    public boolean canBePushed(Mob other) {
        return false;
    }

    @Override
    public boolean canPushMob(Mob other) {
        return false;
    }
}
