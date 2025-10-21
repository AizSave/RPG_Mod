package rpgclasses.registry;

import necesse.inventory.lootTable.LootTablePresets;
import necesse.inventory.lootTable.lootItem.LootItem;
import necesse.inventory.lootTable.lootItem.LootItemList;

public class RPGLootTables {
    public static void modifyLootTables() {
        LootTablePresets.startChest.items.addAll(
                new LootItemList(
                        new LootItem("ringsbox", 1)
                )
        );
    }
}
