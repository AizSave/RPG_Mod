package rpgclasses.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.BuffManager;
import net.bytebuddy.asm.Advice;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.lang.reflect.Field;

public class BuffManagerPatches {

    @ModMethodPatch(target = BuffManager.class, name = "clearBuffs", arguments = {})
    public static class clearBuffs {
        @Advice.OnMethodExit
        static void onExit(@Advice.This BuffManager This) {
            try {
                Field ownerField = BuffManager.class.getDeclaredField("owner");
                ownerField.setAccessible(true);
                Mob owner = (Mob) ownerField.get(This);
                if (owner.isPlayer) {
                    PlayerMob player = (PlayerMob) owner;
                    PlayerData playerData = PlayerDataList.getPlayerData(player);
                    if (playerData != null) playerData.updateAllBuffs(player);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
