package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import aphorea.registry.AphBuffs;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketLevelEvent;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.LevelEventRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.engine.util.LineHitbox;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobDashLevelEvent;
import necesse.entity.mobs.*;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.RPGResources;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.registry.RPGModifiers;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class RatTransformation extends SimpleTranformationActiveSkill {
    public RatTransformation(int levelMax, int requiredClassLevel) {
        super("rattransformation", "#6c6e6b", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dodgechance"};
    }

    @Override
    public int getBaseCooldown() {
        return 2000;
    }

    @Override
    public int castingDuration() {
        return 2000;
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return RatMob.class;
    }

    public static class RatMob extends SkillTransformationMountMob {
        public RatMob() {
            super();

            this.setSpeed(25.0F);
            this.setFriction(3.0F);
            this.collision = new Rectangle(-10, -7, 20, 14);
            this.hitBox = new Rectangle(-12, -14, 24, 24);
            this.selectBox = new Rectangle(-13, -18, 26, 28);
            this.swimMaskMove = 6;
            this.swimMaskOffset = 30;
            this.swimSinkOffset = 0;
        }

        @Override
        public List<ModifierValue<?>> getRiderModifiers() {
            List<ModifierValue<?>> modifiers = super.getRiderModifiers();
            modifiers.add(new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 10F).min(10F));
            modifiers.add(new ModifierValue<>(RPGModifiers.DODGE_CHANCE, 0.8F).min(0.8F));
            modifiers.add(new ModifierValue<>(BuffModifiers.TARGET_RANGE, -0.5F));
            return modifiers;
        }

        @Override
        public void spawnDeathParticles(float knockbackX, float knockbackY) {
            for (int i = 0; i < 4; ++i) {
                this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), MobRegistry.Textures.mouse.body, 12, i, 16, this.x, this.y, 20.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
            }

        }

        @Override
        public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 16;
            int drawY = camera.getDrawY(y) - 22;
            int dir = this.getDir();
            Point sprite = this.getAnimSprite(x, y, dir);
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
            boolean mirror = (dir == 0 || dir == 2) && this.moveX < 0.0F;
            final MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
            final DrawOptions options = MobRegistry.Textures.mouse.body.initDraw().sprite(sprite.x, sprite.y, 32).addMaskShader(swimMask).mirror(mirror, false).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    swimMask.use();
                    options.draw();
                    swimMask.stop();
                }
            });
            TextureDrawOptions shadow = MobRegistry.Textures.mouse.shadow.initDraw().sprite(0, dir, 32).mirror(mirror, false).light(light).pos(drawX, drawY);
            tileList.add((tm) -> {
                shadow.draw();
            });
        }

        @Override
        public int getRockSpeed() {
            return 7;
        }

        @Override
        public int clickCooldown() {
            return 2000;
        }

        @Override
        public void clickRunServer(Level level, int x, int y, PlayerMob player) {
            super.clickRunServer(level, x, y, player);
            PlayerData playerData = PlayerDataList.getPlayerData(player);

            float dx = x - player.getX();
            float dy = y - player.getY();

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float dirX = dx / length;
            float dirY = dy / length;

            int skillLevel = getActualSkillLevel();
            LevelEvent event = new RatChargeLevelEvent(this, Item.getRandomAttackSeed(GameRandom.globalRandom), dirX, dirY, 20, 100, new GameDamage(DamageTypeRegistry.MELEE, playerData.getLevel() + 0.5F * playerData.getStrength(player) * skillLevel + 0.5F * playerData.getIntelligence(player) * skillLevel));
            player.getLevel().entityManager.events.add(event);
            player.getServer().network.sendToClientsWithEntity(new PacketLevelEvent(event), event);
        }

        @Override
        public void playDeathSound() {
            SoundManager.playSound(RPGResources.SOUNDS.Rat, SoundEffect.effect(this).volume(1F));
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
            if (!this.inLiquid()) {
                this.buffManager.addBuff(new ActiveBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, this, 1.0F, null), false);
            }
        }

        @Override
        public boolean tickActiveMountAbility(PlayerMob player, boolean isRunningClient) {
            if (this.inLiquid()) {
                this.buffManager.removeBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, false);
            } else {
                ActiveBuff speedBuff = this.buffManager.getBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE);
                if (speedBuff != null) {
                    speedBuff.setDurationLeftSeconds(1.0F);
                } else {
                    this.buffManager.addBuff(new ActiveBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, this, 1.0F, null), false);
                }

                if ((this.moveX != 0.0F || this.moveY != 0.0F) && (this.dx != 0.0F || this.dy != 0.0F)) {
                    long msToDeplete = 3000L;
                    float usage = 50.0F / (float) msToDeplete;
                    if (!StaminaBuff.useStaminaAndGetValid(player, usage)) {
                        return false;
                    }
                }
            }

            return !isRunningClient || Control.TRINKET_ABILITY.isDown();
        }

        @Override
        public void onActiveMountAbilityStopped(PlayerMob playerMob) {
            this.buffManager.removeBuff(BuffRegistry.LEATHER_DASHERS_ACTIVE, false);
        }
    }

    @Override
    public void registry() {
        super.registry();
        LevelEventRegistry.registerEvent(stringID + "chargelevelevent", RatChargeLevelEvent.class);
    }

    public static class RatChargeLevelEvent extends MobDashLevelEvent {
        public RatChargeLevelEvent() {
        }

        public RatChargeLevelEvent(Mob owner, int seed, float dirX, float dirY, float distance, int animTime, GameDamage damage) {
            super(owner, seed, dirX, dirY, distance, animTime, damage);
        }

        public PlayerMob getPlayer() {
            Mob rider = owner.getRider();
            return rider instanceof PlayerMob ? (PlayerMob) rider : null;
        }

        @Override
        public void init() {
            super.init();
            if (this.level != null && this.level.isClient() && this.owner != null) {
                float forceMod = Math.min((float) this.animTime / 700.0F, 1.0F);
                float forceX = this.dirX * this.distance * forceMod;
                float forceY = this.dirY * this.distance * forceMod;

                for (int i = 0; i < 4; ++i) {
                    this.level.entityManager.addParticle(this.owner.x + (float) GameRandom.globalRandom.nextGaussian() * 4.0F + forceX / 5.0F, this.owner.y + (float) GameRandom.globalRandom.nextGaussian() * 4.0F + forceY / 5.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(forceX * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F, forceY * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F).color(GameRandom.globalRandom.getOneOf(new Color(204, 255, 255))).height(18.0F).lifeTime(700);
                }

                SoundManager.playSound(RPGResources.SOUNDS.Rat, SoundEffect.effect(this.owner).volume(0.6F).pitch(GameRandom.globalRandom.getFloatOffset(0.95F, 0.05F)));
            }

            if (this.owner != null) {
                Mob rider = getRider();
                if (rider != null) {
                    rider.buffManager.addBuff(new ActiveBuff(BuffRegistry.INVULNERABLE_ACTIVE, this.owner, this.animTime, null), false);
                }
                this.owner.addBuff(new ActiveBuff(AphBuffs.SABER_DASH_ACTIVE, this.owner, this.animTime, null), false);
            }

        }

        @Override
        public boolean canHit(Mob mob) {
            PlayerMob player = getPlayer();
            return player != null && mob.canBeTargeted(player, player.getNetworkClient()) && this.hitCooldowns.canHit(mob);
        }

        @Override
        public void serverHit(Mob target, Packet content, boolean clientSubmitted) {
            if (clientSubmitted || this.hitCooldowns.canHit(target)) {
                if (this.damage != null) {
                    PlayerMob player = getPlayer();
                    if (player != null) {
                        target.isServerHit(this.damage, this.dirX, this.dirY, 10.0F, player);
                        MagicPoisonBuff.apply(player, target, this.damage.damage, 10F);
                    }
                }

                this.hitCooldowns.startCooldown(target);
            }
        }

        @Override
        public Shape getHitBox() {
            Point2D.Float dir;
            if (this.owner.getDir() == 3) {
                dir = GameMath.getPerpendicularDir(-this.dirX, -this.dirY);
            } else {
                dir = GameMath.getPerpendicularDir(this.dirX, this.dirY);
            }

            float width = 10.0F;
            float frontOffset = 10.0F;
            float range = 20.0F;
            float rangeOffset = -10.0F;
            return new LineHitbox(this.owner.x + dir.x * rangeOffset + this.dirX * frontOffset, this.owner.y + dir.y * rangeOffset + this.dirY * frontOffset, dir.x, dir.y, range, width);
        }

        public Mob getRider() {
            return owner.getRider();
        }
    }

}
