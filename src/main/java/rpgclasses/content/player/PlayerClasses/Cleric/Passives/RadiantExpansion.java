package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGDamageType;

public class RadiantExpansion extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("5 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public RadiantExpansion(int levelMax, int requiredClassLevel) {
        super("radiantexpansion", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new SanctifiedMindBuff();
    }

    public class SanctifiedMindBuff extends PrincipalPassiveBuff implements AphMagicHealingBuff {
        @Override
        public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            int level = getLevel(activeBuff);
            int damage = (int) (realHealing * params[0].value(level));

            if (damage > 0) {
                AphAreaList areaList = new AphAreaList(
                        new AphArea(100, getColor())
                                .setDamageArea(new GameDamage(RPGDamageType.HOLY, damage))
                );
                areaList.execute(healer, target.x, target.y, true);
            }
        }
    }


    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage"};
    }
}
