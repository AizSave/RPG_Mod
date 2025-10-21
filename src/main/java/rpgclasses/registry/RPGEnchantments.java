package rpgclasses.registry;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.registries.EnchantmentRegistry;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.inventory.enchants.EquipmentItemEnchant;
import necesse.inventory.enchants.ItemEnchantment;

import java.util.Set;

public class RPGEnchantments {
    public static void registerCore() {

        // EQUIPMENT ENCHANTMENTS
        Set<Integer> list = EnchantmentRegistry.equipmentEnchantments;

        registerEnchantment(list, "evasive", new EquipmentItemEnchant(20,
                new ModifierValue<>(RPGModifiers.DODGE_CHANCE, 0.02F)
        ));

        registerEnchantment(list, "stalking", new EquipmentItemEnchant(20,
                new ModifierValue<>(RPGModifiers.FOCUS_CHANCE, 0.01F),
                new ModifierValue<>(RPGModifiers.FOCUS_DAMAGE, 0.05F)
        ));

        registerEnchantment(list, "regenerative", new EquipmentItemEnchant(20,
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, 0.1F)
        ));

        registerEnchantment(list, "energetic", new EquipmentItemEnchant(20,
                new ModifierValue<>(BuffModifiers.STAMINA_CAPACITY, 0.04F),
                new ModifierValue<>(BuffModifiers.STAMINA_REGEN, 0.04F)
        ));

        registerEnchantment(list, "depleted", new EquipmentItemEnchant(-20,
                new ModifierValue<>(BuffModifiers.STAMINA_CAPACITY, -0.04F),
                new ModifierValue<>(BuffModifiers.STAMINA_REGEN, -0.04F)
        ));
    }

    public static void registerEnchantment(Set<Integer> list, String stringID, ItemEnchantment enchantment) {
        int id = EnchantmentRegistry.registerEnchantment(stringID, enchantment);
        list.add(id);
    }
}
