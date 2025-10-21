package rpgclasses.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.lootTable.lootItem.ChanceLootItem;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;
import rpgclasses.content.player.Attribute;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGItems;

public class LevelPatches {

    @ModMethodPatch(target = Level.class, name = "getExtraMobDrops", arguments = {Mob.class})
    public static class LevelExtraDrops {
        @Advice.OnMethodExit
        static void onExit(@Advice.This Level This, @Advice.Argument(0) Mob mob, @Advice.Return(readOnly = false) LootTable lootTable) {
            MobData mobData = MobData.getMob(mob);
            if (mobData != null) {
                lootTable.items.add(
                        new ChanceLootItem(0.0001F * mobData.level, "scrollofoblivion")
                );
                for (RPGItems.RingQuality ringQuality : RPGItems.RING_QUALITIES) {
                    if (ringQuality.defaultDrop && ringQuality.minLevel <= mobData.level && ringQuality.maxLevel >= mobData.level) {
                        lootTable.items.add(
                                new ChanceLootItem(ringQuality.chance, Attribute.getRandom().stringID + ringQuality.stringID + "ring")
                        );
                    }
                }
            }
        }
    }

}
