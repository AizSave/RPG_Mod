package rpgclasses.items;

import necesse.engine.localization.Localization;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.container.item.ItemInventoryContainer;
import necesse.inventory.item.Item;
import necesse.inventory.item.miscItem.PouchItem;
import rpgclasses.registry.RPGContainers;

public class RingsBox extends PouchItem {
    public RingsBox() {
        this.rarity = Rarity.LEGENDARY;
    }

    @Override
    public ListGameTooltips getTooltips(InventoryItem item, PlayerMob perspective, GameBlackboard blackboard) {
        ListGameTooltips tooltips = super.getTooltips(item, perspective, blackboard);
        tooltips.add(Localization.translate("itemtooltip", "ringsboxtip"));
        tooltips.add(Localization.translate("itemtooltip", "rclickinvopentip"));
        tooltips.add(Localization.translate("itemtooltip", "stored", "items", this.getStoredItemAmounts(item)));
        tooltips.add(Localization.translate("global", "rpgmod"));
        return tooltips;
    }

    @Override
    public boolean isValidPouchItem(InventoryItem inventoryItem) {
        return inventoryItem.item != null && isValidRequestItem(inventoryItem.item);
    }

    @Override
    public boolean isValidRequestItem(Item item) {
        return item instanceof AttributeRing;
    }

    @Override
    public boolean isValidRequestType(Type type) {
        return false;
    }

    @Override
    public int getInternalInventorySize() {
        return 100;
    }

    @Override
    protected void openContainer(ServerClient client, PlayerInventorySlot inventorySlot) {
        PacketOpenContainer p = new PacketOpenContainer(RPGContainers.CUSTOM_ITEM_INVENTORY_CONTAINER, ItemInventoryContainer.getContainerContent(this, inventorySlot));
        ContainerRegistry.openAndSendContainer(client, p);
    }

    @Override
    public int getStackSize() {
        return 1;
    }
}
