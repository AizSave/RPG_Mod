package rpgclasses.methodpatches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.EquipmentBuffManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.lang.reflect.Field;

public class EquipmentBuffManagerPatches {

    @ModMethodPatch(target = EquipmentBuffManager.class, name = "updateTrinketBuffs", arguments = {})
    public static class updateTrinketBuffs {
        @Advice.OnMethodExit
        static void onExit(@Advice.This EquipmentBuffManager This) {
            try {
                Field ownerField = EquipmentBuffManager.class.getDeclaredField("owner");
                ownerField.setAccessible(true);
                Mob owner = (Mob) ownerField.get(This);
                if (owner.isPlayer) {
                    PlayerMob player = (PlayerMob) owner;
                    PlayerData playerData = PlayerDataList.getPlayerData(player);
                    if(playerData != null) playerData.updateModifiersBuff(player);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
