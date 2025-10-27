package rpgclasses.utils;

import aphorea.utils.area.AphArea;
import aphorea.utils.magichealing.AphMagicHealing;
import necesse.engine.network.gameNetworkData.GNDItemMap;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RPGArea extends AphArea {
    public float attackerHealthMod = 1F;
    public Consumer<Mob> onTargetDamaged;
    public Map<String, GNDItemMap> buffsGND = new HashMap<>();

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

    public RPGArea addBuffGND(String buffID, GNDItemMap buffGND) {
        buffsGND.put(buffID, buffGND);
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

    @Override
    public void applyBuffs(Mob attacker, @NotNull Mob target) {
        Arrays.stream(this.buffs).forEach(
                (buffID) -> {
                    ActiveBuff activeBuff = new ActiveBuff(BuffRegistry.getBuff(buffID), target, this.buffDuration, attacker);
                    GNDItemMap buffGND = buffsGND.get(buffID);
                    if (buffGND != null) {
                        activeBuff.getGndData().addAll(buffGND);
                    }

                    target.buffManager.addBuff(activeBuff, true);
                }
        );
    }

    @Override
    public void applyDebuffs(Mob attacker, @NotNull Mob target) {
        Arrays.stream(this.debuffs).forEach(
                (debuffID) -> {
                    ActiveBuff activeBuff = new ActiveBuff(BuffRegistry.getBuff(debuffID), target, this.debuffDuration, attacker);
                    GNDItemMap buffGND = buffsGND.get(debuffID);
                    if (buffGND != null) {
                        activeBuff.getGndData().addAll(buffGND);
                    }

                    target.buffManager.addBuff(activeBuff, true);
                }
        );
    }
}
