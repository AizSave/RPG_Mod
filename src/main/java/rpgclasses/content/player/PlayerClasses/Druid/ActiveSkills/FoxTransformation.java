package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.entity.projectile.Projectile;
import necesse.entity.projectile.modifiers.ResilienceOnHitProjectileModifier;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.mobs.mount.SkillTransformationMountSimpleAbilityMob;
import rpgclasses.mobs.summons.DancingFlameMob;
import rpgclasses.projectiles.MiniFireballProjectile;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class FoxTransformation extends SimpleTranformationActiveSkill {
    public FoxTransformation(int levelMax, int requiredClassLevel) {
        super("foxtransformation", "#ff3300", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dancingflame"};
    }

    @Override
    public int getBaseCooldown() {
        return 4000;
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return FoxMob.class;
    }

    @Override
    public boolean initMobTexture() {
        return true;
    }

    public static class FoxMob extends SkillTransformationMountSimpleAbilityMob {
        public FoxMob() {
            super();

            this.setSpeed(50.0F);
            this.collision = new Rectangle(-10, -7, 20, 14);
            this.hitBox = new Rectangle(-14, -12, 28, 24);
            this.selectBox = new Rectangle(-18, -24, 36, 36);
        }

        @Override
        public List<ModifierValue<?>> getRiderModifiers() {
            List<ModifierValue<?>> modifiers = super.getRiderModifiers();
            modifiers.add(new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 1.5F));
            return modifiers;
        }

        @Override
        public void spawnDeathParticles(float knockbackX, float knockbackY) {
            GameTexture texture = getTexture();
            for (int i = 0; i < 3; ++i) {
                this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), texture, GameRandom.globalRandom.nextInt(5), 8, 32, this.x, this.y, 20.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
            }
        }

        @Override
        protected void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(this.getTileX(), this.getTileY());
            int drawX = camera.getDrawX(x) - 32;
            int drawY = camera.getDrawY(y) - 43;
            int dir = this.getDir();
            Point sprite = this.getAnimSprite(x, y, dir);
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(this.getTileX(), this.getTileY()).getMobSinkingAmount(this);
            final DrawOptions drawOptions = getTexture().initDraw().sprite(sprite.x, sprite.y, 64).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    drawOptions.draw();
                }
            });
            this.addShadowDrawables(tileList, level, x, y, light, camera);
        }

        @Override
        public int getRockSpeed() {
            return 15;
        }

        @Override
        public void runMountAbility(PlayerMob player) {
            this.buffManager.addBuff(new ActiveBuff(BuffRegistry.FOW_ACTIVE, this, 0.15F, null), false);
            this.buffManager.forceUpdateBuffs();

            Point2D.Float dir = Skill.getDir(this);
            int strength = 120;
            float forceX = dir.x * strength;
            float forceY = dir.y * strength;

            if (Math.abs(this.dx) < Math.abs(forceX)) {
                this.dx = forceX;
            }

            if (Math.abs(this.dy) < Math.abs(forceY)) {
                this.dy = forceY;
            }

            if (this.isClient()) {
                for (int i = 0; i < 15; ++i) {
                    this.getLevel().entityManager.addParticle(this.x + (float) GameRandom.globalRandom.nextGaussian() * 12.0F + forceX / 10.0F, this.y + (float) GameRandom.globalRandom.nextGaussian() * 12.0F + forceY / 10.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(forceX * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F, forceY * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 10.0F).color(new Color(255, 204, 204)).height(18.0F).lifeTime(700);
                }
                SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(this).volume(0.35F).pitch(1.7F));
            }


        }

        public GameDamage getDamage(PlayerMob player) {
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            int skillLevel = getActualSkillLevel();
            return new GameDamage(DamageTypeRegistry.MAGIC, playerData.getLevel() + 0.5F * playerData.getIntelligence(player) * skillLevel);
        }

        @Override
        public int mountAbilityCooldown() {
            return 2000;
        }

        @Override
        public int clickCooldown() {
            return 500;
        }

        @Override
        public int secondaryClickCooldown() {
            return 3000;
        }

        @Override
        public void clickRunServer(Level level, int x, int y, PlayerMob player) {
            super.clickRunServer(level, x, y, player);

            Projectile projectile = new MiniFireballProjectile(level, player, player.x, player.y, x, y, 200, 600, getDamage(player), 20);
            projectile.setModifier(new ResilienceOnHitProjectileModifier(2));
            projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));

            this.getLevel().entityManager.projectiles.addHidden(projectile);
            this.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
        }

        @Override
        public void clickRunClient(Level level, int x, int y, PlayerMob player) {
            super.clickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.spit, SoundEffect.effect(this).volume(0.6F).pitch(GameRandom.globalRandom.getFloatBetween(1.4F, 1.5F)));
            SoundManager.playSound(GameResources.magicbolt4, SoundEffect.effect(this).volume(0.3F).pitch(GameRandom.globalRandom.getFloatBetween(1.4F, 1.5F)));
        }

        @Override
        public void secondaryClickRunServer(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunServer(level, x, y, player);
            DancingFlameMob mob = (DancingFlameMob) MobRegistry.getMob("dancingflame", player.getLevel());
            int skillLevel = getActualSkillLevel();
            player.serverFollowersManager.addFollower(getStringID() + "follower", mob, FollowPosition.FLYING_CIRCLE_FAST, null, 1, 2 + skillLevel, null, true);
            mob.updateDamage(getDamage(player));
            mob.getLevel().entityManager.addMob(mob, player.x, player.y);
        }

        @Override
        public void secondaryClickRunClient(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.magicbolt4, SoundEffect.effect(this).volume(0.5F).pitch(GameRandom.globalRandom.getFloatBetween(1.4F, 1.5F)));
        }

        @Override
        public boolean hasSecondaryClick() {
            return true;
        }
    }
}
