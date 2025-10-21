package rpgclasses.registry;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.staticBuffs.armorBuffs.trinketBuffs.SimpleTrinketBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.armorItem.ArmorModifiers;
import necesse.inventory.item.armorItem.ninja.NinjaHoodArmorItem;
import necesse.inventory.item.armorItem.ninja.NinjaRobeArmorItem;
import necesse.inventory.item.armorItem.ninja.NinjaShoesArmorItem;
import rpgclasses.content.player.Attribute;
import rpgclasses.items.AttributeRing;
import rpgclasses.items.RingsBox;
import rpgclasses.items.ScrollOfOblivion;

import java.util.ArrayList;

public class RPGItems {
    public static ArrayList<RingQuality> RING_QUALITIES = new ArrayList<>();

    static {
        addRingQuality(RingQuality.defaultDrop(
                "wood", 2, 50, 200, Item.Rarity.COMMON,
                1, 11, 0.04F
        ));
        addRingQuality(RingQuality.defaultDrop(
                "copper", 4, 50, 200, Item.Rarity.COMMON,
                6, 17, 0.02F
        ));
        addRingQuality(RingQuality.defaultDrop(
                "iron", 6, 100, 600, Item.Rarity.UNCOMMON,
                12, 23, 0.01F
        ));
        addRingQuality(RingQuality.defaultDrop(
                "gold", 8, 200, 1000, Item.Rarity.RARE,
                18, Integer.MAX_VALUE, 0.005F
        ));
    }

    public static void addRingQuality(RingQuality ringQuality) {
        RING_QUALITIES.add(ringQuality);
    }

    public static void registerCore() {
        ItemRegistry.registerItem("scrollofoblivion", new ScrollOfOblivion(), 100, true);
        ItemRegistry.registerItem("ringsbox", new RingsBox(), -1F, true);

        for (Attribute attribute : Attribute.attributesList) {
            ArrayList<String> prev = new ArrayList<>();

            for (RingQuality ringQuality : RING_QUALITIES) {
                String itemID = attribute.stringID + ringQuality.stringID + "ring";
                String buffID = itemID + "buff";

                ItemRegistry.registerItem(itemID,
                        new AttributeRing(ringQuality.rarity, buffID, ringQuality.enchantCost)
                                .addDisables(prev.toArray(new String[0])),
                        ringQuality.brokerValue, true);

                BuffRegistry.registerBuff(buffID, new SimpleTrinketBuff(
                        new ModifierValue<>(attribute.ownModifier, ringQuality.points)
                ));

                prev.add(itemID);
            }
        }

        itemReworks();
    }

    public static void itemReworks() {

        // Ninja Armor

        ItemRegistry.replaceItem("ninjahood", new NinjaHoodArmorItem() {
            @Override
            public ArmorModifiers getArmorModifiers(InventoryItem item, Mob mob) {
                ArmorModifiers modifiers = super.getArmorModifiers(item, mob);
                modifiers.addModifier(RPGModifiers.THROWING_CRIT_DAMAGE, 0.25F);
                return modifiers;
            }
        }, 120.0F, true);

        ItemRegistry.replaceItem("ninjarobe", new NinjaRobeArmorItem() {
            @Override
            public ArmorModifiers getArmorModifiers(InventoryItem item, Mob mob) {
                ArmorModifiers modifiers = super.getArmorModifiers(item, mob);
                modifiers.addModifier(RPGModifiers.THROWING_CRIT_CHANCE, 0.1F);
                return modifiers;
            }
        }, 200.0F, true);

        ItemRegistry.replaceItem("ninjashoes", new NinjaShoesArmorItem() {
            @Override
            public ArmorModifiers getArmorModifiers(InventoryItem item, Mob mob) {
                ArmorModifiers modifiers = super.getArmorModifiers(item, mob);
                modifiers.addModifier(RPGModifiers.DODGE_CHANCE, 0.05F);
                return modifiers;
            }
        }, 80.0F, true);

    }

    public static class RingQuality {
        public String stringID;
        public int points;
        public float brokerValue;
        public int enchantCost;
        public Item.Rarity rarity;
        public boolean defaultDrop;
        public int minLevel;
        public int maxLevel;
        public float chance;

        private RingQuality(String stringID, int points, float brokerValue, int enchantCost, Item.Rarity rarity, boolean defaultDrop, int minLevel, int maxLevel, float chance) {
            this.stringID = stringID;
            this.points = points;
            this.brokerValue = brokerValue;
            this.enchantCost = enchantCost;
            this.rarity = rarity;
            this.defaultDrop = defaultDrop;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.chance = chance;
        }

        public static RingQuality defaultDrop(String stringID, int points, float brokerValue, int enchantCost, Item.Rarity rarity, int minLevel, int maxLevel, float chance) {
            return new RingQuality(stringID, points, brokerValue, enchantCost, rarity, true, minLevel, maxLevel, chance);
        }

        public static RingQuality customDrop(String stringID, int points, float brokerValue, int enchantCost, Item.Rarity rarity) {
            return new RingQuality(stringID, points, brokerValue, enchantCost, rarity, false, 0, 0, 0);
        }
    }

}
