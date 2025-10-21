package rpgclasses.mobs.mount;

import necesse.engine.gameLoop.tickManager.TickManager;
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
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.List;

public class LichSkeletonMob extends TransformationMountMob implements ActiveMountAbility {
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
        Projectile projectile = new AncientBoneProjectile(player.x, player.y, x, y, new GameDamage(DamageTypeRegistry.MAGIC, playerData.getLevel() + playerData.getStrength(player) + 0.2F * playerData.getIntelligence(player)), player);
        projectile.setModifier(new ResilienceOnHitProjectileModifier(2));
        projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));

        player.getLevel().entityManager.projectiles.addHidden(projectile);
        player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
    }

    @Override
    public int clickCooldown() {
        return 1000;
    }
}
