package rpgclasses.patches.ThrowingDamage;

import aphorea.items.tools.weapons.melee.dagger.AphDaggerToolItem;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import necesse.inventory.item.toolItem.projectileToolItem.throwToolItem.ThrowToolItem;
import necesse.inventory.item.toolItem.projectileToolItem.throwToolItem.boomerangToolItem.BoomerangToolItem;
import net.bytebuddy.asm.Advice;
import rpgclasses.registry.RPGDamageType;
import rpgclasses.settings.RPGSettings;

import java.lang.reflect.Field;

public class ToolItemPatches {

    @ModConstructorPatch(target = ToolItem.class, arguments = {int.class})
    public static class ExampleConstructorPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.This ToolItem This) {
            if (changeToThrowingDamage(This)) {
                try {
                    Field damageTypeField = ToolItem.class.getDeclaredField("damageType");
                    damageTypeField.setAccessible(true);
                    damageTypeField.set(This, RPGDamageType.THROWING);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @ModMethodPatch(target = ToolItem.class, name = "getTooltips", arguments = {InventoryItem.class, PlayerMob.class, GameBlackboard.class})
    public static class getTooltips {

        @Advice.OnMethodEnter
        static boolean onEnter(@Advice.This ToolItem This) {
            if (changeToThrowingDamage(This)) {
                try {
                    Field damageTypeField = ToolItem.class.getDeclaredField("damageType");
                    damageTypeField.setAccessible(true);
                    damageTypeField.set(This, RPGDamageType.THROWING);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }

        @Advice.OnMethodExit
        static void onExit(@Advice.This ToolItem This, @Advice.Argument(0) InventoryItem inventoryItem, @Advice.Return(readOnly = false) ListGameTooltips tooltips) {
            if (isThrowingDamage(This, inventoryItem) || This instanceof AphDaggerToolItem) {
                tooltips.add(" ");
                tooltips.add(Localization.translate("extraskilldesc", "throwingdamage"));
            }
        }

    }

    public static boolean isThrowingDamage(ToolItem item, InventoryItem inventoryItem) {
        return item.getDamageType(inventoryItem) == RPGDamageType.THROWING && item.getFlatAttackDamage(inventoryItem).damage > 0;
    }

    public static boolean changeToThrowingDamage(ToolItem item) {
        if (item.getDamageType(null) == RPGDamageType.THROWING) return false;
        if (item instanceof ThrowToolItem) {
            return !(item instanceof BoomerangToolItem) || RPGSettings.settingsGetter.getBoolean("boomerangsDealThrowingDamage");
        }
        return false;

    }

}
