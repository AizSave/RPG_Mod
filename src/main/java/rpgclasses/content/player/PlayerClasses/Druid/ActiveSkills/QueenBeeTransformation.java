package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import aphorea.projectiles.toolitem.HoneyProjectile;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.*;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.mobs.buffs.staticBuffs.ShownCooldownBuff;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.entity.projectile.Projectile;
import necesse.entity.projectile.modifiers.ResilienceOnHitProjectileModifier;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.mobs.summons.damageable.BeeDamageableSummonMob;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;
import java.util.List;

public class QueenBeeTransformation extends SimpleTranformationActiveSkill {
    public static Buff queenBeeWasHitBuff;

    public QueenBeeTransformation(int levelMax, int requiredClassLevel) {
        super("queenbeetransformation", "#a98307", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"bee"};
    }

    @Override
    public int getBaseCooldown() {
        return 4000;
    }

    @Override
    public int castingDuration() {
        return 2000;
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return QueenBeeMob.class;
    }

    @Override
    public void registry() {
        super.registry();
        queenBeeWasHitBuff = BuffRegistry.registerBuff("queenbeewashit", new ShownCooldownBuff());
    }

    public static class QueenBeeMob extends SkillTransformationMountMob {
        public QueenBeeMob() {
            super();

            this.setSpeed(30.0F);
            this.setFriction(2.0F);
            this.setSwimSpeed(1.0F);
            this.collision = new Rectangle(-7, -5, 14, 10);
            this.hitBox = new Rectangle(-12, -14, 24, 24);
            this.selectBox = new Rectangle(-16, -28, 32, 34);
        }

        public int getFlyingHeight() {
            return 20;
        }

        public boolean canPushMob(Mob other) {
            return false;
        }

        public boolean canBePushed(Mob other) {
            return false;
        }

        @Override
        public List<ModifierValue<?>> getRiderModifiers() {
            List<ModifierValue<?>> modifiers = super.getRiderModifiers();
            modifiers.add(new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 10F).min(10F));
            modifiers.add(new ModifierValue<>(BuffModifiers.TARGET_RANGE, -0.5F));
            return modifiers;
        }

        @Override
        protected void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 16;
            int drawY = camera.getDrawY(y) - 22;
            int dir = this.getDir();
            int animationTime = 1000;
            long time = level.getTime();
            time += (new GameRandom(this.getUniqueID())).nextInt(animationTime);
            Point sprite = this.getAnimSprite(x, y, dir);
            TextureDrawOptions shadow = MobRegistry.Textures.queenBee.shadow.initDraw().sprite(0, dir, 32).light(light).pos(drawX, drawY);
            tileList.add((tm) -> shadow.draw());
            float bobbingFloat = GameUtils.getBobbing(time, animationTime);
            drawY -= 10;
            drawY = (int) ((float) drawY + bobbingFloat * 5.0F);
            final DrawOptions options = MobRegistry.Textures.queenBee.body.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    options.draw();
                }
            });
        }

        public Point getAnimSprite(int x, int y, int dir) {
            long time = this.getTime();
            time += (new GameRandom(this.getUniqueID())).nextInt(200);
            return new Point(GameUtils.getAnim(time, 2, 200), dir);
        }

        @Override
        public int clickCooldown() {
            return 300;
        }

        @Override
        public int secondaryClickCooldown() {
            return 1000;
        }

        @Override
        public void clickRunServer(Level level, int x, int y, PlayerMob player) {
            super.clickRunServer(level, x, y, player);

            PlayerData playerData = PlayerDataList.getPlayerData(player);
            int skillLevel = getActualSkillLevel();
            Projectile projectile = new HoneyProjectile(level, player, player.x, player.y, x, y, 100, 200, new GameDamage(DamageTypeRegistry.RANGED, 2 * playerData.getLevel() + playerData.getStrength(player) * skillLevel), 20);
            projectile.setModifier(new ResilienceOnHitProjectileModifier(2));
            projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));

            this.getLevel().entityManager.projectiles.addHidden(projectile);
            this.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
        }

        @Override
        public void clickRunClient(Level level, int x, int y, PlayerMob player) {
            super.clickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.spit, SoundEffect.effect(this).volume(1F).pitch(GameRandom.globalRandom.getFloatBetween(1F, 1.2F)));
        }

        @Override
        public void secondaryClickRunServer(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunServer(level, x, y, player);
            summonBee(player);
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

        @Override
        public boolean staminaBasedMountAbility() {
            return true;
        }

        @Override
        public boolean canRunMountAbility(PlayerMob player, Packet content) {
            return (player.isServer() && !Settings.strictServerAuthority) || StaminaBuff.canStartStaminaUsage(player);
        }

        @Override
        public void onActiveMountAbilityStarted(PlayerMob player, Packet content) {
            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.AGGRESSIVE_BEES, player, 1.0F, null), false);
        }

        @Override
        public boolean tickActiveMountAbility(PlayerMob player, boolean isRunningClient) {
            ActiveBuff buff = player.buffManager.getBuff(RPGBuffs.AGGRESSIVE_BEES);
            if (buff != null) {
                buff.setDurationLeftSeconds(1.0F);
            } else {
                player.buffManager.addBuff(new ActiveBuff(RPGBuffs.AGGRESSIVE_BEES, player, 1.0F, null), false);
            }

            long msToDeplete = 20000L;
            float usage = 50.0F / (float) msToDeplete;
            if (!StaminaBuff.useStaminaAndGetValid(player, usage)) {
                return false;
            }

            return !isRunningClient || Control.TRINKET_ABILITY.isDown();
        }

        @Override
        public void onActiveMountAbilityStopped(PlayerMob player) {
            player.buffManager.removeBuff(RPGBuffs.AGGRESSIVE_BEES, false);
        }

        @Override
        public void onBeforeHit(PlayerMob player, MobBeforeHitEvent event) {
            super.onBeforeHit(player, event);
            if (!player.buffManager.hasBuff(queenBeeWasHitBuff)) {
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;

                if (player.isServer()) {
                    player.buffManager.addBuff(new ActiveBuff(queenBeeWasHitBuff, player, 60F, null), true);
                    int skillLevel = getActualSkillLevel();
                    int amount = (int) (2 * skillLevel - player.serverFollowersManager.getFollowerCount(getStringID() + "follower"));
                    for (int i = 0; i < amount; i++) {
                        summonBee(player);
                    }
                }
            }
        }

        public void summonBee(PlayerMob player) {
            BeeDamageableSummonMob mob = (BeeDamageableSummonMob) MobRegistry.getMob("beedamageablesummon", player.getLevel());
            int skillLevel = getActualSkillLevel();
            player.serverFollowersManager.addFollower(getStringID() + "follower", mob, FollowPosition.WALK_CLOSE, null, 1, 2 * skillLevel, null, true);
            mob.updateStats(player, PlayerDataList.getPlayerData(player));

            mob.getLevel().entityManager.addMob(mob, player.x + GameRandom.globalRandom.getFloatOffset(0, 32), player.y + GameRandom.globalRandom.getFloatOffset(0, 32));
        }
    }
}
