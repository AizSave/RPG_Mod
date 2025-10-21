package rpgclasses.mobs.summons.damageable.necrotic;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.registries.MobRegistry;
import necesse.engine.seasons.GameSeasons;
import necesse.engine.seasons.SeasonalHat;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.entity.mobs.ai.behaviourTree.trees.PlayerFollowerCollisionChaserAI;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.armorItem.ArmorItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.data.PlayerData;

import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

public class NecromancerSkeletonMob extends NecroticFollowingMob {
    protected SeasonalHat hat;

    public NecromancerSkeletonMob() {
        super(50);
        this.setSpeed(60.0F);
        this.setFriction(3.0F);

        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-14, -12, 28, 24);
        this.selectBox = new Rectangle(-14, -41, 28, 48);
        this.swimMaskMove = 16;
        this.swimMaskOffset = -2;
        this.swimSinkOffset = -4;
    }

    @Override
    public void init() {
        super.init();

        this.ai = new BehaviourTreeAI<>(this, new PlayerFollowerCollisionChaserAI<>(576, this.summonDamage, 100, 500, Integer.MAX_VALUE, 64));
        this.hat = GameSeasons.getHat(new GameRandom(this.getUniqueID()));
    }

    @Override
    public void playHitSound() {
        float pitch = GameRandom.globalRandom.getOneOf(0.95F, 1.0F, 1.05F);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(this).volume(1.6F).pitch(pitch));
    }

    @Override
    public void playDeathSound() {
        float pitch = GameRandom.globalRandom.getOneOf(0.95F, 1.0F, 1.05F);
        SoundManager.playSound(GameResources.crackdeath, SoundEffect.effect(this).volume(0.8F).pitch(pitch));
    }

    @Override
    public void spawnDeathParticles(float knockbackX, float knockbackY) {
        for (int i = 0; i < 4; ++i) {
            this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), MobRegistry.Textures.ancientSkeleton.body, GameRandom.globalRandom.nextInt(5), 8, 32, this.x, this.y, 20.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
        }
    }

    @Override
    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 22 - 10;
        int drawY = camera.getDrawY(y) - 44 - 7;
        int dir = this.getDir();
        Point sprite = this.getAnimSprite(x, y, dir);
        drawY += this.getBobbing(x, y);
        drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
        MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
        HumanDrawOptions humanDrawOptions = (new HumanDrawOptions(level, MobRegistry.Textures.ancientSkeleton)).sprite(sprite).dir(dir).mask(swimMask).light(light);
        if (this.hat != null) {
            humanDrawOptions.hatTexture(this.hat.getDrawOptions(), ArmorItem.HairDrawMode.NO_HAIR);
        }

        final DrawOptions drawOptions = humanDrawOptions.pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                drawOptions.draw();
            }
        });
        this.addShadowDrawables(tileList, level, x, y, light, camera);
    }

    @Override
    public int getRockSpeed() {
        return 20;
    }


    @Override
    public Stream<ModifierValue<?>> getDefaultModifiers() {
        return Stream.of((new ModifierValue<>(BuffModifiers.FRICTION, 0.0F)).min(0.75F));
    }

    @Override
    public int getHealthStat(PlayerMob player, PlayerData playerData) {
        return (int) (5 * (playerData.getLevel() + playerData.getIntelligence(player)));
    }

    @Override
    public float getDamageStat(PlayerMob player, PlayerData playerData) {
        return playerData.getLevel() + playerData.getIntelligence(player);
    }

}
