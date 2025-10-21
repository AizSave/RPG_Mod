package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerDataList;

public class EmpoweredHealing extends SimpleBuffPassive {
    public EmpoweredHealing(int levelMax, int requiredClassLevel) {
        super("empoweredhealing", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new EmpoweredHealingBuff();
    }

    public class EmpoweredHealingBuff extends PrincipalPassiveBuff implements AphMagicHealingBuff {
        @Override
        public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            giveSecondaryPassiveBuff((PlayerMob) healer, target, PlayerDataList.getPlayerData((PlayerMob) healer), getLevel(activeBuff), 5000);
        }
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return new SecondaryPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, 0.05F * getLevel(activeBuff));
            }
        };
    }
}
