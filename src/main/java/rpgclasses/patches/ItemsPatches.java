package rpgclasses.patches;

import necesse.engine.GlobalData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.Attacker;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.toolItem.ToolItem;
import necesse.inventory.item.toolItem.projectileToolItem.gunProjectileToolItem.SniperProjectileToolItem;
import net.bytebuddy.asm.Advice;
import rpgclasses.registry.RPGModifiers;

public class ItemsPatches {

    @ModMethodPatch(target = Item.class, name = "zoomAmount", arguments = {})
    public static class zoomAmount {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Item This, @Advice.Return(readOnly = false) float zoom) {
            Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
            if (client != null && This instanceof ToolItem && ((ToolItem) (This)).getDamageType(null) == DamageTypeRegistry.RANGED) {
                zoom = client.getPlayer().buffManager.getModifier(RPGModifiers.RANGED_WEAPONS_ZOOM);
            }
        }

    }

    @ModMethodPatch(target = SniperProjectileToolItem.class, name = "zoomAmount", arguments = {})
    public static class zoomAmountSniper {

        @Advice.OnMethodExit
        static void onExit(@Advice.This SniperProjectileToolItem This, @Advice.Return(readOnly = false) float zoom) {
            Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
            if (client != null) {
                zoom = zoom + client.getPlayer().buffManager.getModifier(RPGModifiers.RANGED_WEAPONS_ZOOM);
            }
        }

    }

    @ModMethodPatch(target = ToolItem.class, name = "getKnockback", arguments = {InventoryItem.class, Attacker.class})
    public static class getKnockback {

        @Advice.OnMethodExit
        static void onExit(@Advice.This ToolItem This, @Advice.Argument(0) InventoryItem inventoryItem, @Advice.Argument(1) Attacker attacker) {
            if (This.getDamageType(inventoryItem) == DamageTypeRegistry.RANGED && attacker.getFirstPlayerOwner() != null)
                inventoryItem.getGndData().setFloat("rangedRangeMod", attacker.getFirstPlayerOwner().buffManager.getModifier(RPGModifiers.RANGED_WEAPONS_RANGE));
        }

    }

    @ModMethodPatch(target = ToolItem.class, name = "getAttackRange", arguments = {InventoryItem.class})
    public static class getAttackRange {

        @Advice.OnMethodExit
        static void onExit(@Advice.This ToolItem This, @Advice.Argument(0) InventoryItem inventoryItem, @Advice.Return(readOnly = false) int range) {
            if (This.getDamageType(inventoryItem) == DamageTypeRegistry.RANGED)
                range = (int) (range * inventoryItem.getGndData().getFloat("rangedRangeMod", 1F));
        }

    }

}
