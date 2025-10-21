package rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills;

import aphorea.registry.AphBuffs;
import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.utils.RPGArea;
import rpgclasses.utils.RPGColors;

import java.awt.*;
import java.util.List;

public class BearTransformation extends SimpleTranformationActiveSkill {
    public BearTransformation(int levelMax, int requiredClassLevel) {
        super("beartransformation", "#6c3b2a", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 8000;
    }

    @Override
    public int castingDuration() {
        return 4000;
    }

    @Override
    public Class<? extends SkillTransformationMountMob> getMobClass() {
        return WolfMob.class;
    }

    public static class WolfMob extends SkillTransformationMountMob {
        public WolfMob() {
            super();

            this.setSpeed(20.0F);
            this.setKnockbackModifier(0.2F);
            this.moveAccuracy = 8;
            this.collision = new Rectangle(-10, -7, 20, 14);
            this.hitBox = new Rectangle(-20, -16, 40, 32);
            this.selectBox = new Rectangle(-20, -50, 40, 55);
            this.swimMaskMove = 32;
            this.swimMaskOffset = -55;
            this.swimSinkOffset = -8;

            this.prioritizeVerticalDir = false;
        }

        @Override
        public List<ModifierValue<?>> getRiderModifiers() {
            List<ModifierValue<?>> modifiers = super.getRiderModifiers();
            modifiers.add(new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 0.5F));
            return modifiers;
        }

        @Override
        public void spawnDeathParticles(float knockbackX, float knockbackY) {
            for (int i = 0; i < 4; ++i) {
                this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), MobRegistry.Textures.grizzlyBear, i, 16, 32, this.x, this.y, 20.0F, knockbackX, knockbackY), Particle.GType.IMPORTANT_COSMETIC);
            }
        }

        @Override
        protected void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 64;
            int drawY = camera.getDrawY(y) - 128 + 36;
            int dir = this.getDir();
            Point sprite = this.getAnimSprite(x, y, dir);
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
            final MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
            final DrawOptions options = MobRegistry.Textures.grizzlyBear.initDraw().sprite(sprite.x, sprite.y, 128).addMaskShader(swimMask).light(light).pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    swimMask.use();
                    options.draw();
                    swimMask.stop();
                }
            });
            this.addShadowDrawables(tileList, level, x, y, light, camera);
        }

        @Override
        protected TextureDrawOptions getShadowDrawOptions(Level level, int x, int y, GameLight light, GameCamera camera) {
            GameTexture shadowTexture = MobRegistry.Textures.grizzlyBear_shadow;
            int drawX = camera.getDrawX(x) - 64;
            int drawY = camera.getDrawY(y) - 128 + 36;
            drawY += this.getBobbing(x, y);
            return shadowTexture.initDraw().sprite(0, this.getDir(), 128).light(light).pos(drawX, drawY);
        }

        @Override
        public int getRockSpeed() {
            return 10;
        }

        @Override
        public int clickCooldown() {
            return 2000;
        }

        @Override
        public void clickRun(Level level, int x, int y, PlayerMob player) {
            super.clickRun(level, x, y, player);

            ActiveBuff ab = new ActiveBuff(AphBuffs.STOP, player, 300, null);
            player.buffManager.addBuff(ab, false);

            Color colorArea = RPGColors.dirt;
            if (player.isClient()) {
                Color debrisColor = player.getLevel().getTile(player.getTileX(), player.getTileY()).getDebrisColor(player.getLevel(), player.getTileX(), player.getTileY());
                if (debrisColor != null) colorArea = debrisColor;
            }

            PlayerData playerData = PlayerDataList.getPlayerData(player);

            int skillLevel = getActualSkillLevel();

            AphAreaList areaList = new AphAreaList(
                    new RPGArea(120, colorArea)
                            .addOnTargetDamaged(
                                    target -> doResilienceGain(target, 4)
                            )
                            .setDebuffArea(500 * skillLevel, AphBuffs.STUN.getStringID())
                            .setDamageArea(new GameDamage(DamageTypeRegistry.MELEE, 4 * playerData.getLevel() + 4 * playerData.getStrength(player) * skillLevel))
            );
            areaList.execute(player, false);
        }

        @Override
        public void clickRunClient(Level level, int x, int y, PlayerMob player) {
            super.clickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.punch, SoundEffect.effect(player.x, player.y).volume(2F).pitch(0.5F));
            player.getClient().startCameraShake(player.x, player.y, 300, 40, 3.0F, 3.0F, true);
        }

        @Override
        public boolean hasSecondaryClick() {
            return true;
        }

        @Override
        public int secondaryClickCooldown() {
            return 10000;
        }

        @Override
        public void secondaryClickRun(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRun(level, x, y, player);
            AphAreaList areaList = new AphAreaList(
                    new AphArea(100, new Color(51, 0, 255, 51))
                            .setDebuffArea(1000, "intimidationactiveskillbuff")
            ).setOnlyVision(false);
            areaList.execute(player, false);
        }

        @Override
        public void secondaryClickRunClient(Level level, int x, int y, PlayerMob player) {
            super.secondaryClickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.roar, SoundEffect.effect(this).volume(1F).pitch(0.5F));
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
}
