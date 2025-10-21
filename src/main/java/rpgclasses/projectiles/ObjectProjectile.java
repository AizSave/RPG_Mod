package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketChangeObject;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.entity.projectile.Projectile;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.EntityDrawable;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameSprite;
import necesse.inventory.InventoryItem;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

import java.util.List;

public class ObjectProjectile extends Projectile {

    GameObject gameObject;

    public ObjectProjectile() {
    }

    public ObjectProjectile(Level level, Mob owner, GameObject gameObject, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
        this.setLevel(level);
        this.setOwner(owner);
        this.x = x;
        this.y = y;
        this.setTarget(targetX, targetY);
        this.speed = speed;
        this.distance = distance;
        this.setDamage(damage);
        this.knockback = knockback;

        this.gameObject = gameObject;
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        gameObject = ObjectRegistry.getObject(reader.getNextInt());
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(gameObject.getID());
    }

    @Override
    public void init() {
        super.init();
        this.givesLight = false;
        this.height = 32.0F;
        this.heightBasedOnDistance = true;
        this.setWidth(20.0F, true);
    }

    @Override
    public void remove() {
        if (this.isServer()) {
            boolean placed = false;
            if (!getLevel().isProtected) {
                for (int i = 0; i < 4; i++) {
                    int tileX = (int) ((x / 32) - this.dx / 2 * i);
                    int tileY = (int) ((y / 32) - this.dy / 2 * i);
                    if (gameObject.canPlace(getLevel(), tileX, tileY, 2, true) == null) {
                        gameObject.placeObject(getLevel(), tileX, tileY, 2, true);
                        getServer().network.sendToAllClients(new PacketChangeObject(getLevel(), 0, tileX, tileY));
                        placed = true;
                        break;
                    }
                }
            }
            if (!placed) {
                getLevel().entityManager.pickups.add(
                        new ItemPickupEntity(getLevel(), new InventoryItem(gameObject.getObjectItem()), x, y, 0, 0)
                );
            }
        }
        super.remove();
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables orderableDrawables, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera gameCamera, PlayerMob player) {
        if (!this.removed()) {
            GameLight light = level.getLightLevel(this);
            int drawX = gameCamera.getDrawX(this.x);
            int drawY = (int) (gameCamera.getDrawY(this.y) - getHeight());

            GameSprite sprite = gameObject.getObjectItem().getWorldItemSprite(new InventoryItem(gameObject.getObjectItem()), player);
            int spriteWidth = sprite.width * 2;
            int spriteHeight = sprite.height * 2;

            final DrawOptions options = sprite.initDraw().size(spriteWidth, spriteHeight).light(light).pos(drawX - spriteWidth / 2, drawY - spriteHeight / 2);
            list.add(new EntityDrawable(this) {
                public void draw(TickManager tickManager) {
                    options.draw();
                }
            });
        }
    }
}
