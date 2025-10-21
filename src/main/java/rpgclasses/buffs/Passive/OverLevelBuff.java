package rpgclasses.buffs.Passive;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.gameTooltips.ListGameTooltips;

public class OverLevelBuff extends PassiveBuff {
    public OverLevelBuff() {
        this.isVisible = true;
        this.isImportant = true;
    }

    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        new ModifierValue<>(BuffModifiers.SLOW, 1.0F).min(1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.SPEED, -1.0F).max(-1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.ALL_DAMAGE, -1.0F).max(-1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.ARMOR, -1.0F).max(-1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.MAX_HEALTH, 0F).max(1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.HEALTH_REGEN, -1.0F).max(-1.0F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN, -1.0F).max(-1.0F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.INCOMING_DAMAGE_MOD, 1.0F).min(1.0F).apply(activeBuff);

        activeBuff.addModifier(BuffModifiers.PARALYZED, true);
        activeBuff.addModifier(BuffModifiers.INTIMIDATED, true);
    }

    @Override
    public ListGameTooltips getTooltip(ActiveBuff ab, GameBlackboard blackboard) {
        ListGameTooltips tooltips = super.getTooltip(ab, blackboard);
        tooltips.add(Localization.translate("buffdesc", "overlevelbuff1"));
        tooltips.add(Localization.translate("buffdesc", "overlevelbuff2"));
        return tooltips;
    }
}
