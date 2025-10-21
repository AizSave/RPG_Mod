package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.*;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.entity.projectile.bulletProjectile.SeedBulletProjectile;
import necesse.entity.projectile.modifiers.ResilienceOnHitProjectileModifier;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.SeedObjectItem;
import necesse.inventory.item.toolItem.projectileToolItem.gunProjectileToolItem.SeedGunProjectileToolItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.mobs.summons.damageable.DryadSaplingDamageableSummonMob;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TreantTransformation extends SimpleTranformationActiveSkill {
    public TreantTransformation(int levelMax, int requiredClassLevel) {
        super("treanttransformation", "#753b09", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dryadsapling"};
    }

    @Override
    public int getBaseCooldown() {
        return 10000;
    }

    @Override
    public int castingDuration() {
        return 4000;
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return TreantMob.class;
    }

    public static class TreantMob extends SkillTransformationMountMob {
        public TreantMob() {
            super();

            this.setSpeed(0.0F);
            this.setFriction(2.0F);
            this.setSwimSpeed(0.75F);
            this.setFriction(3.0F);
            this.setKnockbackModifier(0.0F);
            this.collision = new Rectangle(-27, -27, 54, 54);
            this.hitBox = new Rectangle(-32, -27, 64, 54);
            this.selectBox = new Rectangle(-43, -80, 86, 112);
            this.swimMaskMove = 28;
            this.swimMaskOffset = -75;
            this.swimSinkOffset = -4;
        }

        @Override
        public List<ModifierValue<?>> getRiderModifiers() {
            List<ModifierValue<?>> modifiers = super.getRiderModifiers();
            int skillLevel = getActualSkillLevel();
            modifiers.add(new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 0.5F - 0.05F * skillLevel));
            modifiers.add(new ModifierValue<>(BuffModifiers.TARGET_RANGE, -1F));
            modifiers.add(new ModifierValue<>(BuffModifiers.ARMOR, 0.5F));
            return modifiers;
        }

        @Override
        public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 64;
            int drawY = camera.getDrawY(y) - 112 + 20;
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);

            Point sprite;
            if (this.getSpeed() == 0) {
                sprite = new Point(0, 4);
            } else {
                sprite = this.getAnimSprite(x, y, this.getDir());
            }

            final MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
            int spriteRes = 128;
            final DrawOptions options = MobRegistry.Textures.dryadSentinel.initDraw().sprite(sprite.x, sprite.y, spriteRes).addMaskShader(swimMask).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    swimMask.use();
                    options.draw();
                    swimMask.stop();
                }
            });
            TextureDrawOptions shadow = MobRegistry.Textures.dryadSentinel_shadow.initDraw().sprite(sprite.x, sprite.y, spriteRes, spriteRes).light(light).pos(drawX, drawY);
            tileList.add((tm) -> shadow.draw());
        }

        @Override
        public int getRockSpeed() {
            return 17;
        }

        @Override
        public int clickCooldown() {
            return 200;
        }

        @Override
        public int secondaryClickCooldown() {
            return 1500;
        }

        public void spawnDeathParticles(float knockbackX, float knockbackY) {
            for (int i = 0; i < 8; ++i) {
                this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), MobRegistry.Textures.dryadSentinel, i + 12, 20, 32, this.x, this.y, 50.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
            }
        }

        @Override
        public void clickRunServer(Level level, int x, int y, PlayerMob player) {
            super.clickRunServer(level, x, y, player);

            float dx = x - player.x;
            float dy = y - player.y;

            float angle = (float) Math.atan2(dy, dx);

            float offset = (float) ((Math.random() * 2 - 1) * Math.toRadians(15));
            angle += offset;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float newX = player.x + (float) Math.cos(angle) * distance;
            float newY = player.y + (float) Math.sin(angle) * distance;

            PlayerData playerData = PlayerDataList.getPlayerData(player);
            int skillLevel = getActualSkillLevel();
            SeedBulletProjectile projectile = new SeedBulletProjectile(player.x, player.y, newX, newY, 150, 400, new GameDamage(DamageTypeRegistry.RANGED, 0.5F * playerData.getLevel() + playerData.getStrength(player) * skillLevel), 10, player);
            projectile.setModifier(new ResilienceOnHitProjectileModifier(1));
            projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));
            Item item = ItemRegistry.getItem(GameRandom.globalRandom.getOneOf(new ArrayList<>(SeedGunProjectileToolItem.SEED_AMMO_TYPES)));
            if (item instanceof SeedObjectItem) projectile.setSeedBulletVariant((SeedObjectItem) item);
            this.getLevel().entityManager.projectiles.addHidden(projectile);
            this.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
        }

        @Override
        public void clickRunClient(Level level, int x, int y, PlayerMob player) {
            super.clickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.pop, SoundEffect.effect(this).volume(0.5F).pitch(3.0F));
        }

        @Override
        public void secondaryClickRunServer(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunServer(level, x, y, player);
            DryadSaplingDamageableSummonMob mob = (DryadSaplingDamageableSummonMob) MobRegistry.getMob("dryadsaplingdamageablesummon", player.getLevel());
            player.serverFollowersManager.addFollower(getStringID() + "follower", mob, FollowPosition.WALK_CLOSE, null, 1, 100, null, true);
            mob.updateStats(player, PlayerDataList.getPlayerData(player));

            mob.getLevel().entityManager.addMob(mob, player.x, player.y);
        }

        @Override
        public void secondaryClickRunClient(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.dryadSentinelGrowl, SoundEffect.effect(this).volume(0.8F));
        }

        @Override
        public boolean hasSecondaryClick() {
            return true;
        }

        @Override
        public boolean staminaBasedMountAbility() {
            return true;
        }

        @Override
        public boolean canRunClick(PlayerMob player) {
            return this.getSpeed() > 0 && super.canRunClick(player);
        }

        @Override
        public boolean canRunSecondaryClick(PlayerMob player) {
            return this.getSpeed() > 0 && super.canRunSecondaryClick(player);
        }

        @Override
        public boolean canRunMountAbility(PlayerMob player, Packet content) {
            return (player.isServer() && !Settings.strictServerAuthority) || StaminaBuff.canStartStaminaUsage(player);
        }

        @Override
        protected void doMountedLogic() {
            super.doMountedLogic();
            if (this.getRider() != null)
                this.getRider().addBuff(new ActiveBuff(RPGBuffs.TARGET_RANGE_TO_100, this.getRider(), 3.0F, null), this.isServer());
        }

        @Override
        public void onActiveMountAbilityStarted(PlayerMob player, Packet content) {
            this.buffManager.addBuff(new ActiveBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, this, 1.0F, null), false);
            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.TARGET_RANGE_TO_100, player, 3.0F, null), false);
        }

        @Override
        public boolean tickActiveMountAbility(PlayerMob player, boolean isRunningClient) {
            ActiveBuff speedBuff = this.buffManager.getBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE);
            if (speedBuff != null) {
                speedBuff.setDurationLeftSeconds(1.0F);
            } else {
                this.buffManager.addBuff(new ActiveBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, this, 1.0F, null), false);
            }
            ActiveBuff targetRangeBuff = player.buffManager.getBuff(RPGBuffs.TARGET_RANGE_TO_100);
            if (targetRangeBuff != null) {
                targetRangeBuff.setDurationLeftSeconds(3.0F);
            } else {
                player.buffManager.addBuff(new ActiveBuff(RPGBuffs.TARGET_RANGE_TO_100, player, 3.0F, null), false);
            }

            if ((this.moveX != 0.0F || this.moveY != 0.0F) && (this.dx != 0.0F || this.dy != 0.0F)) {
                long msToDeplete = 10000L;
                float usage = 50.0F / (float) msToDeplete;
                if (!StaminaBuff.useStaminaAndGetValid(player, usage)) {
                    return false;
                }
            }

            return !isRunningClient || Control.TRINKET_ABILITY.isDown();
        }

        @Override
        public void onActiveMountAbilityStopped(PlayerMob playerMob) {
            this.buffManager.removeBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, false);
        }

        @Override
        public boolean canBePushed(Mob other) {
            return false;
        }
    }
}
