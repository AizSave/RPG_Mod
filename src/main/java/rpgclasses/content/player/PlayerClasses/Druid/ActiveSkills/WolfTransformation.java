package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import aphorea.registry.AphBuffs;
import aphorea.utils.area.AphAreaList;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.network.Packet;
import necesse.engine.network.gameNetworkData.GNDItemMap;
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
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.RPGResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.mobs.mount.TransformationMountMob;
import rpgclasses.mobs.summons.passive.RangerWolfMob;
import rpgclasses.utils.RPGArea;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class WolfTransformation extends SimpleTranformationActiveSkill {
    public static Buff wolfBarkBuff;

    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(3),
            SkillParam.staticParam(60),
            SkillParam.damageParam(3),
            SkillParam.staticParam(3),
            SkillParam.staticParam(2),
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public WolfTransformation(int levelMax, int requiredClassLevel) {
        super("wolftransformation", "#E6D9CC", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 6000;
    }

    @Override
    public int castingDuration() {
        return (int) (params[0].value() * 1000);
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return WolfMob.class;
    }

    @Override
    public void registry() {
        super.registry();
        LevelEventRegistry.registerEvent(stringID + "chargelevelevent", WolfChargeLevelEvent.class);

        wolfBarkBuff = BuffRegistry.registerBuff("wolfbark", new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.INCOMING_DAMAGE_MOD, 1F + activeBuff.getGndData().getFloat("incomingDamage"));
            }
        });
    }

    public static class WolfMob extends SkillTransformationMountMob {
        public WolfMob() {
            super();

            this.setSpeed(params[1].value());
            this.setFriction(4.0F);
            this.setKnockbackModifier(0.5F);
            this.moveAccuracy = 8;
            this.collision = new Rectangle(-10, -7, 20, 14);
            this.hitBox = new Rectangle(-14, -12, 28, 24);
            this.selectBox = new Rectangle(-18, -24, 36, 36);
            this.swimMaskMove = 16;
            this.swimMaskOffset = 0;
            this.swimSinkOffset = -12;
        }

        @Override
        public void spawnDeathParticles(float knockbackX, float knockbackY) {
            for (int i = 0; i < 4; ++i) {
                this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), RangerWolfMob.texture, 12, i, 32, this.x, this.y, 20.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
            }
        }

        @Override
        protected void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 32;
            int drawY = camera.getDrawY(y) - 36;
            int dir = this.getDir();
            Point sprite = this.getAnimSprite(x, y, dir);
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
            final MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
            final DrawOptions body = RangerWolfMob.texture.initDraw().sprite(sprite.x, sprite.y, 64).addMaskShader(swimMask).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    swimMask.use();
                    body.draw();
                    swimMask.stop();
                }
            });
            TextureDrawOptions shadow = MobRegistry.Textures.snowWolf.shadow.initDraw().sprite(0, sprite.y, 64).light(light).pos(drawX, drawY);
            tileList.add((tm) -> shadow.draw());
        }

        @Override
        public int getRockSpeed() {
            return 7;
        }

        @Override
        public int clickCooldown() {
            return 1000;
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
            LevelEvent event = new WolfChargeLevelEvent(this, Item.getRandomAttackSeed(GameRandom.globalRandom), dirX, dirY, 80, 200, new GameDamage(DamageTypeRegistry.MELEE, params[2].value(playerData.getLevel(), skillLevel)));
            player.getLevel().entityManager.events.addHidden(event);
            player.getServer().network.sendToClientsWithEntity(new PacketLevelEvent(event), event);
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
                    long msToDeplete = 5000L;
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

        @Override
        public boolean hasSecondaryClick() {
            return true;
        }

        @Override
        public int secondaryClickCooldown() {
            return 500;
        }

        @Override
        public void secondaryClickRun(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRun(level, x, y, player);
            AphAreaList areaList = new AphAreaList(
                    new RPGArea(100, new Color(255, 0, 0, 51))
                            .addBuffGND(wolfBarkBuff.getStringID(), new GNDItemMap().setFloat("incomingDamage", params[5].value()))
                            .setDebuffArea((int) (params[4].value() * 1000), "provocationactiveskillbuff", wolfBarkBuff.getStringID())
            ).setOnlyVision(false);
            areaList.execute(player, false);
        }

        @Override
        public void secondaryClickRunClient(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunClient(level, x, y, player);
            SoundManager.playSound(RPGResources.SOUNDS.Bark, SoundEffect.effect(this).volume(0.8F).pitch(GameRandom.globalRandom.getFloatOffset(0.9F, 0.1F)));
        }
    }

    public static class WolfChargeLevelEvent extends MobDashLevelEvent {
        public WolfChargeLevelEvent() {
        }

        public WolfChargeLevelEvent(Mob owner, int seed, float dirX, float dirY, float distance, int animTime, GameDamage damage) {
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

                for (int i = 0; i < 30; ++i) {
                    this.level.entityManager.addParticle(this.owner.x + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + forceX / 5.0F, this.owner.y + (float) GameRandom.globalRandom.nextGaussian() * 20.0F + forceY / 5.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(forceX * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F, forceY * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F).color(GameRandom.globalRandom.getOneOf(new Color(204, 255, 255))).height(18.0F).lifeTime(700);
                }

                SoundManager.playSound(GameResources.roar, SoundEffect.effect(this.owner).volume(0.5F).pitch(2F));
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
                        target.isServerHit(this.damage, this.dirX, this.dirY, 75.0F, player);
                    }
                }

                TransformationMountMob.doResilienceGain(getRider(), target, params[3].value());

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

            float width = 20.0F;
            float frontOffset = 20.0F;
            float range = 40.0F;
            float rangeOffset = -20.0F;
            return new LineHitbox(this.owner.x + dir.x * rangeOffset + this.dirX * frontOffset, this.owner.y + dir.y * rangeOffset + this.dirY * frontOffset, dir.x, dir.y, range, width);
        }

        public Mob getRider() {
            return owner.getRider();
        }
    }

}
