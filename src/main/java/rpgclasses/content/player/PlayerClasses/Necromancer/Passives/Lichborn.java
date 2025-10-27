package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.packet.PacketLifelineEvent;
import necesse.engine.network.packet.PacketMobMount;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.seasons.SeasonalHat;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.*;
import necesse.entity.mobs.ai.behaviourTree.event.AIEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.projectile.AncientBoneProjectile;
import necesse.entity.projectile.Projectile;
import necesse.entity.projectile.modifiers.ResilienceOnHitProjectileModifier;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.item.Item;
import necesse.inventory.item.armorItem.ArmorItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.SkillTransformationMountMob;

import java.awt.*;
import java.util.List;

public class Lichborn extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0),
            new SkillParam("120 - 6 x <skilllevel>"),
            SkillParam.staticParam(100).setDecimals(2, 0),
            SkillParam.damageParam(2),
            SkillParam.staticParam(2)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Lichborn(int levelMax, int requiredClassLevel) {
        super("lichborn", "#6600ff", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerlich"};
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {

            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                this.isVisible = false;
                eventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, (event) -> {
                    if (this.runLogic(activeBuff, event.getExpectedHealth())) {
                        event.prevent();
                    }
                });
            }

            @Override
            public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
                super.onBeforeHitCalculated(activeBuff, event);
                if (this.runLogic(activeBuff, event.getExpectedHealth())) {
                    event.prevent();
                }

            }

            private boolean runLogic(ActiveBuff activeBuff, int expectedHealth) {
                Level level = activeBuff.owner.getLevel();
                if (level.isServer() && expectedHealth <= 0) {
                    Mob mount = activeBuff.owner.getMount();
                    if (mount instanceof LichSkeletonMob || activeBuff.owner.buffManager.hasBuff(getSecondaryBuffStringID()))
                        return false;

                    int skillLevel = getLevel(activeBuff);
                    activeBuff.owner.setHealth((int) (activeBuff.owner.getMaxHealth() * params[0].value(skillLevel)));
                    giveDatalessSecondaryPassiveBuff(activeBuff.owner, params[1].value(skillLevel));
                    level.getServer().network.sendToClientsWithEntity(new PacketLifelineEvent(activeBuff.owner.getUniqueID()), activeBuff.owner);
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return new SecondaryPassiveBuff() {
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                Mob owner = activeBuff.owner;
                Level level = owner.getLevel();
                if (owner.isServer()) {
                    if (owner.isMounted()) {
                        return;
                    }

                    Mob mount = owner.getMount();
                    if (mount instanceof LichSkeletonMob) {
                        this.refreshDurationOnExistingSkeleton((LichSkeletonMob) mount, activeBuff.getDurationLeft());
                    } else {
                        this.spawnAndSetNewSkeleton(level, owner, activeBuff.getDurationLeft());
                    }
                }

                float damageAdd = params[2].value();
                new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 1F + damageAdd).min(1F + damageAdd).apply(activeBuff);

            }

            private void refreshDurationOnExistingSkeleton(LichSkeletonMob mount, int duration) {
                mount.removeAtTime = mount.getTime() + (long) duration;
            }

            private void spawnAndSetNewSkeleton(Level level, Mob target, int duration) {
                LichSkeletonMob lichSkeletonMob = (LichSkeletonMob) MobRegistry.getMob("lichskeletonmob", level);
                lichSkeletonMob.applyData(playerClass, Lichborn.this);
                lichSkeletonMob.removeAtTime = level.getTime() + (long) duration;
                lichSkeletonMob.setPos(target.x, target.y, true);
                lichSkeletonMob.dx = target.dx;
                lichSkeletonMob.dy = target.dy;
                target.mount(lichSkeletonMob, true, target.x, target.y, true);
                level.entityManager.mobs.add(lichSkeletonMob);
                level.getServer().network.sendToClientsWithEntity(new PacketMobMount(target.getUniqueID(), lichSkeletonMob.getUniqueID(), true, target.x, target.y), target);
            }
        };
    }

    @Override
    public void registry() {
        super.registry();
        MobRegistry.registerMob("lichskeletonmob", Lichborn.LichSkeletonMob.class, false);
    }

    public static class LichSkeletonMob extends SkillTransformationMountMob implements ActiveMountAbility {
        public long removeAtTime;
        protected SeasonalHat hat;

        public LichSkeletonMob() {
            super();
            this.setKnockbackModifier(0.4F);
        }

        @Override
        public void addSaveData(SaveData save) {
            super.addSaveData(save);
            save.addLong("removeAtTime", this.removeAtTime);
        }

        @Override
        public void applyLoadData(LoadData save) {
            super.applyLoadData(save);
            this.removeAtTime = save.getLong("removeAtTime");
        }

        @Override
        public void serverTick() {
            super.serverTick();
            Mob rider;
            if (this.getTime() <= this.removeAtTime && this.isMounted()) {
                rider = this.getRider();
                if (rider != null && !rider.buffManager.hasBuff("lichborn2passivebuff")) {
                    this.remove();
                }
            } else {
                rider = this.getRider();
                if (rider != null && !rider.isPlayer && rider.ai != null) {
                    rider.ai.blackboard.mover.stopMoving(rider);
                    rider.ai.blackboard.submitEvent("resetPathTime", new AIEvent());
                }

                this.remove();
            }
        }

        @Override
        public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
            if (this.isMounted()) {
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
        }

        @Override
        public int getRockSpeed() {
            return 20;
        }

        @Override
        public void clickRunClient(Level level, int x, int y, PlayerMob player) {
            super.clickRunClient(level, x, y, player);
            SoundManager.playSound(GameResources.swing2, SoundEffect.effect(LichSkeletonMob.this).volume(0.7F).pitch(1.2F));
        }

        @Override
        public void clickRunServer(Level level, int x, int y, PlayerMob player) {
            super.clickRunServer(level, x, y, player);
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            Projectile projectile = new AncientBoneProjectile(player.x, player.y, x, y, new GameDamage(DamageTypeRegistry.MAGIC, params[3].value(playerData.getLevel(), getActualSkillLevel())), player);
            projectile.setModifier(new ResilienceOnHitProjectileModifier(params[4].value()));
            projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));

            player.getLevel().entityManager.projectiles.addHidden(projectile);
            player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
        }

        @Override
        public int clickCooldown() {
            return 1000;
        }
    }
}
