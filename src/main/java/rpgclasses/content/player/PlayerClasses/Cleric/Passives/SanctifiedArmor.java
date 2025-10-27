package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class SanctifiedArmor extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("2 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public SanctifiedArmor(int levelMax, int requiredClassLevel) {
        super("sanctifiedarmor", "#9999cc", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new SanctifiedArmorBuff();
    }

    public static class SanctifiedArmorBuff extends PrincipalPassiveBuff implements AphMagicHealingBuff {
        @Override
        public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            if (healer.isServer()) {
                int level = getLevel(activeBuff);
                target.addResilience(realHealing * params[0].value(level));
            }
        }
    }
}
