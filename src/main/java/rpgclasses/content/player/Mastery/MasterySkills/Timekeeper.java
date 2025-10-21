package rpgclasses.content.player.Mastery.MasterySkills;

import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.PacketModAllSkillsTime;

public class Timekeeper extends Mastery {

    public Timekeeper(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new TimekeeperBuff();
    }

    public static class TimekeeperBuff extends MasteryBuff implements AphMagicHealingBuff {
        @Override
        public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            if (target instanceof PlayerMob && realHealing > 0) {
                PlayerMob playerTarget = (PlayerMob) target;
                PlayerData playerData = PlayerDataList.getPlayerData(playerTarget);
                int mod = -realHealing * 20;
                for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                    if (!equippedActiveSkill.isEmpty()) equippedActiveSkill.modCooldown(mod);
                }
                playerTarget.getServer().network.sendToClientsAtEntireLevel(new PacketModAllSkillsTime(playerTarget.getServerClient().slot, mod), playerTarget.getLevel());
            }
        }
    }
}
