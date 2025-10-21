package rpgclasses.mobs.summons.damageable;

import aphorea.utils.AphColors;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.List;

public class NecromancerTombMob extends DamageableFollowingMob {
    public static GameTexture texture;

    public static String prefixDataName = "rpgmod_summon_";
    public static String skillLevelDataName = prefixDataName + "skillLevel";
    public static String timeToSpawnDataName = prefixDataName + "timeToSpawn";

    public int skillLevel = 0;
    public int timeToSpawn = 0;

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        skillLevel = load.getInt(skillLevelDataName);
        timeToSpawn = load.getInt(timeToSpawnDataName);
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(skillLevelDataName, skillLevel);
        save.addInt(timeToSpawnDataName, timeToSpawn);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        skillLevel = reader.getNextInt();
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(skillLevel);
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public NecromancerTombMob() {
        super(50);
        this.setSpeed(0.0F);
        this.setFriction(Float.MAX_VALUE);
        this.setKnockbackModifier(0);

        this.collision = new Rectangle(-16, -16 - 12, 32, 32);
        this.hitBox = new Rectangle(-16, -16 - 12, 32, 32);
        this.selectBox = new Rectangle(-16, -16 - 12, 32, 32);
    }

    @Override
    public boolean canPushMob(Mob other) {
        return false;
    }

    @Override
    public boolean canBePushed(Mob other) {
        return false;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        timeToSpawn -= 50;
        if (timeToSpawn <= 0 && isFollowing()) {
            timeToSpawn = (10 - skillLevel) * 1000;

            PlayerMob player = (PlayerMob) this.getFollowingMob();
            PlayerData playerData = PlayerDataList.getPlayerData(player);

            DamageableFollowingMob mob = (DamageableFollowingMob) MobRegistry.getMob("necromancerskeleton", getLevel());
            player.serverFollowersManager.addFollower(getStringID(), mob, FollowPosition.WALK_CLOSE, null, 1, Integer.MAX_VALUE, null, true);

            mob.updateStats(player, playerData);

            player.getLevel().entityManager.addMob(mob, this.x, this.y);
        }
    }

    @Override
    public void playHitSound() {
        float pitch = GameRandom.globalRandom.getOneOf(0.95F, 1.0F, 1.05F);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(this).volume(1.6F).pitch(pitch));
    }

    @Override
    public void playDeathSound() {
        float pitch = GameRandom.globalRandom.getOneOf(0.95F, 1.0F, 1.05F);
        SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this).volume(0.8F).pitch(pitch));
    }

    @Override
    public void spawnDeathParticles(float knockbackX, float knockbackY) {
        ParticleTypeSwitcher particleTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.CRITICAL, Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC);

        for (int i = 0; i < 20; ++i) {
            int angle = (int) (360.0F + GameRandom.globalRandom.nextFloat() * 360.0F);
            float dx = (float) Math.sin(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
            float dy = (float) Math.cos(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
            this.getLevel().entityManager.addParticle(this, particleTypeSwitcher.next()).movesFriction(dx, dy, 0.8F).color(AphColors.stone).heightMoves(10.0F, 30.0F).lifeTime(500);
        }
    }

    @Override
    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 16;
        int drawY = camera.getDrawY(y) - 16 - 12;
        drawY += this.getBobbing(x, y);
        drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);

        final DrawOptions drawOptions = texture.initDraw().light(light).pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                drawOptions.draw();
            }
        });
        this.addShadowDrawables(tileList, level, x, y, light, camera);
    }

    @Override
    public Point getAnimSprite(int x, int y, int dir) {
        return new Point();
    }

    @Override
    public int getHealthStat(PlayerMob player, PlayerData playerData) {
        return 5 * playerData.getLevel();
    }

    @Override
    public float getDamageStat(PlayerMob player, PlayerData playerData) {
        return 0;
    }

}
