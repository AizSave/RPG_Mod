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
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;

public class EmpoweredHealing extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("5 x <skilllevel>").setDecimals(2, 0),
            SkillParam.staticParam(5)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

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
            giveSecondaryPassiveBuff((PlayerMob) healer, target, getLevel(activeBuff), params[1].value());
        }
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return new SecondaryPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, params[0].value(getLevel(activeBuff)));
            }
        };
    }
}
