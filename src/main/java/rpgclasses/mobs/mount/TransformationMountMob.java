package rpgclasses.mobs.mount;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketMobMovement;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.*;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.entity.particle.SmokePuffParticle;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;
import rpgclasses.RPGResources;
import rpgclasses.buffs.Interfaces.TransformationClassBuff;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TransformationMountMob extends Mob implements ActiveMountAbility {
    public int colorInt;

    public TransformationMountMob() {
        super(100);

        this.setSpeed(40.0F);
        this.setFriction(3.0F);
        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-14, -12, 28, 24);
        this.selectBox = new Rectangle(-14, -41, 28, 48);
        this.swimMaskMove = 16;
        this.swimMaskOffset = -2;
        this.swimSinkOffset = -4;
    }

    @Override
    protected void doMountedLogic() {
        super.doMountedLogic();
        TransformationClassBuff.apply(this);
    }

    @Override
    public void onActiveMountAbilityUpdate(PlayerMob playerMob, Packet packet) {
    }

    @Override
    public void onActiveMountAbilityStopped(PlayerMob playerMob) {
    }

    @Override
    public boolean canRunMountAbility(PlayerMob player, Packet content) {
        return true;
    }

    @Override
    public void onActiveMountAbilityStarted(PlayerMob playerMob, Packet packet) {
    }

    @Override
    public boolean tickActiveMountAbility(PlayerMob playerMob, boolean isRunningClient) {
        return false;
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt("colorInt", this.colorInt);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.colorInt = save.getInt("colorInt");
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(colorInt);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        colorInt = reader.getNextInt();
    }

    @Override
    public void init() {
        super.init();
        if (this.isClient()) {
            Level level = this.getLevel();
            level.entityManager.addParticle(new SmokePuffParticle(level, this.x, this.y + 5.0F, new Color(colorInt)), Particle.GType.IMPORTANT_COSMETIC);
        }
    }

    @Override
    public void remove(float knockbackX, float knockbackY, Attacker attacker, boolean isDeath) {
        Level level = this.getLevel();
        level.entityManager.addParticle(new SmokePuffParticle(level, this.x, this.y + 5.0F, new Color(colorInt)), Particle.GType.IMPORTANT_COSMETIC);
        super.remove(knockbackX, knockbackY, attacker, isDeath);
    }

    @Override
    public void tickSendSyncPackets() {
        if (this.isServer() && this.sendNextMovementPacket) {
            Mob rider = this.getRider();
            if (rider != null && !rider.isPlayer) {
                ++this.moveSent;
                this.getLevel().getServer().network.sendToClientsWithEntity(new PacketMobMovement(this, this.nextMovementPacketDirect), this);
                this.nextMovementPacketDirect = false;
            }

            this.movementUpdateTime = this.getTime();
            this.sendNextMovementPacket = false;
        }

        super.tickSendSyncPackets();
    }

    @Override
    public void tickCurrentMovement(float delta) {
        this.moveX = 0.0F;
        this.moveY = 0.0F;
        Mob mounted = this.getRider();
        if (this.isMounted() && mounted != null && mounted.isPlayer) {
            this.setDir(mounted.getDir());
            this.moveX = mounted.moveX;
            this.moveY = mounted.moveY;
        } else if (this.currentMovement != null) {
            this.hasArrivedAtTarget = this.currentMovement.tick(this);
            if (this.stopMoveWhenArrive && this.hasArrivedAtTarget) {
                this.stopMoving();
            }
        } else {
            this.hasArrivedAtTarget = true;
        }

    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.getRider() == null) {
            this.remove();
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
    }

    @Override
    public boolean isVisible() {
        return this.isMounted();
    }

    @Override
    public boolean canLevelInteract() {
        return this.isMounted();
    }

    @Override
    public boolean canPushMob(Mob other) {
        return this.isMounted();
    }

    @Override
    public boolean canBePushed(Mob other) {
        return this.isMounted();
    }

    @Override
    public boolean canTakeDamage() {
        return false;
    }

    @Override
    public boolean shouldDrawRider() {
        return false;
    }

    @Override
    public boolean forceFollowRiderLevelChange(Mob rider) {
        return true;
    }

    public
    @Override GameMessage getMountDismountError(Mob rider, InventoryItem item) {
        return new StaticMessage("");
    }

    @Override
    public Stream<ModifierValue<?>> getDefaultRiderModifiers() {
        return getRiderModifiers().stream();
    }

    public List<ModifierValue<?>> getRiderModifiers() {
        List<ModifierValue<?>> modifiers = new ArrayList<>();
        modifiers.add(new ModifierValue<>(BuffModifiers.INTIMIDATED, true));
        return modifiers;
    }

    @Override
    public boolean onMouseHover(GameCamera camera, PlayerMob perspective, boolean debug) {
        Mob rider = getRider();
        if (rider != null) {
            return rider.onMouseHover(camera, perspective, debug);
        } else {
            return false;
        }
    }

    public GameTexture getTexture() {
        return RPGResources.mobsTexture.get(getStringID());
    }

    public void clickRunServer(Level level, int x, int y, PlayerMob player) {
        clickRun(level, x, y, player);
        nextRunClick = player.getTime() + clickCooldown() - 50;
    }

    public void clickRunClient(Level level, int x, int y, PlayerMob player) {
        clickRun(level, x, y, player);
        nextRunClick = player.getTime() + clickCooldown();
    }

    public void clickRun(Level level, int x, int y, PlayerMob player) {
    }

    public void secondaryClickRunServer(Level level, int x, int y, PlayerMob player) {
        secondaryClickRun(level, x, y, player);
        nextRunSecondaryClick = player.getTime() + secondaryClickCooldown() - 50;
    }

    public void secondaryClickRunClient(Level level, int x, int y, PlayerMob player) {
        secondaryClickRun(level, x, y, player);
        nextRunSecondaryClick = player.getTime() + secondaryClickCooldown();
    }

    public void secondaryClickRun(Level level, int x, int y, PlayerMob player) {
    }

    public int clickCooldown() {
        return 0;
    }

    public int secondaryClickCooldown() {
        return clickCooldown();
    }

    public long nextRunClick = 0;
    public long nextRunSecondaryClick = 0;

    public boolean canRunClick(PlayerMob player) {
        return hasClick() && this.nextRunClick <= player.getTime();
    }

    public boolean hasClick() {
        return true;
    }

    public boolean canRunSecondaryClick(PlayerMob player) {
        return hasSecondaryClick() && this.nextRunSecondaryClick <= player.getTime();
    }

    public boolean hasSecondaryClick() {
        return false;
    }

    public boolean staminaBasedMountAbility() {
        return false;
    }

    public void onBeforeHit(PlayerMob player, MobBeforeHitEvent event) {
    }

    public void doResilienceGain(Mob attackedMob, float resilienceGain) {
        doResilienceGain(this.getRider(), attackedMob, resilienceGain);
    }

    public static void doResilienceGain(Mob attacker, Mob attackedMob, float resilienceGain) {
        if (attacker != null) {
            if (attackedMob.canGiveResilience(attacker)) {
                attacker.addResilience(resilienceGain);
            }
        }
    }
}
