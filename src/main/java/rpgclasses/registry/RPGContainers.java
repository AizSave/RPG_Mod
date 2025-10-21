package rpgclasses.registry;

import necesse.engine.registries.ContainerRegistry;
import necesse.inventory.container.item.ItemInventoryContainer;
import rpgclasses.forms.customiteminventory.CustomItemInventoryForm;
import rpgclasses.forms.rpgmenu.MenuContainer;
import rpgclasses.forms.rpgmenu.MenuContainerForm;

public class RPGContainers {
    public static int MENU_CONTAINER;
    public static int CUSTOM_ITEM_INVENTORY_CONTAINER;

    public static void registerCore() {
        MENU_CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, content) -> new MenuContainerForm(client, new MenuContainer(client.getClient(), uniqueSeed)),
                (client, uniqueSeed, content, serverObject) -> new MenuContainer(client, uniqueSeed)
        );

        CUSTOM_ITEM_INVENTORY_CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, packet) -> new CustomItemInventoryForm<>(client, new ItemInventoryContainer(client.getClient(), uniqueSeed, packet)),
                (client, uniqueSeed, packet, serverObject) -> new ItemInventoryContainer(client, uniqueSeed, packet)
        );

    }

}
