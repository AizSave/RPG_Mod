package rpgclasses.utils;

import aphorea.utils.area.AphArea;
import aphorea.utils.magichealing.AphMagicHealing;
import necesse.entity.mobs.Mob;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;

public class RPGArea extends AphArea {
    public float attackerHealthMod = 1F;
    public Consumer<Mob> onTargetDamaged;

    public RPGArea(float range, Color... colors) {
        super(range, colors);
    }

    public RPGArea(float range, float alpha, Color... colors) {
        super(range, alpha, colors);
    }

    public RPGArea setAttackerHealthMod(float attackerHealthMod) {
        this.attackerHealthMod = attackerHealthMod;
        return this;
    }

    public RPGArea addOnTargetDamaged(Consumer<Mob> onTargetDamaged) {
        this.onTargetDamaged = onTargetDamaged;
        return this;
    }

    @Override
    public void applyHealth(Mob attacker, @NotNull Mob target, InventoryItem item, ToolItem toolItem) {
        int healing = this.areaHealing;
        if (target == attacker) healing = (int) (healing * attackerHealthMod);

        if (this.directExecuteHealing) {
            AphMagicHealing.healMobExecute(attacker, target, healing, item, toolItem);
        } else {
            AphMagicHealing.healMob(attacker, target, healing, item, toolItem);
        }
    }

    @Override
    public void applyDamage(Mob attacker, @NotNull Mob target) {
        super.applyDamage(attacker, target);
        onTargetDamaged.accept(target);
    }
}
