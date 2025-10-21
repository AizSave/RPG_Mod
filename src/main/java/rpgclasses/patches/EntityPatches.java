package rpgclasses.patches;

import necesse.engine.GlobalData;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.network.packet.PacketSpawnMob;
import necesse.engine.state.MainGame;
import necesse.engine.util.GameRandom;
import necesse.entity.Entity;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.hostile.HostileMob;
import necesse.gfx.gameTexture.GameSprite;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.placeableItem.MobSpawnItem;
import net.bytebuddy.asm.Advice;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGModifiers;

import java.awt.*;

public class EntityPatches {

    @ModMethodPatch(target = Entity.class, name = "resetUniqueID", arguments = {GameRandom.class})
    public static class resetUniqueID {
        @Advice.OnMethodExit
        static void onExit(@Advice.This Entity This) {
            if (This instanceof HostileMob) {
                MobData.initMob((HostileMob) This);
            }
        }
    }

    public boolean shouldDrawOnMap() {
        return false;
    }

    @ModMethodPatch(target = Entity.class, name = "shouldDrawOnMap", arguments = {})
    public static class shouldDrawOnMap {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Entity This, @Advice.Return(readOnly = false) boolean draw) {
            if (draw) return;
            if (!(This instanceof Mob)) return;

            if (GlobalData.getCurrentState() instanceof MainGame) {
                Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                if (client != null) {
                    draw = client.getPlayer().buffManager.getModifier(RPGModifiers.MOB_DETECTION_RANGE) >= client.getPlayer().getDistance((Mob) This);
                }
            }
        }
    }


    @ModMethodPatch(target = Entity.class, name = "drawOnMap", arguments = {TickManager.class, Client.class, int.class, int.class, double.class, Rectangle.class, boolean.class})
    public static class drawOnMap {

        @Advice.OnMethodExit
        static void onExit(
                @Advice.This Entity This,
                @Advice.Argument(0) TickManager tickManager,
                @Advice.Argument(1) Client client,
                @Advice.Argument(2) int x,
                @Advice.Argument(3) int y,
                @Advice.Argument(4) double tileScale,
                @Advice.Argument(5) Rectangle drawBounds,
                @Advice.Argument(6) boolean isMinimap
        ) {
            if (!(This instanceof Mob)) return;

            Mob mob = (Mob) This;

            MobSpawnItem spawnItem = mob.getSpawnItem();
            if (spawnItem == null) return;

            GameSprite gameSprite = spawnItem.getItemSprite(new InventoryItem(spawnItem), client.getPlayer());

            if (gameSprite == null) return;

            if (client.getPlayer().buffManager.getModifier(RPGModifiers.MOB_DETECTION_RANGE) < client.getPlayer().getDistance(mob))
                return;

            int drawX = x - gameSprite.width / 2;
            int drawY = y - gameSprite.height / 2;
            gameSprite.initDraw().draw(drawX, drawY);
        }
    }

}
